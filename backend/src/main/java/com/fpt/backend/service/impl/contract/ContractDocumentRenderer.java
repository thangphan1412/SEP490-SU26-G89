package com.fpt.backend.service.impl.contract;

import com.fpt.backend.entity.ContractStatusHistory;
import com.fpt.backend.entity.ContractWorkflowStepInstance;
import com.fpt.backend.entity.Contracts;
import com.fpt.backend.entity.ProjectMember;
import com.fpt.backend.entity.Users;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class ContractDocumentRenderer {
    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile(
            "\\{\\{\\s*([a-zA-Z][a-zA-Z0-9_]*)\\s*}}"
    );
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final Set<String> PARTY_A_ROLES = Set.of(
            "CEO",
            "DIRECTOR"
    );
    private static final Set<String> PARTY_B_ROLES = Set.of(
            "PARTNER",
            "EXTERNAL",
            "EXTERNAL_PARTNER",
            "EXTERNAL_PARTNERS",
            "EXTERNALPARTNER",
            "EXTERNALPARTNERS",
            "EXTERNALPARNERS"
    );

    public RenderedDocument render(
            Contracts contract,
            List<ContractStatusHistory> history,
            Map<String, String> attributeValues
    ) {
        return render(contract, history, attributeValues, false);
    }

    public RenderedDocument renderForSigning(
            Contracts contract, List<ContractStatusHistory> history, Map<String, String> attributeValues
    ) {
        return render(contract, history, attributeValues, true);
    }

    private RenderedDocument render(
            Contracts contract, List<ContractStatusHistory> history,
            Map<String, String> attributeValues, boolean signingPdf
    ) {
        SignatureInformation director = findSignature(history, "SIGN_DIRECTOR");
        SignatureInformation partner = findSignature(history, "SIGN_PARTNER");
        List<SignatureInformation> workflowSignatures = findWorkflowSignatures(
                history
        );
        if (director == null && !workflowSignatures.isEmpty()) {
            // Đã đổi .get(0) thành .getFirst() để loại bỏ cảnh báo
            director = workflowSignatures.getFirst();
        }
        if (partner == null && workflowSignatures.size() > 1) {
            partner = workflowSignatures.get(1);
        }
        Map<String, String> values = createPlaceholderValues(
                contract,
                attributeValues,
                director,
                partner
        );
        // A PDF is frozen before signing. Do not bake a stale "unsigned" status into it.
        if (signingPdf) {
            if (director == null) {
                values.put("director_name", values.get("party_a_name"));
                values.put("director_signature", "(Vị trí chữ ký điện tử)");
            }
            if (partner == null) {
                values.put("partner_name", values.get("party_b_name"));
                values.put("partner_signature", "(Vị trí chữ ký điện tử)");
            }
        }

        return new RenderedDocument(
                replacePlaceholders(contract.getContractContent(), values),
                director == null ? null : director.actorName(),
                director == null ? null : director.signedAt(),
                partner == null ? null : partner.actorName(),
                partner == null ? null : partner.signedAt()
        );
    }

    private Map<String, String> createPlaceholderValues(
            Contracts contract,
            Map<String, String> attributeValues,
            SignatureInformation director,
            SignatureInformation partner
    ) {
        Map<String, String> values = new LinkedHashMap<>();
        if (attributeValues != null) {
            attributeValues.forEach((key, value) -> values.put(
                    normalizeKey(key),
                    formatAttributeValue(key, value)
            ));
        }

        values.put("contract_number", displayValue(contract.getContractNumber()));
        values.put("contract_title", displayValue(contract.getContractTitle()));
        values.put("contract_status", displayValue(contract.getContractStatus()));
        values.put("contract_created_by", displayValue(contract.getContractCreateBy()));
        values.put(
                "contract_date",
                contract.getContractCreatedAt() == null
                        ? "Chưa cập nhật"
                        : formatDate(contract.getContractCreatedAt().toLocalDate())
        );
        values.put("effective_date", formatDate(contract.getEffectiveDate()));
        values.put("expiration_date", formatDate(contract.getExpirationDate()));
        values.put(
                "project_name",
                contract.getProject() == null
                        ? "Không thuộc dự án"
                        : displayValue(contract.getProject().getProjectName())
        );
        values.put(
                "contract_type_code",
                contract.getContractType() == null
                        ? "Chưa cập nhật"
                        : displayValue(contract.getContractType().getContractTypeCode())
        );
        values.put(
                "contract_type_name",
                contract.getContractType() == null
                        ? "Chưa cập nhật"
                        : displayValue(contract.getContractType().getContractTypeName())
        );

        PartyInformation partyA = resolveParty(
                contract,
                director == null ? null : director.actorName(),
                PARTY_A_ROLES,
                0
        );
        PartyInformation partyB = resolveParty(
                contract,
                partner == null ? null : partner.actorName(),
                PARTY_B_ROLES,
                1
        );
        putPartyValues(values, "party_a", partyA);
        putPartyValues(values, "party_b", partyB);
        values.put("director_name", signerName(director));
        values.put("director_signature", signatureText(director));
        values.put("partner_name", signerName(partner));
        values.put("partner_signature", signatureText(partner));
        return values;
    }

    private void putPartyValues(
            Map<String, String> values,
            String prefix,
            PartyInformation party
    ) {
        values.put(prefix + "_name", displayValue(party.fullName()));
        values.put(prefix + "_role", displayValue(party.role()));
        values.put(prefix + "_email", displayValue(party.email()));
        values.put(prefix + "_phone", displayValue(party.phoneNumber()));
        values.put(prefix + "_date_of_birth", displayValue(party.dateOfBirth()));
        values.put(prefix + "_department", displayValue(party.departmentName()));
        values.put(prefix + "_company", displayValue(party.companyName()));
    }

    private PartyInformation resolveParty(
            Contracts contract,
            String signerName,
            Set<String> acceptedRoles,
            int signingOrder
    ) {
        List<Users> candidates = new ArrayList<>();
        if (contract.getProject() != null
                && contract.getProject().getProjectMembers() != null) {
            candidates.addAll(contract.getProject().getProjectMembers().stream()
                    .map(ProjectMember::getUser)
                    .filter(Objects::nonNull)
                    .toList());
        }
        if (contract.getWorkflowStepInstances() != null) {
            candidates.addAll(contract.getWorkflowStepInstances().stream()
                    .map(ContractWorkflowStepInstance::getAssignedUser)
                    .filter(Objects::nonNull)
                    .toList());
        }
        if (contract.getContractCreatedByUser() != null) {
            candidates.add(contract.getContractCreatedByUser());
        }
        candidates = candidates.stream().distinct().toList();

        Users user = candidates.stream()
                .filter(candidate -> sameName(userDisplayName(candidate), signerName))
                .findFirst()
                .orElseGet(() -> resolveWorkflowSigner(
                        contract,
                        acceptedRoles,
                        signingOrder
                ));
        if (user == null) {
            user = candidates.stream()
                    .filter(candidate -> hasAcceptedRole(candidate, acceptedRoles))
                    .findFirst()
                    .orElse(null);
        }

        if (user == null) {
            return new PartyInformation(
                    signerName,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
            );
        }

        String departmentName = user.getDepartment() == null
                ? null
                : user.getDepartment().getDepartmentName();
        String companyName;
        if (user.getCompany() != null) {
            companyName = user.getCompany().getCompanyName();
        } else if (user.getDepartment() != null
                && user.getDepartment().getCompany() != null) {
            companyName = user.getDepartment().getCompany().getCompanyName();
        } else {
            companyName = null;
        }

        return new PartyInformation(
                userDisplayName(user),
                displayRole(user, acceptedRoles),
                user.getEmail(),
                user.getNumberPhone(),
                user.getDob(),
                departmentName,
                companyName
        );
    }

    private Users resolveWorkflowSigner(
            Contracts contract,
            Set<String> acceptedRoles,
            int signingOrder
    ) {
        if (contract.getWorkflowStepInstances() == null) {
            return null;
        }

        List<ContractWorkflowStepInstance> signingSteps = contract
                .getWorkflowStepInstances()
                .stream()
                .filter(Objects::nonNull)
                .filter(step -> step.getActionType() != null
                        && step.getActionType().requiresSignature())
                .filter(step -> step.getAssignedUser() != null)
                .sorted(Comparator.comparing(
                        ContractWorkflowStepInstance::getStepOrder
                ))
                .toList();

        Users roleMatch = signingSteps.stream()
                .filter(step -> acceptedRoles.contains(
                        normalizeRole(step.getRequiredRoleCode())
                ))
                .map(ContractWorkflowStepInstance::getAssignedUser)
                .findFirst()
                .orElse(null);
        if (roleMatch != null) {
            return roleMatch;
        }
        return signingOrder >= 0 && signingOrder < signingSteps.size()
                ? signingSteps.get(signingOrder).getAssignedUser()
                : null;
    }

    private boolean hasAcceptedRole(Users user, Set<String> acceptedRoles) {
        return user != null
                && user.getUserRoles() != null
                && user.getUserRoles().stream()
                .filter(Objects::nonNull)
                .map(userRole -> userRole.getRole())
                .filter(Objects::nonNull)
                .anyMatch(role -> acceptedRoles.contains(
                        normalizeRole(role.getRoleCode())
                ) || acceptedRoles.contains(
                        normalizeRole(role.getRoleName())
                ));
    }

    private String displayRole(Users user, Set<String> acceptedRoles) {
        if (user == null || user.getUserRoles() == null) {
            return null;
        }

        return user.getUserRoles().stream()
                .filter(Objects::nonNull)
                .map(userRole -> userRole.getRole())
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(
                        role -> hasText(role.getRoleCode())
                                && acceptedRoles.contains(
                                normalizeRole(role.getRoleCode())
                        ) ? 0 : 1
                ))
                .map(role -> hasText(role.getRoleName())
                        ? role.getRoleName()
                        : role.getRoleCode())
                .filter(this::hasText)
                .findFirst()
                .orElse(null);
    }

    private String userDisplayName(Users user) {
        if (user == null) {
            return null;
        }

        String firstName = user.getFirstName() == null
                ? ""
                : user.getFirstName().trim();
        String lastName = user.getLastName() == null
                ? ""
                : user.getLastName().trim();
        String fullName = (firstName + " " + lastName).trim();
        return fullName.isBlank() ? user.getEmail() : fullName;
    }

    private boolean sameName(String first, String second) {
        return first != null
                && !first.isBlank()
                && second != null
                && !second.isBlank()
                && first.trim().equalsIgnoreCase(second.trim());
    }

    private String normalizeRole(String role) {
        return role == null
                ? ""
                : role.trim()
                .toUpperCase(Locale.ROOT)
                .replace('-', '_')
                .replace(' ', '_');
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private String replacePlaceholders(
            String templateContent,
            Map<String, String> values
    ) {
        if (templateContent == null || templateContent.isBlank()) {
            return "Không có nội dung hợp đồng.";
        }

        Matcher matcher = PLACEHOLDER_PATTERN.matcher(templateContent);
        // Đã đổi StringBuffer thành StringBuilder để tối ưu code
        StringBuilder rendered = new StringBuilder();
        while (matcher.find()) {
            String replacement = values.getOrDefault(
                    normalizeKey(matcher.group(1)),
                    "Chưa cập nhật"
            );
            matcher.appendReplacement(
                    rendered,
                    Matcher.quoteReplacement(displayValue(replacement))
            );
        }
        matcher.appendTail(rendered);
        return rendered.toString().trim();
    }

    private SignatureInformation findSignature(
            List<ContractStatusHistory> history,
            String action
    ) {
        if (history == null) {
            return null;
        }

        return history.stream()
                .filter(item -> action.equalsIgnoreCase(item.getAction()))
                .filter(item -> Boolean.TRUE.equals(item.getSignerAgeVerified()))
                .filter(item -> item.getActorName() != null
                        && !item.getActorName().isBlank())
                .map(item -> new SignatureInformation(
                        item.getActorName().trim(),
                        item.getChangedAt()
                ))
                .findFirst()
                .orElse(null);
    }

    private List<SignatureInformation> findWorkflowSignatures(
            List<ContractStatusHistory> history
    ) {
        if (history == null) {
            return List.of();
        }

        return history.stream()
                .filter(item -> "SIGN".equalsIgnoreCase(item.getAction())
                        || "APPROVE_AND_SIGN".equalsIgnoreCase(
                        item.getAction()
                ))
                .filter(item -> Boolean.TRUE.equals(
                        item.getSignerAgeVerified()
                ))
                .filter(item -> item.getActorName() != null
                        && !item.getActorName().isBlank())
                .sorted(java.util.Comparator.comparing(
                        ContractStatusHistory::getChangedAt
                ))
                .map(item -> new SignatureInformation(
                        item.getActorName().trim(),
                        item.getChangedAt()
                ))
                .toList();
    }

    private String signerName(SignatureInformation signature) {
        return signature == null ? "Chưa ký" : signature.actorName();
    }

    private String signatureText(SignatureInformation signature) {
        if (signature == null) {
            return "Chưa ký";
        }

        return "Đã ký điện tử bởi " + signature.actorName()
                + " lúc " + formatDateTime(signature.signedAt());
    }

    private String formatAttributeValue(String key, String value) {
        if ("contract_value".equals(normalizeKey(key))) {
            return formatMoney(value);
        }
        return displayValue(value);
    }

    private String formatMoney(String value) {
        if (value == null || value.isBlank()) {
            return "Chưa cập nhật";
        }

        try {
            BigDecimal amount = new BigDecimal(value.trim());
            DecimalFormatSymbols symbols = new DecimalFormatSymbols(
                    Locale.forLanguageTag("vi-VN")
            );
            symbols.setGroupingSeparator('.');
            symbols.setDecimalSeparator(',');
            DecimalFormat formatter = new DecimalFormat("#,##0.##", symbols);
            return formatter.format(amount) + " VNĐ";
        } catch (NumberFormatException exception) {
            return value.trim();
        }
    }

    private String formatDate(LocalDate value) {
        return value == null ? "Chưa cập nhật" : DATE_FORMATTER.format(value);
    }

    private String formatDateTime(LocalDateTime value) {
        return value == null ? "Chưa cập nhật" : DATE_TIME_FORMATTER.format(value);
    }

    private String displayValue(String value) {
        return value == null || value.isBlank() ? "Chưa cập nhật" : value.trim();
    }

    private String normalizeKey(String key) {
        return key == null ? "" : key.trim().toLowerCase(Locale.ROOT);
    }

    private record SignatureInformation(
            String actorName,
            LocalDateTime signedAt
    ) {
    }

    private record PartyInformation(
            String fullName,
            String role,
            String email,
            String phoneNumber,
            String dateOfBirth,
            String departmentName,
            String companyName
    ) {
    }

    public record RenderedDocument(
            String content,
            String directorSignerName,
            LocalDateTime directorSignedAt,
            String partnerSignerName,
            LocalDateTime partnerSignedAt
    ) {
        public boolean fullySigned() {
            return directorSignerName != null && partnerSignerName != null;
        }
    }
}

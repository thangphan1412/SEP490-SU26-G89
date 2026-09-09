package com.fpt.backend.service.impl.contract;

import com.fpt.backend.entity.ContractStatusHistory;
import com.fpt.backend.entity.Contracts;
import com.fpt.backend.entity.Signature;
import com.fpt.backend.enums.SignatureStatus;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
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

    public RenderedDocument render(
            Contracts contract,
            List<ContractStatusHistory> history,
            Map<String, String> attributeValues
    ) {
        return render(contract, history, attributeValues, List.of());
    }

    public RenderedDocument render(
            Contracts contract,
            List<ContractStatusHistory> history,
            Map<String, String> attributeValues,
            List<Signature> signatures
    ) {
        List<SignatureInformation> persistedSignatures = findPersistedSignatures(
                signatures
        );
        SignatureInformation director = findByRole(
                persistedSignatures,
                "DIRECTOR", "CEO", "INTERNAL"
        );
        SignatureInformation partner = findByRole(
                persistedSignatures,
                "PARTNER", "EXTERNAL"
        );
        if (director == null && !persistedSignatures.isEmpty()) {
            director = persistedSignatures.getFirst();
        }
        if (partner == null) {
            SignatureInformation selectedDirector = director;
            partner = persistedSignatures.stream()
                    .filter(signature -> signature != selectedDirector)
                    .findFirst()
                    .orElse(null);
        }
        if (director == null) {
            director = findSignature(history, "SIGN_DIRECTOR");
        }
        if (partner == null) {
            partner = findSignature(history, "SIGN_PARTNER");
        }
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

        return new RenderedDocument(
                replacePlaceholders(contract.getContractContent(), values),
                director == null ? null : director.actorName(),
                director == null ? null : director.signedAt(),
                director == null ? null : director.imagePath(),
                director == null ? null : director.documentHash(),
                partner == null ? null : partner.actorName(),
                partner == null ? null : partner.signedAt(),
                partner == null ? null : partner.imagePath(),
                partner == null ? null : partner.documentHash(),
                Map.copyOf(values)
        );
    }

    private List<SignatureInformation> findPersistedSignatures(
            List<Signature> signatures
    ) {
        if (signatures == null) {
            return List.of();
        }
        return signatures.stream()
                .filter(signature -> signature.getStatus() == SignatureStatus.SIGNED)
                .filter(signature -> signature.getSignedBy() != null)
                .map(signature -> new SignatureInformation(
                        userDisplayName(signature),
                        signature.getSignedAt(),
                        signature.getSignerRole(),
                        signature.getFileStorage() == null
                                ? null
                                : signature.getFileStorage().getFilePath(),
                        signature.getDocumentHash()
                ))
                .toList();
    }

    private SignatureInformation findByRole(
            List<SignatureInformation> signatures,
            String... roleParts
    ) {
        return signatures.stream()
                .filter(signature -> {
                    String role = normalizeKey(signature.signerRole());
                    return java.util.Arrays.stream(roleParts)
                            .map(this::normalizeKey)
                            .anyMatch(role::contains);
                })
                .findFirst()
                .orElse(null);
    }

    private String userDisplayName(Signature signature) {
        String firstName = signature.getSignedBy().getFirstName() == null
                ? ""
                : signature.getSignedBy().getFirstName().trim();
        String lastName = signature.getSignedBy().getLastName() == null
                ? ""
                : signature.getSignedBy().getLastName().trim();
        String fullName = (firstName + " " + lastName).trim();
        return fullName.isBlank()
                ? signature.getSignedBy().getEmail()
                : fullName;
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
        values.put("effective_date", formatDate(contract.getEffectiveDate()));
        values.put("expiration_date", formatDate(contract.getExpirationDate()));
        values.put("contract_date", formatContractDate(contract));
        values.put(
                "project_name",
                contract.getProject() == null
                        ? "Chưa cập nhật"
                        : displayValue(contract.getProject().getProjectName())
        );
        values.put("director_name", signerName(director));
        values.put("director_signature", signatureText(director));
        values.put("partner_name", signerName(partner));
        values.put("partner_signature", signatureText(partner));
        return values;
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
                        item.getChangedAt(),
                        null,
                        null,
                        null
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
                .filter(item -> "SIGN".equalsIgnoreCase(item.getAction()))
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
                        item.getChangedAt(),
                        null,
                        null,
                        null
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

    private String formatContractDate(Contracts contract) {
        LocalDate date = contract.getContractCreatedAt() == null
                ? LocalDate.now()
                : contract.getContractCreatedAt().toLocalDate();
        return "ngày " + date.getDayOfMonth()
                + " tháng " + date.getMonthValue()
                + " năm " + date.getYear();
    }

    private String displayValue(String value) {
        return value == null || value.isBlank() ? "Chưa cập nhật" : value.trim();
    }

    private String normalizeKey(String key) {
        return key == null ? "" : key.trim().toLowerCase(Locale.ROOT);
    }

    private record SignatureInformation(
            String actorName,
            LocalDateTime signedAt,
            String signerRole,
            String imagePath,
            String documentHash
    ) {
    }

    public record RenderedDocument(
            String content,
            String directorSignerName,
            LocalDateTime directorSignedAt,
            String directorSignatureImagePath,
            String directorDocumentHash,
            String partnerSignerName,
            LocalDateTime partnerSignedAt,
            String partnerSignatureImagePath,
            String partnerDocumentHash,
            Map<String, String> placeholderValues
    ) {
        public boolean fullySigned() {
            return directorSignerName != null && partnerSignerName != null;
        }
    }
}

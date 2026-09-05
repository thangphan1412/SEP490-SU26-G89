package com.fpt.backend.service.impl.contract;

import com.fpt.backend.entity.ContractStatusHistory;
import com.fpt.backend.entity.Contracts;
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
        values.put("effective_date", formatDate(contract.getEffectiveDate()));
        values.put("expiration_date", formatDate(contract.getExpirationDate()));
        values.put(
                "project_name",
                contract.getProject() == null
                        ? "Không thuộc dự án"
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

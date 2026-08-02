package com.fpt.backend.service.impl;

import com.fpt.backend.dto.request.contract.ContractPositionRequest;
import com.fpt.backend.dto.request.contract.ContractTemplateLayout;
import com.fpt.backend.dto.response.contract.ContractPositionResponse;
import com.fpt.backend.entity.ContractPositions;
import com.fpt.backend.entity.ContractTemplateVersions;
import com.fpt.backend.exception.BadHttpException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class ContractTemplateLayoutMapper {
    private static final int DEFAULT_PAGE_COUNT = 1;
    private static final int MAX_PAGE_COUNT = 50;
    private static final String COORDINATE_SYSTEM = "NORMALIZED";
    private static final Pattern ATTRIBUTE_KEY_PATTERN = Pattern.compile(
            "^[a-z][a-z0-9_]{1,79}$"
    );
    private static final Set<String> FIELD_TYPES = Set.of(
            "TEXT",
            "DATE",
            "SIGNATURE",
            "CHECKBOX"
    );
    private static final Set<String> VALUE_SOURCES = Set.of(
            "CONTRACT",
            "CURRENT_SIGNER",
            "MANUAL"
    );
    private static final Set<String> SIGNER_ROLES = Set.of(
            "DIRECTOR",
            "PARTNER"
    );

    private final ObjectMapper objectMapper;

    public ContractTemplateLayout normalize(
            Integer requestedPageCount,
            List<ContractPositionRequest> requestedPositions,
            String layoutJson
    ) {
        if (requestedPositions != null) {
            return validateLayout(requestedPageCount, requestedPositions);
        }

        if (layoutJson == null || layoutJson.isBlank()) {
            return validateLayout(requestedPageCount, List.of());
        }

        ContractTemplateLayout parsedLayout;
        try {
            parsedLayout = objectMapper.readValue(
                    layoutJson,
                    ContractTemplateLayout.class
            );
        } catch (JacksonException exception) {
            throw new BadHttpException(
                    "Template layout must be valid JSON with normalized field positions"
            );
        }

        if (parsedLayout == null) {
            throw new BadHttpException("Template layout information is required");
        }

        Integer pageCount = requestedPageCount != null
                ? requestedPageCount
                : parsedLayout.pageCount();
        return validateLayout(pageCount, parsedLayout.fields());
    }

    public ContractTemplateLayout fromVersion(ContractTemplateVersions version) {
        List<ContractPositions> savedPositions = version.getPositions();
        if (savedPositions != null && !savedPositions.isEmpty()) {
            return validateLayout(
                    version.getPageCount(),
                    savedPositions.stream()
                            .map(this::toRequest)
                            .toList()
            );
        }

        return normalize(
                version.getPageCount(),
                null,
                version.getLayoutJson()
        );
    }

    public void applyToVersion(
            ContractTemplateVersions version,
            ContractTemplateLayout layout
    ) {
        ContractTemplateLayout normalized = validateLayout(
                layout.pageCount(),
                layout.fields()
        );
        LocalDateTime now = LocalDateTime.now();
        List<ContractPositions> positions = new ArrayList<>();

        for (ContractPositionRequest field : normalized.fields()) {
            ContractPositions position = new ContractPositions();
            position.setContractTemplateVersion(version);
            position.setAttributeKey(field.attributeKey());
            position.setFieldLabel(field.fieldLabel());
            position.setPageNumber(field.pageNumber());
            position.setXPosition(field.xPosition());
            position.setYPosition(field.yPosition());
            position.setWidth(field.width());
            position.setHeight(field.height());
            position.setFieldType(field.fieldType());
            position.setValueSource(field.valueSource());
            position.setSignerRole(field.signerRole());
            position.setIsSystemField(field.systemField());
            position.setIsRequired(field.required());
            position.setCreatedAt(now);
            position.setUpdatedAt(now);
            positions.add(position);
        }

        version.setPageCount(normalized.pageCount());
        version.setLayoutJson(toJson(normalized));
        version.setPositions(positions);
    }

    public List<ContractPositionResponse> toResponses(
            ContractTemplateVersions version
    ) {
        List<ContractPositions> positions = version.getPositions();
        if (positions == null || positions.isEmpty()) {
            return List.of();
        }

        return positions.stream()
                .map(position -> new ContractPositionResponse(
                        position.getId(),
                        position.getAttributeKey(),
                        position.getFieldLabel(),
                        position.getPageNumber(),
                        position.getXPosition(),
                        position.getYPosition(),
                        position.getWidth(),
                        position.getHeight(),
                        position.getFieldType(),
                        position.getValueSource(),
                        position.getSignerRole(),
                        position.getIsSystemField(),
                        position.getIsRequired()
                ))
                .toList();
    }

    public String toJson(ContractTemplateLayout layout) {
        try {
            return objectMapper.writeValueAsString(layout);
        } catch (JacksonException exception) {
            throw new BadHttpException("Unable to serialize template layout");
        }
    }

    private ContractTemplateLayout validateLayout(
            Integer requestedPageCount,
            List<ContractPositionRequest> requestedPositions
    ) {
        int pageCount = requestedPageCount == null
                ? DEFAULT_PAGE_COUNT
                : requestedPageCount;
        if (pageCount < 1 || pageCount > MAX_PAGE_COUNT) {
            throw new BadHttpException(
                    "Template page count must be between 1 and " + MAX_PAGE_COUNT
            );
        }

        List<ContractPositionRequest> normalizedPositions =
                requestedPositions == null
                        ? List.of()
                        : requestedPositions.stream()
                        .map(position -> normalizePosition(position, pageCount))
                        .toList();

        return new ContractTemplateLayout(
                pageCount,
                COORDINATE_SYSTEM,
                normalizedPositions
        );
    }

    private ContractPositionRequest normalizePosition(
            ContractPositionRequest position,
            int pageCount
    ) {
        if (position == null) {
            throw new BadHttpException("Template position information is required");
        }

        String attributeKey = requireText(
                position.attributeKey(),
                "Position attribute key is required"
        ).toLowerCase(Locale.ROOT);
        if (!ATTRIBUTE_KEY_PATTERN.matcher(attributeKey).matches()) {
            throw new BadHttpException(
                    "Position attribute key must use lowercase letters, numbers and underscores"
            );
        }

        String fieldLabel = requireText(
                position.fieldLabel(),
                "Position field label is required"
        );
        int pageNumber = position.pageNumber() == null
                ? 1
                : position.pageNumber();
        if (pageNumber < 1 || pageNumber > pageCount) {
            throw new BadHttpException(
                    "Position page number must be inside the template page range"
            );
        }

        double x = requireCoordinate(position.xPosition(), "x position");
        double y = requireCoordinate(position.yPosition(), "y position");
        double width = requireSize(position.width(), "width");
        double height = requireSize(position.height(), "height");
        if (x + width > 1.000001 || y + height > 1.000001) {
            throw new BadHttpException(
                    "Template position must stay inside the normalized page bounds"
            );
        }

        String fieldType = normalizeEnum(
                position.fieldType(),
                FIELD_TYPES,
                "Unsupported template field type"
        );
        String valueSource = normalizeEnum(
                position.valueSource(),
                VALUE_SOURCES,
                "Unsupported template value source"
        );
        String signerRole = normalizeOptionalEnum(
                position.signerRole(),
                SIGNER_ROLES,
                "Unsupported template signer role"
        );

        if (("SIGNATURE".equals(fieldType)
                || "CURRENT_SIGNER".equals(valueSource))
                && signerRole == null) {
            throw new BadHttpException(
                    "Director or Partner role is required for signer fields"
            );
        }

        if ("SIGNATURE".equals(fieldType)
                && !"CURRENT_SIGNER".equals(valueSource)) {
            throw new BadHttpException(
                    "Signature fields must use the current signer as their value source"
            );
        }

        return new ContractPositionRequest(
                attributeKey,
                fieldLabel,
                pageNumber,
                roundCoordinate(x),
                roundCoordinate(y),
                roundCoordinate(width),
                roundCoordinate(height),
                fieldType,
                valueSource,
                signerRole,
                Boolean.TRUE.equals(position.systemField()),
                Boolean.TRUE.equals(position.required())
        );
    }

    private ContractPositionRequest toRequest(ContractPositions position) {
        return new ContractPositionRequest(
                position.getAttributeKey(),
                position.getFieldLabel(),
                position.getPageNumber(),
                position.getXPosition(),
                position.getYPosition(),
                position.getWidth(),
                position.getHeight(),
                position.getFieldType(),
                position.getValueSource(),
                position.getSignerRole(),
                position.getIsSystemField(),
                position.getIsRequired()
        );
    }

    private double requireCoordinate(Double value, String label) {
        if (value == null || !Double.isFinite(value) || value < 0 || value > 1) {
            throw new BadHttpException(
                    "Template " + label + " must be between 0 and 1"
            );
        }
        return value;
    }

    private double requireSize(Double value, String label) {
        if (value == null || !Double.isFinite(value) || value <= 0 || value > 1) {
            throw new BadHttpException(
                    "Template " + label + " must be greater than 0 and at most 1"
            );
        }
        return value;
    }

    private double roundCoordinate(double value) {
        return Math.round(value * 10000.0) / 10000.0;
    }

    private String normalizeEnum(
            String value,
            Set<String> allowedValues,
            String message
    ) {
        String normalized = requireText(value, message)
                .toUpperCase(Locale.ROOT)
                .replace('-', '_')
                .replace(' ', '_');
        if (!allowedValues.contains(normalized)) {
            throw new BadHttpException(message + ": " + normalized);
        }
        return normalized;
    }

    private String normalizeOptionalEnum(
            String value,
            Set<String> allowedValues,
            String message
    ) {
        if (value == null || value.isBlank() || "NONE".equalsIgnoreCase(value)) {
            return null;
        }
        return normalizeEnum(value, allowedValues, message);
    }

    private String requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new BadHttpException(message);
        }
        return value.trim();
    }
}

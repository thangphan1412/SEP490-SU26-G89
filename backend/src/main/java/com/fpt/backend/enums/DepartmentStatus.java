package com.fpt.backend.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Locale;

public enum DepartmentStatus {
    ACTIVE,
    INACTIVE;

    @JsonCreator
    public static DepartmentStatus fromValue(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        String normalized = value.trim().toUpperCase(Locale.ROOT);
        try {
            return DepartmentStatus.valueOf(normalized);
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(
                    "Department status must be ACTIVE or INACTIVE"
            );
        }
    }

    public static DepartmentStatus fromNullableFilter(String value) {
        if (value == null || value.isBlank() || "ALL".equalsIgnoreCase(value.trim())) {
            return null;
        }
        return fromValue(value);
    }

    @JsonValue
    public String getValue() {
        return name();
    }
}

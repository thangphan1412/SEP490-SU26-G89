package com.fpt.backend.enums;

import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

/**
 * Canonical system roles used by Role Management.
 *
 * <p>This enum deliberately does not replace {@code Users.role}. The value in
 * {@code users.user_role} remains the source of truth for a user's system
 * role. Aliases only let Role Management recognize legacy values without
 * rewriting user data.</p>
 */
public enum SystemRoleCode {
    ADMINISTRATOR("Administrator", Set.of("ADMIN")),
    CEO("CEO", Set.of("DIRECTOR")),
    ACCOUNTANT("Accountant", Set.of()),
    HEADOFDEPARTMENT(
            "HeadOfDepartment",
            Set.of("MANAGER", "HEAD_OF_DEPARTMENT")
    ),
    EMPLOYEE("Employee", Set.of()),
    EXTERNAL_PARNERS(
            "External Parners",
            Set.of(
                    "CUSTOMER",
                    "PARTNER",
                    "PARTNERS",
                    "EXTERNAL",
                    "EXTERNAL_PARTNER",
                    "EXTERNAL_PARTNERS"
            )
    );

    private final String roleName;
    private final Set<String> aliases;

    SystemRoleCode(String roleName, Set<String> aliases) {
        this.roleName = roleName;
        this.aliases = aliases;
    }

    public String getRoleName() {
        return roleName;
    }

    public static Optional<SystemRoleCode> fromValue(String value) {
        String normalized = normalize(value);
        if (normalized.isEmpty()) {
            return Optional.empty();
        }

        return Arrays.stream(values())
                .filter(role -> role.matchesNormalized(normalized))
                .findFirst();
    }

    public boolean matches(String value) {
        return matchesNormalized(normalize(value));
    }

    public static String normalizeCode(String value) {
        String normalized = value == null
                ? ""
                : value.trim().toUpperCase(Locale.ROOT)
                .replaceAll("[^A-Z0-9]+", "_")
                .replaceAll("^_+|_+$", "");
        return normalized.isEmpty() ? "SYSTEM_ROLE" : normalized;
    }

    private boolean matchesNormalized(String normalized) {
        return normalize(name()).equals(normalized)
                || normalize(roleName).equals(normalized)
                || aliases.stream().map(SystemRoleCode::normalize)
                .anyMatch(normalized::equals);
    }

    private static String normalize(String value) {
        return value == null
                ? ""
                : value.trim().toUpperCase(Locale.ROOT)
                .replaceAll("[^A-Z0-9]", "");
    }
}

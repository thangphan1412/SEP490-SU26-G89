package com.fpt.backend.service.impl.role;

import com.fpt.backend.dto.request.role.RoleRequestDTO;
import com.fpt.backend.dto.response.role.RoleResponseDTO;
import com.fpt.backend.entity.Role;
import com.fpt.backend.enums.SystemRoleCode;
import com.fpt.backend.repository.role.RoleRepository;
import com.fpt.backend.repository.role.SystemRoleUsageProjection;
import com.fpt.backend.service.interfaces.role.IRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements IRoleService {

    private static final Pattern ROLE_CODE_PATTERN =
            Pattern.compile("^[A-Z][A-Z0-9_]{1,49}$");
    private static final String SYSTEM_ROLE_DESCRIPTION =
            "System role sourced from Users.role. Read-only.";

    private final RoleRepository roleRepository;

    @Override
    public List<RoleResponseDTO> getAllRoles() {
        return searchRoles("");
    }

    @Override
    public List<RoleResponseDTO> searchRoles(String search) {
        String normalizedSearch = normalize(search).toLowerCase(Locale.ROOT);
        List<RoleResponseDTO> systemRoles = loadSystemRolesFromUsers();
        Map<String, RoleResponseDTO> systemRolesByCode = new LinkedHashMap<>();

        for (RoleResponseDTO systemRole : systemRoles) {
            systemRolesByCode.put(systemRole.getRoleCode(), systemRole);
        }

        List<RoleResponseDTO> customRoles = new ArrayList<>();
        for (Role role : roleRepository.searchRoles("")) {
            Optional<SystemRoleCode> knownSystemRole = resolveSystemRole(role);
            RoleResponseDTO referencedSystemRole = findReferencedSystemRole(
                    role,
                    systemRoles
            );

            if (knownSystemRole.isPresent() || referencedSystemRole != null) {
                RoleResponseDTO readOnlyRole = referencedSystemRole != null
                        ? referencedSystemRole
                        : fromSystemRole(knownSystemRole.orElseThrow(), 0);
                systemRolesByCode.putIfAbsent(
                        readOnlyRole.getRoleCode(),
                        readOnlyRole
                );
                continue;
            }

            RoleResponseDTO customRole = RoleResponseDTO.fromEntity(role);
            customRole.setAssignedUserCount(
                    roleRepository.countAssignedUsers(role.getId())
            );
            customRoles.add(customRole);
        }

        List<RoleResponseDTO> results = new ArrayList<>(
                systemRolesByCode.values()
        );
        results.addAll(customRoles);

        return results.stream()
                .filter(role -> matchesSearch(role, normalizedSearch))
                .sorted(Comparator
                        .comparing(RoleResponseDTO::isSystemRole)
                        .reversed()
                        .thenComparing(
                                RoleResponseDTO::getRoleName,
                                String.CASE_INSENSITIVE_ORDER
                        ))
                .toList();
    }

    @Override
    public RoleResponseDTO getRoleById(UUID id) {
        Role role = findRole(id);
        List<RoleResponseDTO> systemRoles = loadSystemRolesFromUsers();
        RoleResponseDTO referencedSystemRole = findReferencedSystemRole(
                role,
                systemRoles
        );
        Optional<SystemRoleCode> knownSystemRole = resolveSystemRole(role);

        if (referencedSystemRole != null) {
            return referencedSystemRole;
        }
        if (knownSystemRole.isPresent()) {
            return fromSystemRole(knownSystemRole.orElseThrow(), 0);
        }

        RoleResponseDTO response = RoleResponseDTO.fromEntity(role);
        response.setAssignedUserCount(
                roleRepository.countAssignedUsers(role.getId())
        );
        return response;
    }

    @Override
    public RoleResponseDTO getSystemRoleByCode(String roleCode) {
        return loadSystemRolesFromUsers().stream()
                .filter(role -> role.getRoleCode().equalsIgnoreCase(roleCode))
                .findFirst()
                .orElseThrow(() -> new RuntimeException(
                        "System role is not currently assigned to any user: "
                                + roleCode
                ));
    }

    @Override
    public RoleResponseDTO createRole(RoleRequestDTO request) {
        validateRequest(request);

        String roleCode = normalizeCode(request.getRoleCode());
        String roleName = request.getRoleName().trim();
        assertNotSystemRoleDefinition(roleCode, roleName);

        if (roleRepository.existsByRoleCodeIgnoreCase(roleCode)) {
            throw new RuntimeException("Role code is already in use!");
        }

        Role role = new Role();
        role.setRoleCode(roleCode);
        role.setRoleName(roleName);
        role.setRoleDescription(normalizeDescription(
                request.getRoleDescription()
        ));
        role.setCreatedAt(LocalDateTime.now());
        role.setUpdatedAt(null);

        return RoleResponseDTO.fromEntity(roleRepository.save(role));
    }

    @Override
    public RoleResponseDTO updateRole(
            UUID id,
            RoleRequestDTO request
    ) {
        Role role = findRole(id);
        assertRoleCanBeUpdated(role);
        validateRequest(request);

        String requestedCode = normalizeCode(request.getRoleCode());
        String requestedName = request.getRoleName().trim();

        if (!role.getRoleCode().equalsIgnoreCase(requestedCode)) {
            throw new RuntimeException(
                    "Role code cannot be changed after creation!"
            );
        }

        assertNotSystemRoleDefinition(requestedCode, requestedName);

        if (roleRepository.existsByRoleCodeIgnoreCaseAndIdNot(
                requestedCode,
                id
        )) {
            throw new RuntimeException(
                    "Role code is already in use by another role!"
            );
        }

        role.setRoleName(requestedName);
        role.setRoleDescription(normalizeDescription(
                request.getRoleDescription()
        ));
        role.setUpdatedAt(LocalDateTime.now());

        RoleResponseDTO response = RoleResponseDTO.fromEntity(
                roleRepository.save(role)
        );
        response.setAssignedUserCount(
                roleRepository.countAssignedUsers(role.getId())
        );
        return response;
    }

    @Override
    public void deleteRole(UUID id) {
        Role role = findRole(id);

        if (resolveSystemRole(role).isPresent()) {
            throw new RuntimeException(
                    "System roles are read-only and cannot be deleted!"
            );
        }

        long userRoleAssignments = roleRepository.countAssignedUsers(id);
        long systemRoleReferences = countUsersReferencingRole(
                role.getRoleCode(),
                role.getRoleName()
        );

        if (userRoleAssignments > 0 || systemRoleReferences > 0) {
            throw new RuntimeException(
                    "Role cannot be deleted because it is assigned through "
                            + "UserRole or referenced by Users.role!"
            );
        }

        roleRepository.delete(role);
    }

    private List<RoleResponseDTO> loadSystemRolesFromUsers() {
        Map<String, RoleResponseDTO> rolesByCode = new LinkedHashMap<>();

        for (SystemRoleUsageProjection usage
                : roleRepository.findSystemRoleUsage()) {
            String roleValue = normalize(usage.getRoleValue());
            if (roleValue.isEmpty()) {
                continue;
            }

            Optional<SystemRoleCode> knownRole =
                    SystemRoleCode.fromValue(roleValue);
            String roleCode = knownRole.map(Enum::name)
                    .orElseGet(() ->
                            SystemRoleCode.normalizeCode(roleValue));
            RoleResponseDTO current = rolesByCode.get(roleCode);
            long assignedUserCount = usage.getUserCount()
                    + (current == null
                    ? 0
                    : current.getAssignedUserCount());

            RoleResponseDTO response = knownRole
                    .map(role -> fromSystemRole(
                            role,
                            assignedUserCount
                    ))
                    .orElseGet(() -> fromUnknownSystemRole(
                            roleCode,
                            roleValue,
                            assignedUserCount
                    ));
            rolesByCode.put(roleCode, response);
        }

        return new ArrayList<>(rolesByCode.values());
    }

    private RoleResponseDTO fromSystemRole(
            SystemRoleCode role,
            long assignedUserCount
    ) {
        return RoleResponseDTO.builder()
                .roleCode(role.name())
                .roleName(role.getRoleName())
                .roleDescription(SYSTEM_ROLE_DESCRIPTION)
                .systemRole(true)
                .systemRoleCode(role)
                .assignedUserCount(assignedUserCount)
                .build();
    }

    private RoleResponseDTO fromUnknownSystemRole(
            String roleCode,
            String roleName,
            long assignedUserCount
    ) {
        return RoleResponseDTO.builder()
                .roleCode(roleCode)
                .roleName(roleName)
                .roleDescription(SYSTEM_ROLE_DESCRIPTION)
                .systemRole(true)
                .assignedUserCount(assignedUserCount)
                .build();
    }

    private RoleResponseDTO findReferencedSystemRole(
            Role role,
            List<RoleResponseDTO> systemRoles
    ) {
        return systemRoles.stream()
                .filter(systemRole -> equalsIgnoreCase(
                        systemRole.getRoleCode(),
                        role.getRoleCode()
                ) || equalsIgnoreCase(
                        systemRole.getRoleName(),
                        role.getRoleName()
                ))
                .findFirst()
                .orElse(null);
    }

    private Optional<SystemRoleCode> resolveSystemRole(Role role) {
        Optional<SystemRoleCode> byCode =
                SystemRoleCode.fromValue(role.getRoleCode());
        return byCode.isPresent()
                ? byCode
                : SystemRoleCode.fromValue(role.getRoleName());
    }

    private void assertRoleCanBeUpdated(Role role) {
        if (resolveSystemRole(role).isPresent()
                || countUsersReferencingRole(
                role.getRoleCode(),
                role.getRoleName()
        ) > 0) {
            throw new RuntimeException(
                    "System roles are read-only and cannot be updated!"
            );
        }
    }

    private void assertNotSystemRoleDefinition(
            String roleCode,
            String roleName
    ) {
        if (SystemRoleCode.fromValue(roleCode).isPresent()
                || SystemRoleCode.fromValue(roleName).isPresent()
                || countUsersReferencingRole(
                roleCode,
                roleName
        ) > 0) {
            throw new RuntimeException(
                    "System role codes and names are reserved and read-only!"
            );
        }
    }

    private Role findRole(UUID id) {
        return roleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "Role not found with id: " + id
                ));
    }

    private long countUsersReferencingRole(
            String roleCode,
            String roleName
    ) {
        return roleRepository.countUsersReferencingRole(
                roleCode,
                roleName
        );
    }

    private void validateRequest(RoleRequestDTO request) {
        if (request == null) {
            throw new RuntimeException("Role request is required");
        }

        String roleCode = normalizeCode(request.getRoleCode());

        if (!ROLE_CODE_PATTERN.matcher(roleCode).matches()) {
            throw new RuntimeException(
                    "Role code must contain 2-50 uppercase letters, numbers, or underscores"
            );
        }

        if (request.getRoleName() == null
                || request.getRoleName().isBlank()) {
            throw new RuntimeException("Role name is required");
        }

        if (request.getRoleName().trim().length() > 100) {
            throw new RuntimeException(
                    "Role name cannot exceed 100 characters"
            );
        }

        String description = normalizeDescription(
                request.getRoleDescription()
        );

        if (description != null && description.length() > 255) {
            throw new RuntimeException(
                    "Role description cannot exceed 255 characters"
            );
        }
    }

    private boolean matchesSearch(
            RoleResponseDTO role,
            String normalizedSearch
    ) {
        return normalizedSearch.isEmpty()
                || containsIgnoreCase(role.getRoleCode(), normalizedSearch)
                || containsIgnoreCase(role.getRoleName(), normalizedSearch)
                || containsIgnoreCase(
                role.getRoleDescription(),
                normalizedSearch
        );
    }

    private boolean containsIgnoreCase(String value, String search) {
        return value != null
                && value.toLowerCase(Locale.ROOT).contains(search);
    }

    private boolean equalsIgnoreCase(String first, String second) {
        return first != null
                && second != null
                && first.equalsIgnoreCase(second);
    }

    private String normalizeCode(String value) {
        return normalize(value).toUpperCase(Locale.ROOT);
    }

    private String normalizeDescription(String value) {
        String normalized = normalize(value);
        return normalized.isEmpty() ? null : normalized;
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }
}

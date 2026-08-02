package com.fpt.backend.service.impl.role;

import com.fpt.backend.dto.request.role.RoleRequestDTO;
import com.fpt.backend.dto.response.role.RoleResponseDTO;
import com.fpt.backend.entity.Role;
import com.fpt.backend.repository.role.RoleRepository;
import com.fpt.backend.service.interfaces.role.IRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class RoleServiceImpl implements IRoleService {

    private static final Pattern ROLE_CODE_PATTERN =
            Pattern.compile("^[A-Z][A-Z0-9_]{1,49}$");

    @Autowired
    private RoleRepository roleRepository;

    @Override
    public List<RoleResponseDTO> getAllRoles() {
        return searchRoles("");
    }

    @Override
    public List<RoleResponseDTO> searchRoles(String search) {
        return roleRepository
                .searchRoles(normalize(search).toLowerCase(Locale.ROOT))
                .stream()
                .map(RoleResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public RoleResponseDTO getRoleById(UUID id) {
        return RoleResponseDTO.fromEntity(findRole(id));
    }

    @Override
    public RoleResponseDTO createRole(RoleRequestDTO request) {
        validateRequest(request);

        String roleCode = normalizeCode(request.getRoleCode());

        if (roleRepository.existsByRoleCodeIgnoreCase(roleCode)) {
            throw new RuntimeException("Role code is already in use!");
        }

        Role role = new Role();
        role.setRoleCode(roleCode);
        role.setRoleName(request.getRoleName().trim());
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
        validateRequest(request);

        Role role = findRole(id);
        String requestedCode = normalizeCode(request.getRoleCode());

        if (!role.getRoleCode().equalsIgnoreCase(requestedCode)) {
            throw new RuntimeException(
                    "Role code cannot be changed after creation!"
            );
        }

        if (roleRepository.existsByRoleCodeIgnoreCaseAndIdNot(
                requestedCode,
                id
        )) {
            throw new RuntimeException(
                    "Role code is already in use by another role!"
            );
        }

        role.setRoleName(request.getRoleName().trim());
        role.setRoleDescription(normalizeDescription(
                request.getRoleDescription()
        ));
        role.setUpdatedAt(LocalDateTime.now());

        return RoleResponseDTO.fromEntity(roleRepository.save(role));
    }

    @Override
    public void deleteRole(UUID id) {
        Role role = findRole(id);

        if (roleRepository.countAssignedUsers(id) > 0) {
            throw new RuntimeException(
                    "Role cannot be deleted because it is assigned to users!"
            );
        }

        roleRepository.delete(role);
    }

    private Role findRole(UUID id) {
        return roleRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Role not found with id: " + id
                        )
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

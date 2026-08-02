package com.fpt.backend.service.interfaces.role;

import com.fpt.backend.dto.request.role.RoleRequestDTO;
import com.fpt.backend.dto.response.role.RoleResponseDTO;

import java.util.List;
import java.util.UUID;

public interface IRoleService {
    List<RoleResponseDTO> getAllRoles();

    List<RoleResponseDTO> searchRoles(String search);

    RoleResponseDTO getRoleById(UUID id);

    RoleResponseDTO createRole(RoleRequestDTO request);

    RoleResponseDTO updateRole(UUID id, RoleRequestDTO request);

    void deleteRole(UUID id);
}

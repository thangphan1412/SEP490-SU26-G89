package com.fpt.backend.controller.roleController;

import com.fpt.backend.constant.ApiConstant;
import com.fpt.backend.dto.request.role.RoleRequestDTO;
import com.fpt.backend.dto.response.role.RoleResponseDTO;
import com.fpt.backend.service.interfaces.role.IRoleService;
import com.fpt.backend.util.BaseResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(ApiConstant.Role.ROLES)
public class RoleController {

    @Autowired
    private IRoleService roleService;

    @GetMapping
    public ResponseEntity<BaseResponse<List<RoleResponseDTO>>>
    getAllRoles() {
        List<RoleResponseDTO> roles = roleService.getAllRoles();

        return ResponseEntity.ok(
                BaseResponse.<List<RoleResponseDTO>>builder()
                        .status(HttpStatus.OK.value())
                        .message("Successfully fetched all roles")
                        .data(roles)
                        .build()
        );
    }

    @GetMapping(ApiConstant.Role.LIST)
    public ResponseEntity<BaseResponse<List<RoleResponseDTO>>>
    searchRoles(
            @RequestParam(defaultValue = "") String search
    ) {
        List<RoleResponseDTO> roles =
                roleService.searchRoles(search);

        return ResponseEntity.ok(
                BaseResponse.<List<RoleResponseDTO>>builder()
                        .status(HttpStatus.OK.value())
                        .message("Successfully searched roles")
                        .data(roles)
                        .build()
        );
    }

    @GetMapping(ApiConstant.Role.BY_ID)
    public ResponseEntity<BaseResponse<RoleResponseDTO>>
    getRoleById(@PathVariable UUID id) {
        try {
            RoleResponseDTO role = roleService.getRoleById(id);

            return ResponseEntity.ok(
                    BaseResponse.<RoleResponseDTO>builder()
                            .status(HttpStatus.OK.value())
                            .message("Role found")
                            .data(role)
                            .build()
            );
        } catch (Exception exception) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(BaseResponse.<RoleResponseDTO>builder()
                            .status(HttpStatus.NOT_FOUND.value())
                            .message(exception.getMessage())
                            .build());
        }
    }

    @PostMapping
    public ResponseEntity<BaseResponse<RoleResponseDTO>>
    createRole(@RequestBody RoleRequestDTO request) {
        try {
            RoleResponseDTO role = roleService.createRole(request);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(BaseResponse.<RoleResponseDTO>builder()
                            .status(HttpStatus.CREATED.value())
                            .message("Role created successfully")
                            .data(role)
                            .build());
        } catch (Exception exception) {
            return ResponseEntity.badRequest()
                    .body(BaseResponse.<RoleResponseDTO>builder()
                            .status(HttpStatus.BAD_REQUEST.value())
                            .message(exception.getMessage())
                            .build());
        }
    }

    @PutMapping(ApiConstant.Role.BY_ID)
    public ResponseEntity<BaseResponse<RoleResponseDTO>>
    updateRole(
            @PathVariable UUID id,
            @RequestBody RoleRequestDTO request
    ) {
        try {
            RoleResponseDTO role =
                    roleService.updateRole(id, request);

            return ResponseEntity.ok(
                    BaseResponse.<RoleResponseDTO>builder()
                            .status(HttpStatus.OK.value())
                            .message("Role updated successfully")
                            .data(role)
                            .build()
            );
        } catch (Exception exception) {
            return ResponseEntity.badRequest()
                    .body(BaseResponse.<RoleResponseDTO>builder()
                            .status(HttpStatus.BAD_REQUEST.value())
                            .message(exception.getMessage())
                            .build());
        }
    }

    @DeleteMapping(ApiConstant.Role.BY_ID)
    public ResponseEntity<BaseResponse<Void>>
    deleteRole(@PathVariable UUID id) {
        try {
            roleService.deleteRole(id);

            return ResponseEntity.ok(
                    BaseResponse.<Void>builder()
                            .status(HttpStatus.OK.value())
                            .message("Role deleted successfully")
                            .data(null)
                            .build()
            );
        } catch (Exception exception) {
            return ResponseEntity.badRequest()
                    .body(BaseResponse.<Void>builder()
                            .status(HttpStatus.BAD_REQUEST.value())
                            .message(exception.getMessage())
                            .build());
        }
    }
}

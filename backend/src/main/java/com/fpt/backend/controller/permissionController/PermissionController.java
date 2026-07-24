package com.fpt.backend.controller.permissionController;

import com.fpt.backend.dto.request.permission.PermissionListRequest;
import com.fpt.backend.dto.request.permission.PermissionRequest;
import com.fpt.backend.dto.response.permission.PermissionDetailResponse;
import com.fpt.backend.dto.response.permission.PermissionListResponse;
import com.fpt.backend.dto.response.permission.PermissionProjectResponse;
import com.fpt.backend.dto.response.permission.PermissionRoleResponse;
import com.fpt.backend.service.interfaces.permission.PermissionService;
import com.fpt.backend.util.BaseResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
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
@RequestMapping("/api/permissions")
@CrossOrigin(originPatterns = "*")
@RequiredArgsConstructor
public class PermissionController {
    private final PermissionService permissionService;

    @GetMapping("/list")
    public ResponseEntity<BaseResponse<PermissionListResponse>> getPermissions(
            @RequestParam(defaultValue = "") String search,
            @RequestParam(required = false) UUID projectId,
            @RequestParam(required = false) UUID roleId,
            @RequestParam(required = false) Boolean status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {
        PermissionListResponse permissions = permissionService.getPermissions(
                new PermissionListRequest(search, projectId, roleId, status, page, sortBy, sortDirection)
        );

        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(new BaseResponse<>(permissions));
    }

    @GetMapping("/projects")
    public ResponseEntity<BaseResponse<List<PermissionProjectResponse>>> getProjectsForPermissionSelection() {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(new BaseResponse<>(permissionService.getProjectsForPermissionSelection()));
    }

    @GetMapping("/roles")
    public ResponseEntity<BaseResponse<List<PermissionRoleResponse>>> getRolesForPermissionSelection() {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(new BaseResponse<>(permissionService.getRolesForPermissionSelection()));
    }

    @GetMapping({"/view/{id}", "/{id}"})
    public ResponseEntity<BaseResponse<PermissionDetailResponse>> getPermissionById(@PathVariable UUID id) {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(new BaseResponse<>(permissionService.getPermissionById(id)));
    }

    @PostMapping({"", "/create"})
    public ResponseEntity<BaseResponse<PermissionDetailResponse>> createPermission(
            @RequestBody PermissionRequest request) {
        PermissionDetailResponse permission = permissionService.createPermission(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new BaseResponse<>(HttpStatus.CREATED.value(), "Created", permission));
    }

    @PutMapping({"/{id}", "/update/{id}"})
    public ResponseEntity<BaseResponse<PermissionDetailResponse>> updatePermission(
            @PathVariable UUID id,
            @RequestBody PermissionRequest request) {
        return ResponseEntity.ok(new BaseResponse<>(permissionService.updatePermission(id, request)));
    }

    @DeleteMapping({"/{id}", "/delete/{id}"})
    public ResponseEntity<BaseResponse<Void>> deletePermission(@PathVariable UUID id) {
        permissionService.deletePermission(id);

        return ResponseEntity.ok(new BaseResponse<>(HttpStatus.OK.value(), "Deleted", null));
    }
}

package com.fpt.backend.controller.projectController;

import com.fpt.backend.dto.request.project.ProjectCreateRequest;
import com.fpt.backend.dto.request.project.ProjectListRequest;
import com.fpt.backend.dto.request.project.ProjectPermissionConfigurationRequest;
import com.fpt.backend.dto.request.project.ProjectUpdateRequest;
import com.fpt.backend.dto.response.project.ProjectDetailResponse;
import com.fpt.backend.dto.response.project.ProjectEmployeeResponse;
import com.fpt.backend.dto.response.project.ProjectListResponse;
import com.fpt.backend.dto.response.project.ProjectPermissionConfigurationResponse;
import com.fpt.backend.dto.response.project.ProjectRoleResponse;
import com.fpt.backend.service.interfaces.project.ProjectDeleteResult;
import com.fpt.backend.service.interfaces.project.ProjectService;
import com.fpt.backend.util.BaseResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
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
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {
    private final ProjectService projectService;

    @GetMapping({"", "/list"})
    public ResponseEntity<BaseResponse<ProjectListResponse>> getProjects(
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "") String status,
            @RequestParam(defaultValue = "false") boolean viewOnlyYourProjects,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "projectCreatedAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {
        ProjectListResponse projects = projectService.getProjects(
                new ProjectListRequest(
                        search,
                        status,
                        viewOnlyYourProjects,
                        page,
                        sortBy,
                        sortDirection
                )
        );

        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(new BaseResponse<>(projects));
    }

    @GetMapping({"/{id}", "/view/{id}"})
    public ResponseEntity<BaseResponse<ProjectDetailResponse>> getProjectById(@PathVariable UUID id) {
        ProjectDetailResponse project = projectService.getProjectById(id);

        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(new BaseResponse<>(project));
    }

    @GetMapping("/employees")
    public ResponseEntity<BaseResponse<List<ProjectEmployeeResponse>>> getEmployeesForProjectSelection() {
        List<ProjectEmployeeResponse> employees = projectService.getEmployeesForProjectSelection();

        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(new BaseResponse<>(employees));
    }

    @GetMapping("/roles")
    public ResponseEntity<BaseResponse<List<ProjectRoleResponse>>> getRolesForProjectMemberFilter() {
        List<ProjectRoleResponse> roles = projectService.getRolesForProjectMemberFilter();

        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(new BaseResponse<>(roles));
    }

    @GetMapping("/{projectId}/permission-configurations")
    public ResponseEntity<BaseResponse<List<ProjectPermissionConfigurationResponse>>>
    getProjectPermissionConfigurations(@PathVariable UUID projectId) {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(new BaseResponse<>(
                        projectService.getProjectPermissionConfigurations(projectId)
                ));
    }

    @PutMapping({
            "/{projectId}/permissions/{permissionId}",
            "/{projectId}/permissions/{permissionId}/configure"
    })
    public ResponseEntity<BaseResponse<ProjectPermissionConfigurationResponse>>
    configureProjectPermission(
            @PathVariable UUID projectId,
            @PathVariable UUID permissionId,
            @RequestBody ProjectPermissionConfigurationRequest request) {
        return ResponseEntity.ok(new BaseResponse<>(
                projectService.configureProjectPermission(projectId, permissionId, request)
        ));
    }

    @PostMapping({"", "/create"})
    public ResponseEntity<BaseResponse<ProjectDetailResponse>> createProject(@RequestBody ProjectCreateRequest request) {
        ProjectDetailResponse project = projectService.createProject(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new BaseResponse<>(
                        HttpStatus.CREATED.value(),
                        "Created",
                        project
                ));
    }

    @PutMapping({"/{id}", "/update/{id}"})
    public ResponseEntity<BaseResponse<ProjectDetailResponse>> updateProject(
            @PathVariable UUID id,
            @RequestBody ProjectUpdateRequest request) {
        return ResponseEntity.ok(new BaseResponse<>(projectService.updateProject(id, request)));
    }

    @DeleteMapping({"/{id}", "/delete/{id}"})
    public ResponseEntity<BaseResponse<Void>> deleteProject(@PathVariable UUID id) {
        ProjectDeleteResult deleteResult = projectService.deleteProject(id);
        String message;

        if (deleteResult == ProjectDeleteResult.DELETED_PERMANENTLY) {
            message = "Project deleted permanently";
        } else {
            message = "Project has contracts, so its status was changed to Cancelled";
        }

        return ResponseEntity.ok(new BaseResponse<>(HttpStatus.OK.value(), message, null));
    }
}

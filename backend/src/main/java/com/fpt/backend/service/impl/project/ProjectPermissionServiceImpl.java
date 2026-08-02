package com.fpt.backend.service.impl.project;

import com.fpt.backend.dto.request.project.ProjectPermissionConfigurationRequest;
import com.fpt.backend.dto.response.project.ProjectPermissionConfigurationResponse;
import com.fpt.backend.dto.response.project.ProjectPermissionOptionResponse;
import com.fpt.backend.entity.Permissions;
import com.fpt.backend.entity.Projects;
import com.fpt.backend.exception.BadHttpException;
import com.fpt.backend.exception.NotFoundException;
import com.fpt.backend.repository.permission.PermissionRepository;
import com.fpt.backend.repository.permission.UserPermissionRepository;
import com.fpt.backend.service.interfaces.permission.PermissionModuleService;
import com.fpt.backend.service.interfaces.project.ProjectPermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProjectPermissionServiceImpl
        implements ProjectPermissionService {
    private static final String FULL_ACCESS_PERMISSION_NAME =
            "Project Full Access";
    private static final String FULL_ACCESS_PERMISSION_CODE_PREFIX = "PFA_";
    private static final String FULL_WORK_SCOPE = "FULL";
    private static final List<String> FULL_ACCESS_ACTIONS = List.of(
            "VIEW_TASKS",
            "CREATE_TASKS",
            "EDIT_TASKS",
            "DELETE_TASKS",
            "VIEW_DELIVERABLES",
            "CREATE_DELIVERABLES",
            "EDIT_DELIVERABLES",
            "DELETE_DELIVERABLES",
            "VIEW_CONTRACTS",
            "EDIT_PROJECT",
            "EDIT_PHASE",
            "MANAGE_MEMBERS"
    );

    private final PermissionRepository permissionRepository;
    private final PermissionModuleService permissionModuleService;
    private final UserPermissionRepository userPermissionRepository;

    @Override
    public UUID createProjectFullAccessPermission(Projects project) {
        if (project == null || project.getId() == null) {
            throw new BadHttpException(
                    "Project must be saved before creating its permission"
            );
        }

        Permissions permission = new Permissions();
        permission.setPermissionName(FULL_ACCESS_PERMISSION_NAME);
        permission.setPermissionCode(
                FULL_ACCESS_PERMISSION_CODE_PREFIX + project.getId()
        );
        permission.setPermissionDescription(
                "Full access permission created automatically "
                        + "for the project creator"
        );
        permission.setPermissionModule(
                permissionModuleService.createModuleValue(
                        FULL_ACCESS_ACTIONS,
                        FULL_WORK_SCOPE
                )
        );
        permission.setStatus(true);
        permission.setCreatedAt(LocalDateTime.now());
        permission.setProject(project);

        return permissionRepository.save(permission).getId();
    }

    @Override
    public List<ProjectPermissionConfigurationResponse> getConfigurations(
            UUID projectId) {
        List<Permissions> permissions =
                permissionRepository.findByProjectId(projectId);
        List<ProjectPermissionConfigurationResponse> responses =
                new ArrayList<>();

        for (Permissions permission : permissions) {
            responses.add(toConfiguration(permission));
        }

        return responses;
    }

    @Override
    public ProjectPermissionConfigurationResponse configure(
            Projects project,
            UUID permissionId,
            ProjectPermissionConfigurationRequest request) {
        validateConfigurationRequest(request);

        Optional<Permissions> optionalPermission =
                permissionRepository.findByIdAndProjectId(
                        permissionId,
                        project.getId()
                );

        if (optionalPermission.isEmpty()) {
            throw new NotFoundException(
                    "Permission does not belong to this project"
            );
        }

        Permissions permission = optionalPermission.get();
        permission.setPermissionModule(
                permissionModuleService.createModuleValue(
                        request.allowedActions(),
                        request.workScope()
                )
        );

        return toConfiguration(permissionRepository.save(permission));
    }

    @Override
    public List<ProjectPermissionOptionResponse> getOptions(UUID projectId) {
        List<Permissions> permissions = new ArrayList<>(
                permissionRepository.findByProjectId(projectId)
        );
        permissions.sort(Comparator.comparing(
                this::getPermissionName,
                String.CASE_INSENSITIVE_ORDER
        ));

        List<ProjectPermissionOptionResponse> responses = new ArrayList<>();

        for (Permissions permission : permissions) {
            responses.add(new ProjectPermissionOptionResponse(
                    permission.getId(),
                    getPermissionName(permission),
                    permission.getPermissionCode(),
                    permission.getPermissionDescription(),
                    permission.getStatus()
            ));
        }

        return responses;
    }

    @Override
    public void deleteProjectData(UUID projectId) {
        userPermissionRepository.deleteByProjectId(projectId);
        permissionRepository.deleteByProjectId(projectId);
        permissionRepository.flush();
    }

    private void validateConfigurationRequest(
            ProjectPermissionConfigurationRequest request) {
        if (request == null) {
            throw new BadHttpException(
                    "Permission configuration is required"
            );
        }
    }

    private ProjectPermissionConfigurationResponse toConfiguration(
            Permissions permission) {
        return new ProjectPermissionConfigurationResponse(
                permission.getId(),
                getPermissionName(permission),
                permission.getPermissionCode(),
                permission.getPermissionDescription(),
                permission.getStatus(),
                permissionModuleService.getAllowedActions(
                        permission.getPermissionModule()
                ),
                permissionModuleService.getWorkScope(
                        permission.getPermissionModule()
                )
        );
    }

    private String getPermissionName(Permissions permission) {
        String name = normalize(permission.getPermissionName());

        if (!name.isBlank()) {
            return name;
        }

        String code = normalize(permission.getPermissionCode());

        if (!code.isBlank()) {
            return code;
        }

        return "Permission #" + permission.getId();
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }
}

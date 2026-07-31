package com.fpt.backend.service.impl.project;

import com.fpt.backend.dto.request.project.ProjectPermissionConfigurationRequest;
import com.fpt.backend.dto.response.project.ProjectPermissionConfigurationResponse;
import com.fpt.backend.dto.response.project.ProjectPermissionOptionResponse;
import com.fpt.backend.entity.Permissions;
import com.fpt.backend.entity.Projects;
import com.fpt.backend.exception.BadHttpException;
import com.fpt.backend.exception.NotFoundException;
import com.fpt.backend.repository.permission.PermissionRepository;
import com.fpt.backend.service.interfaces.permission.PermissionModuleService;
import com.fpt.backend.service.interfaces.project.ProjectPermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProjectPermissionServiceImpl
        implements ProjectPermissionService {
    private final PermissionRepository permissionRepository;
    private final PermissionModuleService permissionModuleService;

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
        permissionRepository.deleteByProjectId(projectId);
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

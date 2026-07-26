package com.fpt.backend.service.impl.project;

import com.fpt.backend.dto.request.project.ProjectPermissionConfigurationRequest;
import com.fpt.backend.dto.response.project.ProjectPermissionConfigurationResponse;
import com.fpt.backend.dto.response.project.ProjectPermissionOptionResponse;
import com.fpt.backend.entity.Permissions;
import com.fpt.backend.entity.Projects;
import com.fpt.backend.exception.BadHttpException;
import com.fpt.backend.exception.NotFoundException;
import com.fpt.backend.repository.permission.PermissionRepository;
import com.fpt.backend.service.interfaces.project.ProjectPermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProjectPermissionServiceImpl
        implements ProjectPermissionService {
    private static final String WORK_SCOPE_OWN = "OWN";
    private static final String WORK_SCOPE_FULL = "FULL";
    private static final String WORK_SCOPE_OWN_TOKEN = "WORK_SCOPE_OWN";
    private static final String WORK_SCOPE_FULL_TOKEN = "WORK_SCOPE_FULL";
    private static final List<String> PERMISSION_ACTIONS = List.of(
            "VIEW_TASKS",
            "VIEW_DELIVERABLES",
            "VIEW_CONTRACTS",
            "CREATE_TASKS",
            "EDIT_TASKS",
            "DELETE_TASKS",
            "CREATE_DELIVERABLES",
            "EDIT_DELIVERABLES",
            "DELETE_DELIVERABLES",
            "EDIT_PHASE",
            "MANAGE_MEMBERS"
    );

    private final PermissionRepository permissionRepository;

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
        permission.setPermissionModule(createModuleValue(request));

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

    private String createModuleValue(
            ProjectPermissionConfigurationRequest request) {
        if (request == null) {
            throw new BadHttpException(
                    "Permission configuration is required"
            );
        }

        Set<String> requestedActions = new LinkedHashSet<>();
        List<String> actions = request.allowedActions() == null
                ? List.of()
                : request.allowedActions();

        for (String action : actions) {
            String normalizedAction =
                    normalize(action).toUpperCase(Locale.ROOT);

            if (!PERMISSION_ACTIONS.contains(normalizedAction)) {
                throw new BadHttpException(
                        "Unsupported permission action: " + action
                );
            }

            requestedActions.add(normalizedAction);
        }

        String workScope =
                normalize(request.workScope()).toUpperCase(Locale.ROOT);

        if (!WORK_SCOPE_OWN.equals(workScope)
                && !WORK_SCOPE_FULL.equals(workScope)) {
            throw new BadHttpException(
                    "Work scope must be OWN or FULL"
            );
        }

        List<String> storedValues = new ArrayList<>();

        for (String supportedAction : PERMISSION_ACTIONS) {
            if (requestedActions.contains(supportedAction)) {
                storedValues.add(supportedAction);
            }
        }

        if (WORK_SCOPE_OWN.equals(workScope)) {
            storedValues.add(WORK_SCOPE_OWN_TOKEN);
        } else {
            storedValues.add(WORK_SCOPE_FULL_TOKEN);
        }

        return String.join(",", storedValues);
    }

    private ProjectPermissionConfigurationResponse toConfiguration(
            Permissions permission) {
        Set<String> storedValues = getStoredModuleValues(permission);
        List<String> allowedActions = new ArrayList<>();

        for (String action : PERMISSION_ACTIONS) {
            if (storedValues.contains(action)) {
                allowedActions.add(action);
            }
        }

        String workScope;

        if (storedValues.contains(WORK_SCOPE_OWN_TOKEN)) {
            workScope = WORK_SCOPE_OWN;
        } else {
            workScope = WORK_SCOPE_FULL;
        }

        return new ProjectPermissionConfigurationResponse(
                permission.getId(),
                getPermissionName(permission),
                permission.getPermissionCode(),
                permission.getPermissionDescription(),
                permission.getStatus(),
                allowedActions,
                workScope
        );
    }

    private Set<String> getStoredModuleValues(Permissions permission) {
        Set<String> storedValues = new LinkedHashSet<>();
        String permissionModule = normalize(permission.getPermissionModule());

        if (permissionModule.isBlank()) {
            return storedValues;
        }

        for (String value : permissionModule.split(",")) {
            String normalizedValue =
                    normalize(value).toUpperCase(Locale.ROOT);

            if (!normalizedValue.isBlank()) {
                storedValues.add(normalizedValue);
            }
        }

        return storedValues;
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

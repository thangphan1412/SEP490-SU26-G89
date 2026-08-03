package com.fpt.backend.service.impl.permission;

import com.fpt.backend.dto.response.project.ProjectAccessResponse;
import com.fpt.backend.entity.PermissionAction;
import com.fpt.backend.entity.Permissions;
import com.fpt.backend.entity.Projects;
import com.fpt.backend.entity.UserPermission;
import com.fpt.backend.entity.Users;
import com.fpt.backend.exception.BadHttpException;
import com.fpt.backend.exception.NotFoundException;
import com.fpt.backend.repository.permission.UserPermissionRepository;
import com.fpt.backend.repository.project.ProjectMemberRepository;
import com.fpt.backend.repository.project.ProjectRepository;
import com.fpt.backend.service.interfaces.permission.PermissionAccessService;
import com.fpt.backend.util.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PermissionAccessServiceImpl
        implements PermissionAccessService {
    private static final String ACCESS_DENIED_MESSAGE =
            "You do not have permission to use this function";

    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final UserPermissionRepository userPermissionRepository;
    private final CurrentUser currentUser;

    @Override
    public ProjectAccessResponse getCurrentUserAccess(UUID projectId) {
        Projects project = findProject(projectId);
        Users user = currentUser.getCurrentUser();
        boolean projectCreator = isProjectCreator(project, user);
        boolean projectMember = projectMemberRepository
                .countByProjectIdAndUserId(projectId, user.getId()) > 0;

        if (!projectCreator && !projectMember) {
            throw forbidden("You cannot access this project");
        }

        List<UserPermission> assignedPermissions =
                userPermissionRepository
                        .findActiveByUserIdAndProjectId(
                                user.getId(),
                                projectId
                        );
        Set<String> allowedActions = new LinkedHashSet<>();
        Set<String> fullScopeActions = new LinkedHashSet<>();
        Permissions.WorkScope workScope = Permissions.WorkScope.OWN;

        for (UserPermission assignment : assignedPermissions) {
            Permissions permission = assignment.getPermission();

            if (permission == null
                    || !Boolean.TRUE.equals(permission.getStatus())) {
                continue;
            }

            if (permission.getWorkScope() == Permissions.WorkScope.FULL) {
                workScope = Permissions.WorkScope.FULL;
                addActiveActionCodes(permission, fullScopeActions);
            }

            addActiveActionCodes(permission, allowedActions);
        }

        return new ProjectAccessResponse(
                projectId,
                user.getId(),
                projectCreator,
                projectMember,
                new ArrayList<>(allowedActions),
                new ArrayList<>(fullScopeActions),
                workScope.name()
        );
    }

    @Override
    public void requireAction(UUID projectId, String actionCode) {
        ProjectAccessResponse access = getCurrentUserAccess(projectId);

        if (!hasAction(access, actionCode)) {
            throw forbidden(
                    ACCESS_DENIED_MESSAGE + ": " + normalize(actionCode)
            );
        }
    }

    @Override
    public boolean hasAction(
            ProjectAccessResponse access,
            String actionCode) {
        if (access == null || access.allowedActions() == null) {
            return false;
        }

        String requiredAction = normalize(actionCode);

        for (String allowedAction : access.allowedActions()) {
            if (requiredAction.equals(normalize(allowedAction))) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean hasFullWorkScope(
            ProjectAccessResponse access,
            String actionCode) {
        if (access == null || access.fullScopeActions() == null) {
            return false;
        }

        String requiredAction = normalize(actionCode);

        for (String fullScopeAction : access.fullScopeActions()) {
            if (requiredAction.equals(normalize(fullScopeAction))) {
                return true;
            }
        }

        return false;
    }

    @Override
    public List<UUID> getCurrentUserProjectIdsWithAction(
            String actionCode) {
        Users user = currentUser.getCurrentUser();
        return userPermissionRepository.findProjectIdsByUserAndAction(
                user.getId(),
                normalize(actionCode)
        );
    }

    private Projects findProject(UUID projectId) {
        if (projectId == null) {
            throw new BadHttpException("Project is required");
        }

        Optional<Projects> project = projectRepository.findById(projectId);

        if (project.isEmpty()) {
            throw new NotFoundException("Project not found");
        }

        return project.get();
    }

    private void addActiveActionCodes(
            Permissions permission,
            Set<String> actionCodes) {
        if (permission.getActions() == null) {
            return;
        }

        for (PermissionAction action : permission.getActions()) {
            if (action != null
                    && Boolean.TRUE.equals(action.getStatus())
                    && !normalize(action.getActionCode()).isBlank()) {
                actionCodes.add(normalize(action.getActionCode()));
            }
        }
    }

    private boolean isProjectCreator(Projects project, Users user) {
        Users creator = project.getProjectCreatedBy();
        return creator != null
                && creator.getId() != null
                && creator.getId().equals(user.getId());
    }

    private ResponseStatusException forbidden(String message) {
        return new ResponseStatusException(HttpStatus.FORBIDDEN, message);
    }

    private String normalize(String value) {
        return value == null
                ? ""
                : value.trim().toUpperCase(Locale.ROOT);
    }
}

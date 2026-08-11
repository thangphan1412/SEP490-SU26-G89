package com.fpt.backend.service.impl.permission;

import com.fpt.backend.dto.response.project.ProjectAccessResponse;
import com.fpt.backend.entity.PermissionAction;
import com.fpt.backend.entity.Permissions;
import com.fpt.backend.entity.UserPermission;
import com.fpt.backend.entity.Users;
import com.fpt.backend.enums.WorkScope;
import com.fpt.backend.exception.BadHttpException;
import com.fpt.backend.exception.NotFoundException;
import com.fpt.backend.repository.permission.UserPermissionRepository;
import com.fpt.backend.repository.project.ProjectMemberRepository;
import com.fpt.backend.repository.project.ProjectRepository;
import com.fpt.backend.service.interfaces.permission.IPermissionAccessService;
import com.fpt.backend.util.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PermissionAccessServiceImpl
        implements IPermissionAccessService {
    private static final String ACCESS_DENIED_MESSAGE =
            "You do not have permission to use this function";

    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final UserPermissionRepository userPermissionRepository;
    private final CurrentUser currentUser;

    @Override
    public void requireProjectAccess(UUID projectId) {
        getCurrentUserAccess(projectId);
    }

    @Override
    public void requireAction(UUID projectId, String actionCode) {
        ProjectAccessResponse access = getCurrentUserAccess(projectId);

        if (!hasAction(access, actionCode)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    ACCESS_DENIED_MESSAGE + ": " + actionCode
            );
        }
    }

    //Kiểm tra xem người dùng hiện tại có quyền thực hiện một hành động cụ thể trong dự án hay không
    @Override
    public boolean hasAction(
            ProjectAccessResponse access,
            String actionCode) {
        if (access == null) {
            return false;
        }

        return containsAction(access.allowedActions(), actionCode);
    }

    @Override
    public boolean hasFullWorkScope(
            ProjectAccessResponse access,
            String ignoredActionCode) {
        if (access == null) {
            return false;
        }

        return WorkScope.FULL.name().equalsIgnoreCase(access.workScope());
    }

    //Danh sách các dự án mà người dùng hiện tại có quyền thực hiện một hành động cụ thể
    @Override
    public List<UUID> getCurrentUserProjectIdsWithAction(
            String actionCode) {
        Users user = currentUser.getCurrentUser();
        return userPermissionRepository.findProjectIdsByUserAndAction(
                user.getId(),
                actionCode
        );
    }

    
    @Override
    public ProjectAccessResponse getCurrentUserAccess(UUID projectId) {
        validateProjectExists(projectId);
        Users user = currentUser.getCurrentUser();

        //Kiểm tra xem người dùng hiện tại có phải là thành viên của dự án hay không
        boolean projectMember = projectMemberRepository.countByProjectIdAndUserId(projectId, user.getId()) > 0;
        if (!projectMember) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You cannot access this project");
        }

        //Lấy permission được gán cho người dùng hiện tại trong dự án
        UserPermission assignedPermission =
                userPermissionRepository
                        .findActiveByUserIdAndProjectId(
                                user.getId(),
                                projectId
                        );
        Set<String> allowedActions = new LinkedHashSet<>();
        WorkScope workScope = WorkScope.OWN;

        if (assignedPermission != null) {
            Permissions permission = assignedPermission.getPermission();

            if (permission != null
                    && Boolean.TRUE.equals(permission.getStatus())) {
                if (permission.getWorkScope() == WorkScope.FULL) {
                    workScope = WorkScope.FULL;
                }

                addActionCodes(permission, allowedActions);
            }
        }

        return new ProjectAccessResponse(
                projectId,
                user.getId(),
                projectMember,
                new ArrayList<>(allowedActions),
                workScope.name()
        );
    }

    //Kiểm tra action có tồn tại trong danh sách các action được phép hay không
    private boolean containsAction(
            Iterable<String> actions,
            String actionCode) {
        if (actions == null || actionCode == null) {
            return false;
        }

        for (String action : actions) {
            if (actionCode.equals(action)) {
                return true;
            }
        }

        return false;
    }

    //Kiểm tra xem dự án có tồn tại hay không
    private void validateProjectExists(UUID projectId) {
        if (projectId == null) {
            throw new BadHttpException("Project is required");
        }

        if (!projectRepository.existsById(projectId)) {
            throw new NotFoundException("Project not found");
        }
    }

    //Thêm các mã module đã được tích vào tập hợp actionCodes
    private void addActionCodes(
            Permissions permission,
            Set<String> actionCodes) {
        if (permission.getActions() == null) {
            return;
        }

        for (PermissionAction action : permission.getActions()) {
            if (action == null) {
                continue;
            }

            String actionCode = action.getActionCode();

            if (actionCode != null && !actionCode.isBlank()) {
                actionCodes.add(actionCode);
            }
        }
    }

}

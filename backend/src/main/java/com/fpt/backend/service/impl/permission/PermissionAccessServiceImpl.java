package com.fpt.backend.service.impl.permission;

import com.fpt.backend.dto.response.project.ProjectAccessResponse;
import com.fpt.backend.entity.PermissionAction;
import com.fpt.backend.entity.Permissions;
import com.fpt.backend.entity.Projects;
import com.fpt.backend.entity.UserPermission;
import com.fpt.backend.entity.Users;
import com.fpt.backend.enums.WorkScope;
import com.fpt.backend.exception.BadHttpException;
import com.fpt.backend.exception.NotFoundException;
import com.fpt.backend.repository.permission.UserPermissionRepository;
import com.fpt.backend.repository.project.ProjectMemberRepository;
import com.fpt.backend.repository.project.ProjectRepository;
import com.fpt.backend.service.impl.project.ProjectApprovalService;
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
    private final ProjectApprovalService projectApprovalService;
    private final CurrentUser currentUser;

    // Bảo đảm người dùng hiện tại có quyền truy cập cơ bản vào dự án.
    @Override
    public void requireProjectAccess(UUID projectId) {
        getCurrentUserAccess(projectId);
    }

    // Bảo đảm người dùng hiện tại có action bắt buộc trong dự án.
    @Override
    public void requireAction(UUID projectId, String actionCode) {
        ProjectAccessResponse access = getCurrentUserAccess(projectId);

        // Từ chối thao tác khi action yêu cầu không nằm trong quyền được cấp.
        if (!hasAction(access, actionCode)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    ACCESS_DENIED_MESSAGE + ": " + actionCode
            );
        }
    }

    // Kiểm tra dữ liệu truy cập có chứa action cụ thể hay không.
    @Override
    public boolean hasAction(
            ProjectAccessResponse access,
            String actionCode) {
        // Trả về false khi chưa có thông tin truy cập để kiểm tra.
        if (access == null) {
            return false;
        }

        return containsAction(access.allowedActions(), actionCode);
    }

    // Kiểm tra action được cấp phạm vi toàn dự án hay chỉ dữ liệu sở hữu.
    @Override
    public boolean hasFullWorkScope(
            ProjectAccessResponse access,
            String actionCode) {
        // Trả về false khi chưa có thông tin truy cập để xác định phạm vi.
        if (access == null) {
            return false;
        }

        // Phạm vi FULL luôn cho phép thao tác trên toàn bộ dữ liệu dự án.
        if (WorkScope.FULL.name().equalsIgnoreCase(access.workScope())) {
            return true;
        }

        return access.canViewAllProjectData()
                && isViewAction(actionCode);
    }

    // Lấy danh sách dự án mà người dùng hiện tại có action được yêu cầu.
    @Override
    public List<UUID> getCurrentUserProjectIdsWithAction(
            String actionCode) {
        Users user = currentUser.getCurrentUser();

        // Cho phép người duyệt cấp điều hành xem toàn bộ dự án với các action xem dữ liệu.
        if (projectApprovalService.canReviewProjects(user)
                && isViewAction(actionCode)) {
            List<UUID> projectIds = new ArrayList<>();

            for (Projects project : projectRepository.findAll()) {
                projectIds.add(project.getId());
            }

            return projectIds;
        }

        return userPermissionRepository.findProjectIdsByUserAndAction(
                user.getId(),
                actionCode
        );
    }

    // Tổng hợp vai trò thành viên, action và phạm vi truy cập của người dùng trong dự án.
    @Override
    public ProjectAccessResponse getCurrentUserAccess(UUID projectId) {
        Projects project = findProject(projectId);
        Users user = currentUser.getCurrentUser();
        boolean projectCreator = user.getId().equals(
                project.getProjectCreatedBy().getId()
        );

        // Kiểm tra người dùng hiện tại có phải thành viên của dự án hay không.
        boolean projectMember = projectMemberRepository
                .countByProjectIdAndUserId(
                        projectId,
                        user.getId()
                ) > 0;
        boolean executiveViewer = projectApprovalService
                .canReviewProjects(user);

        // Từ chối truy cập nếu người dùng không phải thành viên hoặc người duyệt điều hành.
        if (!projectMember && !executiveViewer) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You cannot access this project");
        }

        // Lấy quyền đang hoạt động được gán cho thành viên trong dự án.
        UserPermission assignedPermission = null;

        if (projectMember) {
            assignedPermission = userPermissionRepository
                    .findActiveByUserIdAndProjectId(
                            user.getId(),
                            projectId
                    );
        }
        Set<String> allowedActions = new LinkedHashSet<>();
        WorkScope workScope = WorkScope.OWN;

        // Tổng hợp action và phạm vi từ quyền đã gán nếu quyền còn hiệu lực.
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

        // Bổ sung các action chỉ xem dữ liệu cho người duyệt cấp điều hành.
        if (executiveViewer) {
            allowedActions.add("VIEW_TASKS");
            allowedActions.add("VIEW_DELIVERABLES");
            allowedActions.add("VIEW_CONTRACTS");
        }

        List<String> allowedActionList = new ArrayList<>(allowedActions);
        List<String> fullScopeActions = new ArrayList<>();

        // Cấp toàn bộ action cho phạm vi FULL và chỉ action xem cho người duyệt điều hành.
        if (workScope == WorkScope.FULL) {
            fullScopeActions.addAll(allowedActionList);
        } else if (executiveViewer) {
            for (String actionCode : allowedActionList) {
                if (isViewAction(actionCode)) {
                    fullScopeActions.add(actionCode);
                }
            }
        }

        return new ProjectAccessResponse(
                projectId,
                user.getId(),
                projectCreator,
                projectMember,
                executiveViewer,
                allowedActionList,
                fullScopeActions,
                workScope.name()
        );
    }

    // Kiểm tra action có tồn tại trong tập action được phép hay không.
    private boolean containsAction(
            Iterable<String> actions,
            String actionCode) {
        // Không đối chiếu khi danh sách hoặc action đầu vào không hợp lệ.
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

    // Kiểm tra mã dự án rồi trả về dự án tương ứng.
    private Projects findProject(UUID projectId) {
        // Yêu cầu mã dự án bắt buộc phải có.
        if (projectId == null) {
            throw new BadHttpException("Project is required");
        }

        return projectRepository.findById(projectId)
                .orElseThrow(() -> new NotFoundException("Project not found"));
    }

    // Thêm các mã action hợp lệ của một quyền vào tập kết quả.
    private void addActionCodes(
            Permissions permission,
            Set<String> actionCodes) {
        // Bỏ qua quyền chưa có danh sách action.
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

    // Xác định một mã action có phải action chỉ xem dữ liệu hay không.
    private boolean isViewAction(String actionCode) {
        return actionCode != null && actionCode.startsWith("VIEW_");
    }

}

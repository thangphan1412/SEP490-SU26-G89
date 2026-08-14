package com.fpt.backend.service.impl.permission;

import com.fpt.backend.dto.request.permission.PermissionListRequest;
import com.fpt.backend.dto.request.permission.PermissionRequest;
import com.fpt.backend.dto.response.permission.PermissionActionResponse;
import com.fpt.backend.dto.response.permission.PermissionDetailResponse;
import com.fpt.backend.dto.response.permission.PermissionListItemResponse;
import com.fpt.backend.dto.response.permission.PermissionListResponse;
import com.fpt.backend.dto.response.permission.PermissionProjectResponse;
import com.fpt.backend.dto.response.project.ProjectAccessResponse;
import com.fpt.backend.entity.Permissions;
import com.fpt.backend.entity.Projects;
import com.fpt.backend.entity.Users;
import com.fpt.backend.exception.BadHttpException;
import com.fpt.backend.exception.NotFoundException;
import com.fpt.backend.repository.permission.PermissionRepository;
import com.fpt.backend.repository.project.ProjectRepository;
import com.fpt.backend.service.impl.project.ProjectApprovalService;
import com.fpt.backend.service.interfaces.permission.IPermissionAccessService;
import com.fpt.backend.service.interfaces.permission.IPermissionService;
import com.fpt.backend.util.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PermissionServiceImpl implements IPermissionService {
    private static final int PAGE_SIZE = 7;
    private static final String DEFAULT_SORT_FIELD = "createdAt";
    private static final Set<String> SORT_FIELDS = Set.of(
            "id",
            "permissionName",
            "permissionCode",
            "permissionDescription",
            "status",
            "projectName",
            "createdAt");

    private final PermissionRepository permissionRepository;
    private final ProjectRepository projectRepository;
    private final PermissionActionService permissionActionService;
    private final IPermissionAccessService permissionAccessService;
    private final ProjectApprovalService projectApprovalService;
    private final CurrentUser currentUser;

    // Tìm kiếm và phân trang các quyền mà người dùng hiện tại được phép xem.
    @Override
    public PermissionListResponse getPermissions(PermissionListRequest request) {
        String search = normalize(request.search());
        Pageable pageable = createPageable(
                request.page(),
                request.sortBy(),
                request.sortDirection());
        Users user = currentUser.getCurrentUser();
        boolean canViewAllProjects = projectApprovalService
                .canReviewProjects(user);
        List<UUID> manageableProjectIds = permissionAccessService
                .getCurrentUserProjectIdsWithAction("MANAGE_MEMBERS");
        Page<Permissions> permissions = permissionRepository.searchPermissions(
                search.toLowerCase(Locale.ROOT),
                request.projectId(),
                request.status(),
                user.getId(),
                "MANAGE_MEMBERS",
                canViewAllProjects,
                pageable);
        List<PermissionListItemResponse> items = new ArrayList<>();

        for (Permissions permission : permissions.getContent()) {
            Projects project = permission.getProject();
            boolean canManage = project != null
                    && manageableProjectIds.contains(project.getId());
            items.add(toListItem(permission, canManage));
        }

        return new PermissionListResponse(
                items,
                permissions.getNumber(),
                permissions.getSize(),
                permissions.getTotalElements(),
                permissions.getTotalPages(),
                permissions.isFirst(),
                permissions.isLast());
    }

    // Lấy chi tiết quyền và kiểm tra quyền xem của người dùng hiện tại.
    @Override
    public PermissionDetailResponse getPermissionById(UUID id) {
        Permissions permission = findPermission(id);
        ProjectAccessResponse access = getPermissionAccess(permission);
        boolean canManage = permissionAccessService.hasAction(
                access,
                "MANAGE_MEMBERS"
        );

        // Từ chối khi người dùng không thể quản lý hoặc xem toàn bộ dữ liệu dự án.
        if (!canManage && !access.canViewAllProjectData()) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You cannot view this permission"
            );
        }

        return toDetail(permission, canManage);
    }

    // Tạo quyền mới sau khi kiểm tra và chuẩn hóa toàn bộ dữ liệu đầu vào.
    @Override
    @Transactional
    public PermissionDetailResponse createPermission(PermissionRequest request) {
        Permissions permission = new Permissions();
        applyRequest(permission, request, null);
        return toDetail(permissionRepository.save(permission), true);
    }

    // Cập nhật quyền hiện có sau khi xác minh quyền quản lý thành viên.
    @Override
    @Transactional
    public PermissionDetailResponse updatePermission(UUID id, PermissionRequest request) {
        Permissions permission = findPermission(id);
        requireManagePermission(permission);
        applyRequest(permission, request, id);
        return toDetail(permissionRepository.save(permission), true);
    }

    // Xóa quyền hiện có khi người dùng có action quản lý thành viên.
    @Override
    @Transactional
    public void deletePermission(UUID id) {
        Permissions permission = findPermission(id);
        requireManagePermission(permission);
        permissionRepository.delete(permission);
    }

    // Lấy danh sách dự án mà người dùng có thể chọn khi cấu hình quyền.
    @Override
    public List<PermissionProjectResponse> getProjectsForPermissionSelection() {
        Users user = currentUser.getCurrentUser();
        List<UUID> projectIds = permissionAccessService
                .getCurrentUserProjectIdsWithAction("MANAGE_MEMBERS");
        List<Projects> projects;

        // Người duyệt điều hành thấy mọi dự án, người dùng khác chỉ thấy dự án được quản lý.
        if (projectApprovalService.canReviewProjects(user)) {
            projects = projectRepository.findAll();
        } else {
            projects = projectRepository.findAllById(projectIds);
        }
        // Sắp xếp dự án theo tên mà không phân biệt chữ hoa chữ thường.
        projects.sort(
                Comparator.comparing(
                        Projects::getProjectName,
                        String.CASE_INSENSITIVE_ORDER));
                        
        List<PermissionProjectResponse> responses = new ArrayList<>();

        for (Projects project : projects) {
            responses.add(new PermissionProjectResponse(
                    project.getId(),
                    project.getProjectCode(),
                    project.getProjectName(),
                    projectIds.contains(project.getId())));
        }

        return responses;
    }

    // Lấy danh sách action có thể gán từ permission catalog.
    @Override
    public List<PermissionActionResponse> getAvailableActions() {
        return permissionActionService.getAvailableActions();
    }

    // Kiểm tra request rồi áp dụng thông tin, dự án và action vào entity quyền.
    private void applyRequest(Permissions permission, PermissionRequest request, UUID currentId) {
        // Yêu cầu payload quyền phải tồn tại.
        if (request == null) {
            throw new BadHttpException("Permission information is required");
        }

        String permissionName = requireText(request.permissionName(), "Permission name is required", 50);
        String permissionCode = requireText(request.permissionCode(), "Permission code is required", 50);
        String permissionDescription = normalize(request.permissionDescription());
        validateMaxLength(permissionDescription, "Permission description", 255);
        Projects project = findProject(request.projectId());
        permissionAccessService.requireAction(
                project.getId(),
                "MANAGE_MEMBERS");

        boolean duplicateCode = currentId == null
                ? permissionRepository.existsByPermissionCodeIgnoreCase(permissionCode)
                : permissionRepository.existsByPermissionCodeIgnoreCaseAndIdNot(permissionCode, currentId);

        // Ngăn tạo hoặc cập nhật thành mã quyền đã được sử dụng.
        if (duplicateCode) {
            throw new BadHttpException("Permission code already exists");
        }

        permission.setPermissionName(permissionName);
        permission.setPermissionCode(permissionCode);
        permission.setPermissionDescription(permissionDescription);
        permission.setStatus(request.status() == null || request.status());
        permission.setProject(project);

        permissionActionService.configurePermission(
                permission,
                request.allowedActions(),
                request.workScope());

        // Chỉ thiết lập thời điểm tạo cho bản ghi mới.
        if (permission.getCreatedAt() == null) {
            permission.setCreatedAt(LocalDateTime.now());
        }
    }

    // Tìm quyền theo mã định danh hoặc báo không tìm thấy.
    private Permissions findPermission(UUID id) {
        Optional<Permissions> permission = permissionRepository.findById(id);

        // Báo lỗi khi quyền không tồn tại.
        if (permission.isEmpty()) {
            throw new NotFoundException("Permission not found");
        }

        return permission.get();
    }

    // Kiểm tra mã dự án rồi trả về dự án tương ứng.
    private Projects findProject(UUID projectId) {
        // Yêu cầu mã dự án bắt buộc phải có.
        if (projectId == null) {
            throw new BadHttpException("Project is required");
        }

        Optional<Projects> project = projectRepository.findById(projectId);

        // Báo lỗi khi dự án không tồn tại.
        if (project.isEmpty()) {
            throw new NotFoundException("Project not found");
        }

        return project.get();
    }

    // Bảo đảm người dùng có action quản lý thành viên của dự án chứa quyền.
    private void requireManagePermission(Permissions permission) {
        Projects project = requirePermissionProject(permission);
        permissionAccessService.requireAction(
                project.getId(),
                "MANAGE_MEMBERS");
    }

    // Lấy thông tin truy cập dự án liên kết với quyền.
    private ProjectAccessResponse getPermissionAccess(
            Permissions permission) {
        Projects project = requirePermissionProject(permission);
        return permissionAccessService.getCurrentUserAccess(
                project.getId()
        );
    }

    // Lấy dự án liên kết và từ chối quyền không thuộc dự án nào.
    private Projects requirePermissionProject(Permissions permission) {
        Projects project = permission.getProject();

        // Bảo đảm mọi quyền nghiệp vụ đều được gắn với một dự án.
        if (project == null) {
            throw new BadHttpException(
                    "Permission is not connected to a project");
        }

        return project;
    }

    // Chuẩn hóa trang, trường sắp xếp và chiều sắp xếp cho truy vấn quyền.
    private Pageable createPageable(int page, String sortBy, String sortDirection) {
        int validPage = Math.max(page, 0);
        String sortField = sortBy != null && SORT_FIELDS.contains(sortBy) ? sortBy : DEFAULT_SORT_FIELD;

        // Ánh xạ trường hiển thị projectName sang đường dẫn thuộc tính entity.
        if ("projectName".equals(sortField)) {
            sortField = "project.projectName";
        }

        Sort.Direction direction = "asc".equalsIgnoreCase(sortDirection)
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;

        return PageRequest.of(validPage, PAGE_SIZE, Sort.by(direction, sortField));
    }

    // Chuẩn hóa trường bắt buộc và kiểm tra độ dài tối đa.
    private String requireText(String value, String missingMessage, int maxLength) {
        String normalizedValue = normalize(value);

        // Từ chối giá trị rỗng sau khi loại bỏ khoảng trắng thừa.
        if (normalizedValue.isBlank()) {
            throw new BadHttpException(missingMessage);
        }

        // Từ chối giá trị vượt quá độ dài cho phép.
        if (normalizedValue.length() > maxLength) {
            throw new BadHttpException(
                    "Value must not be longer than " + maxLength + " characters");
        }

        return normalizedValue;
    }

    // Kiểm tra một chuỗi không vượt quá độ dài tối đa của trường.
    private void validateMaxLength(String value, String fieldName, int maxLength) {
        // Từ chối giá trị dài hơn giới hạn lưu trữ của trường.
        if (value.length() > maxLength) {
            throw new BadHttpException(
                    fieldName + " must not be longer than " + maxLength + " characters");
        }
    }

    // Chuẩn hóa chuỗi null thành rỗng và loại bỏ khoảng trắng hai đầu.
    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }

    // Chuyển entity quyền thành phần tử hiển thị trong danh sách.
    private PermissionListItemResponse toListItem(
            Permissions permission,
            boolean canManage) {
        Projects project = permission.getProject();

        return new PermissionListItemResponse(
                permission.getId(),
                permission.getPermissionName(),
                permission.getPermissionCode(),
                permission.getPermissionDescription(),
                permission.getStatus(),
                project == null ? null : project.getId(),
                project == null ? null : project.getProjectCode(),
                project == null ? null : project.getProjectName(),
                permission.getCreatedAt(),
                canManage);
    }

    // Chuyển entity quyền thành dữ liệu chi tiết trả về cho client.
    private PermissionDetailResponse toDetail(
            Permissions permission,
            boolean canManage) {
        Projects project = permission.getProject();

        return new PermissionDetailResponse(
                permission.getId(),
                permission.getPermissionName(),
                permission.getPermissionCode(),
                permissionActionService.getAllowedActionCodes(permission),
                permissionActionService.getActionDetails(permission),
                permissionActionService.getWorkScope(permission),
                permission.getPermissionDescription(),
                permission.getStatus(),
                project == null ? null : project.getId(),
                project == null ? null : project.getProjectCode(),
                project == null ? null : project.getProjectName(),
                permission.getCreatedAt(),
                canManage);
    }
}

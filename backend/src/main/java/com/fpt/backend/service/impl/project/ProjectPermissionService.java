package com.fpt.backend.service.impl.project;

import com.fpt.backend.dto.request.project.ProjectPermissionConfigurationRequest;
import com.fpt.backend.dto.response.project.ProjectPermissionConfigurationResponse;
import com.fpt.backend.dto.response.project.ProjectPermissionOptionResponse;
import com.fpt.backend.entity.Permissions;
import com.fpt.backend.entity.Projects;
import com.fpt.backend.exception.BadHttpException;
import com.fpt.backend.exception.NotFoundException;
import com.fpt.backend.repository.permission.PermissionRepository;
import com.fpt.backend.service.impl.permission.PermissionActionService;
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
public class ProjectPermissionService {
    private static final String FULL_ACCESS_PERMISSION_NAME =
            "Project Full Access";
    private static final String FULL_ACCESS_PERMISSION_CODE_PREFIX = "PFA_";
    private final PermissionRepository permissionRepository;
    private final PermissionActionService permissionActionService;

    // Tạo quyền toàn phần mặc định cho người tạo dự án.
    public UUID createProjectFullAccessPermission(Projects project) {
        // Yêu cầu dự án phải được lưu trước khi tạo quyền liên kết.
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
        permissionActionService.configureFullAccess(permission);
        permission.setStatus(true);
        permission.setCreatedAt(LocalDateTime.now());
        permission.setProject(project);

        return permissionRepository.save(permission).getId();
    }

    // Lấy toàn bộ cấu hình quyền của một dự án.
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

    // Cập nhật action và work scope của một quyền thuộc dự án.
    public ProjectPermissionConfigurationResponse configure(
            Projects project,
            UUID permissionId,
            ProjectPermissionConfigurationRequest request) {
        // Yêu cầu payload cấu hình quyền phải tồn tại.
        if (request == null) {
            throw new BadHttpException(
                    "Permission configuration is required"
            );
        }

        Optional<Permissions> optionalPermission =
                permissionRepository.findByIdAndProjectId(
                        permissionId,
                        project.getId()
                );

        // Từ chối quyền không tồn tại hoặc không thuộc dự án được yêu cầu.
        if (optionalPermission.isEmpty()) {
            throw new NotFoundException(
                    "Permission does not belong to this project"
            );
        }

        Permissions permission = optionalPermission.get();
        permissionActionService.configurePermission(
                permission,
                request.allowedActions(),
                request.workScope()
        );

        return toConfiguration(permissionRepository.save(permission));
    }

    // Lấy danh sách quyền có thể chọn khi gán cho thành viên dự án.
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

    // Xóa toàn bộ quyền thuộc dự án trước khi xóa dự án.
    public void deleteProjectData(UUID projectId) {
        List<Permissions> permissions =
                permissionRepository.findByProjectId(projectId);
        permissionRepository.deleteAll(permissions);
        permissionRepository.flush();
    }

    // Chuyển entity quyền thành cấu hình quyền trả về cho client.
    private ProjectPermissionConfigurationResponse toConfiguration(
            Permissions permission) {
        return new ProjectPermissionConfigurationResponse(
                permission.getId(),
                getPermissionName(permission),
                permission.getPermissionCode(),
                permission.getPermissionDescription(),
                permission.getStatus(),
                permissionActionService.getAllowedActionCodes(permission),
                permissionActionService.getWorkScope(permission)
        );
    }

    // Lấy tên hiển thị của quyền với mã quyền và id làm giá trị dự phòng.
    private String getPermissionName(Permissions permission) {
        String name = normalize(permission.getPermissionName());

        // Ưu tiên tên quyền khi đã được cấu hình.
        if (!name.isBlank()) {
            return name;
        }

        String code = normalize(permission.getPermissionCode());

        // Dùng mã quyền khi tên quyền bị trống.
        if (!code.isBlank()) {
            return code;
        }

        return "Permission #" + permission.getId();
    }

    // Chuẩn hóa chuỗi null thành rỗng và loại bỏ khoảng trắng hai đầu.
    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }

}

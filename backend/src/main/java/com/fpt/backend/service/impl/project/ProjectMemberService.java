package com.fpt.backend.service.impl.project;

import com.fpt.backend.dto.request.project.ProjectMemberRequest;
import com.fpt.backend.dto.response.project.ProjectEmployeeResponse;
import com.fpt.backend.dto.response.project.ProjectUserResponse;
import com.fpt.backend.entity.Permissions;
import com.fpt.backend.entity.ProjectMember;
import com.fpt.backend.entity.Projects;
import com.fpt.backend.entity.UserPermission;
import com.fpt.backend.entity.Users;
import com.fpt.backend.exception.BadHttpException;
import com.fpt.backend.exception.NotFoundException;
import com.fpt.backend.repository.permission.PermissionRepository;
import com.fpt.backend.repository.permission.UserPermissionRepository;
import com.fpt.backend.repository.project.ProjectMemberRepository;
import com.fpt.backend.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProjectMemberService {
    private static final ZoneId DATABASE_TIME_ZONE = ZoneId.of("UTC");
    private static final ZoneId PROJECT_TIME_ZONE =
            ZoneId.of("Asia/Ho_Chi_Minh");

    private final ProjectMemberRepository projectMemberRepository;
    private final UserPermissionRepository userPermissionRepository;
    private final PermissionRepository permissionRepository;
    private final UserRepository userRepository;

    // Lấy danh sách nhân viên có thể chọn khi tạo hoặc cập nhật dự án.
    public List<ProjectEmployeeResponse> getEmployeesForSelection() {
        List<Users> users = userRepository.findAll(
                Sort.by(
                        Sort.Direction.ASC,
                        "firstName",
                        "lastName",
                        "email"
                )
        );
        List<ProjectEmployeeResponse> employees = new ArrayList<>();

        for (Users user : users) {
            employees.add(new ProjectEmployeeResponse(
                    user.getId(),
                    user.getEmail(),
                    user.getFirstName(),
                    user.getLastName(),
                    user.getStatus()
            ));
        }

        return employees;
    }

    // Đồng bộ danh sách thành viên và quyền được gán theo request của dự án.
    public void syncMembers(
            Projects project,
            List<ProjectMemberRequest> memberRequests,
            boolean keepExistingWhenMissing) {
        // Giữ nguyên thành viên khi request cập nhật không gửi trường thành viên.
        if (memberRequests == null && keepExistingWhenMissing) {
            return;
        }

        List<ProjectMemberRequest> requests = memberRequests == null
                ? List.of()
                : memberRequests;
        Map<UUID, ProjectMemberRequest> requestByUserId =
                validateMemberRequests(requests);
        Map<UUID, ProjectMember> existingMemberByUserId =
                findExistingMembers(project.getId());

        userPermissionRepository.deleteByProjectId(project.getId());

        for (ProjectMemberRequest request : requestByUserId.values()) {
            Users user = findUser(request.userId());
            ProjectMember member = existingMemberByUserId.remove(user.getId());

            // Tạo liên kết thành viên mới khi người dùng chưa thuộc dự án.
            if (member == null) {
                member = new ProjectMember();
                member.setProject(project);
                member.setUser(user);
                member.setJoinDate(getCurrentDatabaseDateTime());
                projectMemberRepository.save(member);
            }

            // Gán quyền được chọn khi request có permission id.
            if (request.permissionId() != null) {
                Permissions permission = resolvePermission(
                        project,
                        request.permissionId()
                );
                UserPermission userPermission = new UserPermission();
                userPermission.setUser(user);
                userPermission.setPermission(permission);
                userPermissionRepository.save(userPermission);
            }
        }

        for (ProjectMember removedMember : existingMemberByUserId.values()) {
            projectMemberRepository.delete(removedMember);
        }
    }

    // Lấy danh sách người dùng của dự án cùng quyền đang được gán.
    public List<ProjectUserResponse> getProjectUsers(UUID projectId) {
        List<ProjectMember> projectMembers =
                projectMemberRepository.findByProjectId(projectId);
        Map<UUID, Permissions> permissionByUserId = new LinkedHashMap<>();

        for (UserPermission userPermission
                : userPermissionRepository.findByProjectId(projectId)) {
            Users user = userPermission.getUser();
            permissionByUserId.putIfAbsent(
                    user.getId(),
                    userPermission.getPermission()
            );
        }

        List<ProjectUserResponse> users = new ArrayList<>();

        for (ProjectMember member : projectMembers) {
            Users user = member.getUser();
            Permissions permission = permissionByUserId.get(user.getId());
            users.add(toProjectUser(user, member, permission));
        }

        users.sort(Comparator.comparing(
                ProjectUserResponse::getUserName,
                Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)
        ));
        return users;
    }

    // Xóa toàn bộ liên kết thành viên trước khi xóa dự án.
    public void deleteProjectData(UUID projectId) {
        projectMemberRepository.deleteByProjectId(projectId);
    }

    // Lập bản đồ yêu cầu thành viên và từ chối người dùng bị trùng.
    private Map<UUID, ProjectMemberRequest> validateMemberRequests(
            List<ProjectMemberRequest> requests) {
        Map<UUID, ProjectMemberRequest> requestByUserId =
                new LinkedHashMap<>();

        for (ProjectMemberRequest request : requests) {
            ProjectMemberRequest duplicate = requestByUserId.putIfAbsent(
                    request.userId(),
                    request
            );

            // Ngăn một người dùng xuất hiện nhiều lần trong cùng request.
            if (duplicate != null) {
                throw new BadHttpException(
                        "A user cannot be added to the same project more than once"
                );
            }
        }

        return requestByUserId;
    }

    // Lập bản đồ thành viên hiện có và dọn các liên kết bị trùng trong dữ liệu cũ.
    private Map<UUID, ProjectMember> findExistingMembers(UUID projectId) {
        Map<UUID, ProjectMember> existingMemberByUserId =
                new LinkedHashMap<>();

        for (ProjectMember member
                : projectMemberRepository.findByProjectId(projectId)) {
            UUID userId = member.getUser().getId();
            ProjectMember duplicate = existingMemberByUserId.putIfAbsent(
                    userId,
                    member
            );

            // Xóa liên kết dư thừa khi dữ liệu cũ có cùng người dùng nhiều lần.
            if (duplicate != null) {
                projectMemberRepository.delete(member);
            }
        }

        return existingMemberByUserId;
    }

    // Tìm người dùng theo mã định danh hoặc báo không tìm thấy.
    private Users findUser(UUID userId) {
        Optional<Users> optionalUser = userRepository.findById(userId);

        // Báo lỗi khi người dùng được chọn không tồn tại.
        if (optionalUser.isEmpty()) {
            throw new NotFoundException("User not found with id: " + userId);
        }

        return optionalUser.get();
    }

    // Xác minh và lấy quyền thuộc đúng dự án đang cấu hình.
    private Permissions resolvePermission(
            Projects project,
            UUID permissionId) {
        Optional<Permissions> permission =
                permissionRepository.findByIdAndProjectId(
                        permissionId,
                        project.getId()
                );

        // Từ chối quyền không tồn tại hoặc thuộc dự án khác.
        if (permission.isEmpty()) {
            throw new BadHttpException(
                    "Selected permission does not belong to this project"
            );
        }

        return permission.get();
    }

    // Chuyển thành viên và quyền được gán thành dữ liệu trả về cho client.
    private ProjectUserResponse toProjectUser(
            Users user,
            ProjectMember member,
            Permissions permission) {
        UUID permissionId = null;
        String permissionName = "Not assigned";
        String permissionCode = null;
        LocalDate joinDate = toProjectJoinDate(member.getJoinDate());

        // Bổ sung thông tin quyền khi thành viên đã được gán quyền.
        if (permission != null) {
            permissionId = permission.getId();
            permissionName = getPermissionName(permission);
            permissionCode = permission.getPermissionCode();
        }

        return new ProjectUserResponse(
                user.getId(),
                user.getEmail(),
                getUserName(user),
                user.getStatus(),
                joinDate,
                permissionId,
                permissionName,
                permissionCode
        );
    }

    // Lấy tên hiển thị của người dùng với email và id làm giá trị dự phòng.
    private String getUserName(Users user) {
        String fullName = (
                normalize(user.getFirstName())
                        + " "
                        + normalize(user.getLastName())
        ).trim();

        // Ưu tiên họ tên khi người dùng đã cung cấp đầy đủ.
        if (!fullName.isBlank()) {
            return fullName;
        }

        String email = normalize(user.getEmail());

        // Dùng email khi họ tên bị trống.
        if (!email.isBlank()) {
            return email;
        }

        return "User #" + user.getId();
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

    // Lấy thời điểm hiện tại theo múi giờ lưu trữ của cơ sở dữ liệu.
    private Date getCurrentDatabaseDateTime() {
        LocalDateTime utcDateTime =
                LocalDateTime.now(DATABASE_TIME_ZONE);
        return java.sql.Timestamp.valueOf(utcDateTime);
    }

    // Chuyển thời điểm tham gia từ UTC sang ngày theo múi giờ dự án.
    private LocalDate toProjectJoinDate(Date value) {
        // Giữ nguyên giá trị thiếu thay vì phát sinh lỗi chuyển đổi.
        if (value == null) {
            return null;
        }

        LocalDateTime databaseDateTime;

        if (value instanceof java.sql.Timestamp timestamp) {
            databaseDateTime = timestamp.toLocalDateTime();
        } else {
            databaseDateTime = LocalDateTime.ofInstant(
                    value.toInstant(),
                    ZoneId.systemDefault()
            );
        }

        return databaseDateTime
                .atZone(DATABASE_TIME_ZONE)
                .withZoneSameInstant(PROJECT_TIME_ZONE)
                .toLocalDate();
    }

    // Chuẩn hóa chuỗi null thành rỗng và loại bỏ khoảng trắng hai đầu.
    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }

}

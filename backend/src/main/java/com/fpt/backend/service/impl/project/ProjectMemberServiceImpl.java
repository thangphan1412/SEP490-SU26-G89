package com.fpt.backend.service.impl.project;

import com.fpt.backend.dto.request.project.ProjectMemberRequest;
import com.fpt.backend.dto.response.project.ProjectEmployeeResponse;
import com.fpt.backend.dto.response.project.ProjectRoleResponse;
import com.fpt.backend.dto.response.project.ProjectUserResponse;
import com.fpt.backend.entity.Permissions;
import com.fpt.backend.entity.ProjectMember;
import com.fpt.backend.entity.Projects;
import com.fpt.backend.entity.Role;
import com.fpt.backend.entity.UserPermission;
import com.fpt.backend.entity.UserRole;
import com.fpt.backend.entity.Users;
import com.fpt.backend.exception.BadHttpException;
import com.fpt.backend.exception.NotFoundException;
import com.fpt.backend.repository.permission.PermissionRepository;
import com.fpt.backend.repository.permission.RoleRepository;
import com.fpt.backend.repository.permission.UserPermissionRepository;
import com.fpt.backend.repository.project.ProjectMemberRepository;
import com.fpt.backend.repository.project.ProjectUserRoleRepository;
import com.fpt.backend.repository.user.UserRepository;
import com.fpt.backend.service.interfaces.project.ProjectMemberService;
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
public class ProjectMemberServiceImpl implements ProjectMemberService {
    private static final ZoneId DATABASE_TIME_ZONE = ZoneId.of("UTC");
    private static final ZoneId PROJECT_TIME_ZONE =
            ZoneId.of("Asia/Ho_Chi_Minh");

    private final ProjectMemberRepository projectMemberRepository;
    private final ProjectUserRoleRepository projectUserRoleRepository;
    private final UserPermissionRepository userPermissionRepository;
    private final PermissionRepository permissionRepository;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;

    @Override
    public List<ProjectEmployeeResponse> getEmployeesForSelection() {
        List<Users> users = userRepository.findAll(
                Sort.by(
                        Sort.Direction.ASC,
                        "firstName",
                        "lastName",
                        "email"
                )
        );
        Map<UUID, List<ProjectRoleResponse>> rolesByUserId =
                findRolesByUserId();
        List<ProjectEmployeeResponse> employees = new ArrayList<>();

        for (Users user : users) {
            List<ProjectRoleResponse> userRoles =
                    rolesByUserId.get(user.getId());

            if (userRoles == null) {
                userRoles = List.of();
            }

            employees.add(new ProjectEmployeeResponse(
                    user.getId(),
                    user.getEmail(),
                    user.getFirstName(),
                    user.getLastName(),
                    userRoles,
                    user.getStatus()
            ));
        }

        return employees;
    }

    @Override
    public List<ProjectRoleResponse> getRolesForFilter() {
        List<Role> roles = roleRepository.findAllForSelection();
        List<ProjectRoleResponse> responses = new ArrayList<>();

        for (Role role : roles) {
            responses.add(new ProjectRoleResponse(
                    role.getId(),
                    role.getRoleName()
            ));
        }

        return responses;
    }

    @Override
    public void syncMembers(
            Projects project,
            List<ProjectMemberRequest> memberRequests,
            boolean keepExistingWhenMissing) {
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

            if (member == null) {
                member = new ProjectMember();
                member.setProject(project);
                member.setUser(user);
                member.setJoinDate(getCurrentDatabaseDateTime());
                projectMemberRepository.save(member);
            }

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

    @Override
    public List<ProjectUserResponse> getProjectUsers(UUID projectId) {
        Map<UUID, ProjectMember> memberByUserId = new LinkedHashMap<>();
        Map<UUID, Users> userById = new LinkedHashMap<>();
        Map<UUID, Permissions> permissionByUserId = new LinkedHashMap<>();

        for (ProjectMember member : projectMemberRepository.findByProjectId(projectId)) {
            Users user = member.getUser();
            memberByUserId.putIfAbsent(user.getId(), member);
            userById.putIfAbsent(user.getId(), user);
        }

        for (UserPermission userPermission
                : userPermissionRepository.findByProjectId(projectId)) {
            Users user = userPermission.getUser();
            userById.putIfAbsent(user.getId(), user);
            permissionByUserId.putIfAbsent(
                    user.getId(),
                    userPermission.getPermission()
            );
        }

        List<ProjectUserResponse> users = new ArrayList<>();

        for (Users user : userById.values()) {
            ProjectMember member = memberByUserId.get(user.getId());
            Permissions permission = permissionByUserId.get(user.getId());
            users.add(toProjectUser(user, member, permission));
        }

        users.sort(Comparator.comparing(
                ProjectUserResponse::userName,
                Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)
        ));
        return users;
    }

    @Override
    public void deleteProjectData(UUID projectId) {
        projectMemberRepository.deleteByProjectId(projectId);
    }

    private Map<UUID, ProjectMemberRequest> validateMemberRequests(
            List<ProjectMemberRequest> requests) {
        Map<UUID, ProjectMemberRequest> requestByUserId =
                new LinkedHashMap<>();

        for (ProjectMemberRequest request : requests) {
            if (request == null || request.userId() == null) {
                throw new BadHttpException("A valid user is required");
            }

            ProjectMemberRequest duplicate = requestByUserId.putIfAbsent(
                    request.userId(),
                    request
            );

            if (duplicate != null) {
                throw new BadHttpException(
                        "A user cannot be added to the same project more than once"
                );
            }
        }

        return requestByUserId;
    }

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

            if (duplicate != null) {
                projectMemberRepository.delete(member);
            }
        }

        return existingMemberByUserId;
    }

    private Map<UUID, List<ProjectRoleResponse>> findRolesByUserId() {
        List<UserRole> userRoles =
                projectUserRoleRepository.findAllWithUserAndRole();
        Map<UUID, List<ProjectRoleResponse>> rolesByUserId =
                new LinkedHashMap<>();

        for (UserRole userRole : userRoles) {
            UUID userId = userRole.getUser().getId();
            Role role = userRole.getRole();
            List<ProjectRoleResponse> roles = rolesByUserId.get(userId);

            if (roles == null) {
                roles = new ArrayList<>();
                rolesByUserId.put(userId, roles);
            }

            if (!containsRole(roles, role.getId())) {
                roles.add(new ProjectRoleResponse(
                        role.getId(),
                        role.getRoleName()
                ));
            }
        }

        return rolesByUserId;
    }

    private boolean containsRole(
            List<ProjectRoleResponse> roles,
            UUID roleId) {
        for (ProjectRoleResponse existingRole : roles) {
            if (existingRole.id().equals(roleId)) {
                return true;
            }
        }

        return false;
    }

    private Users findUser(UUID userId) {
        Optional<Users> optionalUser = userRepository.findById(userId);

        if (optionalUser.isEmpty()) {
            throw new NotFoundException("User not found with id: " + userId);
        }

        return optionalUser.get();
    }

    private Permissions resolvePermission(
            Projects project,
            UUID permissionId) {
        Optional<Permissions> permission =
                permissionRepository.findByIdAndProjectId(
                        permissionId,
                        project.getId()
                );

        if (permission.isEmpty()) {
            throw new BadHttpException(
                    "Selected permission does not belong to this project"
            );
        }

        return permission.get();
    }

    private ProjectUserResponse toProjectUser(
            Users user,
            ProjectMember member,
            Permissions permission) {
        UUID permissionId = null;
        String permissionName = "Not assigned";
        String permissionCode = null;
        LocalDate joinDate = null;

        if (member != null) {
            joinDate = toProjectJoinDate(member.getJoinDate());
        }

        if (permission != null) {
            permissionId = permission.getId();
            permissionName = getPermissionName(permission);
            permissionCode = permission.getPermissionCode();
        }

        return new ProjectUserResponse(
                user.getId(),
                user.getEmail(),
                getUserName(user),
                user.getRole(),
                user.getStatus(),
                joinDate,
                permissionId,
                permissionName,
                permissionCode
        );
    }

    private String getUserName(Users user) {
        String fullName = (
                normalize(user.getFirstName())
                        + " "
                        + normalize(user.getLastName())
        ).trim();

        if (!fullName.isBlank()) {
            return fullName;
        }

        String email = normalize(user.getEmail());

        if (!email.isBlank()) {
            return email;
        }

        return "User #" + user.getId();
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

    private Date getCurrentDatabaseDateTime() {
        LocalDateTime utcDateTime =
                LocalDateTime.now(DATABASE_TIME_ZONE);
        return java.sql.Timestamp.valueOf(utcDateTime);
    }

    private LocalDate toProjectJoinDate(Date value) {
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

    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }
}

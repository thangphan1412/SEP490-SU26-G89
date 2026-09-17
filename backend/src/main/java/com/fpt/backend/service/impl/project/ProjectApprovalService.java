package com.fpt.backend.service.impl.project;

import com.fpt.backend.dto.response.project.ProjectApprovalAccessResponse;
import com.fpt.backend.entity.Approvals;
import com.fpt.backend.entity.Proposals;
import com.fpt.backend.entity.Projects;
import com.fpt.backend.entity.Role;
import com.fpt.backend.entity.UserRole;
import com.fpt.backend.entity.Users;
import com.fpt.backend.exception.BadHttpException;
import com.fpt.backend.repository.project.ProjectApprovalRepository;
import com.fpt.backend.repository.project.ProjectProposalRepository;
import com.fpt.backend.repository.project.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProjectApprovalService {

    private static final String ON_HOLD_STATUS = "On Hold";
    private static final String PLANNING_STATUS = "Planning";
    private static final String PENDING_STATUS = "PENDING";
    private static final String APPROVED_STATUS = "APPROVED";
    private static final String CEO_LEVEL = "CEO";
    private static final String DEPARTMENT_LEVEL = "HEAD_OF_DEPARTMENT";
    private static final String ADMINISTRATIVE_DEPARTMENT = "Administrative";
    private static final ZoneId APP_TIME_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

    private final ProjectProposalRepository proposalRepository;
    private final ProjectApprovalRepository approvalRepository;
    private final ProjectRepository projectRepository;
    private final ProjectStatusService projectStatusService;

    // Tạo yêu cầu phê duyệt cho dự án nếu yêu cầu chưa tồn tại.
    public void createApprovalRequest(
            Projects project,
            Users requestedBy) {
        getOrCreateProposal(project, requestedBy);
    }

    // Quyền xem cấp điều hành; dự án On Hold còn phải qua kiểm tra trạng thái phê duyệt.
    public boolean canReviewProjects(Users user) {
        return hasRole(user, CEO_LEVEL) || hasRole(user, "HeadOfDepartment");
    }

    // Quyền xem dự án đang chờ duyệt không phụ thuộc việc người dùng đã approve hay chưa.
    public boolean canViewPendingProjects(Users user) {
        return findApprovalLevel(user) != null;
    }

    // On Hold chỉ dành cho CEO và trưởng phòng Administrative; trạng thái khác giữ quyền cũ.
    public boolean canAccessProjectByApprovalStatus(Projects project, Users user) {
        return !ON_HOLD_STATUS.equalsIgnoreCase(project.getProjectStatus())
                || canViewPendingProjects(user);
    }

    // CEO phải chờ trưởng phòng Administrative; người không có quyền không thấy nút.
    public ProjectApprovalAccessResponse getApprovalAccess(Projects project, Users user) {
        String approvalLevel = findApprovalLevel(user);

        if (approvalLevel == null || !ON_HOLD_STATUS.equalsIgnoreCase(project.getProjectStatus())) {
            return new ProjectApprovalAccessResponse(false, false);
        }

        Optional<Proposals> proposal = proposalRepository.findProjectApprovalProposal(
                project.getId(),
                createProposalCode(project));

        boolean departmentApproved = proposal
                .map(value -> isDepartmentApproved(value.getId()))
                .orElse(false);

        if (CEO_LEVEL.equals(approvalLevel)) {
            return new ProjectApprovalAccessResponse(departmentApproved, !departmentApproved);
        }

        return new ProjectApprovalAccessResponse(!departmentApproved, false);
    }

    // Ghi nhận lượt phê duyệt và chuyển trạng thái khi đủ các cấp bắt buộc.
    public void approveProject(
            Projects project,
            Users approvedBy) {
        // Chỉ dự án On Hold mới được đưa qua quy trình phê duyệt.
        if (!ON_HOLD_STATUS.equalsIgnoreCase(
                project.getProjectStatus())) {
            throw new BadHttpException(
                    "Only On Hold projects can be approved");
        }

        String approvalLevel = findApprovalLevel(approvedBy);

        // Chỉ trưởng phòng Administrative mới được thực hiện bước duyệt đầu tiên.
        if (approvalLevel == null) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only CEO or HeadOfDepartment of Administrative can approve projects");
        }

        Optional<Proposals> existingProposal = proposalRepository.findProjectApprovalProposal(
                project.getId(), createProposalCode(project));
        boolean departmentApproved = existingProposal
                .map(value -> isDepartmentApproved(value.getId()))
                .orElse(false);

        // Kiểm tra ở backend để không thể bỏ qua thứ tự bằng cách gọi API trực tiếp.
        if (CEO_LEVEL.equals(approvalLevel) && !departmentApproved) {
            throw new BadHttpException(
                    "Waiting for HeadOfDepartment of Administrative to approve first");
        }

        if (DEPARTMENT_LEVEL.equals(approvalLevel) && departmentApproved) {
            throw new BadHttpException(
                    "This approval level has already approved the project");
        }

        Proposals proposal = existingProposal
                .orElseGet(() -> getOrCreateProposal(project, approvedBy));

        // Tái sử dụng bản ghi của cấp duyệt nếu dự án On Hold còn dữ liệu từ luồng cũ.
        // CEO đã duyệt trước đây vẫn phải xác nhận lại sau trưởng phòng Administrative.
        Approvals approval = approvalRepository
                .findByProposalIdAndApprovalLevelIgnoreCase(proposal.getId(), approvalLevel)
                .orElseGet(Approvals::new);
        approval.setProposal(proposal);
        approval.setApprovedBy(approvedBy);
        approval.setApprovalLevel(approvalLevel);
        approval.setApprovalStatus(APPROVED_STATUS);
        approval.setApprovalAt(LocalDate.now(APP_TIME_ZONE));
        approvalRepository.saveAndFlush(approval);

        // Chỉ lượt duyệt CEO sau Administrative mới kết thúc quy trình.
        if (CEO_LEVEL.equals(approvalLevel)) {
            proposal.setStatus(APPROVED_STATUS);
            project.setProjectStatus(PLANNING_STATUS);
            projectStatusService.activateIfStarted(project);
            projectRepository.save(project);
        } else {
            proposal.setStatus(PENDING_STATUS);
        }

        proposal.setUpdateAt(LocalDate.now(APP_TIME_ZONE).toString());
        proposalRepository.save(proposal);
    }

    // Lượt duyệt cũ của trưởng phòng khác không thay thế được phòng Administrative.
    private boolean isDepartmentApproved(UUID proposalId) {
        return approvalRepository
                .findByProposalIdAndApprovalLevelIgnoreCase(proposalId, DEPARTMENT_LEVEL)
                .filter(approval -> APPROVED_STATUS.equalsIgnoreCase(approval.getApprovalStatus()))
                .map(Approvals::getApprovedBy)
                .filter(this::isAdministrativeDepartment)
                .isPresent();
    }

    // Lấy proposal hiện có hoặc tạo proposal phê duyệt mới cho dự án.
    private Proposals getOrCreateProposal(
            Projects project,
            Users requestedBy) {
        Optional<Proposals> existingProposal = proposalRepository.findProjectApprovalProposal(
                project.getId(),
                createProposalCode(project));

        // Tái sử dụng proposal hiện có để tránh tạo trùng yêu cầu phê duyệt.
        if (existingProposal.isPresent()) {
            return existingProposal.get();
        }

        String today = LocalDate.now(APP_TIME_ZONE).toString();
        Proposals proposal = new Proposals();
        proposal.setProposalCode(createProposalCode(project));
        proposal.setTitle(
                "Approve project " + project.getProjectCode());
        proposal.setDescription(
                "Approval request for project "
                        + project.getProjectName());
        proposal.setStatus(PENDING_STATUS);
        proposal.setCreateAt(today);
        proposal.setUpdateAt(today);
        proposal.setUser(requestedBy);
        proposal.setDepartment(requestedBy.getDepartment());
        proposal.setProject(project);
        return proposalRepository.save(proposal);
    }

    // Tạo mã proposal phê duyệt duy nhất từ dự án.
    private String createProposalCode(Projects project) {
        return "PROJECT_APPROVAL_" + project.getId();
    }

    // Xác định cấp phê duyệt cao nhất phù hợp với vai trò người dùng.
    private String findApprovalLevel(Users user) {
        if (hasRole(user, CEO_LEVEL)) {
            return CEO_LEVEL;
        }

        if (hasRole(user, "HeadOfDepartment") && isAdministrativeDepartment(user)) {
            return DEPARTMENT_LEVEL;
        }

        return null;
    }

    private boolean isAdministrativeDepartment(Users user) {
        return user != null
                && user.getDepartment() != null
                && ADMINISTRATIVE_DEPARTMENT.equalsIgnoreCase(user.getDepartment().getDepartmentName());
    }

    // Kiểm tra người dùng có vai trò được yêu cầu hay không.
    private boolean hasRole(Users user, String acceptedValue) {
        // Người dùng null không thể có vai trò phê duyệt.
        if (user == null) {
            return false;
        }

        List<UserRole> userRoles = user.getUserRoles();

        // Người dùng chưa có danh sách vai trò không thể phê duyệt.
        if (userRoles == null) {
            return false;
        }

        for (UserRole userRole : userRoles) {
            if (userRole == null || userRole.getRole() == null) {
                continue;
            }

            Role role = userRole.getRole();

            if (acceptedValue.equalsIgnoreCase(role.getRoleCode())
                    || acceptedValue.equalsIgnoreCase(role.getRoleName())) {
                return true;
            }
        }

        return false;
    }
}

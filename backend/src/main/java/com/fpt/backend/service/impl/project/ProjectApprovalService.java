package com.fpt.backend.service.impl.project;

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

    // Kiểm tra người dùng có thuộc cấp CEO hoặc trưởng bộ phận hay không.
    public boolean canReviewProjects(Users user) {
        return findApprovalLevel(user) != null;
    }

    // Kiểm tra người dùng còn có thể phê duyệt dự án ở cấp của họ hay không.
    public boolean canApproveProject(Projects project, Users user) {
        String approvalLevel = findApprovalLevel(user);

        // Chỉ người có cấp duyệt hợp lệ mới được duyệt dự án đang On Hold.
        if (approvalLevel == null || !ON_HOLD_STATUS.equalsIgnoreCase(project.getProjectStatus())) {
            return false;
        }

        Optional<Proposals> proposal = proposalRepository.findProjectApprovalProposal(
                project.getId(),
                createProposalCode(project));

        // Cho phép duyệt lần đầu khi dự án chưa có proposal.
        if (proposal.isEmpty()) {
            return true;
        }

        return !isLevelApproved(proposal.get().getId() , approvalLevel);
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

        // Từ chối người dùng không thuộc cấp CEO hoặc trưởng bộ phận.
        if (approvalLevel == null) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only CEO or HeadOfDepartment can approve projects");
        }

        Proposals proposal = getOrCreateProposal(project, approvedBy);

        // Ngăn cùng một cấp duyệt phê duyệt dự án nhiều lần.
        if (isLevelApproved(proposal.getId(), approvalLevel)) {
            throw new BadHttpException(
                    "This approval level has already approved the project");
        }

        Approvals approval = new Approvals();
        approval.setProposal(proposal);
        approval.setApprovedBy(approvedBy);
        approval.setApprovalLevel(approvalLevel);
        approval.setApprovalStatus(APPROVED_STATUS);
        approval.setApprovalAt(LocalDate.now(APP_TIME_ZONE));
        approvalRepository.saveAndFlush(approval);

        // Chuyển dự án sang Planning sau khi đủ hai cấp phê duyệt.
        if (hasRequiredApprovals(proposal.getId())) {
            proposal.setStatus(APPROVED_STATUS);
            project.setProjectStatus(PLANNING_STATUS);
            projectStatusService.activateIfStarted(project);
            projectRepository.save(project);
        }

        proposal.setUpdateAt(LocalDate.now(APP_TIME_ZONE).toString());
        proposalRepository.save(proposal);
    }

    // Kiểm tra proposal đã được cả CEO và trưởng bộ phận phê duyệt hay chưa.
    private boolean hasRequiredApprovals(UUID proposalId) {
        return isLevelApproved(proposalId, "CEO")
                && isLevelApproved(
                        proposalId,
                        "HEAD_OF_DEPARTMENT");
    }

    // Kiểm tra một cấp duyệt đã phê duyệt proposal hay chưa.
    private boolean isLevelApproved(
            UUID proposalId,
            String approvalLevel) {
        return approvalRepository.countApprovedLevel(
                proposalId,
                approvalLevel) > 0;
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
        if (hasRole(user, "CEO")) {
            return "CEO";
        }

        if (hasRole(user, "HeadOfDepartment")) {
            return "HEAD_OF_DEPARTMENT";
        }

        return null;
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

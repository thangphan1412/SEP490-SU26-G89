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
    private static final String CEO_LEVEL = "CEO";
    private static final String HEAD_OF_DEPARTMENT_LEVEL =
            "HEAD_OF_DEPARTMENT";
    private static final String ON_HOLD_STATUS = "On Hold";
    private static final String PLANNING_STATUS = "Planning";
    private static final String PENDING_STATUS = "PENDING";
    private static final String APPROVED_STATUS = "APPROVED";
    private static final ZoneId APP_TIME_ZONE =
            ZoneId.of("Asia/Ho_Chi_Minh");

    private final ProjectProposalRepository proposalRepository;
    private final ProjectApprovalRepository approvalRepository;
    private final ProjectRepository projectRepository;
    private final ProjectStatusService projectStatusService;

    public void createApprovalRequest(
            Projects project,
            Users requestedBy) {
        getOrCreateProposal(project, requestedBy);
    }

    public boolean canReviewProjects(Users user) {
        return findApprovalLevel(user) != null;
    }

    public boolean canApproveProject(
            Projects project,
            Users user) {
        String approvalLevel = findApprovalLevel(user);

        if (approvalLevel == null
                || !ON_HOLD_STATUS.equalsIgnoreCase(
                        project.getProjectStatus())) {
            return false;
        }

        Optional<Proposals> proposal = findProposal(project.getId());

        if (proposal.isEmpty()) {
            return true;
        }

        boolean alreadyApproved = isLevelApproved(
                proposal.get().getId(), approvalLevel
        );
        // Older records may contain CEO approval but remain On Hold because
        // the previous rule also waited for Head of Department approval.
        return !alreadyApproved || CEO_LEVEL.equals(approvalLevel);
    }

    public void approveProject(
            Projects project,
            Users approvedBy) {
        if (!ON_HOLD_STATUS.equalsIgnoreCase(
                project.getProjectStatus())) {
            throw new BadHttpException(
                    "Only On Hold projects can be approved"
            );
        }

        String approvalLevel = findApprovalLevel(approvedBy);

        if (approvalLevel == null) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only CEO or HeadOfDepartment can approve projects"
            );
        }

        Proposals proposal = getOrCreateProposal(project, approvedBy);

        if (isLevelApproved(proposal.getId(), approvalLevel)) {
            if (CEO_LEVEL.equals(approvalLevel)) {
                finalizeApprovedProject(project, proposal);
                return;
            }
            throw new BadHttpException(
                    "This approval level has already approved the project"
            );
        }

        Approvals approval = new Approvals();
        approval.setProposal(proposal);
        approval.setApprovedBy(approvedBy);
        approval.setApprovalLevel(approvalLevel);
        approval.setApprovalStatus(APPROVED_STATUS);
        approval.setApprovalAt(LocalDate.now(APP_TIME_ZONE));
        approvalRepository.saveAndFlush(approval);

        if (hasRequiredApprovals(proposal.getId())) {
            finalizeApprovedProject(project, proposal);
        }

        proposal.setUpdateAt(LocalDate.now(APP_TIME_ZONE).toString());
        proposalRepository.save(proposal);
    }

    private void finalizeApprovedProject(
            Projects project,
            Proposals proposal
    ) {
        proposal.setStatus(APPROVED_STATUS);
        proposal.setUpdateAt(LocalDate.now(APP_TIME_ZONE).toString());
        project.setProjectStatus(PLANNING_STATUS);
        projectStatusService.activateIfStarted(project);
        projectRepository.save(project);
        proposalRepository.save(proposal);
    }

    private boolean hasRequiredApprovals(UUID proposalId) {
        // The Head of Department may review first, but CEO approval is the
        // final decision that releases the project from On Hold.
        return isLevelApproved(proposalId, CEO_LEVEL);
    }

    private boolean isLevelApproved(
            UUID proposalId,
            String approvalLevel) {
        return approvalRepository.countApprovedLevel(
                proposalId,
                approvalLevel
        ) > 0;
    }

    private Proposals getOrCreateProposal(
            Projects project,
            Users requestedBy) {
        Optional<Proposals> existingProposal =
                findProposal(project.getId());

        if (existingProposal.isPresent()) {
            return existingProposal.get();
        }

        String today = LocalDate.now(APP_TIME_ZONE).toString();
        Proposals proposal = new Proposals();
        proposal.setProposalCode(createProposalCode(project));
        proposal.setTitle(
                "Approve project " + project.getProjectCode()
        );
        proposal.setDescription(
                "Approval request for project "
                        + project.getProjectName()
        );
        proposal.setStatus(PENDING_STATUS);
        proposal.setCreateAt(today);
        proposal.setUpdateAt(today);
        proposal.setUser(requestedBy);
        proposal.setDepartment(requestedBy.getDepartment());
        proposal.setProject(project);
        return proposalRepository.save(proposal);
    }

    private Optional<Proposals> findProposal(UUID projectId) {
        return proposalRepository.findProjectApprovalProposal(
                projectId,
                createProposalCode(projectId)
        );
    }

    private String createProposalCode(Projects project) {
        return createProposalCode(project.getId());
    }

    private String createProposalCode(UUID projectId) {
        return "PROJECT_APPROVAL_" + projectId;
    }

    private String findApprovalLevel(Users user) {
        if (hasRole(user, "CEO")) {
            return CEO_LEVEL;
        }

        if (hasRole(
                user,
                "HeadOfDepartment",
                "HEAD_OF_DEPARTMENT",
                "HEADOFDEPARTMENT",
                "HOD"
        )) {
            return HEAD_OF_DEPARTMENT_LEVEL;
        }

        return null;
    }

    private boolean hasRole(Users user, String... acceptedValues) {
        if (user == null) {
            return false;
        }

        List<UserRole> userRoles = user.getUserRoles();

        if (userRoles == null) {
            return false;
        }

        for (UserRole userRole : userRoles) {
            if (userRole == null || userRole.getRole() == null) {
                continue;
            }

            Role role = userRole.getRole();

            for (String acceptedValue : acceptedValues) {
                if (matchesRoleValue(role.getRoleCode(), acceptedValue)
                        || matchesRoleValue(role.getRoleName(), acceptedValue)) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean matchesRoleValue(String actualValue, String acceptedValue) {
        if (actualValue == null || acceptedValue == null) {
            return false;
        }
        String actual = normalizeRoleValue(actualValue);
        String accepted = normalizeRoleValue(acceptedValue);
        return actual.equals(accepted)
                || actual.equals("ROLE" + accepted);
    }

    private String normalizeRoleValue(String value) {
        return value.toUpperCase(java.util.Locale.ROOT)
                .replaceAll("[^A-Z0-9]", "");
    }
}

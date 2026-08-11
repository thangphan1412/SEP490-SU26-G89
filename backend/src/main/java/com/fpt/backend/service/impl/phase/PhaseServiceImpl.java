package com.fpt.backend.service.impl.phase;

import com.fpt.backend.dto.response.phase.PhaseContractResponse;
import com.fpt.backend.dto.response.phase.PhaseDeliverableResponse;
import com.fpt.backend.dto.response.phase.PhaseDetailResponse;
import com.fpt.backend.dto.response.phase.PhaseListItemResponse;
import com.fpt.backend.dto.response.phase.PhaseTaskResponse;
import com.fpt.backend.dto.response.project.ProjectAccessResponse;
import com.fpt.backend.entity.Contracts;
import com.fpt.backend.entity.Deliverable;
import com.fpt.backend.entity.Projects;
import com.fpt.backend.entity.Timeline;
import com.fpt.backend.entity.TimelineContract;
import com.fpt.backend.entity.TimelineTask;
import com.fpt.backend.entity.Users;
import com.fpt.backend.enums.PhaseStatus;
import com.fpt.backend.exception.BadHttpException;
import com.fpt.backend.exception.NotFoundException;
import com.fpt.backend.repository.phase.PhaseContractRepository;
import com.fpt.backend.repository.phase.PhaseDeliverableRepository;
import com.fpt.backend.repository.phase.PhaseRepository;
import com.fpt.backend.repository.phase.PhaseTaskRepository;
import com.fpt.backend.repository.project.ProjectRepository;
import com.fpt.backend.service.interfaces.phase.IPhaseService;
import com.fpt.backend.service.interfaces.permission.IPermissionAccessService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class PhaseServiceImpl implements IPhaseService {
    private static final ZoneId APP_TIME_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

    private final PhaseRepository phaseRepository;
    private final PhaseTaskRepository phaseTaskRepository;
    private final PhaseDeliverableRepository phaseDeliverableRepository;
    private final PhaseContractRepository phaseContractRepository;
    private final ProjectRepository projectRepository;
    private final PhaseStatusService phaseStatusService;
    private final IPermissionAccessService permissionAccessService;

    @Override
    public List<PhaseListItemResponse> getPhasesByProjectId(UUID projectId) {
        if (projectId == null || !projectRepository.existsById(projectId)) {
            throw new NotFoundException("Project not found");
        }

        permissionAccessService.requireProjectAccess(projectId);
        phaseStatusService.refreshProjectStatuses(projectId);

        List<Timeline> phases = phaseRepository.findByProjectId(projectId);
        List<PhaseListItemResponse> responses = new ArrayList<>();

        for (Timeline phase : phases) {
            responses.add(toListItem(phase));
        }

        return responses;
    }

    @Override
    public PhaseDetailResponse getPhaseById(UUID phaseId) {
        Timeline phase = findPhase(phaseId);
        Projects project = phase.getProject();
        ProjectAccessResponse access = permissionAccessService
                .getCurrentUserAccess(project.getId());

        phaseStatusService.refreshProjectStatuses(project.getId());
        phase = findPhase(phaseId);

        if (phase.getStatus() != PhaseStatus.IN_PROGRESS) {
            throw new BadHttpException(
                    "Only an IN_PROGRESS phase can be accessed"
            );
        }

        project = phase.getProject();
        List<PhaseTaskResponse> tasks = getVisibleTasks(phaseId, access);
        List<PhaseDeliverableResponse> deliverables =
                getVisibleDeliverables(phaseId, access);
        List<PhaseContractResponse> contracts =
                getVisibleContracts(phaseId, access);

        return new PhaseDetailResponse(
                phase.getId(),
                phase.getTitle(),
                phase.getDescription(),
                toLocalDate(phase.getStartDate()),
                toLocalDate(phase.getEndDate()),
                phase.getStatus(),
                phase.getProgress(),
                project.getId(),
                project.getProjectCode(),
                project.getProjectName(),
                tasks,
                deliverables,
                contracts,
                access
        );
    }

    private List<PhaseTaskResponse> getVisibleTasks(
            UUID phaseId,
            ProjectAccessResponse access) {
        List<PhaseTaskResponse> responses = new ArrayList<>();

        boolean canViewTasks = permissionAccessService.hasAction(
                access,
                "VIEW_TASKS"
        );
        boolean canEditTasks = permissionAccessService.hasAction(
                access,
                "EDIT_TASKS"
        );

        if (!canViewTasks && !canEditTasks) {
            return responses;
        }

        List<TimelineTask> tasks;

        if (permissionAccessService.hasFullWorkScope(
                access,
                "VIEW_TASKS"
        )) {
            tasks = phaseTaskRepository.findByPhaseId(phaseId);
        } else {
            tasks = phaseTaskRepository.findByPhaseIdAndAssignedUserId(
                    phaseId,
                    access.currentUserId()
            );
        }

        for (TimelineTask task : tasks) {
            responses.add(toTaskResponse(task));
        }

        return responses;
    }

    private List<PhaseDeliverableResponse> getVisibleDeliverables(
            UUID phaseId,
            ProjectAccessResponse access) {
        List<PhaseDeliverableResponse> responses = new ArrayList<>();

        if (!permissionAccessService.hasAction(
                access,
                "VIEW_DELIVERABLES"
        )) {
            return responses;
        }

        for (Deliverable deliverable
                : phaseDeliverableRepository.findByPhaseId(phaseId)) {
            responses.add(new PhaseDeliverableResponse(
                    deliverable.getId(),
                    deliverable.getTitle(),
                    deliverable.getDescription(),
                    toLocalDate(deliverable.getDueDate()),
                    deliverable.getStatus()
            ));
        }

        return responses;
    }

    private List<PhaseContractResponse> getVisibleContracts(
            UUID phaseId,
            ProjectAccessResponse access) {
        List<PhaseContractResponse> responses = new ArrayList<>();

        if (!permissionAccessService.hasAction(access, "VIEW_CONTRACTS")) {
            return responses;
        }

        for (TimelineContract phaseContract
                : phaseContractRepository.findByPhaseId(phaseId)) {
            Contracts contract = phaseContract.getContract();
            responses.add(new PhaseContractResponse(
                    contract.getId(),
                    contract.getContractNumber(),
                    contract.getContractTitle(),
                    contract.getContractStatus(),
                    contract.getEffectiveDate(),
                    contract.getExpirationDate(),
                    phaseContract.getLinkedAt()
            ));
        }

        return responses;
    }

    private PhaseTaskResponse toTaskResponse(TimelineTask task) {
        Users assignedUser = task.getAssignedTo();
        UUID assignedUserId = null;
        String assignedUserName = null;
        String assignedUserEmail = null;

        if (assignedUser != null) {
            assignedUserId = assignedUser.getId();
            assignedUserName = getUserName(assignedUser);
            assignedUserEmail = assignedUser.getEmail();
        }

        return new PhaseTaskResponse(
                task.getId(),
                task.getTitle(),
                task.getStatus(),
                toLocalDate(task.getStartDate()),
                toLocalDate(task.getEndDate()),
                assignedUserId,
                assignedUserName,
                assignedUserEmail
        );
    }

    private PhaseListItemResponse toListItem(Timeline phase) {
        Projects project = phase.getProject();

        return new PhaseListItemResponse(
                phase.getId(),
                phase.getTitle(),
                phase.getDescription(),
                toLocalDate(phase.getStartDate()),
                toLocalDate(phase.getEndDate()),
                phase.getStatus(),
                phase.getProgress(),
                project.getId(),
                project.getProjectCode(),
                project.getProjectName()
        );
    }

    private LocalDate toLocalDate(Date value) {
        if (value == null) {
            return null;
        }

        if (value instanceof java.sql.Date sqlDate) {
            return sqlDate.toLocalDate();
        }

        return value.toInstant().atZone(APP_TIME_ZONE).toLocalDate();
    }

    private String getUserName(Users user) {
        String fullName = (normalize(user.getFirstName()) + " " + normalize(user.getLastName())).trim();
        return fullName.isBlank() ? user.getEmail() : fullName;
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }

    private Timeline findPhase(UUID phaseId) {
        Optional<Timeline> optionalPhase = phaseRepository.findDetailById(
                phaseId
        );

        if (optionalPhase.isEmpty()) {
            throw new NotFoundException("Phase not found");
        }

        return optionalPhase.get();
    }
}

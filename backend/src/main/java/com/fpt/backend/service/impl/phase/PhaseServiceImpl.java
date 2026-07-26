package com.fpt.backend.service.impl.phase;

import com.fpt.backend.dto.response.phase.PhaseContractResponse;
import com.fpt.backend.dto.response.phase.PhaseDeliverableResponse;
import com.fpt.backend.dto.response.phase.PhaseDetailResponse;
import com.fpt.backend.dto.response.phase.PhaseListItemResponse;
import com.fpt.backend.dto.response.phase.PhaseTaskResponse;
import com.fpt.backend.entity.Contracts;
import com.fpt.backend.entity.Deliverable;
import com.fpt.backend.entity.Projects;
import com.fpt.backend.entity.Timeline;
import com.fpt.backend.entity.TimelineContract;
import com.fpt.backend.entity.TimelineTask;
import com.fpt.backend.entity.Users;
import com.fpt.backend.exception.NotFoundException;
import com.fpt.backend.repository.phase.PhaseContractRepository;
import com.fpt.backend.repository.phase.PhaseDeliverableRepository;
import com.fpt.backend.repository.phase.PhaseRepository;
import com.fpt.backend.repository.phase.PhaseTaskRepository;
import com.fpt.backend.repository.project.ProjectRepository;
import com.fpt.backend.service.interfaces.phase.PhaseProgressService;
import com.fpt.backend.service.interfaces.phase.PhaseService;
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
@Transactional(readOnly = true)
public class PhaseServiceImpl implements PhaseService {
    private static final ZoneId APP_TIME_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

    private final PhaseRepository phaseRepository;
    private final PhaseTaskRepository phaseTaskRepository;
    private final PhaseDeliverableRepository phaseDeliverableRepository;
    private final PhaseContractRepository phaseContractRepository;
    private final ProjectRepository projectRepository;
    private final PhaseProgressService phaseProgressService;

    @Override
    public List<PhaseListItemResponse> getPhasesByProjectId(UUID projectId) {
        if (projectId == null || !projectRepository.existsById(projectId)) {
            throw new NotFoundException("Project not found");
        }

        List<Timeline> phases = phaseRepository.findByProjectId(projectId);
        List<PhaseListItemResponse> responses = new ArrayList<>();

        for (Timeline phase : phases) {
            responses.add(toListItem(phase));
        }

        return responses;
    }

    @Override
    public PhaseDetailResponse getPhaseById(UUID phaseId) {
        Optional<Timeline> optionalPhase = phaseRepository.findDetailById(phaseId);

        if (optionalPhase.isEmpty()) {
            throw new NotFoundException("Phase not found");
        }

        Timeline phase = optionalPhase.get();
        Projects project = phase.getProject();

        List<TimelineTask> phaseTasks = phaseTaskRepository.findByPhaseId(phaseId);
        List<PhaseTaskResponse> tasks = new ArrayList<>();

        for (TimelineTask task : phaseTasks) {
            Users assignedUser = task.getAssignedTo();
            UUID assignedUserId = null;
            String assignedUserName = null;
            String assignedUserEmail = null;

            if (assignedUser != null) {
                assignedUserId = assignedUser.getId();
                assignedUserName = getUserName(assignedUser);
                assignedUserEmail = assignedUser.getEmail();
            }

            tasks.add(new PhaseTaskResponse(
                    task.getId(),
                    task.getTitle(),
                    task.getStatus(),
                    toLocalDate(task.getStartDate()),
                    toLocalDate(task.getEndDate()),
                    task.getProgress(),
                    assignedUserId,
                    assignedUserName,
                    assignedUserEmail
            ));
        }

        List<Deliverable> phaseDeliverables = phaseDeliverableRepository.findByPhaseId(phaseId);
        List<PhaseDeliverableResponse> deliverables = new ArrayList<>();

        for (Deliverable deliverable : phaseDeliverables) {
            deliverables.add(new PhaseDeliverableResponse(
                    deliverable.getId(),
                    deliverable.getTitle(),
                    deliverable.getDescription(),
                    toLocalDate(deliverable.getDueDate()),
                    deliverable.getStatus()
            ));
        }

        List<TimelineContract> phaseContracts = phaseContractRepository.findByPhaseId(phaseId);
        List<PhaseContractResponse> contracts = new ArrayList<>();

        for (TimelineContract phaseContract : phaseContracts) {
            Contracts contract = phaseContract.getContract();
            contracts.add(new PhaseContractResponse(
                    contract.getId(),
                    contract.getContractNumber(),
                    contract.getContractTitle(),
                    contract.getContractStatus(),
                    contract.getEffectiveDate(),
                    contract.getExpirationDate(),
                    phaseContract.getLinkedAt()
            ));
        }

        return new PhaseDetailResponse(
                phase.getId(),
                phase.getTitle(),
                phase.getDescription(),
                toLocalDate(phase.getStartDate()),
                toLocalDate(phase.getEndDate()),
                phase.getStatus(),
                phaseProgressService.calculateProgress(phase.getId()),
                project.getId(),
                project.getProjectCode(),
                project.getProjectName(),
                tasks,
                deliverables,
                contracts
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
                phaseProgressService.calculateProgress(phase.getId()),
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
}

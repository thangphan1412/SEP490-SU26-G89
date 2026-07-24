package com.fpt.backend.service.impl.phase;

import com.fpt.backend.dto.response.phase.PhaseContractResponse;
import com.fpt.backend.dto.response.phase.PhaseDeliverableResponse;
import com.fpt.backend.dto.response.phase.PhaseDetailResponse;
import com.fpt.backend.dto.response.phase.PhaseListItemResponse;
import com.fpt.backend.dto.response.phase.PhaseTaskResponse;
import com.fpt.backend.entity.Contracts;
import com.fpt.backend.entity.Projects;
import com.fpt.backend.entity.Timeline;
import com.fpt.backend.entity.Users;
import com.fpt.backend.repository.phase.PhaseContractRepository;
import com.fpt.backend.repository.phase.PhaseDeliverableRepository;
import com.fpt.backend.repository.phase.PhaseRepository;
import com.fpt.backend.repository.phase.PhaseTaskRepository;
import com.fpt.backend.repository.project.ProjectRepository;
import com.fpt.backend.service.interfaces.phase.PhaseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
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

    @Override
    public List<PhaseListItemResponse> getPhasesByProjectId(UUID projectId) {
        if (projectId == null || !projectRepository.existsById(projectId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found");
        }

        return phaseRepository.findByProjectId(projectId)
                .stream()
                .map(this::toListItem)
                .toList();
    }

    @Override
    public PhaseDetailResponse getPhaseById(UUID phaseId) {
        Timeline phase = phaseRepository.findDetailById(phaseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Phase not found"));
        Projects project = phase.getProject();

        List<PhaseTaskResponse> tasks = phaseTaskRepository.findByPhaseId(phaseId)
                .stream()
                .map(task -> {
                    Users assignedUser = task.getAssignedTo();
                    return new PhaseTaskResponse(
                            task.getId(),
                            task.getTitle(),
                            task.getStatus(),
                            toLocalDate(task.getStartDate()),
                            toLocalDate(task.getEndDate()),
                            task.getProgress(),
                            assignedUser == null ? null : assignedUser.getId(),
                            assignedUser == null ? null : getUserName(assignedUser),
                            assignedUser == null ? null : assignedUser.getEmail()
                    );
                })
                .toList();

        List<PhaseDeliverableResponse> deliverables = phaseDeliverableRepository.findByPhaseId(phaseId)
                .stream()
                .map(deliverable -> new PhaseDeliverableResponse(
                        deliverable.getId(),
                        deliverable.getTitle(),
                        deliverable.getDescription(),
                        toLocalDate(deliverable.getDueDate()),
                        deliverable.getStatus()
                ))
                .toList();

        List<PhaseContractResponse> contracts = phaseContractRepository.findByPhaseId(phaseId)
                .stream()
                .map(phaseContract -> {
                    Contracts contract = phaseContract.getContract();
                    return new PhaseContractResponse(
                            contract.getId(),
                            contract.getContractNumber(),
                            contract.getContractTitle(),
                            contract.getContractStatus(),
                            contract.getEffectiveDate(),
                            contract.getExpirationDate(),
                            phaseContract.getLinkedAt()
                    );
                })
                .toList();

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
}

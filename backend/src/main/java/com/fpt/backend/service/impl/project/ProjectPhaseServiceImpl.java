package com.fpt.backend.service.impl.project;

import com.fpt.backend.dto.request.project.ProjectPhaseRequest;
import com.fpt.backend.dto.response.project.ProjectPhaseResponse;
import com.fpt.backend.entity.Projects;
import com.fpt.backend.entity.Timeline;
import com.fpt.backend.enums.PhaseStatus;
import com.fpt.backend.exception.BadHttpException;
import com.fpt.backend.repository.phase.PhaseContractRepository;
import com.fpt.backend.repository.phase.PhaseDeliverableRepository;
import com.fpt.backend.repository.phase.PhaseRepository;
import com.fpt.backend.repository.phase.PhaseTaskRepository;
import com.fpt.backend.service.interfaces.phase.PhaseProgressService;
import com.fpt.backend.service.interfaces.project.ProjectPhaseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProjectPhaseServiceImpl implements ProjectPhaseService {
    private static final PhaseStatus DEFAULT_PHASE_STATUS = PhaseStatus.PLANNING;
    private static final ZoneId PROJECT_TIME_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

    private final PhaseRepository phaseRepository;
    private final PhaseTaskRepository phaseTaskRepository;
    private final PhaseDeliverableRepository phaseDeliverableRepository;
    private final PhaseContractRepository phaseContractRepository;
    private final PhaseProgressService phaseProgressService;

    @Override
    public void syncPhases(
            Projects project,
            List<ProjectPhaseRequest> phaseRequests) {
        List<ProjectPhaseRequest> requests = phaseRequests == null
                ? List.of()
                : phaseRequests;
        validatePhaseSchedule(project, requests);

        Map<UUID, Timeline> existingPhases = new LinkedHashMap<>();

        for (Timeline phase : phaseRepository.findByProjectId(project.getId())) {
            existingPhases.put(phase.getId(), phase);
        }

        LocalDate nextStartDate = project.getProjectStartDate();

        for (ProjectPhaseRequest request : requests) {
            if (request == null) {
                throw new BadHttpException("Phase information is required");
            }

            Timeline phase;

            if (request.id() == null) {
                phase = new Timeline();
            } else {
                phase = existingPhases.remove(request.id());

                if (phase == null) {
                    throw new BadHttpException(
                            "Phase does not belong to this project"
                    );
                }
            }

            applyPhaseInformation(
                    phase,
                    request,
                    project,
                    nextStartDate
            );
            phaseRepository.save(phase);
            nextStartDate = request.endDate().plusDays(1);
        }

        for (Timeline removedPhase : existingPhases.values()) {
            removePhase(removedPhase);
        }
    }

    @Override
    public List<ProjectPhaseResponse> getProjectPhases(UUID projectId) {
        List<Timeline> phases = phaseRepository.findByProjectId(projectId);
        List<ProjectPhaseResponse> responses = new ArrayList<>();

        for (Timeline phase : phases) {
            responses.add(new ProjectPhaseResponse(
                    phase.getId(),
                    phase.getTitle(),
                    phase.getDescription(),
                    toLocalDate(phase.getStartDate()),
                    toLocalDate(phase.getEndDate()),
                    phase.getStatus(),
                    phaseProgressService.calculateProgress(phase.getId())
            ));
        }

        return responses;
    }

    @Override
    public void deleteProjectData(UUID projectId) {
        phaseContractRepository.deleteByProjectId(projectId);
        phaseTaskRepository.deleteByProjectId(projectId);
        phaseDeliverableRepository.deleteByProjectId(projectId);
        phaseRepository.deleteByProjectId(projectId);
    }

    private void validatePhaseSchedule(
            Projects project,
            List<ProjectPhaseRequest> requests) {
        if (requests.isEmpty()) {
            throw new BadHttpException(
                    "At least one phase is required to cover the full project timeline"
            );
        }

        LocalDate expectedStartDate = project.getProjectStartDate();

        for (int index = 0; index < requests.size(); index++) {
            ProjectPhaseRequest request = requests.get(index);
            int phaseNumber = index + 1;

            if (request == null) {
                throw new BadHttpException(
                        "Phase " + phaseNumber + " information is required"
                );
            }

            LocalDate endDate = request.endDate();

            if (endDate == null) {
                throw new BadHttpException(
                        "Phase " + phaseNumber
                                + " end date is required"
                );
            }

            if (expectedStartDate.isAfter(project.getProjectEndDate())) {
                throw new BadHttpException(
                        "Phase " + phaseNumber
                                + " starts after the project end date"
                );
            }

            if (endDate.isBefore(expectedStartDate)) {
                throw new BadHttpException(
                        "Phase " + phaseNumber
                                + " end date must not be before its "
                                + "calculated start date "
                                + expectedStartDate
                );
            }

            if (endDate.isAfter(project.getProjectEndDate())) {
                throw new BadHttpException(
                        "Phase " + phaseNumber
                                + " end date must not be after the "
                                + "project end date"
                );
            }

            expectedStartDate = endDate.plusDays(1);
        }

        ProjectPhaseRequest finalPhase = requests.get(requests.size() - 1);

        if (!finalPhase.endDate().equals(project.getProjectEndDate())) {
            throw new BadHttpException(
                    "The final phase must end on the project end date "
                            + project.getProjectEndDate()
            );
        }
    }

    private void applyPhaseInformation(
            Timeline phase,
            ProjectPhaseRequest request,
            Projects project,
            LocalDate startDate) {
        String title = requireText(
                request.title(),
                "Phase title is required",
                150
        );
        String description = normalize(request.description());
        PhaseStatus status = request.status() == null
                ? DEFAULT_PHASE_STATUS
                : request.status();
        LocalDate endDate = request.endDate();

        validateMaxLength(description, "Phase description", 500);

        phase.setTitle(title);
        phase.setDescription(description);
        phase.setStartDate(java.sql.Date.valueOf(startDate));
        phase.setEndDate(java.sql.Date.valueOf(endDate));
        phase.setStatus(status);

        if (phase.getId() == null) {
            phase.setProgress(0D);
        }

        phase.setProject(project);
    }

    private void removePhase(Timeline phase) {
        long taskCount = phaseTaskRepository.countByPhaseId(phase.getId());
        long deliverableCount =
                phaseDeliverableRepository.countByPhaseId(phase.getId());

        if (taskCount > 0 || deliverableCount > 0) {
            throw new BadHttpException(
                    "Phase cannot be removed because it has tasks or deliverables"
            );
        }

        phaseRepository.delete(phase);
    }

    private LocalDate toLocalDate(Date value) {
        if (value == null) {
            return null;
        }

        if (value instanceof java.sql.Date sqlDate) {
            return sqlDate.toLocalDate();
        }

        return value.toInstant()
                .atZone(PROJECT_TIME_ZONE)
                .toLocalDate();
    }

    private String requireText(String value, String message, int maxLength) {
        String normalizedValue = normalize(value);

        if (normalizedValue.isBlank()) {
            throw new BadHttpException(message);
        }

        validateMaxLength(
                normalizedValue,
                message.replace(" is required", ""),
                maxLength
        );
        return normalizedValue;
    }

    private void validateMaxLength(
            String value,
            String fieldName,
            int maxLength) {
        if (value.length() > maxLength) {
            throw new BadHttpException(
                    fieldName + " must not be longer than "
                            + maxLength + " characters"
            );
        }
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }
}

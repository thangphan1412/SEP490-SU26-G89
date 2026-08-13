package com.fpt.backend.service.impl.phase;

import com.fpt.backend.entity.Timeline;
import com.fpt.backend.enums.PhaseStatus;
import com.fpt.backend.repository.phase.PhaseRepository;
import com.fpt.backend.service.impl.project.ProjectStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class PhaseStatusService {
    private static final ZoneId APP_TIME_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

    private final PhaseRepository phaseRepository;
    private final PhaseProgressService phaseProgressService;
    private final ProjectStatusService projectStatusService;

    public void refreshAllProjectStatuses() {
        for (UUID projectId : phaseRepository.findProjectIds()) {
            refreshProjectStatuses(projectId);
        }
    }

    public void refreshProjectStatuses(UUID projectId) {
        List<Timeline> phases = phaseRepository.findByProjectId(projectId);
        LocalDate today = LocalDate.now(APP_TIME_ZONE);
        PhaseStatus previousStatus = null;

        for (int index = 0; index < phases.size(); index++) {
            Timeline phase = phases.get(index);
            double progress = phaseProgressService.calculateProgress(
                    phase.getId()
            );
            String projectStatus = phase.getProject().getProjectStatus();
            boolean projectIsWaiting = "On Hold".equalsIgnoreCase(
                    projectStatus
            ) || "Planning".equalsIgnoreCase(projectStatus);
            PhaseStatus status = calculateStatus(
                    phase,
                    progress,
                    today,
                    index == 0,
                    previousStatus,
                    projectIsWaiting
            );

            phase.setProgress(progress);
            phase.setStatus(status);
            previousStatus = status;
        }

        phaseRepository.saveAll(phases);
        phaseRepository.flush();
        projectStatusService.completeIfAllPhasesCompleted(phases);
    }

    private PhaseStatus calculateStatus(
            Timeline phase,
            double progress,
            LocalDate today,
            boolean firstPhase,
            PhaseStatus previousStatus,
            boolean projectIsWaiting) {
        if (progress >= 100D) {
            return PhaseStatus.COMPLETED;
        }

        if (projectIsWaiting) {
            return PhaseStatus.PLANNING;
        }

        LocalDate startDate = toLocalDate(phase.getStartDate());
        LocalDate endDate = toLocalDate(phase.getEndDate());

        if (today.isAfter(endDate)) {
            return PhaseStatus.OVER_DUE;
        }

        boolean dateIsInPhase = !today.isBefore(startDate)
                && !today.isAfter(endDate);
        boolean previousPhaseIsCompleted = previousStatus
                == PhaseStatus.COMPLETED;

        if (dateIsInPhase
                && (firstPhase || previousPhaseIsCompleted)) {
            return PhaseStatus.IN_PROGRESS;
        }

        return PhaseStatus.PLANNING;
    }

    private LocalDate toLocalDate(Date value) {
        if (value instanceof java.sql.Date sqlDate) {
            return sqlDate.toLocalDate();
        }

        return value.toInstant().atZone(APP_TIME_ZONE).toLocalDate();
    }
}

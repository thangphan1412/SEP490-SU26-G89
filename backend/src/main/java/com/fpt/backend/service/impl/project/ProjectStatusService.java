package com.fpt.backend.service.impl.project;

import com.fpt.backend.entity.Projects;
import com.fpt.backend.entity.Timeline;
import com.fpt.backend.enums.PhaseStatus;
import com.fpt.backend.repository.project.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ProjectStatusService {
    private static final String PLANNING_STATUS = "Planning";
    private static final String ACTIVE_STATUS = "Active";
    private static final String COMPLETED_STATUS = "Completed";
    private static final ZoneId APP_TIME_ZONE =
            ZoneId.of("Asia/Ho_Chi_Minh");

    private final ProjectRepository projectRepository;

    public void refreshPlanningProjects() {
        List<Projects> projects = projectRepository
                .findAllByProjectStatusIgnoreCase(PLANNING_STATUS);

        for (Projects project : projects) {
            activateIfStarted(project);
        }

        projectRepository.saveAll(projects);
        projectRepository.flush();
    }

    public void activateIfStarted(Projects project) {
        if (!PLANNING_STATUS.equalsIgnoreCase(
                project.getProjectStatus())) {
            return;
        }

        LocalDate startDate = project.getProjectStartDate();
        LocalDate today = LocalDate.now(APP_TIME_ZONE);

        if (startDate != null && !today.isBefore(startDate)) {
            project.setProjectStatus(ACTIVE_STATUS);
        }
    }

    public void completeIfAllPhasesCompleted(List<Timeline> phases) {
        if (phases.isEmpty()) {
            return;
        }

        for (Timeline phase : phases) {
            if (phase.getStatus() != PhaseStatus.COMPLETED) {
                return;
            }
        }

        Projects project = phases.get(0).getProject();
        project.setProjectStatus(COMPLETED_STATUS);
        projectRepository.save(project);
        projectRepository.flush();
    }
}

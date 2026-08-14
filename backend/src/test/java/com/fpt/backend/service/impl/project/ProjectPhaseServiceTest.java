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
import com.fpt.backend.service.impl.phase.PhaseStatusService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectPhaseServiceTest {

    @Mock
    private PhaseRepository phaseRepository;

    @Mock
    private PhaseTaskRepository phaseTaskRepository;

    @Mock
    private PhaseDeliverableRepository phaseDeliverableRepository;

    @Mock
    private PhaseContractRepository phaseContractRepository;

    @Mock
    private PhaseStatusService phaseStatusService;

    @InjectMocks
    private ProjectPhaseService projectPhaseService;

    // Kiểm tra đồng bộ phase cập nhật phase cũ, tạo phase mới và làm mới trạng thái lịch trình.
    @Test
    void syncPhases_existingAndNewPhases_updatesTimelineAndRefreshesStatuses() {
        Projects project = project();
        Timeline existing = phase(project, project.getProjectStartDate(), LocalDate.of(2026, 8, 15));
        when(phaseRepository.findByProjectId(project.getId())).thenReturn(List.of(existing));
        List<ProjectPhaseRequest> requests = List.of(
                new ProjectPhaseRequest(
                        existing.getId(), " Updated planning ", " Description ",
                        LocalDate.of(2026, 8, 20)
                ),
                new ProjectPhaseRequest(
                        null, "Execution", null, project.getProjectEndDate()
                )
        );

        projectPhaseService.syncPhases(project, requests);

        ArgumentCaptor<Timeline> captor = ArgumentCaptor.forClass(Timeline.class);
        verify(phaseRepository, org.mockito.Mockito.times(2)).save(captor.capture());
        Timeline updated = captor.getAllValues().get(0);
        Timeline created = captor.getAllValues().get(1);
        assertThat(updated.getTitle()).isEqualTo("Updated planning");
        assertThat(updated.getDescription()).isEqualTo("Description");
        assertThat(((java.sql.Date) updated.getStartDate()).toLocalDate())
                .isEqualTo(project.getProjectStartDate());
        assertThat(created.getStatus()).isEqualTo(PhaseStatus.PLANNING);
        assertThat(created.getProgress()).isZero();
        assertThat(((java.sql.Date) created.getStartDate()).toLocalDate())
                .isEqualTo(LocalDate.of(2026, 8, 21));
        assertThat(created.getProject()).isSameAs(project);
        verify(phaseRepository).flush();
        verify(phaseStatusService).refreshProjectStatuses(project.getId());
    }

    // Kiểm tra không cho xóa phase đang chứa task.
    @Test
    void syncPhases_removedPhaseWithTasks_rejectsRemoval() {
        Projects project = project();
        Timeline removed = phase(
                project, project.getProjectStartDate(), project.getProjectEndDate()
        );
        when(phaseRepository.findByProjectId(project.getId())).thenReturn(List.of(removed));
        when(phaseTaskRepository.countByPhaseId(removed.getId())).thenReturn(1L);

        assertThatThrownBy(() -> projectPhaseService.syncPhases(
                project,
                List.of(new ProjectPhaseRequest(
                        null,
                        "Replacement",
                        null,
                        project.getProjectEndDate()
                ))
        )).isInstanceOf(BadHttpException.class)
                .hasMessageContaining("tasks or deliverables");
        verify(phaseRepository, never()).delete(removed);
    }

    // Kiểm tra không cho xóa phase đang chứa deliverable.
    @Test
    void syncPhases_removedPhaseWithDeliverables_rejectsRemoval() {
        Projects project = project();
        Timeline removed = phase(
                project, project.getProjectStartDate(), project.getProjectEndDate()
        );
        when(phaseRepository.findByProjectId(project.getId())).thenReturn(List.of(removed));
        when(phaseDeliverableRepository.countByPhaseId(removed.getId())).thenReturn(1L);

        assertThatThrownBy(() -> projectPhaseService.syncPhases(
                project,
                List.of(new ProjectPhaseRequest(
                        null,
                        "Replacement",
                        null,
                        project.getProjectEndDate()
                ))
        )).isInstanceOf(BadHttpException.class)
                .hasMessageContaining("tasks or deliverables");
        verify(phaseRepository, never()).delete(removed);
    }

    // Kiểm tra cho phép xóa phase khi phase không còn task hoặc deliverable phụ thuộc.
    @Test
    void syncPhases_removedPhaseWithoutDependencies_deletesPhase() {
        Projects project = project();
        Timeline removed = phase(
                project, project.getProjectStartDate(), project.getProjectEndDate()
        );
        when(phaseRepository.findByProjectId(project.getId())).thenReturn(List.of(removed));

        projectPhaseService.syncPhases(
                project,
                List.of(new ProjectPhaseRequest(
                        null,
                        "Replacement",
                        null,
                        project.getProjectEndDate()
                ))
        );

        verify(phaseRepository).delete(removed);
    }

    private static Projects project() {
        Projects project = new Projects();
        project.setId(UUID.randomUUID());
        project.setProjectStartDate(LocalDate.of(2026, 8, 10));
        project.setProjectEndDate(LocalDate.of(2026, 9, 10));
        return project;
    }

    private static Timeline phase(Projects project, LocalDate start, LocalDate end) {
        Timeline phase = new Timeline();
        phase.setId(UUID.randomUUID());
        phase.setProject(project);
        phase.setStartDate(java.sql.Date.valueOf(start));
        phase.setEndDate(java.sql.Date.valueOf(end));
        phase.setStatus(PhaseStatus.PLANNING);
        phase.setProgress(0D);
        return phase;
    }
}

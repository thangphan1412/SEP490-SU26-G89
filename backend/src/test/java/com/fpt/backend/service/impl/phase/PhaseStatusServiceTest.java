package com.fpt.backend.service.impl.phase;

import com.fpt.backend.entity.Projects;
import com.fpt.backend.entity.Timeline;
import com.fpt.backend.enums.PhaseStatus;
import com.fpt.backend.repository.phase.PhaseRepository;
import com.fpt.backend.service.impl.project.ProjectStatusService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PhaseStatusServiceTest {
    private static final UUID PROJECT_ID = new UUID(0, 100);
    private static final ZoneId PROJECT_TIME_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

    @Mock
    private PhaseRepository phaseRepository;
    @Mock
    private PhaseProgressService phaseProgressService;
    @Mock
    private ProjectStatusService projectStatusService;
    @InjectMocks
    private PhaseStatusService phaseStatusService;

    private Projects project;
    private LocalDate today;

    @BeforeEach
    void setUp() {
        today = LocalDate.now(PROJECT_TIME_ZONE);
        project = new Projects();
        project.setId(PROJECT_ID);
        project.setProjectStatus("Active");
    }

    /**
     * Kiểm tra 5 mốc ngày của phase đầu tiên, project Active, progress = 25%.
     * Input: ngày bắt đầu/kết thúc = today + offset trong từng dòng dữ liệu.
     * Expected: trước ngày bắt đầu là PLANNING; trong khoảng, kể cả hai biên,
     * là IN_PROGRESS; sau ngày kết thúc là OVER_DUE.
     */
    @ParameterizedTest(name = "start={0}, end={1}, expected={2}")
    @CsvSource({
            "1, 5, PLANNING",
            "0, 5, IN_PROGRESS",
            "-2, 2, IN_PROGRESS",
            "-2, 0, IN_PROGRESS",
            "-5, -1, OVER_DUE"
    })
    void refreshProjectStatuses_preservesDateBoundaries(
            int startOffset, int endOffset, PhaseStatus expectedStatus) {
        Timeline phase = phase(101, startOffset, endOffset);
        List<Timeline> phases = List.of(phase);
        when(phaseRepository.findByProjectId(PROJECT_ID)).thenReturn(phases);
        when(phaseProgressService.calculateProgress(phase.getId())).thenReturn(25D);

        phaseStatusService.refreshProjectStatuses(PROJECT_ID);

        assertThat(phase.getStatus()).isEqualTo(expectedStatus);
        assertThat(phase.getProgress()).isEqualTo(25D);
        verify(phaseRepository).saveAll(phases);
        verify(phaseRepository).flush();
        verify(projectStatusService).completeIfAllPhasesCompleted(phases);
    }

    /**
     * Input: project lần lượt On Hold và Planning; phase kết thúc hôm qua,
     * progress = 25%. Expected: phase vẫn PLANNING, không thành OVER_DUE.
     */
    @ParameterizedTest
    @ValueSource(strings = {"On Hold", "Planning"})
    void refreshProjectStatuses_keepsWaitingProjectPhasesPlanning(String status) {
        project.setProjectStatus(status);
        Timeline phase = phase(101, -5, -1);
        when(phaseRepository.findByProjectId(PROJECT_ID)).thenReturn(List.of(phase));
        when(phaseProgressService.calculateProgress(phase.getId())).thenReturn(25D);

        phaseStatusService.refreshProjectStatuses(PROJECT_ID);

        assertThat(phase.getStatus()).isEqualTo(PhaseStatus.PLANNING);
    }

    /**
     * Input: project On Hold, phase kết thúc hôm qua, progress = 100%.
     * Expected: COMPLETED được ưu tiên trước kiểm tra trạng thái project và ngày.
     */
    @Test
    void refreshProjectStatuses_prioritizesCompletedProgress() {
        project.setProjectStatus("On Hold");
        Timeline phase = phase(101, -5, -1);
        when(phaseRepository.findByProjectId(PROJECT_ID)).thenReturn(List.of(phase));
        when(phaseProgressService.calculateProgress(phase.getId())).thenReturn(100D);

        phaseStatusService.refreshProjectStatuses(PROJECT_ID);

        assertThat(phase.getStatus()).isEqualTo(PhaseStatus.COMPLETED);
    }

    /**
     * Input: hai phase đều đang trong khoảng ngày hợp lệ; phase sau đạt 25%.
     * Expected: phase sau PLANNING khi phase trước đạt 25%, và IN_PROGRESS
     * khi phase trước đạt 100% (COMPLETED).
     */
    @ParameterizedTest(name = "previous progress={0}, next status={1}")
    @CsvSource({"25, PLANNING", "100, IN_PROGRESS"})
    void refreshProjectStatuses_preservesSequentialActivation(
            double previousProgress, PhaseStatus expectedStatus) {
        Timeline previousPhase = phase(101, -2, 5);
        Timeline nextPhase = phase(102, -1, 6);
        when(phaseRepository.findByProjectId(PROJECT_ID))
                .thenReturn(List.of(previousPhase, nextPhase));
        when(phaseProgressService.calculateProgress(previousPhase.getId()))
                .thenReturn(previousProgress);
        when(phaseProgressService.calculateProgress(nextPhase.getId())).thenReturn(25D);

        phaseStatusService.refreshProjectStatuses(PROJECT_ID);

        assertThat(nextPhase.getStatus()).isEqualTo(expectedStatus);
    }

    private Timeline phase(long id, int startOffset, int endOffset) {
        Timeline phase = new Timeline();
        phase.setId(new UUID(0, id));
        phase.setProject(project);
        phase.setStartDate(java.sql.Date.valueOf(today.plusDays(startOffset)));
        phase.setEndDate(java.sql.Date.valueOf(today.plusDays(endOffset)));
        return phase;
    }
}

package com.fpt.backend.service.impl.project;

import com.fpt.backend.entity.Projects;
import com.fpt.backend.repository.project.ProjectRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class ProjectStatusServiceTest {

    private static final ZoneId PROJECT_TIME_ZONE =
            ZoneId.of("Asia/Ho_Chi_Minh");

    @Mock
    private ProjectRepository projectRepository;

    @InjectMocks
    private ProjectStatusService projectStatusService;

    /**
     * Project Planning có ngày bắt đầu là hôm qua phải được kích hoạt.
     * Input: status = Planning, startDate = today - 1 day.
     * Expected: status = Active.
     */
    @Test
    void activateIfStarted_withYesterdayStartDateChangesPlanningToActive() {
        Projects project = planningProject(LocalDate.now(PROJECT_TIME_ZONE).minusDays(1));

        projectStatusService.activateIfStarted(project);

        assertThat(project.getProjectStatus()).isEqualTo("Active");
    }

    /**
     * Project Planning bắt đầu đúng hôm nay cũng phải được kích hoạt.
     * Input: status = Planning, startDate = today.
     * Expected: status = Active.
     */
    @Test
    void activateIfStarted_withTodayStartDateChangesPlanningToActive() {
        Projects project = planningProject(LocalDate.now(PROJECT_TIME_ZONE));

        projectStatusService.activateIfStarted(project);

        assertThat(project.getProjectStatus()).isEqualTo("Active");
    }

    /**
     * Project Planning có ngày bắt đầu trong tương lai phải tiếp tục chờ.
     * Input: status = Planning, startDate = today + 1 day.
     * Expected: status vẫn là Planning.
     */
    @Test
    void activateIfStarted_withTomorrowStartDateKeepsPlanning() {
        Projects project = planningProject(LocalDate.now(PROJECT_TIME_ZONE).plusDays(1));

        projectStatusService.activateIfStarted(project);

        assertThat(project.getProjectStatus()).isEqualTo("Planning");
    }

    /**
     * Project chưa được approve đang On Hold không được tự động kích hoạt.
     * Input: status = On Hold, startDate = today - 1 day.
     * Expected: status vẫn là On Hold.
     */
    @Test
    void activateIfStarted_withOnHoldProjectDoesNotChangeStatus() {
        Projects project = planningProject(LocalDate.now(PROJECT_TIME_ZONE).minusDays(1));
        project.setProjectStatus("On Hold");

        projectStatusService.activateIfStarted(project);

        assertThat(project.getProjectStatus()).isEqualTo("On Hold");
    }

    /**
     * Project Planning chưa có ngày bắt đầu không được kích hoạt.
     * Input: status = Planning, startDate = null.
     * Expected: status vẫn là Planning.
     */
    @Test
    void activateIfStarted_withNullStartDateKeepsPlanning() {
        Projects project = planningProject(null);

        projectStatusService.activateIfStarted(project);

        assertThat(project.getProjectStatus()).isEqualTo("Planning");
    }

    private static Projects planningProject(LocalDate startDate) {
        Projects project = new Projects();
        project.setProjectStatus("Planning");
        project.setProjectStartDate(startDate);
        return project;
    }
}

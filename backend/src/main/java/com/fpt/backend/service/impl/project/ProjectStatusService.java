package com.fpt.backend.service.impl.project;

import com.fpt.backend.entity.Projects;
import com.fpt.backend.entity.Timeline;
import com.fpt.backend.enums.PhaseStatus;
import com.fpt.backend.enums.ProjectStatus;
import com.fpt.backend.repository.project.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ProjectStatusService {
    private static final String PLANNING_STATUS = "Planning";
    private static final String ACTIVE_STATUS = "Active";
    private static final String COMPLETED_STATUS = "Completed";
    private static final String OVER_DUE_STATUS =
            ProjectStatus.OVER_DUE.name();
    private static final ZoneId APP_TIME_ZONE =
            ZoneId.of("Asia/Ho_Chi_Minh");

    private final ProjectRepository projectRepository;

    // Làm mới trạng thái theo ngày cho các dự án chưa kết thúc.
    public void refreshPlanningProjects() {
        List<Projects> projects = new ArrayList<>(projectRepository
                .findAllByProjectStatusIgnoreCase(PLANNING_STATUS));
        projects.addAll(projectRepository
                .findAllByProjectStatusIgnoreCase(ACTIVE_STATUS));
        projects.addAll(projectRepository
                .findAllByProjectStatusIgnoreCase(OVER_DUE_STATUS));

        for (Projects project : projects) {
            refreshDateBasedStatus(project);
        }

        projectRepository.saveAll(projects);
        projectRepository.flush();
    }

    // Chuyển một dự án từ Planning sang Active khi đã đến ngày bắt đầu.
    public void activateIfStarted(Projects project) {
        // Bỏ qua dự án không còn ở trạng thái Planning.
        if (!PLANNING_STATUS.equalsIgnoreCase(
                project.getProjectStatus())) {
            return;
        }

        LocalDate startDate = project.getProjectStartDate();
        LocalDate today = LocalDate.now(APP_TIME_ZONE);

        // Dự án được duyệt sau hạn kết thúc phải chuyển thẳng sang quá hạn.
        if (hasPassedEndDate(project, today)) {
            project.setProjectStatus(OVER_DUE_STATUS);
            return;
        }

        // Chỉ kích hoạt khi dự án có ngày bắt đầu và ngày đó đã tới.
        if (startDate != null && !today.isBefore(startDate)) {
            project.setProjectStatus(ACTIVE_STATUS);
        }
    }

    // Đánh dấu dự án hoàn thành khi tất cả phase của dự án đã hoàn thành.
    public void completeIfAllPhasesCompleted(List<Timeline> phases) {
        // Không cập nhật dự án khi danh sách phase rỗng.
        if (phases.isEmpty()) {
            return;
        }

        Projects project = phases.get(0).getProject();
        boolean allPhasesCompleted = true;

        for (Timeline phase : phases) {
            if (phase.getStatus() != PhaseStatus.COMPLETED) {
                allPhasesCompleted = false;
                break;
            }
        }

        // Hoàn thành toàn bộ phase luôn được ưu tiên hơn trạng thái quá hạn.
        if (allPhasesCompleted) {
            project.setProjectStatus(COMPLETED_STATUS);
            projectRepository.save(project);
            projectRepository.flush();
            return;
        }

        // Tính lại hai chiều để project OVER_DUE có thể trở về trạng thái phù hợp
        // khi end date được gia hạn hoặc thời gian hệ thống được điều chỉnh.
        if (canRefreshDateBasedStatus(project)) {
            refreshDateBasedStatus(project);
            projectRepository.save(project);
            projectRepository.flush();
        }
    }

    // Cập nhật trạng thái Planning/Active/OVER_DUE theo mốc thời gian.
    private void refreshDateBasedStatus(Projects project) {
        LocalDate today = LocalDate.now(APP_TIME_ZONE);

        if (hasPassedEndDate(project, today)) {
            project.setProjectStatus(OVER_DUE_STATUS);
            return;
        }

        LocalDate startDate = project.getProjectStartDate();
        if (startDate != null && !today.isBefore(startDate)) {
            project.setProjectStatus(ACTIVE_STATUS);
            return;
        }

        project.setProjectStatus(PLANNING_STATUS);
    }

    // Chỉ tính lại các dự án đã qua bước chờ duyệt và chưa kết thúc.
    private boolean canRefreshDateBasedStatus(Projects project) {
        String status = project.getProjectStatus();
        return PLANNING_STATUS.equalsIgnoreCase(status)
                || ACTIVE_STATUS.equalsIgnoreCase(status)
                || OVER_DUE_STATUS.equalsIgnoreCase(status);
    }

    // End date chỉ được xem là quá hạn từ ngày kế tiếp.
    private boolean hasPassedEndDate(
            Projects project,
            LocalDate today) {
        LocalDate endDate = project.getProjectEndDate();
        return endDate != null && today.isAfter(endDate);
    }
}

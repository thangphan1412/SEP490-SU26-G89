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

    // Làm mới trạng thái phase cho toàn bộ dự án đang có timeline.
    public void refreshAllProjectStatuses() {
        for (UUID projectId : phaseRepository.findProjectIds()) {
            refreshProjectStatuses(projectId);
        }
    }

    // Tính lại tiến độ và trạng thái tuần tự của các phase trong một dự án.
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

    // Xác định trạng thái phase theo tiến độ, thời gian và trạng thái phase trước.
    private PhaseStatus calculateStatus(
            Timeline phase,
            double progress,
            LocalDate today,
            boolean firstPhase,
            PhaseStatus previousStatus,
            boolean projectIsWaiting) {
        // Đánh dấu hoàn thành khi mọi task của phase đã hoàn tất.
        if (progress >= 100D) {
            return PhaseStatus.COMPLETED;
        }

        // Giữ phase ở PLANNING khi dự án đang chờ hoặc lên kế hoạch.
        if (projectIsWaiting) {
            return PhaseStatus.PLANNING;
        }

        LocalDate startDate = toLocalDate(phase.getStartDate());
        LocalDate endDate = toLocalDate(phase.getEndDate());

        // Đánh dấu quá hạn khi ngày hiện tại vượt ngày kết thúc.
        if (today.isAfter(endDate)) {
            return PhaseStatus.OVER_DUE;
        }

        boolean dateIsInPhase = !today.isBefore(startDate)
                && !today.isAfter(endDate);
        boolean previousPhaseIsCompleted = previousStatus
                == PhaseStatus.COMPLETED;

        // Chỉ kích hoạt phase trong thời gian chạy khi phase trước đã hoàn thành.
        if (dateIsInPhase
                && (firstPhase || previousPhaseIsCompleted)) {
            return PhaseStatus.IN_PROGRESS;
        }

        return PhaseStatus.PLANNING;
    }

    // Chuyển Date sang LocalDate theo múi giờ ứng dụng.
    private LocalDate toLocalDate(Date value) {
        if (value instanceof java.sql.Date sqlDate) {
            return sqlDate.toLocalDate();
        }

        return value.toInstant().atZone(APP_TIME_ZONE).toLocalDate();
    }
}

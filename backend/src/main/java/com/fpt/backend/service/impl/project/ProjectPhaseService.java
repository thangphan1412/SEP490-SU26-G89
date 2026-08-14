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
public class ProjectPhaseService {
    private static final PhaseStatus DEFAULT_PHASE_STATUS = PhaseStatus.PLANNING;
    private static final ZoneId PROJECT_TIME_ZONE =
            ZoneId.of("Asia/Ho_Chi_Minh");

    private final PhaseRepository phaseRepository;
    private final PhaseTaskRepository phaseTaskRepository;
    private final PhaseDeliverableRepository phaseDeliverableRepository;
    private final PhaseContractRepository phaseContractRepository;
    private final PhaseStatusService phaseStatusService;

    // Đồng bộ phase của dự án theo lịch liên tục được gửi từ request.
    public void syncPhases(
            Projects project,
            List<ProjectPhaseRequest> phaseRequests) {
        List<ProjectPhaseRequest> requests = phaseRequests;
        validatePhaseSchedule(project, requests);

        Map<UUID, Timeline> existingPhases = new LinkedHashMap<>();

        for (Timeline phase : phaseRepository.findByProjectId(project.getId())) {
            existingPhases.put(phase.getId(), phase);
        }

        LocalDate nextStartDate = project.getProjectStartDate();

        for (ProjectPhaseRequest request : requests) {
            Timeline phase;

            // Tạo phase mới hoặc lấy phase hiện có để cập nhật.
            if (request.id() == null) {
                phase = new Timeline();
            } else {
                phase = existingPhases.remove(request.id());

                // Ngăn cập nhật phase không thuộc dự án hiện tại.
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

        phaseRepository.flush();
        phaseStatusService.refreshProjectStatuses(project.getId());
    }

    // Lấy danh sách phase của dự án sau khi làm mới trạng thái và tiến độ.
    public List<ProjectPhaseResponse> getProjectPhases(UUID projectId) {
        phaseStatusService.refreshProjectStatuses(projectId);
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
                    phase.getProgress()
            ));
        }

        return responses;
    }

    // Xóa dữ liệu phụ thuộc rồi xóa toàn bộ phase của dự án.
    public void deleteProjectData(UUID projectId) {
        phaseContractRepository.deleteByProjectId(projectId);
        phaseTaskRepository.deleteByProjectId(projectId);
        phaseDeliverableRepository.deleteByProjectId(projectId);
        phaseRepository.deleteByProjectId(projectId);
    }

    // Kiểm tra các phase phủ kín timeline dự án mà không vượt hoặc đảo ngày.
    private void validatePhaseSchedule(
            Projects project,
            List<ProjectPhaseRequest> requests) {
        LocalDate expectedStartDate = project.getProjectStartDate();

        for (int index = 0; index < requests.size(); index++) {
            ProjectPhaseRequest request = requests.get(index);
            int phaseNumber = index + 1;

            LocalDate endDate = request.endDate();

            // Từ chối phase bắt đầu sau ngày kết thúc dự án.
            if (expectedStartDate.isAfter(project.getProjectEndDate())) {
                throw new BadHttpException(
                        "Phase " + phaseNumber
                                + " starts after the project end date"
                );
            }

            // Từ chối phase có ngày bắt đầu sau ngày kết thúc của chính nó.
            if (expectedStartDate.isAfter(endDate)) {
                throw new BadHttpException(
                        "Phase " + phaseNumber
                                + " start date must not be after "
                                + "its end date"
                );
            }

            // Từ chối phase kết thúc ngoài timeline dự án.
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

        // Bảo đảm phase cuối kết thúc đúng ngày kết thúc dự án.
        if (!finalPhase.endDate().equals(project.getProjectEndDate())) {
            throw new BadHttpException(
                    "The final phase must end on the project end date "
                            + project.getProjectEndDate()
            );
        }
    }

    // Chuẩn hóa request rồi áp dụng thông tin và trạng thái mặc định vào phase.
    private void applyPhaseInformation(
            Timeline phase,
            ProjectPhaseRequest request,
            Projects project,
            LocalDate startDate) {
        String title = request.title().trim();
        String description = normalize(request.description());
        LocalDate endDate = request.endDate();

        phase.setTitle(title);
        phase.setDescription(description);
        phase.setStartDate(java.sql.Date.valueOf(startDate));
        phase.setEndDate(java.sql.Date.valueOf(endDate));

        // Khởi tạo trạng thái và tiến độ cho phase mới.
        if (phase.getId() == null) {
            phase.setStatus(DEFAULT_PHASE_STATUS);
            phase.setProgress(0D);
        }

        phase.setProject(project);
    }

    // Xóa phase khi phase chưa phát sinh task hoặc deliverable phụ thuộc.
    private void removePhase(Timeline phase) {
        long taskCount = phaseTaskRepository.countByPhaseId(phase.getId());
        long deliverableCount =
                phaseDeliverableRepository.countByPhaseId(phase.getId());

        // Ngăn xóa phase đang chứa task hoặc deliverable.
        if (taskCount > 0 || deliverableCount > 0) {
            throw new BadHttpException(
                    "Phase cannot be removed because it has tasks or deliverables"
            );
        }

        phaseRepository.delete(phase);
    }

    // Chuyển Date sang LocalDate theo múi giờ dự án.
    private LocalDate toLocalDate(Date value) {
        // Giữ nguyên giá trị thiếu thay vì phát sinh lỗi chuyển đổi.
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

    // Chuẩn hóa chuỗi null thành rỗng và loại bỏ khoảng trắng hai đầu.
    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }
}

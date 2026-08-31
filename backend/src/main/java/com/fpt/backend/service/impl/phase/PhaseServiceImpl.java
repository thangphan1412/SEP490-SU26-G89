package com.fpt.backend.service.impl.phase;

import com.fpt.backend.dto.response.phase.PhaseContractResponse;
import com.fpt.backend.dto.response.phase.PhaseDeliverableResponse;
import com.fpt.backend.dto.response.phase.PhaseDetailResponse;
import com.fpt.backend.dto.response.phase.PhaseListItemResponse;
import com.fpt.backend.dto.response.phase.PhaseTaskResponse;
import com.fpt.backend.dto.response.project.ProjectAccessResponse;
import com.fpt.backend.entity.Contracts;
import com.fpt.backend.entity.Deliverable;
import com.fpt.backend.entity.Projects;
import com.fpt.backend.entity.Timeline;
import com.fpt.backend.entity.TimelineContract;
import com.fpt.backend.entity.TimelineTask;
import com.fpt.backend.entity.Users;
import com.fpt.backend.enums.PhaseStatus;
import com.fpt.backend.exception.BadHttpException;
import com.fpt.backend.exception.NotFoundException;
import com.fpt.backend.repository.phase.PhaseContractRepository;
import com.fpt.backend.repository.phase.PhaseDeliverableRepository;
import com.fpt.backend.repository.phase.PhaseRepository;
import com.fpt.backend.repository.phase.PhaseTaskRepository;
import com.fpt.backend.repository.project.ProjectRepository;
import com.fpt.backend.service.interfaces.phase.IPhaseService;
import com.fpt.backend.service.interfaces.permission.IPermissionAccessService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class PhaseServiceImpl implements IPhaseService {
    private static final ZoneId APP_TIME_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

    private final PhaseRepository phaseRepository;
    private final PhaseTaskRepository phaseTaskRepository;
    private final PhaseDeliverableRepository phaseDeliverableRepository;
    private final PhaseContractRepository phaseContractRepository;
    private final ProjectRepository projectRepository;
    private final PhaseStatusService phaseStatusService;
    private final IPermissionAccessService permissionAccessService;

    // Lấy danh sách phase của dự án sau khi kiểm tra quyền truy cập và làm mới trạng thái.
    @Override
    public List<PhaseListItemResponse> getPhasesByProjectId(UUID projectId) {
        // Từ chối mã dự án thiếu hoặc không tồn tại.
        if (projectId == null || !projectRepository.existsById(projectId)) {
            throw new NotFoundException("Project not found");
        }

        permissionAccessService.requireProjectAccess(projectId);
        phaseStatusService.refreshProjectStatuses(projectId);

        List<Timeline> phases = phaseRepository.findByProjectId(projectId);
        List<PhaseListItemResponse> responses = new ArrayList<>();

        for (Timeline phase : phases) {
            responses.add(toListItem(phase));
        }

        return responses;
    }

    // Lấy chi tiết phase cùng task, deliverable và hợp đồng mà người dùng được xem.
    @Override
    public PhaseDetailResponse getPhaseById(UUID phaseId) {
        Timeline phase = findPhase(phaseId);
        Projects project = phase.getProject();
        ProjectAccessResponse access = permissionAccessService
                .getCurrentUserAccess(project.getId());

        phaseStatusService.refreshProjectStatuses(project.getId());
        phase = findPhase(phaseId);

        // Thành viên thường có thể mở phase để chuẩn bị task từ PLANNING.
        boolean phaseSupportsTaskPreparation =
                phase.getStatus() == PhaseStatus.PLANNING
                        || phase.getStatus() == PhaseStatus.IN_PROGRESS;

        if (!phaseSupportsTaskPreparation && !access.isExecutiveViewer()) {
            throw new BadHttpException(
                    "Only a PLANNING or IN_PROGRESS phase can be accessed"
            );
        }

        project = phase.getProject();
        List<PhaseTaskResponse> tasks = getVisibleTasks(phaseId, access);
        List<PhaseDeliverableResponse> deliverables =
                getVisibleDeliverables(phaseId, access);
        List<PhaseContractResponse> contracts =
                getVisibleContracts(phaseId, access);

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
                contracts,
                access
        );
    }

    // Lấy các task hiển thị theo action và phạm vi dữ liệu của người dùng.
    private List<PhaseTaskResponse> getVisibleTasks(
            UUID phaseId,
            ProjectAccessResponse access) {
        List<PhaseTaskResponse> responses = new ArrayList<>();

        boolean canViewTasks = permissionAccessService.hasAction(
                access,
                "VIEW_TASKS"
        );
        boolean canEditTasks = permissionAccessService.hasAction(
                access,
                "EDIT_TASKS"
        );

        // Không trả task khi người dùng không có action xem hoặc chỉnh sửa.
        if (!canViewTasks && !canEditTasks) {
            return responses;
        }

        List<TimelineTask> tasks;

        // Chọn toàn bộ task hoặc chỉ task được giao theo work scope.
        if (permissionAccessService.hasFullWorkScope(
                access,
                "VIEW_TASKS"
        )) {
            tasks = phaseTaskRepository.findByPhaseId(phaseId);
        } else {
            tasks = phaseTaskRepository.findByPhaseIdAndAssignedUserId(
                    phaseId,
                    access.currentUserId()
            );
        }

        for (TimelineTask task : tasks) {
            responses.add(toTaskResponse(task));
        }

        return responses;
    }

    // Lấy deliverable của phase khi người dùng có action xem tương ứng.
    private List<PhaseDeliverableResponse> getVisibleDeliverables(
            UUID phaseId,
            ProjectAccessResponse access) {
        List<PhaseDeliverableResponse> responses = new ArrayList<>();

        // Trả danh sách rỗng khi người dùng không có action xem deliverable.
        if (!permissionAccessService.hasAction(
                access,
                "VIEW_DELIVERABLES"
        )) {
            return responses;
        }

        for (Deliverable deliverable
                : phaseDeliverableRepository.findByPhaseId(phaseId)) {
            responses.add(new PhaseDeliverableResponse(
                    deliverable.getId(),
                    deliverable.getTitle(),
                    deliverable.getDescription(),
                    toLocalDate(deliverable.getDueDate()),
                    deliverable.getStatus()
            ));
        }

        return responses;
    }

    // Hợp nhất các hợp đồng liên kết trực tiếp và qua task của phase.
    private List<PhaseContractResponse> getVisibleContracts(
            UUID phaseId,
            ProjectAccessResponse access) {
        // Trả danh sách rỗng khi người dùng không có action xem hợp đồng.
        if (!permissionAccessService.hasAction(access, "VIEW_CONTRACTS")) {
            return new ArrayList<>();
        }

        Map<UUID, PhaseContractResponse> contractsById = new LinkedHashMap<>();

        // Giữ các hợp đồng sử dụng quan hệ timeline_contract cũ.
        for (TimelineContract phaseContract
                : phaseContractRepository.findByPhaseId(phaseId)) {
            Contracts contract = phaseContract.getContract();
            contractsById.put(
                    contract.getId(),
                    toContractResponse(contract, phaseContract.getLinkedAt())
            );
        }

        // Bổ sung hợp đồng mới liên kết với phase thông qua task đã chọn.
        for (Contracts contract
                : phaseContractRepository.findByTaskPhaseId(phaseId)) {
            contractsById.putIfAbsent(
                    contract.getId(),
                    toContractResponse(
                            contract,
                            contract.getContractCreatedAt()
                    )
            );
        }

        return new ArrayList<>(contractsById.values());
    }

    // Chuyển entity hợp đồng thành dữ liệu hợp đồng hiển thị trong phase.
    private PhaseContractResponse toContractResponse(
            Contracts contract,
            LocalDateTime linkedAt) {
        return new PhaseContractResponse(
                contract.getId(),
                contract.getContractNumber(),
                contract.getContractTitle(),
                contract.getContractStatus(),
                contract.getEffectiveDate(),
                contract.getExpirationDate(),
                linkedAt
        );
    }

    // Chuyển entity task thành dữ liệu task hiển thị trong phase.
    private PhaseTaskResponse toTaskResponse(TimelineTask task) {
        Users assignedUser = task.getAssignedTo();
        UUID assignedUserId = null;
        String assignedUserName = null;
        String assignedUserEmail = null;

        // Bổ sung thông tin người được giao khi task đã có assignee.
        if (assignedUser != null) {
            assignedUserId = assignedUser.getId();
            assignedUserName = getUserName(assignedUser);
            assignedUserEmail = assignedUser.getEmail();
        }

        return new PhaseTaskResponse(
                task.getId(),
                task.getTitle(),
                task.getStatus(),
                toLocalDate(task.getStartDate()),
                toLocalDate(task.getEndDate()),
                assignedUserId,
                assignedUserName,
                assignedUserEmail
        );
    }

    // Chuyển entity phase thành phần tử hiển thị trong danh sách.
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

    // Chuyển Date sang LocalDate theo múi giờ ứng dụng.
    private LocalDate toLocalDate(Date value) {
        // Giữ nguyên giá trị thiếu thay vì phát sinh lỗi chuyển đổi.
        if (value == null) {
            return null;
        }

        if (value instanceof java.sql.Date sqlDate) {
            return sqlDate.toLocalDate();
        }

        return value.toInstant().atZone(APP_TIME_ZONE).toLocalDate();
    }

    // Ghép tên người dùng và dùng email khi họ tên bị trống.
    private String getUserName(Users user) {
        String fullName = (normalize(user.getFirstName()) + " " + normalize(user.getLastName())).trim();
        return fullName.isBlank() ? user.getEmail() : fullName;
    }

    // Chuẩn hóa chuỗi null thành rỗng và loại bỏ khoảng trắng hai đầu.
    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }

    // Tìm phase kèm dự án hoặc báo không tìm thấy.
    private Timeline findPhase(UUID phaseId) {
        Optional<Timeline> optionalPhase = phaseRepository.findDetailById(
                phaseId
        );

        // Báo lỗi khi phase không tồn tại.
        if (optionalPhase.isEmpty()) {
            throw new NotFoundException("Phase not found");
        }

        return optionalPhase.get();
    }
}

package com.fpt.backend.service.impl.task;

import com.fpt.backend.dto.request.task.TaskCreateRequest;
import com.fpt.backend.dto.request.task.TaskUpdateRequest;
import com.fpt.backend.dto.response.project.ProjectAccessResponse;
import com.fpt.backend.dto.response.task.TaskContractResponse;
import com.fpt.backend.dto.response.task.TaskItemResponse;
import com.fpt.backend.dto.response.task.TaskManagementResponse;
import com.fpt.backend.dto.response.task.TaskMemberOptionResponse;
import com.fpt.backend.entity.Contracts;
import com.fpt.backend.entity.ProjectMember;
import com.fpt.backend.entity.Projects;
import com.fpt.backend.entity.Timeline;
import com.fpt.backend.entity.TimelineTask;
import com.fpt.backend.entity.Users;
import com.fpt.backend.enums.PhaseStatus;
import com.fpt.backend.enums.TaskStatus;
import com.fpt.backend.exception.BadHttpException;
import com.fpt.backend.exception.NotFoundException;
import com.fpt.backend.repository.phase.PhaseRepository;
import com.fpt.backend.repository.project.ProjectMemberRepository;
import com.fpt.backend.repository.task.TaskRepository;
import com.fpt.backend.service.impl.phase.PhaseStatusService;
import com.fpt.backend.service.interfaces.permission.IPermissionAccessService;
import com.fpt.backend.service.interfaces.task.ITaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TaskServiceImpl implements ITaskService {
    private static final ZoneId APP_TIME_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");
    private static final List<TaskStatus> EDITABLE_TASK_STATUSES = List.of(
            TaskStatus.TODO,
            TaskStatus.IN_PROGRESS,
            TaskStatus.ON_HOLD
    );

    private final TaskRepository taskRepository;
    private final PhaseRepository phaseRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final IPermissionAccessService permissionAccessService;
    private final PhaseStatusService phaseStatusService;

    // Lấy dữ liệu quản lý task của phase theo action và work scope hiện tại.
    @Override
    @Transactional
    public TaskManagementResponse getTasksByPhaseId(UUID phaseId) {
        Timeline phase = findPhase(phaseId);
        Projects project = phase.getProject();
        ProjectAccessResponse access = requireEditTaskAccess(project.getId());

        phaseStatusService.refreshProjectStatuses(project.getId());
        phase = findPhase(phaseId);
        validatePhaseIsInProgress(phase);
        project = phase.getProject();

        boolean fullWorkScope = permissionAccessService.hasFullWorkScope(
                access,
                "EDIT_TASKS"
        );
        boolean canViewContracts = permissionAccessService.hasAction(
                access,
                "VIEW_CONTRACTS"
        );
        boolean canCreateTasks = permissionAccessService.hasAction(
                access,
                "CREATE_TASKS"
        );
        boolean canApproveTasks = permissionAccessService.hasAction(
                access,
                "APPROVE_TASKS"
        );

        List<TimelineTask> tasks;

        // Lấy toàn bộ task với phạm vi FULL hoặc chỉ task được giao với phạm vi OWN.
        if (fullWorkScope) {
            tasks = taskRepository.findByPhaseId(phaseId);
        } else {
            tasks = taskRepository.findByPhaseIdAndAssignedUserId(
                    phaseId,
                    access.currentUserId()
            );
        }

        return new TaskManagementResponse(
                phase.getId(),
                phase.getTitle(),
                toLocalDate(phase.getStartDate()),
                toLocalDate(phase.getEndDate()),
                project.getId(),
                project.getProjectName(),
                fullWorkScope,
                canCreateTasks,
                canApproveTasks,
                EDITABLE_TASK_STATUSES,
                getMemberOptions(project.getId(), access, fullWorkScope),
                toTaskResponses(tasks, canViewContracts)
        );
    }

    // Tạo task mới trong phase sau khi kiểm tra quyền, trạng thái, ngày và assignee.
    @Override
    @Transactional
    public TaskItemResponse createTask(
            UUID phaseId,
            TaskCreateRequest request) {
        Timeline phase = findPhase(phaseId);
        UUID projectId = phase.getProject().getId();
        ProjectAccessResponse access = requireCreateTaskAccess(projectId);

        phaseStatusService.refreshProjectStatuses(projectId);
        phase = findPhase(phaseId);
        validatePhaseIsInProgress(phase);

        boolean fullWorkScope = permissionAccessService.hasFullWorkScope(
                access,
                "CREATE_TASKS"
        );

        validateDates(request.startDate(), request.endDate(), phase);
        Users assignedUser = findAssignedUser(
                projectId,
                request.assignedToId()
        );
        validateAssignee(access, assignedUser, fullWorkScope);

        TimelineTask task = new TimelineTask();
        task.setTitle(request.title().trim());
        task.setStartDate(java.sql.Date.valueOf(request.startDate()));
        task.setEndDate(java.sql.Date.valueOf(request.endDate()));
        task.setStatus(TaskStatus.TODO.name());
        task.setTimeline(phase);
        task.setAssignedTo(assignedUser);

        TimelineTask createdTask = taskRepository.save(task);
        taskRepository.flush();
        phaseStatusService.refreshProjectStatuses(projectId);
        return toTaskResponse(
                createdTask,
                permissionAccessService.hasAction(access, "VIEW_CONTRACTS")
        );
    }

    // Cập nhật task hiện có sau khi kiểm tra mọi điều kiện chỉnh sửa.
    @Override
    @Transactional
    public TaskItemResponse updateTask(
            UUID taskId,
            TaskUpdateRequest request) {
        TimelineTask task = findTask(taskId);
        Timeline phase = task.getTimeline();
        UUID projectId = phase.getProject().getId();
        ProjectAccessResponse access = requireEditTaskAccess(projectId);

        phaseStatusService.refreshProjectStatuses(projectId);
        task = findTask(taskId);
        phase = task.getTimeline();
        validateTaskIsEditable(task);
        validatePhaseIsInProgress(phase);
        validateEditableStatus(request.status());

        boolean fullWorkScope = permissionAccessService.hasFullWorkScope(
                access,
                "EDIT_TASKS"
        );

        validateTaskAccess(task, access, fullWorkScope);
        validateDates(
                request.startDate(),
                request.endDate(),
                phase
        );

        Users assignedUser = findAssignedUser(
                phase.getProject().getId(),
                request.assignedToId()
        );
        validateAssignee(access, assignedUser, fullWorkScope);

        task.setTitle(request.title().trim());
        task.setStartDate(java.sql.Date.valueOf(request.startDate()));
        task.setEndDate(java.sql.Date.valueOf(request.endDate()));
        task.setStatus(request.status().name());
        task.setAssignedTo(assignedUser);

        TimelineTask updatedTask = taskRepository.save(task);
        taskRepository.flush();
        phaseStatusService.refreshProjectStatuses(projectId);
        return toTaskResponse(
                updatedTask,
                permissionAccessService.hasAction(access, "VIEW_CONTRACTS")
        );
    }

    // Đánh dấu task hoàn thành khi người dùng có quyền phê duyệt và chỉnh sửa task.
    @Override
    @Transactional
    public TaskItemResponse markTaskAsDone(UUID taskId) {
        TimelineTask task = findTask(taskId);
        UUID projectId = task.getTimeline().getProject().getId();
        ProjectAccessResponse access = requireEditTaskAccess(projectId);

        // Yêu cầu action APPROVE_TASKS trước khi hoàn thành task.
        if (!permissionAccessService.hasAction(access, "APPROVE_TASKS")) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You do not have permission to approve tasks"
            );
        }

        phaseStatusService.refreshProjectStatuses(projectId);
        task = findTask(taskId);
        validateTaskIsEditable(task);
        validatePhaseIsInProgress(task.getTimeline());

        boolean fullWorkScope = permissionAccessService.hasFullWorkScope(
                access,
                "EDIT_TASKS"
        );

        validateTaskAccess(task, access, fullWorkScope);
        task.setStatus(TaskStatus.DONE.name());

        TimelineTask updatedTask = taskRepository.save(task);
        taskRepository.flush();
        phaseStatusService.refreshProjectStatuses(projectId);
        return toTaskResponse(
                updatedTask,
                permissionAccessService.hasAction(access, "VIEW_CONTRACTS")
        );
    }

    // Tìm phase kèm dự án hoặc báo không tìm thấy.
    private Timeline findPhase(UUID phaseId) {
        Optional<Timeline> optionalPhase = phaseRepository.findDetailById(phaseId);

        // Báo lỗi khi phase không tồn tại.
        if (optionalPhase.isEmpty()) {
            throw new NotFoundException("Phase not found");
        }

        return optionalPhase.get();
    }

    // Tìm task kèm phase, dự án và assignee hoặc báo không tìm thấy.
    private TimelineTask findTask(UUID taskId) {
        Optional<TimelineTask> optionalTask = taskRepository.findDetailById(taskId);

        // Báo lỗi khi task không tồn tại.
        if (optionalTask.isEmpty()) {
            throw new NotFoundException("Task not found");
        }

        return optionalTask.get();
    }

    // Lấy quyền truy cập và yêu cầu action chỉnh sửa task trong dự án.
    private ProjectAccessResponse requireEditTaskAccess(UUID projectId) {
        ProjectAccessResponse access = permissionAccessService
                .getCurrentUserAccess(projectId);

        // Từ chối khi người dùng không có action EDIT_TASKS.
        if (!permissionAccessService.hasAction(access, "EDIT_TASKS")) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You do not have permission to edit tasks"
            );
        }

        return access;
    }

    // Lấy quyền truy cập và yêu cầu action tạo task trong dự án.
    private ProjectAccessResponse requireCreateTaskAccess(UUID projectId) {
        ProjectAccessResponse access = permissionAccessService
                .getCurrentUserAccess(projectId);

        // Từ chối khi người dùng không có action CREATE_TASKS.
        if (!permissionAccessService.hasAction(access, "CREATE_TASKS")) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You do not have permission to create tasks"
            );
        }

        return access;
    }

    // Kiểm tra task chưa hoàn thành để vẫn có thể chỉnh sửa.
    private void validateTaskIsEditable(TimelineTask task) {
        // Không cho phép thay đổi task đã ở trạng thái DONE.
        if (TaskStatus.DONE.name().equalsIgnoreCase(task.getStatus())) {
            throw new BadHttpException(
                    "A completed task cannot be edited"
            );
        }
    }

    // Kiểm tra trạng thái cập nhật nằm trong tập trạng thái được phép chỉnh sửa.
    private void validateEditableStatus(TaskStatus status) {
        // Từ chối trạng thái nằm ngoài tập trạng thái chỉnh sửa được hỗ trợ.
        if (!EDITABLE_TASK_STATUSES.contains(status)) {
            throw new BadHttpException(
                    "Task status must be TODO, IN_PROGRESS or ON_HOLD"
            );
        }
    }

    // Kiểm tra phase đang IN_PROGRESS trước khi quản lý task.
    private void validatePhaseIsInProgress(Timeline phase) {
        // Chỉ cho phép quản lý task khi phase đang được thực hiện.
        if (phase.getStatus() != PhaseStatus.IN_PROGRESS) {
            throw new BadHttpException(
                    "Tasks can only be managed while the phase is IN_PROGRESS"
            );
        }
    }

    // Kiểm tra người dùng phạm vi OWN chỉ chỉnh sửa task được giao cho chính họ.
    private void validateTaskAccess(
            TimelineTask task,
            ProjectAccessResponse access,
            boolean fullWorkScope) {
        // Bỏ qua giới hạn assignee khi người dùng có phạm vi FULL.
        if (fullWorkScope) {
            return;
        }

        Users assignedUser = task.getAssignedTo();

        // Từ chối task chưa giao hoặc được giao cho người dùng khác.
        if (assignedUser == null
                || !assignedUser.getId().equals(access.currentUserId())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You can only edit tasks assigned to you"
            );
        }
    }

    // Kiểm tra người dùng phạm vi OWN chỉ được giao task cho chính họ.
    private void validateAssignee(
            ProjectAccessResponse access,
            Users assignedUser,
            boolean fullWorkScope) {
        // Bỏ qua giới hạn assignee khi người dùng có phạm vi FULL.
        if (fullWorkScope) {
            return;
        }

        // Từ chối assignee trống hoặc khác người dùng hiện tại.
        if (assignedUser == null
                || !assignedUser.getId().equals(access.currentUserId())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You cannot assign this task to another member"
            );
        }
    }

    // Kiểm tra ngày task hợp lệ và nằm trọn trong timeline của phase.
    private void validateDates(
            LocalDate startDate,
            LocalDate endDate,
            Timeline phase) {
        LocalDate phaseStartDate = toLocalDate(phase.getStartDate());
        LocalDate phaseEndDate = toLocalDate(phase.getEndDate());

        // Ngăn ngày bắt đầu task nằm sau ngày kết thúc.
        if (startDate.isAfter(endDate)) {
            throw new BadHttpException(
                    "Task start date must not be after its end date"
            );
        }

        // Ngăn task bắt đầu trước hoặc kết thúc sau phase.
        if (startDate.isBefore(phaseStartDate)
                || endDate.isAfter(phaseEndDate)) {
            throw new BadHttpException(
                    "Task dates must be within the phase timeline"
            );
        }
    }

    // Tìm assignee trong danh sách thành viên dự án hoặc trả về null khi chưa gán.
    private Users findAssignedUser(UUID projectId, UUID assignedToId) {
        // Cho phép task chưa được gán cho thành viên cụ thể.
        if (assignedToId == null) {
            return null;
        }

        List<ProjectMember> members = projectMemberRepository
                .findByProjectId(projectId);

        for (ProjectMember member : members) {
            Users user = member.getUser();

            if (user.getId().equals(assignedToId)) {
                return user;
            }
        }

        throw new BadHttpException(
                "The selected assignee is not a member of this project"
        );
    }

    // Lấy danh sách thành viên có thể được chọn theo work scope của người dùng.
    private List<TaskMemberOptionResponse> getMemberOptions(
            UUID projectId,
            ProjectAccessResponse access,
            boolean fullWorkScope) {
        List<TaskMemberOptionResponse> options = new ArrayList<>();

        for (ProjectMember member
                : projectMemberRepository.findByProjectId(projectId)) {
            Users user = member.getUser();

            if (fullWorkScope
                    || user.getId().equals(access.currentUserId())) {
                options.add(new TaskMemberOptionResponse(
                        user.getId(),
                        getUserName(user),
                        user.getEmail()
                ));
            }
        }

        return options;
    }

    // Chuyển danh sách entity task thành danh sách response.
    private List<TaskItemResponse> toTaskResponses(
            List<TimelineTask> tasks,
            boolean canViewContracts) {
        List<TaskItemResponse> responses = new ArrayList<>();

        for (TimelineTask task : tasks) {
            responses.add(toTaskResponse(task, canViewContracts));
        }

        return responses;
    }

    // Chuyển entity task thành dữ liệu chi tiết trả về cho client.
    private TaskItemResponse toTaskResponse(
            TimelineTask task,
            boolean canViewContracts) {
        Users assignedUser = task.getAssignedTo();
        UUID assignedUserId = null;
        String assignedUserName = null;
        String assignedUserEmail = null;

        // Bổ sung thông tin assignee khi task đã được giao.
        if (assignedUser != null) {
            assignedUserId = assignedUser.getId();
            assignedUserName = getUserName(assignedUser);
            assignedUserEmail = assignedUser.getEmail();
        }

        return new TaskItemResponse(
                task.getId(),
                task.getTitle(),
                task.getStatus(),
                toLocalDate(task.getStartDate()),
                toLocalDate(task.getEndDate()),
                assignedUserId,
                assignedUserName,
                assignedUserEmail,
                getTaskContracts(task.getId(), canViewContracts)
        );
    }

    // Lấy hợp đồng liên kết với task khi người dùng có action xem hợp đồng.
    private List<TaskContractResponse> getTaskContracts(
            UUID taskId,
            boolean canViewContracts) {
        List<TaskContractResponse> responses = new ArrayList<>();

        // Không truy vấn hợp đồng khi người dùng không có quyền xem.
        if (!canViewContracts) {
            return responses;
        }

        for (Contracts contract
                : taskRepository.findContractsByTaskId(taskId)) {
            responses.add(new TaskContractResponse(
                    contract.getId(),
                    contract.getContractNumber(),
                    contract.getContractTitle()
            ));
        }

        return responses;
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
        String firstName = user.getFirstName() == null
                ? ""
                : user.getFirstName().trim();
        String lastName = user.getLastName() == null
                ? ""
                : user.getLastName().trim();
        String fullName = (firstName + " " + lastName).trim();

        // Ưu tiên họ tên khi người dùng đã cung cấp.
        if (!fullName.isBlank()) {
            return fullName;
        }

        return user.getEmail();
    }
}

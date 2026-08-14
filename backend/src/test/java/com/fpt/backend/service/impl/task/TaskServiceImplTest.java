package com.fpt.backend.service.impl.task;

import com.fpt.backend.dto.request.task.TaskCreateRequest;
import com.fpt.backend.dto.request.task.TaskUpdateRequest;
import com.fpt.backend.dto.response.project.ProjectAccessResponse;
import com.fpt.backend.dto.response.task.TaskManagementResponse;
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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskServiceImplTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private PhaseRepository phaseRepository;

    @Mock
    private ProjectMemberRepository projectMemberRepository;

    @Mock
    private IPermissionAccessService permissionAccessService;

    @Mock
    private PhaseStatusService phaseStatusService;

    @InjectMocks
    private TaskServiceImpl taskService;

    // Kiểm tra full scope trả toàn bộ task, thành viên và contract mà người dùng được xem.
    @Test
    void getTasksByPhaseId_fullScope_returnsAllTasksMembersAndVisibleContracts() {
        Timeline phase = phase(PhaseStatus.IN_PROGRESS);
        Users current = user("Lan", "Nguyen", "lan@g89.vn");
        Users other = user("Mai", "Tran", "mai@g89.vn");
        ProjectAccessResponse access = access(current.getId());
        TimelineTask task = task(phase, other, TaskStatus.IN_PROGRESS);
        Contracts contract = contract(task);
        stubPhaseAndAccess(phase, access);
        when(permissionAccessService.hasAction(access, "EDIT_TASKS")).thenReturn(true);
        when(permissionAccessService.hasAction(access, "VIEW_CONTRACTS")).thenReturn(true);
        when(permissionAccessService.hasAction(access, "CREATE_TASKS")).thenReturn(true);
        when(permissionAccessService.hasAction(access, "APPROVE_TASKS")).thenReturn(true);
        when(permissionAccessService.hasFullWorkScope(access, "EDIT_TASKS"))
                .thenReturn(true);
        when(taskRepository.findByPhaseId(phase.getId())).thenReturn(List.of(task));
        when(projectMemberRepository.findByProjectId(phase.getProject().getId()))
                .thenReturn(List.of(
                        member(phase.getProject(), current),
                        member(phase.getProject(), other)
                ));
        when(taskRepository.findContractsByTaskId(task.getId()))
                .thenReturn(List.of(contract));

        TaskManagementResponse result = taskService.getTasksByPhaseId(phase.getId());

        assertThat(result.isFullWorkScope()).isTrue();
        assertThat(result.isCanCreateTasks()).isTrue();
        assertThat(result.isCanApproveTasks()).isTrue();
        assertThat(result.getStatusOptions()).containsExactly(
                TaskStatus.TODO, TaskStatus.IN_PROGRESS, TaskStatus.ON_HOLD
        );
        assertThat(result.getMemberOptions()).hasSize(2);
        assertThat(result.getTasks()).singleElement().satisfies(response -> {
            assertThat(response.getAssignedToName()).isEqualTo("Mai Tran");
            assertThat(response.getContracts()).singleElement().satisfies(item ->
                    assertThat(item.getId()).isEqualTo(contract.getId())
            );
        });
        verify(taskRepository).findByPhaseId(phase.getId());
        verify(taskRepository, never()).findByPhaseIdAndAssignedUserId(
                phase.getId(), current.getId()
        );
    }

    // Kiểm tra người dùng thiếu action tạo task nhận lỗi Forbidden.
    @Test
    void createTask_withoutCreateAction_returnsForbidden() {
        Timeline phase = phase(PhaseStatus.IN_PROGRESS);
        ProjectAccessResponse access = access(UUID.randomUUID());
        when(phaseRepository.findDetailById(phase.getId()))
                .thenReturn(Optional.of(phase));
        when(permissionAccessService.getCurrentUserAccess(
                phase.getProject().getId()
        )).thenReturn(access);
        when(permissionAccessService.hasAction(access, "CREATE_TASKS"))
                .thenReturn(false);

        assertThatThrownBy(() -> taskService.createTask(
                phase.getId(), createRequest(null)
        )).isInstanceOfSatisfying(ResponseStatusException.class, exception ->
                assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN)
        );
    }

    // Kiểm tra full scope tạo task hợp lệ, loại khoảng trắng và đặt trạng thái mặc định TODO.
    @Test
    void createTask_fullScopeValidRequest_createsTrimmedTodoTask() {
        Timeline phase = phase(PhaseStatus.IN_PROGRESS);
        Users assignee = user("Mai", "Tran", "mai@g89.vn");
        ProjectAccessResponse access = access(UUID.randomUUID());
        stubPhaseAndAccess(phase, access);
        when(permissionAccessService.hasAction(access, "CREATE_TASKS")).thenReturn(true);
        when(permissionAccessService.hasAction(access, "VIEW_CONTRACTS")).thenReturn(false);
        when(permissionAccessService.hasFullWorkScope(access, "CREATE_TASKS"))
                .thenReturn(true);
        when(projectMemberRepository.findByProjectId(phase.getProject().getId()))
                .thenReturn(List.of(member(phase.getProject(), assignee)));
        when(taskRepository.save(any(TimelineTask.class))).thenAnswer(invocation -> {
            TimelineTask saved = invocation.getArgument(0);
            saved.setId(UUID.randomUUID());
            return saved;
        });
        TaskCreateRequest request = createRequest(assignee.getId());

        var result = taskService.createTask(phase.getId(), request);

        ArgumentCaptor<TimelineTask> captor = ArgumentCaptor.forClass(TimelineTask.class);
        verify(taskRepository).save(captor.capture());
        assertThat(captor.getValue()).satisfies(task -> {
            assertThat(task.getTitle()).isEqualTo("Prepare samples");
            assertThat(task.getStatus()).isEqualTo("TODO");
            assertThat(task.getTimeline()).isSameAs(phase);
            assertThat(task.getAssignedTo()).isSameAs(assignee);
        });
        assertThat(result.getStatus()).isEqualTo("TODO");
        verify(taskRepository).flush();
        verify(phaseStatusService, org.mockito.Mockito.times(2))
                .refreshProjectStatuses(phase.getProject().getId());
    }

    // Kiểm tra ngày thực hiện task bắt buộc phải nằm hoàn toàn trong khoảng ngày của phase.
    @Test
    void createTask_datesOutsidePhase_rejectsDateRange() {
        Timeline phase = phase(PhaseStatus.IN_PROGRESS);
        ProjectAccessResponse access = access(UUID.randomUUID());
        stubPhaseAndAccess(phase, access);
        when(permissionAccessService.hasAction(access, "CREATE_TASKS")).thenReturn(true);
        when(permissionAccessService.hasFullWorkScope(access, "CREATE_TASKS"))
                .thenReturn(true);
        TaskCreateRequest request = new TaskCreateRequest(
                "Task",
                LocalDate.of(2026, 8, 1),
                LocalDate.of(2026, 8, 15),
                null
        );

        assertThatThrownBy(() -> taskService.createTask(phase.getId(), request))
                .isInstanceOf(BadHttpException.class)
                .hasMessageContaining("within the phase timeline");
    }

    // Kiểm tra task đã hoàn thành không thể tiếp tục chỉnh sửa.
    @Test
    void updateTask_completedTask_rejectsModification() {
        TimelineTask task = task(phase(PhaseStatus.IN_PROGRESS), user("A", "B", "a@g89.vn"), TaskStatus.DONE);
        ProjectAccessResponse access = access(task.getAssignedTo().getId());
        stubTaskAndAccess(task, access);
        when(permissionAccessService.hasAction(access, "EDIT_TASKS")).thenReturn(true);

        assertThatThrownBy(() -> taskService.updateTask(
                task.getId(), updateRequest(TaskStatus.TODO)
        )).isInstanceOf(BadHttpException.class)
                .hasMessageContaining("completed task");
    }

    // Kiểm tra full scope cập nhật task hợp lệ và làm mới trạng thái phase liên quan.
    @Test
    void updateTask_validFullScopeRequest_updatesTaskAndRefreshesPhase() {
        Timeline phase = phase(PhaseStatus.IN_PROGRESS);
        Users assignee = user("Mai", "Tran", "mai@g89.vn");
        TimelineTask task = task(phase, assignee, TaskStatus.TODO);
        ProjectAccessResponse access = access(UUID.randomUUID());
        stubTaskAndAccess(task, access);
        when(permissionAccessService.hasAction(access, "EDIT_TASKS")).thenReturn(true);
        when(permissionAccessService.hasAction(access, "VIEW_CONTRACTS")).thenReturn(false);
        when(permissionAccessService.hasFullWorkScope(access, "EDIT_TASKS"))
                .thenReturn(true);
        when(projectMemberRepository.findByProjectId(phase.getProject().getId()))
                .thenReturn(List.of(member(phase.getProject(), assignee)));
        when(taskRepository.save(task)).thenReturn(task);
        TaskUpdateRequest request = new TaskUpdateRequest(
                " Updated task ",
                LocalDate.of(2026, 8, 12),
                LocalDate.of(2026, 8, 22),
                TaskStatus.IN_PROGRESS,
                assignee.getId()
        );

        var result = taskService.updateTask(task.getId(), request);

        assertThat(task.getTitle()).isEqualTo("Updated task");
        assertThat(task.getStatus()).isEqualTo("IN_PROGRESS");
        assertThat(result.getStatus()).isEqualTo("IN_PROGRESS");
        verify(taskRepository).flush();
        verify(phaseStatusService, org.mockito.Mockito.times(2))
                .refreshProjectStatuses(phase.getProject().getId());
    }

    // Kiểm tra người được phép đánh dấu task của mình thành DONE và nhận dữ liệu đã cập nhật.
    @Test
    void markTaskAsDone_validOwnTask_marksDoneAndReturnsResponse() {
        Users current = user("Lan", "Nguyen", "lan@g89.vn");
        TimelineTask task = task(phase(PhaseStatus.IN_PROGRESS), current, TaskStatus.IN_PROGRESS);
        ProjectAccessResponse access = access(current.getId());
        stubTaskAndAccess(task, access);
        when(permissionAccessService.hasAction(access, "EDIT_TASKS")).thenReturn(true);
        when(permissionAccessService.hasAction(access, "APPROVE_TASKS")).thenReturn(true);
        when(permissionAccessService.hasAction(access, "VIEW_CONTRACTS")).thenReturn(false);
        when(permissionAccessService.hasFullWorkScope(access, "EDIT_TASKS"))
                .thenReturn(false);
        when(taskRepository.save(task)).thenReturn(task);

        var result = taskService.markTaskAsDone(task.getId());

        assertThat(task.getStatus()).isEqualTo("DONE");
        assertThat(result.getStatus()).isEqualTo("DONE");
        verify(taskRepository).flush();
    }

    private void stubPhaseAndAccess(
            Timeline phase,
            ProjectAccessResponse access) {
        when(phaseRepository.findDetailById(phase.getId()))
                .thenReturn(Optional.of(phase));
        when(permissionAccessService.getCurrentUserAccess(
                phase.getProject().getId()
        )).thenReturn(access);
    }

    private void stubTaskAndAccess(
            TimelineTask task,
            ProjectAccessResponse access) {
        when(taskRepository.findDetailById(task.getId())).thenReturn(Optional.of(task));
        when(permissionAccessService.getCurrentUserAccess(
                task.getTimeline().getProject().getId()
        )).thenReturn(access);
    }

    private static ProjectAccessResponse access(UUID currentUserId) {
        return ProjectAccessResponse.builder()
                .projectId(UUID.randomUUID())
                .currentUserId(currentUserId)
                .allowedActions(List.of())
                .fullScopeActions(List.of())
                .workScope("OWN")
                .build();
    }

    private static Timeline phase(PhaseStatus status) {
        Projects project = new Projects();
        project.setId(UUID.randomUUID());
        project.setProjectName("Winter Collection");
        Timeline phase = new Timeline();
        phase.setId(UUID.randomUUID());
        phase.setProject(project);
        phase.setTitle("Execution");
        phase.setStartDate(java.sql.Date.valueOf(LocalDate.of(2026, 8, 10)));
        phase.setEndDate(java.sql.Date.valueOf(LocalDate.of(2026, 8, 31)));
        phase.setStatus(status);
        return phase;
    }

    private static TimelineTask task(
            Timeline phase,
            Users assignee,
            TaskStatus status) {
        TimelineTask task = new TimelineTask();
        task.setId(UUID.randomUUID());
        task.setTimeline(phase);
        task.setAssignedTo(assignee);
        task.setTitle("Task");
        task.setStatus(status.name());
        task.setStartDate(java.sql.Date.valueOf(LocalDate.of(2026, 8, 11)));
        task.setEndDate(java.sql.Date.valueOf(LocalDate.of(2026, 8, 20)));
        return task;
    }

    private static Users user(String firstName, String lastName, String email) {
        Users user = new Users();
        user.setId(UUID.randomUUID());
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(email);
        return user;
    }

    private static ProjectMember member(Projects project, Users user) {
        ProjectMember member = new ProjectMember();
        member.setProject(project);
        member.setUser(user);
        return member;
    }

    private static Contracts contract(TimelineTask task) {
        Contracts contract = new Contracts();
        contract.setId(UUID.randomUUID());
        contract.setContractNumber("C-001");
        contract.setContractTitle("Supply contract");
        contract.setTimelineTask(task);
        return contract;
    }

    private static TaskCreateRequest createRequest(UUID assignedToId) {
        return new TaskCreateRequest(
                " Prepare samples ",
                LocalDate.of(2026, 8, 11),
                LocalDate.of(2026, 8, 20),
                assignedToId
        );
    }

    private static TaskUpdateRequest updateRequest(TaskStatus status) {
        return new TaskUpdateRequest(
                " Updated task ",
                LocalDate.of(2026, 8, 12),
                LocalDate.of(2026, 8, 22),
                status,
                null
        );
    }
}

package com.fpt.backend.service.impl.task;

import com.fpt.backend.dto.request.task.TaskUpdateRequest;
import com.fpt.backend.dto.response.project.ProjectAccessResponse;
import com.fpt.backend.dto.response.task.TaskItemResponse;
import com.fpt.backend.dto.response.task.TaskManagementResponse;
import com.fpt.backend.dto.response.task.TaskMemberOptionResponse;
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

    private final TaskRepository taskRepository;
    private final PhaseRepository phaseRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final IPermissionAccessService permissionAccessService;
    private final PhaseStatusService phaseStatusService;

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

        List<TimelineTask> tasks;

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
                List.of(TaskStatus.values()),
                getMemberOptions(project.getId(), access, fullWorkScope),
                toTaskResponses(tasks)
        );
    }

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
        validatePhaseIsInProgress(phase);

        boolean fullWorkScope = permissionAccessService.hasFullWorkScope(
                access,
                "EDIT_TASKS"
        );

        validateTaskAccess(task, access, fullWorkScope);
        validateDates(request, phase);

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
        return toTaskResponse(updatedTask);
    }

    @Override
    @Transactional
    public TaskItemResponse markTaskAsDone(UUID taskId) {
        TimelineTask task = findTask(taskId);
        UUID projectId = task.getTimeline().getProject().getId();
        ProjectAccessResponse access = requireEditTaskAccess(projectId);

        phaseStatusService.refreshProjectStatuses(projectId);
        task = findTask(taskId);
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
        return toTaskResponse(updatedTask);
    }

    private Timeline findPhase(UUID phaseId) {
        Optional<Timeline> optionalPhase = phaseRepository.findDetailById(phaseId);

        if (optionalPhase.isEmpty()) {
            throw new NotFoundException("Phase not found");
        }

        return optionalPhase.get();
    }

    private TimelineTask findTask(UUID taskId) {
        Optional<TimelineTask> optionalTask = taskRepository.findDetailById(taskId);

        if (optionalTask.isEmpty()) {
            throw new NotFoundException("Task not found");
        }

        return optionalTask.get();
    }

    private ProjectAccessResponse requireEditTaskAccess(UUID projectId) {
        ProjectAccessResponse access = permissionAccessService
                .getCurrentUserAccess(projectId);

        if (!permissionAccessService.hasAction(access, "EDIT_TASKS")) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You do not have permission to edit tasks"
            );
        }

        return access;
    }

    private void validatePhaseIsInProgress(Timeline phase) {
        if (phase.getStatus() != PhaseStatus.IN_PROGRESS) {
            throw new BadHttpException(
                    "Tasks can only be managed while the phase is IN_PROGRESS"
            );
        }
    }

    private void validateTaskAccess(
            TimelineTask task,
            ProjectAccessResponse access,
            boolean fullWorkScope) {
        if (fullWorkScope) {
            return;
        }

        Users assignedUser = task.getAssignedTo();

        if (assignedUser == null
                || !assignedUser.getId().equals(access.currentUserId())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You can only edit tasks assigned to you"
            );
        }
    }

    private void validateAssignee(
            ProjectAccessResponse access,
            Users assignedUser,
            boolean fullWorkScope) {
        if (fullWorkScope) {
            return;
        }

        if (assignedUser == null
                || !assignedUser.getId().equals(access.currentUserId())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You cannot assign this task to another member"
            );
        }
    }

    private void validateDates(TaskUpdateRequest request, Timeline phase) {
        LocalDate startDate = request.startDate();
        LocalDate endDate = request.endDate();
        LocalDate phaseStartDate = toLocalDate(phase.getStartDate());
        LocalDate phaseEndDate = toLocalDate(phase.getEndDate());

        if (startDate.isAfter(endDate)) {
            throw new BadHttpException(
                    "Task start date must not be after its end date"
            );
        }

        if (startDate.isBefore(phaseStartDate)
                || endDate.isAfter(phaseEndDate)) {
            throw new BadHttpException(
                    "Task dates must be within the phase timeline"
            );
        }
    }

    private Users findAssignedUser(UUID projectId, UUID assignedToId) {
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

    private List<TaskItemResponse> toTaskResponses(
            List<TimelineTask> tasks) {
        List<TaskItemResponse> responses = new ArrayList<>();

        for (TimelineTask task : tasks) {
            responses.add(toTaskResponse(task));
        }

        return responses;
    }

    private TaskItemResponse toTaskResponse(TimelineTask task) {
        Users assignedUser = task.getAssignedTo();
        UUID assignedUserId = null;
        String assignedUserName = null;
        String assignedUserEmail = null;

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
                assignedUserEmail
        );
    }

    private LocalDate toLocalDate(Date value) {
        if (value == null) {
            return null;
        }

        if (value instanceof java.sql.Date sqlDate) {
            return sqlDate.toLocalDate();
        }

        return value.toInstant().atZone(APP_TIME_ZONE).toLocalDate();
    }

    private String getUserName(Users user) {
        String firstName = user.getFirstName() == null
                ? ""
                : user.getFirstName().trim();
        String lastName = user.getLastName() == null
                ? ""
                : user.getLastName().trim();
        String fullName = (firstName + " " + lastName).trim();

        if (!fullName.isBlank()) {
            return fullName;
        }

        return user.getEmail();
    }
}

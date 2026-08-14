package com.fpt.backend.service.impl.phase;

import com.fpt.backend.dto.response.phase.PhaseDetailResponse;
import com.fpt.backend.dto.response.phase.PhaseListItemResponse;
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
import com.fpt.backend.service.interfaces.permission.IPermissionAccessService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PhaseServiceImplTest {

    @Mock
    private PhaseRepository phaseRepository;

    @Mock
    private PhaseTaskRepository phaseTaskRepository;

    @Mock
    private PhaseDeliverableRepository phaseDeliverableRepository;

    @Mock
    private PhaseContractRepository phaseContractRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private PhaseStatusService phaseStatusService;

    @Mock
    private IPermissionAccessService permissionAccessService;

    @InjectMocks
    private PhaseServiceImpl phaseService;

    // Kiểm tra dự án được phép truy cập sẽ làm mới trạng thái và trả đúng danh sách phase.
    @Test
    void getPhasesByProjectId_accessibleProject_refreshesAndMapsPhases() {
        Projects project = project();
        Timeline phase = phase(project, PhaseStatus.IN_PROGRESS);
        when(projectRepository.existsById(project.getId())).thenReturn(true);
        when(phaseRepository.findByProjectId(project.getId())).thenReturn(List.of(phase));

        List<PhaseListItemResponse> result =
                phaseService.getPhasesByProjectId(project.getId());

        verify(permissionAccessService).requireProjectAccess(project.getId());
        verify(phaseStatusService).refreshProjectStatuses(project.getId());
        assertThat(result).singleElement().satisfies(response -> {
            assertThat(response.getId()).isEqualTo(phase.getId());
            assertThat(response.getProjectId()).isEqualTo(project.getId());
            assertThat(response.getProjectCode()).isEqualTo(project.getProjectCode());
            assertThat(response.getStatus()).isEqualTo(PhaseStatus.IN_PROGRESS);
        });
    }

    // Kiểm tra lấy chi tiết phase không tồn tại trả về lỗi NotFound.
    @Test
    void getPhaseById_unknownPhase_throwsNotFound() {
        UUID phaseId = UUID.randomUUID();
        when(phaseRepository.findDetailById(phaseId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> phaseService.getPhaseById(phaseId))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Phase not found");
    }

    // Kiểm tra thành viên thường không được xem chi tiết phase chưa chạy.
    @Test
    void getPhaseById_nonRunningPhaseForRegularMember_rejectsAccess() {
        Timeline phase = phase(project(), PhaseStatus.PLANNING);
        ProjectAccessResponse access = access(false);
        when(phaseRepository.findDetailById(phase.getId()))
                .thenReturn(Optional.of(phase));
        when(permissionAccessService.getCurrentUserAccess(
                phase.getProject().getId()
        )).thenReturn(access);

        assertThatThrownBy(() -> phaseService.getPhaseById(phase.getId()))
                .isInstanceOf(BadHttpException.class)
                .hasMessageContaining("Only an IN_PROGRESS phase");
    }

    // Kiểm tra full scope trả đủ task, deliverable và loại trùng contract trong dữ liệu phase.
    @Test
    void getPhaseById_fullScopeActions_mapsTasksDeliverablesAndDeduplicatesContracts() {
        Projects project = project();
        Timeline phase = phase(project, PhaseStatus.IN_PROGRESS);
        ProjectAccessResponse access = access(false);
        Users assignee = user("Lan", "Nguyen", "lan@g89.vn");
        TimelineTask task = task(phase, assignee);
        Deliverable deliverable = deliverable(phase);
        Contracts directContract = contract("C-001");
        Contracts taskContract = contract("C-002");
        TimelineContract directLink = new TimelineContract();
        directLink.setTimeline(phase);
        directLink.setContract(directContract);
        directLink.setLinkedAt(LocalDateTime.of(2026, 8, 11, 9, 0));
        when(phaseRepository.findDetailById(phase.getId()))
                .thenReturn(Optional.of(phase));
        when(permissionAccessService.getCurrentUserAccess(project.getId()))
                .thenReturn(access);
        when(permissionAccessService.hasAction(access, "VIEW_TASKS")).thenReturn(true);
        when(permissionAccessService.hasAction(access, "EDIT_TASKS")).thenReturn(false);
        when(permissionAccessService.hasAction(access, "VIEW_DELIVERABLES"))
                .thenReturn(true);
        when(permissionAccessService.hasAction(access, "VIEW_CONTRACTS"))
                .thenReturn(true);
        when(permissionAccessService.hasFullWorkScope(access, "VIEW_TASKS"))
                .thenReturn(true);
        when(phaseTaskRepository.findByPhaseId(phase.getId())).thenReturn(List.of(task));
        when(phaseDeliverableRepository.findByPhaseId(phase.getId()))
                .thenReturn(List.of(deliverable));
        when(phaseContractRepository.findByPhaseId(phase.getId()))
                .thenReturn(List.of(directLink));
        when(phaseContractRepository.findByTaskPhaseId(phase.getId()))
                .thenReturn(List.of(directContract, taskContract));

        PhaseDetailResponse result = phaseService.getPhaseById(phase.getId());

        assertThat(result.getTasks()).singleElement().satisfies(response -> {
            assertThat(response.getId()).isEqualTo(task.getId());
            assertThat(response.getAssignedToName()).isEqualTo("Lan Nguyen");
        });
        assertThat(result.getDeliverables()).singleElement().satisfies(response ->
                assertThat(response.getId()).isEqualTo(deliverable.getId())
        );
        assertThat(result.getContracts())
                .extracting(com.fpt.backend.dto.response.phase.PhaseContractResponse::getId)
                .containsExactly(directContract.getId(), taskContract.getId());
        assertThat(result.getContracts().getFirst().getLinkedAt())
                .isEqualTo(directLink.getLinkedAt());
    }

    private static Projects project() {
        Projects project = new Projects();
        project.setId(UUID.randomUUID());
        project.setProjectCode("PRJ-2026-Winter Collection");
        project.setProjectName("Winter Collection");
        project.setProjectStatus("Active");
        return project;
    }

    private static Timeline phase(Projects project, PhaseStatus status) {
        Timeline phase = new Timeline();
        phase.setId(UUID.randomUUID());
        phase.setProject(project);
        phase.setTitle("Planning");
        phase.setDescription("Plan collection");
        phase.setStartDate(java.sql.Date.valueOf(LocalDate.of(2026, 8, 10)));
        phase.setEndDate(java.sql.Date.valueOf(LocalDate.of(2026, 8, 31)));
        phase.setStatus(status);
        phase.setProgress(25D);
        return phase;
    }

    private static ProjectAccessResponse access(boolean executive) {
        return ProjectAccessResponse.builder()
                .projectId(UUID.randomUUID())
                .currentUserId(UUID.randomUUID())
                .isExecutiveViewer(executive)
                .allowedActions(List.of())
                .fullScopeActions(List.of())
                .workScope("OWN")
                .build();
    }

    private static Users user(String first, String last, String email) {
        Users user = new Users();
        user.setId(UUID.randomUUID());
        user.setFirstName(first);
        user.setLastName(last);
        user.setEmail(email);
        return user;
    }

    private static TimelineTask task(Timeline phase, Users assignee) {
        TimelineTask task = new TimelineTask();
        task.setId(UUID.randomUUID());
        task.setTimeline(phase);
        task.setAssignedTo(assignee);
        task.setTitle("Prepare samples");
        task.setStatus("IN_PROGRESS");
        task.setStartDate(java.sql.Date.valueOf(LocalDate.of(2026, 8, 11)));
        task.setEndDate(java.sql.Date.valueOf(LocalDate.of(2026, 8, 20)));
        return task;
    }

    private static Deliverable deliverable(Timeline phase) {
        Deliverable deliverable = new Deliverable();
        deliverable.setId(UUID.randomUUID());
        deliverable.setTimeline(phase);
        deliverable.setTitle("Sample set");
        deliverable.setDueDate(java.sql.Date.valueOf(LocalDate.of(2026, 8, 25)));
        deliverable.setStatus("DRAFT");
        return deliverable;
    }

    private static Contracts contract(String number) {
        Contracts contract = new Contracts();
        contract.setId(UUID.randomUUID());
        contract.setContractNumber(number);
        contract.setContractTitle("Supply " + number);
        contract.setContractStatus("NEW");
        contract.setContractCreatedAt(LocalDateTime.of(2026, 8, 12, 9, 0));
        return contract;
    }
}

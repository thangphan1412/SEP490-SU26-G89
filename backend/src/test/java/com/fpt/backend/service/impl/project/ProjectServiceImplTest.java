package com.fpt.backend.service.impl.project;

import com.fpt.backend.dto.request.project.ProjectCreateRequest;
import com.fpt.backend.dto.request.project.ProjectPhaseRequest;
import com.fpt.backend.dto.request.project.ProjectUpdateRequest;
import com.fpt.backend.dto.response.project.ProjectAccessResponse;
import com.fpt.backend.dto.response.project.ProjectDetailResponse;
import com.fpt.backend.entity.Projects;
import com.fpt.backend.entity.Users;
import com.fpt.backend.exception.BadHttpException;
import com.fpt.backend.repository.phase.PhaseContractRepository;
import com.fpt.backend.repository.phase.PhaseDeliverableRepository;
import com.fpt.backend.repository.phase.PhaseRepository;
import com.fpt.backend.repository.phase.PhaseTaskRepository;
import com.fpt.backend.service.impl.phase.PhaseStatusService;
import com.fpt.backend.repository.project.ProjectCleanupRepository;
import com.fpt.backend.repository.project.ProjectContractRepository;
import com.fpt.backend.repository.project.ProjectMemberRepository;
import com.fpt.backend.repository.project.ProjectRepository;
import com.fpt.backend.repository.user.UserRepository;
import com.fpt.backend.service.interfaces.permission.IPermissionAccessService;
import com.fpt.backend.util.CurrentUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectServiceImplTest {

    private static final ZoneId PROJECT_TIME_ZONE =
            ZoneId.of("Asia/Ho_Chi_Minh");

    @Mock
    private ProjectRepository projectRepository;
    @Mock
    private ProjectContractRepository projectContractRepository;
    @Mock
    private ProjectMemberRepository projectMemberRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ProjectCleanupRepository projectCleanupRepository;
    @Mock
    private ProjectPhaseService projectPhaseService;
    @Mock
    private ProjectMemberService projectMemberService;
    @Mock
    private ProjectPermissionService projectPermissionService;
    @Mock
    private ProjectApprovalService projectApprovalService;
    @Mock
    private IPermissionAccessService permissionAccessService;
    @Mock
    private CurrentUser currentUserUtil;

    private ProjectServiceImpl projectService;

    @BeforeEach
    void setUp() {
        projectService = createService(projectPhaseService);
    }

    private ProjectServiceImpl createService(ProjectPhaseService phaseService) {
        ProjectStatusService projectStatusService =
                new ProjectStatusService(projectRepository);
        return new ProjectServiceImpl(
                projectRepository,
                projectContractRepository,
                projectMemberRepository,
                userRepository,
                projectCleanupRepository,
                phaseService,
                projectMemberService,
                projectPermissionService,
                projectApprovalService,
                projectStatusService,
                permissionAccessService,
                currentUserUtil
        );
    }

    /**
     * Regression test cho trường hợp approve Project rồi đổi start date về hôm qua.
     * Input: Project đang Planning, startDate cũ là ngày mai; request đổi thành hôm qua.
     * Expected: update trả startDate mới và chuyển status ngay thành Active.
     */
    @Test
    void updateProject_whenStartDateMovesToYesterdayActivatesPlanningProject() {
        LocalDate today = LocalDate.now(PROJECT_TIME_ZONE);
        LocalDate yesterday = today.minusDays(1);
        LocalDate endDate = today.plusDays(30);
        UUID projectId = UUID.fromString(
                "00000000-0000-0000-0000-000000000101"
        );
        Users creator = new Users();
        creator.setFirstName("Project");
        creator.setLastName("Owner");

        Projects project = new Projects();
        project.setId(projectId);
        project.setProjectCode("PRJ-2026-Status Test");
        project.setProjectName("Status Test Project");
        project.setProjectDescription("Validate status after changing dates");
        project.setProjectStatus("Planning");
        project.setProjectStartDate(today.plusDays(1));
        project.setProjectEndDate(endDate);
        project.setProjectCreatedBy(creator);
        project.setProjectCreatedAt(today.toString());

        ProjectUpdateRequest request = new ProjectUpdateRequest(
                project.getProjectName(),
                project.getProjectCode(),
                yesterday,
                endDate,
                project.getProjectDescription(),
                List.of(),
                null
        );
        ProjectAccessResponse access = new ProjectAccessResponse(
                projectId,
                UUID.fromString("00000000-0000-0000-0000-000000000102"),
                true,
                true,
                false,
                List.of("EDIT_PROJECT"),
                List.of("EDIT_PROJECT"),
                "FULL"
        );

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(permissionAccessService.getCurrentUserAccess(projectId)).thenReturn(access);
        when(projectRepository.existsByProjectCodeIgnoreCaseAndIdNot(
                project.getProjectCode(),
                projectId
        )).thenReturn(false);
        when(projectPhaseService.getProjectPhases(projectId)).thenReturn(List.of());

        ProjectDetailResponse result = projectService.updateProject(projectId, request);

        assertThat(project.getProjectStartDate()).isEqualTo(yesterday);
        assertThat(project.getProjectStatus()).isEqualTo("Active");
        assertThat(result.projectStartDate()).isEqualTo(yesterday);
        assertThat(result.projectStatus()).isEqualTo("Active");
        verify(projectRepository).save(project);
        verify(projectPhaseService).syncPhases(project, List.of());
        verify(projectRepository).flush();
    }

    @Test
    void createProject_rejectsOverlappingPhasesThroughTheRealPhaseValidator() {
        projectService = createService(realPhaseService());
        Projects schedule = scheduleProject();
        Users creator = new Users();
        creator.setId(UUID.randomUUID());
        when(currentUserUtil.getCurrentUser()).thenReturn(creator);
        when(projectRepository.save(any(Projects.class))).thenAnswer(invocation -> {
            Projects saved = invocation.getArgument(0);
            saved.setId(schedule.getId());
            return saved;
        });
        ProjectCreateRequest request = new ProjectCreateRequest(
                "Phase validation", "PRJ-2099-Phase Validation",
                schedule.getProjectStartDate(), schedule.getProjectEndDate(),
                "", overlappingPhases(false), List.of()
        );

        assertThatThrownBy(() -> projectService.createProject(request))
                .isInstanceOf(BadHttpException.class)
                .hasMessage("Phase 2 start date must be after phase 1 end date");

        verify(projectRepository, never()).flush();
        verifyNoInteractions(projectMemberService);
    }

    @Test
    void updateProject_rejectsOverlappingPhasesThroughTheRealPhaseValidator() {
        projectService = createService(realPhaseService());
        Projects project = scheduleProject();
        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));
        ProjectUpdateRequest request = new ProjectUpdateRequest(
                null, null, null, null, null, overlappingPhases(true), null
        );

        assertThatThrownBy(() -> projectService.updateProject(project.getId(), request))
                .isInstanceOf(BadHttpException.class)
                .hasMessage("Phase 2 start date must be after phase 1 end date");

        verify(permissionAccessService).requireAction(project.getId(), "EDIT_PROJECT");
        verify(projectRepository, never()).flush();
    }

    private ProjectPhaseService realPhaseService() {
        return new ProjectPhaseService(
                mock(PhaseRepository.class), mock(PhaseTaskRepository.class),
                mock(PhaseDeliverableRepository.class), mock(PhaseContractRepository.class),
                mock(PhaseStatusService.class)
        );
    }

    private Projects scheduleProject() {
        Projects project = new Projects();
        project.setId(UUID.randomUUID());
        project.setProjectStatus("Planning");
        project.setProjectStartDate(LocalDate.of(2099, 10, 1));
        project.setProjectEndDate(LocalDate.of(2099, 10, 31));
        return project;
    }

    private List<ProjectPhaseRequest> overlappingPhases(boolean existing) {
        return List.of(
                new ProjectPhaseRequest(
                        existing ? UUID.randomUUID() : null, "Phase 1", "",
                        LocalDate.of(2099, 10, 1), LocalDate.of(2099, 10, 10)
                ),
                new ProjectPhaseRequest(
                        existing ? UUID.randomUUID() : null, "Phase 2", "",
                        LocalDate.of(2099, 10, 5), LocalDate.of(2099, 10, 20)
                )
        );
    }
}

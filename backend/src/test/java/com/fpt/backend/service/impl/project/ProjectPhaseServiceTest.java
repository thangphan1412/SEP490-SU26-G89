package com.fpt.backend.service.impl.project;

import com.fpt.backend.dto.request.project.ProjectPhaseRequest;
import com.fpt.backend.entity.Projects;
import com.fpt.backend.entity.Timeline;
import com.fpt.backend.exception.BadHttpException;
import com.fpt.backend.repository.phase.PhaseContractRepository;
import com.fpt.backend.repository.phase.PhaseDeliverableRepository;
import com.fpt.backend.repository.phase.PhaseRepository;
import com.fpt.backend.repository.phase.PhaseTaskRepository;
import com.fpt.backend.service.impl.phase.PhaseStatusService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectPhaseServiceTest {
    private static final LocalDate PROJECT_START = LocalDate.of(2026, 10, 1);
    private static final LocalDate PROJECT_END = LocalDate.of(2026, 10, 31);

    @Mock private PhaseRepository phaseRepository;
    @Mock private PhaseTaskRepository phaseTaskRepository;
    @Mock private PhaseDeliverableRepository phaseDeliverableRepository;
    @Mock private PhaseContractRepository phaseContractRepository;
    @Mock private PhaseStatusService phaseStatusService;

    private ProjectPhaseService service;
    private Projects project;

    @BeforeEach
    void setUp() {
        service = new ProjectPhaseService(
                phaseRepository, phaseTaskRepository,
                phaseDeliverableRepository, phaseContractRepository,
                phaseStatusService
        );
        project = new Projects();
        project.setId(UUID.randomUUID());
        project.setProjectStartDate(PROJECT_START);
        project.setProjectEndDate(PROJECT_END);
    }

    @ParameterizedTest
    @CsvSource({"5,false", "10,false", "5,true", "10,true"})
    void syncPhases_rejectsOverlapAndSharedBoundaryForNewAndExistingPhases(
            int secondStartDay, boolean existing) {
        List<ProjectPhaseRequest> phases = List.of(
                phase(existing ? UUID.randomUUID() : null, 1, 10),
                phase(existing ? UUID.randomUUID() : null, secondStartDay, 20)
        );

        assertThatThrownBy(() -> service.syncPhases(project, phases))
                .isInstanceOf(BadHttpException.class)
                .hasMessage("Phase 2 start date must be after phase 1 end date");

        verifyNoPhaseChanges();
    }

    @Test
    void syncPhases_rejectsReverseChronologicalOrder() {
        assertThatThrownBy(() -> service.syncPhases(project, List.of(
                phase(null, 12, 15), phase(null, 1, 10)
        )))
                .isInstanceOf(BadHttpException.class)
                .hasMessage("Phase 2 start date must be after phase 1 end date");
        verifyNoPhaseChanges();
    }

    @Test
    void syncPhases_validatesEveryPhaseBoundaryBeforeSaving() {
        assertThatThrownBy(() -> service.syncPhases(project, List.of(
                phase(null, 1, 10), phase(null, 11, 20), phase(null, 19, 30)
        )))
                .isInstanceOf(BadHttpException.class)
                .hasMessage("Phase 3 start date must be after phase 2 end date");
        verifyNoPhaseChanges();
    }

    @ParameterizedTest
    @ValueSource(ints = {11, 15})
    void syncPhases_acceptsOrderedNonOverlappingPhases(int secondStartDay) {
        when(phaseRepository.findByProjectId(project.getId())).thenReturn(List.of());

        service.syncPhases(project, List.of(
                phase(null, 1, 10), phase(null, secondStartDay, 20)
        ));

        ArgumentCaptor<Timeline> savedPhases = ArgumentCaptor.forClass(Timeline.class);
        verify(phaseRepository, times(2)).save(savedPhases.capture());
        assertThat(savedPhases.getAllValues())
                .extracting(value -> ((java.sql.Date) value.getStartDate()).toLocalDate())
                .containsExactly(PROJECT_START, PROJECT_START.withDayOfMonth(secondStartDay));
        verify(phaseRepository).flush();
        verify(phaseStatusService).refreshProjectStatuses(project.getId());
    }

    @ParameterizedTest
    @CsvSource({
            "2026-10-11,2026-10-10,start date must not be after its end date",
            "2026-09-30,2026-10-10,start date must not be before the project start date",
            "2026-10-01,2026-11-01,end date must not be after the project end date"
    })
    void syncPhases_preservesIndividualPhaseDateRules(
            String start, String end, String message) {
        ProjectPhaseRequest invalid = new ProjectPhaseRequest(
                null, "Phase", "", LocalDate.parse(start), LocalDate.parse(end)
        );

        assertThatThrownBy(() -> service.syncPhases(project, List.of(invalid)))
                .isInstanceOf(BadHttpException.class)
                .hasMessage("Phase 1 " + message);
        verifyNoPhaseChanges();
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void syncPhases_rejectsMissingDates(boolean missingStart) {
        ProjectPhaseRequest invalid = new ProjectPhaseRequest(
                null, "Phase", "",
                missingStart ? null : PROJECT_START,
                missingStart ? PROJECT_END : null
        );

        assertThatThrownBy(() -> service.syncPhases(project, List.of(invalid)))
                .isInstanceOf(BadHttpException.class)
                .hasMessage("Phase 1 start date and end date are required");
        verifyNoPhaseChanges();
    }

    @Test
    void syncPhases_keepsPhasesOptional() {
        when(phaseRepository.findByProjectId(project.getId())).thenReturn(List.of());

        service.syncPhases(project, List.of());

        verify(phaseRepository, never()).save(any(Timeline.class));
        verify(phaseRepository).flush();
    }

    private ProjectPhaseRequest phase(UUID id, int startDay, int endDay) {
        return new ProjectPhaseRequest(
                id, "Phase", "",
                PROJECT_START.withDayOfMonth(startDay),
                PROJECT_START.withDayOfMonth(endDay)
        );
    }

    private void verifyNoPhaseChanges() {
        verifyNoInteractions(
                phaseRepository, phaseTaskRepository,
                phaseDeliverableRepository, phaseContractRepository,
                phaseStatusService
        );
    }
}

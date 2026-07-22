package com.fpt.backend.service.interfaces.phase;

import com.fpt.backend.dto.response.phase.PhaseDetailResponse;
import com.fpt.backend.dto.response.phase.PhaseListItemResponse;

import java.util.List;

public interface PhaseService {
    List<PhaseListItemResponse> getPhasesByProjectId(int projectId);

    PhaseDetailResponse getPhaseById(int phaseId);
}

package com.fpt.backend.service.interfaces.phase;

import com.fpt.backend.dto.response.phase.PhaseDetailResponse;
import com.fpt.backend.dto.response.phase.PhaseListItemResponse;

import java.util.List;
import java.util.UUID;

public interface IPhaseService {
    List<PhaseListItemResponse> getPhasesByProjectId(UUID projectId);

    PhaseDetailResponse getPhaseById(UUID phaseId);
}

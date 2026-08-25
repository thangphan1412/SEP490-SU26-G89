package com.fpt.backend.service.interfaces.phase;

import com.fpt.backend.dto.response.phase.PhaseDetailResponse;
import com.fpt.backend.dto.response.phase.PhaseListItemResponse;

import java.util.List;
import java.util.UUID;

public interface IPhaseService {
    // Lấy danh sách phase thuộc một dự án.
    List<PhaseListItemResponse> getPhasesByProjectId(UUID projectId);

    // Lấy thông tin chi tiết của một phase.
    PhaseDetailResponse getPhaseById(UUID phaseId);
}

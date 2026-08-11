package com.fpt.backend.dto.response.contract;

import java.util.List;
import java.util.UUID;

public record ContractProjectContextResponse(
        UUID projectId,
        List<ContractPhaseOptionResponse> phases,
        List<ContractProjectMemberOptionResponse> members
) {
}

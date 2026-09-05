package com.fpt.backend.dto.response.contract;

import java.util.List;

public record ContractStandaloneContextResponse(
        List<ContractProjectMemberOptionResponse> members
) {
}

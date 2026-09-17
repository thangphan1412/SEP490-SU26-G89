package com.fpt.backend.dto.response.project;

public record ProjectApprovalAccessResponse(
        boolean canApprove,
        boolean waitingForDepartmentApproval
) {
}

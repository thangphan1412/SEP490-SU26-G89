package com.fpt.backend.dto.request.contract;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.List;
import java.util.UUID;

public record ContractRequest(
        UUID projectId,
        UUID phaseId,
        UUID taskId,
        UUID contractTypeId,
        UUID contractTemplateId,
        UUID contractTemplateVersionId,
        String contractNumber,
        String contractTitle,
        String contractStatus,
        LocalDate effectiveDate,
        LocalDate expirationDate,
        String contractCreatedBy,
        LocalDateTime contractCreatedAt,
        String contractContent,
        String contractLayoutJson,
        Boolean saveAsTemplateVersion,
        String templateVersionName,
        String templateVersionNote,
        UUID previousContractId,
        String actorName,
        String actorRole,
        Map<String, String> attributeValues,
        List<ContractWorkflowAssigneeRequest> workflowAssignees
) {
}

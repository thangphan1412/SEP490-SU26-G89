package com.fpt.backend.dto.response.contract;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record ContractResponse(
        UUID id,
        UUID projectId,
        String projectName,
        UUID contractTypeId,
        String contractTypeCode,
        String contractTypeName,
        UUID contractTemplateId,
        String contractTemplateName,
        UUID contractTemplateVersionId,
        Integer contractTemplateVersionNumber,
        String contractTemplateVersionName,
        String contractNumber,
        String contractTitle,
        String contractStatus,
        LocalDate effectiveDate,
        LocalDate expirationDate,
        String contractCreatedBy,
        LocalDateTime contractCreatedAt,
        String contractContent,
        String contractLayoutJson,
        LocalDateTime contractStatusUpdatedAt,
        LocalDateTime contractEndedAt,
        String contractCancellationReason,
        UUID previousContractId,
        String previousContractNumber,
        List<ContractStatusHistoryResponse> statusHistory
) {
}

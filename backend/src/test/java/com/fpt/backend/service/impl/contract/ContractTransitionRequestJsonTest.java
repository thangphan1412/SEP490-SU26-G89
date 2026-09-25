package com.fpt.backend.service.impl.contract;

import com.fpt.backend.dto.request.contract.ContractTransitionRequest;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class ContractTransitionRequestJsonTest {
    private final JsonMapper mapper = JsonMapper.builder().build();

    @Test
    void readsSigningDeadlineForExtendAction() {
        ContractTransitionRequest request = mapper.readValue("""
                {"action":"EXTEND_SIGNING_DEADLINE","comment":"reason","signingDeadline":"2026-10-10"}
                """, ContractTransitionRequest.class);

        assertThat(request.action()).isEqualTo("EXTEND_SIGNING_DEADLINE");
        assertThat(request.signingDeadline()).isEqualTo(LocalDate.of(2026, 10, 10));
    }

    @Test
    void oldPayloadWithoutSigningDeadlineStillWorks() {
        ContractTransitionRequest request = mapper.readValue("""
                {"action":"SETTLE","actorName":"A","actorRole":"CEO","comment":"note"}
                """, ContractTransitionRequest.class);

        assertThat(request.action()).isEqualTo("SETTLE");
        assertThat(request.signingDeadline()).isNull();
    }
}

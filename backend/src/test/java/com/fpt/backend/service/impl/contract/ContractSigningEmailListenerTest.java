package com.fpt.backend.service.impl.contract;

import com.fpt.backend.mail.EmailService;
import com.fpt.backend.mail.MessageInfor;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class ContractSigningEmailListenerTest {

    @Test
    void ceoSigned_sendsConfirmationAndPartnerRequestWithPublicKey() {
        EmailService emailService = mock(EmailService.class);
        ContractSigningEmailListener listener =
                new ContractSigningEmailListener(emailService);

        listener.handle(event(ContractSigningEmailEvent.Type.CEO_SIGNED));

        ArgumentCaptor<MessageInfor> messages =
                ArgumentCaptor.forClass(MessageInfor.class);
        verify(emailService, times(2)).sendEmail(messages.capture());
        List<MessageInfor> values = messages.getAllValues();
        assertThat(values).extracting(MessageInfor::getEmail)
                .containsExactly("ceo@example.com", "partner@example.com");
        assertThat(values.get(1).getText()).contains("PUBLIC_KEY_VALUE");
    }

    @Test
    void partnerSigned_sendsCompletionToCeo() {
        EmailService emailService = mock(EmailService.class);
        ContractSigningEmailListener listener =
                new ContractSigningEmailListener(emailService);

        listener.handle(event(ContractSigningEmailEvent.Type.PARTNER_SIGNED));

        ArgumentCaptor<MessageInfor> message =
                ArgumentCaptor.forClass(MessageInfor.class);
        verify(emailService).sendEmail(message.capture());
        assertThat(message.getValue().getEmail()).isEqualTo("ceo@example.com");
        assertThat(message.getValue().getText()).contains("Partner User has completed");
    }

    private ContractSigningEmailEvent event(ContractSigningEmailEvent.Type type) {
        boolean partnerSigned = type == ContractSigningEmailEvent.Type.PARTNER_SIGNED;
        return new ContractSigningEmailEvent(
                type,
                "CTR-001",
                "Supply agreement",
                partnerSigned ? "Partner User" : "CEO User",
                partnerSigned ? "partner@example.com" : "ceo@example.com",
                partnerSigned ? "CEO User" : "Partner User",
                partnerSigned ? "ceo@example.com" : "partner@example.com",
                "PUBLIC_KEY_VALUE",
                LocalDateTime.of(2026, 8, 15, 12, 0)
        );
    }
}

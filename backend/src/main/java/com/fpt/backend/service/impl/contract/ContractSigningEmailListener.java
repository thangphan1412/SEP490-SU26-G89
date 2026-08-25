package com.fpt.backend.service.impl.contract;

import com.fpt.backend.mail.EmailService;
import com.fpt.backend.mail.MessageInfor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class ContractSigningEmailListener {

    private final EmailService emailService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(ContractSigningEmailEvent event) {
        if (event.type() == ContractSigningEmailEvent.Type.CEO_SIGNED) {
            sendCeoConfirmation(event);
            sendPartnerRequest(event);
            return;
        }
        sendPartnerCompletionToCeo(event);
    }

    private void sendCeoConfirmation(ContractSigningEmailEvent event) {
        send(
                event.signerEmail(),
                "Contract signed successfully - " + event.contractNumber(),
                "Dear " + event.signerName() + ",\n\n"
                        + "Your electronic signature has been recorded successfully.\n\n"
                        + contractInformation(event)
                        + "Signed at: " + event.signedAt() + "\n\n"
                        + "The contract has now been sent to " + event.recipientName()
                        + " for the next signature."
        );
    }

    private void sendPartnerRequest(ContractSigningEmailEvent event) {
        send(
                event.recipientEmail(),
                "Contract signature requested - " + event.contractNumber(),
                "Dear " + event.recipientName() + ",\n\n"
                        + event.signerName() + " has signed the following contract. "
                        + "Please review the contract information and complete your signature.\n\n"
                        + contractInformation(event)
                        + "CEO signer: " + event.signerName() + " (" + event.signerEmail() + ")\n"
                        + "CEO public key:\n" + event.publicKey() + "\n\n"
                        + "Keep this public key with the contract information so the CEO's "
                        + "digital signature can be verified."
        );
    }

    private void sendPartnerCompletionToCeo(ContractSigningEmailEvent event) {
        send(
                event.recipientEmail(),
                "Partner signed contract - " + event.contractNumber(),
                "Dear " + event.recipientName() + ",\n\n"
                        + event.signerName() + " has completed the partner signature.\n\n"
                        + contractInformation(event)
                        + "Partner email: " + event.signerEmail() + "\n"
                        + "Signed at: " + event.signedAt() + "\n\n"
                        + "The contract signing workflow is complete."
        );
    }

    private String contractInformation(ContractSigningEmailEvent event) {
        return "Contract number: " + event.contractNumber() + "\n"
                + "Contract title: " + event.contractTitle() + "\n";
    }

    private void send(String email, String subject, String body) {
        if (email == null || email.isBlank()) {
            log.warn("Contract signing email skipped because the recipient email is empty");
            return;
        }
        try {
            emailService.sendEmail(new MessageInfor(email, subject, body));
        } catch (RuntimeException exception) {
            log.error("Unable to send contract signing email to {}", email, exception);
        }
    }
}

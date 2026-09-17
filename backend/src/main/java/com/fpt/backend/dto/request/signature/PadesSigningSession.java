package com.fpt.backend.dto.request.signature;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PadesSigningSession {
    private UUID sessionId;
    private UUID contractId;
    private UUID userId;
    private UUID electronicSignatureId;
    private String keyCode;
    private byte[] originalPdf;
    private byte[] preparedPdf;
    private byte[] contentToSign;
    private String documentHash;
    private int signatureOffset;
    private int signatureCapacity;
    private int pageNumber;

    private float positionX;

    private float positionY;

    private float signatureWidth;

    private float signatureHeight;
}
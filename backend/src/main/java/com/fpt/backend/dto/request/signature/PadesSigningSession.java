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

    /**
     * PDF sau khi PDFBox tạo signature placeholder.
     */
    private byte[] preparedPdf;

    /**
     * ByteRange content mà frontend sẽ ký.
     */
    private byte[] contentToSign;

    /**
     * SHA-256 của contentToSign.
     */
    private String documentHash;

    /**
     * Vị trí bắt đầu ghi CMS vào /Contents.
     */
    private int signatureOffset;

    /**
     * Số byte tối đa mà placeholder dành cho CMS.
     */
    private int signatureCapacity;
}
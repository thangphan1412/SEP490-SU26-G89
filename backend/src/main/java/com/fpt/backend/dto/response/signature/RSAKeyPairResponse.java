package com.fpt.backend.dto.response.signature;

import lombok.*;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RSAKeyPairResponse {
    private String publicKey;
    private String privateKey;
    private String algorithm;
    private Integer keySize;
}

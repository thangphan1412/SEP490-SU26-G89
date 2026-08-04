package com.fpt.backend.service.interfaces.signature;

import com.fpt.backend.dto.response.signature.SignatureListResponse;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ISignature {
    List<SignatureListResponse> findAll();
}

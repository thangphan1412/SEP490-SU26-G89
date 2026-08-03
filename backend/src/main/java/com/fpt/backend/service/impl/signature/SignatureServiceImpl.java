package com.fpt.backend.service.impl.signature;

import com.fpt.backend.dto.response.signature.SignatureListResponse;
import com.fpt.backend.entity.Users;
import com.fpt.backend.repository.signature.SignatureRepository;
import com.fpt.backend.service.interfaces.signature.ISignature;
import com.fpt.backend.util.CurrentUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class SignatureServiceImpl  implements ISignature {
    @Autowired
    private SignatureRepository signatureRepository;
    @Autowired
    private CurrentUser currentUser;
    @Override
    public List<SignatureListResponse> findAll() {
        Users users = currentUser.getCurrentUser();
        UUID userId = users.getId();

        return signatureRepository.findAll(userId);
    }
}

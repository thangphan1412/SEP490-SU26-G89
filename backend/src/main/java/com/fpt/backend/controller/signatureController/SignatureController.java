package com.fpt.backend.controller.signatureController;

import com.fpt.backend.dto.response.signature.SignatureListResponse;
import com.fpt.backend.service.impl.signature.SignatureServiceImpl;
import com.fpt.backend.util.BaseResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/signature")
@RequiredArgsConstructor
public class SignatureController {

    @Autowired
    private SignatureServiceImpl signatureService;
    @GetMapping("/list/signature")
    public ResponseEntity<BaseResponse<List<SignatureListResponse>>> getAllSignature() {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(new BaseResponse<>(signatureService.findAll()));
    }
}

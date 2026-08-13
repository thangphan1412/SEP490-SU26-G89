//package com.fpt.backend.controller.signatureController;
//
//import com.fpt.backend.dto.response.signature.SignatureListResponse;
//import com.fpt.backend.service.impl.signature.DigitalSignatureService;
//import com.fpt.backend.service.impl.signature.SignatureServiceImpl;
//import com.fpt.backend.util.BaseResponse;
//import com.fpt.backend.util.CurrentUser;
//import lombok.RequiredArgsConstructor;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.CacheControl;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.util.List;
//import java.util.UUID;
//
//@RestController
//@RequestMapping("/api/signature")
//@RequiredArgsConstructor
//public class SignatureController {
//
//    @Autowired
//    private CurrentUser currentUser;
//    @Autowired
//    private DigitalSignatureService digitalSignatureService;
//    @PostMapping("/contracts/{contractId}/sign")
//    public ResponseEntity<?> signContract(@PathVariable UUID contractId, @RequestParam("file") MultipartFile file) throws Exception {
//
//        UUID userId = currentUser.getCurrentUser().getId();
//
//        String signature = digitalSignatureService.sign(
//                file.getBytes(),
//                userId
//        );
//
//        return ResponseEntity.ok(signature);
//    }
//}

package com.fpt.backend.service.impl.electronicSignature;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.fpt.backend.dto.request.electronicSignature.CreateElectronicSignatureRequest;
import com.fpt.backend.dto.request.fileStorage.CreateFileStorageRequest;
import com.fpt.backend.entity.ElectronicSignatures;
import com.fpt.backend.entity.FileStorage;
import com.fpt.backend.entity.UserKeys;
import com.fpt.backend.entity.Users;
import com.fpt.backend.exception.BadHttpException;
import com.fpt.backend.repository.FileStorageRepository;
import com.fpt.backend.repository.electronicSignature.ElectronicSignatureRepository;
import com.fpt.backend.service.interfaces.electronicSignature.IElectronicSignatureService;
import com.fpt.backend.util.CurrentUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@Service
public class ElectronicSignatureServiceImpl implements IElectronicSignatureService {
    @Autowired
    private ElectronicSignatureRepository  electronicSignatureRepository;
    @Autowired
    private CurrentUser  currentUser;
    @Autowired
    private Cloudinary    cloudinary;
    @Autowired
    private FileStorageRepository fileStorageRepository;
    @Override
    public ElectronicSignatures createElectronicSignature(CreateElectronicSignatureRequest createElectronicSignatureRequest) {
        Users users = currentUser.getCurrentUser();
        MultipartFile img = createElectronicSignatureRequest.getCreateFileStorageRequests().getMultipartFile();
        

        return null;
    }
    
}

package com.fpt.backend.util;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Calculate256 {
    public String calculate256(MultipartFile multipartFile) throws NoSuchAlgorithmException, IOException {
        MessageDigest messageDigest = MessageDigest.getInstance("HS-256");
        byte[] hash = messageDigest.digest(multipartFile.getBytes());
        StringBuilder sb = new StringBuilder();
        for(byte b :hash){
            sb.append(String.format("%02x",b));
        }
        return sb.toString();
    }
}

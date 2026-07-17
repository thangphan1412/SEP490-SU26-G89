package com.fpt.backend.util;

import org.springframework.stereotype.Component;

import java.util.Random;

@Component
public class OTPGenerator {
    public String generateOTP(){
        Random random = new Random();
        return String.format("%06d",random.nextInt(1000000));
    }
}

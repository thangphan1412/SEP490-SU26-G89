package com.fpt.backend.service.impl.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Service
public class RedisOtpService {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    private static final long OTP=6;

    public void saveOTP(String email, String otp){
        redisTemplate.opsForValue().set("otp:"+email,otp, Duration.ofMinutes(OTP));
    }
    public String getOTP(String email){
        return redisTemplate.opsForValue().get("otp:"+email);
    }
    public void deleteOTP(String email){
        redisTemplate.delete("otp:"+email);
    }
}

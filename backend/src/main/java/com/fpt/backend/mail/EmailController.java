package com.fpt.backend.mail;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
@RestController
public class EmailController {
    @Autowired
    private EmailService emailService;
    @PostMapping("/testSendGmail")
    public String testSendGmail(@RequestBody MessageInfor messageInfor){
        emailService.sendEmail(messageInfor);
        return "success";
    }
}

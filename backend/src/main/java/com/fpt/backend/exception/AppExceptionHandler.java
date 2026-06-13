package com.fpt.backend.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;

@RestControllerAdvice
public class AppExceptionHandler {
    @ExceptionHandler(AppException.class)
    @ResponseBody
    public ResponseEntity<?> handleException(AppException ex) {
        HashMap<String, Object> response = new HashMap<>();
        response.put("message", ex.getMessage());
        response.put("status", 400);
        if (ex.getData() != null) {
            response.put("data", ex.getData());
        }
        return ResponseEntity.badRequest().body(response);
    }
}

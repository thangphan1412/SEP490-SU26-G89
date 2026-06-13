package com.fpt.backend.exception;


import com.fpt.backend.util.BaseResponse;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalException {
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<BaseResponse<?>> handleNotFoundException(NotFoundException ex){
        BaseResponse<?> response = new BaseResponse<>();
        response.setStatus(HttpStatus.NOT_FOUND.value());
        response.setMessage(ex.getMessage());
        response.setData(null);
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(BadHttpException.class)
    public ResponseEntity<BaseResponse<?>> handleBadException(BadHttpException ex){
        BaseResponse<?> response = new BaseResponse<>();
        response.setStatus(HttpStatus.BAD_REQUEST.value());
        response.setMessage(ex.getMessage());
        response.setData(null);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }
}

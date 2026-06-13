package com.fpt.backend.exception;

public class BadHttpException extends RuntimeException{
    public BadHttpException(String message){
        super(message);
    }
}

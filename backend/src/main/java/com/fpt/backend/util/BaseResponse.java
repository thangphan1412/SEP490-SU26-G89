package com.fpt.backend.util;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.http.HttpStatus;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class BaseResponse<T> {
    private int status;
    private String message;
    private T data;

    public BaseResponse(T data){
        this.status = HttpStatus.OK.value();
        this.message = HttpStatus.OK.getReasonPhrase();
        this.data = data;
    }
}

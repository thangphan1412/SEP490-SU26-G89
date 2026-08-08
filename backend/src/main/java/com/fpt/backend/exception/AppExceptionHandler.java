package com.fpt.backend.exception;

import com.fpt.backend.util.BaseResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

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

    // BẮT LỖI @Valid
    @ExceptionHandler(org.springframework.web.bind.MethodArgumentNotValidException.class)
    public ResponseEntity<BaseResponse<Map<String, String>>> handleValidationExceptions(org.springframework.web.bind.MethodArgumentNotValidException ex) {

        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((org.springframework.validation.FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        BaseResponse<Map<String, String>> response = new BaseResponse<>(
                org.springframework.http.HttpStatus.BAD_REQUEST.value(),
                "Dữ liệu đầu vào không hợp lệ",
                errors
        );

        return ResponseEntity.status(org.springframework.http.HttpStatus.BAD_REQUEST).body(response);
    }

    // Chặn những data cố tình "lách" Enum
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<BaseResponse<String>> handleIllegalArgumentException(IllegalArgumentException ex) {
        BaseResponse<String> response = new BaseResponse<>(
                org.springframework.http.HttpStatus.BAD_REQUEST.value(),
                "Dữ liệu Enum không hợp lệ: " + ex.getMessage(),
                null
        );
        return ResponseEntity.status(org.springframework.http.HttpStatus.BAD_REQUEST).body(response);
    }


    @ExceptionHandler(org.springframework.http.converter.HttpMessageNotReadableException.class)
    public ResponseEntity<BaseResponse<String>> handleHttpMessageNotReadable(org.springframework.http.converter.HttpMessageNotReadableException ex) {
        BaseResponse<String> response = new BaseResponse<>(
                org.springframework.http.HttpStatus.BAD_REQUEST.value(),
                "Dữ liệu gửi lên không đúng định dạng (Ví dụ: Sai kiểu Enum, sai kiểu ngày tháng...)",
                null
        );
        return ResponseEntity.status(org.springframework.http.HttpStatus.BAD_REQUEST).body(response);
    }


    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<BaseResponse<String>> handleRuntimeException(RuntimeException ex) {

        // Vì code của bạn bạn đang bọc lỗi bằng "new RuntimeException(e)",
        // ta cần moi cái thông báo gốc ở lõi ra (getCause)
        String errorMessage = ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage();

        // Nếu lỗi là do Spring Security ném ra (Sai email hoặc mật khẩu)
        if (errorMessage != null && errorMessage.contains("Bad credentials")) {
            errorMessage = "Email hoặc mật khẩu không chính xác!";
        }

        // Đóng gói lại thành chuẩn BaseResponse
        BaseResponse<String> response = new BaseResponse<>(
                org.springframework.http.HttpStatus.BAD_REQUEST.value(),
                errorMessage,
                null
        );

        return ResponseEntity.status(org.springframework.http.HttpStatus.BAD_REQUEST).body(response);
    }
}

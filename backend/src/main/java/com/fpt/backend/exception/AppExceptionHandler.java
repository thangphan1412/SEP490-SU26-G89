package com.fpt.backend.exception;

import com.fpt.backend.util.BaseResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class AppExceptionHandler {
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<BaseResponse<Void>> handleAccessDenied(
            AccessDeniedException ex
    ) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                new BaseResponse<>(
                        HttpStatus.FORBIDDEN.value(),
                        "You do not have permission to perform this action",
                        null
                )
        );
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<BaseResponse<Void>> handleDataIntegrityViolation(
            DataIntegrityViolationException ex
    ) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
                new BaseResponse<>(
                        HttpStatus.CONFLICT.value(),
                        "The data conflicts with an existing record; reload and try again",
                        null
                )
        );
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<BaseResponse<Void>> handleResponseStatusException(
            ResponseStatusException ex
    ) {
        String message = ex.getReason() == null
                ? ex.getStatusCode().toString()
                : ex.getReason();
        return ResponseEntity.status(ex.getStatusCode()).body(
                new BaseResponse<>(ex.getStatusCode().value(), message, null)
        );
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<BaseResponse<Void>> handleNotFoundException(
            NotFoundException ex
    ) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                new BaseResponse<>(HttpStatus.NOT_FOUND.value(), ex.getMessage(), null)
        );
    }

    @ExceptionHandler(BadHttpException.class)
    public ResponseEntity<BaseResponse<Void>> handleBadHttpException(
            BadHttpException ex
    ) {
        return ResponseEntity.badRequest().body(
                new BaseResponse<>(HttpStatus.BAD_REQUEST.value(), ex.getMessage(), null)
        );
    }

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
        log.error("Unhandled server error", ex);
        BaseResponse<String> response = new BaseResponse<>(
                org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Unexpected server error",
                null
        );

        return ResponseEntity.status(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}

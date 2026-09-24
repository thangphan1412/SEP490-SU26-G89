package com.fpt.backend.util.userDate;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = UserDateValidator.class)
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidUserDate {
    String message() default "Ngày phải hợp lệ và theo định dạng dd/MM/yyyy";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

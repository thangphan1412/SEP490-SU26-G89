package com.fpt.backend.util.startDate;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = StartDateValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidStartDate {
    String message() default "Ngày bắt đầu làm việc không được nằm trong quá khứ";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

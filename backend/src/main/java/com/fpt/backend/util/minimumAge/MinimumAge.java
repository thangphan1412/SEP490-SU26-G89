package com.fpt.backend.util.minimumAge;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Constraint(validatedBy = MinimumAgeValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface MinimumAge {

    int value() default 18;

    String message() default "Users must be at least {value} years old.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
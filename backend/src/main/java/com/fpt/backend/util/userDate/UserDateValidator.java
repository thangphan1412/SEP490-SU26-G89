package com.fpt.backend.util.userDate;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.time.format.DateTimeParseException;

public class UserDateValidator implements ConstraintValidator<ValidUserDate, String> {
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) return true; // Handled by @NotBlank.
        try {
            UserDateFormat.parse(value);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }
}

package com.fpt.backend.util.startDate;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public class StartDateValidator implements ConstraintValidator<ValidStartDate, String> {

    @Override
    public boolean isValid(String dateString, ConstraintValidatorContext context) {
        if (dateString == null || dateString.trim().isEmpty()) {
            return true;
        }
        try {
            LocalDate startDate = LocalDate.parse(dateString);
            LocalDate today = LocalDate.now();

            // Hợp lệ nếu ngày bắt đầu >= hôm nay
            return !startDate.isBefore(today);

        } catch (DateTimeParseException e) {
            return true;
        }
    }
}

package com.fpt.backend.util.minimumAge;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeParseException;

public class MinimumAgeValidator implements ConstraintValidator<MinimumAge, String> {

    private int minimumAge;

    @Override
    public void initialize(MinimumAge constraintAnnotation) {
        this.minimumAge = constraintAnnotation.value();
    }

    @Override
    public boolean isValid(String dobString, ConstraintValidatorContext context) {
        if (dobString == null || dobString.trim().isEmpty()) {
            return true;
        }

        try {
            LocalDate dob = LocalDate.parse(dobString);
            LocalDate today = LocalDate.now();
            int age = Period.between(dob, today).getYears();
            return age >= minimumAge;
        } catch (DateTimeParseException e) {
            return true;
        }
    }
}
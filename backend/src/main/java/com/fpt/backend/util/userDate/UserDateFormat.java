package com.fpt.backend.util.userDate;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;

public final class UserDateFormat {
    private static final DateTimeFormatter FORMAT = DateTimeFormatter
            .ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT);

    private UserDateFormat() {}

    public static LocalDate parse(String value) {
        if (value == null || !value.matches("[0-9]{2}/[0-9]{2}/[0-9]{4}")) {
            throw new DateTimeParseException("Expected dd/MM/yyyy", value == null ? "" : value, 0);
        }
        LocalDate date = LocalDate.parse(value, FORMAT);
        if (date.getYear() < 1) {
            throw new DateTimeParseException("Year must be positive", value, 6);
        }
        return date;
    }
}

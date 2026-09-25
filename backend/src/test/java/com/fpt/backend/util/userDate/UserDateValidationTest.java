package com.fpt.backend.util.userDate;

import com.fpt.backend.dto.request.user.UserCreateRequestDTO;
import com.fpt.backend.util.startDate.StartDateValidator;
import jakarta.validation.Validation;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import static org.junit.jupiter.api.Assertions.*;

class UserDateValidationTest {
    @Test
    void acceptsDayFirstDatesAndPreservesIsoStorage() {
        assertEquals("2000-02-29", UserDateFormat.parse("29/02/2000").toString());
        assertEquals("2026-09-24", UserDateFormat.parse("24/09/2026").toString());
    }

    @Test
    void validatesBothCreateRequestFieldsIncludingInvalidCalendarDates() {
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            var validator = factory.getValidator();
            for (String field : new String[]{"dob", "startDate"}) {
                for (String value : new String[]{"31/02/2026", "29/02/2025", "2026-09-24", "1/2/2026", "01/01/0000", ""}) {
                    assertFalse(validator.validateValue(UserCreateRequestDTO.class, field, value).isEmpty(), field + ": " + value);
                }
            }
            assertTrue(validator.validateValue(UserCreateRequestDTO.class, "dob", "29/02/2000").isEmpty());
            String tomorrow = LocalDate.now().plusDays(1).format(DateTimeFormatter.ofPattern("dd/MM/uuuu"));
            assertTrue(validator.validateValue(UserCreateRequestDTO.class, "startDate", tomorrow).isEmpty());
        }
    }

    @Test
    void stillRejectsPastStartDates() {
        var validator = new StartDateValidator();
        var format = DateTimeFormatter.ofPattern("dd/MM/uuuu");
        assertFalse(validator.isValid(LocalDate.now().minusDays(1).format(format), null));
        assertTrue(validator.isValid(LocalDate.now().format(format), null));
        assertTrue(validator.isValid(LocalDate.now().plusDays(1).format(format), null));
    }
}

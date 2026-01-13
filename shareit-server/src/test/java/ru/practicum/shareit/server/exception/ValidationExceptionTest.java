package ru.practicum.shareit.server.exception;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ValidationExceptionTest {

    @Test
    void testValidationException() {
        String message = "Validation failed";
        ValidationException exception = new ValidationException(message);

        assertThat(exception.getMessage()).isEqualTo(message);
    }

    @Test
    void testValidationExceptionWithCause() {
        String message = "Validation failed";
        Throwable cause = new RuntimeException("Root cause");
        ValidationException exception = new ValidationException(message, cause);

        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception.getCause()).isEqualTo(cause);
    }
}

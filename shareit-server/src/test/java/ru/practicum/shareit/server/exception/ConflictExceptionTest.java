package ru.practicum.shareit.server.exception;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ConflictExceptionTest {

    @Test
    void testConflictException() {
        String message = "Resource conflict";
        ConflictException exception = new ConflictException(message);

        assertThat(exception.getMessage()).isEqualTo(message);
    }

    @Test
    void testConflictExceptionWithCause() {
        String message = "Resource conflict";
        Throwable cause = new RuntimeException("Root cause");
        ConflictException exception = new ConflictException(message, cause);

        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception.getCause()).isEqualTo(cause);
    }
}

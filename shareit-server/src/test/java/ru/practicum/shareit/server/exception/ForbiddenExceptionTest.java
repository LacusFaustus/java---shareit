package ru.practicum.shareit.server.exception;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ForbiddenExceptionTest {

    @Test
    void testForbiddenException() {
        String message = "Access forbidden";
        ForbiddenException exception = new ForbiddenException(message);

        assertThat(exception.getMessage()).isEqualTo(message);
    }

    @Test
    void testForbiddenExceptionWithCause() {
        String message = "Access forbidden";
        Throwable cause = new RuntimeException("Root cause");
        ForbiddenException exception = new ForbiddenException(message, cause);

        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception.getCause()).isEqualTo(cause);
    }
}

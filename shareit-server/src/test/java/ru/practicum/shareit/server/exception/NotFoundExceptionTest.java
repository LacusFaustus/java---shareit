package ru.practicum.shareit.server.exception;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class NotFoundExceptionTest {

    @Test
    void testNotFoundException() {
        String message = "Resource not found";
        NotFoundException exception = new NotFoundException(message);

        assertThat(exception.getMessage()).isEqualTo(message);
    }

    @Test
    void testNotFoundExceptionWithCause() {
        String message = "Resource not found";
        Throwable cause = new RuntimeException("Root cause");
        NotFoundException exception = new NotFoundException(message, cause);

        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception.getCause()).isEqualTo(cause);
    }
}

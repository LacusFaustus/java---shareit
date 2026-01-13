package ru.practicum.shareit.server.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ExceptionHandlerTest {

    private final GlobalExceptionHandler exceptionHandler = new GlobalExceptionHandler();

    @Test
    void handleNotFoundException_ReturnsNotFoundResponse() {
        NotFoundException exception = new NotFoundException("User not found");

        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleNotFoundException(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("error")).isEqualTo("User not found");
        assertThat(response.getBody().get("status")).isEqualTo("404 NOT_FOUND");
        assertThat(response.getBody().get("timestamp")).isInstanceOf(LocalDateTime.class);
    }

    @Test
    void handleValidationException_ReturnsBadRequestResponse() {
        ValidationException exception = new ValidationException("Invalid email format");

        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleValidationException(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().get("error")).isEqualTo("Invalid email format");
        assertThat(response.getBody().get("status")).isEqualTo("400 BAD_REQUEST");
    }

    @Test
    void handleConflictException_ReturnsConflictResponse() {
        ConflictException exception = new ConflictException("Email already exists");

        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleConflictException(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody().get("error")).isEqualTo("Email already exists");
        assertThat(response.getBody().get("status")).isEqualTo("409 CONFLICT");
    }

    @Test
    void handleForbiddenException_ReturnsForbiddenResponse() {
        ForbiddenException exception = new ForbiddenException("Access denied");

        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleForbiddenException(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody().get("error")).isEqualTo("Access denied");
        assertThat(response.getBody().get("status")).isEqualTo("403 FORBIDDEN");
    }

    @Test
    void handleOtherExceptions_ReturnsInternalServerError() {
        Exception exception = new RuntimeException("Database connection failed");

        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleOtherExceptions(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody().get("error")).isEqualTo("Internal server error");
        assertThat(response.getBody().get("status")).isEqualTo("500 INTERNAL_SERVER_ERROR");
    }
}

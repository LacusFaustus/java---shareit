package ru.practicum.shareit.gateway.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GatewayErrorHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Object> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        String errorMessage = e.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                .findFirst()
                .orElse("Validation error");

        log.warn("Validation error: {}", errorMessage);

        return Map.of(
                "error", errorMessage,
                "status", "BAD_REQUEST",
                "timestamp", LocalDateTime.now().toString()
        );
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Object> handleMissingHeaderException(MissingRequestHeaderException e) {
        String errorMessage = "Missing required header: " + e.getHeaderName();
        log.warn(errorMessage);

        return Map.of(
                "error", errorMessage,
                "status", "BAD_REQUEST",
                "timestamp", LocalDateTime.now().toString()
        );
    }

    @ExceptionHandler(HttpClientErrorException.class)
    public ResponseEntity<Map<String, Object>> handleHttpClientErrorException(HttpClientErrorException e) {
        log.warn("HTTP Client Error: {} - {}", e.getStatusCode(), e.getStatusText());

        Map<String, Object> errorResponse = Map.of(
                "error", "Server validation failed: " + e.getMessage(),
                "status", e.getStatusCode().toString(),
                "timestamp", LocalDateTime.now().toString()
        );

        return ResponseEntity
                .status(e.getStatusCode())
                .body(errorResponse);
    }

    @ExceptionHandler(HttpServerErrorException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Map<String, Object> handleHttpServerErrorException(HttpServerErrorException e) {
        log.error("HTTP Server Error: {} - {}", e.getStatusCode(), e.getStatusText());

        return Map.of(
                "error", "Internal server error occurred: " + e.getMessage(),
                "status", "INTERNAL_SERVER_ERROR",
                "timestamp", LocalDateTime.now().toString()
        );
    }

    @ExceptionHandler(ResourceAccessException.class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public Map<String, Object> handleResourceAccessException(ResourceAccessException e) {
        log.error("Service unavailable: {}", e.getMessage());

        return Map.of(
                "error", "ShareIt Server is currently unavailable. Please try again later.",
                "status", "SERVICE_UNAVAILABLE",
                "timestamp", LocalDateTime.now().toString()
        );
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Object> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e) {
        String errorMessage = "Invalid parameter value: " + e.getName() + " should be of type " +
                (e.getRequiredType() != null ? e.getRequiredType().getSimpleName() : "unknown");
        log.warn(errorMessage);

        return Map.of(
                "error", errorMessage,
                "status", "BAD_REQUEST",
                "timestamp", LocalDateTime.now().toString()
        );
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Object> handleIllegalArgumentException(IllegalArgumentException e) {
        log.warn("Illegal argument: {}", e.getMessage());

        return Map.of(
                "error", e.getMessage(),
                "status", "BAD_REQUEST",
                "timestamp", LocalDateTime.now().toString()
        );
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Map<String, Object> handleOtherExceptions(Exception e) {
        log.error("Unexpected error in gateway", e);

        return Map.of(
                "error", "Internal gateway error occurred",
                "status", "INTERNAL_SERVER_ERROR",
                "timestamp", LocalDateTime.now().toString()
        );
    }
}

package ru.practicum.shareit.gateway.exception;

import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.method.support.HandlerMethodReturnValueHandler;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Path;
import java.lang.reflect.Method;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class GatewayErrorHandlerEdgeCasesTest {

    private final GatewayErrorHandler errorHandler = new GatewayErrorHandler();

    @Test
    void handleMethodArgumentNotValidException_WithNoFieldErrors_ReturnsGenericError() throws Exception {
        // Arrange
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);

        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(Collections.emptyList());

        // Act
        Map<String, Object> response = errorHandler.handleMethodArgumentNotValidException(exception);

        // Assert
        assertThat(response).containsKeys("error", "status", "timestamp");
        assertThat(response.get("error")).isEqualTo("Validation error");
    }

    @Test
    void handleHandlerMethodValidationException_WithEmptyValidationResults_ReturnsGenericError() {
        // Arrange
        HandlerMethodValidationException exception = mock(HandlerMethodValidationException.class);
        when(exception.getAllValidationResults()).thenReturn(Collections.emptyList());

        // Act
        Map<String, Object> response = errorHandler.handleHandlerMethodValidationException(exception);

        // Assert
        assertThat(response).containsKeys("error", "status", "timestamp");
        assertThat(response.get("error")).isEqualTo("Validation error in request parameters");
    }

    @Test
    void handleMethodArgumentTypeMismatchException_WithNullRequiredType() {
        // Arrange
        MethodArgumentTypeMismatchException exception = mock(MethodArgumentTypeMismatchException.class);
        when(exception.getName()).thenReturn("param");
        when(exception.getRequiredType()).thenReturn(null);

        // Act
        Map<String, Object> response = errorHandler.handleMethodArgumentTypeMismatchException(exception);

        // Assert
        assertThat(response).containsKeys("error", "status", "timestamp");
        assertThat(response.get("error")).asString().contains("Invalid parameter value: param should be of type unknown");
    }

    @Test
    void handleHttpClientErrorException_WithEmptyResponseBody() {
        // Arrange
        HttpClientErrorException exception = HttpClientErrorException.create(
                HttpStatus.BAD_REQUEST,
                "Bad Request",
                null,
                new byte[0],
                null
        );

        // Act
        ResponseEntity<Map<String, Object>> response = errorHandler.handleHttpClientErrorException(exception);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).containsKeys("error", "status", "timestamp");
        assertThat(response.getBody().get("error")).asString().contains("Server validation failed:");
    }

    @Test
    void handleConstraintViolationException_WithMultipleViolations() {
        // Arrange
        ConstraintViolationException exception = mock(ConstraintViolationException.class);

        Set<ConstraintViolation<?>> violations = new HashSet<>();

        // Первое нарушение
        ConstraintViolation<?> violation1 = mock(ConstraintViolation.class);
        when(violation1.getPropertyPath()).thenReturn(new TestPath("email"));
        when(violation1.getMessage()).thenReturn("must be a valid email");
        violations.add(violation1);

        // Второе нарушение
        ConstraintViolation<?> violation2 = mock(ConstraintViolation.class);
        when(violation2.getPropertyPath()).thenReturn(new TestPath("password"));
        when(violation2.getMessage()).thenReturn("must not be empty");
        violations.add(violation2);

        when(exception.getConstraintViolations()).thenReturn(violations);

        // Act
        Map<String, Object> response = errorHandler.handleConstraintViolationException(exception);

        // Assert
        assertThat(response).containsKeys("error", "status", "timestamp");
        String error = (String) response.get("error");
        assertThat(error).contains("email: must be a valid email");
        assertThat(error).contains("password: must not be empty");
    }

    // Вспомогательный класс для Path
    private static class TestPath implements Path {
        private final String path;

        TestPath(String path) {
            this.path = path;
        }

        @Override
        public String toString() {
            return path;
        }

        @Override
        public Iterator<Node> iterator() {
            return Collections.emptyIterator();
        }
    }
}

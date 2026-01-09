package ru.practicum.shareit.gateway.exception;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.method.ParameterValidationResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.context.support.DefaultMessageSourceResolvable;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GatewayErrorHandlerComprehensiveTest {

    private GatewayErrorHandler errorHandler;

    @BeforeEach
    void setUp() {
        errorHandler = new GatewayErrorHandler();
    }

    @Nested
    class MethodArgumentNotValidExceptionTests {

        @Test
        void handleMethodArgumentNotValidException_WithSingleError() {
            MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
            BindingResult bindingResult = mock(BindingResult.class);

            List<FieldError> fieldErrors = Collections.singletonList(
                    new FieldError("object", "field1", "must not be empty")
            );

            when(exception.getBindingResult()).thenReturn(bindingResult);
            when(bindingResult.getFieldErrors()).thenReturn(fieldErrors);

            Map<String, Object> response = errorHandler.handleMethodArgumentNotValidException(exception);

            assertThat(response.get("error")).isEqualTo("field1: must not be empty");
            assertThat(response.get("status")).isEqualTo("BAD_REQUEST");
        }

        @Test
        void handleMethodArgumentNotValidException_WithMultipleErrors() {
            MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
            BindingResult bindingResult = mock(BindingResult.class);

            List<FieldError> fieldErrors = Arrays.asList(
                    new FieldError("object", "field1", "must not be empty"),
                    new FieldError("object", "field2", "must be valid")
            );

            when(exception.getBindingResult()).thenReturn(bindingResult);
            when(bindingResult.getFieldErrors()).thenReturn(fieldErrors);

            Map<String, Object> response = errorHandler.handleMethodArgumentNotValidException(exception);

            assertThat(response.get("error")).isEqualTo("field1: must not be empty");
        }

        @Test
        void handleMethodArgumentNotValidException_WithEmptyFieldErrors() {
            MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
            BindingResult bindingResult = mock(BindingResult.class);

            when(exception.getBindingResult()).thenReturn(bindingResult);
            when(bindingResult.getFieldErrors()).thenReturn(Collections.emptyList());

            Map<String, Object> response = errorHandler.handleMethodArgumentNotValidException(exception);

            assertThat(response.get("error")).isEqualTo("Validation error");
        }
    }

    @Nested
    class ConstraintViolationExceptionTests {

        @Test
        void handleConstraintViolationException_WithSingleViolation() {
            ConstraintViolationException exception = mock(ConstraintViolationException.class);

            Set<ConstraintViolation<?>> violations = new HashSet<>();
            ConstraintViolation<?> violation = mock(ConstraintViolation.class);

            when(violation.getPropertyPath()).thenReturn(new TestPath("email"));
            when(violation.getMessage()).thenReturn("must be a valid email");
            violations.add(violation);

            when(exception.getConstraintViolations()).thenReturn(violations);

            Map<String, Object> response = errorHandler.handleConstraintViolationException(exception);

            assertThat(response.get("error")).isEqualTo("email: must be a valid email");
        }

        @Test
        void handleConstraintViolationException_WithMultipleViolations() {
            ConstraintViolationException exception = mock(ConstraintViolationException.class);

            Set<ConstraintViolation<?>> violations = new HashSet<>();

            ConstraintViolation<?> violation1 = mock(ConstraintViolation.class);
            when(violation1.getPropertyPath()).thenReturn(new TestPath("email"));
            when(violation1.getMessage()).thenReturn("must be a valid email");

            ConstraintViolation<?> violation2 = mock(ConstraintViolation.class);
            when(violation2.getPropertyPath()).thenReturn(new TestPath("password"));
            when(violation2.getMessage()).thenReturn("must not be empty");

            violations.add(violation1);
            violations.add(violation2);

            when(exception.getConstraintViolations()).thenReturn(violations);

            Map<String, Object> response = errorHandler.handleConstraintViolationException(exception);

            String error = (String) response.get("error");
            assertThat(error).contains("email: must be a valid email");
            assertThat(error).contains("password: must not be empty");
        }
    }

    @Nested
    class HandlerMethodValidationExceptionTests {

        @Test
        void handleHandlerMethodValidationException_WithEmptyValidationResults() {
            HandlerMethodValidationException exception = mock(HandlerMethodValidationException.class);
            when(exception.getAllValidationResults()).thenReturn(Collections.emptyList());

            Map<String, Object> response = errorHandler.handleHandlerMethodValidationException(exception);

            assertThat(response.get("error")).isEqualTo("Validation error in request parameters");
        }

        @Test
        void handleHandlerMethodValidationException_WithNonEmptyValidationResults() {
            HandlerMethodValidationException exception = mock(HandlerMethodValidationException.class);
            ParameterValidationResult validationResult = mock(ParameterValidationResult.class);

            DefaultMessageSourceResolvable error = mock(DefaultMessageSourceResolvable.class);
            when(error.getDefaultMessage()).thenReturn("Parameter must be positive");

            when(validationResult.getResolvableErrors()).thenReturn(Collections.singletonList(error));
            when(exception.getAllValidationResults()).thenReturn(Collections.singletonList(validationResult));

            Map<String, Object> response = errorHandler.handleHandlerMethodValidationException(exception);

            assertThat(response.get("error")).isEqualTo("Parameter must be positive");
        }

        @Test
        void handleHandlerMethodValidationException_WithEmptyResolvableErrors() {
            HandlerMethodValidationException exception = mock(HandlerMethodValidationException.class);
            ParameterValidationResult validationResult = mock(ParameterValidationResult.class);

            when(validationResult.getResolvableErrors()).thenReturn(Collections.emptyList());
            when(exception.getAllValidationResults()).thenReturn(Collections.singletonList(validationResult));

            Map<String, Object> response = errorHandler.handleHandlerMethodValidationException(exception);

            assertThat(response.get("error")).isEqualTo("Validation error in request parameters");
        }
    }

    @Nested
    class HttpClientAndServerErrorTests {

        @Test
        void handleHttpClientErrorException() {
            HttpClientErrorException exception = HttpClientErrorException.create(
                    HttpStatus.NOT_FOUND,
                    "Not Found",
                    null,
                    "Item not found".getBytes(),
                    null
            );

            ResponseEntity<Map<String, Object>> response = errorHandler.handleHttpClientErrorException(exception);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            assertThat(response.getBody().get("error")).asString().contains("Server validation failed");
        }

        @Test
        void handleHttpServerErrorException() {
            HttpServerErrorException exception = HttpServerErrorException.create(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Internal Server Error",
                    null,
                    "Database error".getBytes(),
                    null
            );

            Map<String, Object> response = errorHandler.handleHttpServerErrorException(exception);

            assertThat(response.get("status")).isEqualTo("INTERNAL_SERVER_ERROR");
            assertThat(response.get("error")).asString().contains("Internal server error occurred");
        }
    }

    @Nested
    class OtherExceptionTests {

        @Test
        void handleMissingRequestHeaderException() {
            MissingRequestHeaderException exception = mock(MissingRequestHeaderException.class);
            when(exception.getHeaderName()).thenReturn("X-Sharer-User-Id");

            Map<String, Object> response = errorHandler.handleMissingHeaderException(exception);

            assertThat(response.get("error")).isEqualTo("Missing required header: X-Sharer-User-Id");
        }

        @Test
        void handleMethodArgumentTypeMismatchException() {
            MethodArgumentTypeMismatchException exception = mock(MethodArgumentTypeMismatchException.class);
            when(exception.getName()).thenReturn("userId");
            when(exception.getRequiredType()).thenReturn((Class) Long.class);

            Map<String, Object> response = errorHandler.handleMethodArgumentTypeMismatchException(exception);

            assertThat(response.get("error")).asString().contains("Invalid parameter value: userId");
        }

        @Test
        void handleMethodArgumentTypeMismatchException_WithNullRequiredType() {
            MethodArgumentTypeMismatchException exception = mock(MethodArgumentTypeMismatchException.class);
            when(exception.getName()).thenReturn("param");
            when(exception.getRequiredType()).thenReturn(null);

            Map<String, Object> response = errorHandler.handleMethodArgumentTypeMismatchException(exception);

            assertThat(response.get("error")).asString().contains("Invalid parameter value: param");
        }

        @Test
        void handleResourceAccessException() {
            ResourceAccessException exception = new ResourceAccessException("Connection refused");

            Map<String, Object> response = errorHandler.handleResourceAccessException(exception);

            assertThat(response.get("status")).isEqualTo("SERVICE_UNAVAILABLE");
            assertThat(response.get("error")).asString().contains("ShareIt Server is currently unavailable");
        }

        @Test
        void handleIllegalArgumentException() {
            IllegalArgumentException exception = new IllegalArgumentException("Invalid date range");

            Map<String, Object> response = errorHandler.handleIllegalArgumentException(exception);

            assertThat(response.get("error")).isEqualTo("Invalid date range");
            assertThat(response.get("status")).isEqualTo("BAD_REQUEST");
        }

        @Test
        void handleOtherExceptions() {
            RuntimeException exception = new RuntimeException("Unexpected error");

            Map<String, Object> response = errorHandler.handleOtherExceptions(exception);

            assertThat(response.get("error")).isEqualTo("Internal gateway error occurred");
            assertThat(response.get("status")).isEqualTo("INTERNAL_SERVER_ERROR");
        }
    }

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

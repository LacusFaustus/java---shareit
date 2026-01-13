package ru.practicum.shareit.gateway.client;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ResilientBaseClientTest {

    @Mock
    private org.springframework.web.client.RestTemplate restTemplate;

    @Test
    void constructor_CreatesInstanceSuccessfully() {
        // Act
        ResilientBaseClient client = new ResilientBaseClient("http://localhost:8080", restTemplate) {
            // Анонимный класс
        };

        // Assert
        assertNotNull(client);
    }

    @Test
    void fallbackResponse_ReturnsServiceUnavailable() {
        // Arrange
        ResilientBaseClient client = new ResilientBaseClient("http://localhost:8080", restTemplate) {
            // Анонимный класс
        };

        // Act
        ResponseEntity<Object> response = client.fallbackResponse(new RuntimeException("test"));

        // Assert
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
        assertNotNull(response.getBody());

        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertEquals("Service temporarily unavailable. Please try again later.", body.get("error"));
        assertEquals("SERVICE_UNAVAILABLE", body.get("status"));
    }

    @Test
    void fallbackResponse_WithDifferentException_ReturnsServiceUnavailable() {
        // Arrange
        ResilientBaseClient client = new ResilientBaseClient("http://localhost:8080", restTemplate) {
            // Анонимный класс
        };

        // Act
        ResponseEntity<Object> response = client.fallbackResponse(
                new IllegalStateException("test exception")
        );

        // Assert
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
    }

    @Test
    @SuppressWarnings("unchecked")
    void get_DelegatesToSuperClass() {
        // Arrange
        ResilientBaseClient client = new ResilientBaseClient("http://localhost:8080", restTemplate) {
            // Анонимный класс
        };
        ResilientBaseClient spyClient = spy(client);

        ResponseEntity<Object> expectedResponse = ResponseEntity.ok("test");
        doReturn(expectedResponse).when(spyClient).get(anyString(), any(), any());

        // Act
        ResponseEntity<Object> response = spyClient.get("test", 1L, null);

        // Assert
        assertEquals(expectedResponse, response);
        verify(spyClient).get("test", 1L, null);
    }

    @Test
    @SuppressWarnings("unchecked")
    void post_DelegatesToSuperClass() {
        // Arrange
        ResilientBaseClient client = new ResilientBaseClient("http://localhost:8080", restTemplate) {
            // Анонимный класс
        };
        ResilientBaseClient spyClient = spy(client);

        ResponseEntity<Object> expectedResponse = ResponseEntity.ok("created");
        doReturn(expectedResponse).when(spyClient).post(anyString(), any(), any(), any());

        // Act
        ResponseEntity<Object> response = spyClient.post("test", 1L, null, "body");

        // Assert
        assertEquals(expectedResponse, response);
        verify(spyClient).post("test", 1L, null, "body");
    }

    @Test
    @SuppressWarnings("unchecked")
    void patch_DelegatesToSuperClass() {
        // Arrange
        ResilientBaseClient client = new ResilientBaseClient("http://localhost:8080", restTemplate) {
            // Анонимный класс
        };
        ResilientBaseClient spyClient = spy(client);

        ResponseEntity<Object> expectedResponse = ResponseEntity.ok("updated");
        doReturn(expectedResponse).when(spyClient).patch(anyString(), any(), any(), any());

        // Act
        ResponseEntity<Object> response = spyClient.patch("test", 1L, null, "body");

        // Assert
        assertEquals(expectedResponse, response);
        verify(spyClient).patch("test", 1L, null, "body");
    }

    @Test
    @SuppressWarnings("unchecked")
    void delete_DelegatesToSuperClass() {
        // Arrange
        ResilientBaseClient client = new ResilientBaseClient("http://localhost:8080", restTemplate) {
            // Анонимный класс
        };
        ResilientBaseClient spyClient = spy(client);

        ResponseEntity<Object> expectedResponse = ResponseEntity.noContent().build();
        doReturn(expectedResponse).when(spyClient).delete(anyString(), any(), any());

        // Act
        ResponseEntity<Object> response = spyClient.delete("test", 1L, null);

        // Assert
        assertEquals(expectedResponse, response);
        verify(spyClient).delete("test", 1L, null);
    }

    @Test
    void circuitBreakerAnnotations_Present() throws Exception {
        // Проверяем что методы аннотированы @CircuitBreaker
        var getMethod = ResilientBaseClient.class.getDeclaredMethod("get", String.class, Long.class, Map.class);
        assertTrue(getMethod.isAnnotationPresent(io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker.class));
    }
}

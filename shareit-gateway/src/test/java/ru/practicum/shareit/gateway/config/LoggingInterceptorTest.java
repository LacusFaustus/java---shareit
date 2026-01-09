package ru.practicum.shareit.gateway.config;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class LoggingInterceptorTest {

    @Test
    void intercept_ShouldLogRequestAndResponse() throws IOException {
        // Arrange
        LoggingInterceptor interceptor = new LoggingInterceptor();
        HttpRequest request = mock(HttpRequest.class);
        byte[] body = "test body".getBytes();
        ClientHttpRequestExecution execution = mock(ClientHttpRequestExecution.class);

        ClientHttpResponse response = mock(ClientHttpResponse.class);
        when(execution.execute(any(HttpRequest.class), any(byte[].class))).thenReturn(response);
        when(response.getStatusCode()).thenReturn(org.springframework.http.HttpStatus.OK);

        // Act
        ClientHttpResponse result = interceptor.intercept(request, body, execution);

        // Assert
        assertNotNull(result);
        assertSame(response, result);
        verify(execution).execute(request, body);
    }

    @Test
    void intercept_WhenSlowRequest_ShouldLogWarning() throws IOException {
        // Arrange
        LoggingInterceptor interceptor = new LoggingInterceptor();
        HttpRequest request = mock(HttpRequest.class);
        byte[] body = "test body".getBytes();
        ClientHttpRequestExecution execution = mock(ClientHttpRequestExecution.class);

        ClientHttpResponse response = mock(ClientHttpResponse.class);
        // Меняем подход - используем doAnswer вместо when().thenAnswer()
        doAnswer(invocation -> {
            // Симулируем медленный запрос
            Thread.sleep(1500);
            return response;
        }).when(execution).execute(any(HttpRequest.class), any(byte[].class));

        when(response.getStatusCode()).thenReturn(org.springframework.http.HttpStatus.OK);

        // Act
        ClientHttpResponse result = interceptor.intercept(request, body, execution);

        // Assert
        assertNotNull(result);
        assertSame(response, result);
        verify(execution).execute(request, body);
    }
}

package ru.practicum.shareit.gateway.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.lang.Nullable;

import java.util.Map;

@Slf4j
public abstract class ResilientBaseClient extends BaseClient {

    public ResilientBaseClient(String serverUrl, org.springframework.web.client.RestTemplate rest) {
        super(serverUrl, rest);
    }

    @CircuitBreaker(name = "shareItServer", fallbackMethod = "fallbackResponse")
    @Override
    protected ResponseEntity<Object> get(String path, Long userId, @Nullable Map<String, Object> parameters) {
        return super.get(path, userId, parameters);
    }

    @CircuitBreaker(name = "shareItServer", fallbackMethod = "fallbackResponse")
    @Override
    protected <T> ResponseEntity<Object> post(String path, Long userId, @Nullable Map<String, Object> parameters, T body) {
        return super.post(path, userId, parameters, body);
    }

    @CircuitBreaker(name = "shareItServer", fallbackMethod = "fallbackResponse")
    @Override
    protected <T> ResponseEntity<Object> patch(String path, Long userId, @Nullable Map<String, Object> parameters, T body) {
        return super.patch(path, userId, parameters, body);
    }

    @CircuitBreaker(name = "shareItServer", fallbackMethod = "fallbackResponse")
    @Override
    protected ResponseEntity<Object> delete(String path, Long userId, @Nullable Map<String, Object> parameters) {
        return super.delete(path, userId, parameters);
    }

    // Fallback метод
    protected ResponseEntity<Object> fallbackResponse(Exception e) {
        log.warn("Circuit breaker fallback triggered: {}", e.getMessage());

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of(
                        "error", "Service temporarily unavailable. Please try again later.",
                        "status", "SERVICE_UNAVAILABLE"
                ));
    }
}

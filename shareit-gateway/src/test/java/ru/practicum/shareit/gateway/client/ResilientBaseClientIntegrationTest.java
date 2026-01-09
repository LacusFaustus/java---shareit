package ru.practicum.shareit.gateway.client;

import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class ResilientBaseClientIntegrationTest {

    @Autowired
    private CircuitBreakerRegistry circuitBreakerRegistry;

    @Autowired
    private RestTemplate restTemplate;

    @Test
    void testCircuitBreakerIntegration() {
        // Создаем тестовый клиент
        ResilientBaseClient client = new ResilientBaseClient("http://localhost:9090", restTemplate) {
            // Простая реализация для тестирования
        };

        // Проверяем, что CircuitBreaker создан
        var circuitBreaker = circuitBreakerRegistry.circuitBreaker("shareItServer");
        assertThat(circuitBreaker).isNotNull();

        // Тестируем fallback
        ResponseEntity<Object> fallbackResponse = client.fallbackResponse(
                new RuntimeException("Test exception")
        );

        assertThat(fallbackResponse.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        assertThat(fallbackResponse.getBody()).asString().contains("Service temporarily unavailable");
    }
}

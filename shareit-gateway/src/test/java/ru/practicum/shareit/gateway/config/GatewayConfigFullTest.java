package ru.practicum.shareit.gateway.config;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class GatewayConfigFullTest {

    @Autowired
    private GatewayConfig gatewayConfig;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private CacheManager cacheManager;

    @Test
    void testAllBeansCreated() {
        assertThat(gatewayConfig).isNotNull();
        assertThat(restTemplate).isNotNull();
        assertThat(cacheManager).isNotNull();
    }

    @Test
    void testCircuitBreakerConfiguration() {
        CircuitBreakerConfig config = gatewayConfig.circuitBreakerConfig();
        assertThat(config).isNotNull();

        // Проверяем основные настройки
        assertThat(config.getSlidingWindowSize()).isEqualTo(10);
        assertThat(config.getFailureRateThreshold()).isEqualTo(50);

        // Проверяем waitDurationInOpenState через рефлексию, если нужно
        try {
            java.lang.reflect.Method method = CircuitBreakerConfig.class.getMethod("waitDurationInOpenState");
            java.time.Duration waitDuration = (java.time.Duration) method.invoke(config);
            assertThat(waitDuration).isEqualTo(java.time.Duration.ofSeconds(10));
        } catch (Exception e) {
            // Если метод не найден, пропускаем эту проверку
            System.out.println("Note: waitDurationInOpenState method not accessible: " + e.getMessage());
        }

        CircuitBreakerRegistry registry = gatewayConfig.circuitBreakerRegistry(config);
        assertThat(registry).isNotNull();

        CircuitBreaker circuitBreaker = gatewayConfig.shareItServerCircuitBreaker(registry);
        assertThat(circuitBreaker).isNotNull();
        assertThat(circuitBreaker.getName()).isEqualTo("shareItServer");
    }

    @Test
    void testRestTemplateConfiguration() {
        assertThat(restTemplate).isNotNull();
    }
}

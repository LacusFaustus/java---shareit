package ru.practicum.shareit.gateway.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
public class HealthController implements HealthIndicator {

    private final RestTemplate restTemplate;

    @GetMapping("/health")
    public Map<String, String> getHealth() {
        return Map.of(
                "status", "UP",
                "service", "shareit-gateway"
        );
    }

    @GetMapping("/health/detailed")
    public Map<String, Object> getDetailedHealth() {
        try {
            String serverUrl = "http://localhost:9090/actuator/health";
            ResponseEntity<String> response = restTemplate.getForEntity(serverUrl, String.class);

            // Сервер доступен и здоров (2xx)
            if (response.getStatusCode().is2xxSuccessful()) {
                return Map.of(
                        "overall", "UP",
                        "gateway", Map.of("status", "UP", "service", "shareit-gateway"),
                        "server", Map.of("reachable", true, "status", "UP")
                );
            } else {
                // Сервер доступен, но не здоров (4xx/5xx)
                return Map.of(
                        "overall", "DEGRADED",
                        "gateway", Map.of("status", "UP", "service", "shareit-gateway"),
                        "server", Map.of("reachable", true, "status", "DOWN")
                );
            }
        } catch (Exception e) {
            // Сервер недоступен
            log.warn("Server health check failed: {}", e.getMessage());
            return Map.of(
                    "overall", "DEGRADED",
                    "gateway", Map.of("status", "UP", "service", "shareit-gateway"),
                    "server", Map.of("reachable", false, "status", "DOWN")
            );
        }
    }

    private boolean checkServerHealth() {
        try {
            String serverUrl = "http://localhost:9090/actuator/health";
            ResponseEntity<String> response = restTemplate.getForEntity(serverUrl, String.class);

            // Возвращаем true если получили любой HTTP ответ (даже с ошибкой)
            // Это означает что сервер "достижим", но может быть в нерабочем состоянии
            return true;
        } catch (Exception e) {
            log.warn("Server health check failed: {}", e.getMessage());
            // Возвращаем false только если не можем получить ответ (таймаут, соединение разорвано и т.д.)
            return false;
        }
    }

    @Override
    public Health health() {
        return Health.up()
                .withDetail("service", "shareit-gateway")
                .build();
    }
}

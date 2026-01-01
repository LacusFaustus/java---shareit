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
        boolean serverReachable = checkServerHealth();

        String overallStatus = serverReachable ? "UP" : "DEGRADED";

        return Map.of(
                "overall", overallStatus,
                "gateway", Map.of(
                        "status", "UP",
                        "service", "shareit-gateway"
                ),
                "server", Map.of(
                        "reachable", serverReachable,
                        "status", serverReachable ? "UP" : "DOWN"
                )
        );
    }

    private boolean checkServerHealth() {
        try {
            String serverUrl = "http://localhost:9090/actuator/health";
            ResponseEntity<String> response = restTemplate.getForEntity(serverUrl, String.class);
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            log.warn("Server health check failed: {}", e.getMessage());
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

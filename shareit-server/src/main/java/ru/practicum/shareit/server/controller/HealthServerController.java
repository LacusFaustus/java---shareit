package ru.practicum.shareit.server.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
public class HealthServerController {

    private final DataSource dataSource;

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "shareit-server",
                "timestamp", java.time.LocalDateTime.now().toString()
        ));
    }

    @GetMapping("/health/detailed")
    public ResponseEntity<Map<String, Object>> detailedHealth() {
        boolean dbHealthy = checkDatabaseHealth();

        Map<String, Object> healthDetails = Map.of(
                "server", Map.of(
                        "status", "UP",
                        "timestamp", java.time.LocalDateTime.now().toString()
                ),
                "database", Map.of(
                        "status", dbHealthy ? "UP" : "DOWN",
                        "connected", dbHealthy
                ),
                "overall", dbHealthy ? "UP" : "DEGRADED"
        );

        return ResponseEntity.ok(healthDetails);
    }

    private boolean checkDatabaseHealth() {
        try (Connection connection = dataSource.getConnection()) {
            return connection.isValid(5);
        } catch (SQLException e) {
            log.error("Database health check failed: {}", e.getMessage());
            return false;
        }
    }
}

package ru.practicum.shareit.gateway;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public abstract class BaseGatewayTest {

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.main.web-application-type", () -> "servlet");
        registry.add("spring.main.banner-mode", () -> "off");
        registry.add("shareit.server.url", () -> "http://localhost:9090");
        registry.add("spring.autoconfigure.exclude", () -> "");
        registry.add("spring.cache.type", () -> "none");
        registry.add("resilience4j.circuitbreaker.enabled", () -> "false");
        registry.add("resilience4j.retry.enabled", () -> "false");
        registry.add("management.endpoints.web.exposure.include", () -> "health");
        registry.add("logging.level.root", () -> "WARN");
    }
}

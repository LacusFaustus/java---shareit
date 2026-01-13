package ru.practicum.shareit.gateway.config;

import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;

class TestConfigTest {

    @Test
    void restTemplateBean_ShouldBeCreated() {
        // Arrange
        TestConfig config = new TestConfig();

        // Act
        RestTemplate restTemplate = config.restTemplate();

        // Assert
        assertThat(restTemplate).isNotNull();
    }

    @Test
    void cacheManagerBean_ShouldBeCreated() {
        // Arrange
        TestConfig config = new TestConfig();

        // Act
        org.springframework.cache.CacheManager cacheManager = config.cacheManager();

        // Assert
        assertThat(cacheManager).isNotNull();
    }
}

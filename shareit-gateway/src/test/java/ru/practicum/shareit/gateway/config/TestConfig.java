package ru.practicum.shareit.gateway.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@TestConfiguration
public class TestConfig {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean(name = "loggingRestTemplate")
    public RestTemplate loggingRestTemplate() {
        return new RestTemplate();
    }
}

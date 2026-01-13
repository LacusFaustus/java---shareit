package ru.practicum.shareit.server.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@TestConfiguration
@EnableTransactionManagement
public class TestConfig {

    @Bean
    public String testBean() {
        return "test";
    }
}

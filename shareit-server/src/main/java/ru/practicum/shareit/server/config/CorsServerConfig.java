package ru.practicum.shareit.server.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;
import java.util.List;

@Configuration
public class CorsServerConfig {

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();

        // Разрешаем запросы только от Gateway
        config.setAllowedOrigins(List.of("http://localhost:8080"));

        // Разрешаемые методы
        config.setAllowedMethods(Arrays.asList(
                "GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS", "HEAD"
        ));

        // Разрешаемые заголовки
        config.setAllowedHeaders(Arrays.asList(
                "Origin", "Content-Type", "Accept", "Authorization",
                "X-Requested-With", "X-Sharer-User-Id", "Cache-Control"
        ));

        // Экспонируемые заголовки
        config.setExposedHeaders(Arrays.asList(
                "X-Sharer-User-Id", "Content-Type", "Content-Length",
                "Cache-Control", "ETag"
        ));

        // Разрешаем отправку credentials
        config.setAllowCredentials(true);

        // Максимальное время кэширования предварительных запросов
        config.setMaxAge(3600L);

        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}

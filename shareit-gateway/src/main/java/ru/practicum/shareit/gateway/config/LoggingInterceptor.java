package ru.practicum.shareit.gateway.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;

@Slf4j
public class LoggingInterceptor implements ClientHttpRequestInterceptor {

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body,
                                        ClientHttpRequestExecution execution) throws IOException {

        Instant start = Instant.now();

        // Логируем только основные детали запроса
        if (log.isDebugEnabled()) {
            log.debug("REST Request: {} {} ", request.getMethod(), request.getURI());
        }

        // Выполняем запрос
        ClientHttpResponse response = execution.execute(request, body);

        Instant end = Instant.now();
        long duration = Duration.between(start, end).toMillis();

        // Логируем только если запрос медленный
        if (duration > 1000) {
            log.warn("Slow REST call: {} ms - {} {}", duration, request.getMethod(), request.getURI());
        } else if (log.isDebugEnabled()) {
            log.debug("REST Response: {} {} - {} ms",
                    response.getStatusCode(), request.getMethod(), duration);
        }

        return response;
    }
}

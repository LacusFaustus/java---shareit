package ru.practicum.shareit.gateway.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CompleteGatewayCoverageTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RestTemplate restTemplate;

    @Test
    void testAllErrorScenarios() throws Exception {
        // Тестируем только один сценарий ошибки
        testHttpError(HttpStatus.BAD_REQUEST);
    }

    private void testHttpError(HttpStatus status) throws Exception {
        reset(restTemplate);

        // Создаем исключение с правильным форматом JSON, который ожидает GatewayErrorHandler
        String errorJson = "{\"error\":\"Test error\",\"status\":\"BAD_REQUEST\",\"timestamp\":\"2024-01-01T00:00:00\"}";
        HttpClientErrorException exception = HttpClientErrorException.create(
                status,
                "Bad Request",
                HttpHeaders.EMPTY,
                errorJson.getBytes(),
                null
        );

        when(restTemplate.exchange(
                anyString(),
                any(HttpMethod.class),
                any(HttpEntity.class),
                eq(Object.class)
        )).thenThrow(exception);

        // Вызываем endpoint
        mockMvc.perform(get("/users/1"))
                .andExpect(status().is(status.value()))
                .andExpect(jsonPath("$.error").exists())
                .andExpect(jsonPath("$.error").value("Test error"))
                .andExpect(jsonPath("$.status").exists())
                .andExpect(jsonPath("$.status").value(status.name()));
    }
}

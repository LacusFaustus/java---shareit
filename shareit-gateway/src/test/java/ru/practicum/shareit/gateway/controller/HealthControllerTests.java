package ru.practicum.shareit.gateway.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(HealthController.class)
@ActiveProfiles("test")
class HealthControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean(name = "loggingRestTemplate")
    private RestTemplate loggingRestTemplate;

    // Тесты из HealthControllerTest
    @Test
    void health_ReturnsOk() throws Exception {
        mockMvc.perform(get("/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.service").value("shareit-gateway"));
    }

    // Тесты из HealthControllerCorrectedTest
    @Test
    void detailedHealth_WhenServerReachable_ReturnsUp() throws Exception {
        when(loggingRestTemplate.getForEntity(anyString(), eq(String.class)))
                .thenReturn(ResponseEntity.ok("{\"status\":\"UP\"}"));

        mockMvc.perform(get("/health/detailed"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.overall").value("UP"))
                .andExpect(jsonPath("$.server.reachable").value(true));
    }

    // Тесты из HealthControllerGatewayTest
    @Test
    void detailedHealth_WhenServerUnreachable_ReturnsDegraded() throws Exception {
        when(loggingRestTemplate.getForEntity(anyString(), eq(String.class)))
                .thenThrow(new RuntimeException("Connection refused"));

        mockMvc.perform(get("/health/detailed"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.overall").value("DEGRADED"))
                .andExpect(jsonPath("$.server.reachable").value(false));
    }

    @Test
    void detailedHealth_WhenServerReturns5xx_ReturnsDegraded() throws Exception {
        when(loggingRestTemplate.getForEntity(anyString(), eq(String.class)))
                .thenReturn(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());

        mockMvc.perform(get("/health/detailed"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.overall").value("DEGRADED"))
                .andExpect(jsonPath("$.server.reachable").value(true))
                .andExpect(jsonPath("$.server.status").value("DOWN"));
    }

    @Test
    void detailedHealth_WhenServerReturns4xx_ReturnsDegraded() throws Exception {
        when(loggingRestTemplate.getForEntity(anyString(), eq(String.class)))
                .thenReturn(ResponseEntity.status(HttpStatus.NOT_FOUND).build());

        mockMvc.perform(get("/health/detailed"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.overall").value("DEGRADED"))
                .andExpect(jsonPath("$.server.reachable").value(true))
                .andExpect(jsonPath("$.server.status").value("DOWN"));
    }

    // Тесты из HealthControllerAdditionalTest
    @Test
    void detailedHealth_WhenServerThrowsException_ReturnsDegraded() throws Exception {
        when(loggingRestTemplate.getForEntity(anyString(), eq(String.class)))
                .thenThrow(new RuntimeException("Connection refused"));

        mockMvc.perform(get("/health/detailed"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.overall").value("DEGRADED"))
                .andExpect(jsonPath("$.server.reachable").value(false))
                .andExpect(jsonPath("$.server.status").value("DOWN"));
    }

    // Тесты из HealthControllerExceptionTest
    @Test
    void detailedHealth_WhenServerCompletelyUnreachable_ReturnsDegraded() throws Exception {
        when(loggingRestTemplate.getForEntity(anyString(), eq(String.class)))
                .thenThrow(new RuntimeException("Connection refused: connect"));

        mockMvc.perform(get("/health/detailed"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.overall").value("DEGRADED"))
                .andExpect(jsonPath("$.server.reachable").value(false))
                .andExpect(jsonPath("$.server.status").value("DOWN"));
    }

    @Test
    void detailedHealth_WhenServerReturns4xxException_ReturnsDegraded() throws Exception {
        when(loggingRestTemplate.getForEntity(anyString(), eq(String.class)))
                .thenThrow(org.springframework.web.client.HttpClientErrorException
                        .create(org.springframework.http.HttpStatus.NOT_FOUND, "Not Found", null, null, null));

        mockMvc.perform(get("/health/detailed"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.overall").value("DEGRADED"))
                .andExpect(jsonPath("$.server.reachable").value(false))
                .andExpect(jsonPath("$.server.status").value("DOWN"));
    }

    @Test
    void detailedHealth_WhenServerReturns5xxException_ReturnsDegraded() throws Exception {
        when(loggingRestTemplate.getForEntity(anyString(), eq(String.class)))
                .thenThrow(org.springframework.web.client.HttpServerErrorException
                        .create(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
                                "Internal Server Error", null, null, null));

        mockMvc.perform(get("/health/detailed"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.overall").value("DEGRADED"))
                .andExpect(jsonPath("$.server.reachable").value(false))
                .andExpect(jsonPath("$.server.status").value("DOWN"));
    }

    // Тесты из HealthControllerFullTest
    @Test
    void healthIndicator_ReturnsUp() {
        HealthController controller = new HealthController(loggingRestTemplate);
        var health = controller.health();

        assertEquals("UP", health.getStatus().getCode());
    }

    @Test
    void detailedHealth_WhenServerReturns500_ReturnsDegraded() throws Exception {
        when(loggingRestTemplate.getForEntity(anyString(), eq(String.class)))
                .thenReturn(ResponseEntity.status(500).build());

        mockMvc.perform(get("/health/detailed"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.overall").value("DEGRADED"))
                .andExpect(jsonPath("$.server.reachable").value(true))
                .andExpect(jsonPath("$.server.status").value("DOWN"));
    }
}

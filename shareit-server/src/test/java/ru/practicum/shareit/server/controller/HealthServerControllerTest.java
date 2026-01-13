package ru.practicum.shareit.server.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import javax.sql.DataSource;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(HealthServerController.class)
class HealthServerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DataSource dataSource;

    @MockBean
    private java.sql.Connection connection;

    @Test
    void health_ShouldReturnOk() throws Exception {
        mockMvc.perform(get("/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    void detailedHealth_ShouldReturnOk() throws Exception {
        // Mock the database connection
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.isValid(5)).thenReturn(true);

        mockMvc.perform(get("/health/detailed"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.server.status").value("UP"));
    }

    @Test
    void detailedHealth_ShouldReturnDegraded_WhenDatabaseDown() throws Exception {
        // Mock the database connection failure
        when(dataSource.getConnection()).thenThrow(new java.sql.SQLException("Connection failed"));

        mockMvc.perform(get("/health/detailed"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.overall").value("DEGRADED"))
                .andExpect(jsonPath("$.database.status").value("DOWN"));
    }
}

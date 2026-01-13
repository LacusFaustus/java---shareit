package ru.practicum.shareit.gateway.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.dto.ItemRequestDto;
import ru.practicum.shareit.gateway.client.ItemRequestClient;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ItemRequestController.class)
class ItemRequestControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemRequestClient itemRequestClient;

    // Тесты из ItemRequestControllerTest
    @Test
    void createItemRequest_WithValidData_ReturnsOk() throws Exception {
        ItemRequestDto requestDto = ItemRequestDto.builder()
                .description("Need a power drill")
                .build();

        ItemRequestDto responseDto = ItemRequestDto.builder()
                .id(1L)
                .description("Need a power drill")
                .created(LocalDateTime.now())
                .requestorId(1L)
                .build();

        when(itemRequestClient.createItemRequest(any(ItemRequestDto.class), eq(1L)))
                .thenReturn(ResponseEntity.ok(responseDto));

        mockMvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.description").value("Need a power drill"));
    }

    // Тесты из ItemRequestControllerGatewayTest
    @Test
    void createItemRequest_WithEmptyDescription_ReturnsBadRequest() throws Exception {
        ItemRequestDto requestDto = ItemRequestDto.builder()
                .description("")
                .build();

        mockMvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getUserItemRequests_ReturnsOk() throws Exception {
        when(itemRequestClient.getUserItemRequests(eq(1L)))
                .thenReturn(ResponseEntity.ok("User requests"));

        mockMvc.perform(get("/requests")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk());
    }

    @Test
    void getAllItemRequests_WithDefaultParams_ReturnsOk() throws Exception {
        when(itemRequestClient.getAllItemRequests(eq(1L), eq(0), eq(10)))
                .thenReturn(ResponseEntity.ok("All requests"));

        mockMvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk());
    }

    @Test
    void getAllItemRequests_WithCustomParams_ReturnsOk() throws Exception {
        when(itemRequestClient.getAllItemRequests(eq(1L), eq(10), eq(20)))
                .thenReturn(ResponseEntity.ok("All requests"));

        mockMvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", 1L)
                        .param("from", "10")
                        .param("size", "20"))
                .andExpect(status().isOk());
    }

    @Test
    void getItemRequestById_ReturnsOk() throws Exception {
        when(itemRequestClient.getItemRequestById(eq(1L), eq(1L)))
                .thenReturn(ResponseEntity.ok("Request details"));

        mockMvc.perform(get("/requests/1")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk());
    }
}

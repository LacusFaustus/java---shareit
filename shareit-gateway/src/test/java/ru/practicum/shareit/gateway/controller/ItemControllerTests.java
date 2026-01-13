package ru.practicum.shareit.gateway.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.dto.CommentDto;
import ru.practicum.shareit.dto.ItemDto;
import ru.practicum.shareit.gateway.client.CachedItemClient;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ItemController.class)
class ItemControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CachedItemClient itemClient;

    // Тесты из ItemControllerMockMvcTest
    @Test
    void createItem_WithValidData_ReturnsOk() throws Exception {
        ItemDto requestDto = ItemDto.builder()
                .name("Дрель")
                .description("Аккумуляторная дрель")
                .available(true)
                .build();

        ItemDto responseDto = ItemDto.builder()
                .id(1L)
                .name("Дрель")
                .description("Аккумуляторная дрель")
                .available(true)
                .build();

        when(itemClient.createItem(any(ItemDto.class), eq(1L)))
                .thenReturn(ResponseEntity.ok(responseDto));

        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Дрель"));
    }

    // Тесты из ItemControllerGatewayTest
    @Test
    void createItem_WithInvalidData_ReturnsBadRequest() throws Exception {
        ItemDto invalidDto = ItemDto.builder()
                .name("")
                .description("")
                .available(null)
                .build();

        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void searchItems_WithEmptyText_ReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/items/search")
                        .header("X-Sharer-User-Id", 1L)
                        .param("text", ""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void searchItems_WithBlankText_ReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/items/search")
                        .header("X-Sharer-User-Id", 1L)
                        .param("text", "   "))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void getAllItemsByOwner_WithoutUserId_ReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/items"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addComment_WithEmptyText_ReturnsBadRequest() throws Exception {
        String invalidJson = """
            {
                "text": ""
            }
            """;

        mockMvc.perform(post("/items/1/comment")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    // Тесты из ItemControllerAdditionalTest
    @Test
    void getAllItemsByOwner_WithoutUserId_ReturnsBadRequestWithMessage() throws Exception {
        mockMvc.perform(get("/items"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Missing required header: X-Sharer-User-Id"));
    }

    // Тесты из ItemControllerCompleteTest
    @Test
    void getAllItemsByOwner_WithValidUserId_ReturnsOk() throws Exception {
        when(itemClient.getAllItemsByOwner(anyLong()))
                .thenReturn(ResponseEntity.ok("items"));

        mockMvc.perform(get("/items")
                        .header("X-Sharer-User-Id", "123"))
                .andExpect(status().isOk());
    }

    @Test
    void getItemById_WithValidUserId_ReturnsOk() throws Exception {
        when(itemClient.getItemById(anyLong(), anyLong()))
                .thenReturn(ResponseEntity.ok("item"));

        mockMvc.perform(get("/items/1")
                        .header("X-Sharer-User-Id", "123"))
                .andExpect(status().isOk());
    }

    @Test
    void getItemById_WithoutUserId_ReturnsOk() throws Exception {
        when(itemClient.getItemById(anyLong(), eq(0L)))
                .thenReturn(ResponseEntity.ok("item"));

        mockMvc.perform(get("/items/1"))
                .andExpect(status().isOk());
    }

    // Тесты из ItemControllerFullTest
    @Test
    void updateItem_WithValidData_ReturnsOk() throws Exception {
        ItemDto requestDto = ItemDto.builder()
                .name("Updated Дрель")
                .description("Обновленная дрель")
                .available(true)
                .build();

        when(itemClient.updateItem(eq(1L), any(ItemDto.class), eq(1L)))
                .thenReturn(ResponseEntity.ok(requestDto));

        mockMvc.perform(patch("/items/1")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk());
    }

    @Test
    void searchItems_WithValidText_ReturnsOk() throws Exception {
        when(itemClient.searchItems(anyString(), eq(1L)))
                .thenReturn(ResponseEntity.ok("items"));

        mockMvc.perform(get("/items/search")
                        .header("X-Sharer-User-Id", 1L)
                        .param("text", "drill"))
                .andExpect(status().isOk());
    }

    @Test
    void addComment_WithValidData_ReturnsOk() throws Exception {
        CommentDto commentDto = CommentDto.builder()
                .text("Great item!")
                .build();

        when(itemClient.addComment(eq(1L), any(CommentDto.class), eq(1L)))
                .thenReturn(ResponseEntity.ok("comment added"));

        mockMvc.perform(post("/items/1/comment")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentDto)))
                .andExpect(status().isOk());
    }

    // Тесты из ItemControllerHeaderTest
    @Test
    void getAllItemsByOwner_WithInvalidUserId_ReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/items")
                        .header("X-Sharer-User-Id", "not-a-number"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").isString());
    }

    // Тесты из ItemControllerValidationTest
    @Test
    void getItemById_WithInvalidUserIdType_ReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/items/1")
                        .header("X-Sharer-User-Id", "not-a-number"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invalid parameter value: X-Sharer-User-Id should be of type Long"));
    }
}

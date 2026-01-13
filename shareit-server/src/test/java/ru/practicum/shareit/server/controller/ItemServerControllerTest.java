package ru.practicum.shareit.server.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.dto.*;
import ru.practicum.shareit.server.service.ItemService;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ItemServerController.class)
class ItemServerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemService itemService;

    @Test
    void createItem_ShouldReturnOk() throws Exception {
        ItemDto requestDto = ItemDto.builder()
                .name("Item")
                .description("Description")
                .available(true)
                .build();
        ItemResponseDto responseDto = new ItemResponseDto();

        when(itemService.createItem(any(), anyLong())).thenReturn(responseDto);

        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk());
    }

    @Test
    void updateItem_ShouldReturnOk() throws Exception {
        ItemDto requestDto = ItemDto.builder().name("Updated").build();
        ItemResponseDto responseDto = new ItemResponseDto();

        when(itemService.updateItem(anyLong(), any(), anyLong())).thenReturn(responseDto);

        mockMvc.perform(patch("/items/1")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk());
    }

    @Test
    void getItemById_ShouldReturnOk() throws Exception {
        ItemResponseDto responseDto = new ItemResponseDto();
        when(itemService.getItemById(anyLong(), anyLong())).thenReturn(responseDto);

        mockMvc.perform(get("/items/1")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk());
    }

    @Test
    void getAllItemsByOwner_ShouldReturnOk() throws Exception {
        when(itemService.getAllItemsByOwner(anyLong())).thenReturn(List.of());

        mockMvc.perform(get("/items")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk());
    }

    @Test
    void searchItems_ShouldReturnOk() throws Exception {
        when(itemService.searchItems(anyString(), anyLong())).thenReturn(List.of());

        mockMvc.perform(get("/items/search")
                        .header("X-Sharer-User-Id", 1L)
                        .param("text", "test"))
                .andExpect(status().isOk());
    }

    @Test
    void addComment_ShouldReturnOk() throws Exception {
        CommentDto requestDto = CommentDto.builder().text("Great").build();
        CommentDto responseDto = new CommentDto();

        when(itemService.addComment(anyLong(), any(), anyLong())).thenReturn(responseDto);

        mockMvc.perform(post("/items/1/comment")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk());
    }
}

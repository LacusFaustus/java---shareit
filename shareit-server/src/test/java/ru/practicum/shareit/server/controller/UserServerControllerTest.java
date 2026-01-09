package ru.practicum.shareit.server.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.dto.UserDto;
import ru.practicum.shareit.server.service.UserService;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserServerController.class)
class UserServerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @Test
    void createUser_ShouldReturnCreated() throws Exception {
        UserDto requestDto = UserDto.builder()
                .name("User")
                .email("user@example.com")
                .build();
        UserDto responseDto = UserDto.builder().id(1L).build();

        when(userService.createUser(any())).thenReturn(responseDto);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated());
    }

    @Test
    void updateUser_ShouldReturnOk() throws Exception {
        UserDto requestDto = UserDto.builder().name("Updated").build();
        UserDto responseDto = UserDto.builder().id(1L).build();

        when(userService.updateUser(anyLong(), any())).thenReturn(responseDto);

        mockMvc.perform(patch("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk());
    }

    @Test
    void getUserById_ShouldReturnOk() throws Exception {
        UserDto responseDto = UserDto.builder().id(1L).build();
        when(userService.getUserById(anyLong())).thenReturn(responseDto);

        mockMvc.perform(get("/users/1"))
                .andExpect(status().isOk());
    }

    @Test
    void getAllUsers_ShouldReturnOk() throws Exception {
        when(userService.getAllUsers()).thenReturn(List.of());

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk());
    }

    @Test
    void deleteUser_ShouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/users/1"))
                .andExpect(status().isNoContent());
    }
}

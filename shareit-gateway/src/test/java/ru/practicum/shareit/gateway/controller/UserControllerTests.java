package ru.practicum.shareit.gateway.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.dto.UserDto;
import ru.practicum.shareit.gateway.client.UserClient;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserClient userClient;

    // Тесты из UserControllerMockMvcTest
    @Test
    void createUser_WithValidData_ReturnsOk() throws Exception {
        UserDto requestDto = UserDto.builder()
                .name("John Doe")
                .email("john@example.com")
                .build();

        UserDto responseDto = UserDto.builder()
                .id(1L)
                .name("John Doe")
                .email("john@example.com")
                .build();

        when(userClient.createUser(any(UserDto.class)))
                .thenReturn(ResponseEntity.ok(responseDto));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk());
    }

    // Тесты из UserControllerGatewayTest
    @Test
    void createUser_WithEmptyName_ReturnsBadRequest() throws Exception {
        String invalidJson = """
            {
                "name": "",
                "email": "test@example.com"
            }
            """;

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createUser_WithInvalidEmail_ReturnsBadRequest() throws Exception {
        String invalidJson = """
            {
                "name": "John",
                "email": "invalid-email"
            }
            """;

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createUser_WithoutEmail_ReturnsBadRequest() throws Exception {
        String invalidJson = """
            {
                "name": "John"
            }
            """;

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateUser_WithInvalidData_ReturnsBadRequest() throws Exception {
        String invalidJson = """
            {
                "name": "",
                "email": "invalid"
            }
            """;

        mockMvc.perform(patch("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    // Тесты из UserControllerFullTest
    @Test
    void updateUser_WithValidData_ReturnsOk() throws Exception {
        UserDto requestDto = UserDto.builder()
                .name("Updated Name")
                .email("updated@example.com")
                .build();

        when(userClient.updateUser(eq(1L), any(UserDto.class)))
                .thenReturn(ResponseEntity.ok(requestDto));

        mockMvc.perform(patch("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk());
    }

    @Test
    void getUserById_ReturnsOk() throws Exception {
        when(userClient.getUserById(eq(1L)))
                .thenReturn(ResponseEntity.ok("user"));

        mockMvc.perform(get("/users/1"))
                .andExpect(status().isOk());
    }

    @Test
    void getAllUsers_ReturnsOk() throws Exception {
        when(userClient.getAllUsers())
                .thenReturn(ResponseEntity.ok("users"));

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk());
    }

    @Test
    void deleteUser_ReturnsOk() throws Exception {
        when(userClient.deleteUser(eq(1L)))
                .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(delete("/users/1"))
                .andExpect(status().isOk());
    }
}

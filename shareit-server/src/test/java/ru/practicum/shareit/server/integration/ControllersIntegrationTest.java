package ru.practicum.shareit.server.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.dto.*;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
class ControllersIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createBooking_WithInvalidDates_ReturnsBadRequest() throws Exception {
        // Создаем владельца и арендатора
        UserDto owner = createUser("owner-invalid-dates");
        UserDto booker = createUser("booker-invalid-dates");

        // Создаем вещь
        ItemDto item = ItemDto.builder()
                .name("Test Item")
                .description("Description")
                .available(true)
                .build();

        String itemResponse = mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", owner.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(item)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        Long itemId = objectMapper.readTree(itemResponse).get("id").asLong();

        // Пытаемся создать бронирование с прошедшей датой
        BookingRequestDto invalidBooking = new BookingRequestDto(
                itemId,
                LocalDateTime.now().minusDays(1), // Прошлая дата
                LocalDateTime.now().plusDays(1)
        );

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", booker.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidBooking)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void createItem_WithInvalidData_ReturnsBadRequest() throws Exception {
        UserDto user = createUser("user-invalid-item");

        // Пустое имя
        ItemDto invalidItem = ItemDto.builder()
                .name("") // Пустое имя
                .description("Description")
                .available(true)
                .build();

        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", user.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidItem)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void addComment_ByNonBooker_ReturnsBadRequest() throws Exception {
        // Создаем владельца, арендатора и другого пользователя
        UserDto owner = createUser("owner-comment");
        UserDto booker = createUser("booker-comment");
        UserDto anotherUser = createUser("another-comment");

        // Создаем вещь
        ItemDto item = ItemDto.builder()
                .name("Test Item")
                .description("Description")
                .available(true)
                .build();

        String itemResponse = mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", owner.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(item)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        Long itemId = objectMapper.readTree(itemResponse).get("id").asLong();

        // Создаем бронирование
        BookingRequestDto booking = new BookingRequestDto(
                itemId,
                LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusDays(1)
        );

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", booker.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(booking)))
                .andExpect(status().isCreated());

        // Другой пользователь пытается оставить комментарий
        CommentDto comment = CommentDto.builder()
                .text("Great item!")
                .build();

        mockMvc.perform(post("/items/{itemId}/comment", itemId)
                        .header("X-Sharer-User-Id", anotherUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(comment)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    private UserDto createUser(String prefix) throws Exception {
        String email = prefix + "-" + UUID.randomUUID() + "@example.com";
        UserDto user = UserDto.builder()
                .name(prefix)
                .email(email)
                .build();

        String response = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readValue(response, UserDto.class);
    }
}

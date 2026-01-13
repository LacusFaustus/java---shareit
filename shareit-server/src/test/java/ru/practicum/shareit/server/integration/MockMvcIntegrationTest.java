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
import ru.practicum.shareit.dto.BookingRequestDto;
import ru.practicum.shareit.dto.ItemDto;
import ru.practicum.shareit.dto.UserDto;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
class MockMvcIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testUpdateBookingStatus_WithInvalidUser_ReturnsNotFound() throws Exception {
        // 1. Создаем владельца
        String ownerEmail = "owner-" + UUID.randomUUID() + "@example.com";
        UserDto owner = UserDto.builder()
                .name("Owner")
                .email(ownerEmail)
                .build();

        String ownerResponse = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(owner)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        UserDto createdOwner = objectMapper.readValue(ownerResponse, UserDto.class);

        // 2. Создаем арендатора
        String bookerEmail = "booker-" + UUID.randomUUID() + "@example.com";
        UserDto booker = UserDto.builder()
                .name("Booker")
                .email(bookerEmail)
                .build();

        String bookerResponse = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(booker)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        UserDto createdBooker = objectMapper.readValue(bookerResponse, UserDto.class);

        // 3. Создаем вещь
        ItemDto item = ItemDto.builder()
                .name("Test Item")
                .description("Description for test")
                .available(true)
                .build();

        String itemResponse = mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", createdOwner.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(item)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        ItemDto createdItem = objectMapper.readValue(itemResponse, ItemDto.class);

        // 4. Создаем бронирование
        BookingRequestDto booking = new BookingRequestDto(
                createdItem.getId(),
                LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusDays(1)
        );

        String bookingResponse = mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", createdBooker.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(booking)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // 5. Создаем другого пользователя
        String anotherEmail = "another-" + UUID.randomUUID() + "@example.com";
        UserDto anotherUser = UserDto.builder()
                .name("Another User")
                .email(anotherEmail)
                .build();

        String anotherResponse = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(anotherUser)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        UserDto createdAnother = objectMapper.readValue(anotherResponse, UserDto.class);

        // 6. Пытаемся обновить статус от другого пользователя
        mockMvc.perform(patch("/bookings/1?approved=true")
                        .header("X-Sharer-User-Id", createdAnother.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    void testHealthEndpoints() throws Exception {
        mockMvc.perform(get("/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));

        mockMvc.perform(get("/health/detailed"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.server.status").value("UP"));
    }
}

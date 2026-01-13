package ru.practicum.shareit.server.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.dto.BookingRequestDto;
import ru.practicum.shareit.dto.ItemDto;
import ru.practicum.shareit.dto.UserDto;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Transactional
@ActiveProfiles("test")
class SimpleIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private String baseUrl() {
        return "http://localhost:" + port;
    }

    @Test
    void testCreateAndGetUser() {
        // Создаем пользователя
        String uniqueEmail = "test-" + UUID.randomUUID() + "@example.com";
        UserDto user = UserDto.builder()
                .name("Test User")
                .email(uniqueEmail)
                .build();

        ResponseEntity<UserDto> response = restTemplate.postForEntity(
                baseUrl() + "/users", user, UserDto.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isNotNull();
        assertThat(response.getBody().getEmail()).isEqualTo(uniqueEmail);

        // Получаем пользователя по ID
        Long userId = response.getBody().getId();
        ResponseEntity<UserDto> getResponse = restTemplate.getForEntity(
                baseUrl() + "/users/" + userId, UserDto.class);

        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getResponse.getBody()).isNotNull();
        assertThat(getResponse.getBody().getId()).isEqualTo(userId);
    }

    @Test
    void testCreateItemAndBooking() {
        // 1. Создаем владельца
        String ownerEmail = "owner-" + UUID.randomUUID() + "@example.com";
        UserDto owner = UserDto.builder()
                .name("Owner")
                .email(ownerEmail)
                .build();
        ResponseEntity<UserDto> ownerResponse = restTemplate.postForEntity(
                baseUrl() + "/users", owner, UserDto.class);
        Long ownerId = ownerResponse.getBody().getId();

        // 2. Создаем арендатора
        String bookerEmail = "booker-" + UUID.randomUUID() + "@example.com";
        UserDto booker = UserDto.builder()
                .name("Booker")
                .email(bookerEmail)
                .build();
        ResponseEntity<UserDto> bookerResponse = restTemplate.postForEntity(
                baseUrl() + "/users", booker, UserDto.class);
        Long bookerId = bookerResponse.getBody().getId();

        // 3. Создаем вещь
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Sharer-User-Id", ownerId.toString());

        ItemDto item = ItemDto.builder()
                .name("Test Item")
                .description("Description for test")
                .available(true)
                .build();

        ResponseEntity<ItemDto> itemResponse = restTemplate.postForEntity(
                baseUrl() + "/items",
                new HttpEntity<>(item, headers),
                ItemDto.class);
        Long itemId = itemResponse.getBody().getId();

        // 4. Создаем бронирование
        headers.set("X-Sharer-User-Id", bookerId.toString());
        BookingRequestDto booking = new BookingRequestDto(
                itemId,
                LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusDays(1)
        );

        ResponseEntity<?> bookingResponse = restTemplate.postForEntity(
                baseUrl() + "/bookings",
                new HttpEntity<>(booking, headers),
                Object.class);

        assertThat(bookingResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    @Test
    void testHealthEndpoints() {
        // Проверяем health endpoint
        ResponseEntity<String> healthResponse = restTemplate.getForEntity(
                baseUrl() + "/health", String.class);
        assertThat(healthResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        // Проверяем detailed health endpoint
        ResponseEntity<String> detailedResponse = restTemplate.getForEntity(
                baseUrl() + "/health/detailed", String.class);
        assertThat(detailedResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}

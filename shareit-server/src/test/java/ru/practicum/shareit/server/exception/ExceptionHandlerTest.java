package ru.practicum.shareit.server.exception;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.dto.UserDto;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("unchecked")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Transactional
@ActiveProfiles("test")
class ExceptionHandlerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void handleValidationException_ReturnsBadRequestStatus() {
        String url = "http://localhost:" + port + "/users";

        // Пытаемся создать пользователя с невалидным email
        UserDto invalidUser = UserDto.builder()
                .name("Test User")
                .email("invalid-email") // Некорректный email
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<UserDto> entity = new HttpEntity<>(invalidUser, headers);

        ResponseEntity<Map<String, Object>> response = restTemplate.postForEntity(url, entity, (Class<Map<String, Object>>) (Class<?>) Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).containsKeys("error", "status", "timestamp");
        assertThat(response.getBody().get("error")).asString().contains("Invalid email");
    }

    @Test
    void handleNotFoundException_ReturnsNotFoundStatus() {
        // Попытка получить несуществующего пользователя
        String url = "http://localhost:" + port + "/users/999999";

        ResponseEntity<Map<String, Object>> response = restTemplate.getForEntity(url, (Class<Map<String, Object>>) (Class<?>) Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).containsKeys("error", "status", "timestamp");
    }

    @Test
    void handleConflictException_ReturnsConflictStatus() {
        String url = "http://localhost:" + port + "/users";

        // Создаем пользователя с уникальным email
        String email = "duplicate-" + UUID.randomUUID() + "@example.com";
        UserDto user1 = UserDto.builder()
                .name("User 1")
                .email(email)
                .build();

        ResponseEntity<UserDto> firstResponse = restTemplate.postForEntity(url, user1, UserDto.class);
        assertThat(firstResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        // Пытаемся создать пользователя с тем же email
        UserDto user2 = UserDto.builder()
                .name("User 2")
                .email(email)
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<UserDto> entity = new HttpEntity<>(user2, headers);

        ResponseEntity<Map<String, Object>> response = restTemplate.postForEntity(url, entity, (Class<Map<String, Object>>) (Class<?>) Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).containsKeys("error", "status", "timestamp");
        assertThat(response.getBody().get("error")).asString().contains("Email already exists");
    }
}

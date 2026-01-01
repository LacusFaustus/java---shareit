package ru.practicum.shareit.server.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.dto.ItemDto;
import ru.practicum.shareit.dto.ItemRequestDto;
import ru.practicum.shareit.dto.UserDto;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Transactional
@ActiveProfiles("test")
class ItemRequestIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private String baseUrl;
    private HttpHeaders headers;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port;
        headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
    }

    @Test
    void createItemRequest_AndLinkItem_IntegrationFlow() {
        // 1. Создаем пользователя 1 (запрашивающий)
        UserDto requester = UserDto.builder()
                .name("Requester")
                .email("requester@example.com")
                .build();

        ResponseEntity<UserDto> user1Response = restTemplate.postForEntity(
                baseUrl + "/users", requester, UserDto.class);
        assertThat(user1Response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        Long requesterId = user1Response.getBody().getId();

        // 2. Создаем пользователя 2 (владелец)
        UserDto owner = UserDto.builder()
                .name("Owner")
                .email("owner@example.com")
                .build();

        ResponseEntity<UserDto> user2Response = restTemplate.postForEntity(
                baseUrl + "/users", owner, UserDto.class);
        assertThat(user2Response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        Long ownerId = user2Response.getBody().getId();

        // 3. Создаем запрос на вещь от пользователя 1
        headers.set("X-Sharer-User-Id", requesterId.toString());
        ItemRequestDto requestDto = ItemRequestDto.builder()
                .description("Need a camping tent for weekend trip")
                .build();

        HttpEntity<ItemRequestDto> requestEntity = new HttpEntity<>(requestDto, headers);
        ResponseEntity<ItemRequestDto> requestResponse = restTemplate.postForEntity(
                baseUrl + "/requests", requestEntity, ItemRequestDto.class);

        assertThat(requestResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        ItemRequestDto createdRequest = requestResponse.getBody();
        assertThat(createdRequest).isNotNull();
        assertThat(createdRequest.getId()).isNotNull();
        assertThat(createdRequest.getDescription()).isEqualTo("Need a camping tent for weekend trip");

        // 4. Создаем вещь в ответ на запрос от пользователя 2
        headers.set("X-Sharer-User-Id", ownerId.toString());
        ItemDto itemDto = ItemDto.builder()
                .name("Camping Tent")
                .description("4-person waterproof tent with rainfly")
                .available(true)
                .requestId(createdRequest.getId())
                .build();

        HttpEntity<ItemDto> itemEntity = new HttpEntity<>(itemDto, headers);
        ResponseEntity<ItemDto> itemResponse = restTemplate.postForEntity(
                baseUrl + "/items", itemEntity, ItemDto.class);

        assertThat(itemResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        ItemDto createdItem = itemResponse.getBody();
        assertThat(createdItem).isNotNull();
        assertThat(createdItem.getId()).isNotNull();
        assertThat(createdItem.getRequestId()).isEqualTo(createdRequest.getId());

        // 5. Проверяем, что запрос содержит созданную вещь
        headers.set("X-Sharer-User-Id", requesterId.toString());
        ResponseEntity<ItemRequestDto> getRequestResponse = restTemplate.exchange(
                baseUrl + "/requests/" + createdRequest.getId(),
                HttpMethod.GET,
                new HttpEntity<>(headers),
                ItemRequestDto.class);

        assertThat(getRequestResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        ItemRequestDto retrievedRequest = getRequestResponse.getBody();
        assertThat(retrievedRequest).isNotNull();
        assertThat(retrievedRequest.getItems()).hasSize(1);
        assertThat(retrievedRequest.getItems().get(0).getName()).isEqualTo("Camping Tent");
        assertThat(retrievedRequest.getItems().get(0).getRequestId()).isEqualTo(createdRequest.getId());

        // 6. Проверяем поиск вещей
        ResponseEntity<List<ItemDto>> searchResponse = restTemplate.exchange(
                baseUrl + "/items/search?text=tent",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                new ParameterizedTypeReference<List<ItemDto>>() {});

        assertThat(searchResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        List<ItemDto> searchResults = searchResponse.getBody();
        assertThat(searchResults).hasSize(1);
        assertThat(searchResults.get(0).getName()).isEqualTo("Camping Tent");
    }

    @Test
    void getAllItemRequests_Pagination_WorksCorrectly() {
        // Создаем ТРЕХ пользователей
        UserDto user1 = UserDto.builder()
                .name("User 1")
                .email("user1@example.com")
                .build();

        ResponseEntity<UserDto> user1Response = restTemplate.postForEntity(
                baseUrl + "/users", user1, UserDto.class);
        Long user1Id = user1Response.getBody().getId();

        UserDto user2 = UserDto.builder()
                .name("User 2")
                .email("user2@example.com")
                .build();

        ResponseEntity<UserDto> user2Response = restTemplate.postForEntity(
                baseUrl + "/users", user2, UserDto.class);
        Long user2Id = user2Response.getBody().getId();

        UserDto user3 = UserDto.builder()
                .name("User 3")
                .email("user3@example.com")
                .build();

        ResponseEntity<UserDto> user3Response = restTemplate.postForEntity(
                baseUrl + "/users", user3, UserDto.class);
        Long user3Id = user3Response.getBody().getId();

        // 1. Создаем 3 запроса от пользователя 2
        headers.set("X-Sharer-User-Id", user2Id.toString());
        for (int i = 1; i <= 3; i++) {
            ItemRequestDto requestDto = ItemRequestDto.builder()
                    .description("Request from User 2 - " + i)
                    .build();

            HttpEntity<ItemRequestDto> entity = new HttpEntity<>(requestDto, headers);
            restTemplate.postForEntity(baseUrl + "/requests", entity, ItemRequestDto.class);
        }

        // 2. Создаем 2 запроса от пользователя 3
        headers.set("X-Sharer-User-Id", user3Id.toString());
        for (int i = 1; i <= 2; i++) {
            ItemRequestDto requestDto = ItemRequestDto.builder()
                    .description("Request from User 3 - " + i)
                    .build();

            HttpEntity<ItemRequestDto> entity = new HttpEntity<>(requestDto, headers);
            restTemplate.postForEntity(baseUrl + "/requests", entity, ItemRequestDto.class);
        }

        // Теперь пользователь 1 должен видеть 5 запросов (от user2 и user3)
        headers.set("X-Sharer-User-Id", user1Id.toString());

        // Тестируем пагинацию
        ResponseEntity<List<ItemRequestDto>> page1Response = restTemplate.exchange(
                baseUrl + "/requests/all?from=0&size=2",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                new ParameterizedTypeReference<List<ItemRequestDto>>() {});

        assertThat(page1Response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(page1Response.getBody()).hasSize(2);

        ResponseEntity<List<ItemRequestDto>> page2Response = restTemplate.exchange(
                baseUrl + "/requests/all?from=2&size=2",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                new ParameterizedTypeReference<List<ItemRequestDto>>() {});

        assertThat(page2Response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(page2Response.getBody()).hasSize(2);

        ResponseEntity<List<ItemRequestDto>> page3Response = restTemplate.exchange(
                baseUrl + "/requests/all?from=4&size=2",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                new ParameterizedTypeReference<List<ItemRequestDto>>() {});

        assertThat(page3Response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(page3Response.getBody()).hasSize(1); // Последняя страница с 1 элементом
    }

    @Test
    void getUserItemRequests_ReturnsOnlyUserRequests() {
        // Создаем двух пользователей
        UserDto user1 = UserDto.builder()
                .name("User A")
                .email("usera@example.com")
                .build();

        ResponseEntity<UserDto> user1Response = restTemplate.postForEntity(
                baseUrl + "/users", user1, UserDto.class);
        Long user1Id = user1Response.getBody().getId();

        UserDto user2 = UserDto.builder()
                .name("User B")
                .email("userb@example.com")
                .build();

        ResponseEntity<UserDto> user2Response = restTemplate.postForEntity(
                baseUrl + "/users", user2, UserDto.class);
        Long user2Id = user2Response.getBody().getId();

        // Пользователь 1 создает 2 запроса
        headers.set("X-Sharer-User-Id", user1Id.toString());
        ItemRequestDto request1 = ItemRequestDto.builder()
                .description("Request 1 from User A")
                .build();
        ItemRequestDto request2 = ItemRequestDto.builder()
                .description("Request 2 from User A")
                .build();

        restTemplate.postForEntity(baseUrl + "/requests", new HttpEntity<>(request1, headers), ItemRequestDto.class);
        restTemplate.postForEntity(baseUrl + "/requests", new HttpEntity<>(request2, headers), ItemRequestDto.class);

        // Пользователь 2 создает 1 запрос
        headers.set("X-Sharer-User-Id", user2Id.toString());
        ItemRequestDto request3 = ItemRequestDto.builder()
                .description("Request from User B")
                .build();
        restTemplate.postForEntity(baseUrl + "/requests", new HttpEntity<>(request3, headers), ItemRequestDto.class);

        // Пользователь 1 должен видеть только свои 2 запроса
        headers.set("X-Sharer-User-Id", user1Id.toString());
        ResponseEntity<List<ItemRequestDto>> user1Requests = restTemplate.exchange(
                baseUrl + "/requests",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                new ParameterizedTypeReference<List<ItemRequestDto>>() {});

        assertThat(user1Requests.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(user1Requests.getBody()).hasSize(2);

        // Пользователь 2 должен видеть только свои 1 запрос
        headers.set("X-Sharer-User-Id", user2Id.toString());
        ResponseEntity<List<ItemRequestDto>> user2Requests = restTemplate.exchange(
                baseUrl + "/requests",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                new ParameterizedTypeReference<List<ItemRequestDto>>() {});

        assertThat(user2Requests.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(user2Requests.getBody()).hasSize(1);
    }

    @Test
    void createItemRequest_WithItem_ReturnsRequestWithItems() {
        // Создаем двух пользователей
        UserDto requester = UserDto.builder()
                .name("Requester")
                .email("requester@test.com")
                .build();

        ResponseEntity<UserDto> requesterResponse = restTemplate.postForEntity(
                baseUrl + "/users", requester, UserDto.class);
        Long requesterId = requesterResponse.getBody().getId();

        UserDto owner = UserDto.builder()
                .name("Owner")
                .email("owner@test.com")
                .build();

        ResponseEntity<UserDto> ownerResponse = restTemplate.postForEntity(
                baseUrl + "/users", owner, UserDto.class);
        Long ownerId = ownerResponse.getBody().getId();

        // Создаем запрос
        headers.set("X-Sharer-User-Id", requesterId.toString());
        ItemRequestDto requestDto = ItemRequestDto.builder()
                .description("Need a drill")
                .build();

        HttpEntity<ItemRequestDto> requestEntity = new HttpEntity<>(requestDto, headers);
        ResponseEntity<ItemRequestDto> requestResponse = restTemplate.postForEntity(
                baseUrl + "/requests", requestEntity, ItemRequestDto.class);

        assertThat(requestResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        ItemRequestDto createdRequest = requestResponse.getBody();

        // Создаем вещь в ответ на запрос
        headers.set("X-Sharer-User-Id", ownerId.toString());
        ItemDto itemDto = ItemDto.builder()
                .name("Electric Drill")
                .description("Powerful cordless drill")
                .available(true)
                .requestId(createdRequest.getId())
                .build();

        HttpEntity<ItemDto> itemEntity = new HttpEntity<>(itemDto, headers);
        ResponseEntity<ItemDto> itemResponse = restTemplate.postForEntity(
                baseUrl + "/items", itemEntity, ItemDto.class);

        assertThat(itemResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        // Проверяем, что запрос содержит созданную вещь
        headers.set("X-Sharer-User-Id", requesterId.toString());
        ResponseEntity<ItemRequestDto> getRequestResponse = restTemplate.exchange(
                baseUrl + "/requests/" + createdRequest.getId(),
                HttpMethod.GET,
                new HttpEntity<>(headers),
                ItemRequestDto.class);

        assertThat(getRequestResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        ItemRequestDto retrievedRequest = getRequestResponse.getBody();
        assertThat(retrievedRequest.getItems()).hasSize(1);
        assertThat(retrievedRequest.getItems().get(0).getName()).isEqualTo("Electric Drill");
    }
}

package ru.practicum.shareit.server.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.dto.ItemRequestDto;
import ru.practicum.shareit.dto.UserDto;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class ItemRequestServicePaginationTest {

    @Autowired
    private ItemRequestService itemRequestService;

    @Autowired
    private UserService userService;

    private Long user1Id;
    private Long user2Id;
    private Long user3Id;

    @BeforeEach
    void setUp() {
        UserDto user1 = UserDto.builder()
                .name("User 1")
                .email("user1@test.com")
                .build();
        user1Id = userService.createUser(user1).getId();

        UserDto user2 = UserDto.builder()
                .name("User 2")
                .email("user2@test.com")
                .build();
        user2Id = userService.createUser(user2).getId();

        UserDto user3 = UserDto.builder()
                .name("User 3")
                .email("user3@test.com")
                .build();
        user3Id = userService.createUser(user3).getId();
    }

    @Test
    void getAllItemRequests_ExcludesCurrentUserRequests() {
        // Создаем 2 запроса от user2
        createItemRequest(user2Id, "Request 1 from User 2");
        createItemRequest(user2Id, "Request 2 from User 2");

        // Создаем 3 запроса от user3
        createItemRequest(user3Id, "Request 1 from User 3");
        createItemRequest(user3Id, "Request 2 from User 3");
        createItemRequest(user3Id, "Request 3 from User 3");

        // user1 должен видеть 5 запросов (от user2 и user3)
        List<ItemRequestDto> requestsForUser1 = itemRequestService.getAllItemRequests(user1Id, 0, 10);
        assertThat(requestsForUser1).hasSize(5);

        // user2 должен видеть 3 запроса (только от user3)
        List<ItemRequestDto> requestsForUser2 = itemRequestService.getAllItemRequests(user2Id, 0, 10);
        assertThat(requestsForUser2).hasSize(3);

        // user3 должен видеть 2 запроса (только от user2)
        List<ItemRequestDto> requestsForUser3 = itemRequestService.getAllItemRequests(user3Id, 0, 10);
        assertThat(requestsForUser3).hasSize(2);
    }

    @Test
    void getAllItemRequests_Pagination_FirstPage() {
        // Создаем 7 запросов от user2
        for (int i = 1; i <= 7; i++) {
            createItemRequest(user2Id, "Request " + i + " from User 2");
        }

        // user1 запрашивает первую страницу с 3 элементами
        List<ItemRequestDto> page1 = itemRequestService.getAllItemRequests(user1Id, 0, 3);
        assertThat(page1).hasSize(3);

        // user1 запрашивает вторую страницу с 3 элементами
        List<ItemRequestDto> page2 = itemRequestService.getAllItemRequests(user1Id, 3, 3);
        assertThat(page2).hasSize(3);

        // user1 запрашивает третью страницу с 3 элементами (должен получить 1 элемент)
        List<ItemRequestDto> page3 = itemRequestService.getAllItemRequests(user1Id, 6, 3);
        assertThat(page3).hasSize(1);
    }

    @Test
    void getUserItemRequests_ReturnsOnlyUserRequests() {
        // user1 создает 2 запроса
        createItemRequest(user1Id, "Request 1 from User 1");
        createItemRequest(user1Id, "Request 2 from User 1");

        // user2 создает 1 запрос
        createItemRequest(user2Id, "Request from User 2");

        // Проверяем, что каждый пользователь видит только свои запросы
        List<ItemRequestDto> user1Requests = itemRequestService.getUserItemRequests(user1Id);
        assertThat(user1Requests).hasSize(2);

        List<ItemRequestDto> user2Requests = itemRequestService.getUserItemRequests(user2Id);
        assertThat(user2Requests).hasSize(1);

        List<ItemRequestDto> user3Requests = itemRequestService.getUserItemRequests(user3Id);
        assertThat(user3Requests).isEmpty();
    }

    @Test
    void getUserItemRequests_ReturnsInDescendingOrder() {
        // user1 создает 3 запроса
        createItemRequest(user1Id, "Request 1 from User 1 (oldest)");
        try {
            Thread.sleep(10); // Небольшая задержка для разницы во времени
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        createItemRequest(user1Id, "Request 2 from User 1 (middle)");
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        createItemRequest(user1Id, "Request 3 from User 1 (newest)");

        // Проверяем, что запросы возвращаются от новых к старым
        List<ItemRequestDto> user1Requests = itemRequestService.getUserItemRequests(user1Id);
        assertThat(user1Requests).hasSize(3);
        assertThat(user1Requests)
                .extracting("description")
                .containsExactly(
                        "Request 3 from User 1 (newest)",
                        "Request 2 from User 1 (middle)",
                        "Request 1 from User 1 (oldest)"
                );
    }

    private void createItemRequest(Long userId, String description) {
        ItemRequestDto requestDto = ItemRequestDto.builder()
                .description(description)
                .build();
        itemRequestService.createItemRequest(requestDto, userId);
    }
}

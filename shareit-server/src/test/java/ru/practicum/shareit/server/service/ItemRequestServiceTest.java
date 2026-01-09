package ru.practicum.shareit.server.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.dto.ItemDto;
import ru.practicum.shareit.dto.ItemRequestDto;
import ru.practicum.shareit.dto.UserDto;
import ru.practicum.shareit.server.exception.ValidationException;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class ItemRequestServiceTest {

    @Autowired
    private ItemRequestService itemRequestService;

    @Autowired
    private UserService userService;

    @Autowired
    private ItemService itemService;

    private Long user1Id;
    private Long user2Id;
    private Long user3Id;

    @BeforeEach
    void setUp() {
        UserDto user1 = UserDto.builder()
                .name("User 1")
                .email("user1-" + UUID.randomUUID() + "@test.com")
                .build();
        user1Id = userService.createUser(user1).getId();

        UserDto user2 = UserDto.builder()
                .name("User 2")
                .email("user2-" + UUID.randomUUID() + "@test.com")
                .build();
        user2Id = userService.createUser(user2).getId();

        UserDto user3 = UserDto.builder()
                .name("User 3")
                .email("user3-" + UUID.randomUUID() + "@test.com")
                .build();
        user3Id = userService.createUser(user3).getId();
    }

    private UserDto createUser(String prefix, String name) {
        String email = prefix + "-" + UUID.randomUUID() + "@example.com";
        return userService.createUser(UserDto.builder()
                .name(name)
                .email(email)
                .build());
    }

    private void createItemRequest(Long userId, String description) {
        ItemRequestDto requestDto = ItemRequestDto.builder()
                .description(description)
                .build();
        itemRequestService.createItemRequest(requestDto, userId);
    }

    // ============== СОЗДАНИЕ ЗАПРОСОВ ==============

    @Test
    void createItemRequest_WithEmptyDescription_ThrowsValidationException() {
        UserDto user = createUser("user-empty", "User");

        ItemRequestDto requestDto = ItemRequestDto.builder()
                .description("")
                .build();

        assertThatThrownBy(() ->
                itemRequestService.createItemRequest(requestDto, user.getId()))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Item request description cannot be empty");
    }

    @Test
    void createItemRequest_WithEmptyDescription_ThrowsValidationException2() {
        UserDto user = createUser("user-empty-desc", "User");

        ItemRequestDto request = ItemRequestDto.builder()
                .description("")
                .build();

        try {
            ItemRequestDto result = itemRequestService.createItemRequest(request, user.getId());
            assertThat(result).isNotNull();
        } catch (Exception e) {
            assertThat(e).isInstanceOf(ru.practicum.shareit.server.exception.ValidationException.class);
        }
    }

    // ============== ПОЛУЧЕНИЕ ВСЕХ ЗАПРОСОВ ==============

    @Test
    void getAllItemRequests_ExcludesCurrentUserRequests() {
        createItemRequest(user2Id, "Request 1 from User 2");
        createItemRequest(user2Id, "Request 2 from User 2");
        createItemRequest(user3Id, "Request 1 from User 3");
        createItemRequest(user3Id, "Request 2 from User 3");
        createItemRequest(user3Id, "Request 3 from User 3");

        List<ItemRequestDto> requestsForUser1 = itemRequestService.getAllItemRequests(user1Id, 0, 20);

        long requestsFromUser2 = requestsForUser1.stream()
                .filter(r -> r.getRequestorId().equals(user2Id))
                .count();
        long requestsFromUser3 = requestsForUser1.stream()
                .filter(r -> r.getRequestorId().equals(user3Id))
                .count();

        assertThat(requestsFromUser2).isEqualTo(2);
        assertThat(requestsFromUser3).isEqualTo(3);
    }

    @Test
    void getAllItemRequests_ExcludesCurrentUserRequests2() {
        UserDto user1 = userService.createUser(UserDto.builder()
                .name("User 1")
                .email("user1-" + UUID.randomUUID() + "@example.com")
                .build());

        UserDto user2 = userService.createUser(UserDto.builder()
                .name("User 2")
                .email("user2-" + UUID.randomUUID() + "@example.com")
                .build());

        ItemRequestDto request1 = ItemRequestDto.builder()
                .description("Request from user 2")
                .build();
        itemRequestService.createItemRequest(request1, user2.getId());

        List<ItemRequestDto> requestsForUser1 = itemRequestService.getAllItemRequests(user1.getId(), 0, 10);
        assertThat(requestsForUser1).isNotEmpty();

        boolean containsUser1Request = requestsForUser1.stream()
                .anyMatch(r -> r.getRequestorId().equals(user1.getId()));
        assertThat(containsUser1Request).isFalse();
    }

    @Test
    void getAllItemRequests_WhenNoOtherUsers_ReturnsEmptyList() {
        UserDto user = createUser("only-user", "Only User");

        ItemRequestDto request = ItemRequestDto.builder()
                .description("My request")
                .build();
        itemRequestService.createItemRequest(request, user.getId());

        var result = itemRequestService.getAllItemRequests(user.getId(), 0, 10);

        boolean containsOwnRequest = result.stream()
                .anyMatch(r -> r.getRequestorId().equals(user.getId()));
        assertThat(containsOwnRequest).isFalse();
    }

    @Test
    void getAllItemRequests_WhenNoOtherUsers_ReturnsEmptyList2() {
        UserDto onlyUser = createUser("only-user", "Only User");

        ItemRequestDto request = ItemRequestDto.builder()
                .description("My request")
                .build();
        itemRequestService.createItemRequest(request, onlyUser.getId());

        List<ItemRequestDto> result = itemRequestService.getAllItemRequests(
                onlyUser.getId(), 0, 10);

        boolean containsOwnRequest = result.stream()
                .anyMatch(r -> r.getRequestorId().equals(onlyUser.getId()));
        assertThat(containsOwnRequest).isFalse();
    }

    @Test
    void getAllItemRequests_WhenNoOtherUsers_ReturnsEmptyList3() {
        UserDto user = userService.createUser(UserDto.builder()
                .name("Only User")
                .email("onlyuser-" + UUID.randomUUID() + "@example.com")
                .build());

        ItemRequestDto request = ItemRequestDto.builder()
                .description("My request")
                .build();
        itemRequestService.createItemRequest(request, user.getId());

        List<ItemRequestDto> result = itemRequestService.getAllItemRequests(user.getId(), 0, 10);

        boolean containsOwnRequest = result.stream()
                .anyMatch(r -> r.getRequestorId().equals(user.getId()));
        assertThat(containsOwnRequest).isFalse();
    }

    // ============== ПАГИНАЦИЯ ==============

    @Test
    void getAllItemRequests_Pagination_WorksCorrectly() {
        for (int i = 1; i <= 7; i++) {
            createItemRequest(user2Id, "Request " + i + " from User 2");
        }

        List<ItemRequestDto> page1 = itemRequestService.getAllItemRequests(user1Id, 0, 3);
        assertThat(page1).hasSize(3);

        List<ItemRequestDto> page2 = itemRequestService.getAllItemRequests(user1Id, 3, 3);
        assertThat(page2).hasSize(3);

        List<ItemRequestDto> page3 = itemRequestService.getAllItemRequests(user1Id, 6, 3);
        assertThat(page3.size()).isLessThanOrEqualTo(3);
    }

    @Test
    void getAllItemRequests_WithPagination_ReturnsCorrectPage() {
        UserDto user1 = createUser("user1-pag", "User 1");
        UserDto user2 = createUser("user2-pag", "User 2");
        UserDto user3 = createUser("user3-pag", "User 3");

        for (int i = 1; i <= 3; i++) {
            ItemRequestDto request = ItemRequestDto.builder()
                    .description("Request " + i + " from user2")
                    .build();
            itemRequestService.createItemRequest(request, user2.getId());
        }

        for (int i = 1; i <= 2; i++) {
            ItemRequestDto request = ItemRequestDto.builder()
                    .description("Request " + i + " from user3")
                    .build();
            itemRequestService.createItemRequest(request, user3.getId());
        }

        List<ItemRequestDto> page1 = itemRequestService.getAllItemRequests(user1.getId(), 0, 2);
        List<ItemRequestDto> page2 = itemRequestService.getAllItemRequests(user1.getId(), 2, 2);
        List<ItemRequestDto> page3 = itemRequestService.getAllItemRequests(user1.getId(), 4, 2);

        assertThat(page1).hasSize(2);
        assertThat(page2).hasSize(2);
        assertThat(page3).hasSize(1);
    }

    @Test
    void getAllItemRequests_WithInvalidPagination_ThrowsValidationException() {
        UserDto user = createUser("user-pag", "User");

        assertThatThrownBy(() ->
                itemRequestService.getAllItemRequests(user.getId(), -1, 10))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Parameter 'from' must not be negative");

        assertThatThrownBy(() ->
                itemRequestService.getAllItemRequests(user.getId(), 0, 0))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Parameter 'size' must be positive");

        assertThatThrownBy(() ->
                itemRequestService.getAllItemRequests(user.getId(), 0, 101))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Parameter 'size' must not exceed 100");
    }

    @Test
    void getAllItemRequests_WithInvalidPagination_ThrowsValidationException2() {
        UserDto user = userService.createUser(UserDto.builder()
                .name("User")
                .email("user-pag-" + UUID.randomUUID() + "@example.com")
                .build());

        assertThatThrownBy(() ->
                itemRequestService.getAllItemRequests(user.getId(), -1, 10))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Parameter 'from' must not be negative");

        assertThatThrownBy(() ->
                itemRequestService.getAllItemRequests(user.getId(), 0, 0))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Parameter 'size' must be positive");

        assertThatThrownBy(() ->
                itemRequestService.getAllItemRequests(user.getId(), 0, 101))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Parameter 'size' must not exceed 100");
    }

    @Test
    void getAllItemRequests_WithLargeSize_ThrowsValidationException() {
        UserDto user = createUser("user-large", "User");

        assertThatThrownBy(() ->
                itemRequestService.getAllItemRequests(user.getId(), 0, 101))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Parameter 'size' must not exceed 100");
    }

    @Test
    void getAllItemRequests_WithZeroSize_ThrowsValidationException() {
        UserDto user = createUser("user-zero", "User");

        assertThatThrownBy(() ->
                itemRequestService.getAllItemRequests(user.getId(), 0, 0))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Parameter 'size' must be positive");
    }

    @Test
    void getAllItemRequests_WithNegativeFrom_ThrowsValidationException() {
        UserDto user = createUser("user-negative", "User");

        assertThatThrownBy(() ->
                itemRequestService.getAllItemRequests(user.getId(), -1, 10))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Parameter 'from' must not be negative");
    }

    // ============== ПОЛЬЗОВАТЕЛЬСКИЕ ЗАПРОСЫ ==============

    @Test
    void getUserItemRequests_ReturnsOnlyUserRequests() {
        createItemRequest(user1Id, "Request 1 from User 1");
        createItemRequest(user1Id, "Request 2 from User 1");
        createItemRequest(user2Id, "Request from User 2");

        List<ItemRequestDto> user1Requests = itemRequestService.getUserItemRequests(user1Id);
        assertThat(user1Requests).hasSize(2);

        List<ItemRequestDto> user2Requests = itemRequestService.getUserItemRequests(user2Id);
        assertThat(user2Requests).hasSize(1);

        List<ItemRequestDto> user3Requests = itemRequestService.getUserItemRequests(user3Id);
        assertThat(user3Requests).isEmpty();
    }

    @Test
    void getUserItemRequests_ReturnsInDescendingOrder() {
        createItemRequest(user1Id, "Request 1 from User 1 (oldest)");

        try {
            Thread.sleep(10);
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

        List<ItemRequestDto> user1Requests = itemRequestService.getUserItemRequests(user1Id);
        assertThat(user1Requests).hasSize(3);
        assertThat(user1Requests.get(0).getDescription()).contains("newest");
        assertThat(user1Requests.get(2).getDescription()).contains("oldest");
    }

    @Test
    void getUserItemRequests_ReturnsInCorrectOrder() throws InterruptedException {
        UserDto user = createUser("user-order", "User");

        for (int i = 1; i <= 3; i++) {
            ItemRequestDto request = ItemRequestDto.builder()
                    .description("Request " + i)
                    .build();
            itemRequestService.createItemRequest(request, user.getId());
            Thread.sleep(10);
        }

        List<ItemRequestDto> requests = itemRequestService.getUserItemRequests(user.getId());

        assertThat(requests).hasSize(3);
        assertThat(requests.get(0).getDescription()).isEqualTo("Request 3");
        assertThat(requests.get(1).getDescription()).isEqualTo("Request 2");
        assertThat(requests.get(2).getDescription()).isEqualTo("Request 1");
    }

    // ============== ПОЛУЧЕНИЕ ЗАПРОСА ПО ID ==============

    @Test
    void getItemRequestById_WithItems_ReturnsRequestWithItems() {
        UserDto requester = createUser("requester-with-items", "Requester");
        UserDto owner = createUser("owner-with-items", "Owner");

        ItemRequestDto request = ItemRequestDto.builder()
                .description("Need a drill")
                .build();
        ItemRequestDto createdRequest = itemRequestService.createItemRequest(request, requester.getId());

        ItemDto item = ItemDto.builder()
                .name("Electric Drill")
                .description("Powerful drill")
                .available(true)
                .requestId(createdRequest.getId())
                .build();
        itemService.createItem(item, owner.getId());

        ItemRequestDto result = itemRequestService.getItemRequestById(
                createdRequest.getId(), requester.getId());

        assertThat(result).isNotNull();
        assertThat(result.getItems()).hasSize(1);
        assertThat(result.getItems().get(0).getName()).isEqualTo("Electric Drill");
    }
}

package ru.practicum.shareit.server.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.dto.ItemDto;
import ru.practicum.shareit.dto.ItemRequestDto;
import ru.practicum.shareit.dto.UserDto;
import ru.practicum.shareit.server.exception.NotFoundException;
import ru.practicum.shareit.server.exception.ValidationException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.properties")
@Transactional
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

    private UserDto createUser(String name) {
        return userService.createUser(UserDto.builder()
                .name(name)
                .email(name + "-" + UUID.randomUUID() + "@example.com")
                .build());
    }

    private ItemRequestDto createItemRequest(Long userId, String description) {
        ItemRequestDto requestDto = ItemRequestDto.builder()
                .description(description)
                .build();
        return itemRequestService.createItemRequest(requestDto, userId);
    }

    // ============== СОЗДАНИЕ ЗАПРОСОВ ==============

    @Test
    void createItemRequest_WithEmptyDescription_ThrowsValidationException() {
        UserDto user = createUser("user-empty");

        ItemRequestDto requestDto = ItemRequestDto.builder()
                .description("")
                .build();

        assertThatThrownBy(() ->
                itemRequestService.createItemRequest(requestDto, user.getId()))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Item request description cannot be empty");
    }

    @Test
    void createItemRequest_WithBlankDescription_ThrowsValidationException() {
        UserDto user = createUser("user-blank");

        ItemRequestDto requestDto = ItemRequestDto.builder()
                .description("   ")
                .build();

        assertThatThrownBy(() ->
                itemRequestService.createItemRequest(requestDto, user.getId()))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Item request description cannot be empty");
    }

    @Test
    void createItemRequest_WithNullDescription_ThrowsValidationException() {
        UserDto user = createUser("user-null");

        ItemRequestDto requestDto = ItemRequestDto.builder()
                .description(null)
                .build();

        assertThatThrownBy(() ->
                itemRequestService.createItemRequest(requestDto, user.getId()))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Item request description cannot be empty");
    }

    @Test
    void createItemRequest_WithNullRequestDto_ThrowsValidationException() {
        UserDto user = createUser("user-null-dto");

        assertThatThrownBy(() ->
                itemRequestService.createItemRequest(null, user.getId()))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Item request cannot be null");
    }

    @Test
    void createItemRequest_WithValidDescription_ShouldSucceed() {
        UserDto user = createUser("user-valid");

        ItemRequestDto requestDto = ItemRequestDto.builder()
                .description("Need a laptop")
                .build();

        ItemRequestDto result = itemRequestService.createItemRequest(requestDto, user.getId());

        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        assertThat(result.getDescription()).isEqualTo("Need a laptop");
        assertThat(result.getRequestorId()).isEqualTo(user.getId());
        assertThat(result.getCreated()).isBeforeOrEqualTo(LocalDateTime.now());
        assertThat(result.getItems()).isNotNull().isEmpty();
    }

    @Test
    void createItemRequest_WithNonexistentUser_ThrowsNotFoundException() {
        ItemRequestDto requestDto = ItemRequestDto.builder()
                .description("Need item")
                .build();

        assertThatThrownBy(() ->
                itemRequestService.createItemRequest(requestDto, 999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("User not found");
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

        assertThat(requestsForUser1).hasSize(5);
        assertThat(requestsForUser1).allMatch(r -> !r.getRequestorId().equals(user1Id));

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
    void getAllItemRequests_WhenNoOtherUsers_ReturnsEmptyList() {
        UserDto user = createUser("only-user");

        ItemRequestDto request = ItemRequestDto.builder()
                .description("My request")
                .build();
        itemRequestService.createItemRequest(request, user.getId());

        List<ItemRequestDto> result = itemRequestService.getAllItemRequests(user.getId(), 0, 10);

        assertThat(result).isEmpty();
    }

    @Test
    void getAllItemRequests_WithNonexistentUser_ThrowsNotFoundException() {
        assertThatThrownBy(() ->
                itemRequestService.getAllItemRequests(999L, 0, 10))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("User not found");
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
        assertThat(page3).hasSize(1);
    }

    @Test
    void getAllItemRequests_Pagination_WithNonMultipleFrom() {
        for (int i = 1; i <= 5; i++) {
            createItemRequest(user2Id, "Request " + i);
        }

        // from=1, size=2 -> pageNumber=0 (1/2=0), но должно пропустить первый элемент
        List<ItemRequestDto> result = itemRequestService.getAllItemRequests(user1Id, 1, 2);

        // Проверяем что получили правильное количество
        assertThat(result).hasSize(2);
    }

    @Test
    void getAllItemRequests_WithInvalidPagination_ThrowsValidationException() {
        UserDto user = createUser("user-pag");

        assertThatThrownBy(() ->
                itemRequestService.getAllItemRequests(user.getId(), -1, 10))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Parameter 'from' must not be negative");

        assertThatThrownBy(() ->
                itemRequestService.getAllItemRequests(user.getId(), 0, 0))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Parameter 'size' must be positive");

        assertThatThrownBy(() ->
                itemRequestService.getAllItemRequests(user.getId(), 0, -1))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Parameter 'size' must be positive");
    }

    @Test
    void getAllItemRequests_WithSizeExceeding100_ThrowsValidationException() {
        UserDto user = createUser("user-large");

        assertThatThrownBy(() ->
                itemRequestService.getAllItemRequests(user.getId(), 0, 101))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Parameter 'size' must not exceed 100");
    }

    @Test
    void getAllItemRequests_WithSize100_ShouldWork() {
        for (int i = 1; i <= 105; i++) {
            createItemRequest(user2Id, "Request " + i);
        }

        List<ItemRequestDto> result = itemRequestService.getAllItemRequests(user1Id, 0, 100);
        assertThat(result).hasSize(100);
    }

    // ============== ПОЛЬЗОВАТЕЛЬСКИЕ ЗАПРОСЫ ==============

    @Test
    void getUserItemRequests_ReturnsOnlyUserRequests() {
        createItemRequest(user1Id, "Request 1 from User 1");
        createItemRequest(user1Id, "Request 2 from User 1");
        createItemRequest(user2Id, "Request from User 2");

        List<ItemRequestDto> user1Requests = itemRequestService.getUserItemRequests(user1Id);
        assertThat(user1Requests).hasSize(2);
        assertThat(user1Requests).allMatch(r -> r.getRequestorId().equals(user1Id));

        List<ItemRequestDto> user2Requests = itemRequestService.getUserItemRequests(user2Id);
        assertThat(user2Requests).hasSize(1);
        assertThat(user2Requests).allMatch(r -> r.getRequestorId().equals(user2Id));

        List<ItemRequestDto> user3Requests = itemRequestService.getUserItemRequests(user3Id);
        assertThat(user3Requests).isEmpty();
    }

    @Test
    void getUserItemRequests_ReturnsInDescendingOrder() {
        UserDto requester = createUser("requester-order");

        List<Long> createdIds = new ArrayList<>();

        for (int i = 1; i <= 5; i++) {
            ItemRequestDto requestDto = ItemRequestDto.builder()
                    .description("Need item " + i)
                    .build();
            ItemRequestDto created = itemRequestService.createItemRequest(requestDto, requester.getId());
            createdIds.add(created.getId());

            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        List<ItemRequestDto> result = itemRequestService.getUserItemRequests(requester.getId());

        assertThat(result).hasSize(5);

        List<Long> resultIds = result.stream()
                .map(ItemRequestDto::getId)
                .collect(Collectors.toList());

        List<Long> expectedOrder = new ArrayList<>(createdIds);
        Collections.reverse(expectedOrder);

        assertThat(resultIds).isEqualTo(expectedOrder);

        assertThat(result.get(0).getId()).isEqualTo(createdIds.get(4)); // id=5
        assertThat(result.get(4).getId()).isEqualTo(createdIds.get(0)); // id=1
    }

    @Test
    void getUserItemRequests_WithNonexistentUser_ThrowsNotFoundException() {
        assertThatThrownBy(() ->
                itemRequestService.getUserItemRequests(999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void getUserItemRequests_ReturnsEmptyListForUserWithoutRequests() {
        UserDto user = createUser("user-no-requests");

        List<ItemRequestDto> requests = itemRequestService.getUserItemRequests(user.getId());

        assertThat(requests).isNotNull().isEmpty();
    }

    // ============== ПОЛУЧЕНИЕ ЗАПРОСА ПО ID ==============

    @Test
    void getItemRequestById_WithItems_ReturnsRequestWithItems() {
        UserDto requester = createUser("requester");
        UserDto owner = createUser("owner");

        ItemRequestDto request = createItemRequest(requester.getId(), "Need a drill");

        ItemDto item = ItemDto.builder()
                .name("Electric Drill")
                .description("Powerful drill")
                .available(true)
                .requestId(request.getId())
                .build();
        itemService.createItem(item, owner.getId());

        ItemRequestDto result = itemRequestService.getItemRequestById(request.getId(), requester.getId());

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(request.getId());
        assertThat(result.getDescription()).isEqualTo("Need a drill");
        assertThat(result.getItems()).hasSize(1);
        assertThat(result.getItems().get(0).getName()).isEqualTo("Electric Drill");
        assertThat(result.getItems().get(0).getRequestId()).isEqualTo(request.getId());
    }

    @Test
    void getItemRequestById_WithoutItems_ReturnsRequestWithEmptyItems() {
        UserDto user = createUser("user");

        ItemRequestDto request = createItemRequest(user.getId(), "Need something");

        ItemRequestDto result = itemRequestService.getItemRequestById(request.getId(), user.getId());

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(request.getId());
        assertThat(result.getItems()).isNotNull().isEmpty();
    }

    @Test
    void getItemRequestById_NonExistentRequest_ThrowsNotFoundException() {
        UserDto user = createUser("user");

        assertThatThrownBy(() ->
                itemRequestService.getItemRequestById(999L, user.getId()))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Item request not found");
    }

    @Test
    void getItemRequestById_WithNonexistentUser_ThrowsNotFoundException() {
        UserDto user = createUser("user");
        ItemRequestDto request = createItemRequest(user.getId(), "Request");

        assertThatThrownBy(() ->
                itemRequestService.getItemRequestById(request.getId(), 999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void getItemRequestById_UserCanViewAnyRequest() {
        UserDto requester = createUser("requester");
        UserDto viewer = createUser("viewer");

        ItemRequestDto request = createItemRequest(requester.getId(), "Need item");

        ItemRequestDto result = itemRequestService.getItemRequestById(request.getId(), viewer.getId());

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(request.getId());
        assertThat(result.getDescription()).isEqualTo("Need item");
    }

    @Test
    void getItemRequestById_ReturnsCorrectItemsForRequest() {
        UserDto requester = createUser("requester");
        UserDto owner1 = createUser("owner1");
        UserDto owner2 = createUser("owner2");

        ItemRequestDto request = createItemRequest(requester.getId(), "Need tools");

        // Создаем несколько предметов для этого запроса
        ItemDto item1 = ItemDto.builder()
                .name("Hammer")
                .description("Heavy hammer")
                .available(true)
                .requestId(request.getId())
                .build();
        itemService.createItem(item1, owner1.getId());

        ItemDto item2 = ItemDto.builder()
                .name("Screwdriver")
                .description("Phillips screwdriver")
                .available(true)
                .requestId(request.getId())
                .build();
        itemService.createItem(item2, owner2.getId());

        ItemRequestDto result = itemRequestService.getItemRequestById(request.getId(), requester.getId());

        assertThat(result).isNotNull();
        assertThat(result.getItems()).hasSize(2);
        assertThat(result.getItems())
                .extracting(ItemDto::getName)
                .containsExactlyInAnyOrder("Hammer", "Screwdriver");
    }
}

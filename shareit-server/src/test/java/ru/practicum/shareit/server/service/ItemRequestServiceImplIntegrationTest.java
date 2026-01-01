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
import ru.practicum.shareit.server.exception.NotFoundException;
import ru.practicum.shareit.server.exception.ValidationException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class ItemRequestServiceImplIntegrationTest {

    @Autowired
    private ItemRequestService itemRequestService;

    @Autowired
    private ItemService itemService;

    @Autowired
    private UserService userService;

    private Long userId1;
    private Long userId2;

    @BeforeEach
    void setUp() {
        UserDto user1 = UserDto.builder()
                .name("User 1")
                .email("user1@example.com")
                .build();
        UserDto savedUser1 = userService.createUser(user1);
        userId1 = savedUser1.getId();

        UserDto user2 = UserDto.builder()
                .name("User 2")
                .email("user2@example.com")
                .build();
        UserDto savedUser2 = userService.createUser(user2);
        userId2 = savedUser2.getId();
    }

    @Test
    void createItemRequest_WithValidData_CreatesSuccessfully() {
        ItemRequestDto requestDto = ItemRequestDto.builder()
                .description("Need a power drill for home renovation")
                .build();

        ItemRequestDto result = itemRequestService.createItemRequest(requestDto, userId1);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        assertThat(result.getDescription()).isEqualTo("Need a power drill for home renovation");
        assertThat(result.getCreated()).isNotNull();
        assertThat(result.getItems()).isEmpty();
    }

    @Test
    void createItemRequest_WithEmptyDescription_ThrowsValidationException() {
        ItemRequestDto requestDto = ItemRequestDto.builder()
                .description("")
                .build();

        assertThatThrownBy(() -> itemRequestService.createItemRequest(requestDto, userId1))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Item request description cannot be empty");
    }

    @Test
    void getUserItemRequests_ReturnsUserRequests() {
        ItemRequestDto request1 = ItemRequestDto.builder()
                .description("First request")
                .build();
        ItemRequestDto request2 = ItemRequestDto.builder()
                .description("Second request")
                .build();

        // Создаем запросы
        itemRequestService.createItemRequest(request1, userId1);
        // Ждем немного, чтобы второй запрос был новее
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        itemRequestService.createItemRequest(request2, userId1);

        List<ItemRequestDto> result = itemRequestService.getUserItemRequests(userId1);

        assertThat(result).hasSize(2);
        // В порядке от новых к старым
        assertThat(result)
                .extracting("description")
                .containsExactly("Second request", "First request");
    }

    @Test
    void getAllItemRequests_ExcludesCurrentUserRequests() {
        ItemRequestDto request1 = ItemRequestDto.builder()
                .description("Request from user 1")
                .build();
        ItemRequestDto request2 = ItemRequestDto.builder()
                .description("Request from user 2")
                .build();

        itemRequestService.createItemRequest(request1, userId1);
        itemRequestService.createItemRequest(request2, userId2);

        List<ItemRequestDto> result = itemRequestService.getAllItemRequests(userId1, 0, 10);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getDescription()).isEqualTo("Request from user 2");
    }

    @Test
    void getItemRequestById_WithItems_ReturnsRequestWithItems() {
        ItemRequestDto requestDto = ItemRequestDto.builder()
                .description("Need a drill")
                .build();
        ItemRequestDto createdRequest = itemRequestService.createItemRequest(requestDto, userId1);

        ItemDto itemDto = ItemDto.builder()
                .name("Electric Drill")
                .description("Powerful cordless drill")
                .available(true)
                .requestId(createdRequest.getId())  // Связываем с запросом
                .build();
        itemService.createItem(itemDto, userId2);

        ItemRequestDto result = itemRequestService.getItemRequestById(createdRequest.getId(), userId1);

        assertThat(result).isNotNull();
        assertThat(result.getItems()).hasSize(1);
        assertThat(result.getItems().get(0).getName()).isEqualTo("Electric Drill");
        assertThat(result.getItems().get(0).getRequestId()).isEqualTo(createdRequest.getId());
    }

    @Test
    void getAllItemRequests_WithPagination_WorksCorrectly() {
        // Создаем 5 запросов от пользователя 2
        for (int i = 1; i <= 5; i++) {
            ItemRequestDto requestDto = ItemRequestDto.builder()
                    .description("Request " + i + " from user 2")
                    .build();
            itemRequestService.createItemRequest(requestDto, userId2);
        }

        // Пользователь 1 должен получить запросы от пользователя 2
        List<ItemRequestDto> firstPage = itemRequestService.getAllItemRequests(userId1, 0, 2);
        assertThat(firstPage).hasSize(2);

        List<ItemRequestDto> secondPage = itemRequestService.getAllItemRequests(userId1, 2, 2);
        assertThat(secondPage).hasSize(2);

        List<ItemRequestDto> thirdPage = itemRequestService.getAllItemRequests(userId1, 4, 2);
        assertThat(thirdPage).hasSize(1);
    }
}

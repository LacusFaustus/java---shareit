package ru.practicum.shareit.server.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import ru.practicum.shareit.dto.ItemRequestDto;
import ru.practicum.shareit.server.exception.NotFoundException;
import ru.practicum.shareit.server.exception.ValidationException;
import ru.practicum.shareit.server.mapper.ItemMapperImpl;
import ru.practicum.shareit.server.mapper.ItemRequestMapperImpl;
import ru.practicum.shareit.server.model.Item;
import ru.practicum.shareit.server.model.ItemRequest;
import ru.practicum.shareit.server.model.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@Import({ItemRequestServiceImpl.class, ItemRequestMapperImpl.class, ItemMapperImpl.class})
class ItemRequestServiceTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ItemRequestServiceImpl itemRequestService;

    @Autowired
    private ru.practicum.shareit.server.repository.ItemRequestRepository itemRequestRepository;

    @Autowired
    private ru.practicum.shareit.server.repository.UserRepository userRepository;

    @Autowired
    private ru.practicum.shareit.server.repository.ItemRepository itemRepository;

    private User user1;
    private User user2;
    private ItemRequest existingRequest;

    @BeforeEach
    void setUp() {
        // Очистка базы данных перед каждым тестом
        itemRequestRepository.deleteAll();
        itemRepository.deleteAll();
        userRepository.deleteAll();

        // Создание тестовых данных
        user1 = User.builder()
                .name("Пользователь 1")
                .email("user1@example.com")
                .build();
        entityManager.persist(user1);

        user2 = User.builder()
                .name("Пользователь 2")
                .email("user2@example.com")
                .build();
        entityManager.persist(user2);

        // Создаем существующий запрос
        existingRequest = ItemRequest.builder()
                .description("Нужна дрель")
                .requestor(user1)
                .created(LocalDateTime.now().minusDays(1))
                .build();
        entityManager.persist(existingRequest);

        // Создаем вещь для запроса
        Item item = Item.builder()
                .name("Дрель")
                .description("Аккумуляторная дрель")
                .available(true)
                .owner(user2)
                .requestId(existingRequest.getId())
                .build();
        entityManager.persist(item);

        entityManager.flush();
    }

    @Test
    void createItemRequest_WithValidData_CreatesSuccessfully() {
        // Given
        ItemRequestDto requestDto = ItemRequestDto.builder()
                .description("Нужен отвертка")
                .build();

        // When
        ItemRequestDto result = itemRequestService.createItemRequest(requestDto, user1.getId());

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        assertThat(result.getDescription()).isEqualTo("Нужен отвертка");
        assertThat(result.getRequestorId()).isEqualTo(user1.getId());
        assertThat(result.getItems()).isEmpty(); // Вещей пока нет
    }

    @Test
    void createItemRequest_WithEmptyDescription_ThrowsValidationException() {
        // Given
        ItemRequestDto requestDto = ItemRequestDto.builder()
                .description(" ")
                .build();

        // When & Then
        assertThatThrownBy(() -> itemRequestService.createItemRequest(requestDto, user1.getId()))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Item request description cannot be empty");
    }

    @Test
    void createItemRequest_WithNullDescription_ThrowsValidationException() {
        // Given
        ItemRequestDto requestDto = ItemRequestDto.builder()
                .description(null)
                .build();

        // When & Then
        assertThatThrownBy(() -> itemRequestService.createItemRequest(requestDto, user1.getId()))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Item request description cannot be empty");
    }

    @Test
    void createItemRequest_ByNonExistentUser_ThrowsNotFoundException() {
        // Given
        ItemRequestDto requestDto = ItemRequestDto.builder()
                .description("Нужна вещь")
                .build();

        // When & Then
        assertThatThrownBy(() -> itemRequestService.createItemRequest(requestDto, 999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void getUserItemRequests_ReturnsUserRequests() {
        // When
        List<ItemRequestDto> requests = itemRequestService.getUserItemRequests(user1.getId());

        // Then
        assertThat(requests).hasSize(1);
        assertThat(requests.get(0).getDescription()).isEqualTo("Нужна дрель");
        assertThat(requests.get(0).getItems()).hasSize(1); // Есть одна вещь
    }

    @Test
    void getUserItemRequests_ForUserWithoutRequests_ReturnsEmptyList() {
        // When
        List<ItemRequestDto> requests = itemRequestService.getUserItemRequests(user2.getId());

        // Then
        assertThat(requests).isEmpty();
    }

    @Test
    void getAllItemRequests_ReturnsOtherUsersRequests() {
        // When
        List<ItemRequestDto> requests = itemRequestService.getAllItemRequests(user2.getId(), 0, 10);

        // Then
        assertThat(requests).hasSize(1);
        assertThat(requests.get(0).getDescription()).isEqualTo("Нужна дрель");
        assertThat(requests.get(0).getRequestorId()).isEqualTo(user1.getId());
    }

    @Test
    void getAllItemRequests_WithPagination_ReturnsPaginatedResults() {
        // Given - создаем больше запросов
        for (int i = 0; i < 15; i++) {
            ItemRequest request = ItemRequest.builder()
                    .description("Запрос " + i)
                    .requestor(user1)
                    .created(LocalDateTime.now().minusHours(i))
                    .build();
            entityManager.persist(request);
        }
        entityManager.flush();

        // When - получаем первую страницу
        List<ItemRequestDto> firstPage = itemRequestService.getAllItemRequests(user2.getId(), 0, 5);

        // Then
        assertThat(firstPage).hasSize(5);

        // When - получаем вторую страницу
        List<ItemRequestDto> secondPage = itemRequestService.getAllItemRequests(user2.getId(), 5, 5);

        // Then
        assertThat(secondPage).hasSize(5);

        // Когда from > общего количества, возвращается пустой список
        List<ItemRequestDto> emptyPage = itemRequestService.getAllItemRequests(user2.getId(), 100, 5);
        assertThat(emptyPage).isEmpty();
    }

    @Test
    void getAllItemRequests_ForSameUser_ReturnsEmptyList() {
        // Когда пользователь запрашивает свои же запросы, они не возвращаются
        List<ItemRequestDto> requests = itemRequestService.getAllItemRequests(user1.getId(), 0, 10);

        assertThat(requests).isEmpty();
    }

    @Test
    void getItemRequestById_WithValidId_ReturnsRequest() {
        // When
        ItemRequestDto request = itemRequestService.getItemRequestById(existingRequest.getId(), user2.getId());

        // Then
        assertThat(request).isNotNull();
        assertThat(request.getId()).isEqualTo(existingRequest.getId());
        assertThat(request.getDescription()).isEqualTo("Нужна дрель");
        assertThat(request.getItems()).hasSize(1); // Есть одна вещь
    }

    @Test
    void getItemRequestById_WithNonExistentId_ThrowsNotFoundException() {
        // When & Then
        assertThatThrownBy(() -> itemRequestService.getItemRequestById(999L, user1.getId()))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Item request not found");
    }

    @Test
    void getItemRequestById_WithNonExistentUser_ThrowsNotFoundException() {
        // When & Then
        assertThatThrownBy(() -> itemRequestService.getItemRequestById(existingRequest.getId(), 999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("User not found");
    }

    // Тесты валидации параметров пагинации удалены, так как валидация в Gateway

    @Test
    void getAllItemRequests_WithZeroSize_ThrowsValidationException() {
        // Параметры валидируются в сервисе для безопасности
        assertThatThrownBy(() ->
                itemRequestService.getAllItemRequests(user2.getId(), 0, 0))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Parameter 'size' must be positive");
    }
}

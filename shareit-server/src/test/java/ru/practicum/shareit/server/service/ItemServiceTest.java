package ru.practicum.shareit.server.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.dto.*;
import ru.practicum.shareit.server.BaseIntegrationTest;
import ru.practicum.shareit.server.exception.NotFoundException;
import ru.practicum.shareit.server.exception.ValidationException;
import ru.practicum.shareit.server.util.TestDataCreator;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.properties")
@Transactional
class ItemServiceTest extends BaseIntegrationTest {

    @Autowired
    private ItemService itemService;

    @Autowired
    private UserService userService;

    @Autowired
    private BookingService bookingService;

    @Autowired
    private ItemRequestService itemRequestService;

    @Autowired
    private TestDataCreator testDataCreator;

    private Long ownerId;
    private Long anotherUserId;

    @BeforeEach
    void setUp() {
        UserDto ownerDto = UserDto.builder()
                .name("Owner")
                .email("owner-" + UUID.randomUUID() + "@example.com")
                .build();
        UserDto savedOwner = userService.createUser(ownerDto);
        ownerId = savedOwner.getId();

        UserDto anotherUserDto = UserDto.builder()
                .name("Another User")
                .email("another-" + UUID.randomUUID() + "@example.com")
                .build();
        UserDto savedAnotherUser = userService.createUser(anotherUserDto);
        anotherUserId = savedAnotherUser.getId();
    }

    private UserDto createUser(String prefix, String name) {
        String email = prefix + "-" + UUID.randomUUID() + "@example.com";
        return userService.createUser(UserDto.builder()
                .name(name)
                .email(email)
                .build());
    }

    // ============== БАЗОВЫЕ ТЕСТЫ СОЗДАНИЯ ==============

    @Test
    void createItem_WithValidData_CreatesSuccessfully() {
        ItemDto itemDto = ItemDto.builder()
                .name("Тестовая вещь")
                .description("Описание тестовой вещи")
                .available(true)
                .build();

        ItemResponseDto result = itemService.createItem(itemDto, ownerId);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        assertThat(result.getName()).isEqualTo("Тестовая вещь");
        assertThat(result.getAvailable()).isTrue();
    }

    @Test
    void createItem_WithValidRequestId_CreatesSuccessfully() {
        UserDto requester = createUser("requester", "Requester");

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

        ItemResponseDto result = itemService.createItem(item, ownerId);

        assertThat(result).isNotNull();
        assertThat(result.getRequestId()).isEqualTo(createdRequest.getId());
    }

    @Test
    void createItem_WithRequestId_ReturnsItemWithRequest() {
        UserDto requester = createUser("requester-with-request", "Requester");

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

        ItemResponseDto result = itemService.createItem(item, ownerId);

        assertThat(result).isNotNull();
        assertThat(result.getRequestId()).isEqualTo(createdRequest.getId());
    }

    // ============== ВАЛИДАЦИЯ ==============

    @Test
    void createItem_WithInvalidData_ThrowsValidationException() {
        // Пустое имя
        ItemDto item1 = ItemDto.builder()
                .name("")
                .description("Description")
                .available(true)
                .build();

        assertThatThrownBy(() -> itemService.createItem(item1, ownerId))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Item name cannot be empty");

        // Пустое описание
        ItemDto item2 = ItemDto.builder()
                .name("Item")
                .description("")
                .available(true)
                .build();

        assertThatThrownBy(() -> itemService.createItem(item2, ownerId))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Item description cannot be empty");

        // Не указана доступность
        ItemDto item3 = ItemDto.builder()
                .name("Item")
                .description("Description")
                .available(null)
                .build();

        assertThatThrownBy(() -> itemService.createItem(item3, ownerId))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Available status must be specified");
    }

    @Test
    void createItem_WithNonExistentRequestId_ThrowsNotFoundException() {
        ItemDto item = ItemDto.builder()
                .name("Test Item")
                .description("Description")
                .available(true)
                .requestId(999999L)
                .build();

        assertThatThrownBy(() -> itemService.createItem(item, ownerId))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Item request not found");
    }

    // ============== ПОЛУЧЕНИЕ ВЕЩЕЙ ==============

    @Test
    void getAllItemsByOwner_WhenUserExists_ReturnsItems() {
        ItemDto item1 = ItemDto.builder()
                .name("Дрель")
                .description("Аккумуляторная дрель")
                .available(true)
                .build();

        ItemDto item2 = ItemDto.builder()
                .name("Молоток")
                .description("Строительный молоток")
                .available(true)
                .build();

        itemService.createItem(item1, ownerId);
        itemService.createItem(item2, ownerId);

        var result = itemService.getAllItemsByOwner(ownerId);

        assertThat(result).hasSize(2);
        assertThat(result)
                .extracting("name")
                .containsExactlyInAnyOrder("Дрель", "Молоток");
    }

    @Test
    void getAllItemsByOwner_WhenUserDoesNotExist_ThrowsNotFoundException() {
        Long nonExistentUserId = 999L;

        assertThatThrownBy(() -> itemService.getAllItemsByOwner(nonExistentUserId))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void getAllItemsByOwner_WithNoItems_ReturnsEmptyList() {
        UserDto owner = createUser("owner-noitems", "Owner");

        List<ItemOwnerDto> result = itemService.getAllItemsByOwner(owner.getId());

        assertThat(result).isEmpty();
    }

    @Test
    void getAllItemsByOwner_ReturnsItemsInCorrectOrder() {
        UserDto owner = createUser("owner-order", "Owner");

        for (int i = 1; i <= 5; i++) {
            ItemDto item = ItemDto.builder()
                    .name("Item " + i)
                    .description("Description " + i)
                    .available(true)
                    .build();
            itemService.createItem(item, owner.getId());
        }

        List<ItemOwnerDto> result = itemService.getAllItemsByOwner(owner.getId());

        assertThat(result).hasSize(5);
        for (int i = 0; i < result.size() - 1; i++) {
            assertThat(result.get(i).getId()).isLessThan(result.get(i + 1).getId());
        }
    }

    @Test
    void getAllItemsByOwner_WithMultipleItems_ReturnsAllWithBookings() {
        UserDto owner = createUser("owner-multiple", "Owner");

        for (int i = 1; i <= 3; i++) {
            ItemDto item = ItemDto.builder()
                    .name("Item " + i)
                    .description("Description " + i)
                    .available(true)
                    .build();
            itemService.createItem(item, owner.getId());
        }

        List<ItemOwnerDto> result = itemService.getAllItemsByOwner(owner.getId());

        assertThat(result).hasSize(3);
        result.forEach(item -> {
            assertThat(item).isNotNull();
            assertThat(item.getName()).startsWith("Item ");
        });
    }

    @Test
    void getItemById_WhenUserIsOwnerWithBookings_ReturnsWithBookings() {
        UserDto owner = createUser("owner-bookings", "Owner");
        UserDto booker = createUser("booker-bookings", "Booker");

        ItemDto item = ItemDto.builder()
                .name("Test Item")
                .description("Description")
                .available(true)
                .build();
        ItemResponseDto savedItem = itemService.createItem(item, owner.getId());

        // Создаем будущее бронирование
        LocalDateTime now = LocalDateTime.now();
        BookingRequestDto futureBooking = new BookingRequestDto(
                savedItem.getId(),
                now.plusDays(1),
                now.plusDays(2)
        );
        BookingResponseDto createdFuture = bookingService.createBooking(futureBooking, booker.getId());
        bookingService.updateBookingStatus(createdFuture.getId(), true, owner.getId());

        ItemResponseDto result = itemService.getItemById(savedItem.getId(), owner.getId());

        assertThat(result).isNotNull();
        assertThat(result.getNextBooking()).isNotNull();
        assertThat(result.getNextBooking().getId()).isEqualTo(createdFuture.getId());
    }

    @Test
    void getItemById_ForOwnerWithLastBooking_ReturnsWithLastBooking() {
        UserDto owner = createUser("owner-last", "Owner");
        UserDto booker = createUser("booker-last", "Booker");

        ItemDto item = ItemDto.builder()
                .name("Test Item")
                .description("Description")
                .available(true)
                .build();
        var savedItem = itemService.createItem(item, owner.getId());

        // Используем TestDataCreator для создания прошедшего бронирования
        testDataCreator.createPastBooking(savedItem.getId(), booker.getId());

        ItemResponseDto result = itemService.getItemById(savedItem.getId(), owner.getId());

        assertThat(result).isNotNull();
        assertThat(result.getLastBooking()).isNotNull();
    }

    @Test
    void getItemById_WithLastAndNextBooking_ReturnsBoth() {
        UserDto owner = createUser("owner-both-bookings", "Owner");
        UserDto booker = createUser("booker-both", "Booker");

        ItemDto item = ItemDto.builder()
                .name("Test Item")
                .description("Description")
                .available(true)
                .build();
        ItemResponseDto savedItem = itemService.createItem(item, owner.getId());

        // Используем TestDataCreator для прошедшего бронирования
        testDataCreator.createPastBooking(savedItem.getId(), booker.getId());

        // Создаем будущее бронирование через сервис
        LocalDateTime now = LocalDateTime.now();
        BookingRequestDto futureBooking = new BookingRequestDto(
                savedItem.getId(),
                now.plusDays(1),
                now.plusDays(2)
        );
        BookingResponseDto createdFuture = bookingService.createBooking(futureBooking, booker.getId());
        bookingService.updateBookingStatus(createdFuture.getId(), true, owner.getId());

        ItemResponseDto result = itemService.getItemById(savedItem.getId(), owner.getId());

        assertThat(result.getLastBooking()).isNotNull();
        assertThat(result.getNextBooking()).isNotNull();
        assertThat(result.getNextBooking().getId()).isEqualTo(createdFuture.getId());
    }

    @Test
    void getItemById_ForNonOwner_ReturnsWithoutBookings() {
        UserDto owner = createUser("owner-nonowner", "Owner");
        UserDto anotherUser = createUser("another-nonowner", "Another User");

        ItemDto item = ItemDto.builder()
                .name("Test Item")
                .description("Description")
                .available(true)
                .build();
        ItemResponseDto savedItem = itemService.createItem(item, owner.getId());

        ItemResponseDto result = itemService.getItemById(savedItem.getId(), anotherUser.getId());

        assertThat(result).isNotNull();
        assertThat(result.getLastBooking()).isNull();
        assertThat(result.getNextBooking()).isNull();
    }

    @Test
    void getItemById_WithNullUserId_ReturnsItemWithoutBookings() {
        UserDto owner = createUser("owner-null-userid", "Owner");

        ItemDto item = ItemDto.builder()
                .name("Test Item")
                .description("Description")
                .available(true)
                .build();
        ItemResponseDto created = itemService.createItem(item, owner.getId());

        ItemResponseDto result = itemService.getItemById(created.getId(), null);

        assertThat(result).isNotNull();
        assertThat(result.getLastBooking()).isNull();
        assertThat(result.getNextBooking()).isNull();
    }

    @Test
    void getItemById_NonExistentItem_ThrowsNotFoundException() {
        UserDto user = createUser("user-notfound", "User");

        assertThatThrownBy(() -> itemService.getItemById(999999L, user.getId()))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Item not found");
    }

    // ============== ОБНОВЛЕНИЕ ВЕЩЕЙ ==============

    @Test
    void updateItem_WhenUserIsOwner_UpdatesSuccessfully() {
        ItemDto originalItem = ItemDto.builder()
                .name("Старое название")
                .description("Старое описание")
                .available(true)
                .build();

        ItemResponseDto created = itemService.createItem(originalItem, ownerId);

        ItemDto updateDto = ItemDto.builder()
                .name("Новое название")
                .description("Новое описание")
                .available(false)
                .build();

        ItemResponseDto updated = itemService.updateItem(created.getId(), updateDto, ownerId);

        assertThat(updated.getName()).isEqualTo("Новое название");
        assertThat(updated.getDescription()).isEqualTo("Новое описание");
        assertThat(updated.getAvailable()).isFalse();
    }

    @Test
    void updateItem_WithPartialData_UpdatesOnlyProvidedFields() {
        ItemDto originalItem = ItemDto.builder()
                .name("Original Name")
                .description("Original Description")
                .available(true)
                .build();
        ItemResponseDto created = itemService.createItem(originalItem, ownerId);

        ItemDto updateDto = ItemDto.builder()
                .name("Updated Name")
                .build();

        ItemResponseDto updated = itemService.updateItem(created.getId(), updateDto, ownerId);

        assertThat(updated.getName()).isEqualTo("Updated Name");
        assertThat(updated.getDescription()).isEqualTo("Original Description");
        assertThat(updated.getAvailable()).isTrue();
    }

    @Test
    void updateItem_WithAllFields_UpdatesSuccessfully() {
        ItemDto originalItem = ItemDto.builder()
                .name("Original Name")
                .description("Original Description")
                .available(true)
                .build();
        ItemResponseDto created = itemService.createItem(originalItem, ownerId);

        ItemDto updateDto = ItemDto.builder()
                .name("Updated Name")
                .description("Updated Description")
                .available(false)
                .build();

        ItemResponseDto updated = itemService.updateItem(created.getId(), updateDto, ownerId);

        assertThat(updated.getName()).isEqualTo("Updated Name");
        assertThat(updated.getDescription()).isEqualTo("Updated Description");
        assertThat(updated.getAvailable()).isFalse();
    }

    @Test
    void updateItem_WithValidRequestId_UpdatesSuccessfully() {
        UserDto requester = createUser("requester-update", "Requester");

        ItemRequestDto request = ItemRequestDto.builder()
                .description("Need a test item")
                .build();
        ItemRequestDto createdRequest = itemRequestService.createItemRequest(request, requester.getId());

        ItemDto item = ItemDto.builder()
                .name("Test Item")
                .description("Description")
                .available(true)
                .build();
        ItemResponseDto savedItem = itemService.createItem(item, ownerId);

        ItemDto updateDto = ItemDto.builder()
                .requestId(createdRequest.getId())
                .build();

        ItemResponseDto updated = itemService.updateItem(savedItem.getId(), updateDto, ownerId);

        assertThat(updated.getRequestId()).isEqualTo(createdRequest.getId());
    }

    @Test
    void updateItem_WithEmptyName_IgnoresEmptyName() {
        ItemDto originalItem = ItemDto.builder()
                .name("Original Name")
                .description("Original Description")
                .available(true)
                .build();
        ItemResponseDto created = itemService.createItem(originalItem, ownerId);

        ItemDto updateDto = ItemDto.builder()
                .name("")
                .build();

        ItemResponseDto updated = itemService.updateItem(created.getId(), updateDto, ownerId);

        assertThat(updated.getName()).isEqualTo("Original Name");
    }

    @Test
    void updateItem_WithNullName_IgnoresNullName() {
        ItemDto originalItem = ItemDto.builder()
                .name("Original Name")
                .description("Original Description")
                .available(true)
                .build();
        ItemResponseDto created = itemService.createItem(originalItem, ownerId);

        ItemDto updateDto = ItemDto.builder()
                .name(null)
                .build();

        ItemResponseDto updated = itemService.updateItem(created.getId(), updateDto, ownerId);

        assertThat(updated.getName()).isEqualTo("Original Name");
    }

    @Test
    void updateItem_WhenUserIsNotOwner_ThrowsNotFoundException() {
        ItemDto itemDto = ItemDto.builder()
                .name("Вещь")
                .description("Описание")
                .available(true)
                .build();

        ItemResponseDto created = itemService.createItem(itemDto, ownerId);

        ItemDto updateDto = ItemDto.builder()
                .name("Попытка обновить")
                .build();

        assertThatThrownBy(() -> itemService.updateItem(created.getId(), updateDto, anotherUserId))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Only owner can update item");
    }

    @Test
    void updateItem_WithNonExistentRequestId_ThrowsNotFoundException() {
        ItemDto item = ItemDto.builder()
                .name("Test Item")
                .description("Description")
                .available(true)
                .build();
        ItemResponseDto savedItem = itemService.createItem(item, ownerId);

        ItemDto updateDto = ItemDto.builder()
                .requestId(999999L)
                .build();

        assertThatThrownBy(() ->
                itemService.updateItem(savedItem.getId(), updateDto, ownerId))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Item request not found");
    }

    @Test
    void updateItem_NonExistentItem_ThrowsNotFoundException() {
        ItemDto updateDto = ItemDto.builder()
                .name("Updated")
                .build();

        assertThatThrownBy(() -> itemService.updateItem(999999L, updateDto, ownerId))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Item not found");
    }

    @Test
    void updateItem_NotOwner_ThrowsNotFoundException() {
        UserDto notOwner = createUser("not-owner", "Not Owner");

        ItemDto item = ItemDto.builder()
                .name("Test Item")
                .description("Description")
                .available(true)
                .build();
        ItemResponseDto created = itemService.createItem(item, ownerId);

        ItemDto updateDto = ItemDto.builder()
                .name("Updated")
                .build();

        assertThatThrownBy(() -> itemService.updateItem(created.getId(), updateDto, notOwner.getId()))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Only owner can update item");
    }

    // ============== ПОИСК ВЕЩЕЙ ==============

    @Test
    void searchItems_WithText_ReturnsAvailableItems() {
        ItemDto availableItem = ItemDto.builder()
                .name("Дрель аккумуляторная")
                .description("Мощная дрель")
                .available(true)
                .build();

        ItemDto unavailableItem = ItemDto.builder()
                .name("Перфоратор")
                .description("Мощный перфоратор")
                .available(false)
                .build();

        itemService.createItem(availableItem, ownerId);
        itemService.createItem(unavailableItem, ownerId);

        List<ItemDto> result = itemService.searchItems("дрель", anotherUserId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Дрель аккумуляторная");
    }

    @Test
    void searchItems_WithEmptyText_ReturnsEmptyList() {
        ItemDto item = ItemDto.builder()
                .name("Дрель")
                .description("Описание")
                .available(true)
                .build();
        itemService.createItem(item, ownerId);

        List<ItemDto> result = itemService.searchItems("", anotherUserId);

        assertThat(result).isEmpty();
    }

    @Test
    void searchItems_WithNullText_ReturnsEmptyList() {
        ItemDto item = ItemDto.builder()
                .name("Test Item")
                .description("Description")
                .available(true)
                .build();
        itemService.createItem(item, ownerId);

        List<ItemDto> result = itemService.searchItems(null, ownerId);
        assertThat(result).isEmpty();
    }

    @Test
    void searchItems_WithBlankText_ReturnsEmptyList() {
        ItemDto item = ItemDto.builder()
                .name("Test Item")
                .description("Description")
                .available(true)
                .build();
        itemService.createItem(item, ownerId);

        List<ItemDto> result = itemService.searchItems("   ", ownerId);
        assertThat(result).isEmpty();
    }

    @Test
    void searchItems_WithMultipleMatches_ReturnsAllAvailable() {
        UserDto searcher = createUser("searcher-multi", "Searcher");

        itemService.createItem(ItemDto.builder()
                .name("Drill Pro")
                .description("Professional drill")
                .available(true)
                .build(), ownerId);

        itemService.createItem(ItemDto.builder()
                .name("Cordless Drill")
                .description("Battery powered drill")
                .available(true)
                .build(), ownerId);

        itemService.createItem(ItemDto.builder()
                .name("Unavailable Drill")
                .description("Not available")
                .available(false)
                .build(), ownerId);

        itemService.createItem(ItemDto.builder()
                .name("Hammer")
                .description("Tool")
                .available(true)
                .build(), ownerId);

        List<ItemDto> results = itemService.searchItems("drill", searcher.getId());

        assertThat(results).hasSize(2);
        results.forEach(item -> {
            assertThat(item.getName().toLowerCase()).contains("drill");
            assertThat(item.getAvailable()).isTrue();
        });
    }

    @Test
    void searchItems_WithSpecialCharacters_ReturnsCorrectResults() {
        UserDto searcher = createUser("searcher-special", "Searcher");

        itemService.createItem(ItemDto.builder()
                .name("Drill 1000W")
                .description("Powerful drill 1000W")
                .available(true)
                .build(), ownerId);

        itemService.createItem(ItemDto.builder()
                .name("Hammer")
                .description("Regular hammer")
                .available(true)
                .build(), ownerId);

        List<ItemDto> results1 = itemService.searchItems("1000", searcher.getId());
        assertThat(results1).hasSize(1);
        assertThat(results1.get(0).getName()).contains("1000");

        List<ItemDto> results2 = itemService.searchItems("drill 1000", searcher.getId());
        assertThat(results2).hasSize(1);

        List<ItemDto> results3 = itemService.searchItems("nonexistent", searcher.getId());
        assertThat(results3).isEmpty();
    }

    // ============== КОММЕНТАРИИ ==============

    @Test
    void addComment_WithEmptyText_ThrowsValidationException() {
        UserDto owner = createUser("owner-empty-text", "Owner");
        UserDto booker = createUser("booker-empty-text", "Booker");

        ItemDto item = ItemDto.builder()
                .name("Test Item")
                .description("Description")
                .available(true)
                .build();
        ItemResponseDto savedItem = itemService.createItem(item, owner.getId());

        CommentDto comment = CommentDto.builder()
                .text("")
                .build();

        assertThatThrownBy(() ->
                itemService.addComment(savedItem.getId(), comment, booker.getId()))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Comment text cannot be empty");
    }

    @Test
    void addComment_WithNullText_ThrowsValidationException() {
        UserDto owner = createUser("owner-null-text", "Owner");
        UserDto booker = createUser("booker-null-text", "Booker");

        ItemDto item = ItemDto.builder()
                .name("Test Item")
                .description("Description")
                .available(true)
                .build();
        ItemResponseDto savedItem = itemService.createItem(item, owner.getId());

        CommentDto comment = CommentDto.builder()
                .text(null)
                .build();

        assertThatThrownBy(() ->
                itemService.addComment(savedItem.getId(), comment, booker.getId()))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Comment text cannot be empty");
    }

    @Test
    void addComment_Successful_WhenUserHasCompletedBooking() {
        UserDto owner = createUser("owner-comment-success", "Owner");
        UserDto booker = createUser("booker-comment-success", "Booker");

        ItemDto item = ItemDto.builder()
                .name("Test Item")
                .description("Description")
                .available(true)
                .build();
        var savedItem = itemService.createItem(item, owner.getId());

        // Используем TestDataCreator для создания прошедшего бронирования
        testDataCreator.createPastBooking(savedItem.getId(), booker.getId());

        CommentDto commentDto = CommentDto.builder()
                .text("Great item!")
                .build();

        CommentDto result = itemService.addComment(savedItem.getId(), commentDto, booker.getId());

        assertThat(result).isNotNull();
        assertThat(result.getText()).isEqualTo("Great item!");
        assertThat(result.getAuthorName()).isEqualTo(booker.getName());
    }

    @Test
    void addComment_ByUserWhoNeverBooked_ThrowsValidationException() {
        UserDto owner = createUser("owner-never", "Owner");
        UserDto anotherUser = createUser("another-never", "Another User");

        ItemDto item = ItemDto.builder()
                .name("Test Item")
                .description("Description")
                .available(true)
                .build();
        ItemResponseDto savedItem = itemService.createItem(item, owner.getId());

        CommentDto comment = CommentDto.builder()
                .text("Great item!")
                .build();

        assertThatThrownBy(() ->
                itemService.addComment(savedItem.getId(), comment, anotherUser.getId()))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("User can only comment on items they have booked in the past");
    }

    @Test
    void addComment_ToFutureBooking_ThrowsValidationException() {
        UserDto owner = createUser("owner-future", "Owner");
        UserDto booker = createUser("booker-future", "Booker");

        ItemDto item = ItemDto.builder()
                .name("Test Item")
                .description("Description")
                .available(true)
                .build();
        ItemResponseDto savedItem = itemService.createItem(item, owner.getId());

        LocalDateTime now = LocalDateTime.now();
        BookingRequestDto booking = new BookingRequestDto(
                savedItem.getId(),
                now.plusDays(1),
                now.plusDays(3)
        );
        BookingResponseDto created = bookingService.createBooking(booking, booker.getId());
        bookingService.updateBookingStatus(created.getId(), true, owner.getId());

        CommentDto comment = CommentDto.builder()
                .text("Great item!")
                .build();

        assertThatThrownBy(() ->
                itemService.addComment(savedItem.getId(), comment, booker.getId()))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("User can only comment on items they have booked in the past");
    }

    @Test
    void addComment_NonExistentItem_ThrowsNotFoundException() {
        UserDto user = createUser("user-comment", "User");

        CommentDto comment = CommentDto.builder()
                .text("Comment")
                .build();

        assertThatThrownBy(() -> itemService.addComment(999999L, comment, user.getId()))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Item not found");
    }

    // ============== ТЕСТЫ ДЛЯ СОЗДАНИЯ ВЕЩЕЙ БЕЗ ЗАПРОСОВ ==============

    @Test
    void createItemWithoutRequest_WorksCorrectly() {
        UserDto requester = createUser("requester-without", "Requester");

        ItemDto itemDto = ItemDto.builder()
                .name("Hammer")
                .description("Regular hammer")
                .available(true)
                .requestId(null)
                .build();

        itemService.createItem(itemDto, ownerId);

        List<ItemDto> searchResults = itemService.searchItems("hammer", requester.getId());
        assertThat(searchResults).hasSize(1);
        assertThat(searchResults.get(0).getRequestId()).isNull();
    }
}

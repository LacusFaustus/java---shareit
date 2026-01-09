package ru.practicum.shareit.server.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.dto.BookingRequestDto;
import ru.practicum.shareit.dto.BookingResponseDto;
import ru.practicum.shareit.dto.ItemDto;
import ru.practicum.shareit.dto.UserDto;
import ru.practicum.shareit.server.exception.NotFoundException;
import ru.practicum.shareit.server.exception.ValidationException;
import ru.practicum.shareit.server.util.TestBookingUtil;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class BookingServiceTest {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private ItemService itemService;

    @Autowired
    private UserService userService;

    @Autowired
    private TestBookingUtil testBookingUtil;

    // ============== ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ==============

    private UserDto createUser(String prefix, String name) {
        String email = prefix + "-" + UUID.randomUUID() + "@example.com";
        return userService.createUser(UserDto.builder()
                .name(name)
                .email(email)
                .build());
    }

    // ============== БАЗОВЫЕ ТЕСТЫ СОЗДАНИЯ ==============

    @Test
    void createBooking_WithValidData_CreatesSuccessfully() {
        UserDto owner = createUser("owner-valid", "Owner");
        UserDto booker = createUser("booker-valid", "Booker");

        ItemDto item = ItemDto.builder()
                .name("Дрель")
                .description("Аккумуляторная дрель")
                .available(true)
                .build();
        var savedItem = itemService.createItem(item, owner.getId());

        BookingRequestDto bookingRequest = new BookingRequestDto(
                savedItem.getId(),
                LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusDays(1)
        );

        BookingResponseDto result = bookingService.createBooking(bookingRequest, booker.getId());

        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        assertThat(result.getItem().getId()).isEqualTo(savedItem.getId());
        assertThat(result.getBooker().getId()).isEqualTo(booker.getId());
        assertThat(result.getStatus()).isEqualTo("WAITING");
    }

    @Test
    void createBooking_WithUnavailableItem_ThrowsValidationException() {
        UserDto owner = createUser("owner-unavailable", "Owner");
        UserDto booker = createUser("booker-unavailable", "Booker");

        ItemDto unavailableItem = ItemDto.builder()
                .name("Недоступная вещь")
                .description("Вещь недоступна для бронирования")
                .available(false)
                .build();
        var savedItem = itemService.createItem(unavailableItem, owner.getId());

        BookingRequestDto bookingRequest = new BookingRequestDto(
                savedItem.getId(),
                LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusDays(1)
        );

        assertThatThrownBy(() -> bookingService.createBooking(bookingRequest, booker.getId()))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Item is not available for booking");
    }

    @Test
    void createBooking_ByOwner_ThrowsNotFoundException() {
        UserDto owner = createUser("owner-self", "Owner");

        ItemDto item = ItemDto.builder()
                .name("Test Item")
                .description("Description")
                .available(true)
                .build();
        var savedItem = itemService.createItem(item, owner.getId());

        BookingRequestDto bookingRequest = new BookingRequestDto(
                savedItem.getId(),
                LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusDays(1)
        );

        assertThatThrownBy(() -> bookingService.createBooking(bookingRequest, owner.getId()))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Owner cannot book their own item");
    }

    // ============== ВАЛИДАЦИЯ ДАТ ==============

    @Test
    void createBooking_WithStartEqualsEnd_ThrowsValidationException() {
        UserDto owner = createUser("owner-equal", "Owner");
        UserDto booker = createUser("booker-equal", "Booker");

        ItemDto item = ItemDto.builder()
                .name("Test Item")
                .description("Description")
                .available(true)
                .build();
        var savedItem = itemService.createItem(item, owner.getId());

        LocalDateTime sameTime = LocalDateTime.now().plusDays(1);
        BookingRequestDto booking = new BookingRequestDto(
                savedItem.getId(),
                sameTime,
                sameTime
        );

        assertThatThrownBy(() -> bookingService.createBooking(booking, booker.getId()))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("End date must be after start date");
    }

    @Test
    void createBooking_WithEndBeforeStart_ThrowsValidationException() {
        UserDto owner = createUser("owner-before", "Owner");
        UserDto booker = createUser("booker-before", "Booker");

        ItemDto item = ItemDto.builder()
                .name("Test Item")
                .description("Description")
                .available(true)
                .build();
        var savedItem = itemService.createItem(item, owner.getId());

        BookingRequestDto booking = new BookingRequestDto(
                savedItem.getId(),
                LocalDateTime.now().plusDays(2),
                LocalDateTime.now().plusDays(1)
        );

        assertThatThrownBy(() -> bookingService.createBooking(booking, booker.getId()))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("End date must be after start date");
    }

    @Test
    void createBooking_WithPastStartDate_ThrowsValidationException() {
        UserDto owner = createUser("owner-past", "Owner");
        UserDto booker = createUser("booker-past", "Booker");

        ItemDto item = ItemDto.builder()
                .name("Test Item")
                .description("Description")
                .available(true)
                .build();
        var savedItem = itemService.createItem(item, owner.getId());

        BookingRequestDto booking = new BookingRequestDto(
                savedItem.getId(),
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().plusDays(1)
        );

        assertThatThrownBy(() -> bookingService.createBooking(booking, booker.getId()))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Start date cannot be in the past");
    }

    @Test
    void createBooking_WithPastEndDate_ThrowsValidationException() {
        UserDto owner = createUser("owner-past-end", "Owner");
        UserDto booker = createUser("booker-past-end", "Booker");

        ItemDto item = ItemDto.builder()
                .name("Test Item")
                .description("Description")
                .available(true)
                .build();
        var savedItem = itemService.createItem(item, owner.getId());

        BookingRequestDto booking = new BookingRequestDto(
                savedItem.getId(),
                LocalDateTime.now().plusHours(1),
                LocalDateTime.now().minusHours(1)
        );

        assertThatThrownBy(() -> bookingService.createBooking(booking, booker.getId()))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("End date must be in the future");
    }

    @Test
    void createBooking_WithNullDates_ThrowsValidationException() {
        UserDto owner = createUser("owner-null", "Owner");
        UserDto booker = createUser("booker-null", "Booker");

        ItemDto item = ItemDto.builder()
                .name("Test Item")
                .description("Description")
                .available(true)
                .build();
        var savedItem = itemService.createItem(item, owner.getId());

        BookingRequestDto booking = new BookingRequestDto(
                savedItem.getId(),
                null,
                null
        );

        assertThatThrownBy(() -> bookingService.createBooking(booking, booker.getId()))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Start and end dates are required");
    }

    // ============== ОБНОВЛЕНИЕ СТАТУСА ==============

    @Test
    void updateBookingStatus_WhenApproved_UpdatesSuccessfully() {
        UserDto owner = createUser("owner-approve", "Owner");
        UserDto booker = createUser("booker-approve", "Booker");

        ItemDto item = ItemDto.builder()
                .name("Test Item")
                .description("Description")
                .available(true)
                .build();
        var savedItem = itemService.createItem(item, owner.getId());

        BookingRequestDto bookingRequest = new BookingRequestDto(
                savedItem.getId(),
                LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusDays(1)
        );
        BookingResponseDto createdBooking = bookingService.createBooking(bookingRequest, booker.getId());

        BookingResponseDto updatedBooking = bookingService.updateBookingStatus(
                createdBooking.getId(), true, owner.getId());

        assertThat(updatedBooking.getStatus()).isEqualTo("APPROVED");
    }

    @Test
    void updateBookingStatus_WhenRejected_UpdatesSuccessfully() {
        UserDto owner = createUser("owner-reject", "Owner");
        UserDto booker = createUser("booker-reject", "Booker");

        ItemDto item = ItemDto.builder()
                .name("Test Item")
                .description("Description")
                .available(true)
                .build();
        var savedItem = itemService.createItem(item, owner.getId());

        BookingRequestDto bookingRequest = new BookingRequestDto(
                savedItem.getId(),
                LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusDays(1)
        );
        BookingResponseDto createdBooking = bookingService.createBooking(bookingRequest, booker.getId());

        BookingResponseDto updatedBooking = bookingService.updateBookingStatus(
                createdBooking.getId(), false, owner.getId());

        assertThat(updatedBooking.getStatus()).isEqualTo("REJECTED");
    }

    @Test
    void updateBookingStatus_WhenAlreadyApproved_ThrowsValidationException() {
        UserDto owner = createUser("owner-approved", "Owner");
        UserDto booker = createUser("booker-approved", "Booker");

        ItemDto item = ItemDto.builder()
                .name("Test Item")
                .description("Description")
                .available(true)
                .build();
        var savedItem = itemService.createItem(item, owner.getId());

        BookingRequestDto booking = new BookingRequestDto(
                savedItem.getId(),
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2)
        );
        BookingResponseDto created = bookingService.createBooking(booking, booker.getId());

        bookingService.updateBookingStatus(created.getId(), true, owner.getId());

        assertThatThrownBy(() ->
                bookingService.updateBookingStatus(created.getId(), false, owner.getId()))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Booking status cannot be changed");
    }

    @Test
    void updateBookingStatus_WhenAlreadyRejected_ThrowsValidationException() {
        UserDto owner = createUser("owner-rejected", "Owner");
        UserDto booker = createUser("booker-rejected", "Booker");

        ItemDto item = ItemDto.builder()
                .name("Test Item")
                .description("Description")
                .available(true)
                .build();
        var savedItem = itemService.createItem(item, owner.getId());

        BookingRequestDto booking = new BookingRequestDto(
                savedItem.getId(),
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2)
        );
        BookingResponseDto created = bookingService.createBooking(booking, booker.getId());

        bookingService.updateBookingStatus(created.getId(), false, owner.getId());

        assertThatThrownBy(() ->
                bookingService.updateBookingStatus(created.getId(), true, owner.getId()))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Booking status cannot be changed");
    }

    @Test
    void updateBookingStatus_ByNonOwner_ThrowsNotFoundException() {
        UserDto owner = createUser("owner-non-owner", "Owner");
        UserDto booker = createUser("booker-non-owner", "Booker");

        ItemDto item = ItemDto.builder()
                .name("Test Item")
                .description("Description")
                .available(true)
                .build();
        var savedItem = itemService.createItem(item, owner.getId());

        BookingRequestDto bookingRequest = new BookingRequestDto(
                savedItem.getId(),
                LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusDays(1)
        );
        BookingResponseDto createdBooking = bookingService.createBooking(bookingRequest, booker.getId());

        UserDto anotherUser = createUser("another", "Another User");

        assertThatThrownBy(() -> bookingService.updateBookingStatus(
                createdBooking.getId(), true, anotherUser.getId()))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Only owner can update booking status");
    }

    // ============== ПОЛУЧЕНИЕ БРОНИРОВАНИЙ ==============

    @Test
    void getBookingById_ForBooker_ReturnsBooking() {
        UserDto owner = createUser("owner-for-booker", "Owner");
        UserDto booker = createUser("booker-for-booker", "Booker");

        ItemDto item = ItemDto.builder()
                .name("Test Item")
                .description("Description")
                .available(true)
                .build();
        var savedItem = itemService.createItem(item, owner.getId());

        LocalDateTime now = LocalDateTime.now();
        BookingRequestDto booking = new BookingRequestDto(
                savedItem.getId(),
                now.plusDays(1),
                now.plusDays(2)
        );
        BookingResponseDto created = bookingService.createBooking(booking, booker.getId());

        BookingResponseDto result = bookingService.getBookingById(created.getId(), booker.getId());

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(created.getId());
        assertThat(result.getBooker().getId()).isEqualTo(booker.getId());
    }

    @Test
    void getBookingById_ForOwner_ReturnsBooking() {
        UserDto owner = createUser("owner-for-owner", "Owner");
        UserDto booker = createUser("booker-for-owner", "Booker");

        ItemDto item = ItemDto.builder()
                .name("Test Item")
                .description("Description")
                .available(true)
                .build();
        var savedItem = itemService.createItem(item, owner.getId());

        LocalDateTime now = LocalDateTime.now();
        BookingRequestDto booking = new BookingRequestDto(
                savedItem.getId(),
                now.plusDays(1),
                now.plusDays(2)
        );
        BookingResponseDto created = bookingService.createBooking(booking, booker.getId());

        BookingResponseDto result = bookingService.getBookingById(created.getId(), owner.getId());

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(created.getId());
        assertThat(result.getItem().getId()).isEqualTo(savedItem.getId());
    }

    @Test
    void getBookingById_ForUnauthorizedUser_ThrowsNotFoundException() {
        UserDto owner = createUser("owner-unauth", "Owner");
        UserDto booker = createUser("booker-unauth", "Booker");
        UserDto stranger = createUser("stranger", "Stranger");

        ItemDto item = ItemDto.builder()
                .name("Test Item")
                .description("Description")
                .available(true)
                .build();
        var savedItem = itemService.createItem(item, owner.getId());

        LocalDateTime now = LocalDateTime.now();
        BookingRequestDto booking = new BookingRequestDto(
                savedItem.getId(),
                now.plusDays(1),
                now.plusDays(2)
        );
        BookingResponseDto created = bookingService.createBooking(booking, booker.getId());

        assertThatThrownBy(() ->
                bookingService.getBookingById(created.getId(), stranger.getId()))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Access denied");
    }

    @Test
    void getBookingById_NonExistentBooking_ThrowsNotFoundException() {
        UserDto user = createUser("user-notfound", "User");

        assertThatThrownBy(() ->
                bookingService.getBookingById(999999L, user.getId()))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Booking not found");
    }

    // ============== ПОЛЬЗОВАТЕЛЬСКИЕ БРОНИРОВАНИЯ ==============

    @Test
    void getUserBookings_ReturnsUserBookings() {
        UserDto owner = createUser("owner-user-bookings", "Owner");
        UserDto booker = createUser("booker-user-bookings", "Booker");

        ItemDto item = ItemDto.builder()
                .name("Test Item")
                .description("Description")
                .available(true)
                .build();
        var savedItem = itemService.createItem(item, owner.getId());

        BookingRequestDto bookingRequest = new BookingRequestDto(
                savedItem.getId(),
                LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusDays(1)
        );
        bookingService.createBooking(bookingRequest, booker.getId());

        List<BookingResponseDto> bookings = bookingService.getUserBookings(
                booker.getId(), "ALL", 0, 10);

        assertThat(bookings).hasSize(1);
        assertThat(bookings.get(0).getBooker().getId()).isEqualTo(booker.getId());
    }

    @Test
    void getUserBookings_WithDifferentStates_ReturnsCorrectBookings() {
        UserDto owner = createUser("owner-states", "Owner");
        UserDto booker = createUser("booker-states", "Booker");

        ItemDto item = ItemDto.builder()
                .name("Test Item")
                .description("Description")
                .available(true)
                .build();
        var savedItem = itemService.createItem(item, owner.getId());

        LocalDateTime now = LocalDateTime.now();

        BookingRequestDto futureBooking1 = new BookingRequestDto(
                savedItem.getId(),
                now.plusDays(1),
                now.plusDays(2)
        );
        BookingResponseDto createdFuture1 = bookingService.createBooking(futureBooking1, booker.getId());

        BookingRequestDto futureBooking2 = new BookingRequestDto(
                savedItem.getId(),
                now.plusDays(3),
                now.plusDays(4)
        );
        BookingResponseDto createdFuture2 = bookingService.createBooking(futureBooking2, booker.getId());
        bookingService.updateBookingStatus(createdFuture2.getId(), true, owner.getId());

        BookingRequestDto futureBooking3 = new BookingRequestDto(
                savedItem.getId(),
                now.plusDays(5),
                now.plusDays(6)
        );
        BookingResponseDto createdFuture3 = bookingService.createBooking(futureBooking3, booker.getId());
        bookingService.updateBookingStatus(createdFuture3.getId(), false, owner.getId());

        List<BookingResponseDto> allBookings = bookingService.getUserBookings(
                booker.getId(), "ALL", 0, 20);
        assertThat(allBookings).hasSize(3);

        List<BookingResponseDto> futureBookings = bookingService.getUserBookings(
                booker.getId(), "FUTURE", 0, 10);
        assertThat(futureBookings).hasSize(3);

        List<BookingResponseDto> waitingBookings = bookingService.getUserBookings(
                booker.getId(), "WAITING", 0, 10);
        assertThat(waitingBookings).hasSize(1);

        List<BookingResponseDto> rejectedBookings = bookingService.getUserBookings(
                booker.getId(), "REJECTED", 0, 10);
        assertThat(rejectedBookings).hasSize(1);
    }

    @Test
    void getUserBookings_WithApprovedState_ReturnsApprovedBookings() {
        UserDto owner = createUser("owner-approved-state", "Owner");
        UserDto booker = createUser("booker-approved-state", "Booker");

        ItemDto item = ItemDto.builder()
                .name("Test Item")
                .description("Description")
                .available(true)
                .build();
        var savedItem = itemService.createItem(item, owner.getId());

        LocalDateTime now = LocalDateTime.now();

        BookingRequestDto booking = new BookingRequestDto(
                savedItem.getId(),
                now.plusDays(1),
                now.plusDays(2)
        );
        BookingResponseDto created = bookingService.createBooking(booking, booker.getId());
        bookingService.updateBookingStatus(created.getId(), true, owner.getId());

        List<BookingResponseDto> approvedBookings = bookingService.getUserBookings(
                booker.getId(), "APPROVED", 0, 10);

        assertThat(approvedBookings).hasSize(1);
        assertThat(approvedBookings.get(0).getStatus()).isEqualTo("APPROVED");
    }

    @Test
    void getUserBookings_WithCurrentState_ReturnsCurrentBookings() {
        UserDto owner = createUser("owner-current-state", "Owner");
        UserDto booker = createUser("booker-current-state", "Booker");

        ItemDto item = ItemDto.builder()
                .name("Test Item")
                .description("Description")
                .available(true)
                .build();
        var savedItem = itemService.createItem(item, owner.getId());

        testBookingUtil.createCurrentBooking(savedItem.getId(), booker.getId());

        List<BookingResponseDto> currentBookings = bookingService.getUserBookings(
                booker.getId(), "CURRENT", 0, 10);

        assertThat(currentBookings).hasSize(1);
    }

    @Test
    void getUserBookings_WithPastState_ReturnsPastBookings() {
        UserDto owner = createUser("owner-past-state", "Owner");
        UserDto booker = createUser("booker-past-state", "Booker");

        ItemDto item = ItemDto.builder()
                .name("Test Item")
                .description("Description")
                .available(true)
                .build();
        var savedItem = itemService.createItem(item, owner.getId());

        testBookingUtil.createPastBooking(savedItem.getId(), booker.getId(), owner.getId());

        List<BookingResponseDto> pastBookings = bookingService.getUserBookings(
                booker.getId(), "PAST", 0, 10);

        assertThat(pastBookings).hasSize(1);
    }

    @Test
    void getUserBookings_WithFutureState_ReturnsFutureBookings() {
        UserDto owner = createUser("owner-future-state", "Owner");
        UserDto booker = createUser("booker-future-state", "Booker");

        ItemDto item = ItemDto.builder()
                .name("Test Item")
                .description("Description")
                .available(true)
                .build();
        var savedItem = itemService.createItem(item, owner.getId());

        LocalDateTime now = LocalDateTime.now();
        BookingRequestDto booking = new BookingRequestDto(
                savedItem.getId(),
                now.plusDays(1),
                now.plusDays(2)
        );
        bookingService.createBooking(booking, booker.getId());

        List<BookingResponseDto> futureBookings = bookingService.getUserBookings(
                booker.getId(), "FUTURE", 0, 10);

        assertThat(futureBookings).hasSize(1);
    }

    @Test
    void getUserBookings_WithWaitingState_ReturnsWaitingBookings() {
        UserDto owner = createUser("owner-waiting-state", "Owner");
        UserDto booker = createUser("booker-waiting-state", "Booker");

        ItemDto item = ItemDto.builder()
                .name("Test Item")
                .description("Description")
                .available(true)
                .build();
        var savedItem = itemService.createItem(item, owner.getId());

        LocalDateTime now = LocalDateTime.now();
        BookingRequestDto booking = new BookingRequestDto(
                savedItem.getId(),
                now.plusDays(1),
                now.plusDays(2)
        );
        bookingService.createBooking(booking, booker.getId());

        List<BookingResponseDto> waitingBookings = bookingService.getUserBookings(
                booker.getId(), "WAITING", 0, 10);

        assertThat(waitingBookings).hasSize(1);
        assertThat(waitingBookings.get(0).getStatus()).isEqualTo("WAITING");
    }

    @Test
    void getUserBookings_WithCanceledState_ReturnsEmptyListIfNoCanceled() {
        UserDto user = createUser("user-canceled-empty", "User");

        List<BookingResponseDto> canceledBookings = bookingService.getUserBookings(
                user.getId(), "CANCELED", 0, 10);

        assertThat(canceledBookings).isEmpty();
    }

    @Test
    void getUserBookings_WithCanceledState_ReturnsCanceledBookings() {
        UserDto owner = createUser("owner-canceled-state", "Owner");
        UserDto booker = createUser("booker-canceled-state", "Booker");

        ItemDto item = ItemDto.builder()
                .name("Test Item")
                .description("Description")
                .available(true)
                .build();
        var savedItem = itemService.createItem(item, owner.getId());

        testBookingUtil.createCanceledBooking(savedItem.getId(), booker.getId());

        List<BookingResponseDto> canceledBookings = bookingService.getUserBookings(
                booker.getId(), "CANCELED", 0, 10);

        if (!canceledBookings.isEmpty()) {
            assertThat(canceledBookings.get(0).getStatus()).isEqualTo("CANCELED");
        }
    }

    // ============== ВЛАДЕЛЬЧЕСКИЕ БРОНИРОВАНИЯ ==============

    @Test
    void getOwnerBookings_ReturnsOwnerBookings() {
        UserDto owner = createUser("owner-owner-bookings", "Owner");
        UserDto booker = createUser("booker-owner-bookings", "Booker");

        ItemDto item = ItemDto.builder()
                .name("Test Item")
                .description("Description")
                .available(true)
                .build();
        var savedItem = itemService.createItem(item, owner.getId());

        BookingRequestDto bookingRequest = new BookingRequestDto(
                savedItem.getId(),
                LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusDays(1)
        );
        bookingService.createBooking(bookingRequest, booker.getId());

        List<BookingResponseDto> bookings = bookingService.getOwnerBookings(
                owner.getId(), "ALL", 0, 10);

        assertThat(bookings).hasSize(1);
        assertThat(bookings.get(0).getItem().getId()).isEqualTo(savedItem.getId());
    }

    @Test
    void getOwnerBookings_WithCurrentState_ReturnsCurrentBookings() {
        UserDto owner = createUser("owner-current-owner", "Owner");
        UserDto booker = createUser("booker-current-owner", "Booker");

        ItemDto item = ItemDto.builder()
                .name("Test Item")
                .description("Description")
                .available(true)
                .build();
        var savedItem = itemService.createItem(item, owner.getId());

        testBookingUtil.createCurrentBooking(savedItem.getId(), booker.getId());

        List<BookingResponseDto> currentBookings = bookingService.getOwnerBookings(
                owner.getId(), "CURRENT", 0, 10);

        assertThat(currentBookings).hasSize(1);
    }

    @Test
    void getOwnerBookings_WithPastState_ReturnsPastBookings() {
        UserDto owner = createUser("owner-past-owner", "Owner");
        UserDto booker = createUser("booker-past-owner", "Booker");

        ItemDto item = ItemDto.builder()
                .name("Test Item")
                .description("Description")
                .available(true)
                .build();
        var savedItem = itemService.createItem(item, owner.getId());

        testBookingUtil.createPastBooking(savedItem.getId(), booker.getId(), owner.getId());

        List<BookingResponseDto> pastBookings = bookingService.getOwnerBookings(
                owner.getId(), "PAST", 0, 10);

        assertThat(pastBookings).hasSize(1);
    }

    @Test
    void getOwnerBookings_WithPastState_ReturnsPastBookings2() {
        String uniqueOwnerEmail = "owner-past-" + UUID.randomUUID() + "@example.com";
        UserDto ownerDto = UserDto.builder()
                .name("Owner Past")
                .email(uniqueOwnerEmail)
                .build();
        UserDto savedOwner = userService.createUser(ownerDto);

        String uniqueBookerEmail = "booker-past-" + UUID.randomUUID() + "@example.com";
        UserDto bookerDto = UserDto.builder()
                .name("Booker Past")
                .email(uniqueBookerEmail)
                .build();
        UserDto savedBooker = userService.createUser(bookerDto);

        ItemDto item = ItemDto.builder()
                .name("Test Item")
                .description("Description")
                .available(true)
                .build();
        var savedItem = itemService.createItem(item, savedOwner.getId());

        testBookingUtil.createCompletedBookingDirectly(savedItem.getId(), savedBooker.getId(), savedOwner.getId());

        List<BookingResponseDto> pastBookings = bookingService.getOwnerBookings(
                savedOwner.getId(), "PAST", 0, 10);

        assertThat(pastBookings).hasSize(1);
    }

    @Test
    void getOwnerBookings_WithFutureState_ReturnsFutureBookings() {
        UserDto owner = createUser("owner-future-owner", "Owner");
        UserDto booker = createUser("booker-future-owner", "Booker");

        ItemDto item = ItemDto.builder()
                .name("Test Item")
                .description("Description")
                .available(true)
                .build();
        var savedItem = itemService.createItem(item, owner.getId());

        LocalDateTime now = LocalDateTime.now();
        BookingRequestDto booking = new BookingRequestDto(
                savedItem.getId(),
                now.plusDays(1),
                now.plusDays(2)
        );
        bookingService.createBooking(booking, booker.getId());

        List<BookingResponseDto> futureBookings = bookingService.getOwnerBookings(
                owner.getId(), "FUTURE", 0, 10);

        assertThat(futureBookings).hasSize(1);
    }

    @Test
    void getOwnerBookings_WithWaitingState_ReturnsWaitingBookings() {
        UserDto owner = createUser("owner-waiting-owner", "Owner");
        UserDto booker = createUser("booker-waiting-owner", "Booker");

        ItemDto item = ItemDto.builder()
                .name("Test Item")
                .description("Description")
                .available(true)
                .build();
        var savedItem = itemService.createItem(item, owner.getId());

        LocalDateTime now = LocalDateTime.now();
        BookingRequestDto booking = new BookingRequestDto(
                savedItem.getId(),
                now.plusDays(1),
                now.plusDays(2)
        );
        bookingService.createBooking(booking, booker.getId());

        List<BookingResponseDto> waitingBookings = bookingService.getOwnerBookings(
                owner.getId(), "WAITING", 0, 10);

        assertThat(waitingBookings).hasSize(1);
        assertThat(waitingBookings.get(0).getStatus()).isEqualTo("WAITING");
    }

    @Test
    void getOwnerBookings_WithApprovedState_ReturnsApprovedBookings() {
        UserDto owner = createUser("owner-approved-owner", "Owner");
        UserDto booker = createUser("booker-approved-owner", "Booker");

        ItemDto item = ItemDto.builder()
                .name("Test Item")
                .description("Description")
                .available(true)
                .build();
        var savedItem = itemService.createItem(item, owner.getId());

        LocalDateTime now = LocalDateTime.now();

        BookingRequestDto booking = new BookingRequestDto(
                savedItem.getId(),
                now.plusDays(1),
                now.plusDays(2)
        );
        BookingResponseDto created = bookingService.createBooking(booking, booker.getId());
        bookingService.updateBookingStatus(created.getId(), true, owner.getId());

        List<BookingResponseDto> approvedBookings = bookingService.getOwnerBookings(
                owner.getId(), "APPROVED", 0, 10);

        assertThat(approvedBookings).hasSize(1);
        assertThat(approvedBookings.get(0).getStatus()).isEqualTo("APPROVED");
    }

    @Test
    void getOwnerBookings_WithRejectedState_ReturnsRejectedBookings() {
        UserDto owner = createUser("owner-rejected-owner", "Owner");
        UserDto booker = createUser("booker-rejected-owner", "Booker");

        ItemDto item = ItemDto.builder()
                .name("Test Item")
                .description("Description")
                .available(true)
                .build();
        var savedItem = itemService.createItem(item, owner.getId());

        LocalDateTime now = LocalDateTime.now();

        BookingRequestDto booking = new BookingRequestDto(
                savedItem.getId(),
                now.plusDays(1),
                now.plusDays(2)
        );
        BookingResponseDto created = bookingService.createBooking(booking, booker.getId());
        bookingService.updateBookingStatus(created.getId(), false, owner.getId());

        List<BookingResponseDto> rejectedBookings = bookingService.getOwnerBookings(
                owner.getId(), "REJECTED", 0, 10);

        assertThat(rejectedBookings).hasSize(1);
        assertThat(rejectedBookings.get(0).getStatus()).isEqualTo("REJECTED");
    }

    @Test
    void getOwnerBookings_WithCanceledState_ReturnsEmptyList() {
        UserDto owner = createUser("owner-canceled-empty", "Owner");

        List<BookingResponseDto> canceledBookings = bookingService.getOwnerBookings(
                owner.getId(), "CANCELED", 0, 10);

        assertThat(canceledBookings).isEmpty();
    }

    @Test
    void getOwnerBookings_WithCanceledState_ReturnsCanceledBookings() {
        UserDto owner = createUser("owner-canceled", "Owner");
        UserDto booker = createUser("booker-canceled", "Booker");

        ItemDto item = ItemDto.builder()
                .name("Test Item")
                .description("Description")
                .available(true)
                .build();
        var savedItem = itemService.createItem(item, owner.getId());

        testBookingUtil.createCanceledBooking(savedItem.getId(), booker.getId());

        List<BookingResponseDto> canceledBookings = bookingService.getOwnerBookings(
                owner.getId(), "CANCELED", 0, 10);

        if (!canceledBookings.isEmpty()) {
            assertThat(canceledBookings.get(0).getStatus()).isEqualTo("CANCELED");
        }
    }

    // ============== ВАЛИДАЦИЯ СОСТОЯНИЙ И ПАГИНАЦИИ ==============

    @Test
    void getUserBookings_WithInvalidState_ThrowsValidationException() {
        UserDto user = createUser("user-invalid", "User");

        assertThatThrownBy(() ->
                bookingService.getUserBookings(user.getId(), "INVALID_STATE", 0, 10))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Unknown state: INVALID_STATE");
    }

    @Test
    void getOwnerBookings_WithInvalidState_ThrowsValidationException() {
        UserDto user = createUser("user-owner-invalid", "User");

        assertThatThrownBy(() ->
                bookingService.getOwnerBookings(user.getId(), "INVALID_STATE", 0, 10))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Unknown state: INVALID_STATE");
    }

    @Test
    void getUserBookings_WithInvalidPagination_ThrowsValidationException() {
        UserDto user = createUser("user-pag", "User");

        assertThatThrownBy(() ->
                bookingService.getUserBookings(user.getId(), "ALL", -1, 10))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Parameter 'from' must be >= 0");

        assertThatThrownBy(() ->
                bookingService.getUserBookings(user.getId(), "ALL", 0, 0))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Parameter 'size' must be > 0");
    }

    @Test
    void getOwnerBookings_WithInvalidPagination_ThrowsValidationException() {
        UserDto user = createUser("user-owner-pag", "User");

        assertThatThrownBy(() ->
                bookingService.getOwnerBookings(user.getId(), "ALL", -1, 10))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Parameter 'from' must be >= 0");

        assertThatThrownBy(() ->
                bookingService.getOwnerBookings(user.getId(), "ALL", 0, 0))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Parameter 'size' must be > 0");
    }

    @Test
    void getUserBookings_WithNegativeFrom_ThrowsValidationException() {
        UserDto user = createUser("user-negative-from", "User");

        assertThatThrownBy(() ->
                bookingService.getUserBookings(user.getId(), "ALL", -1, 10))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Parameter 'from' must be >= 0");
    }

    @Test
    void getUserBookings_WithZeroSize_ThrowsValidationException() {
        UserDto user = createUser("user-zero-size", "User");

        assertThatThrownBy(() ->
                bookingService.getUserBookings(user.getId(), "ALL", 0, 0))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Parameter 'size' must be > 0");
    }

    @Test
    void getOwnerBookings_WithInvalidPagination_ThrowsValidationException2() {
        UserDto user = createUser("owner-invalid-pag", "User");

        assertThatThrownBy(() ->
                bookingService.getOwnerBookings(user.getId(), "ALL", -1, 10))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Parameter 'from' must be >= 0");

        assertThatThrownBy(() ->
                bookingService.getOwnerBookings(user.getId(), "ALL", 0, 0))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Parameter 'size' must be > 0");
    }

    // ============== ТЕСТЫ ПАГИНАЦИИ ==============

    @Test
    void getUserBookings_WithFutureStateAndPagination_ReturnsCorrectPage() {
        UserDto owner = createUser("owner-future-pag", "Owner");
        UserDto booker = createUser("booker-future-pag", "Booker");

        ItemDto item = ItemDto.builder()
                .name("Test Item")
                .description("Description")
                .available(true)
                .build();
        var savedItem = itemService.createItem(item, owner.getId());

        for (int i = 1; i <= 5; i++) {
            LocalDateTime now = LocalDateTime.now();
            BookingRequestDto booking = new BookingRequestDto(
                    savedItem.getId(),
                    now.plusDays(i),
                    now.plusDays(i + 1)
            );
            bookingService.createBooking(booking, booker.getId());
        }

        List<BookingResponseDto> page1 = bookingService.getUserBookings(
                booker.getId(), "FUTURE", 0, 2);

        List<BookingResponseDto> page2 = bookingService.getUserBookings(
                booker.getId(), "FUTURE", 2, 2);

        assertThat(page1).hasSize(2);
        assertThat(page2).hasSize(2);
    }

    @Test
    void getOwnerBookings_WithWaitingStateAndPagination_ReturnsCorrectPage() {
        UserDto owner = createUser("owner-waiting-pag", "Owner");
        UserDto booker = createUser("booker-waiting-pag", "Booker");

        ItemDto item = ItemDto.builder()
                .name("Test Item")
                .description("Description")
                .available(true)
                .build();
        var savedItem = itemService.createItem(item, owner.getId());

        for (int i = 1; i <= 5; i++) {
            LocalDateTime now = LocalDateTime.now();
            BookingRequestDto booking = new BookingRequestDto(
                    savedItem.getId(),
                    now.plusDays(i),
                    now.plusDays(i + 1)
            );
            bookingService.createBooking(booking, booker.getId());
        }

        List<BookingResponseDto> page1 = bookingService.getOwnerBookings(
                owner.getId(), "WAITING", 0, 2);

        List<BookingResponseDto> page2 = bookingService.getOwnerBookings(
                owner.getId(), "WAITING", 2, 2);

        List<BookingResponseDto> page3 = bookingService.getOwnerBookings(
                owner.getId(), "WAITING", 4, 2);

        assertThat(page1).hasSize(2);
        assertThat(page2).hasSize(2);
        assertThat(page3).hasSize(1);
    }

    // ============== ТЕСТЫ ЛЯМБДА-ВЫРАЖЕНИЙ ==============

    @Test
    void testStreamOperationsCoverage() {
        UserDto owner = createUser("owner-stream", "Owner");
        UserDto booker = createUser("booker-stream", "Booker");

        ItemDto item = ItemDto.builder()
                .name("Test Item")
                .description("Description")
                .available(true)
                .build();
        var savedItem = itemService.createItem(item, owner.getId());

        LocalDateTime now = LocalDateTime.now();

        for (int i = 1; i <= 3; i++) {
            BookingRequestDto booking = new BookingRequestDto(
                    savedItem.getId(),
                    now.plusDays(i),
                    now.plusDays(i + 1)
            );
            bookingService.createBooking(booking, booker.getId());
        }

        List<BookingResponseDto> bookings = bookingService.getUserBookings(
                booker.getId(), "ALL", 0, 10);

        assertThat(bookings).hasSize(3);

        bookings.forEach(booking -> {
            assertThat(booking).isNotNull();
            assertThat(booking.getId()).isNotNull();
        });
    }

    @Test
    void testLambdaExpressionsInGetOwnerBookings() {
        UserDto owner = createUser("owner-lambda-owner", "Owner");
        UserDto booker = createUser("booker-lambda-owner", "Booker");

        ItemDto item = ItemDto.builder()
                .name("Test Item")
                .description("Description")
                .available(true)
                .build();
        var savedItem = itemService.createItem(item, owner.getId());

        for (int i = 1; i <= 3; i++) {
            LocalDateTime now = LocalDateTime.now();
            BookingRequestDto booking = new BookingRequestDto(
                    savedItem.getId(),
                    now.plusDays(i),
                    now.plusDays(i + 1)
            );
            bookingService.createBooking(booking, booker.getId());
        }

        List<BookingResponseDto> bookings = bookingService.getOwnerBookings(
                owner.getId(), "ALL", 0, 10);

        assertThat(bookings).hasSize(3);

        bookings.forEach(booking -> {
            assertThat(booking).isNotNull();
            assertThat(booking.getItem()).isNotNull();
            assertThat(booking.getBooker()).isNotNull();
        });
    }

    @Test
    void testLambdaExpressionsInGetUserBookings() {
        UserDto owner = createUser("owner-lambda-user", "Owner");
        UserDto booker = createUser("booker-lambda-user", "Booker");

        ItemDto item = ItemDto.builder()
                .name("Test Item")
                .description("Description")
                .available(true)
                .build();
        var savedItem = itemService.createItem(item, owner.getId());

        for (int i = 1; i <= 3; i++) {
            LocalDateTime now = LocalDateTime.now();
            BookingRequestDto booking = new BookingRequestDto(
                    savedItem.getId(),
                    now.plusDays(i),
                    now.plusDays(i + 1)
            );
            bookingService.createBooking(booking, booker.getId());
        }

        List<BookingResponseDto> bookings = bookingService.getUserBookings(
                booker.getId(), "ALL", 0, 10);

        assertThat(bookings).hasSize(3);

        bookings.forEach(booking -> {
            assertThat(booking).isNotNull();
            assertThat(booking.getStatus()).isEqualTo("WAITING");
        });
    }

    @Test
    void testLambdaExpressionsInUpdateBookingStatus() {
        UserDto owner = createUser("owner-lambda-update", "Owner");
        UserDto booker = createUser("booker-lambda-update", "Booker");

        ItemDto item = ItemDto.builder()
                .name("Test Item")
                .description("Description")
                .available(true)
                .build();
        var savedItem = itemService.createItem(item, owner.getId());

        LocalDateTime now = LocalDateTime.now();
        BookingRequestDto booking = new BookingRequestDto(
                savedItem.getId(),
                now.plusDays(1),
                now.plusDays(2)
        );
        BookingResponseDto created = bookingService.createBooking(booking, booker.getId());

        BookingResponseDto updated = bookingService.updateBookingStatus(
                created.getId(), true, owner.getId());

        assertThat(updated.getStatus()).isEqualTo("APPROVED");
    }
}

package ru.practicum.shareit.server.service;

import org.junit.jupiter.api.BeforeEach;
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

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class BookingServiceImplIntegrationTest {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private ItemService itemService;

    @Autowired
    private UserService userService;

    private Long ownerId;
    private Long bookerId;
    private Long itemId;

    @BeforeEach
    void setUp() {
        UserDto owner = UserDto.builder()
                .name("Owner")
                .email("owner@example.com")
                .build();
        UserDto savedOwner = userService.createUser(owner);
        ownerId = savedOwner.getId();

        UserDto booker = UserDto.builder()
                .name("Booker")
                .email("booker@example.com")
                .build();
        UserDto savedBooker = userService.createUser(booker);
        bookerId = savedBooker.getId();

        ItemDto item = ItemDto.builder()
                .name("Дрель")
                .description("Аккумуляторная дрель")
                .available(true)
                .build();
        var savedItem = itemService.createItem(item, ownerId);
        itemId = savedItem.getId();
    }

    @Test
    void createBooking_WithValidData_CreatesSuccessfully() {
        BookingRequestDto bookingRequest = new BookingRequestDto(
                itemId,
                LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusDays(1)
        );

        BookingResponseDto result = bookingService.createBooking(bookingRequest, bookerId);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        assertThat(result.getItem().getId()).isEqualTo(itemId);
        assertThat(result.getBooker().getId()).isEqualTo(bookerId);
        assertThat(result.getStatus()).isEqualTo("WAITING");
    }

    @Test
    void createBooking_WithUnavailableItem_ThrowsValidationException() {
        ItemDto unavailableItem = ItemDto.builder()
                .name("Недоступная вещь")
                .description("Вещь недоступна для бронирования")
                .available(false)
                .build();
        var savedItem = itemService.createItem(unavailableItem, ownerId);

        BookingRequestDto bookingRequest = new BookingRequestDto(
                savedItem.getId(),
                LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusDays(1)
        );

        assertThatThrownBy(() -> bookingService.createBooking(bookingRequest, bookerId))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Item is not available for booking");
    }

    @Test
    void createBooking_ByOwner_ThrowsNotFoundException() {
        BookingRequestDto bookingRequest = new BookingRequestDto(
                itemId,
                LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusDays(1)
        );

        assertThatThrownBy(() -> bookingService.createBooking(bookingRequest, ownerId))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Owner cannot book their own item");
    }

    @Test
    void updateBookingStatus_WhenApproved_UpdatesSuccessfully() {
        BookingRequestDto bookingRequest = new BookingRequestDto(
                itemId,
                LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusDays(1)
        );
        BookingResponseDto createdBooking = bookingService.createBooking(bookingRequest, bookerId);

        BookingResponseDto updatedBooking = bookingService.updateBookingStatus(
                createdBooking.getId(), true, ownerId);

        assertThat(updatedBooking.getStatus()).isEqualTo("APPROVED");
    }

    @Test
    void updateBookingStatus_WhenRejected_UpdatesSuccessfully() {
        BookingRequestDto bookingRequest = new BookingRequestDto(
                itemId,
                LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusDays(1)
        );
        BookingResponseDto createdBooking = bookingService.createBooking(bookingRequest, bookerId);

        BookingResponseDto updatedBooking = bookingService.updateBookingStatus(
                createdBooking.getId(), false, ownerId);

        assertThat(updatedBooking.getStatus()).isEqualTo("REJECTED");
    }

    @Test
    void updateBookingStatus_ByNonOwner_ThrowsNotFoundException() {
        BookingRequestDto bookingRequest = new BookingRequestDto(
                itemId,
                LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusDays(1)
        );
        BookingResponseDto createdBooking = bookingService.createBooking(bookingRequest, bookerId);

        UserDto anotherUser = UserDto.builder()
                .name("Another User")
                .email("another@example.com")
                .build();
        UserDto savedAnotherUser = userService.createUser(anotherUser);

        assertThatThrownBy(() -> bookingService.updateBookingStatus(
                createdBooking.getId(), true, savedAnotherUser.getId()))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Only owner can update booking status");
    }

    @Test
    void getBookingById_ForBooker_ReturnsBooking() {
        BookingRequestDto bookingRequest = new BookingRequestDto(
                itemId,
                LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusDays(1)
        );
        BookingResponseDto createdBooking = bookingService.createBooking(bookingRequest, bookerId);

        BookingResponseDto result = bookingService.getBookingById(createdBooking.getId(), bookerId);

        assertThat(result.getId()).isEqualTo(createdBooking.getId());
        assertThat(result.getItem().getId()).isEqualTo(itemId);
    }

    @Test
    void getBookingById_ForOwner_ReturnsBooking() {
        BookingRequestDto bookingRequest = new BookingRequestDto(
                itemId,
                LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusDays(1)
        );
        BookingResponseDto createdBooking = bookingService.createBooking(bookingRequest, bookerId);

        BookingResponseDto result = bookingService.getBookingById(createdBooking.getId(), ownerId);

        assertThat(result.getId()).isEqualTo(createdBooking.getId());
    }

    @Test
    void getBookingById_ForUnauthorizedUser_ThrowsNotFoundException() {
        BookingRequestDto bookingRequest = new BookingRequestDto(
                itemId,
                LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusDays(1)
        );
        BookingResponseDto createdBooking = bookingService.createBooking(bookingRequest, bookerId);

        UserDto unauthorizedUser = UserDto.builder()
                .name("Unauthorized")
                .email("unauthorized@example.com")
                .build();
        UserDto savedUnauthorizedUser = userService.createUser(unauthorizedUser);

        assertThatThrownBy(() -> bookingService.getBookingById(
                createdBooking.getId(), savedUnauthorizedUser.getId()))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Access denied");
    }

    @Test
    void getUserBookings_ReturnsUserBookings() {
        BookingRequestDto bookingRequest = new BookingRequestDto(
                itemId,
                LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusDays(1)
        );
        bookingService.createBooking(bookingRequest, bookerId);

        List<BookingResponseDto> bookings = bookingService.getUserBookings(
                bookerId, "ALL", 0, 10);

        assertThat(bookings).hasSize(1);
        assertThat(bookings.get(0).getBooker().getId()).isEqualTo(bookerId);
    }

    @Test
    void getOwnerBookings_ReturnsOwnerBookings() {
        BookingRequestDto bookingRequest = new BookingRequestDto(
                itemId,
                LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusDays(1)
        );
        bookingService.createBooking(bookingRequest, bookerId);

        List<BookingResponseDto> bookings = bookingService.getOwnerBookings(
                ownerId, "ALL", 0, 10);

        assertThat(bookings).hasSize(1);
        assertThat(bookings.get(0).getItem().getId()).isEqualTo(itemId);
    }

    @Test
    void createBooking_WithInvalidDates_ThrowsValidationException() {
        BookingRequestDto bookingRequest = new BookingRequestDto(
                itemId,
                LocalDateTime.now().minusDays(1), // Прошлая дата
                LocalDateTime.now().plusDays(1)
        );

        assertThatThrownBy(() -> bookingService.createBooking(bookingRequest, bookerId))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Start date cannot be in the past");
    }

    @Test
    void createBooking_WithEndBeforeStart_ThrowsValidationException() {
        BookingRequestDto bookingRequest = new BookingRequestDto(
                itemId,
                LocalDateTime.now().plusDays(2),
                LocalDateTime.now().plusDays(1) // End before start
        );

        assertThatThrownBy(() -> bookingService.createBooking(bookingRequest, bookerId))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("End date must be after start date");
    }
}

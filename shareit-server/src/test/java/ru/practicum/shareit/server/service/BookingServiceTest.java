package ru.practicum.shareit.server.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import ru.practicum.shareit.dto.BookingRequestDto;
import ru.practicum.shareit.dto.BookingResponseDto;
import ru.practicum.shareit.server.exception.NotFoundException;
import ru.practicum.shareit.server.exception.ValidationException;
import ru.practicum.shareit.server.mapper.BookingMapperImpl;
import ru.practicum.shareit.server.model.*;
import ru.practicum.shareit.server.repository.BookingRepository;
import ru.practicum.shareit.server.repository.ItemRepository;
import ru.practicum.shareit.server.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@Import({BookingServiceImpl.class, BookingMapperImpl.class})
class BookingServiceTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private BookingServiceImpl bookingService;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRepository itemRepository;

    private User owner;
    private User booker;
    private Item availableItem;
    private Item unavailableItem;

    @BeforeEach
    void setUp() {
        // Очистка базы данных перед каждым тестом
        bookingRepository.deleteAll();
        itemRepository.deleteAll();
        userRepository.deleteAll();

        // Создание тестовых данных
        owner = User.builder()
                .name("Владелец")
                .email("owner@example.com")
                .build();
        entityManager.persist(owner);

        booker = User.builder()
                .name("Бронирующий")
                .email("booker@example.com")
                .build();
        entityManager.persist(booker);

        availableItem = Item.builder()
                .name("Дрель")
                .description("Аккумуляторная дрель")
                .available(true)
                .owner(owner)
                .build();
        entityManager.persist(availableItem);

        unavailableItem = Item.builder()
                .name("Недоступная вещь")
                .description("Вещь недоступна для бронирования")
                .available(false)
                .owner(owner)
                .build();
        entityManager.persist(unavailableItem);

        entityManager.flush();
    }

    @Test
    void createBooking_WithValidData_CreatesSuccessfully() {
        // Given
        BookingRequestDto bookingRequest = new BookingRequestDto(
                availableItem.getId(),
                LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusDays(1)
        );

        // When
        BookingResponseDto result = bookingService.createBooking(bookingRequest, booker.getId());

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        assertThat(result.getItem().getId()).isEqualTo(availableItem.getId());
        assertThat(result.getBooker().getId()).isEqualTo(booker.getId());
        assertThat(result.getStatus()).isEqualTo("WAITING");
    }

    @Test
    void createBooking_WithUnavailableItem_ThrowsValidationException() {
        // Given
        BookingRequestDto bookingRequest = new BookingRequestDto(
                unavailableItem.getId(),
                LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusDays(1)
        );

        // When & Then
        assertThatThrownBy(() -> bookingService.createBooking(bookingRequest, booker.getId()))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Item is not available for booking");
    }

    @Test
    void createBooking_ByOwner_ThrowsNotFoundException() {
        // Given
        BookingRequestDto bookingRequest = new BookingRequestDto(
                availableItem.getId(),
                LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusDays(1)
        );

        // When & Then
        assertThatThrownBy(() -> bookingService.createBooking(bookingRequest, owner.getId()))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Owner cannot book their own item");
    }

    @Test
    void createBooking_WithEndBeforeStart_ThrowsValidationException() {
        // Given
        BookingRequestDto bookingRequest = new BookingRequestDto(
                availableItem.getId(),
                LocalDateTime.now().plusDays(2),
                LocalDateTime.now().plusDays(1)
        );

        // When & Then
        assertThatThrownBy(() -> bookingService.createBooking(bookingRequest, booker.getId()))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("End date must be after start date");
    }

    @Test
    void updateBookingStatus_WhenApproved_UpdatesSuccessfully() {
        // Given
        Booking booking = Booking.builder()
                .start(LocalDateTime.now().plusHours(1))
                .end(LocalDateTime.now().plusDays(1))
                .item(availableItem)
                .booker(booker)
                .status(BookingStatus.WAITING)
                .build();
        entityManager.persist(booking);
        entityManager.flush();

        // When
        BookingResponseDto updatedBooking = bookingService.updateBookingStatus(
                booking.getId(), true, owner.getId());

        // Then
        assertThat(updatedBooking.getStatus()).isEqualTo("APPROVED");
    }

    @Test
    void updateBookingStatus_WhenRejected_UpdatesSuccessfully() {
        // Given
        Booking booking = Booking.builder()
                .start(LocalDateTime.now().plusHours(1))
                .end(LocalDateTime.now().plusDays(1))
                .item(availableItem)
                .booker(booker)
                .status(BookingStatus.WAITING)
                .build();
        entityManager.persist(booking);
        entityManager.flush();

        // When
        BookingResponseDto updatedBooking = bookingService.updateBookingStatus(
                booking.getId(), false, owner.getId());

        // Then
        assertThat(updatedBooking.getStatus()).isEqualTo("REJECTED");
    }

    @Test
    void updateBookingStatus_ByNonOwner_ThrowsNotFoundException() {
        // Given
        Booking booking = Booking.builder()
                .start(LocalDateTime.now().plusHours(1))
                .end(LocalDateTime.now().plusDays(1))
                .item(availableItem)
                .booker(booker)
                .status(BookingStatus.WAITING)
                .build();
        entityManager.persist(booking);
        entityManager.flush();

        User anotherUser = User.builder()
                .name("Другой пользователь")
                .email("another@example.com")
                .build();
        entityManager.persist(anotherUser);
        entityManager.flush();

        // When & Then
        assertThatThrownBy(() -> bookingService.updateBookingStatus(
                booking.getId(), true, anotherUser.getId()))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Only owner can update booking status");
    }

    @Test
    void updateBookingStatus_WhenAlreadyApproved_ThrowsValidationException() {
        // Given
        Booking booking = Booking.builder()
                .start(LocalDateTime.now().plusHours(1))
                .end(LocalDateTime.now().plusDays(1))
                .item(availableItem)
                .booker(booker)
                .status(BookingStatus.APPROVED)
                .build();
        entityManager.persist(booking);
        entityManager.flush();

        // When & Then
        assertThatThrownBy(() -> bookingService.updateBookingStatus(
                booking.getId(), false, owner.getId()))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Booking status cannot be changed");
    }

    @Test
    void getBookingById_ForBooker_ReturnsBooking() {
        // Given
        Booking booking = Booking.builder()
                .start(LocalDateTime.now().plusHours(1))
                .end(LocalDateTime.now().plusDays(1))
                .item(availableItem)
                .booker(booker)
                .status(BookingStatus.WAITING)
                .build();
        entityManager.persist(booking);
        entityManager.flush();

        // When
        BookingResponseDto result = bookingService.getBookingById(booking.getId(), booker.getId());

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(booking.getId());
        assertThat(result.getBooker().getId()).isEqualTo(booker.getId());
    }

    @Test
    void getBookingById_ForOwner_ReturnsBooking() {
        // Given
        Booking booking = Booking.builder()
                .start(LocalDateTime.now().plusHours(1))
                .end(LocalDateTime.now().plusDays(1))
                .item(availableItem)
                .booker(booker)
                .status(BookingStatus.WAITING)
                .build();
        entityManager.persist(booking);
        entityManager.flush();

        // When
        BookingResponseDto result = bookingService.getBookingById(booking.getId(), owner.getId());

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(booking.getId());
        assertThat(result.getItem().getId()).isEqualTo(availableItem.getId());
    }

    @Test
    void getBookingById_ForUnauthorizedUser_ThrowsNotFoundException() {
        // Given
        Booking booking = Booking.builder()
                .start(LocalDateTime.now().plusHours(1))
                .end(LocalDateTime.now().plusDays(1))
                .item(availableItem)
                .booker(booker)
                .status(BookingStatus.WAITING)
                .build();
        entityManager.persist(booking);
        entityManager.flush();

        User stranger = User.builder()
                .name("Незнакомец")
                .email("stranger@example.com")
                .build();
        entityManager.persist(stranger);
        entityManager.flush();

        // When & Then
        assertThatThrownBy(() -> bookingService.getBookingById(booking.getId(), stranger.getId()))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Access denied");
    }

    @Test
    void getUserBookings_ReturnsUserBookings() {
        // Given
        Booking booking = Booking.builder()
                .start(LocalDateTime.now().plusHours(1))
                .end(LocalDateTime.now().plusDays(1))
                .item(availableItem)
                .booker(booker)
                .status(BookingStatus.WAITING)
                .build();
        entityManager.persist(booking);
        entityManager.flush();

        // When
        List<BookingResponseDto> bookings = bookingService.getUserBookings(
                booker.getId(), "ALL", 0, 10);

        // Then
        assertThat(bookings).hasSize(1);
        assertThat(bookings.get(0).getBooker().getId()).isEqualTo(booker.getId());
    }

    @Test
    void getUserBookings_WithInvalidState_ThrowsValidationException() {
        // When & Then
        assertThatThrownBy(() ->
                bookingService.getUserBookings(booker.getId(), "INVALID_STATE", 0, 10))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Unknown state: INVALID_STATE");
    }

    @Test
    void getUserBookings_WithNegativeFrom_ThrowsValidationException() {
        // When & Then
        assertThatThrownBy(() ->
                bookingService.getUserBookings(booker.getId(), "ALL", -1, 10))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Parameter 'from' must not be negative");
    }

    @Test
    void getUserBookings_WithZeroSize_ThrowsValidationException() {
        // When & Then
        assertThatThrownBy(() ->
                bookingService.getUserBookings(booker.getId(), "ALL", 0, 0))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Parameter 'size' must be positive");
    }

    @Test
    void getOwnerBookings_ReturnsOwnerBookings() {
        // Given
        Booking booking = Booking.builder()
                .start(LocalDateTime.now().plusHours(1))
                .end(LocalDateTime.now().plusDays(1))
                .item(availableItem)
                .booker(booker)
                .status(BookingStatus.WAITING)
                .build();
        entityManager.persist(booking);
        entityManager.flush();

        // When
        List<BookingResponseDto> bookings = bookingService.getOwnerBookings(
                owner.getId(), "ALL", 0, 10);

        // Then
        assertThat(bookings).hasSize(1);
        assertThat(bookings.get(0).getItem().getId()).isEqualTo(availableItem.getId());
    }

    @Test
    void getOwnerBookings_WithInvalidState_ThrowsValidationException() {
        // When & Then
        assertThatThrownBy(() ->
                bookingService.getOwnerBookings(owner.getId(), "INVALID_STATE", 0, 10))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Unknown state: INVALID_STATE");
    }
}

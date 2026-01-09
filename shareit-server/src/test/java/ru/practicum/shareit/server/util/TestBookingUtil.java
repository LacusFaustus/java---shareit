package ru.practicum.shareit.server.util;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.server.model.Booking;
import ru.practicum.shareit.server.model.BookingStatus;
import ru.practicum.shareit.server.model.Item;
import ru.practicum.shareit.server.model.User;
import ru.practicum.shareit.server.repository.BookingRepository;
import ru.practicum.shareit.server.repository.ItemRepository;
import ru.practicum.shareit.server.repository.UserRepository;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Transactional
public class TestBookingUtil {

    private final BookingRepository bookingRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    /**
     * Создает завершенное бронирование (этот метод нужно использовать)
     */
    public void createCompletedBookingDirectly(Long itemId, Long bookerId, Long ownerId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Item not found"));
        User booker = userRepository.findById(bookerId)
                .orElseThrow(() -> new IllegalArgumentException("Booker not found"));

        Booking booking = new Booking();
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStart(LocalDateTime.now().minusDays(2));
        booking.setEnd(LocalDateTime.now().minusDays(1));
        booking.setStatus(BookingStatus.APPROVED);

        bookingRepository.save(booking);
    }

    /**
     * Создает прошедшее бронирование
     */
    public Booking createPastBooking(Long itemId, Long bookerId, Long ownerId) {
        return createCompletedBooking(itemId, bookerId, BookingStatus.APPROVED);
    }

    /**
     * Создает текущее бронирование
     */
    public Booking createCurrentBooking(Long itemId, Long bookerId) {
        Item item = itemRepository.findById(itemId).orElseThrow();
        User booker = userRepository.findById(bookerId).orElseThrow();

        Booking booking = new Booking();
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStart(LocalDateTime.now().minusHours(1));
        booking.setEnd(LocalDateTime.now().plusHours(1));
        booking.setStatus(BookingStatus.APPROVED);

        return bookingRepository.save(booking);
    }

    /**
     * Создает отмененное бронирование
     */
    public Booking createCanceledBooking(Long itemId, Long bookerId) {
        Item item = itemRepository.findById(itemId).orElseThrow();
        User booker = userRepository.findById(bookerId).orElseThrow();

        Booking booking = new Booking();
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStart(LocalDateTime.now().minusDays(2));
        booking.setEnd(LocalDateTime.now().minusDays(1));
        booking.setStatus(BookingStatus.CANCELED);

        return bookingRepository.save(booking);
    }

    /**
     * Создает ожидающее бронирование
     */
    public Booking createWaitingBooking(Long itemId, Long bookerId) {
        return createCompletedBooking(itemId, bookerId, BookingStatus.WAITING);
    }

    /**
     * Создает отклоненное бронирование
     */
    public Booking createRejectedBooking(Long itemId, Long bookerId) {
        return createCompletedBooking(itemId, bookerId, BookingStatus.REJECTED);
    }

    private Booking createCompletedBooking(Long itemId, Long bookerId, BookingStatus status) {
        Item item = itemRepository.findById(itemId).orElseThrow();
        User booker = userRepository.findById(bookerId).orElseThrow();

        Booking booking = new Booking();
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStart(LocalDateTime.now().minusDays(2));
        booking.setEnd(LocalDateTime.now().minusDays(1));
        booking.setStatus(status);

        return bookingRepository.save(booking);
    }

    /**
     * Создает будущее бронирование (для тестирования)
     */
    public Booking createFutureBooking(Long itemId, Long bookerId) {
        Item item = itemRepository.findById(itemId).orElseThrow();
        User booker = userRepository.findById(bookerId).orElseThrow();

        Booking booking = new Booking();
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStart(LocalDateTime.now().plusDays(1));
        booking.setEnd(LocalDateTime.now().plusDays(2));
        booking.setStatus(BookingStatus.APPROVED);

        return bookingRepository.save(booking);
    }
}

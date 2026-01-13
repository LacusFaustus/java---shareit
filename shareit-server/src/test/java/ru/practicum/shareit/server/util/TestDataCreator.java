package ru.practicum.shareit.server.util;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.server.model.*;
import ru.practicum.shareit.server.repository.BookingRepository;
import ru.practicum.shareit.server.repository.ItemRepository;
import ru.practicum.shareit.server.repository.UserRepository;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Transactional
public class TestDataCreator {
    private final BookingRepository bookingRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    /**
     * Создает текущее бронирование (start < now < end) со статусом APPROVED
     */
    public Long createCurrentBooking(Long itemId, Long bookerId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Item not found with id: " + itemId));
        User booker = userRepository.findById(bookerId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + bookerId));

        LocalDateTime now = LocalDateTime.now();
        Booking booking = Booking.builder()
                .item(item)
                .booker(booker)
                .start(now.minusHours(2))  // началось 2 часа назад
                .end(now.plusHours(2))     // закончится через 2 часа
                .status(BookingStatus.APPROVED)
                .build();

        return bookingRepository.save(booking).getId();
    }

    /**
     * Создает прошедшее бронирование (end < now) со статусом APPROVED
     */
    public Long createPastBooking(Long itemId, Long bookerId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Item not found with id: " + itemId));
        User booker = userRepository.findById(bookerId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + bookerId));

        LocalDateTime now = LocalDateTime.now();
        Booking booking = Booking.builder()
                .item(item)
                .booker(booker)
                .start(now.minusDays(3))   // началось 3 дня назад
                .end(now.minusDays(2))     // закончилось 2 дня назад
                .status(BookingStatus.APPROVED)
                .build();

        return bookingRepository.save(booking).getId();
    }

    /**
     * Создает будущее бронирование (start > now) со статусом APPROVED
     */
    public Long createFutureBooking(Long itemId, Long bookerId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Item not found with id: " + itemId));
        User booker = userRepository.findById(bookerId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + bookerId));

        LocalDateTime now = LocalDateTime.now();
        Booking booking = Booking.builder()
                .item(item)
                .booker(booker)
                .start(now.plusDays(1))    // начнется завтра
                .end(now.plusDays(2))      // закончится послезавтра
                .status(BookingStatus.APPROVED)
                .build();

        return bookingRepository.save(booking).getId();
    }

    /**
     * Создает прошедшее бронирование со статусом APPROVED для комментариев
     * (end < now, статус APPROVED)
     */
    public Long createCompletedBookingForComment(Long itemId, Long bookerId) {
        return createPastBooking(itemId, bookerId);
    }

    /**
     * Создает бронирование со статусом WAITING
     */
    public Long createWaitingBooking(Long itemId, Long bookerId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Item not found with id: " + itemId));
        User booker = userRepository.findById(bookerId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + bookerId));

        LocalDateTime now = LocalDateTime.now();
        Booking booking = Booking.builder()
                .item(item)
                .booker(booker)
                .start(now.plusDays(1))
                .end(now.plusDays(2))
                .status(BookingStatus.WAITING)
                .build();

        return bookingRepository.save(booking).getId();
    }

    /**
     * Создает бронирование со статусом REJECTED
     */
    public Long createRejectedBooking(Long itemId, Long bookerId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Item not found with id: " + itemId));
        User booker = userRepository.findById(bookerId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + bookerId));

        LocalDateTime now = LocalDateTime.now();
        Booking booking = Booking.builder()
                .item(item)
                .booker(booker)
                .start(now.plusDays(1))
                .end(now.plusDays(2))
                .status(BookingStatus.REJECTED)
                .build();

        return bookingRepository.save(booking).getId();
    }

    /**
     * Создает бронирование со статусом CANCELED
     */
    public Long createCanceledBooking(Long itemId, Long bookerId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Item not found with id: " + itemId));
        User booker = userRepository.findById(bookerId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + bookerId));

        LocalDateTime now = LocalDateTime.now();
        Booking booking = Booking.builder()
                .item(item)
                .booker(booker)
                .start(now.plusDays(1))
                .end(now.plusDays(2))
                .status(BookingStatus.CANCELED)
                .build();

        return bookingRepository.save(booking).getId();
    }
}

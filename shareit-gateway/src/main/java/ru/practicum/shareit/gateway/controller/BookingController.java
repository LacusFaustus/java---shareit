package ru.practicum.shareit.gateway.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.gateway.client.BookingClient;
import ru.practicum.shareit.dto.BookingRequestDto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import java.time.LocalDateTime;
import java.util.Set;

@Slf4j
@Validated
@RestController
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
public class BookingController {
    private final BookingClient bookingClient;

    private static final Set<String> VALID_STATES = Set.of(
            "ALL", "CURRENT", "PAST", "FUTURE", "WAITING",
            "APPROVED", "REJECTED", "CANCELED"
    );

    @PostMapping
    public ResponseEntity<Object> createBooking(
            @Valid @RequestBody BookingRequestDto bookingRequestDto,
            @RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("POST /bookings - создание бронирования пользователем {}", userId);

        if (!bookingRequestDto.isValid()) {
            throw new IllegalArgumentException("End date must be after start date");
        }

        if (bookingRequestDto.getStart().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Start date cannot be in the past");
        }

        return bookingClient.createBooking(bookingRequestDto, userId);
    }

    @PatchMapping("/{bookingId}")
    public ResponseEntity<Object> updateBookingStatus(
            @PathVariable Long bookingId,
            @RequestParam Boolean approved,
            @RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("PATCH /bookings/{}?approved={} - обновление статуса бронирования пользователем {}",
                bookingId, approved, userId);
        return bookingClient.updateBookingStatus(bookingId, approved, userId);
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<Object> getBookingById(
            @PathVariable Long bookingId,
            @RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("GET /bookings/{} - получение бронирования пользователем {}", bookingId, userId);
        return bookingClient.getBookingById(bookingId, userId);
    }

    @GetMapping
    public ResponseEntity<Object> getUserBookings(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @RequestParam(defaultValue = "ALL") String state,
            @RequestParam(defaultValue = "0") @PositiveOrZero int from,
            @RequestParam(defaultValue = "10") @Positive int size) {
        log.info("GET /bookings?state={}&from={}&size={} - получение бронирований пользователя {}",
                state, from, size, userId);

        if (!VALID_STATES.contains(state.toUpperCase())) {
            throw new IllegalArgumentException("Unknown state: " + state);
        }

        return bookingClient.getUserBookings(userId, state, from, size);
    }

    @GetMapping("/owner")
    public ResponseEntity<Object> getOwnerBookings(
            @RequestHeader("X-Sharer-User-Id") Long ownerId,
            @RequestParam(defaultValue = "ALL") String state,
            @RequestParam(defaultValue = "0") @PositiveOrZero int from,
            @RequestParam(defaultValue = "10") @Positive int size) {
        log.info("GET /bookings/owner?state={}&from={}&size={} - получение бронирований владельца {}",
                state, from, size, ownerId);

        if (!VALID_STATES.contains(state.toUpperCase())) {
            throw new IllegalArgumentException("Unknown state: " + state);
        }

        return bookingClient.getOwnerBookings(ownerId, state, from, size);
    }
}

package ru.practicum.shareit.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.dto.BookingRequestDto;
import ru.practicum.shareit.dto.BookingResponseDto;
import ru.practicum.shareit.service.BookingService;

import jakarta.validation.Valid;
import java.util.List;

@Slf4j
@RestController
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
public class BookingController {
    private final BookingService bookingService;

    @PostMapping
    public ResponseEntity<BookingResponseDto> createBooking(
            @Valid @RequestBody BookingRequestDto bookingRequestDto,
            @RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("POST /bookings - создание бронирования пользователем {}", userId);
        BookingResponseDto createdBooking = bookingService.createBooking(bookingRequestDto, userId);
        return ResponseEntity.ok(createdBooking);
    }

    @PatchMapping("/{bookingId}")
    public ResponseEntity<BookingResponseDto> updateBookingStatus(
            @PathVariable Long bookingId,
            @RequestParam Boolean approved,
            @RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("PATCH /bookings/{}?approved={} - обновление статуса бронирования пользователем {}",
                bookingId, approved, userId);
        BookingResponseDto updatedBooking = bookingService.updateBookingStatus(bookingId, approved, userId);
        return ResponseEntity.ok(updatedBooking);
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<BookingResponseDto> getBookingById(
            @PathVariable Long bookingId,
            @RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("GET /bookings/{} - получение бронирования пользователем {}", bookingId, userId);
        BookingResponseDto booking = bookingService.getBookingById(bookingId, userId);
        return ResponseEntity.ok(booking);
    }

    @GetMapping
    public ResponseEntity<List<BookingResponseDto>> getUserBookings(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @RequestParam(defaultValue = "ALL") String state,
            @RequestParam(defaultValue = "0") int from,
            @RequestParam(defaultValue = "10") int size) {
        log.info("GET /bookings?state={}&from={}&size={} - получение бронирований пользователя {}",
                state, from, size, userId);
        List<BookingResponseDto> bookings = bookingService.getUserBookings(userId, state, from, size);
        return ResponseEntity.ok(bookings);
    }

    @GetMapping("/owner")
    public ResponseEntity<List<BookingResponseDto>> getOwnerBookings(
            @RequestHeader("X-Sharer-User-Id") Long ownerId,
            @RequestParam(defaultValue = "ALL") String state,
            @RequestParam(defaultValue = "0") int from,
            @RequestParam(defaultValue = "10") int size) {
        log.info("GET /bookings/owner?state={}&from={}&size={} - получение бронирований владельца {}",
                state, from, size, ownerId);
        List<BookingResponseDto> bookings = bookingService.getOwnerBookings(ownerId, state, from, size);
        return ResponseEntity.ok(bookings);
    }
}

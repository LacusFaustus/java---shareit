package ru.practicum.shareit.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.dto.BookingDto;
import ru.practicum.shareit.service.BookingService;
import java.util.List;

@RestController
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
public class BookingController {
    private final BookingService bookingService;

    @PostMapping
    public ResponseEntity<BookingDto> createBooking(@RequestBody BookingDto bookingDto,
                                                    @RequestHeader("X-Sharer-User-Id") Long userId) {
        BookingDto createdBooking = bookingService.createBooking(bookingDto, userId);
        return ResponseEntity.ok(createdBooking);
    }

    @PatchMapping("/{bookingId}")
    public ResponseEntity<BookingDto> updateBookingStatus(@PathVariable Long bookingId,
                                                          @RequestParam Boolean approved,
                                                          @RequestHeader("X-Sharer-User-Id") Long userId) {
        BookingDto updatedBooking = bookingService.updateBookingStatus(bookingId, approved, userId);
        return ResponseEntity.ok(updatedBooking);
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<BookingDto> getBookingById(@PathVariable Long bookingId,
                                                     @RequestHeader("X-Sharer-User-Id") Long userId) {
        BookingDto booking = bookingService.getBookingById(bookingId, userId);
        return ResponseEntity.ok(booking);
    }

    @GetMapping
    public ResponseEntity<List<BookingDto>> getUserBookings(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                            @RequestParam(defaultValue = "ALL") String state) {
        List<BookingDto> bookings = bookingService.getUserBookings(userId, state);
        return ResponseEntity.ok(bookings);
    }

    @GetMapping("/owner")
    public ResponseEntity<List<BookingDto>> getOwnerBookings(@RequestHeader("X-Sharer-User-Id") Long ownerId,
                                                             @RequestParam(defaultValue = "ALL") String state) {
        List<BookingDto> bookings = bookingService.getOwnerBookings(ownerId, state);
        return ResponseEntity.ok(bookings);
    }
}

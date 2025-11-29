package ru.practicum.shareit.service;

import ru.practicum.shareit.dto.BookingDto;
import java.util.List;

public interface BookingService {
    BookingDto createBooking(BookingDto bookingDto, Long userId);
    BookingDto updateBookingStatus(Long bookingId, Boolean approved, Long userId);
    BookingDto getBookingById(Long bookingId, Long userId);
    List<BookingDto> getUserBookings(Long userId, String state);
    List<BookingDto> getOwnerBookings(Long ownerId, String state);
}

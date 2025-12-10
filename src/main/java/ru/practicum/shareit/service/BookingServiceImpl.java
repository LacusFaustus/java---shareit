package ru.practicum.shareit.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.dto.BookingRequestDto;
import ru.practicum.shareit.dto.BookingResponseDto;
import ru.practicum.shareit.mapper.BookingMapper;
import ru.practicum.shareit.model.Booking;
import ru.practicum.shareit.model.BookingState;
import ru.practicum.shareit.model.BookingStatus;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.model.Item;
import ru.practicum.shareit.repository.ItemRepository;
import ru.practicum.shareit.model.User;
import ru.practicum.shareit.repository.BookingRepository;
import ru.practicum.shareit.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final BookingMapper bookingMapper;

    @Override
    @Transactional
    public BookingResponseDto createBooking(BookingRequestDto bookingRequestDto, Long userId) {
        User booker = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        Item item = itemRepository.findById(bookingRequestDto.getItemId())
                .orElseThrow(() -> new NotFoundException("Item not found"));

        if (!item.getAvailable()) {
            throw new ValidationException("Item is not available for booking");
        }

        if (item.getOwner().getId().equals(userId)) {
            throw new NotFoundException("Owner cannot book their own item");
        }

        validateBookingDates(bookingRequestDto);

        Booking booking = bookingMapper.toEntity(bookingRequestDto);
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStatus(BookingStatus.WAITING);

        Booking savedBooking = bookingRepository.save(booking);
        return bookingMapper.toResponseDto(savedBooking);
    }

    @Override
    @Transactional
    public BookingResponseDto updateBookingStatus(Long bookingId, Boolean approved, Long userId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Booking not found"));

        if (!booking.getItem().getOwner().getId().equals(userId)) {
            throw new NotFoundException("Only owner can update booking status");
        }

        if (booking.getStatus() != BookingStatus.WAITING) {
            throw new ValidationException("Booking status cannot be changed");
        }

        booking.setStatus(approved ? BookingStatus.APPROVED : BookingStatus.REJECTED);
        Booking updatedBooking = bookingRepository.save(booking);
        return bookingMapper.toResponseDto(updatedBooking);
    }

    @Override
    public BookingResponseDto getBookingById(Long bookingId, Long userId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Booking not found"));

        boolean isBooker = booking.getBooker().getId().equals(userId);
        boolean isOwner = booking.getItem().getOwner().getId().equals(userId);

        if (!isBooker && !isOwner) {
            throw new NotFoundException("Access denied");
        }

        return bookingMapper.toResponseDto(booking);
    }

    @Override
    public List<BookingResponseDto> getUserBookings(Long userId, String state, int from, int size) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        validatePaginationParams(from, size);

        Pageable pageable = PageRequest.of(from / size, size, Sort.by(Sort.Direction.DESC, "start"));
        LocalDateTime now = LocalDateTime.now();

        BookingState bookingState = getBookingState(state);

        List<Booking> bookings;
        switch (bookingState) {
            case ALL:
                bookings = bookingRepository.findByBookerIdOrderByStartDesc(userId, pageable);
                break;
            case CURRENT:
                bookings = bookingRepository.findByBookerIdAndStartBeforeAndEndAfterOrderByStartDesc(
                        userId, now, now, pageable);
                break;
            case PAST:
                bookings = bookingRepository.findByBookerIdAndEndBeforeOrderByStartDesc(userId, now, pageable);
                break;
            case FUTURE:
                bookings = bookingRepository.findByBookerIdAndStartAfterOrderByStartDesc(userId, now, pageable);
                break;
            case WAITING:
                bookings = bookingRepository.findByBookerIdAndStatusOrderByStartDesc(
                        userId, BookingStatus.WAITING, pageable);
                break;
            case REJECTED:
                bookings = bookingRepository.findByBookerIdAndStatusOrderByStartDesc(
                        userId, BookingStatus.REJECTED, pageable);
                break;
            case CANCELED:
                bookings = bookingRepository.findByBookerIdAndStatusOrderByStartDesc(
                        userId, BookingStatus.CANCELED, pageable);
                break;
            default:
                throw new ValidationException("Unknown state: " + state);
        }

        return bookings.stream()
                .map(bookingMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<BookingResponseDto> getOwnerBookings(Long ownerId, String state, int from, int size) {
        userRepository.findById(ownerId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        validatePaginationParams(from, size);

        Pageable pageable = PageRequest.of(from / size, size, Sort.by(Sort.Direction.DESC, "start"));
        LocalDateTime now = LocalDateTime.now();

        BookingState bookingState = getBookingState(state);

        List<Booking> bookings;
        switch (bookingState) {
            case ALL:
                bookings = bookingRepository.findByItemOwnerIdOrderByStartDesc(ownerId, pageable);
                break;
            case CURRENT:
                bookings = bookingRepository.findByItemOwnerIdAndStartBeforeAndEndAfterOrderByStartDesc(
                        ownerId, now, now, pageable);
                break;
            case PAST:
                bookings = bookingRepository.findByItemOwnerIdAndEndBeforeOrderByStartDesc(ownerId, now, pageable);
                break;
            case FUTURE:
                bookings = bookingRepository.findByItemOwnerIdAndStartAfterOrderByStartDesc(ownerId, now, pageable);
                break;
            case WAITING:
                bookings = bookingRepository.findByItemOwnerIdAndStatusOrderByStartDesc(
                        ownerId, BookingStatus.WAITING, pageable);
                break;
            case REJECTED:
                bookings = bookingRepository.findByItemOwnerIdAndStatusOrderByStartDesc(
                        ownerId, BookingStatus.REJECTED, pageable);
                break;
            case CANCELED:
                bookings = bookingRepository.findByItemOwnerIdAndStatusOrderByStartDesc(
                        ownerId, BookingStatus.CANCELED, pageable);
                break;
            default:
                throw new ValidationException("Unknown state: " + state);
        }

        return bookings.stream()
                .map(bookingMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    private void validateBookingDates(BookingRequestDto bookingRequestDto) {
        if (bookingRequestDto.getStart() == null || bookingRequestDto.getEnd() == null) {
            throw new ValidationException("Start and end dates are required");
        }

        LocalDateTime now = LocalDateTime.now();

        if (bookingRequestDto.getStart().isBefore(now)) {
            throw new ValidationException("Start date cannot be in the past");
        }

        if (bookingRequestDto.getEnd().isBefore(now)) {
            throw new ValidationException("End date must be in the future");
        }

        if (!bookingRequestDto.getEnd().isAfter(bookingRequestDto.getStart())) {
            throw new ValidationException("End date must be after start date");
        }

        if (bookingRequestDto.getStart().isEqual(bookingRequestDto.getEnd())) {
            throw new ValidationException("Start and end dates cannot be the same");
        }
    }

    private BookingState getBookingState(String state) {
        try {
            return BookingState.valueOf(state.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Unknown state: " + state);
        }
    }

    private void validatePaginationParams(int from, int size) {
        if (from < 0) {
            throw new ValidationException("Parameter 'from' must be >= 0");
        }
        if (size <= 0) {
            throw new ValidationException("Parameter 'size' must be > 0");
        }
    }
}

package ru.practicum.shareit.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
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
        log.info("Creating booking for user ID: {}, item ID: {}", userId, bookingRequestDto.getItemId());

        User booker = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("User not found with ID: {}", userId);
                    return new NotFoundException("User not found");
                });

        Item item = itemRepository.findById(bookingRequestDto.getItemId())
                .orElseThrow(() -> {
                    log.warn("Item not found with ID: {}", bookingRequestDto.getItemId());
                    return new NotFoundException("Item not found");
                });

        if (!item.getAvailable()) {
            log.warn("Item ID: {} is not available for booking", item.getId());
            throw new ValidationException("Item is not available for booking");
        }

        if (item.getOwner().getId().equals(userId)) {
            log.warn("Owner ID: {} cannot book their own item ID: {}", userId, item.getId());
            throw new NotFoundException("Owner cannot book their own item");
        }

        validateBookingDates(bookingRequestDto);

        Booking booking = bookingMapper.toEntity(bookingRequestDto);
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStatus(BookingStatus.WAITING);

        Booking savedBooking = bookingRepository.save(booking);
        log.info("Booking created successfully with ID: {} for user ID: {}", savedBooking.getId(), userId);

        return bookingMapper.toResponseDto(savedBooking);
    }

    @Override
    @Transactional
    public BookingResponseDto updateBookingStatus(Long bookingId, Boolean approved, Long userId) {
        log.info("Updating booking status. Booking ID: {}, approved: {}, user ID: {}", bookingId, approved, userId);

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> {
                    log.warn("Booking not found with ID: {}", bookingId);
                    return new NotFoundException("Booking not found");
                });

        if (!booking.getItem().getOwner().getId().equals(userId)) {
            log.warn("User ID: {} is not owner of item for booking ID: {}", userId, bookingId);
            throw new NotFoundException("Only owner can update booking status");
        }

        if (booking.getStatus() != BookingStatus.WAITING) {
            log.warn("Booking ID: {} status cannot be changed. Current status: {}", bookingId, booking.getStatus());
            throw new ValidationException("Booking status cannot be changed");
        }

        booking.setStatus(approved ? BookingStatus.APPROVED : BookingStatus.REJECTED);
        Booking updatedBooking = bookingRepository.save(booking);

        log.info("Booking ID: {} status updated to: {}", bookingId, updatedBooking.getStatus());
        return bookingMapper.toResponseDto(updatedBooking);
    }

    @Override
    public BookingResponseDto getBookingById(Long bookingId, Long userId) {
        log.info("Getting booking by ID: {} for user ID: {}", bookingId, userId);

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> {
                    log.warn("Booking not found with ID: {}", bookingId);
                    return new NotFoundException("Booking not found");
                });

        boolean isBooker = booking.getBooker().getId().equals(userId);
        boolean isOwner = booking.getItem().getOwner().getId().equals(userId);

        if (!isBooker && !isOwner) {
            log.warn("Access denied for user ID: {} to booking ID: {}", userId, bookingId);
            throw new NotFoundException("Access denied");
        }

        log.info("Booking ID: {} retrieved successfully for user ID: {}", bookingId, userId);
        return bookingMapper.toResponseDto(booking);
    }

    @Override
    public List<BookingResponseDto> getUserBookings(Long userId, String state, int from, int size) {
        log.info("Getting user bookings. User ID: {}, state: {}, from: {}, size: {}", userId, state, from, size);

        userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("User not found with ID: {}", userId);
                    return new NotFoundException("User not found");
                });

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
                log.warn("Unknown booking state: {}", state);
                throw new ValidationException("Unknown state: " + state);
        }

        List<BookingResponseDto> result = bookings.stream()
                .map(bookingMapper::toResponseDto)
                .collect(Collectors.toList());

        log.info("Found {} bookings for user ID: {} with state: {}", result.size(), userId, state);
        return result;
    }

    @Override
    public List<BookingResponseDto> getOwnerBookings(Long ownerId, String state, int from, int size) {
        log.info("Getting owner bookings. Owner ID: {}, state: {}, from: {}, size: {}", ownerId, state, from, size);

        userRepository.findById(ownerId)
                .orElseThrow(() -> {
                    log.warn("User not found with ID: {}", ownerId);
                    return new NotFoundException("User not found");
                });

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
                log.warn("Unknown booking state: {}", state);
                throw new ValidationException("Unknown state: " + state);
        }

        List<BookingResponseDto> result = bookings.stream()
                .map(bookingMapper::toResponseDto)
                .collect(Collectors.toList());

        log.info("Found {} bookings for owner ID: {} with state: {}", result.size(), ownerId, state);
        return result;
    }

    private void validateBookingDates(BookingRequestDto bookingRequestDto) {
        if (bookingRequestDto.getStart() == null || bookingRequestDto.getEnd() == null) {
            log.warn("Start and end dates are required");
            throw new ValidationException("Start and end dates are required");
        }

        LocalDateTime now = LocalDateTime.now();

        if (bookingRequestDto.getStart().isBefore(now)) {
            log.warn("Start date cannot be in the past. Start: {}", bookingRequestDto.getStart());
            throw new ValidationException("Start date cannot be in the past");
        }

        if (bookingRequestDto.getEnd().isBefore(now)) {
            log.warn("End date must be in the future. End: {}", bookingRequestDto.getEnd());
            throw new ValidationException("End date must be in the future");
        }

        if (!bookingRequestDto.getEnd().isAfter(bookingRequestDto.getStart())) {
            log.warn("End date must be after start date. Start: {}, End: {}",
                    bookingRequestDto.getStart(), bookingRequestDto.getEnd());
            throw new ValidationException("End date must be after start date");
        }

        if (bookingRequestDto.getStart().isEqual(bookingRequestDto.getEnd())) {
            log.warn("Start and end dates cannot be the same. Date: {}", bookingRequestDto.getStart());
            throw new ValidationException("Start and end dates cannot be the same");
        }
    }

    private BookingState getBookingState(String state) {
        try {
            return BookingState.valueOf(state.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("Invalid booking state: {}", state);
            throw new ValidationException("Unknown state: " + state);
        }
    }

    private void validatePaginationParams(int from, int size) {
        if (from < 0) {
            log.warn("Invalid pagination parameter 'from': {}", from);
            throw new ValidationException("Parameter 'from' must be >= 0");
        }
        if (size <= 0) {
            log.warn("Invalid pagination parameter 'size': {}", size);
            throw new ValidationException("Parameter 'size' must be > 0");
        }
    }
}

package ru.practicum.shareit.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.dto.BookingRequestDto;
import ru.practicum.shareit.dto.BookingResponseDto;
import ru.practicum.shareit.model.Booking;

@Component
@RequiredArgsConstructor
public class BookingMapper {
    private final ItemMapper itemMapper;
    private final UserMapper userMapper;

    public Booking toEntity(BookingRequestDto bookingRequestDto) {
        if (bookingRequestDto == null) {
            return null;
        }

        Booking booking = new Booking();
        booking.setStart(bookingRequestDto.getStart());
        booking.setEnd(bookingRequestDto.getEnd());
        // item, booker, status устанавливаются в сервисе
        return booking;
    }

    public BookingResponseDto toResponseDto(Booking booking) {
        if (booking == null) {
            return null;
        }

        return BookingResponseDto.builder()
                .id(booking.getId())
                .start(booking.getStart())
                .end(booking.getEnd())
                .item(itemMapper.toDto(booking.getItem()))
                .booker(userMapper.toDto(booking.getBooker()))
                .status(booking.getStatus())
                .build();
    }
}

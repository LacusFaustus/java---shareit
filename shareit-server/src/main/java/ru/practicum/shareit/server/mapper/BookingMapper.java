package ru.practicum.shareit.server.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.shareit.server.config.MapStructConfig;
import ru.practicum.shareit.dto.BookingRequestDto;
import ru.practicum.shareit.dto.BookingResponseDto;
import ru.practicum.shareit.server.model.Booking;

@Mapper(config = MapStructConfig.class)
public interface BookingMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "item", ignore = true)
    @Mapping(target = "booker", ignore = true)
    @Mapping(target = "status", ignore = true)
    Booking toEntity(BookingRequestDto bookingRequestDto);

    BookingResponseDto toResponseDto(Booking booking);
}

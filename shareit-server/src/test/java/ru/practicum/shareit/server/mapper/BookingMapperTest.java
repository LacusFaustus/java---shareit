package ru.practicum.shareit.server.mapper;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import ru.practicum.shareit.dto.BookingRequestDto;
import ru.practicum.shareit.server.model.Booking;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class BookingMapperTest {
    private final BookingMapper mapper = Mappers.getMapper(BookingMapper.class);

    @Test
    void toEntity() {
        BookingRequestDto dto = new BookingRequestDto(
                1L,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2)
        );

        Booking entity = mapper.toEntity(dto);

        assertThat(entity).isNotNull();
        assertThat(entity.getStart()).isEqualTo(dto.getStart());
        assertThat(entity.getEnd()).isEqualTo(dto.getEnd());
    }
}

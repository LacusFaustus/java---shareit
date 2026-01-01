package ru.practicum.shareit.gateway.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import ru.practicum.shareit.dto.BookingRequestDto;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class BookingRequestDtoJsonTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void serializeBookingRequestDto() throws Exception {
        BookingRequestDto dto = new BookingRequestDto(
                1L,
                LocalDateTime.of(2024, 1, 20, 10, 0),
                LocalDateTime.of(2024, 1, 22, 10, 0)
        );

        String json = objectMapper.writeValueAsString(dto);

        assertThat(json).contains("\"itemId\":1");
        assertThat(json).contains("\"start\":\"2024-01-20T10:00:00\"");
        assertThat(json).contains("\"end\":\"2024-01-22T10:00:00\"");
    }

    @Test
    void deserializeBookingRequestDto() throws Exception {
        String json = """
            {
                "itemId": 1,
                "start": "2024-01-20T10:00:00",
                "end": "2024-01-22T10:00:00"
            }
            """;

        BookingRequestDto dto = objectMapper.readValue(json, BookingRequestDto.class);

        assertThat(dto.getItemId()).isEqualTo(1L);
        assertThat(dto.getStart()).isEqualTo(LocalDateTime.of(2024, 1, 20, 10, 0));
        assertThat(dto.getEnd()).isEqualTo(LocalDateTime.of(2024, 1, 22, 10, 0));
    }

    @Test
    void builderCreatesValidDto() {
        BookingRequestDto dto = new BookingRequestDto(
                1L,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2)
        );

        assertThat(dto.getItemId()).isEqualTo(1L);
        assertThat(dto.getStart()).isAfter(LocalDateTime.now());
        assertThat(dto.getEnd()).isAfter(dto.getStart());
    }
}

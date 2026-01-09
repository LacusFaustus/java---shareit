package ru.practicum.shareit.dto;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class BookingResponseDtoTest {

    @Test
    void testNoArgsConstructor() {
        BookingResponseDto dto = new BookingResponseDto();
        assertNotNull(dto);
        assertNull(dto.getId());
        assertNull(dto.getStart());
        assertNull(dto.getEnd());
        assertNull(dto.getItem());
        assertNull(dto.getBooker());
        assertNull(dto.getStatus());
    }

    @Test
    void testAllArgsConstructor() {
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = start.plusDays(1);
        ItemDto item = ItemDto.builder().id(1L).build();
        UserDto booker = UserDto.builder().id(2L).build();

        BookingResponseDto dto = new BookingResponseDto(
                1L, start, end, item, booker, "APPROVED"
        );

        assertEquals(1L, dto.getId());
        assertEquals(start, dto.getStart());
        assertEquals(end, dto.getEnd());
        assertEquals(item, dto.getItem());
        assertEquals(booker, dto.getBooker());
        assertEquals("APPROVED", dto.getStatus());
    }

    @Test
    void testBuilder() {
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = start.plusDays(1);
        ItemDto item = ItemDto.builder().id(1L).build();
        UserDto booker = UserDto.builder().id(2L).build();

        BookingResponseDto dto = BookingResponseDto.builder()
                .id(1L)
                .start(start)
                .end(end)
                .item(item)
                .booker(booker)
                .status("APPROVED")
                .build();

        assertEquals(1L, dto.getId());
        assertEquals(start, dto.getStart());
        assertEquals(end, dto.getEnd());
        assertEquals(item, dto.getItem());
        assertEquals(booker, dto.getBooker());
        assertEquals("APPROVED", dto.getStatus());
    }

    @Test
    void testSettersAndGetters() {
        BookingResponseDto dto = new BookingResponseDto();
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = start.plusDays(1);
        ItemDto item = ItemDto.builder().id(1L).build();
        UserDto booker = UserDto.builder().id(2L).build();

        dto.setId(1L);
        dto.setStart(start);
        dto.setEnd(end);
        dto.setItem(item);
        dto.setBooker(booker);
        dto.setStatus("APPROVED");

        assertEquals(1L, dto.getId());
        assertEquals(start, dto.getStart());
        assertEquals(end, dto.getEnd());
        assertEquals(item, dto.getItem());
        assertEquals(booker, dto.getBooker());
        assertEquals("APPROVED", dto.getStatus());
    }

    @Test
    void testEqualsAndHashCode() {
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = start.plusDays(1);
        ItemDto item = ItemDto.builder().id(1L).build();
        UserDto booker = UserDto.builder().id(2L).build();

        BookingResponseDto dto1 = new BookingResponseDto(1L, start, end, item, booker, "APPROVED");
        BookingResponseDto dto2 = new BookingResponseDto(1L, start, end, item, booker, "APPROVED");
        BookingResponseDto dto3 = new BookingResponseDto(2L, start, end, item, booker, "APPROVED");

        assertEquals(dto1, dto2);
        assertNotEquals(dto1, dto3);
        assertEquals(dto1.hashCode(), dto2.hashCode());
        assertNotEquals(dto1.hashCode(), dto3.hashCode());
    }

    @Test
    void testNullSafety() {
        // Проверяем, что DTO можно создать без NPE
        BookingInfoDto dto = new BookingInfoDto();

        // Проверяем сеттеры с null
        dto.setId(null);
        dto.setBookerId(null);
        dto.setStart(null);
        dto.setEnd(null);

        assertNull(dto.getId());
        assertNull(dto.getBookerId());
        assertNull(dto.getStart());
        assertNull(dto.getEnd());
    }

    @Test
    void testEquals_WithNull() {
        ItemDto dto = new ItemDto(1L, "Test", "Desc", true, 10L);
        assertNotEquals(null, dto);
        assertFalse(dto.equals(null));
    }

    @Test
    void testEquals_WithDifferentClass() {
        ItemDto dto = new ItemDto(1L, "Test", "Desc", true, 10L);
        Object obj = new Object();
        assertNotEquals(dto, obj);
        assertFalse(dto.equals(obj));
    }

    @Test
    void testEquals_SameObject() {
        ItemDto dto = new ItemDto(1L, "Test", "Desc", true, 10L);
        assertEquals(dto, dto);
        assertTrue(dto.equals(dto));
    }

    @Test
    void testEquals_WithNullFields() {
        ItemDto dto1 = new ItemDto();
        ItemDto dto2 = new ItemDto();
        assertEquals(dto1, dto2);
    }
}

package ru.practicum.shareit.dto;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class BookingInfoDtoTest {

    @Test
    void testNoArgsConstructor() {
        BookingInfoDto dto = new BookingInfoDto();
        assertNotNull(dto);
        assertNull(dto.getId());
        assertNull(dto.getBookerId());
        assertNull(dto.getStart());
        assertNull(dto.getEnd());
    }

    @Test
    void testAllArgsConstructor() {
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = start.plusDays(1);
        BookingInfoDto dto = new BookingInfoDto(1L, 2L, start, end);

        assertEquals(1L, dto.getId());
        assertEquals(2L, dto.getBookerId());
        assertEquals(start, dto.getStart());
        assertEquals(end, dto.getEnd());
    }

    @Test
    void testBuilder() {
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = start.plusDays(1);
        BookingInfoDto dto = BookingInfoDto.builder()
                .id(1L)
                .bookerId(2L)
                .start(start)
                .end(end)
                .build();

        assertEquals(1L, dto.getId());
        assertEquals(2L, dto.getBookerId());
        assertEquals(start, dto.getStart());
        assertEquals(end, dto.getEnd());
    }

    @Test
    void testSettersAndGetters() {
        BookingInfoDto dto = new BookingInfoDto();
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = start.plusDays(1);

        dto.setId(1L);
        dto.setBookerId(2L);
        dto.setStart(start);
        dto.setEnd(end);

        assertEquals(1L, dto.getId());
        assertEquals(2L, dto.getBookerId());
        assertEquals(start, dto.getStart());
        assertEquals(end, dto.getEnd());
    }

    @Test
    void testEqualsAndHashCode() {
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = start.plusDays(1);
        BookingInfoDto dto1 = new BookingInfoDto(1L, 2L, start, end);
        BookingInfoDto dto2 = new BookingInfoDto(1L, 2L, start, end);
        BookingInfoDto dto3 = new BookingInfoDto(3L, 4L, start, end);

        assertEquals(dto1, dto2);
        assertNotEquals(dto1, dto3);
        assertEquals(dto1.hashCode(), dto2.hashCode());
        assertNotEquals(dto1.hashCode(), dto3.hashCode());
    }

    @Test
    void testToString() {
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = start.plusDays(1);
        BookingInfoDto dto = new BookingInfoDto(1L, 2L, start, end);
        String str = dto.toString();

        assertTrue(str.contains("id=1"));
        assertTrue(str.contains("bookerId=2"));
        assertTrue(str.contains("start="));
        assertTrue(str.contains("end="));
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

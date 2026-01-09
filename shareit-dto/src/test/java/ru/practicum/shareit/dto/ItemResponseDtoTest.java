package ru.practicum.shareit.dto;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ItemResponseDtoTest {

    @Test
    void testNoArgsConstructor() {
        ItemResponseDto dto = new ItemResponseDto();
        assertNotNull(dto);
        assertNull(dto.getId());
        assertNull(dto.getName());
        assertNull(dto.getDescription());
        assertNull(dto.getAvailable());
        assertNull(dto.getRequestId());
        assertNull(dto.getLastBooking());
        assertNull(dto.getNextBooking());
        assertNull(dto.getComments());
    }

    @Test
    void testAllArgsConstructor() {
        LocalDateTime now = LocalDateTime.now();
        BookingInfoDto lastBooking = new BookingInfoDto(1L, 10L, now.minusDays(2), now.minusDays(1));
        BookingInfoDto nextBooking = new BookingInfoDto(2L, 11L, now.plusDays(1), now.plusDays(2));
        CommentDto comment1 = new CommentDto(1L, "Great!", "User1", now);
        CommentDto comment2 = new CommentDto(2L, "Awesome!", "User2", now);
        List<CommentDto> comments = Arrays.asList(comment1, comment2);

        ItemResponseDto dto = new ItemResponseDto(
                1L, "Hammer", "Heavy hammer", true, 100L,
                lastBooking, nextBooking, comments
        );

        assertEquals(1L, dto.getId());
        assertEquals("Hammer", dto.getName());
        assertEquals("Heavy hammer", dto.getDescription());
        assertTrue(dto.getAvailable());
        assertEquals(100L, dto.getRequestId());
        assertEquals(lastBooking, dto.getLastBooking());
        assertEquals(nextBooking, dto.getNextBooking());
        assertEquals(comments, dto.getComments());
        assertEquals(2, dto.getComments().size());
    }

    @Test
    void testBuilder() {
        LocalDateTime now = LocalDateTime.now();
        BookingInfoDto lastBooking = new BookingInfoDto(1L, 10L, now.minusDays(2), now.minusDays(1));
        BookingInfoDto nextBooking = new BookingInfoDto(2L, 11L, now.plusDays(1), now.plusDays(2));
        CommentDto comment = new CommentDto(1L, "Great!", "User1", now);
        List<CommentDto> comments = Collections.singletonList(comment);

        ItemResponseDto dto = ItemResponseDto.builder()
                .id(1L)
                .name("Hammer")
                .description("Heavy hammer")
                .available(true)
                .requestId(100L)
                .lastBooking(lastBooking)
                .nextBooking(nextBooking)
                .comments(comments)
                .build();

        assertEquals(1L, dto.getId());
        assertEquals("Hammer", dto.getName());
        assertEquals("Heavy hammer", dto.getDescription());
        assertTrue(dto.getAvailable());
        assertEquals(100L, dto.getRequestId());
        assertEquals(lastBooking, dto.getLastBooking());
        assertEquals(nextBooking, dto.getNextBooking());
        assertEquals(comments, dto.getComments());
    }

    @Test
    void testSettersAndGetters() {
        ItemResponseDto dto = new ItemResponseDto();
        LocalDateTime now = LocalDateTime.now();
        BookingInfoDto lastBooking = new BookingInfoDto(1L, 10L, now.minusDays(2), now.minusDays(1));
        BookingInfoDto nextBooking = new BookingInfoDto(2L, 11L, now.plusDays(1), now.plusDays(2));
        CommentDto comment = new CommentDto(1L, "Great!", "User1", now);
        List<CommentDto> comments = Collections.singletonList(comment);

        dto.setId(1L);
        dto.setName("Hammer");
        dto.setDescription("Heavy hammer");
        dto.setAvailable(true);
        dto.setRequestId(100L);
        dto.setLastBooking(lastBooking);
        dto.setNextBooking(nextBooking);
        dto.setComments(comments);

        assertEquals(1L, dto.getId());
        assertEquals("Hammer", dto.getName());
        assertEquals("Heavy hammer", dto.getDescription());
        assertTrue(dto.getAvailable());
        assertEquals(100L, dto.getRequestId());
        assertEquals(lastBooking, dto.getLastBooking());
        assertEquals(nextBooking, dto.getNextBooking());
        assertEquals(comments, dto.getComments());
    }

    @Test
    void testEqualsAndHashCode() {
        LocalDateTime now = LocalDateTime.now();
        BookingInfoDto lastBooking = new BookingInfoDto(1L, 10L, now.minusDays(2), now.minusDays(1));
        BookingInfoDto nextBooking = new BookingInfoDto(2L, 11L, now.plusDays(1), now.plusDays(2));
        CommentDto comment = new CommentDto(1L, "Great!", "User1", now);
        List<CommentDto> comments = Collections.singletonList(comment);

        ItemResponseDto dto1 = new ItemResponseDto(1L, "Hammer", "Desc", true, 100L,
                lastBooking, nextBooking, comments);
        ItemResponseDto dto2 = new ItemResponseDto(1L, "Hammer", "Desc", true, 100L,
                lastBooking, nextBooking, comments);
        ItemResponseDto dto3 = new ItemResponseDto(2L, "Drill", "Desc2", false, 200L,
                lastBooking, nextBooking, comments);

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

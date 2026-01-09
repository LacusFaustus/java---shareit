package ru.practicum.shareit.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class JsonSerializationTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule());
    }

    @Test
    void testBookingRequestDtoSerialization() throws Exception {
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = start.plusDays(2);

        BookingRequestDto dto = BookingRequestDto.builder()
                .itemId(1L)
                .start(start)
                .end(end)
                .build();

        String json = objectMapper.writeValueAsString(dto);

        BookingRequestDto deserialized = objectMapper.readValue(json, BookingRequestDto.class);

        assertEquals(dto.getItemId(), deserialized.getItemId());
        assertEquals(dto.getStart(), deserialized.getStart());
        assertEquals(dto.getEnd(), deserialized.getEnd());
        assertTrue(deserialized.isValid());
    }

    @Test
    void testItemDtoSerialization() throws Exception {
        ItemDto dto = ItemDto.builder()
                .id(1L)
                .name("Test Item")
                .description("Test Description")
                .available(true)
                .requestId(10L)
                .build();

        String json = objectMapper.writeValueAsString(dto);
        ItemDto deserialized = objectMapper.readValue(json, ItemDto.class);

        assertEquals(dto.getId(), deserialized.getId());
        assertEquals(dto.getName(), deserialized.getName());
        assertEquals(dto.getDescription(), deserialized.getDescription());
        assertEquals(dto.getAvailable(), deserialized.getAvailable());
        assertEquals(dto.getRequestId(), deserialized.getRequestId());
    }

    @Test
    void testUserDtoSerialization() throws Exception {
        UserDto dto = UserDto.builder()
                .id(1L)
                .name("Test User")
                .email("test@example.com")
                .build();

        String json = objectMapper.writeValueAsString(dto);
        UserDto deserialized = objectMapper.readValue(json, UserDto.class);

        assertEquals(dto.getId(), deserialized.getId());
        assertEquals(dto.getName(), deserialized.getName());
        assertEquals(dto.getEmail(), deserialized.getEmail());
    }

    @Test
    void testCommentDtoSerialization() throws Exception {
        LocalDateTime created = LocalDateTime.now();

        CommentDto dto = CommentDto.builder()
                .id(1L)
                .text("Test comment")
                .authorName("Test Author")
                .created(created)
                .build();

        String json = objectMapper.writeValueAsString(dto);
        CommentDto deserialized = objectMapper.readValue(json, CommentDto.class);

        assertEquals(dto.getId(), deserialized.getId());
        assertEquals(dto.getText(), deserialized.getText());
        assertEquals(dto.getAuthorName(), deserialized.getAuthorName());
        assertEquals(dto.getCreated(), deserialized.getCreated());
    }

    @Test
    void testJsonIgnoreOnValidMethod() throws Exception {
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = start.plusDays(2);

        BookingRequestDto dto = BookingRequestDto.builder()
                .itemId(1L)
                .start(start)
                .end(end)
                .build();

        String json = objectMapper.writeValueAsString(dto);

        assertFalse(json.contains("\"valid\""));
        assertFalse(json.contains("\"isValid\""));
        assertTrue(json.contains("\"itemId\""));
        assertTrue(json.contains("\"start\""));
        assertTrue(json.contains("\"end\""));
    }
}

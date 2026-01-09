package ru.practicum.shareit.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class CommentDtoTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void testNoArgsConstructor() {
        CommentDto dto = new CommentDto();
        assertNotNull(dto);
        assertNull(dto.getId());
        assertNull(dto.getText());
        assertNull(dto.getAuthorName());
        assertNull(dto.getCreated());
    }

    @Test
    void testAllArgsConstructor() {
        LocalDateTime created = LocalDateTime.now();
        CommentDto dto = new CommentDto(1L, "Great item!", "John Doe", created);

        assertEquals(1L, dto.getId());
        assertEquals("Great item!", dto.getText());
        assertEquals("John Doe", dto.getAuthorName());
        assertEquals(created, dto.getCreated());
    }

    @Test
    void testBuilder() {
        LocalDateTime created = LocalDateTime.now();
        CommentDto dto = CommentDto.builder()
                .id(1L)
                .text("Great item!")
                .authorName("John Doe")
                .created(created)
                .build();

        assertEquals(1L, dto.getId());
        assertEquals("Great item!", dto.getText());
        assertEquals("John Doe", dto.getAuthorName());
        assertEquals(created, dto.getCreated());
    }

    @Test
    void testValidation_Valid() {
        CommentDto dto = CommentDto.builder()
                .text("Great item!")
                .build();

        Set<ConstraintViolation<CommentDto>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testValidation_BlankText() {
        CommentDto dto = CommentDto.builder()
                .text(" ")
                .build();

        Set<ConstraintViolation<CommentDto>> violations = validator.validate(dto);
        assertEquals(1, violations.size());
        assertEquals("Comment text cannot be blank", violations.iterator().next().getMessage());
    }

    @Test
    void testValidation_NullText() {
        CommentDto dto = CommentDto.builder()
                .build();

        Set<ConstraintViolation<CommentDto>> violations = validator.validate(dto);
        assertEquals(1, violations.size());
        assertEquals("Comment text cannot be blank", violations.iterator().next().getMessage());
    }

    @Test
    void testSettersAndGetters() {
        CommentDto dto = new CommentDto();
        LocalDateTime created = LocalDateTime.now();

        dto.setId(1L);
        dto.setText("Great item!");
        dto.setAuthorName("John Doe");
        dto.setCreated(created);

        assertEquals(1L, dto.getId());
        assertEquals("Great item!", dto.getText());
        assertEquals("John Doe", dto.getAuthorName());
        assertEquals(created, dto.getCreated());
    }

    @Test
    void testEqualsAndHashCode() {
        LocalDateTime created = LocalDateTime.now();
        CommentDto dto1 = new CommentDto(1L, "Text", "Author", created);
        CommentDto dto2 = new CommentDto(1L, "Text", "Author", created);
        CommentDto dto3 = new CommentDto(2L, "Text", "Author", created);

        assertEquals(dto1, dto2);
        assertNotEquals(dto1, dto3);
        assertEquals(dto1.hashCode(), dto2.hashCode());
        assertNotEquals(dto1.hashCode(), dto3.hashCode());
    }

    @Test
    void testValidation_MaxLength() {
        String longText = "a".repeat(1024); // Максимальная длина из сущности

        CommentDto dto = CommentDto.builder()
                .text(longText)
                .build();

        Set<ConstraintViolation<CommentDto>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testValidation_TooLongText() {
        String tooLongText = "a".repeat(1025); // На 1 символ больше максимума

        CommentDto dto = CommentDto.builder()
                .text(tooLongText)
                .build();

        // Валидация пройдет, так как @NotBlank проверяет только что не пусто
        // Длину проверяет БД на уровне constraints
        Set<ConstraintViolation<CommentDto>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty());
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

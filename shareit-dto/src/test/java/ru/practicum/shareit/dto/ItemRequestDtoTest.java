package ru.practicum.shareit.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ItemRequestDtoTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void testNoArgsConstructor() {
        ItemRequestDto dto = new ItemRequestDto();
        assertNotNull(dto);
        assertNull(dto.getId());
        assertNull(dto.getDescription());
        assertNull(dto.getCreated());
        assertNull(dto.getItems());
        assertNull(dto.getRequestorId());
    }

    @Test
    void testAllArgsConstructor() {
        LocalDateTime created = LocalDateTime.now();
        ItemDto item1 = ItemDto.builder().id(1L).name("Item1").build();
        ItemDto item2 = ItemDto.builder().id(2L).name("Item2").build();
        List<ItemDto> items = Arrays.asList(item1, item2);

        ItemRequestDto dto = new ItemRequestDto(
                1L, "Need a hammer", created, items, 10L
        );

        assertEquals(1L, dto.getId());
        assertEquals("Need a hammer", dto.getDescription());
        assertEquals(created, dto.getCreated());
        assertEquals(items, dto.getItems());
        assertEquals(10L, dto.getRequestorId());
        assertEquals(2, dto.getItems().size());
    }

    @Test
    void testBuilder() {
        LocalDateTime created = LocalDateTime.now();
        ItemDto item = ItemDto.builder().id(1L).name("Hammer").build();
        List<ItemDto> items = Collections.singletonList(item);

        ItemRequestDto dto = ItemRequestDto.builder()
                .id(1L)
                .description("Need a hammer")
                .created(created)
                .items(items)
                .requestorId(10L)
                .build();

        assertEquals(1L, dto.getId());
        assertEquals("Need a hammer", dto.getDescription());
        assertEquals(created, dto.getCreated());
        assertEquals(items, dto.getItems());
        assertEquals(10L, dto.getRequestorId());
    }

    @Test
    void testValidation_Valid() {
        ItemRequestDto dto = ItemRequestDto.builder()
                .description("Need a hammer")
                .build();

        Set<ConstraintViolation<ItemRequestDto>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testValidation_BlankDescription() {
        ItemRequestDto dto = ItemRequestDto.builder()
                .description(" ")
                .build();

        Set<ConstraintViolation<ItemRequestDto>> violations = validator.validate(dto);
        assertEquals(1, violations.size());
        assertEquals("Description cannot be blank", violations.iterator().next().getMessage());
    }

    @Test
    void testValidation_NullDescription() {
        ItemRequestDto dto = ItemRequestDto.builder()
                .build();

        Set<ConstraintViolation<ItemRequestDto>> violations = validator.validate(dto);
        assertEquals(1, violations.size());
        assertEquals("Description cannot be blank", violations.iterator().next().getMessage());
    }

    @Test
    void testSettersAndGetters() {
        ItemRequestDto dto = new ItemRequestDto();
        LocalDateTime created = LocalDateTime.now();
        ItemDto item = ItemDto.builder().id(1L).name("Hammer").build();
        List<ItemDto> items = Collections.singletonList(item);

        dto.setId(1L);
        dto.setDescription("Need a hammer");
        dto.setCreated(created);
        dto.setItems(items);
        dto.setRequestorId(10L);

        assertEquals(1L, dto.getId());
        assertEquals("Need a hammer", dto.getDescription());
        assertEquals(created, dto.getCreated());
        assertEquals(items, dto.getItems());
        assertEquals(10L, dto.getRequestorId());
    }

    @Test
    void testEqualsAndHashCode() {
        LocalDateTime created = LocalDateTime.now();
        ItemDto item = ItemDto.builder().id(1L).name("Hammer").build();
        List<ItemDto> items = Collections.singletonList(item);

        ItemRequestDto dto1 = new ItemRequestDto(1L, "Need hammer", created, items, 10L);
        ItemRequestDto dto2 = new ItemRequestDto(1L, "Need hammer", created, items, 10L);
        ItemRequestDto dto3 = new ItemRequestDto(2L, "Need drill", created, items, 20L);

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

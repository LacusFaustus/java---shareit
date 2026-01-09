package ru.practicum.shareit.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ItemDtoTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void testNoArgsConstructor() {
        ItemDto dto = new ItemDto();
        assertNotNull(dto);
        assertNull(dto.getId());
        assertNull(dto.getName());
        assertNull(dto.getDescription());
        assertNull(dto.getAvailable());
        assertNull(dto.getRequestId());
    }

    @Test
    void testAllArgsConstructor() {
        ItemDto dto = new ItemDto(1L, "Hammer", "Heavy hammer", true, 10L);

        assertEquals(1L, dto.getId());
        assertEquals("Hammer", dto.getName());
        assertEquals("Heavy hammer", dto.getDescription());
        assertTrue(dto.getAvailable());
        assertEquals(10L, dto.getRequestId());
    }

    @Test
    void testBuilder() {
        ItemDto dto = ItemDto.builder()
                .id(1L)
                .name("Hammer")
                .description("Heavy hammer")
                .available(true)
                .requestId(10L)
                .build();

        assertEquals(1L, dto.getId());
        assertEquals("Hammer", dto.getName());
        assertEquals("Heavy hammer", dto.getDescription());
        assertTrue(dto.getAvailable());
        assertEquals(10L, dto.getRequestId());
    }

    @Test
    void testValidation_AllValid() {
        ItemDto dto = ItemDto.builder()
                .name("Hammer")
                .description("Heavy hammer")
                .available(true)
                .build();

        Set<ConstraintViolation<ItemDto>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testValidation_BlankName() {
        ItemDto dto = ItemDto.builder()
                .name(" ")
                .description("Heavy hammer")
                .available(true)
                .build();

        Set<ConstraintViolation<ItemDto>> violations = validator.validate(dto);
        assertEquals(1, violations.size());
        assertEquals("Name cannot be blank", violations.iterator().next().getMessage());
    }

    @Test
    void testValidation_NullName() {
        ItemDto dto = ItemDto.builder()
                .description("Heavy hammer")
                .available(true)
                .build();

        Set<ConstraintViolation<ItemDto>> violations = validator.validate(dto);
        assertEquals(1, violations.size());
        assertEquals("Name cannot be blank", violations.iterator().next().getMessage());
    }

    @Test
    void testValidation_BlankDescription() {
        ItemDto dto = ItemDto.builder()
                .name("Hammer")
                .description(" ")
                .available(true)
                .build();

        Set<ConstraintViolation<ItemDto>> violations = validator.validate(dto);
        assertEquals(1, violations.size());
        assertEquals("Description cannot be blank", violations.iterator().next().getMessage());
    }

    @Test
    void testValidation_NullAvailable() {
        ItemDto dto = ItemDto.builder()
                .name("Hammer")
                .description("Heavy hammer")
                .build();

        Set<ConstraintViolation<ItemDto>> violations = validator.validate(dto);
        assertEquals(1, violations.size());
        assertEquals("Available status must be specified", violations.iterator().next().getMessage());
    }

    @Test
    void testSettersAndGetters() {
        ItemDto dto = new ItemDto();
        dto.setId(1L);
        dto.setName("Hammer");
        dto.setDescription("Heavy hammer");
        dto.setAvailable(true);
        dto.setRequestId(10L);

        assertEquals(1L, dto.getId());
        assertEquals("Hammer", dto.getName());
        assertEquals("Heavy hammer", dto.getDescription());
        assertTrue(dto.getAvailable());
        assertEquals(10L, dto.getRequestId());
    }

    @Test
    void testEqualsAndHashCode() {
        ItemDto dto1 = new ItemDto(1L, "Hammer", "Heavy hammer", true, 10L);
        ItemDto dto2 = new ItemDto(1L, "Hammer", "Heavy hammer", true, 10L);
        ItemDto dto3 = new ItemDto(2L, "Screwdriver", "Small screwdriver", true, 20L);

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

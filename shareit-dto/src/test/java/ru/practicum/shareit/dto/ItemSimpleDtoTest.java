package ru.practicum.shareit.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ItemSimpleDtoTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void testNoArgsConstructor() {
        ItemSimpleDto dto = new ItemSimpleDto();
        assertNotNull(dto);
        assertNull(dto.getId());
        assertNull(dto.getName());
        assertNull(dto.getDescription());
        assertNull(dto.getAvailable());
        assertNull(dto.getRequestId());
    }

    @Test
    void testAllArgsConstructor() {
        ItemSimpleDto dto = new ItemSimpleDto(1L, "Hammer", "Heavy hammer", true, 10L);

        assertEquals(1L, dto.getId());
        assertEquals("Hammer", dto.getName());
        assertEquals("Heavy hammer", dto.getDescription());
        assertTrue(dto.getAvailable());
        assertEquals(10L, dto.getRequestId());
    }

    @Test
    void testBuilder() {
        ItemSimpleDto dto = ItemSimpleDto.builder()
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
        ItemSimpleDto dto = ItemSimpleDto.builder()
                .name("Hammer")
                .description("Heavy hammer")
                .available(true)
                .build();

        Set<ConstraintViolation<ItemSimpleDto>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testValidation_BlankName() {
        ItemSimpleDto dto = ItemSimpleDto.builder()
                .name(" ")
                .description("Heavy hammer")
                .available(true)
                .build();

        Set<ConstraintViolation<ItemSimpleDto>> violations = validator.validate(dto);
        assertEquals(1, violations.size());
        assertEquals("Name cannot be blank", violations.iterator().next().getMessage());
    }

    @Test
    void testValidation_NullName() {
        ItemSimpleDto dto = ItemSimpleDto.builder()
                .description("Heavy hammer")
                .available(true)
                .build();

        Set<ConstraintViolation<ItemSimpleDto>> violations = validator.validate(dto);
        assertEquals(1, violations.size());
        assertEquals("Name cannot be blank", violations.iterator().next().getMessage());
    }

    @Test
    void testValidation_BlankDescription() {
        ItemSimpleDto dto = ItemSimpleDto.builder()
                .name("Hammer")
                .description(" ")
                .available(true)
                .build();

        Set<ConstraintViolation<ItemSimpleDto>> violations = validator.validate(dto);
        assertEquals(1, violations.size());
        assertEquals("Description cannot be blank", violations.iterator().next().getMessage());
    }

    @Test
    void testValidation_NullAvailable() {
        ItemSimpleDto dto = ItemSimpleDto.builder()
                .name("Hammer")
                .description("Heavy hammer")
                .build();

        Set<ConstraintViolation<ItemSimpleDto>> violations = validator.validate(dto);
        assertEquals(1, violations.size());
        assertEquals("Available status must be specified", violations.iterator().next().getMessage());
    }

    @Test
    void testSettersAndGetters() {
        ItemSimpleDto dto = new ItemSimpleDto();
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
        ItemSimpleDto dto1 = new ItemSimpleDto(1L, "Hammer", "Heavy hammer", true, 10L);
        ItemSimpleDto dto2 = new ItemSimpleDto(1L, "Hammer", "Heavy hammer", true, 10L);
        ItemSimpleDto dto3 = new ItemSimpleDto(2L, "Screwdriver", "Small screwdriver", true, 20L);

        assertEquals(dto1, dto2);
        assertNotEquals(dto1, dto3);
        assertEquals(dto1.hashCode(), dto2.hashCode());
        assertNotEquals(dto1.hashCode(), dto3.hashCode());
    }

    @Test
    void testToString() {
        ItemSimpleDto dto = new ItemSimpleDto(1L, "Hammer", "Heavy hammer", true, 10L);
        String str = dto.toString();

        assertTrue(str.contains("id=1"));
        assertTrue(str.contains("name=Hammer"));
        assertTrue(str.contains("description=Heavy hammer"));
        assertTrue(str.contains("available=true"));
        assertTrue(str.contains("requestId=10"));
    }

    @Test
    void testEquals_WithNull() {
        ItemSimpleDto dto = new ItemSimpleDto(1L, "Item", "Desc", true, 10L);
        assertNotEquals(null, dto);
    }

    @Test
    void testEquals_WithDifferentClass() {
        ItemSimpleDto dto = new ItemSimpleDto(1L, "Item", "Desc", true, 10L);
        assertNotEquals("string", dto);
    }
}

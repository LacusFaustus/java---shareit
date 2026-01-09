package ru.practicum.shareit.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class UserDtoTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void testNoArgsConstructor() {
        UserDto dto = new UserDto();
        assertNotNull(dto);
        assertNull(dto.getId());
        assertNull(dto.getName());
        assertNull(dto.getEmail());
    }

    @Test
    void testAllArgsConstructor() {
        UserDto dto = new UserDto(1L, "John Doe", "john@example.com");

        assertEquals(1L, dto.getId());
        assertEquals("John Doe", dto.getName());
        assertEquals("john@example.com", dto.getEmail());
    }

    @Test
    void testBuilder() {
        UserDto dto = UserDto.builder()
                .id(1L)
                .name("John Doe")
                .email("john@example.com")
                .build();

        assertEquals(1L, dto.getId());
        assertEquals("John Doe", dto.getName());
        assertEquals("john@example.com", dto.getEmail());
    }

    @Test
    void testValidation_AllValid() {
        UserDto dto = UserDto.builder()
                .name("John Doe")
                .email("john@example.com")
                .build();

        Set<ConstraintViolation<UserDto>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testValidation_BlankName() {
        UserDto dto = UserDto.builder()
                .name(" ")
                .email("john@example.com")
                .build();

        Set<ConstraintViolation<UserDto>> violations = validator.validate(dto);
        assertEquals(1, violations.size());
        assertEquals("Name cannot be blank", violations.iterator().next().getMessage());
    }

    @Test
    void testValidation_NullName() {
        UserDto dto = UserDto.builder()
                .email("john@example.com")
                .build();

        Set<ConstraintViolation<UserDto>> violations = validator.validate(dto);
        assertEquals(1, violations.size());
        assertEquals("Name cannot be blank", violations.iterator().next().getMessage());
    }

    @Test
    void testValidation_BlankEmail() {
        UserDto dto = UserDto.builder()
                .name("John Doe")
                .email(" ")
                .build();

        Set<ConstraintViolation<UserDto>> violations = validator.validate(dto);
        assertEquals(2, violations.size()); // Blank and invalid email format
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().equals("Email cannot be blank")));
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().equals("Invalid email format")));
    }

    @Test
    void testValidation_InvalidEmail() {
        UserDto dto = UserDto.builder()
                .name("John Doe")
                .email("not-an-email")
                .build();

        Set<ConstraintViolation<UserDto>> violations = validator.validate(dto);
        assertEquals(1, violations.size());
        assertEquals("Invalid email format", violations.iterator().next().getMessage());
    }

    @Test
    void testValidation_NullEmail() {
        UserDto dto = UserDto.builder()
                .name("John Doe")
                .build();

        Set<ConstraintViolation<UserDto>> violations = validator.validate(dto);
        assertEquals(1, violations.size());
        assertEquals("Email cannot be blank", violations.iterator().next().getMessage());
    }

    @Test
    void testSettersAndGetters() {
        UserDto dto = new UserDto();
        dto.setId(1L);
        dto.setName("John Doe");
        dto.setEmail("john@example.com");

        assertEquals(1L, dto.getId());
        assertEquals("John Doe", dto.getName());
        assertEquals("john@example.com", dto.getEmail());
    }

    @Test
    void testEqualsAndHashCode() {
        UserDto dto1 = new UserDto(1L, "John Doe", "john@example.com");
        UserDto dto2 = new UserDto(1L, "John Doe", "john@example.com");
        UserDto dto3 = new UserDto(2L, "Jane Doe", "jane@example.com");

        assertEquals(dto1, dto2);
        assertNotEquals(dto1, dto3);
        assertEquals(dto1.hashCode(), dto2.hashCode());
        assertNotEquals(dto1.hashCode(), dto3.hashCode());
    }

    @Test
    void testToString() {
        UserDto dto = new UserDto(1L, "John Doe", "john@example.com");
        String str = dto.toString();

        assertTrue(str.contains("id=1"));
        assertTrue(str.contains("name=John Doe"));
        assertTrue(str.contains("email=john@example.com"));
    }
}

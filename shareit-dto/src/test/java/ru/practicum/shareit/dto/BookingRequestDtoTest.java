package ru.practicum.shareit.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class BookingRequestDtoTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void testNoArgsConstructor() {
        BookingRequestDto dto = new BookingRequestDto();
        assertNotNull(dto);
        assertNull(dto.getItemId());
        assertNull(dto.getStart());
        assertNull(dto.getEnd());
        assertFalse(dto.isValid());
    }

    @Test
    void testAllArgsConstructor() {
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = start.plusDays(2);
        BookingRequestDto dto = new BookingRequestDto(1L, start, end);

        assertEquals(1L, dto.getItemId());
        assertEquals(start, dto.getStart());
        assertEquals(end, dto.getEnd());
        assertTrue(dto.isValid());
    }

    @Test
    void testBuilder() {
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = start.plusDays(2);
        BookingRequestDto dto = BookingRequestDto.builder()
                .itemId(1L)
                .start(start)
                .end(end)
                .build();

        assertEquals(1L, dto.getItemId());
        assertEquals(start, dto.getStart());
        assertEquals(end, dto.getEnd());
        assertTrue(dto.isValid());
    }

    @Test
    void testIsValid_ValidDates() {
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = start.plusDays(2);
        BookingRequestDto dto = BookingRequestDto.builder()
                .itemId(1L)
                .start(start)
                .end(end)
                .build();

        assertTrue(dto.isValid());
    }

    @Test
    void testIsValid_EndBeforeStart() {
        LocalDateTime start = LocalDateTime.now().plusDays(2);
        LocalDateTime end = start.minusDays(1);
        BookingRequestDto dto = BookingRequestDto.builder()
                .itemId(1L)
                .start(start)
                .end(end)
                .build();

        assertFalse(dto.isValid());
    }

    @Test
    void testIsValid_NullDates() {
        BookingRequestDto dto = BookingRequestDto.builder()
                .itemId(1L)
                .build();

        assertFalse(dto.isValid());
    }

    @Test
    void testValidation_AllValid() {
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = start.plusDays(2);
        BookingRequestDto dto = BookingRequestDto.builder()
                .itemId(1L)
                .start(start)
                .end(end)
                .build();

        Set<ConstraintViolation<BookingRequestDto>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testValidation_NullItemId() {
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = start.plusDays(2);
        BookingRequestDto dto = BookingRequestDto.builder()
                .start(start)
                .end(end)
                .build();

        Set<ConstraintViolation<BookingRequestDto>> violations = validator.validate(dto);
        assertEquals(1, violations.size());
        assertEquals("Item ID cannot be null", violations.iterator().next().getMessage());
    }

    @Test
    void testValidation_StartInPast() {
        LocalDateTime start = LocalDateTime.now().minusDays(1);
        LocalDateTime end = start.plusDays(2);
        BookingRequestDto dto = BookingRequestDto.builder()
                .itemId(1L)
                .start(start)
                .end(end)
                .build();

        Set<ConstraintViolation<BookingRequestDto>> violations = validator.validate(dto);
        assertEquals(1, violations.size());
        assertEquals("Start date must be in present or future", violations.iterator().next().getMessage());
    }

    @Test
    void testValidation_EndNotInFuture() {
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = LocalDateTime.now().minusHours(1);
        BookingRequestDto dto = BookingRequestDto.builder()
                .itemId(1L)
                .start(start)
                .end(end)
                .build();

        Set<ConstraintViolation<BookingRequestDto>> violations = validator.validate(dto);
        assertEquals(1, violations.size());
        assertEquals("End date must be in future", violations.iterator().next().getMessage());
    }

    @Test
    void testValidation_EndEqualsStart() {
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = start;
        BookingRequestDto dto = BookingRequestDto.builder()
                .itemId(1L)
                .start(start)
                .end(end)
                .build();

        Set<ConstraintViolation<BookingRequestDto>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty());
        assertFalse(dto.isValid());
    }

    @Test
    void testSettersAndGetters() {
        BookingRequestDto dto = new BookingRequestDto();
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = start.plusDays(2);

        dto.setItemId(1L);
        dto.setStart(start);
        dto.setEnd(end);

        assertEquals(1L, dto.getItemId());
        assertEquals(start, dto.getStart());
        assertEquals(end, dto.getEnd());
        assertTrue(dto.isValid());
    }

    @Test
    void testToString() {
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = start.plusDays(2);
        BookingRequestDto dto = BookingRequestDto.builder()
                .itemId(1L)
                .start(start)
                .end(end)
                .build();

        String str = dto.toString();
        assertTrue(str.contains("itemId=1"));
        assertTrue(str.contains("start="));
        assertTrue(str.contains("end="));
    }

    @Test
    void testEqualsAndHashCode() {
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = start.plusDays(2);

        BookingRequestDto dto1 = BookingRequestDto.builder()
                .itemId(1L)
                .start(start)
                .end(end)
                .build();

        BookingRequestDto dto2 = BookingRequestDto.builder()
                .itemId(1L)
                .start(start)
                .end(end)
                .build();

        BookingRequestDto dto3 = BookingRequestDto.builder()
                .itemId(2L)
                .start(start)
                .end(end)
                .build();

        assertEquals(dto1, dto2);
        assertNotEquals(dto1, dto3);
        assertEquals(dto1.hashCode(), dto2.hashCode());
        assertNotEquals(dto1.hashCode(), dto3.hashCode());
    }

    @Test
    void testEquals_Null() {
        BookingRequestDto dto = BookingRequestDto.builder()
                .itemId(1L)
                .start(LocalDateTime.now())
                .end(LocalDateTime.now().plusDays(1))
                .build();
        assertNotEquals(null, dto);
    }

    @Test
    void testEquals_DifferentClass() {
        BookingRequestDto dto = BookingRequestDto.builder()
                .itemId(1L)
                .start(LocalDateTime.now())
                .end(LocalDateTime.now().plusDays(1))
                .build();
        assertNotEquals("string", dto);
    }

    @Test
    void testEquals_SameObject() {
        BookingRequestDto dto = BookingRequestDto.builder()
                .itemId(1L)
                .start(LocalDateTime.now())
                .end(LocalDateTime.now().plusDays(1))
                .build();
        assertEquals(dto, dto);
    }

    @Test
    void testIsValid_BothNotNullButEndNotAfterStart() {
        LocalDateTime start = LocalDateTime.now().plusDays(2);
        LocalDateTime end = start.minusDays(1); // end раньше start

        BookingRequestDto dto = BookingRequestDto.builder()
                .itemId(1L)
                .start(start)
                .end(end)
                .build();

        // Оба не null, но end не после start
        assertNotNull(dto.getStart());
        assertNotNull(dto.getEnd());
        assertFalse(dto.getEnd().isAfter(dto.getStart()));
        assertFalse(dto.isValid()); // Это должно покрыть недостающую ветвь
    }

    @Test
    void testIsValid_NullEnd() {
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        BookingRequestDto dto = BookingRequestDto.builder()
                .itemId(1L)
                .start(start)
                .end(null)  // null end - первое условие false
                .build();

        assertFalse(dto.isValid());
    }

    @Test
    void testIsValid_NullStart() {
        LocalDateTime end = LocalDateTime.now().plusDays(2);
        BookingRequestDto dto = BookingRequestDto.builder()
                .itemId(1L)
                .start(null)  // null start - второе условие false
                .end(end)
                .build();

        assertFalse(dto.isValid());
    }

    @Test
    void testIsValid_BothNull() {
        BookingRequestDto dto = BookingRequestDto.builder()
                .itemId(1L)
                .start(null)  // оба null
                .end(null)
                .build();

        assertFalse(dto.isValid());
    }

    @Test
    void testIsValid_EndNotAfterStart() {
        LocalDateTime start = LocalDateTime.now().plusDays(2);
        LocalDateTime end = start.minusDays(1); // end раньше start

        BookingRequestDto dto = BookingRequestDto.builder()
                .itemId(1L)
                .start(start)
                .end(end)
                .build();

        // end != null - true
        // start != null - true
        // end.isAfter(start) - false
        assertFalse(dto.isValid());
    }

    @Test
    void testIsValid_EndEqualsStart() {
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = start; // точно такое же время

        BookingRequestDto dto = BookingRequestDto.builder()
                .itemId(1L)
                .start(start)
                .end(end)
                .build();

        // end != null - true
        // start != null - true
        // end.isAfter(start) - false (равны, не после)
        assertFalse(dto.isValid());
    }

    @Test
    void testIsValid_ValidCase() {
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = start.plusDays(2); // end позже start

        BookingRequestDto dto = BookingRequestDto.builder()
                .itemId(1L)
                .start(start)
                .end(end)
                .build();

        // Все три условия true
        assertTrue(dto.isValid());
    }

    @Test
    void testEquals_NullObject() {
        BookingRequestDto dto = createValidBookingRequest();
        assertNotEquals(null, dto);
        assertFalse(dto.equals(null));
    }

    @Test
    void testEquals_DifferentStart() {
        LocalDateTime start1 = LocalDateTime.now().plusDays(1);
        LocalDateTime start2 = LocalDateTime.now().plusDays(2);
        LocalDateTime end = start1.plusDays(3);

        BookingRequestDto dto1 = BookingRequestDto.builder()
                .itemId(1L)
                .start(start1)
                .end(end)
                .build();

        BookingRequestDto dto2 = BookingRequestDto.builder()
                .itemId(1L)
                .start(start2)  // другой start
                .end(end)
                .build();

        assertNotEquals(dto1, dto2);
    }

    @Test
    void testEquals_DifferentEnd() {
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end1 = start.plusDays(2);
        LocalDateTime end2 = start.plusDays(3);

        BookingRequestDto dto1 = BookingRequestDto.builder()
                .itemId(1L)
                .start(start)
                .end(end1)
                .build();

        BookingRequestDto dto2 = BookingRequestDto.builder()
                .itemId(1L)
                .start(start)
                .end(end2)  // другой end
                .build();

        assertNotEquals(dto1, dto2);
    }

    @Test
    void testEquals_WithNullFields() {
        // Тест для equals когда у одного объекта поле null
        BookingRequestDto dto1 = BookingRequestDto.builder()
                .itemId(1L)
                .start(null)
                .end(LocalDateTime.now())
                .build();

        BookingRequestDto dto2 = BookingRequestDto.builder()
                .itemId(1L)
                .start(null)
                .end(LocalDateTime.now())
                .build();

        assertEquals(dto1, dto2);
    }

    @Test
    void testHashCode_Consistency() {
        BookingRequestDto dto = createValidBookingRequest();
        int hash1 = dto.hashCode();
        int hash2 = dto.hashCode();
        assertEquals(hash1, hash2);
    }

    @Test
    void testHashCode_WithNullFields() {
        BookingRequestDto dto = BookingRequestDto.builder()
                .itemId(null)
                .start(null)
                .end(null)
                .build();

        int hash = dto.hashCode();
        assertNotNull(Integer.valueOf(hash)); // просто проверяем что не падает
    }

    private BookingRequestDto createValidBookingRequest() {
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = start.plusDays(2);
        return BookingRequestDto.builder()
                .itemId(1L)
                .start(start)
                .end(end)
                .build();
    }
}

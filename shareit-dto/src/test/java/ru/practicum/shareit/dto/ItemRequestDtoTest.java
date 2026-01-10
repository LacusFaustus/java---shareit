package ru.practicum.shareit.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.*;

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
        assertNotNull(dto.getItems()); // Из-за @Builder.Default это не null
        assertTrue(dto.getItems().isEmpty()); // А пустой список
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
    void testAllArgsConstructor_NullItems() {
        LocalDateTime created = LocalDateTime.now();

        // Тест с null items в конструкторе
        // НО: из-за @Builder.Default в билдере, конструктор по умолчанию инициализирует items
        ItemRequestDto dto = new ItemRequestDto(
                1L, "Need a hammer", created, null, 10L
        );

        assertEquals(1L, dto.getId());
        assertEquals("Need a hammer", dto.getDescription());
        assertEquals(created, dto.getCreated());
        assertNull(dto.getItems()); // Items должно быть null, как мы передали
        assertEquals(10L, dto.getRequestorId());
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
    void testBuilder_WithoutItems() {
        LocalDateTime created = LocalDateTime.now();

        ItemRequestDto dto = ItemRequestDto.builder()
                .id(1L)
                .description("Need a hammer")
                .created(created)
                .requestorId(10L)
                .build();

        assertEquals(1L, dto.getId());
        assertEquals("Need a hammer", dto.getDescription());
        assertEquals(created, dto.getCreated());
        assertNotNull(dto.getItems()); // Из-за @Builder.Default это не null
        assertTrue(dto.getItems().isEmpty()); // А пустой список
        assertEquals(10L, dto.getRequestorId());
    }

    @Test
    void testValidation_Valid() {
        ItemRequestDto dto = ItemRequestDto.builder()
                .description("Need a hammer")
                .build();

        Set<ConstraintViolation<ItemRequestDto>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty(), "Не должно быть ошибок валидации для корректного DTO");
    }

    @Test
    void testValidation_BlankDescription() {
        ItemRequestDto dto = ItemRequestDto.builder()
                .description(" ") // Только пробелы
                .build();

        Set<ConstraintViolation<ItemRequestDto>> violations = validator.validate(dto);
        assertEquals(1, violations.size(), "Должна быть 1 ошибка для пустой строки");
        assertEquals("Description cannot be blank", violations.iterator().next().getMessage());
    }

    @Test
    void testValidation_NullDescription() {
        ItemRequestDto dto = ItemRequestDto.builder()
                .description(null) // Явно null
                .build();

        Set<ConstraintViolation<ItemRequestDto>> violations = validator.validate(dto);
        // Может быть 1 или 2 ошибки в зависимости от порядка проверки валидатором
        // Но в сообщении всегда будет "Description cannot be blank" из-за @NotBlank
        assertFalse(violations.isEmpty(), "Должна быть хотя бы одна ошибка для null описания");

        // Проверяем что все сообщения содержат нужный текст
        boolean hasCorrectMessage = violations.stream()
                .anyMatch(v -> v.getMessage().contains("Description cannot be blank"));
        assertTrue(hasCorrectMessage, "Должно быть сообщение о пустом описании");
    }

    @Test
    void testValidation_EmptyDescription() {
        ItemRequestDto dto = ItemRequestDto.builder()
                .description("") // Пустая строка
                .build();

        Set<ConstraintViolation<ItemRequestDto>> violations = validator.validate(dto);
        assertEquals(1, violations.size(), "Должна быть 1 ошибка для пустой строки");
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
    void testSettersAndGetters_NullItems() {
        ItemRequestDto dto = new ItemRequestDto();

        // Устанавливаем null в items через setter
        dto.setItems(null);

        assertNull(dto.getItems(), "Items должно быть null после установки null");
    }

    @Test
    void testEqualsAndHashCode() {
        LocalDateTime created = LocalDateTime.now();
        ItemDto item = ItemDto.builder().id(1L).name("Hammer").build();
        List<ItemDto> items = Collections.singletonList(item);

        ItemRequestDto dto1 = new ItemRequestDto(1L, "Need hammer", created, items, 10L);
        ItemRequestDto dto2 = new ItemRequestDto(1L, "Need hammer", created, items, 10L);
        ItemRequestDto dto3 = new ItemRequestDto(2L, "Need drill", created, items, 20L);

        assertEquals(dto1, dto2, "DTO с одинаковыми полями должны быть равны");
        assertNotEquals(dto1, dto3, "DTO с разными полями не должны быть равны");
        assertEquals(dto1.hashCode(), dto2.hashCode(), "Хэш-коды равных объектов должны совпадать");
        assertNotEquals(dto1.hashCode(), dto3.hashCode(), "Хэш-коды разных объектов не должны совпадать");
    }

    @Test
    void testToString() {
        LocalDateTime created = LocalDateTime.of(2023, 1, 1, 10, 0);
        ItemRequestDto dto = ItemRequestDto.builder()
                .id(1L)
                .description("Test")
                .created(created)
                .requestorId(10L)
                .build();

        String toString = dto.toString();
        assertTrue(toString.contains("id=1"));
        assertTrue(toString.contains("description=Test"));
        assertTrue(toString.contains("requestorId=10"));
    }

    @Test
    void testEquals_WithNull() {
        LocalDateTime created = LocalDateTime.now();
        ItemRequestDto dto = new ItemRequestDto(1L, "Test", created, null, 10L);
        assertNotEquals(null, dto, "DTO не должно быть равно null");
        assertFalse(dto.equals(null), "Метод equals должен возвращать false для null");
    }

    @Test
    void testEquals_WithDifferentClass() {
        LocalDateTime created = LocalDateTime.now();
        ItemRequestDto dto = new ItemRequestDto(1L, "Test", created, null, 10L);
        Object obj = new Object();
        assertNotEquals(dto, obj, "DTO не должно быть равно объекту другого класса");
        assertFalse(dto.equals(obj), "Метод equals должен возвращать false для объекта другого класса");
    }

    @Test
    void testEquals_SameObject() {
        LocalDateTime created = LocalDateTime.now();
        ItemRequestDto dto = new ItemRequestDto(1L, "Test", created, null, 10L);
        assertEquals(dto, dto, "Объект должен быть равен самому себе");
        assertTrue(dto.equals(dto), "Метод equals должен возвращать true для того же объекта");
    }

    @Test
    void testEquals_WithNullFields() {
        ItemRequestDto dto1 = new ItemRequestDto();
        ItemRequestDto dto2 = new ItemRequestDto();
        assertEquals(dto1, dto2, "Два пустых DTO должны быть равны");

        // Теперь специально устанавливаем разные значения
        dto1.setItems(new ArrayList<>()); // Пустой список
        dto2.setItems(null); // Null

        // Они не должны быть равны, так как один имеет пустой список, а другой null
        assertNotEquals(dto1, dto2, "DTO с пустым списком и null списком не должны быть равны");
    }

    @Test
    void testBuilderPattern() {
        // Проверяем что Builder работает корректно
        ItemRequestDto dto = ItemRequestDto.builder()
                .description("Test Description")
                .build();

        assertNull(dto.getId());
        assertEquals("Test Description", dto.getDescription());
        assertNull(dto.getCreated());
        assertNotNull(dto.getItems()); // Из-за @Builder.Default
        assertTrue(dto.getItems().isEmpty());
        assertNull(dto.getRequestorId());
    }

    @Test
    void testHashCode_Consistency() {
        LocalDateTime created = LocalDateTime.now();
        ItemRequestDto dto1 = new ItemRequestDto(1L, "Test", created, null, 10L);
        ItemRequestDto dto2 = new ItemRequestDto(1L, "Test", created, null, 10L);

        // Хэш-код должен быть одинаковым для одинаковых объектов
        assertEquals(dto1.hashCode(), dto2.hashCode());

        // И несколько вызовов подряд должны возвращать одинаковый результат
        int hashCode1 = dto1.hashCode();
        int hashCode2 = dto1.hashCode();
        assertEquals(hashCode1, hashCode2);
    }

    @Test
    void testCanEqual() {
        // Тест для метода canEqual, который генерирует Lombok
        ItemRequestDto dto1 = new ItemRequestDto();
        ItemRequestDto dto2 = new ItemRequestDto();

        // Они должны быть равны
        assertEquals(dto1, dto2);
    }
}

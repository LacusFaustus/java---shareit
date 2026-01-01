package ru.practicum.shareit.server.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.dto.ItemDto;
import ru.practicum.shareit.dto.ItemResponseDto;
import ru.practicum.shareit.dto.UserDto;
import ru.practicum.shareit.server.exception.NotFoundException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class ItemServiceImplIntegrationTest {

    @Autowired
    private ItemService itemService;

    @Autowired
    private UserService userService;

    private Long ownerId;
    private Long anotherUserId;

    @BeforeEach
    void setUp() {
        UserDto ownerDto = UserDto.builder()
                .name("Owner")
                .email("owner@example.com")
                .build();
        UserDto savedOwner = userService.createUser(ownerDto);
        ownerId = savedOwner.getId();

        UserDto anotherUserDto = UserDto.builder()
                .name("Another User")
                .email("another@example.com")
                .build();
        UserDto savedAnotherUser = userService.createUser(anotherUserDto);
        anotherUserId = savedAnotherUser.getId();
    }

    @Test
    void getUserItems_WhenUserExists_ReturnsItems() {
        ItemDto item1 = ItemDto.builder()
                .name("Дрель")
                .description("Аккумуляторная дрель")
                .available(true)
                .build();

        ItemDto item2 = ItemDto.builder()
                .name("Молоток")
                .description("Строительный молоток")
                .available(true)
                .build();

        itemService.createItem(item1, ownerId);
        itemService.createItem(item2, ownerId);

        var result = itemService.getAllItemsByOwner(ownerId);

        assertThat(result).hasSize(2);
        assertThat(result)
                .extracting("name")
                .containsExactlyInAnyOrder("Дрель", "Молоток");
    }

    @Test
    void getUserItems_WhenUserDoesNotExist_ThrowsNotFoundException() {
        Long nonExistentUserId = 999L;

        assertThatThrownBy(() -> itemService.getAllItemsByOwner(nonExistentUserId))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void createItem_WithValidData_CreatesSuccessfully() {
        ItemDto itemDto = ItemDto.builder()
                .name("Тестовая вещь")
                .description("Описание тестовой вещи")
                .available(true)
                .build();

        ItemResponseDto result = itemService.createItem(itemDto, ownerId);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        assertThat(result.getName()).isEqualTo("Тестовая вещь");
        assertThat(result.getAvailable()).isTrue();
    }

    @Test
    void searchItems_WithText_ReturnsAvailableItems() {
        ItemDto availableItem = ItemDto.builder()
                .name("Дрель аккумуляторная")
                .description("Мощная дрель")
                .available(true)
                .build();

        ItemDto unavailableItem = ItemDto.builder()
                .name("Перфоратор")
                .description("Мощный перфоратор")
                .available(false)
                .build();

        itemService.createItem(availableItem, ownerId);
        itemService.createItem(unavailableItem, ownerId);

        List<ItemDto> result = itemService.searchItems("дрель", anotherUserId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Дрель аккумуляторная");
    }

    @Test
    void searchItems_WithEmptyText_ReturnsEmptyList() {
        ItemDto item = ItemDto.builder()
                .name("Дрель")
                .description("Описание")
                .available(true)
                .build();
        itemService.createItem(item, ownerId);

        List<ItemDto> result = itemService.searchItems("", anotherUserId);

        assertThat(result).isEmpty();
    }

    @Test
    void updateItem_WhenUserIsOwner_UpdatesSuccessfully() {
        ItemDto originalItem = ItemDto.builder()
                .name("Старое название")
                .description("Старое описание")
                .available(true)
                .build();

        ItemResponseDto created = itemService.createItem(originalItem, ownerId);

        ItemDto updateDto = ItemDto.builder()
                .name("Новое название")
                .description("Новое описание")
                .available(false)
                .build();

        ItemResponseDto updated = itemService.updateItem(created.getId(), updateDto, ownerId);

        assertThat(updated.getName()).isEqualTo("Новое название");
        assertThat(updated.getDescription()).isEqualTo("Новое описание");
        assertThat(updated.getAvailable()).isFalse();
    }

    @Test
    void updateItem_WhenUserIsNotOwner_ThrowsNotFoundException() {
        ItemDto itemDto = ItemDto.builder()
                .name("Вещь")
                .description("Описание")
                .available(true)
                .build();

        ItemResponseDto created = itemService.createItem(itemDto, ownerId);

        ItemDto updateDto = ItemDto.builder()
                .name("Попытка обновить")
                .build();

        assertThatThrownBy(() -> itemService.updateItem(created.getId(), updateDto, anotherUserId))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Only owner can update item");
    }
}

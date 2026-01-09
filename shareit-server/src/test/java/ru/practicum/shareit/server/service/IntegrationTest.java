package ru.practicum.shareit.server.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.dto.ItemDto;
import ru.practicum.shareit.dto.ItemRequestDto;
import ru.practicum.shareit.dto.UserDto;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class IntegrationTest {

    @Autowired
    private ItemRequestService itemRequestService;

    @Autowired
    private ItemService itemService;

    @Autowired
    private UserService userService;

    // ============== ИНТЕГРАЦИОННЫЙ ТЕСТ СВЯЗИ ЗАПРОСОВ И ВЕЩЕЙ ==============

    @Test
    void createItemInResponseToRequest_LinkIsEstablished() {
        // 1. Пользователь создает запрос на вещь
        UserDto requester = userService.createUser(UserDto.builder()
                .name("Requester")
                .email("requester-" + UUID.randomUUID() + "@example.com")
                .build());

        UserDto owner = userService.createUser(UserDto.builder()
                .name("Owner")
                .email("owner-" + UUID.randomUUID() + "@example.com")
                .build());

        ItemRequestDto requestDto = ItemRequestDto.builder()
                .description("Need a camping tent")
                .build();
        ItemRequestDto createdRequest = itemRequestService.createItemRequest(requestDto, requester.getId());

        // 2. Другой пользователь создает вещь в ответ на запрос
        ItemDto itemDto = ItemDto.builder()
                .name("Camping Tent")
                .description("4-person waterproof tent")
                .available(true)
                .requestId(createdRequest.getId())
                .build();
        itemService.createItem(itemDto, owner.getId());

        // 3. Проверяем, что запрос содержит созданную вещь
        ItemRequestDto retrievedRequest = itemRequestService.getItemRequestById(createdRequest.getId(), requester.getId());

        assertThat(retrievedRequest.getItems()).hasSize(1);
        assertThat(retrievedRequest.getItems().get(0).getName()).isEqualTo("Camping Tent");
        assertThat(retrievedRequest.getItems().get(0).getRequestId()).isEqualTo(createdRequest.getId());

        // 4. Проверяем, что вещь создана успешно
        List<ItemDto> searchResults = itemService.searchItems("tent", requester.getId());
        assertThat(searchResults).hasSize(1);
        assertThat(searchResults.get(0).getRequestId()).isEqualTo(createdRequest.getId());
    }

    @Test
    void createItemWithoutRequest_WorksCorrectly() {
        UserDto owner = userService.createUser(UserDto.builder()
                .name("Owner")
                .email("owner-" + UUID.randomUUID() + "@example.com")
                .build());

        UserDto requester = userService.createUser(UserDto.builder()
                .name("Requester")
                .email("requester-" + UUID.randomUUID() + "@example.com")
                .build());

        // Создаем вещь без привязки к запросу
        ItemDto itemDto = ItemDto.builder()
                .name("Hammer")
                .description("Regular hammer")
                .available(true)
                .requestId(null)
                .build();

        itemService.createItem(itemDto, owner.getId());

        // Проверяем, что вещь создана и доступна для поиска
        List<ItemDto> searchResults = itemService.searchItems("hammer", requester.getId());
        assertThat(searchResults).hasSize(1);
        assertThat(searchResults.get(0).getRequestId()).isNull();
    }
}

package ru.practicum.shareit.server.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.dto.ItemDto;
import ru.practicum.shareit.dto.ItemRequestDto;
import ru.practicum.shareit.dto.UserDto;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class ItemRequestItemIntegrationTest {

    @Autowired
    private ItemRequestService itemRequestService;

    @Autowired
    private ItemService itemService;

    @Autowired
    private UserService userService;

    private Long requesterId;
    private Long ownerId;

    @BeforeEach
    void setUp() {
        UserDto requester = UserDto.builder()
                .name("Requester")
                .email("requester@example.com")
                .build();
        UserDto savedRequester = userService.createUser(requester);
        requesterId = savedRequester.getId();

        UserDto owner = UserDto.builder()
                .name("Owner")
                .email("owner@example.com")
                .build();
        UserDto savedOwner = userService.createUser(owner);
        ownerId = savedOwner.getId();
    }

    @Test
    void createItemInResponseToRequest_LinkIsEstablished() {
        // 1. Пользователь создает запрос на вещь
        ItemRequestDto requestDto = ItemRequestDto.builder()
                .description("Need a camping tent")
                .build();
        ItemRequestDto createdRequest = itemRequestService.createItemRequest(requestDto, requesterId);

        // 2. Другой пользователь создает вещь в ответ на запрос
        ItemDto itemDto = ItemDto.builder()
                .name("Camping Tent")
                .description("4-person waterproof tent")
                .available(true)
                .requestId(createdRequest.getId())
                .build();
        itemService.createItem(itemDto, ownerId);

        // 3. Проверяем, что запрос содержит созданную вещь
        ItemRequestDto retrievedRequest = itemRequestService.getItemRequestById(createdRequest.getId(), requesterId);

        assertThat(retrievedRequest.getItems()).hasSize(1);
        assertThat(retrievedRequest.getItems().get(0).getName()).isEqualTo("Camping Tent");
        assertThat(retrievedRequest.getItems().get(0).getRequestId()).isEqualTo(createdRequest.getId());

        // 4. Проверяем, что вещь создана успешно
        List<ItemDto> searchResults = itemService.searchItems("tent", requesterId);
        assertThat(searchResults).hasSize(1);
        assertThat(searchResults.get(0).getRequestId()).isEqualTo(createdRequest.getId());
    }

    @Test
    void createItemWithoutRequest_WorksCorrectly() {
        // Создаем вещь без привязки к запросу (requestId = null)
        ItemDto itemDto = ItemDto.builder()
                .name("Hammer")
                .description("Regular hammer")
                .available(true)
                .requestId(null) // Явно указываем null
                .build();

        itemService.createItem(itemDto, ownerId);

        // Проверяем, что вещь создана и доступна для поиска
        List<ItemDto> searchResults = itemService.searchItems("hammer", requesterId);
        assertThat(searchResults).hasSize(1);
        assertThat(searchResults.get(0).getRequestId()).isNull();
    }
}

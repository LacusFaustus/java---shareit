package ru.practicum.shareit.server.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.dto.*;
import ru.practicum.shareit.server.service.ItemService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemServerController {
    private final ItemService itemService;

    @PostMapping
    public ResponseEntity<ItemResponseDto> createItem(
            @RequestBody ItemDto itemDto, // Убрано @Valid
            @RequestHeader("X-Sharer-User-Id") Long ownerId) {
        log.info("SERVER: POST /items - создание вещи пользователем {}", ownerId);
        return ResponseEntity.ok(itemService.createItem(itemDto, ownerId));
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<ItemResponseDto> updateItem(
            @PathVariable Long itemId,
            @RequestBody ItemDto itemDto,
            @RequestHeader("X-Sharer-User-Id") Long ownerId) {
        log.info("SERVER: PATCH /items/{} - обновление вещи пользователем {}", itemId, ownerId);
        return ResponseEntity.ok(itemService.updateItem(itemId, itemDto, ownerId));
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<ItemResponseDto> getItemById(
            @PathVariable Long itemId,
            @RequestHeader(value = "X-Sharer-User-Id", required = false) Long userId) {
        log.info("SERVER: GET /items/{} - получение вещи", itemId);
        return ResponseEntity.ok(itemService.getItemById(itemId, userId));
    }

    @GetMapping
    public ResponseEntity<List<ItemOwnerDto>> getAllItemsByOwner(
            @RequestHeader("X-Sharer-User-Id") Long ownerId) {
        log.info("SERVER: GET /items - получение всех вещей владельца {}", ownerId);
        return ResponseEntity.ok(itemService.getAllItemsByOwner(ownerId));
    }

    @GetMapping("/search")
    public ResponseEntity<List<ItemDto>> searchItems(
            @RequestParam String text, // Убрано @NotBlank
            @RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("SERVER: GET /items/search?text={} - поиск вещей", text);
        return ResponseEntity.ok(itemService.searchItems(text, userId));
    }

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<CommentDto> addComment(
            @PathVariable Long itemId,
            @RequestBody CommentDto commentDto,
            @RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("SERVER: POST /items/{}/comment - добавление комментария", itemId);
        return ResponseEntity.ok(itemService.addComment(itemId, commentDto, userId));
    }
}

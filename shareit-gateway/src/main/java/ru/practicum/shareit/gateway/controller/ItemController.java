package ru.practicum.shareit.gateway.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.gateway.client.CachedItemClient;
import ru.practicum.shareit.dto.CommentDto;
import ru.practicum.shareit.dto.ItemDto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

@Slf4j
@Validated
@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {
    private final CachedItemClient itemClient;

    @PostMapping
    public ResponseEntity<Object> createItem(
            @Valid @RequestBody ItemDto itemDto,
            @RequestHeader("X-Sharer-User-Id") Long ownerId) {
        log.info("POST /items - создание вещи пользователем {}", ownerId);
        return itemClient.createItem(itemDto, ownerId);
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<Object> updateItem(
            @PathVariable Long itemId,
            @RequestBody ItemDto itemDto,
            @RequestHeader("X-Sharer-User-Id") Long ownerId) {
        log.info("PATCH /items/{} - обновление вещи пользователем {}", itemId, ownerId);
        return itemClient.updateItem(itemId, itemDto, ownerId);
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<Object> getItemById(
            @PathVariable Long itemId,
            @RequestHeader(value = "X-Sharer-User-Id", required = false) Long userId) {
        log.info("GET /items/{} - получение вещи", itemId);
        Long actualUserId = userId != null ? userId : 0L;
        return itemClient.getItemById(itemId, actualUserId);
    }

    @GetMapping
    public ResponseEntity<Object> getAllItemsByOwner(
            @RequestHeader("X-Sharer-User-Id") Long ownerId) {
        log.info("GET /items - получение всех вещей владельца {}", ownerId);
        return itemClient.getAllItemsByOwner(ownerId);
    }

    @GetMapping("/search")
    public ResponseEntity<Object> searchItems(
            @RequestParam @NotBlank String text,
            @RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("GET /items/search?text={} - поиск вещей пользователем {}", text, userId);
        return itemClient.searchItems(text, userId);
    }

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<Object> addComment(
            @PathVariable Long itemId,
            @Valid @RequestBody CommentDto commentDto,
            @RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("POST /items/{}/comment - добавление комментария пользователем {}", itemId, userId);
        return itemClient.addComment(itemId, commentDto, userId);
    }
}

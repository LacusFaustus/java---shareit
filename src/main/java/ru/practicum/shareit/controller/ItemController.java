package ru.practicum.shareit.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.dto.*;
import ru.practicum.shareit.service.ItemService;
import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {
    private final ItemService itemService;

    @PostMapping
    public ResponseEntity<ItemResponseDto> createItem(
            @Valid @RequestBody ItemDto itemDto,
            @RequestHeader("X-Sharer-User-Id") Long ownerId) {
        ItemResponseDto createdItem = itemService.createItem(itemDto, ownerId);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdItem);
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<ItemResponseDto> updateItem(
            @PathVariable Long itemId,
            @RequestBody ItemDto itemDto,
            @RequestHeader("X-Sharer-User-Id") Long ownerId) {
        ItemResponseDto updatedItem = itemService.updateItem(itemId, itemDto, ownerId);
        return ResponseEntity.ok(updatedItem);
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<ItemResponseDto> getItemById(
            @PathVariable Long itemId,
            @RequestHeader(value = "X-Sharer-User-Id", required = false) Long userId) {
        ItemResponseDto item = itemService.getItemById(itemId, userId);
        return ResponseEntity.ok(item);
    }

    @GetMapping
    public ResponseEntity<List<ItemOwnerDto>> getAllItemsByOwner(
            @RequestHeader("X-Sharer-User-Id") Long ownerId) {
        List<ItemOwnerDto> items = itemService.getAllItemsByOwner(ownerId);
        return ResponseEntity.ok(items);
    }

    @GetMapping("/search")
    public ResponseEntity<List<ItemDto>> searchItems(
            @RequestParam String text,
            @RequestHeader("X-Sharer-User-Id") Long userId) {
        List<ItemDto> items = itemService.searchItems(text, userId);
        return ResponseEntity.ok(items);
    }

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<CommentDto> addComment(
            @PathVariable Long itemId,
            @Valid @RequestBody CommentDto commentDto,
            @RequestHeader("X-Sharer-User-Id") Long userId) {
        CommentDto createdComment = itemService.addComment(itemId, commentDto, userId);
        return ResponseEntity.ok(createdComment);
    }
}

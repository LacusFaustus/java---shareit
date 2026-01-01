package ru.practicum.shareit.server.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.dto.ItemRequestDto;
import ru.practicum.shareit.server.service.ItemRequestService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
public class ItemRequestServerController {
    private final ItemRequestService itemRequestService;

    @PostMapping
    public ResponseEntity<ItemRequestDto> createItemRequest(
            @RequestBody ItemRequestDto itemRequestDto,  // Убрана валидация
            @RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("SERVER: POST /requests - создание запроса вещи пользователем {}", userId);
        return ResponseEntity.ok(itemRequestService.createItemRequest(itemRequestDto, userId));
    }

    @GetMapping
    public ResponseEntity<List<ItemRequestDto>> getUserItemRequests(
            @RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("SERVER: GET /requests - получение запросов пользователя {}", userId);
        return ResponseEntity.ok(itemRequestService.getUserItemRequests(userId));
    }

    @GetMapping("/all")
    public ResponseEntity<List<ItemRequestDto>> getAllItemRequests(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @RequestParam(defaultValue = "0") int from,
            @RequestParam(defaultValue = "10") int size) {
        log.info("SERVER: GET /requests/all?from={}&size={} - получение всех запросов", from, size);
        return ResponseEntity.ok(itemRequestService.getAllItemRequests(userId, from, size));
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<ItemRequestDto> getItemRequestById(
            @PathVariable Long requestId,
            @RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("SERVER: GET /requests/{} - получение запроса", requestId);
        return ResponseEntity.ok(itemRequestService.getItemRequestById(requestId, userId));
    }
}

package ru.practicum.shareit.service;

import ru.practicum.shareit.dto.*;

import java.util.List;

public interface ItemService {

    ItemResponseDto createItem(ItemDto itemDto, Long ownerId);

    ItemResponseDto updateItem(Long itemId, ItemDto itemDto, Long ownerId);

    ItemResponseDto getItemById(Long itemId, Long userId);

    List<?> getAllItemsByOwner(Long ownerId);

    List<ItemDto> searchItems(String text, Long userId);

    CommentDto addComment(Long itemId, CommentDto commentDto, Long userId);
}

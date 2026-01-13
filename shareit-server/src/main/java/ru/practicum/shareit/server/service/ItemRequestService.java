package ru.practicum.shareit.server.service;

import ru.practicum.shareit.dto.ItemRequestDto;
import java.util.List;

public interface ItemRequestService {
    ItemRequestDto createItemRequest(ItemRequestDto itemRequestDto, Long userId);

    List<ItemRequestDto> getUserItemRequests(Long userId);

    List<ItemRequestDto> getAllItemRequests(Long userId, int from, int size);

    ItemRequestDto getItemRequestById(Long requestId, Long userId);
}

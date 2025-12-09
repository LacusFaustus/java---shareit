package ru.practicum.shareit.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.dto.ItemRequestDto;
import ru.practicum.shareit.model.ItemRequest;

@Component
public class ItemRequestMapper {

    public ItemRequestDto toDto(ItemRequest itemRequest) {
        if (itemRequest == null) {
            return null;
        }

        return ItemRequestDto.builder()
                .id(itemRequest.getId())
                .description(itemRequest.getDescription())
                .created(itemRequest.getCreated())
                .build();
    }

    public ItemRequest toEntity(ItemRequestDto itemRequestDto) {
        if (itemRequestDto == null) {
            return null;
        }

        ItemRequest itemRequest = new ItemRequest();
        itemRequest.setId(itemRequestDto.getId());
        itemRequest.setDescription(itemRequestDto.getDescription());
        itemRequest.setCreated(itemRequestDto.getCreated());
        return itemRequest;
    }
}

package ru.practicum.shareit.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.dto.ItemDto;
import ru.practicum.shareit.dto.ItemOwnerDto;
import ru.practicum.shareit.dto.ItemResponseDto;
import ru.practicum.shareit.model.Item;

@Component
@RequiredArgsConstructor
public class ItemMapper {

    public Item toEntity(ItemDto itemDto) {
        if (itemDto == null) {
            return null;
        }

        Item item = new Item();
        item.setName(itemDto.getName());
        item.setDescription(itemDto.getDescription());
        item.setAvailable(itemDto.getAvailable());
        item.setRequestId(itemDto.getRequestId());
        // owner устанавливается в сервисе
        return item;
    }

    public ItemDto toDto(Item item) {
        if (item == null) {
            return null;
        }

        return ItemDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable())
                .requestId(item.getRequestId())
                .build();
    }

    public ItemResponseDto toResponseDto(Item item) {
        if (item == null) {
            return null;
        }

        return ItemResponseDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable())
                .requestId(item.getRequestId())
                // lastBooking, nextBooking, comments устанавливаются в сервисе
                .build();
    }

    public ItemOwnerDto toOwnerDto(Item item) {
        if (item == null) {
            return null;
        }

        return ItemOwnerDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable())
                .requestId(item.getRequestId())
                // lastBooking, nextBooking, comments устанавливаются в сервисе
                .build();
    }
}

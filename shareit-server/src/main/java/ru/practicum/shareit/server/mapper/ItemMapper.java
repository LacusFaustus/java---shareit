package ru.practicum.shareit.server.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.shareit.server.config.MapStructConfig;
import ru.practicum.shareit.dto.*;
import ru.practicum.shareit.server.model.Item;

import java.util.List;

@Mapper(config = MapStructConfig.class)
public interface ItemMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "owner", ignore = true)
    @Mapping(target = "comments", ignore = true)
    Item toEntity(ItemDto itemDto);

    ItemDto toDto(Item item);

    @Mapping(target = "lastBooking", ignore = true)
    @Mapping(target = "nextBooking", ignore = true)
    @Mapping(target = "comments", ignore = true)
    ItemResponseDto toResponseDto(Item item);

    @Mapping(target = "lastBooking", ignore = true)
    @Mapping(target = "nextBooking", ignore = true)
    @Mapping(target = "comments", ignore = true)
    ItemOwnerDto toOwnerDto(Item item);

    List<ItemDto> toDtoList(List<Item> items);

    List<ItemOwnerDto> toOwnerDtoList(List<Item> items);
}

package ru.practicum.shareit.server.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.shareit.server.config.MapStructConfig;
import ru.practicum.shareit.dto.ItemRequestDto;
import ru.practicum.shareit.server.model.ItemRequest;

@Mapper(config = MapStructConfig.class)
public interface ItemRequestMapper {

    @Mapping(target = "items", ignore = true)
    @Mapping(source = "requestor.id", target = "requestorId")
    ItemRequestDto toDto(ItemRequest itemRequest);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "requestor", ignore = true)
    @Mapping(target = "created", ignore = true)
    ItemRequest toEntity(ItemRequestDto itemRequestDto);
}

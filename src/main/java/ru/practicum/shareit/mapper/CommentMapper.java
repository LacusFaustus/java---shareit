package ru.practicum.shareit.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.shareit.config.MapStructConfig;
import ru.practicum.shareit.dto.CommentDto;
import ru.practicum.shareit.model.Comment;

@Mapper(config = MapStructConfig.class)
public interface CommentMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "item", ignore = true)
    @Mapping(target = "author", ignore = true)
    @Mapping(target = "created", ignore = true)
    Comment toEntity(CommentDto commentDto);

    @Mapping(source = "author.name", target = "authorName")
    CommentDto toDto(Comment comment);
}

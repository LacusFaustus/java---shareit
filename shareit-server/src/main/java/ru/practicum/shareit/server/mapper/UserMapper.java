package ru.practicum.shareit.server.mapper;

import org.mapstruct.Mapper;
import ru.practicum.shareit.server.config.MapStructConfig;
import ru.practicum.shareit.dto.UserDto;
import ru.practicum.shareit.server.model.User;

import java.util.List;

@Mapper(config = MapStructConfig.class)
public interface UserMapper {

    UserDto toDto(User user);

    List<UserDto> toDtoList(List<User> users);
}

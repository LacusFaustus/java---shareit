package ru.practicum.shareit.mapper;

import org.mapstruct.Mapper;
import ru.practicum.shareit.config.MapStructConfig;
import ru.practicum.shareit.dto.UserDto;
import ru.practicum.shareit.model.User;

import java.util.List;

@Mapper(config = MapStructConfig.class)
public interface UserMapper {

    UserDto toDto(User user);

    List<UserDto> toDtoList(List<User> users);
}

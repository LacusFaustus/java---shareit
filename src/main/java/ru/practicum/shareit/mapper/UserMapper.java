package ru.practicum.shareit.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.dto.UserDto;
import ru.practicum.shareit.model.User;

@Component
public class UserMapper {

    public User toEntity(UserDto userDto) {
        if (userDto == null) {
            return null;
        }

        User user = new User();
        user.setName(userDto.getName());
        user.setEmail(userDto.getEmail());
        return user;
    }

    public UserDto toDto(User user) {
        if (user == null) {
            return null;
        }

        return UserDto.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .build();
    }
}

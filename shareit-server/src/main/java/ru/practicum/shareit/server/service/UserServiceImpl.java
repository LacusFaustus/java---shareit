package ru.practicum.shareit.server.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.dto.UserDto;
import ru.practicum.shareit.server.exception.ConflictException;
import ru.practicum.shareit.server.exception.NotFoundException;
import ru.practicum.shareit.server.exception.ValidationException;
import ru.practicum.shareit.server.mapper.UserMapper;
import ru.practicum.shareit.server.model.User;
import ru.practicum.shareit.server.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public UserDto createUser(UserDto userDto) {
        log.info("Creating user with email: {}", userDto.getEmail());

        validateUser(userDto);

        if (Boolean.TRUE.equals(userRepository.existsByEmail(userDto.getEmail()))) {
            throw new ConflictException("Email already exists");
        }

        User user = User.builder()
                .name(userDto.getName())
                .email(userDto.getEmail())
                .build();

        User savedUser = userRepository.save(user);
        return userMapper.toDto(savedUser);
    }

    @Override
    @Transactional
    public UserDto updateUser(Long userId, UserDto userDto) {
        log.info("Updating user ID: {}", userId);

        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (userDto.getName() != null) {
            existingUser.setName(userDto.getName());
        }

        if (userDto.getEmail() != null && !existingUser.getEmail().equals(userDto.getEmail())) {
            if (Boolean.TRUE.equals(userRepository.existsByEmail(userDto.getEmail()))) {
                throw new ConflictException("Email already exists");
            }
            existingUser.setEmail(userDto.getEmail());
        }

        User updatedUser = userRepository.save(existingUser);
        return userMapper.toDto(updatedUser);
    }

    @Override
    public UserDto getUserById(Long userId) {
        log.info("Getting user by ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        return userMapper.toDto(user);
    }

    @Override
    public List<UserDto> getAllUsers() {
        log.info("Getting all users");

        List<UserDto> users = userRepository.findAll().stream()
                .map(userMapper::toDto)
                .collect(Collectors.toList());

        log.info("Found {} users", users.size());
        return users;
    }

    @Override
    @Transactional
    public void deleteUser(Long userId) {
        log.info("Deleting user ID: {}", userId);

        if (!userRepository.findById(userId).isPresent()) {
            throw new NotFoundException("User not found");
        }

        userRepository.deleteById(userId);
    }

    private void validateUser(UserDto userDto) {
        if (userDto.getEmail() == null || userDto.getEmail().isBlank() || !userDto.getEmail().contains("@")) {
            throw new ValidationException("Invalid email");
        }
        if (userDto.getName() == null || userDto.getName().isBlank()) {
            throw new ValidationException("Name cannot be empty");
        }
    }
}

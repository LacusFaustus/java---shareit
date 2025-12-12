package ru.practicum.shareit.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.dto.UserDto;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.mapper.UserMapper;
import ru.practicum.shareit.model.User;
import ru.practicum.shareit.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    public UserDto createUser(UserDto userDto) {
        log.info("Creating user with email: {}", userDto.getEmail());

        validateUser(userDto);

        if (Boolean.TRUE.equals(userRepository.existsByEmail(userDto.getEmail()))) {
            log.warn("Email already exists: {}", userDto.getEmail());
            throw new ConflictException("Email already exists");
        }

        User user = new User();
        user.setName(userDto.getName());
        user.setEmail(userDto.getEmail());

        User savedUser = userRepository.save(user);

        log.info("User created successfully with ID: {} and email: {}", savedUser.getId(), savedUser.getEmail());
        return userMapper.toDto(savedUser);
    }

    @Override
    public UserDto updateUser(Long userId, UserDto userDto) {
        log.info("Updating user ID: {}", userId);

        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("User not found with ID: {}", userId);
                    return new NotFoundException("User not found");
                });

        if (userDto.getName() != null) {
            existingUser.setName(userDto.getName());
            log.debug("Updating user ID: {} name", userId);
        }

        if (userDto.getEmail() != null && !existingUser.getEmail().equals(userDto.getEmail())) {
            if (Boolean.TRUE.equals(userRepository.existsByEmail(userDto.getEmail()))) {
                log.warn("Email already exists: {}", userDto.getEmail());
                throw new ConflictException("Email already exists");
            }
            existingUser.setEmail(userDto.getEmail());
            log.debug("Updating user ID: {} email to: {}", userId, userDto.getEmail());
        }

        User updatedUser = userRepository.save(existingUser);
        log.info("User ID: {} updated successfully", userId);

        return userMapper.toDto(updatedUser);
    }

    @Override
    public UserDto getUserById(Long userId) {
        log.info("Getting user by ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("User not found with ID: {}", userId);
                    return new NotFoundException("User not found");
                });

        log.info("User ID: {} retrieved successfully", userId);
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
    public void deleteUser(Long userId) {
        log.info("Deleting user ID: {}", userId);

        if (!userRepository.findById(userId).isPresent()) {
            log.warn("User not found with ID: {}", userId);
            throw new NotFoundException("User not found");
        }

        userRepository.deleteById(userId);
        log.info("User ID: {} deleted successfully", userId);
    }

    private void validateUser(UserDto userDto) {
        if (userDto.getEmail() == null || userDto.getEmail().isBlank() || !userDto.getEmail().contains("@")) {
            log.warn("Invalid email: {}", userDto.getEmail());
            throw new ValidationException("Invalid email");
        }
        if (userDto.getName() == null || userDto.getName().isBlank()) {
            log.warn("User name cannot be empty");
            throw new ValidationException("Name cannot be empty");
        }
    }
}

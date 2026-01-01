package ru.practicum.shareit.server.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.dto.UserDto;
import ru.practicum.shareit.server.exception.ConflictException;
import ru.practicum.shareit.server.exception.NotFoundException;
import ru.practicum.shareit.server.exception.ValidationException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class UserServiceImplIntegrationTest {

    @Autowired
    private UserService userService;

    private UserDto testUser;

    @BeforeEach
    void setUp() {
        testUser = UserDto.builder()
                .name("Test User")
                .email("test@example.com")
                .build();
    }

    @Test
    void createUser_WithValidData_CreatesSuccessfully() {
        UserDto result = userService.createUser(testUser);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        assertThat(result.getName()).isEqualTo("Test User");
        assertThat(result.getEmail()).isEqualTo("test@example.com");
    }

    @Test
    void createUser_WithDuplicateEmail_ThrowsConflictException() {
        userService.createUser(testUser);

        UserDto duplicateUser = UserDto.builder()
                .name("Another User")
                .email("test@example.com")
                .build();

        assertThatThrownBy(() -> userService.createUser(duplicateUser))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Email already exists");
    }

    @Test
    void createUser_WithInvalidEmail_ThrowsValidationException() {
        UserDto invalidUser = UserDto.builder()
                .name("Test User")
                .email("invalid-email")
                .build();

        assertThatThrownBy(() -> userService.createUser(invalidUser))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Invalid email");
    }

    @Test
    void getUserById_WhenUserExists_ReturnsUser() {
        UserDto createdUser = userService.createUser(testUser);
        UserDto result = userService.getUserById(createdUser.getId());

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(createdUser.getId());
        assertThat(result.getName()).isEqualTo("Test User");
    }

    @Test
    void getUserById_WhenUserNotExists_ThrowsNotFoundException() {
        assertThatThrownBy(() -> userService.getUserById(999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void updateUser_WithValidData_UpdatesSuccessfully() {
        UserDto createdUser = userService.createUser(testUser);

        UserDto updateDto = UserDto.builder()
                .name("Updated Name")
                .email("updated@example.com")
                .build();

        UserDto result = userService.updateUser(createdUser.getId(), updateDto);

        assertThat(result.getName()).isEqualTo("Updated Name");
        assertThat(result.getEmail()).isEqualTo("updated@example.com");
    }

    @Test
    void updateUser_WithDuplicateEmail_ThrowsConflictException() {
        UserDto user1 = userService.createUser(testUser);

        UserDto user2 = UserDto.builder()
                .name("User 2")
                .email("user2@example.com")
                .build();
        userService.createUser(user2);

        UserDto updateDto = UserDto.builder()
                .name("Updated Name")
                .email("user2@example.com")
                .build();

        assertThatThrownBy(() -> userService.updateUser(user1.getId(), updateDto))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Email already exists");
    }

    @Test
    void getAllUsers_ReturnsAllUsers() {
        userService.createUser(testUser);

        UserDto user2 = UserDto.builder()
                .name("User 2")
                .email("user2@example.com")
                .build();
        userService.createUser(user2);

        List<UserDto> users = userService.getAllUsers();

        assertThat(users).hasSize(2);
        assertThat(users)
                .extracting("email")
                .containsExactlyInAnyOrder("test@example.com", "user2@example.com");
    }

    @Test
    void deleteUser_WhenUserExists_DeletesSuccessfully() {
        UserDto createdUser = userService.createUser(testUser);
        List<UserDto> beforeDelete = userService.getAllUsers();
        assertThat(beforeDelete).hasSize(1);

        userService.deleteUser(createdUser.getId());

        List<UserDto> afterDelete = userService.getAllUsers();
        assertThat(afterDelete).isEmpty();
    }

    @Test
    void deleteUser_WhenUserNotExists_ThrowsNotFoundException() {
        assertThatThrownBy(() -> userService.deleteUser(999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("User not found");
    }
}

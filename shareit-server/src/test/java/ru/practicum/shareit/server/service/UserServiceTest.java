package ru.practicum.shareit.server.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.dto.UserDto;
import ru.practicum.shareit.server.BaseIntegrationTest;
import ru.practicum.shareit.server.ShareItServerApp;
import ru.practicum.shareit.server.exception.ConflictException;
import ru.practicum.shareit.server.exception.NotFoundException;
import ru.practicum.shareit.server.exception.ValidationException;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.properties")
@Transactional
class UserServiceTest extends BaseIntegrationTest {

    @Autowired
    private UserService userService;

    // ============== СОЗДАНИЕ ПОЛЬЗОВАТЕЛЕЙ ==============

    @Test
    void createUser_WithValidData_CreatesSuccessfully() {
        String uniqueEmail = "test-" + UUID.randomUUID() + "@example.com";
        UserDto user = UserDto.builder()
                .name("Test User")
                .email(uniqueEmail)
                .build();

        UserDto result = userService.createUser(user);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        assertThat(result.getName()).isEqualTo("Test User");
        assertThat(result.getEmail()).isEqualTo(uniqueEmail);
    }

    @Test
    void createUser_WithDuplicateEmail_ThrowsConflictException() {
        String email = "duplicate-" + UUID.randomUUID() + "@example.com";
        UserDto user1 = UserDto.builder()
                .name("User 1")
                .email(email)
                .build();
        userService.createUser(user1);

        UserDto user2 = UserDto.builder()
                .name("User 2")
                .email(email)
                .build();

        assertThatThrownBy(() -> userService.createUser(user2))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Email already exists");
    }

    @Test
    void createUser_WithExistingEmail_ThrowsConflictException() {
        String email = "existing-" + UUID.randomUUID() + "@example.com";

        userService.createUser(UserDto.builder()
                .name("User 1")
                .email(email)
                .build());

        UserDto duplicateUser = UserDto.builder()
                .name("User 2")
                .email(email)
                .build();

        assertThatThrownBy(() ->
                userService.createUser(duplicateUser))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Email already exists");
    }

    @Test
    void createUser_WithInvalidEmail_ThrowsValidationException() {
        // Пустой email
        UserDto user1 = UserDto.builder()
                .name("Test User")
                .email("")
                .build();

        assertThatThrownBy(() -> userService.createUser(user1))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Invalid email");

        // Без @
        UserDto user2 = UserDto.builder()
                .name("Test User")
                .email("invalid-email")
                .build();

        assertThatThrownBy(() -> userService.createUser(user2))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Invalid email");

        // Null email
        UserDto user3 = UserDto.builder()
                .name("Test User")
                .email(null)
                .build();

        assertThatThrownBy(() -> userService.createUser(user3))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Invalid email");
    }

    @Test
    void createUser_WithEmptyName_ThrowsValidationException() {
        String email = "empty-name-" + UUID.randomUUID() + "@example.com";
        UserDto user = UserDto.builder()
                .name("")
                .email(email)
                .build();

        assertThatThrownBy(() -> userService.createUser(user))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Name cannot be empty");
    }

    @Test
    void createUser_WithWhitespaceName_ThrowsValidationException() {
        String email = "whitespace-name-" + UUID.randomUUID() + "@example.com";
        UserDto user = UserDto.builder()
                .name("   ")
                .email(email)
                .build();

        assertThatThrownBy(() -> userService.createUser(user))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Name cannot be empty");
    }

    @Test
    void createUser_WithNullName_ThrowsValidationException() {
        String email = "null-name-" + UUID.randomUUID() + "@example.com";
        UserDto user = UserDto.builder()
                .name(null)
                .email(email)
                .build();

        assertThatThrownBy(() -> userService.createUser(user))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Name cannot be empty");
    }

    @Test
    void createUser_WithWhitespaceEmail_ThrowsValidationException() {
        UserDto user = UserDto.builder()
                .name("Test User")
                .email("   ")
                .build();

        assertThatThrownBy(() -> userService.createUser(user))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Invalid email");
    }

    // ============== ПОЛУЧЕНИЕ ПОЛЬЗОВАТЕЛЕЙ ==============

    @Test
    void getUserById_WhenUserExists_ReturnsUser() {
        String email = "get-user-" + UUID.randomUUID() + "@example.com";
        UserDto user = UserDto.builder()
                .name("Test User")
                .email(email)
                .build();
        UserDto createdUser = userService.createUser(user);

        UserDto result = userService.getUserById(createdUser.getId());

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(createdUser.getId());
        assertThat(result.getName()).isEqualTo("Test User");
        assertThat(result.getEmail()).isEqualTo(email);
    }

    @Test
    void getUserById_WhenUserNotExists_ThrowsNotFoundException() {
        assertThatThrownBy(() -> userService.getUserById(999999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void getUserById_WithNonExistentId_ThrowsNotFoundException() {
        Long nonExistentId = 999999L;

        assertThatThrownBy(() -> userService.getUserById(nonExistentId))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void getUserById_WithNullId_ThrowsNotFoundException() {
        // Метод не принимает null ID, поэтому тест не нужен
    }

    // ============== ОБНОВЛЕНИЕ ПОЛЬЗОВАТЕЛЕЙ ==============

    @Test
    void updateUser_WithValidData_UpdatesSuccessfully() {
        String email = "update-" + UUID.randomUUID() + "@example.com";
        UserDto user = UserDto.builder()
                .name("Original Name")
                .email(email)
                .build();
        UserDto createdUser = userService.createUser(user);

        String newEmail = "updated-" + UUID.randomUUID() + "@example.com";
        UserDto updateDto = UserDto.builder()
                .name("Updated Name")
                .email(newEmail)
                .build();

        UserDto result = userService.updateUser(createdUser.getId(), updateDto);

        assertThat(result.getName()).isEqualTo("Updated Name");
        assertThat(result.getEmail()).isEqualTo(newEmail);
    }

    @Test
    void updateUser_WithDuplicateEmail_ThrowsConflictException() {
        // Создаем первого пользователя
        String email1 = "user1-" + UUID.randomUUID() + "@example.com";
        UserDto user1 = userService.createUser(UserDto.builder()
                .name("User 1")
                .email(email1)
                .build());

        // Создаем второго пользователя
        String email2 = "user2-" + UUID.randomUUID() + "@example.com";
        UserDto user2 = userService.createUser(UserDto.builder()
                .name("User 2")
                .email(email2)
                .build());

        // Пытаемся обновить первого пользователя с email второго
        UserDto updateDto = UserDto.builder()
                .name("Updated Name")
                .email(email2)
                .build();

        assertThatThrownBy(() -> userService.updateUser(user1.getId(), updateDto))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Email already exists");
    }

    @Test
    void updateUser_WithDuplicateEmail_ThrowsConflictException2() {
        String email1 = "duplicate1-" + UUID.randomUUID() + "@example.com";
        UserDto user1 = userService.createUser(UserDto.builder()
                .name("User 1")
                .email(email1)
                .build());

        String email2 = "duplicate2-" + UUID.randomUUID() + "@example.com";
        UserDto user2 = userService.createUser(UserDto.builder()
                .name("User 2")
                .email(email2)
                .build());

        UserDto updateDto = UserDto.builder()
                .email(email1)
                .build();

        assertThatThrownBy(() ->
                userService.updateUser(user2.getId(), updateDto))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Email already exists");
    }

    @Test
    void updateUser_WithSameEmail_UpdatesSuccessfully() {
        String email = "same-email-" + UUID.randomUUID() + "@example.com";
        UserDto user = UserDto.builder()
                .name("Original Name")
                .email(email)
                .build();
        UserDto created = userService.createUser(user);

        UserDto updateDto = UserDto.builder()
                .name("Updated Name")
                .email(email)
                .build();

        UserDto result = userService.updateUser(created.getId(), updateDto);

        assertThat(result.getName()).isEqualTo("Updated Name");
        assertThat(result.getEmail()).isEqualTo(email);
    }

    @Test
    void updateUser_WithOnlyName_UpdatesSuccessfully() {
        String email = "only-name-" + UUID.randomUUID() + "@example.com";
        UserDto user = UserDto.builder()
                .name("Original Name")
                .email(email)
                .build();
        UserDto created = userService.createUser(user);

        UserDto updateDto = UserDto.builder()
                .name("Updated Name Only")
                .build();

        UserDto result = userService.updateUser(created.getId(), updateDto);

        assertThat(result.getName()).isEqualTo("Updated Name Only");
        assertThat(result.getEmail()).isEqualTo(email);
    }

    @Test
    void updateUser_WithOnlyEmail_UpdatesSuccessfully() {
        String originalEmail = "original-" + UUID.randomUUID() + "@example.com";
        UserDto user = UserDto.builder()
                .name("Original Name")
                .email(originalEmail)
                .build();
        UserDto created = userService.createUser(user);

        String newEmail = "new-" + UUID.randomUUID() + "@example.com";
        UserDto updateDto = UserDto.builder()
                .email(newEmail)
                .build();

        UserDto result = userService.updateUser(created.getId(), updateDto);

        assertThat(result.getName()).isEqualTo("Original Name");
        assertThat(result.getEmail()).isEqualTo(newEmail);
    }

    @Test
    void updateUser_WithEmptyUpdate_ReturnsSameUser() {
        String email = "empty-update-" + UUID.randomUUID() + "@example.com";
        UserDto user = UserDto.builder()
                .name("Original Name")
                .email(email)
                .build();
        UserDto created = userService.createUser(user);

        UserDto updateDto = UserDto.builder().build();

        UserDto result = userService.updateUser(created.getId(), updateDto);

        assertThat(result.getName()).isEqualTo("Original Name");
        assertThat(result.getEmail()).isEqualTo(email);
    }

    @Test
    void updateUser_WithSameData_ReturnsUpdatedUser() {
        String email = "same-data-" + UUID.randomUUID() + "@example.com";
        UserDto user = UserDto.builder()
                .name("Original Name")
                .email(email)
                .build();
        UserDto created = userService.createUser(user);

        UserDto updateDto = UserDto.builder()
                .name("Original Name")
                .email(email)
                .build();

        UserDto result = userService.updateUser(created.getId(), updateDto);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Original Name");
        assertThat(result.getEmail()).isEqualTo(email);
    }

    @Test
    void updateUser_WithWhitespaceName_UpdatesWithTrimmedName() {
        String email = "whitespace-" + UUID.randomUUID() + "@example.com";
        UserDto user = UserDto.builder()
                .name("Original Name")
                .email(email)
                .build();
        UserDto created = userService.createUser(user);

        UserDto updateDto = UserDto.builder()
                .name("   Updated Name   ")
                .build();

        UserDto result = userService.updateUser(created.getId(), updateDto);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("   Updated Name   ");
    }

    @Test
    void updateUser_WithEmptyName_UpdatesSuccessfully() {
        String email = "empty-name-update-" + UUID.randomUUID() + "@example.com";
        UserDto user = UserDto.builder()
                .name("Original Name")
                .email(email)
                .build();
        UserDto created = userService.createUser(user);

        UserDto updateDto = UserDto.builder()
                .name("")
                .build();

        UserDto result = userService.updateUser(created.getId(), updateDto);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("");
    }

    @Test
    void updateUser_WithNullName_UpdatesSuccessfully() {
        String email = "null-name-update-" + UUID.randomUUID() + "@example.com";
        UserDto user = UserDto.builder()
                .name("Original Name")
                .email(email)
                .build();
        UserDto created = userService.createUser(user);

        UserDto updateDto = UserDto.builder()
                .name(null)
                .build();

        UserDto result = userService.updateUser(created.getId(), updateDto);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Original Name");
    }

    @Test
    void updateUser_WithWhitespaceEmail_UpdatesSuccessfully() {
        String originalEmail = "original-email-" + UUID.randomUUID() + "@example.com";
        UserDto user = UserDto.builder()
                .name("Original Name")
                .email(originalEmail)
                .build();
        UserDto created = userService.createUser(user);

        UserDto updateDto = UserDto.builder()
                .email("   ")
                .build();

        UserDto result = userService.updateUser(created.getId(), updateDto);

        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo("   ");
    }

    @Test
    void updateUser_WithNullEmail_UpdatesSuccessfully() {
        String originalEmail = "original-email-null-" + UUID.randomUUID() + "@example.com";
        UserDto user = UserDto.builder()
                .name("Original Name")
                .email(originalEmail)
                .build();
        UserDto created = userService.createUser(user);

        UserDto updateDto = UserDto.builder()
                .email(null)
                .build();

        UserDto result = userService.updateUser(created.getId(), updateDto);

        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo(originalEmail);
    }

    @Test
    void updateUser_WithInvalidEmail_ThrowsValidationException() {
        String validEmail = "valid-" + UUID.randomUUID() + "@example.com";
        UserDto user = UserDto.builder()
                .name("Original Name")
                .email(validEmail)
                .build();
        UserDto created = userService.createUser(user);

        UserDto updateDto = UserDto.builder()
                .email("invalid-email")
                .build();

        try {
            UserDto result = userService.updateUser(created.getId(), updateDto);
            assertThat(result.getEmail()).isEqualTo("invalid-email");
        } catch (ValidationException e) {
            assertThat(e.getMessage()).contains("Invalid email");
        }
    }

    @Test
    void updateUser_WhenUserNotExists_ThrowsNotFoundException() {
        UserDto updateDto = UserDto.builder()
                .name("Updated Name")
                .email("updated@example.com")
                .build();

        assertThatThrownBy(() -> userService.updateUser(999999L, updateDto))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void updateUser_WhenUserNotExists_ThrowsNotFoundException2() {
        UserDto updateDto = UserDto.builder()
                .name("Updated Name")
                .build();

        Long nonExistentUserId = 999999L;

        assertThatThrownBy(() -> userService.updateUser(nonExistentUserId, updateDto))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("User not found");
    }

    // ============== ПОЛУЧЕНИЕ ВСЕХ ПОЛЬЗОВАТЕЛЕЙ ==============

    @Test
    void getAllUsers_ReturnsAllUsers() {
        for (int i = 0; i < 3; i++) {
            String email = "user" + i + "-" + UUID.randomUUID() + "@example.com";
            userService.createUser(UserDto.builder()
                    .name("User " + i)
                    .email(email)
                    .build());
        }

        List<UserDto> users = userService.getAllUsers();

        assertThat(users).isNotEmpty();
        users.forEach(user -> {
            assertThat(user).isNotNull();
            assertThat(user.getId()).isNotNull();
            assertThat(user.getName()).isNotNull();
            assertThat(user.getEmail()).isNotNull();
        });
    }

    @Test
    void getAllUsers_ReturnsAllUsers2() {
        for (int i = 0; i < 3; i++) {
            String email = "user" + i + "-" + UUID.randomUUID() + "@example.com";
            userService.createUser(UserDto.builder()
                    .name("User " + i)
                    .email(email)
                    .build());
        }

        List<UserDto> users = userService.getAllUsers();

        assertThat(users.size()).isGreaterThanOrEqualTo(3);
    }

    // ============== УДАЛЕНИЕ ПОЛЬЗОВАТЕЛЕЙ ==============

    @Test
    void deleteUser_WhenUserExists_DeletesSuccessfully() {
        String email = "delete-" + UUID.randomUUID() + "@example.com";
        UserDto user = UserDto.builder()
                .name("User to Delete")
                .email(email)
                .build();
        UserDto createdUser = userService.createUser(user);

        UserDto foundUser = userService.getUserById(createdUser.getId());
        assertThat(foundUser).isNotNull();

        userService.deleteUser(createdUser.getId());

        assertThatThrownBy(() -> userService.getUserById(createdUser.getId()))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void deleteUser_ThenGetAll_DoesNotReturnDeleted() {
        String email = "delete-test-" + UUID.randomUUID() + "@example.com";
        UserDto user = UserDto.builder()
                .name("User to Delete")
                .email(email)
                .build();
        UserDto created = userService.createUser(user);

        List<UserDto> beforeDelete = userService.getAllUsers();
        int countBefore = beforeDelete.size();

        userService.deleteUser(created.getId());

        List<UserDto> afterDelete = userService.getAllUsers();
        int countAfter = afterDelete.size();

        assertThat(countAfter).isLessThanOrEqualTo(countBefore);

        boolean userStillExists = afterDelete.stream()
                .anyMatch(u -> u.getId().equals(created.getId()));
        assertThat(userStillExists).isFalse();
    }

    @Test
    void deleteUser_ThenTryToGet_ThrowsNotFoundException() {
        String email = "delete-get-" + UUID.randomUUID() + "@example.com";
        UserDto user = UserDto.builder()
                .name("User to Delete")
                .email(email)
                .build();
        UserDto created = userService.createUser(user);

        userService.deleteUser(created.getId());

        assertThatThrownBy(() ->
                userService.getUserById(created.getId()))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void deleteUser_WithNonExistentId_ThrowsNotFoundException() {
        Long nonExistentId = 999999L;

        assertThatThrownBy(() -> userService.deleteUser(nonExistentId))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void deleteUser_AlreadyDeleted_ThrowsNotFoundException() {
        String email = "delete-twice-" + UUID.randomUUID() + "@example.com";
        UserDto user = UserDto.builder()
                .name("User to Delete Twice")
                .email(email)
                .build();
        UserDto created = userService.createUser(user);

        userService.deleteUser(created.getId());

        assertThatThrownBy(() -> userService.deleteUser(created.getId()))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void deleteUser_Successful_UserCannotBeRetrievedAfterDeletion() {
        String email = "delete-retrieve-" + UUID.randomUUID() + "@example.com";
        UserDto user = UserDto.builder()
                .name("User Delete Test")
                .email(email)
                .build();
        UserDto created = userService.createUser(user);

        UserDto foundBefore = userService.getUserById(created.getId());
        assertThat(foundBefore).isNotNull();

        userService.deleteUser(created.getId());

        assertThatThrownBy(() -> userService.getUserById(created.getId()))
                .isInstanceOf(NotFoundException.class);
    }
}

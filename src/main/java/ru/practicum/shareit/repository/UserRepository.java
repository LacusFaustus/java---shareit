package ru.practicum.shareit.repository;

import ru.practicum.shareit.model.User;
import java.util.List;
import java.util.Optional;

public interface UserRepository {
    User save(User user);
    Optional<User> findById(Long id);
    List<User> findAll();
    void deleteById(Long id);
    boolean existsByEmail(String email);
}

package ru.practicum.shareit.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.shareit.server.model.User;

public interface UserRepository extends JpaRepository<User, Long> {
    Boolean existsByEmail(String email);
}

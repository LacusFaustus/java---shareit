package ru.practicum.shareit.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.shareit.model.User;

public interface UserRepository extends JpaRepository<User, Long> {

    Boolean existsByEmail(String email);
}

package ru.practicum.shareit.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.shareit.server.model.Comment;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByItemIdOrderByCreatedDesc(Long itemId);
}

package ru.practicum.shareit.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.shareit.server.model.Comment;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByItemIdOrderByCreatedDesc(Long itemId);

    // Новый метод для batch запросов
    @Query("SELECT c FROM Comment c WHERE c.item.id IN :itemIds ORDER BY c.created DESC")
    List<Comment> findAllByItemIdInOrderByCreatedDesc(@Param("itemIds") List<Long> itemIds);
}

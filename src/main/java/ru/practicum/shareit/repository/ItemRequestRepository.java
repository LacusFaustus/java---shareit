package ru.practicum.shareit.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.shareit.model.ItemRequest;

import java.util.List;

public interface ItemRequestRepository extends JpaRepository<ItemRequest, Long> {

    List<ItemRequest> findByRequestorIdOrderByCreatedDesc(Long requestorId);

    // Метод с пагинацией для получения запросов других пользователей
    @Query("SELECT ir FROM ItemRequest ir WHERE ir.requestor.id != :requestorId ORDER BY ir.created DESC")
    Page<ItemRequest> findOtherUsersRequests(@Param("requestorId") Long requestorId, Pageable pageable);

    // Альтернативный метод через имя (Spring Data JPA сгенерирует запрос)
    Page<ItemRequest> findByRequestorIdNot(Long requestorId, Pageable pageable);
}

package ru.practicum.shareit.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.shareit.server.model.Item;

import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long> {
    List<Item> findByOwnerIdOrderById(Long ownerId);

    @Query("SELECT i FROM Item i " +
            "WHERE (LOWER(i.name) LIKE LOWER(CONCAT('%', :text, '%')) " +
            "OR LOWER(i.description) LIKE LOWER(CONCAT('%', :text, '%'))) " +
            "AND i.available = true")
    List<Item> searchAvailableItems(@Param("text") String text);

    List<Item> findByRequestId(Long requestId);

    // Новый метод для batch запросов
    @Query("SELECT i FROM Item i WHERE i.requestId IN :requestIds")
    List<Item> findByRequestIdIn(@Param("requestIds") List<Long> requestIds);
}

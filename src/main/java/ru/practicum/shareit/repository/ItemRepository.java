package ru.practicum.shareit.repository;

import ru.practicum.shareit.model.Item;
import java.util.List;
import java.util.Optional;

public interface ItemRepository {
    Item save(Item item);
    Optional<Item> findById(Long id);
    List<Item> findAllByOwnerId(Long ownerId);
    List<Item> searchAvailableItems(String text);
    List<Item> findAll();
}

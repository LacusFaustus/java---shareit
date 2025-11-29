package ru.practicum.shareit.repository;

import ru.practicum.shareit.model.ItemRequest;
import java.util.List;
import java.util.Optional;

public interface ItemRequestRepository {
    ItemRequest save(ItemRequest itemRequest);
    Optional<ItemRequest> findById(Long id);
    List<ItemRequest> findByRequestorId(Long requestorId);
    List<ItemRequest> findAll();
}

package ru.practicum.shareit.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.dto.ItemDto;
import ru.practicum.shareit.mapper.ItemMapper;
import ru.practicum.shareit.repository.ItemRepository;
import ru.practicum.shareit.mapper.ItemRequestMapper;
import ru.practicum.shareit.model.ItemRequest;
import ru.practicum.shareit.model.User;
import ru.practicum.shareit.repository.ItemRequestRepository;
import ru.practicum.shareit.repository.UserRepository;
import ru.practicum.shareit.dto.ItemRequestDto;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemRequestServiceImpl implements ItemRequestService {
    private final ItemRequestRepository itemRequestRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final ItemRequestMapper itemRequestMapper;
    private final ItemMapper itemMapper;

    @Override
    public ItemRequestDto createItemRequest(ItemRequestDto itemRequestDto, Long userId) {
        if (itemRequestDto.getDescription() == null || itemRequestDto.getDescription().isBlank()) {
            throw new ValidationException("Item request description cannot be empty");
        }

        User requestor = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        ItemRequest itemRequest = new ItemRequest();
        itemRequest.setDescription(itemRequestDto.getDescription());
        itemRequest.setRequestor(requestor);
        itemRequest.setCreated(LocalDateTime.now());

        ItemRequest savedRequest = itemRequestRepository.save(itemRequest);
        return itemRequestMapper.toDto(savedRequest);
    }

    @Override
    public List<ItemRequestDto> getUserItemRequests(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        return itemRequestRepository.findByRequestorIdOrderByCreatedDesc(userId).stream()
                .map(itemRequest -> {
                    ItemRequestDto dto = itemRequestMapper.toDto(itemRequest);
                    addItemsToRequestDto(dto);
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemRequestDto> getAllItemRequests(Long userId, int from, int size) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (from < 0 || size <= 0) {
            throw new ValidationException("Invalid pagination parameters: from=" + from + ", size=" + size);
        }

        Pageable pageable = PageRequest.of(from / size, size, Sort.by(Sort.Direction.DESC, "created"));

        // Используем метод с пагинацией
        return itemRequestRepository.findByRequestorIdNot(userId, pageable).stream()
                .map(itemRequest -> {
                    ItemRequestDto dto = itemRequestMapper.toDto(itemRequest);
                    addItemsToRequestDto(dto);
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public ItemRequestDto getItemRequestById(Long requestId, Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        ItemRequest itemRequest = itemRequestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Item request not found"));

        ItemRequestDto dto = itemRequestMapper.toDto(itemRequest);
        addItemsToRequestDto(dto);
        return dto;
    }

    private void addItemsToRequestDto(ItemRequestDto dto) {
        List<ItemDto> items = itemRepository.findByRequestId(dto.getId()).stream()
                .map(itemMapper::toDto)
                .collect(Collectors.toList());
        dto.setItems(items);
    }
}

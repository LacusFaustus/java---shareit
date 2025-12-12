package ru.practicum.shareit.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
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
        log.info("Creating item request by user ID: {}", userId);

        if (itemRequestDto.getDescription() == null || itemRequestDto.getDescription().isBlank()) {
            log.warn("Attempt to create item request with empty description by user ID: {}", userId);
            throw new ValidationException("Item request description cannot be empty");
        }

        User requestor = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("User not found with ID: {}", userId);
                    return new NotFoundException("User not found");
                });

        ItemRequest itemRequest = itemRequestMapper.toEntity(itemRequestDto);
        itemRequest.setRequestor(requestor);
        itemRequest.setCreated(LocalDateTime.now());

        ItemRequest savedRequest = itemRequestRepository.save(itemRequest);
        log.info("Item request created successfully with ID: {}", savedRequest.getId());

        return convertToDtoWithItems(savedRequest);
    }

    @Override
    public List<ItemRequestDto> getUserItemRequests(Long userId) {
        log.info("Getting item requests for user ID: {}", userId);

        userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("User not found with ID: {}", userId);
                    return new NotFoundException("User not found");
                });

        List<ItemRequest> requests = itemRequestRepository.findByRequestorIdOrderByCreatedDesc(userId);

        return requests.stream()
                .map(this::convertToDtoWithItems)
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemRequestDto> getAllItemRequests(Long userId, int from, int size) {
        log.info("Getting all item requests (excluding user ID: {}), from: {}, size: {}", userId, from, size);

        userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("User not found with ID: {}", userId);
                    return new NotFoundException("User not found");
                });

        if (from < 0 || size <= 0) {
            log.warn("Invalid pagination parameters: from={}, size={}", from, size);
            throw new ValidationException("Invalid pagination parameters: from=" + from + ", size=" + size);
        }

        Pageable pageable = PageRequest.of(from / size, size, Sort.by(Sort.Direction.DESC, "created"));

        List<ItemRequest> requests = itemRequestRepository.findByRequestorIdNot(userId, pageable).getContent();

        return requests.stream()
                .map(this::convertToDtoWithItems)
                .collect(Collectors.toList());
    }

    @Override
    public ItemRequestDto getItemRequestById(Long requestId, Long userId) {
        log.info("Getting item request by ID: {} for user ID: {}", requestId, userId);

        userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("User not found with ID: {}", userId);
                    return new NotFoundException("User not found");
                });

        ItemRequest itemRequest = itemRequestRepository.findById(requestId)
                .orElseThrow(() -> {
                    log.warn("Item request not found with ID: {}", requestId);
                    return new NotFoundException("Item request not found");
                });

        return convertToDtoWithItems(itemRequest);
    }

    private ItemRequestDto convertToDtoWithItems(ItemRequest itemRequest) {
        // Сначала мапим базовый DTO
        ItemRequestDto dto = itemRequestMapper.toDto(itemRequest);

        // Затем добавляем items
        List<ItemDto> items = itemRepository.findByRequestId(itemRequest.getId()).stream()
                .map(itemMapper::toDto)
                .collect(Collectors.toList());
        dto.setItems(items);

        return dto;
    }
}

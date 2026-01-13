package ru.practicum.shareit.server.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.dto.ItemDto;
import ru.practicum.shareit.dto.ItemRequestDto;
import ru.practicum.shareit.server.exception.NotFoundException;
import ru.practicum.shareit.server.exception.ValidationException;
import ru.practicum.shareit.server.mapper.ItemMapper;
import ru.practicum.shareit.server.mapper.ItemRequestMapper;
import ru.practicum.shareit.server.model.Item;
import ru.practicum.shareit.server.model.ItemRequest;
import ru.practicum.shareit.server.model.User;
import ru.practicum.shareit.server.repository.ItemRepository;
import ru.practicum.shareit.server.repository.ItemRequestRepository;
import ru.practicum.shareit.server.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemRequestServiceImpl implements ItemRequestService {
    private final ItemRequestRepository itemRequestRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final ItemRequestMapper itemRequestMapper;
    private final ItemMapper itemMapper;

    @Override
    @Transactional
    public ItemRequestDto createItemRequest(ItemRequestDto itemRequestDto, Long userId) {
        log.info("Creating item request by user ID: {}", userId);

        // Проверка на null DTO
        if (itemRequestDto == null) {
            throw new ValidationException("Item request cannot be null");
        }

        // Базовая проверка описания
        if (itemRequestDto.getDescription() == null || itemRequestDto.getDescription().isBlank()) {
            throw new ValidationException("Item request description cannot be empty");
        }

        User requestor = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        ItemRequest itemRequest = itemRequestMapper.toEntity(itemRequestDto);
        itemRequest.setRequestor(requestor);
        itemRequest.setCreated(LocalDateTime.now());

        ItemRequest savedRequest = itemRequestRepository.save(itemRequest);
        log.info("Item request created successfully with ID: {}", savedRequest.getId());

        ItemRequestDto result = itemRequestMapper.toDto(savedRequest);
        result.setItems(Collections.emptyList());
        return result;
    }

    @Override
    public List<ItemRequestDto> getUserItemRequests(Long userId) {
        log.info("Getting item requests for user ID: {}", userId);

        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        // Убедимся что сортировка работает правильно
        List<ItemRequest> requests = itemRequestRepository.findByRequestorIdOrderByCreatedDesc(userId);

        if (requests.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> requestIds = requests.stream()
                .map(ItemRequest::getId)
                .collect(Collectors.toList());

        Map<Long, List<Item>> itemsByRequestId = itemRepository.findByRequestIdIn(requestIds).stream()
                .collect(Collectors.groupingBy(Item::getRequestId));

        return requests.stream().map(request -> {
            ItemRequestDto dto = itemRequestMapper.toDto(request);

            List<ItemDto> itemDtos = itemsByRequestId.getOrDefault(request.getId(), Collections.emptyList())
                    .stream()
                    .map(itemMapper::toDto)
                    .collect(Collectors.toList());
            dto.setItems(itemDtos);

            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    public List<ItemRequestDto> getAllItemRequests(Long userId, int from, int size) {
        log.info("Getting all item requests (excluding user ID: {}), from: {}, size: {}", userId, from, size);

        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        // Параметры уже валидированы в Gateway, но проверяем базовую логику
        if (from < 0) {
            throw new ValidationException("Parameter 'from' must not be negative");
        }
        if (size <= 0) {
            throw new ValidationException("Parameter 'size' must be positive");
        }
        if (size > 100) {
            throw new ValidationException("Parameter 'size' must not exceed 100");
        }

        int pageNumber = from / size;
        Pageable pageable = PageRequest.of(pageNumber, size, Sort.by(Sort.Direction.DESC, "created"));

        Page<ItemRequest> requestsPage = itemRequestRepository.findByRequestorIdNot(userId, pageable);

        if (requestsPage.isEmpty()) {
            return Collections.emptyList();
        }

        List<ItemRequest> requests = requestsPage.getContent();

        List<Long> requestIds = requests.stream()
                .map(ItemRequest::getId)
                .collect(Collectors.toList());

        Map<Long, List<Item>> itemsByRequestId = itemRepository.findByRequestIdIn(requestIds).stream()
                .collect(Collectors.groupingBy(Item::getRequestId));

        return requests.stream().map(request -> {
            ItemRequestDto dto = itemRequestMapper.toDto(request);

            List<ItemDto> itemDtos = itemsByRequestId.getOrDefault(request.getId(), Collections.emptyList())
                    .stream()
                    .map(itemMapper::toDto)
                    .collect(Collectors.toList());
            dto.setItems(itemDtos);

            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    public ItemRequestDto getItemRequestById(Long requestId, Long userId) {
        log.info("Getting item request by ID: {} for user ID: {}", requestId, userId);

        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        ItemRequest itemRequest = itemRequestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Item request not found"));

        ItemRequestDto dto = itemRequestMapper.toDto(itemRequest);

        List<ItemDto> items = itemRepository.findByRequestId(requestId).stream()
                .map(itemMapper::toDto)
                .collect(Collectors.toList());
        dto.setItems(items);

        return dto;
    }
}

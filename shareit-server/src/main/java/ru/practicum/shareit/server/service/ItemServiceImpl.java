package ru.practicum.shareit.server.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.dto.*;
import ru.practicum.shareit.server.exception.NotFoundException;
import ru.practicum.shareit.server.exception.ValidationException;
import ru.practicum.shareit.server.mapper.CommentMapper;
import ru.practicum.shareit.server.mapper.ItemMapper;
import ru.practicum.shareit.server.model.*;
import ru.practicum.shareit.server.repository.BookingRepository;
import ru.practicum.shareit.server.repository.CommentRepository;
import ru.practicum.shareit.server.repository.ItemRepository;
import ru.practicum.shareit.server.repository.ItemRequestRepository;
import ru.practicum.shareit.server.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;
    private final ItemRequestRepository itemRequestRepository;
    private final ItemMapper itemMapper;
    private final CommentMapper commentMapper;

    @Override
    @Transactional
    public ItemResponseDto createItem(ItemDto itemDto, Long ownerId) {
        log.info("Creating item for owner ID: {}", ownerId);

        // Базовые проверки (основная валидация в Gateway)
        if (itemDto.getName() == null || itemDto.getName().isBlank()) {
            throw new ValidationException("Item name cannot be empty");
        }
        if (itemDto.getDescription() == null || itemDto.getDescription().isBlank()) {
            throw new ValidationException("Item description cannot be empty");
        }
        if (itemDto.getAvailable() == null) {
            throw new ValidationException("Available status must be specified");
        }

        // Проверяем существование requestId если он указан
        if (itemDto.getRequestId() != null) {
            itemRequestRepository.findById(itemDto.getRequestId())
                    .orElseThrow(() -> new NotFoundException("Item request not found with ID: " + itemDto.getRequestId()));
        }

        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        Item item = itemMapper.toEntity(itemDto);
        item.setOwner(owner);

        Item savedItem = itemRepository.save(item);
        log.info("Item created successfully with ID: {} for owner ID: {}", savedItem.getId(), ownerId);

        return getItemById(savedItem.getId(), ownerId);
    }

    @Override
    @Transactional
    public ItemResponseDto updateItem(Long itemId, ItemDto itemDto, Long ownerId) {
        log.info("Updating item ID: {} by owner ID: {}", itemId, ownerId);

        Item existingItem = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Item not found"));

        if (!existingItem.getOwner().getId().equals(ownerId)) {
            throw new NotFoundException("Only owner can update item");
        }

        if (itemDto.getName() != null && !itemDto.getName().isBlank()) {
            existingItem.setName(itemDto.getName());
        }
        if (itemDto.getDescription() != null && !itemDto.getDescription().isBlank()) {
            existingItem.setDescription(itemDto.getDescription());
        }
        if (itemDto.getAvailable() != null) {
            existingItem.setAvailable(itemDto.getAvailable());
        }
        if (itemDto.getRequestId() != null) {
            // Проверяем существование requestId если он указан
            itemRequestRepository.findById(itemDto.getRequestId())
                    .orElseThrow(() -> new NotFoundException("Item request not found with ID: " + itemDto.getRequestId()));
            existingItem.setRequestId(itemDto.getRequestId());
        }

        Item updatedItem = itemRepository.save(existingItem);
        log.info("Item ID: {} updated successfully", itemId);

        return getItemById(itemId, ownerId);
    }

    @Override
    public ItemResponseDto getItemById(Long itemId, Long userId) {
        log.info("Getting item by ID: {} for user ID: {}", itemId, userId);

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Item not found"));

        ItemResponseDto itemDto = itemMapper.toResponseDto(item);
        LocalDateTime now = LocalDateTime.now();

        // Добавляем бронирования если пользователь - владелец
        if (userId != null && item.getOwner().getId().equals(userId)) {
            addLastAndNextBookings(Collections.singletonList(itemDto), Collections.singletonList(item.getId()), now);
        } else {
            itemDto.setLastBooking(null);
            itemDto.setNextBooking(null);
        }

        // Добавляем комментарии
        addComments(Collections.singletonList(itemDto), Collections.singletonList(item.getId()));

        return itemDto;
    }

    @Override
    public List<ItemOwnerDto> getAllItemsByOwner(Long ownerId) {
        log.info("Getting all items for owner ID: {}", ownerId);

        userRepository.findById(ownerId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        List<Item> items = itemRepository.findByOwnerIdOrderById(ownerId);

        if (items.isEmpty()) {
            return Collections.emptyList();
        }

        List<ItemOwnerDto> itemDtos = items.stream()
                .map(itemMapper::toOwnerDto)
                .collect(Collectors.toList());

        LocalDateTime now = LocalDateTime.now();
        List<Long> itemIds = items.stream()
                .map(Item::getId)
                .collect(Collectors.toList());

        // Добавляем бронирования
        addLastAndNextBookingsForOwner(itemDtos, itemIds, now, ownerId);

        // Добавляем комментарии
        addCommentsForOwner(itemDtos, itemIds);

        return itemDtos;
    }

    @Override
    public List<ItemDto> searchItems(String text, Long userId) {
        log.info("Searching items with text: '{}' for user ID: {}", text, userId);

        if (text == null || text.isBlank()) {
            return Collections.emptyList();
        }

        return itemRepository.searchAvailableItems(text)
                .stream()
                .map(itemMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CommentDto addComment(Long itemId, CommentDto commentDto, Long userId) {
        log.info("Adding comment to item ID: {} by user ID: {}", itemId, userId);

        // Базовые проверки (основная валидация в Gateway)
        if (commentDto.getText() == null || commentDto.getText().isBlank()) {
            throw new ValidationException("Comment text cannot be empty");
        }

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Item not found"));

        User author = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        LocalDateTime now = LocalDateTime.now();
        List<Booking> pastBookings = bookingRepository.findByItemIdAndBookerIdAndEndBeforeAndStatus(
                itemId, userId, now, BookingStatus.APPROVED);

        if (pastBookings.isEmpty()) {
            throw new ValidationException("User can only comment on items they have booked in the past");
        }

        Comment comment = commentMapper.toEntity(commentDto);
        comment.setItem(item);
        comment.setAuthor(author);
        comment.setCreated(now);

        Comment savedComment = commentRepository.save(comment);
        log.info("Comment added successfully with ID: {} to item ID: {} by user ID: {}",
                savedComment.getId(), itemId, userId);

        return commentMapper.toDto(savedComment);
    }

    // Вспомогательные методы для избежания N+1

    private void addLastAndNextBookings(List<? extends ItemResponseDto> itemDtos, List<Long> itemIds, LocalDateTime now) {
        if (itemIds.isEmpty()) {
            return;
        }

        // Получаем последние бронирования для всех items
        Map<Long, Booking> lastBookings = bookingRepository
                .findAllByItemIdInAndStatusAndStartBeforeOrderByStartDesc(itemIds, BookingStatus.APPROVED, now)
                .stream()
                .collect(Collectors.groupingBy(
                        booking -> booking.getItem().getId(),
                        Collectors.collectingAndThen(
                                Collectors.maxBy(Comparator.comparing(Booking::getStart)),
                                Optional::get
                        )
                ));

        // Получаем следующие бронирования для всех items
        Map<Long, Booking> nextBookings = bookingRepository
                .findAllByItemIdInAndStatusAndStartAfterOrderByStartAsc(itemIds, BookingStatus.APPROVED, now)
                .stream()
                .collect(Collectors.groupingBy(
                        booking -> booking.getItem().getId(),
                        Collectors.collectingAndThen(
                                Collectors.minBy(Comparator.comparing(Booking::getStart)),
                                Optional::get
                        )
                ));

        // Заполняем DTO
        for (ItemResponseDto dto : itemDtos) {
            Booking lastBooking = lastBookings.get(dto.getId());
            if (lastBooking != null) {
                dto.setLastBooking(createBookingInfoDto(lastBooking));
            }

            Booking nextBooking = nextBookings.get(dto.getId());
            if (nextBooking != null) {
                dto.setNextBooking(createBookingInfoDto(nextBooking));
            }
        }
    }

    private void addLastAndNextBookingsForOwner(List<ItemOwnerDto> itemDtos, List<Long> itemIds, LocalDateTime now, Long ownerId) {
        if (itemIds.isEmpty()) {
            return;
        }

        // Для владельца нужно только его items
        Map<Long, Booking> lastBookings = bookingRepository
                .findAllByItemIdInAndStatusAndStartBeforeOrderByStartDesc(itemIds, BookingStatus.APPROVED, now)
                .stream()
                .filter(booking -> booking.getItem().getOwner().getId().equals(ownerId))
                .collect(Collectors.groupingBy(
                        booking -> booking.getItem().getId(),
                        Collectors.collectingAndThen(
                                Collectors.maxBy(Comparator.comparing(Booking::getStart)),
                                Optional::get
                        )
                ));

        Map<Long, Booking> nextBookings = bookingRepository
                .findAllByItemIdInAndStatusAndStartAfterOrderByStartAsc(itemIds, BookingStatus.APPROVED, now)
                .stream()
                .filter(booking -> booking.getItem().getOwner().getId().equals(ownerId))
                .collect(Collectors.groupingBy(
                        booking -> booking.getItem().getId(),
                        Collectors.collectingAndThen(
                                Collectors.minBy(Comparator.comparing(Booking::getStart)),
                                Optional::get
                        )
                ));

        // Заполняем DTO
        for (ItemOwnerDto dto : itemDtos) {
            Booking lastBooking = lastBookings.get(dto.getId());
            if (lastBooking != null) {
                dto.setLastBooking(createBookingInfoDto(lastBooking));
            }

            Booking nextBooking = nextBookings.get(dto.getId());
            if (nextBooking != null) {
                dto.setNextBooking(createBookingInfoDto(nextBooking));
            }
        }
    }

    private void addComments(List<? extends ItemResponseDto> itemDtos, List<Long> itemIds) {
        if (itemIds.isEmpty()) {
            return;
        }

        Map<Long, List<Comment>> commentsByItemId = commentRepository.findAllByItemIdInOrderByCreatedDesc(itemIds)
                .stream()
                .collect(Collectors.groupingBy(comment -> comment.getItem().getId()));

        for (ItemResponseDto dto : itemDtos) {
            List<Comment> comments = commentsByItemId.get(dto.getId());
            if (comments != null && !comments.isEmpty()) {
                List<CommentDto> commentDtos = comments.stream()
                        .map(commentMapper::toDto)
                        .collect(Collectors.toList());
                dto.setComments(commentDtos);
            } else {
                dto.setComments(Collections.emptyList());
            }
        }
    }

    private void addCommentsForOwner(List<ItemOwnerDto> itemDtos, List<Long> itemIds) {
        if (itemIds.isEmpty()) {
            return;
        }

        Map<Long, List<Comment>> commentsByItemId = commentRepository.findAllByItemIdInOrderByCreatedDesc(itemIds)
                .stream()
                .collect(Collectors.groupingBy(comment -> comment.getItem().getId()));

        for (ItemOwnerDto dto : itemDtos) {
            List<Comment> comments = commentsByItemId.get(dto.getId());
            if (comments != null && !comments.isEmpty()) {
                List<CommentDto> commentDtos = comments.stream()
                        .map(commentMapper::toDto)
                        .collect(Collectors.toList());
                dto.setComments(commentDtos);
            } else {
                dto.setComments(Collections.emptyList());
            }
        }
    }

    private BookingInfoDto createBookingInfoDto(Booking booking) {
        return BookingInfoDto.builder()
                .id(booking.getId())
                .bookerId(booking.getBooker().getId())
                .start(booking.getStart())
                .end(booking.getEnd())
                .build();
    }
}

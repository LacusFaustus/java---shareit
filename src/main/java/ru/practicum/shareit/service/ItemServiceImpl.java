package ru.practicum.shareit.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.model.*;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.dto.*;
import ru.practicum.shareit.mapper.CommentMapper;
import ru.practicum.shareit.mapper.ItemMapper;
import ru.practicum.shareit.repository.CommentRepository;
import ru.practicum.shareit.repository.ItemRepository;
import ru.practicum.shareit.repository.BookingRepository;
import ru.practicum.shareit.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
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
    private final ItemMapper itemMapper;
    private final CommentMapper commentMapper;

    @Override
    @Transactional
    public ItemResponseDto createItem(ItemDto itemDto, Long ownerId) {
        log.info("Creating item for owner ID: {}", ownerId);

        validateItem(itemDto);

        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> {
                    log.warn("User not found with ID: {}", ownerId);
                    return new NotFoundException("User not found");
                });

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
                .orElseThrow(() -> {
                    log.warn("Item not found with ID: {}", itemId);
                    return new NotFoundException("Item not found");
                });

        if (!existingItem.getOwner().getId().equals(ownerId)) {
            log.warn("User ID: {} is not owner of item ID: {}", ownerId, itemId);
            throw new NotFoundException("Only owner can update item");
        }

        if (itemDto.getName() != null && !itemDto.getName().isBlank()) {
            existingItem.setName(itemDto.getName());
            log.debug("Updating item ID: {} name", itemId);
        }
        if (itemDto.getDescription() != null && !itemDto.getDescription().isBlank()) {
            existingItem.setDescription(itemDto.getDescription());
            log.debug("Updating item ID: {} description", itemId);
        }
        if (itemDto.getAvailable() != null) {
            existingItem.setAvailable(itemDto.getAvailable());
            log.debug("Updating item ID: {} availability to: {}", itemId, itemDto.getAvailable());
        }

        Item updatedItem = itemRepository.save(existingItem);
        log.info("Item ID: {} updated successfully", itemId);

        return getItemById(itemId, ownerId);
    }

    @Override
    public ItemResponseDto getItemById(Long itemId, Long userId) {
        log.info("Getting item by ID: {} for user ID: {}", itemId, userId);

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> {
                    log.warn("Item not found with ID: {}", itemId);
                    return new NotFoundException("Item not found");
                });

        ItemResponseDto itemDto = itemMapper.toResponseDto(item);
        LocalDateTime now = LocalDateTime.now();

        // Добавляем информацию о бронированиях если пользователь - владелец
        if (userId != null && item.getOwner().getId().equals(userId)) {
            // Находим последнее завершенное бронирование
            bookingRepository.findFirstByItemIdAndStatusAndStartBeforeOrderByStartDesc(
                    itemId, BookingStatus.APPROVED, now
            ).ifPresent(booking -> itemDto.setLastBooking(
                    BookingInfoDto.builder()
                            .id(booking.getId())
                            .bookerId(booking.getBooker().getId())
                            .start(booking.getStart())
                            .end(booking.getEnd())
                            .build()
            ));

            // Находим ближайшее будущее бронирование
            bookingRepository.findFirstByItemIdAndStatusAndStartAfterOrderByStartAsc(
                    itemId, BookingStatus.APPROVED, now
            ).ifPresent(booking -> itemDto.setNextBooking(
                    BookingInfoDto.builder()
                            .id(booking.getId())
                            .bookerId(booking.getBooker().getId())
                            .start(booking.getStart())
                            .end(booking.getEnd())
                            .build()
            ));
        } else {
            // Для не-владельца не показываем информацию о бронированиях
            itemDto.setLastBooking(null);
            itemDto.setNextBooking(null);
        }

        // Добавляем комментарии (видны всем)
        List<Comment> comments = commentRepository.findByItemIdOrderByCreatedDesc(itemId);
        List<CommentDto> commentDtos = comments.stream()
                .map(commentMapper::toDto)
                .collect(Collectors.toList());
        itemDto.setComments(commentDtos);

        return itemDto;
    }

    @Override
    public List<ItemOwnerDto> getAllItemsByOwner(Long ownerId) {
        log.info("Getting all items for owner ID: {}", ownerId);

        userRepository.findById(ownerId)
                .orElseThrow(() -> {
                    log.warn("User not found with ID: {}", ownerId);
                    return new NotFoundException("User not found");
                });

        List<Item> items = itemRepository.findByOwnerIdOrderById(ownerId);
        LocalDateTime now = LocalDateTime.now();

        return items.stream().map(item -> {
            ItemOwnerDto itemDto = itemMapper.toOwnerDto(item);

            // Находим последнее завершенное бронирование
            bookingRepository.findFirstByItemIdAndStatusAndStartBeforeOrderByStartDesc(
                    item.getId(), BookingStatus.APPROVED, now
            ).ifPresent(booking -> itemDto.setLastBooking(
                    BookingInfoDto.builder()
                            .id(booking.getId())
                            .bookerId(booking.getBooker().getId())
                            .start(booking.getStart())
                            .end(booking.getEnd())
                            .build()
            ));

            // Находим ближайшее будущее бронирование
            bookingRepository.findFirstByItemIdAndStatusAndStartAfterOrderByStartAsc(
                    item.getId(), BookingStatus.APPROVED, now
            ).ifPresent(booking -> itemDto.setNextBooking(
                    BookingInfoDto.builder()
                            .id(booking.getId())
                            .bookerId(booking.getBooker().getId())
                            .start(booking.getStart())
                            .end(booking.getEnd())
                            .build()
            ));

            // Добавляем комментарии
            List<Comment> comments = commentRepository.findByItemIdOrderByCreatedDesc(item.getId());
            List<CommentDto> commentDtos = comments.stream()
                    .map(commentMapper::toDto)
                    .collect(Collectors.toList());
            itemDto.setComments(commentDtos);

            return itemDto;
        }).collect(Collectors.toList());
    }

    @Override
    public List<ItemDto> searchItems(String text, Long userId) {
        log.info("Searching items with text: '{}' for user ID: {}", text, userId);

        if (text == null || text.isBlank()) {
            log.info("Search text is empty, returning empty list");
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

        if (commentDto.getText() == null || commentDto.getText().isBlank()) {
            log.warn("Attempt to add empty comment to item ID: {} by user ID: {}", itemId, userId);
            throw new ValidationException("Comment text cannot be empty");
        }

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> {
                    log.warn("Item not found with ID: {}", itemId);
                    return new NotFoundException("Item not found");
                });

        User author = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("User not found with ID: {}", userId);
                    return new NotFoundException("User not found");
                });

        // Проверка что пользователь брал вещь в аренду и аренда завершена
        LocalDateTime now = LocalDateTime.now();
        List<Booking> pastBookings = bookingRepository.findByItemIdAndBookerIdAndEndBeforeAndStatus(
                itemId, userId, now, BookingStatus.APPROVED);

        if (pastBookings.isEmpty()) {
            log.warn("User ID: {} cannot comment on item ID: {} - no past bookings found", userId, itemId);
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

    private void validateItem(ItemDto itemDto) {
        if (itemDto.getName() == null || itemDto.getName().isBlank()) {
            log.warn("Item name cannot be empty");
            throw new ValidationException("Item name cannot be empty");
        }
        if (itemDto.getDescription() == null || itemDto.getDescription().isBlank()) {
            log.warn("Item description cannot be empty");
            throw new ValidationException("Item description cannot be empty");
        }
        if (itemDto.getAvailable() == null) {
            log.warn("Available status must be specified");
            throw new ValidationException("Available status must be specified");
        }
    }
}

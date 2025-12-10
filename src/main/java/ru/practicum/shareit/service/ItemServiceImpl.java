package ru.practicum.shareit.service;

import lombok.RequiredArgsConstructor;
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
        validateItem(itemDto);

        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        Item item = itemMapper.toEntity(itemDto);
        item.setOwner(owner);

        Item savedItem = itemRepository.save(item);
        return getItemById(savedItem.getId(), ownerId);
    }

    @Override
    @Transactional
    public ItemResponseDto updateItem(Long itemId, ItemDto itemDto, Long ownerId) {
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

        Item updatedItem = itemRepository.save(existingItem);
        return getItemById(itemId, ownerId);
    }

    @Override
    public ItemResponseDto getItemById(Long itemId, Long userId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Item not found"));

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
        userRepository.findById(ownerId)
                .orElseThrow(() -> new NotFoundException("User not found"));

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
        if (commentDto.getText() == null || commentDto.getText().isBlank()) {
            throw new ValidationException("Comment text cannot be empty");
        }

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Item not found"));

        User author = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        // Проверка что пользователь брал вещь в аренду и аренда завершена
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
        return commentMapper.toDto(savedComment);
    }

    private void validateItem(ItemDto itemDto) {
        if (itemDto.getName() == null || itemDto.getName().isBlank()) {
            throw new ValidationException("Item name cannot be empty");
        }
        if (itemDto.getDescription() == null || itemDto.getDescription().isBlank()) {
            throw new ValidationException("Item description cannot be empty");
        }
        if (itemDto.getAvailable() == null) {
            throw new ValidationException("Available status must be specified");
        }
    }
}

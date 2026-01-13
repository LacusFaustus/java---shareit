package ru.practicum.shareit.server.util;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.server.model.*;
import ru.practicum.shareit.server.repository.*;

import java.time.LocalDateTime;

@Component
@Transactional
public class TestDataUtil {

    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final BookingRepository bookingRepository;
    private final ItemRequestRepository itemRequestRepository;
    private final CommentRepository commentRepository;

    public TestDataUtil(UserRepository userRepository, ItemRepository itemRepository,
                        BookingRepository bookingRepository, ItemRequestRepository itemRequestRepository,
                        CommentRepository commentRepository) {
        this.userRepository = userRepository;
        this.itemRepository = itemRepository;
        this.bookingRepository = bookingRepository;
        this.itemRequestRepository = itemRequestRepository;
        this.commentRepository = commentRepository;
    }

    public User createUser(String name, String email) {
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        return userRepository.save(user);
    }

    public Item createItem(String name, String description, User owner, Long requestId) {
        Item item = new Item();
        item.setName(name);
        item.setDescription(description);
        item.setAvailable(true);
        item.setOwner(owner);
        item.setRequestId(requestId);
        return itemRepository.save(item);
    }

    public Booking createBooking(Item item, User booker, LocalDateTime start, LocalDateTime end, BookingStatus status) {
        Booking booking = new Booking();
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStart(start);
        booking.setEnd(end);
        booking.setStatus(status);
        return bookingRepository.save(booking);
    }

    public ItemRequest createItemRequest(String description, User requestor) {
        ItemRequest request = new ItemRequest();
        request.setDescription(description);
        request.setRequestor(requestor);
        request.setCreated(LocalDateTime.now());
        return itemRequestRepository.save(request);
    }

    public Comment createComment(String text, Item item, User author) {
        Comment comment = new Comment();
        comment.setText(text);
        comment.setItem(item);
        comment.setAuthor(author);
        comment.setCreated(LocalDateTime.now());
        return commentRepository.save(comment);
    }
}

package ru.practicum.shareit.dto;

import java.time.LocalDateTime;

public class TestUtils {

    private TestUtils() {
    }

    public static BookingRequestDto createValidBookingRequest() {
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = start.plusDays(2);

        return BookingRequestDto.builder()
                .itemId(1L)
                .start(start)
                .end(end)
                .build();
    }

    public static ItemDto createValidItemDto() {
        return ItemDto.builder()
                .name("Test Item")
                .description("Test Description")
                .available(true)
                .build();
    }

    public static ItemSimpleDto createValidItemSimpleDto() {
        return ItemSimpleDto.builder()
                .name("Test Item")
                .description("Test Description")
                .available(true)
                .build();
    }

    public static UserDto createValidUserDto() {
        return UserDto.builder()
                .name("Test User")
                .email("test@example.com")
                .build();
    }

    public static CommentDto createValidCommentDto() {
        return CommentDto.builder()
                .text("Test comment")
                .build();
    }

    public static BookingInfoDto createValidBookingInfoDto() {
        LocalDateTime start = LocalDateTime.now().minusDays(2);
        LocalDateTime end = start.plusDays(1);

        return BookingInfoDto.builder()
                .id(1L)
                .bookerId(10L)
                .start(start)
                .end(end)
                .build();
    }
}

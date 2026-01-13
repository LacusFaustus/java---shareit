package ru.practicum.shareit.server.model;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class BookingStateTest {

    @Test
    void bookingStateValues() {
        BookingState[] states = BookingState.values();
        assertThat(states).containsExactlyInAnyOrder(
                BookingState.ALL,
                BookingState.CURRENT,
                BookingState.PAST,
                BookingState.FUTURE,
                BookingState.WAITING,
                BookingState.APPROVED,  // Добавляем APPROVED
                BookingState.REJECTED,
                BookingState.CANCELED
        );
    }

    @Test
    void bookingStateValueOf() {
        assertThat(BookingState.valueOf("ALL")).isEqualTo(BookingState.ALL);
        assertThat(BookingState.valueOf("CURRENT")).isEqualTo(BookingState.CURRENT);
        assertThat(BookingState.valueOf("PAST")).isEqualTo(BookingState.PAST);
        assertThat(BookingState.valueOf("FUTURE")).isEqualTo(BookingState.FUTURE);
        assertThat(BookingState.valueOf("WAITING")).isEqualTo(BookingState.WAITING);
        assertThat(BookingState.valueOf("APPROVED")).isEqualTo(BookingState.APPROVED); // Добавляем
        assertThat(BookingState.valueOf("REJECTED")).isEqualTo(BookingState.REJECTED);
        assertThat(BookingState.valueOf("CANCELED")).isEqualTo(BookingState.CANCELED);
    }
}

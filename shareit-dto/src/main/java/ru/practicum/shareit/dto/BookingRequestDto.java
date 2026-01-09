package ru.practicum.shareit.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingRequestDto {
    @NotNull(message = "Item ID cannot be null")
    private Long itemId;

    @NotNull(message = "Start date cannot be null")
    @FutureOrPresent(message = "Start date must be in present or future")
    private LocalDateTime start;

    @NotNull(message = "End date cannot be null")
    @Future(message = "End date must be in future")
    private LocalDateTime end;

    @JsonIgnore
    public boolean isValid() {
        return end != null && start != null && end.isAfter(start);
    }
}

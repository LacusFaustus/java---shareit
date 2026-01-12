package ru.practicum.shareit.gateway.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.dto.BookingRequestDto;
import ru.practicum.shareit.gateway.client.BookingClient;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookingController.class)
class BookingControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @org.springframework.boot.test.mock.mockito.MockBean
    private BookingClient bookingClient;

    @Test
    void createBooking_WithValidData_ReturnsOk() {
        BookingRequestDto requestDto = new BookingRequestDto(
                1L,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2)
        );

        when(bookingClient.createBooking(any(BookingRequestDto.class), eq(1L)))
                .thenReturn(ResponseEntity.ok("Booking created"));

        try {
            mockMvc.perform(post("/bookings")
                            .header("X-Sharer-User-Id", 1L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDto)))
                    .andExpect(status().isOk());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void createBooking_WithoutUserIdHeader_ReturnsBadRequest() {
        BookingRequestDto requestDto = new BookingRequestDto(
                1L,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2)
        );

        try {
            mockMvc.perform(post("/bookings")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDto)))
                    .andExpect(status().isBadRequest());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void updateBookingStatus_WithValidData_ReturnsOk() {
        when(bookingClient.updateBookingStatus(eq(1L), eq(true), eq(1L)))
                .thenReturn(ResponseEntity.ok("Booking updated"));

        try {
            mockMvc.perform(patch("/bookings/1")
                            .header("X-Sharer-User-Id", 1L)
                            .param("approved", "true"))
                    .andExpect(status().isOk());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void getBookingById_ReturnsOk() {
        when(bookingClient.getBookingById(eq(1L), eq(1L)))
                .thenReturn(ResponseEntity.ok("Booking details"));

        try {
            mockMvc.perform(get("/bookings/1")
                            .header("X-Sharer-User-Id", 1L))
                    .andExpect(status().isOk());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void getUserBookings_WithDefaultParams_ReturnsOk() {
        when(bookingClient.getUserBookings(eq(1L), eq("ALL"), eq(0), eq(10)))
                .thenReturn(ResponseEntity.ok("User bookings"));

        try {
            mockMvc.perform(get("/bookings")
                            .header("X-Sharer-User-Id", 1L))
                    .andExpect(status().isOk());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void getUserBookings_WithCustomParams_ReturnsOk() {
        when(bookingClient.getUserBookings(eq(1L), eq("FUTURE"), eq(5), eq(20)))
                .thenReturn(ResponseEntity.ok("User bookings"));

        try {
            mockMvc.perform(get("/bookings")
                            .header("X-Sharer-User-Id", 1L)
                            .param("state", "FUTURE")
                            .param("from", "5")
                            .param("size", "20"))
                    .andExpect(status().isOk());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void getOwnerBookings_ReturnsOk() {
        when(bookingClient.getOwnerBookings(eq(1L), eq("ALL"), eq(0), eq(10)))
                .thenReturn(ResponseEntity.ok("Owner bookings"));

        try {
            mockMvc.perform(get("/bookings/owner")
                            .header("X-Sharer-User-Id", 1L))
                    .andExpect(status().isOk());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void createBooking_WithInvalidDates_ReturnsBadRequest() {
        // End date before start date
        BookingRequestDto requestDto = new BookingRequestDto(
                1L,
                LocalDateTime.now().plusDays(2),
                LocalDateTime.now().plusDays(1)
        );

        try {
            mockMvc.perform(post("/bookings")
                            .header("X-Sharer-User-Id", 1L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDto)))
                    .andExpect(status().isBadRequest());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void createBooking_WithPastStartDate_ReturnsBadRequest() {
        BookingRequestDto requestDto = new BookingRequestDto(
                1L,
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().plusDays(1)
        );

        try {
            mockMvc.perform(post("/bookings")
                            .header("X-Sharer-User-Id", 1L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDto)))
                    .andExpect(status().isBadRequest());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void createBooking_WithoutItemId_ReturnsBadRequest() {
        String invalidJson = """
            {
                "start": "2024-01-20T10:00:00",
                "end": "2024-01-22T10:00:00"
            }
            """;

        try {
            mockMvc.perform(post("/bookings")
                            .header("X-Sharer-User-Id", 1L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(invalidJson))
                    .andExpect(status().isBadRequest());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // Тесты из BookingControllerAdditionalTest
    @Test
    void updateBookingStatus_WithoutApprovedParam_ReturnsBadRequest() {
        try {
            mockMvc.perform(patch("/bookings/1")
                            .header("X-Sharer-User-Id", 1L))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("Missing required parameter: approved"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void getUserBookings_WithInvalidState_ReturnsOk() {
        when(bookingClient.getUserBookings(eq(1L), eq("INVALID_STATE"), eq(0), eq(10)))
                .thenReturn(ResponseEntity.ok("User bookings"));

        try {
            mockMvc.perform(get("/bookings")
                            .header("X-Sharer-User-Id", 1L)
                            .param("state", "INVALID_STATE"))
                    .andExpect(status().isOk());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void getUserBookings_WithNegativeFrom_ReturnsBadRequest() {
        try {
            mockMvc.perform(get("/bookings")
                            .header("X-Sharer-User-Id", 1L)
                            .param("from", "-1"))
                    .andExpect(status().isBadRequest());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void getUserBookings_WithZeroSize_ReturnsBadRequest() {
        try {
            mockMvc.perform(get("/bookings")
                            .header("X-Sharer-User-Id", 1L)
                            .param("size", "0"))
                    .andExpect(status().isBadRequest());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void getOwnerBookings_WithInvalidParams_ReturnsBadRequest() {
        try {
            mockMvc.perform(get("/bookings/owner")
                            .header("X-Sharer-User-Id", 1L)
                            .param("from", "-5")
                            .param("size", "0"))
                    .andExpect(status().isBadRequest());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void createBooking_WithNullDates_ReturnsBadRequest() {
        String invalidJson = """
            {
                "itemId": 1
            }
            """;

        try {
            mockMvc.perform(post("/bookings")
                            .header("X-Sharer-User-Id", 1L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(invalidJson))
                    .andExpect(status().isBadRequest());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

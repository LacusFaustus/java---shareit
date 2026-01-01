package ru.practicum.shareit.gateway.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
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
class BookingControllerGatewayTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BookingClient bookingClient;

    @Test
    void createBooking_WithValidData_ReturnsOk() throws Exception {
        BookingRequestDto requestDto = new BookingRequestDto(
                1L,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2)
        );

        when(bookingClient.createBooking(any(BookingRequestDto.class), eq(1L)))
                .thenReturn(ResponseEntity.ok("Booking created"));

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk());
    }

    @Test
    void createBooking_WithoutUserIdHeader_ReturnsBadRequest() throws Exception {
        BookingRequestDto requestDto = new BookingRequestDto(
                1L,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2)
        );

        mockMvc.perform(post("/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateBookingStatus_WithValidData_ReturnsOk() throws Exception {
        when(bookingClient.updateBookingStatus(eq(1L), eq(true), eq(1L)))
                .thenReturn(ResponseEntity.ok("Booking updated"));

        mockMvc.perform(patch("/bookings/1")
                        .header("X-Sharer-User-Id", 1L)
                        .param("approved", "true"))
                .andExpect(status().isOk());
    }

    @Test
    void getBookingById_ReturnsOk() throws Exception {
        when(bookingClient.getBookingById(eq(1L), eq(1L)))
                .thenReturn(ResponseEntity.ok("Booking details"));

        mockMvc.perform(get("/bookings/1")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk());
    }

    @Test
    void getUserBookings_WithDefaultParams_ReturnsOk() throws Exception {
        when(bookingClient.getUserBookings(eq(1L), eq("ALL"), eq(0), eq(10)))
                .thenReturn(ResponseEntity.ok("User bookings"));

        mockMvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk());
    }

    @Test
    void getUserBookings_WithCustomParams_ReturnsOk() throws Exception {
        when(bookingClient.getUserBookings(eq(1L), eq("FUTURE"), eq(5), eq(20)))
                .thenReturn(ResponseEntity.ok("User bookings"));

        mockMvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", 1L)
                        .param("state", "FUTURE")
                        .param("from", "5")
                        .param("size", "20"))
                .andExpect(status().isOk());
    }

    @Test
    void getOwnerBookings_ReturnsOk() throws Exception {
        when(bookingClient.getOwnerBookings(eq(1L), eq("ALL"), eq(0), eq(10)))
                .thenReturn(ResponseEntity.ok("Owner bookings"));

        mockMvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk());
    }

    @Test
    void createBooking_WithInvalidDates_ReturnsBadRequest() throws Exception {
        // End date before start date
        BookingRequestDto requestDto = new BookingRequestDto(
                1L,
                LocalDateTime.now().plusDays(2),
                LocalDateTime.now().plusDays(1)
        );

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createBooking_WithPastStartDate_ReturnsBadRequest() throws Exception {
        BookingRequestDto requestDto = new BookingRequestDto(
                1L,
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().plusDays(1)
        );

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createBooking_WithoutItemId_ReturnsBadRequest() throws Exception {
        // Создаем JSON без itemId
        String invalidJson = """
            {
                "start": "2024-01-20T10:00:00",
                "end": "2024-01-22T10:00:00"
            }
            """;

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }
}

package ru.practicum.shareit.gateway.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import ru.practicum.shareit.gateway.controller.BookingController;
import ru.practicum.shareit.gateway.client.BookingClient;
import ru.practicum.shareit.dto.BookingRequestDto;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest({BookingController.class, GatewayErrorHandler.class})
class GatewayErrorHandlerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BookingClient bookingClient;

    @Test
    void handleMissingServletRequestParameterException() throws Exception {
        mockMvc.perform(patch("/bookings/1")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Missing required parameter: approved"));
    }

    @Test
    void handleHttpClientErrorException_WithBadRequest() throws Exception {
        when(bookingClient.createBooking(any(BookingRequestDto.class), anyLong()))
                .thenThrow(HttpClientErrorException.create(
                        HttpStatus.BAD_REQUEST,
                        "Bad Request",
                        null,
                        "Invalid data".getBytes(),
                        null
                ));

        BookingRequestDto requestDto = new BookingRequestDto(
                1L,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2)
        );

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Server validation failed: 400 Bad Request"));
    }

    @Test
    void handleHttpClientErrorException_WithForbidden() throws Exception {
        when(bookingClient.getBookingById(eq(1L), eq(1L)))
                .thenThrow(HttpClientErrorException.create(
                        HttpStatus.FORBIDDEN,
                        "Forbidden",
                        null,
                        "Access denied".getBytes(),
                        null
                ));

        mockMvc.perform(get("/bookings/1")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Server validation failed: 403 Forbidden"));
    }

    @Test
    void handleHttpServerErrorException() throws Exception {
        when(bookingClient.getBookingById(eq(1L), eq(1L)))
                .thenThrow(HttpServerErrorException.create(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "Internal Server Error",
                        null,
                        "Database error".getBytes(),
                        null
                ));

        mockMvc.perform(get("/bookings/1")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Internal server error occurred: 500 Internal Server Error"));
    }

    @Test
    void handleIllegalArgumentException() throws Exception {
        when(bookingClient.createBooking(any(BookingRequestDto.class), anyLong()))
                .thenThrow(new IllegalArgumentException("End date must be after start date"));

        BookingRequestDto requestDto = new BookingRequestDto(
                1L,
                LocalDateTime.now().plusDays(2),
                LocalDateTime.now().plusDays(1)
        );

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("End date must be after start date"));
    }

    @Test
    void handleMissingRequestHeaderException() throws Exception {
        BookingRequestDto requestDto = new BookingRequestDto(
                1L,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2)
        );

        mockMvc.perform(post("/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Missing required header: X-Sharer-User-Id"));
    }

    @Test
    void handleMethodArgumentTypeMismatchException() throws Exception {
        mockMvc.perform(get("/bookings/invalid-id")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invalid parameter value: bookingId should be of type Long"));
    }
}

package ru.practicum.shareit.server.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.dto.BookingRequestDto;
import ru.practicum.shareit.dto.BookingResponseDto;
import ru.practicum.shareit.server.service.BookingService;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookingServerController.class)
class BookingServerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BookingService bookingService;

    @Test
    void createBooking_ShouldReturnCreated() throws Exception {
        BookingRequestDto requestDto = new BookingRequestDto(1L,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2));
        BookingResponseDto responseDto = new BookingResponseDto();

        when(bookingService.createBooking(any(), anyLong())).thenReturn(responseDto);

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated());
    }

    @Test
    void updateBookingStatus_ShouldReturnOk() throws Exception {
        BookingResponseDto responseDto = new BookingResponseDto();
        when(bookingService.updateBookingStatus(anyLong(), anyBoolean(), anyLong()))
                .thenReturn(responseDto);

        mockMvc.perform(patch("/bookings/1?approved=true")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk());
    }

    @Test
    void getBookingById_ShouldReturnOk() throws Exception {
        BookingResponseDto responseDto = new BookingResponseDto();
        when(bookingService.getBookingById(anyLong(), anyLong()))
                .thenReturn(responseDto);

        mockMvc.perform(get("/bookings/1")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk());
    }

    @Test
    void getUserBookings_ShouldReturnOk() throws Exception {
        when(bookingService.getUserBookings(anyLong(), anyString(), anyInt(), anyInt()))
                .thenReturn(List.of());

        mockMvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", 1L)
                        .param("state", "ALL")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk());
    }

    @Test
    void getOwnerBookings_ShouldReturnOk() throws Exception {
        when(bookingService.getOwnerBookings(anyLong(), anyString(), anyInt(), anyInt()))
                .thenReturn(List.of());

        mockMvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", 1L)
                        .param("state", "ALL")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk());
    }
}

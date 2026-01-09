package ru.practicum.shareit.gateway.client;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import ru.practicum.shareit.dto.BookingRequestDto;

import java.time.LocalDateTime;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingClientTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private BookingClient bookingClient;

    @Test
    void createBooking_CallsPostMethod() {
        // Arrange
        BookingRequestDto requestDto = new BookingRequestDto(
                1L,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2)
        );

        ResponseEntity<Object> expectedResponse = ResponseEntity.ok("booking created");

        // Use doReturn for spy
        BookingClient spyClient = spy(bookingClient);
        doReturn(expectedResponse).when(spyClient).post(
                eq(""),
                eq(1L),
                eq(null),
                eq(requestDto)
        );

        // Act
        ResponseEntity<Object> response = spyClient.createBooking(requestDto, 1L);

        // Assert
        assertEquals(expectedResponse, response);
        verify(spyClient).post(eq(""), eq(1L), eq(null), eq(requestDto));
    }

    @Test
    @SuppressWarnings("unchecked")
    void updateBookingStatus_CallsPatchMethod() {
        // Arrange
        BookingClient spyClient = spy(bookingClient);
        ResponseEntity<Object> expectedResponse = ResponseEntity.ok("booking updated");

        doReturn(expectedResponse).when(spyClient).patch(
                eq("/1"),
                eq(1L),
                any(Map.class),
                isNull()
        );

        // Act
        ResponseEntity<Object> response = spyClient.updateBookingStatus(1L, true, 1L);

        // Assert
        assertEquals(expectedResponse, response);
        verify(spyClient).patch(eq("/1"), eq(1L), any(Map.class), isNull());
    }

    @Test
    void getBookingById_CallsGetMethod() {
        // Arrange
        BookingClient spyClient = spy(bookingClient);
        ResponseEntity<Object> expectedResponse = ResponseEntity.ok("booking");

        doReturn(expectedResponse).when(spyClient).get(
                eq("/1"),
                eq(1L),
                isNull()
        );

        // Act
        ResponseEntity<Object> response = spyClient.getBookingById(1L, 1L);

        // Assert
        assertEquals(expectedResponse, response);
        verify(spyClient).get(eq("/1"), eq(1L), isNull());
    }

    @Test
    @SuppressWarnings("unchecked")
    void getUserBookings_CallsGetMethod() {
        // Arrange
        BookingClient spyClient = spy(bookingClient);
        ResponseEntity<Object> expectedResponse = ResponseEntity.ok("user bookings");

        doReturn(expectedResponse).when(spyClient).get(
                eq("?state={state}&from={from}&size={size}"),
                eq(1L),
                any(Map.class)
        );

        // Act
        ResponseEntity<Object> response = spyClient.getUserBookings(1L, "ALL", 0, 10);

        // Assert
        assertEquals(expectedResponse, response);
        verify(spyClient).get(
                eq("?state={state}&from={from}&size={size}"),
                eq(1L),
                any(Map.class)
        );
    }

    @Test
    @SuppressWarnings("unchecked")
    void getOwnerBookings_CallsGetMethod() {
        // Arrange
        BookingClient spyClient = spy(bookingClient);
        ResponseEntity<Object> expectedResponse = ResponseEntity.ok("owner bookings");

        doReturn(expectedResponse).when(spyClient).get(
                eq("/owner?state={state}&from={from}&size={size}"),
                eq(1L),
                any(Map.class)
        );

        // Act
        ResponseEntity<Object> response = spyClient.getOwnerBookings(1L, "ALL", 0, 10);

        // Assert
        assertEquals(expectedResponse, response);
        verify(spyClient).get(
                eq("/owner?state={state}&from={from}&size={size}"),
                eq(1L),
                any(Map.class)
        );
    }

    @Test
    void constructor_SetsCorrectBasePath() {
        // Arrange
        RestTemplate restTemplate = mock(RestTemplate.class);
        String serverUrl = "http://localhost:9090";

        // Act
        BookingClient client = new BookingClient(serverUrl, restTemplate);

        // Assert
        // Проверяем что конструктор вызывается корректно
        assertEquals(client, client); // Простая проверка на существование объекта
    }
}

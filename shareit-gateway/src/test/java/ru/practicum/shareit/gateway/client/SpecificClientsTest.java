package ru.practicum.shareit.gateway.client;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import ru.practicum.shareit.dto.*;
import ru.practicum.shareit.dto.BookingRequestDto;

import java.time.LocalDateTime;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SpecificClientsTest {

    @Nested
    class UserClientTest {
        @Mock private RestTemplate restTemplate;
        @InjectMocks private UserClient userClient;

        @Test
        void createUser_CallsPostMethod() {
            UserClient spyClient = spy(userClient);
            UserDto userDto = UserDto.builder()
                    .name("John Doe")
                    .email("john@example.com")
                    .build();

            ResponseEntity<Object> expectedResponse = ResponseEntity.ok(userDto);
            doReturn(expectedResponse).when(spyClient).post(eq(""), eq(userDto));

            ResponseEntity<Object> response = spyClient.createUser(userDto);
            assertEquals(expectedResponse, response);
            verify(spyClient).post(eq(""), eq(userDto));
        }

        @Test
        void updateUser_CallsPatchMethod() {
            UserClient spyClient = spy(userClient);
            UserDto userDto = UserDto.builder()
                    .name("Updated Name")
                    .email("updated@example.com")
                    .build();

            ResponseEntity<Object> expectedResponse = ResponseEntity.ok(userDto);
            doReturn(expectedResponse).when(spyClient).patch(eq("/1"), eq(userDto));

            ResponseEntity<Object> response = spyClient.updateUser(1L, userDto);
            assertEquals(expectedResponse, response);
            verify(spyClient).patch(eq("/1"), eq(userDto));
        }

        @Test
        void getUserById_CallsGetMethod() {
            UserClient spyClient = spy(userClient);
            ResponseEntity<Object> expectedResponse = ResponseEntity.ok("user");
            doReturn(expectedResponse).when(spyClient).get(eq("/1"));

            ResponseEntity<Object> response = spyClient.getUserById(1L);
            assertEquals(expectedResponse, response);
            verify(spyClient).get(eq("/1"));
        }

        @Test
        void getAllUsers_CallsGetMethod() {
            UserClient spyClient = spy(userClient);
            ResponseEntity<Object> expectedResponse = ResponseEntity.ok("users");
            doReturn(expectedResponse).when(spyClient).get(eq(""));

            ResponseEntity<Object> response = spyClient.getAllUsers();
            assertEquals(expectedResponse, response);
            verify(spyClient).get(eq(""));
        }

        @Test
        void deleteUser_CallsDeleteMethod() {
            UserClient spyClient = spy(userClient);
            ResponseEntity<Object> expectedResponse = ResponseEntity.noContent().build();
            doReturn(expectedResponse).when(spyClient).delete(eq("/1"));

            ResponseEntity<Object> response = spyClient.deleteUser(1L);
            assertEquals(expectedResponse, response);
            verify(spyClient).delete(eq("/1"));
        }

        @Test
        void constructor_SetsCorrectBasePath() {
            RestTemplate restTemplate = mock(RestTemplate.class);
            UserClient client = new UserClient("http://localhost:9090", restTemplate);
            assertEquals(client, client);
        }
    }

    @Nested
    class BookingClientTest {
        @Mock private RestTemplate restTemplate;
        @InjectMocks private BookingClient bookingClient;

        @Test
        void createBooking_CallsPostMethod() {
            BookingRequestDto requestDto = new BookingRequestDto(
                    1L,
                    LocalDateTime.now().plusDays(1),
                    LocalDateTime.now().plusDays(2)
            );

            ResponseEntity<Object> expectedResponse = ResponseEntity.ok("booking created");
            BookingClient spyClient = spy(bookingClient);
            doReturn(expectedResponse).when(spyClient).post(eq(""), eq(1L), eq(null), eq(requestDto));

            ResponseEntity<Object> response = spyClient.createBooking(requestDto, 1L);
            assertEquals(expectedResponse, response);
            verify(spyClient).post(eq(""), eq(1L), eq(null), eq(requestDto));
        }

        @Test
        @SuppressWarnings("unchecked")
        void updateBookingStatus_CallsPatchMethod() {
            BookingClient spyClient = spy(bookingClient);
            ResponseEntity<Object> expectedResponse = ResponseEntity.ok("booking updated");
            doReturn(expectedResponse).when(spyClient).patch(eq("/1"), eq(1L), any(Map.class), isNull());

            ResponseEntity<Object> response = spyClient.updateBookingStatus(1L, true, 1L);
            assertEquals(expectedResponse, response);
            verify(spyClient).patch(eq("/1"), eq(1L), any(Map.class), isNull());
        }

        @Test
        void getBookingById_CallsGetMethod() {
            BookingClient spyClient = spy(bookingClient);
            ResponseEntity<Object> expectedResponse = ResponseEntity.ok("booking");
            doReturn(expectedResponse).when(spyClient).get(eq("/1"), eq(1L), isNull());

            ResponseEntity<Object> response = spyClient.getBookingById(1L, 1L);
            assertEquals(expectedResponse, response);
            verify(spyClient).get(eq("/1"), eq(1L), isNull());
        }

        @Test
        @SuppressWarnings("unchecked")
        void getUserBookings_CallsGetMethod() {
            BookingClient spyClient = spy(bookingClient);
            ResponseEntity<Object> expectedResponse = ResponseEntity.ok("user bookings");
            doReturn(expectedResponse).when(spyClient).get(
                    eq("?state={state}&from={from}&size={size}"), eq(1L), any(Map.class));

            ResponseEntity<Object> response = spyClient.getUserBookings(1L, "ALL", 0, 10);
            assertEquals(expectedResponse, response);
            verify(spyClient).get(eq("?state={state}&from={from}&size={size}"), eq(1L), any(Map.class));
        }

        @Test
        @SuppressWarnings("unchecked")
        void getOwnerBookings_CallsGetMethod() {
            BookingClient spyClient = spy(bookingClient);
            ResponseEntity<Object> expectedResponse = ResponseEntity.ok("owner bookings");
            doReturn(expectedResponse).when(spyClient).get(
                    eq("/owner?state={state}&from={from}&size={size}"), eq(1L), any(Map.class));

            ResponseEntity<Object> response = spyClient.getOwnerBookings(1L, "ALL", 0, 10);
            assertEquals(expectedResponse, response);
            verify(spyClient).get(eq("/owner?state={state}&from={from}&size={size}"), eq(1L), any(Map.class));
        }

        @Test
        void constructor_SetsCorrectBasePath() {
            RestTemplate restTemplate = mock(RestTemplate.class);
            BookingClient client = new BookingClient("http://localhost:9090", restTemplate);
            assertEquals(client, client);
        }
    }

    @Nested
    class CachedItemClientTest {
        @Mock private RestTemplate restTemplate;
        @InjectMocks private CachedItemClient itemClient;

        @Test
        void createItem_CallsPostMethod() {
            CachedItemClient spyClient = spy(itemClient);
            ItemDto itemDto = ItemDto.builder()
                    .name("Test Item")
                    .description("Test Description")
                    .available(true)
                    .build();

            ResponseEntity<Object> expectedResponse = ResponseEntity.ok(itemDto);
            doReturn(expectedResponse).when(spyClient).post(eq(""), eq(1L), eq(itemDto));

            ResponseEntity<Object> response = spyClient.createItem(itemDto, 1L);
            assertEquals(expectedResponse, response);
            verify(spyClient).post(eq(""), eq(1L), eq(itemDto));
        }

        @Test
        void updateItem_CallsPatchMethod() {
            CachedItemClient spyClient = spy(itemClient);
            ItemDto itemDto = ItemDto.builder()
                    .name("Updated Item")
                    .description("Updated Description")
                    .available(false)
                    .build();

            ResponseEntity<Object> expectedResponse = ResponseEntity.ok(itemDto);
            doReturn(expectedResponse).when(spyClient).patch(eq("/1"), eq(1L), eq(itemDto));

            ResponseEntity<Object> response = spyClient.updateItem(1L, itemDto, 1L);
            assertEquals(expectedResponse, response);
            verify(spyClient).patch(eq("/1"), eq(1L), eq(itemDto));
        }

        @Test
        @SuppressWarnings("unchecked")
        void getItemById_CallsGetMethod() {
            CachedItemClient spyClient = spy(itemClient);
            ResponseEntity<Object> expectedResponse = ResponseEntity.ok("item");
            doReturn(expectedResponse).when(spyClient).get(eq("/1?userId={userId}"), eq(1L), any(Map.class));

            ResponseEntity<Object> response = spyClient.getItemById(1L, 1L);
            assertEquals(expectedResponse, response);
            verify(spyClient).get(eq("/1?userId={userId}"), eq(1L), any(Map.class));
        }

        @Test
        void getAllItemsByOwner_CallsGetMethod() {
            CachedItemClient spyClient = spy(itemClient);
            ResponseEntity<Object> expectedResponse = ResponseEntity.ok("items");
            doReturn(expectedResponse).when(spyClient).get(eq(""), eq(1L));

            ResponseEntity<Object> response = spyClient.getAllItemsByOwner(1L);
            assertEquals(expectedResponse, response);
            verify(spyClient).get(eq(""), eq(1L));
        }

        @Test
        @SuppressWarnings("unchecked")
        void searchItems_CallsGetMethod() {
            CachedItemClient spyClient = spy(itemClient);
            ResponseEntity<Object> expectedResponse = ResponseEntity.ok("search results");
            doReturn(expectedResponse).when(spyClient).get(
                    eq("/search?text={text}&userId={userId}"), eq(1L), any(Map.class));

            ResponseEntity<Object> response = spyClient.searchItems("drill", 1L);
            assertEquals(expectedResponse, response);
            verify(spyClient).get(eq("/search?text={text}&userId={userId}"), eq(1L), any(Map.class));
        }

        @Test
        void addComment_CallsPostMethod() {
            CachedItemClient spyClient = spy(itemClient);
            CommentDto commentDto = CommentDto.builder()
                    .text("Great item!")
                    .build();

            ResponseEntity<Object> expectedResponse = ResponseEntity.ok("comment added");
            doReturn(expectedResponse).when(spyClient).post(eq("/1/comment"), eq(1L), eq(commentDto));

            ResponseEntity<Object> response = spyClient.addComment(1L, commentDto, 1L);
            assertEquals(expectedResponse, response);
            verify(spyClient).post(eq("/1/comment"), eq(1L), eq(commentDto));
        }

        @Test
        void constructor_SetsCorrectBasePath() {
            RestTemplate restTemplate = mock(RestTemplate.class);
            CachedItemClient client = new CachedItemClient("http://localhost:9090", restTemplate);
            assertEquals(client, client);
        }
    }

    @Nested
    class ItemRequestClientTest {
        @Mock private RestTemplate restTemplate;
        @InjectMocks private ItemRequestClient itemRequestClient;

        @Test
        void createItemRequest_CallsPostMethod() {
            ItemRequestClient spyClient = spy(itemRequestClient);
            ItemRequestDto requestDto = ItemRequestDto.builder()
                    .description("Need a power drill")
                    .build();

            ResponseEntity<Object> expectedResponse = ResponseEntity.ok("request created");
            doReturn(expectedResponse).when(spyClient).post(eq(""), eq(1L), eq(requestDto));

            ResponseEntity<Object> response = spyClient.createItemRequest(requestDto, 1L);
            assertEquals(expectedResponse, response);
            verify(spyClient).post(eq(""), eq(1L), eq(requestDto));
        }

        @Test
        void getUserItemRequests_CallsGetMethod() {
            ItemRequestClient spyClient = spy(itemRequestClient);
            ResponseEntity<Object> expectedResponse = ResponseEntity.ok("user requests");
            doReturn(expectedResponse).when(spyClient).get(eq(""), eq(1L));

            ResponseEntity<Object> response = spyClient.getUserItemRequests(1L);
            assertEquals(expectedResponse, response);
            verify(spyClient).get(eq(""), eq(1L));
        }

        @Test
        @SuppressWarnings("unchecked")
        void getAllItemRequests_CallsGetMethod() {
            ItemRequestClient spyClient = spy(itemRequestClient);
            ResponseEntity<Object> expectedResponse = ResponseEntity.ok("all requests");
            doReturn(expectedResponse).when(spyClient).get(
                    eq("/all?from={from}&size={size}"), eq(1L), any(Map.class));

            ResponseEntity<Object> response = spyClient.getAllItemRequests(1L, 0, 10);
            assertEquals(expectedResponse, response);
            verify(spyClient).get(eq("/all?from={from}&size={size}"), eq(1L), any(Map.class));
        }

        @Test
        void getItemRequestById_CallsGetMethod() {
            ItemRequestClient spyClient = spy(itemRequestClient);
            ResponseEntity<Object> expectedResponse = ResponseEntity.ok("request details");
            doReturn(expectedResponse).when(spyClient).get(eq("/1"), eq(1L));

            ResponseEntity<Object> response = spyClient.getItemRequestById(1L, 1L);
            assertEquals(expectedResponse, response);
            verify(spyClient).get(eq("/1"), eq(1L));
        }

        @Test
        void constructor_SetsCorrectBasePath() {
            RestTemplate restTemplate = mock(RestTemplate.class);
            ItemRequestClient client = new ItemRequestClient("http://localhost:9090", restTemplate);
            assertEquals(client, client);
        }
    }
}

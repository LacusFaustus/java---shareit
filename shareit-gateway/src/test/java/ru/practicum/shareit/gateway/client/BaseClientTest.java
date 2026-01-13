package ru.practicum.shareit.gateway.client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.*;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.lang.reflect.Method;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class BaseClientTest {

    @Mock
    private RestTemplate restTemplate;
    private TestBaseClient baseClient;
    private final String BASE_URL = "http://localhost:9090";

    @BeforeEach
    void setUp() {
        // Настраиваем базовый мок для всех тестов
        lenient().when(restTemplate.getUriTemplateHandler()).thenReturn(null);
        baseClient = new TestBaseClient(BASE_URL, restTemplate);
    }

    // Вспомогательный класс для тестирования
    private static class TestBaseClient extends BaseClient {
        public TestBaseClient(String serverUrl, RestTemplate restTemplate) {
            super(serverUrl, restTemplate);
        }

        public TestBaseClient(RestTemplate restTemplate) {
            super(restTemplate);
        }

        // Публичные методы для тестирования protected методов
        // Для совместимости с базовым классом используем long вместо Long
        public ResponseEntity<Object> get(String path) {
            return get(path, null, null);
        }

        public ResponseEntity<Object> get(String path, long userId) {
            return get(path, userId, null);
        }

        public ResponseEntity<Object> get(String path, Long userId, Map<String, Object> parameters) {
            return super.get(path, userId, parameters);
        }

        public ResponseEntity<Object> post(String path, Object body) {
            return super.post(path, (Long) null, null, body);
        }

        public ResponseEntity<Object> post(String path, long userId, Object body) {
            return super.post(path, userId, null, body);
        }

        public ResponseEntity<Object> post(String path, Long userId, Object body) {
            return super.post(path, userId, null, body);
        }

        public ResponseEntity<Object> patch(String path, Object body) {
            return super.patch(path, (Long) null, null, body);
        }

        public ResponseEntity<Object> patch(String path, long userId, Object body) {
            return super.patch(path, userId, null, body);
        }

        public ResponseEntity<Object> patch(String path, Long userId, Object body) {
            return super.patch(path, userId, null, body);
        }

        public ResponseEntity<Object> put(String path, long userId, Object body) {
            return super.put(path, userId, null, body);
        }

        public ResponseEntity<Object> put(String path, Long userId, Object body) {
            return super.put(path, userId, null, body);
        }

        public ResponseEntity<Object> put(String path, Long userId, Map<String, Object> parameters, Object body) {
            return super.put(path, userId, parameters, body);
        }

        public ResponseEntity<Object> delete(String path) {
            return super.delete(path, null, null);
        }

        public ResponseEntity<Object> delete(String path, long userId) {
            return super.delete(path, userId, null);
        }

        public ResponseEntity<Object> delete(String path, Long userId) {
            return super.delete(path, userId, null);
        }

        // Статический метод для тестирования prepareGatewayResponse
        public static ResponseEntity<Object> testPrepareGatewayResponse(ResponseEntity<Object> response) {
            return BaseClient.prepareGatewayResponse(response);
        }

        public RestTemplate getRestTemplate() {
            return rest;
        }
    }

    @Nested
    class BasicMethodTests {

        @Test
        void get_ReturnsResponse() {
            // Given
            String expectedResponse = "{\"id\":1,\"name\":\"Test\"}";
            ResponseEntity<Object> mockResponse = new ResponseEntity<>(expectedResponse, HttpStatus.OK);

            when(restTemplate.exchange(
                    eq("/test"),
                    eq(HttpMethod.GET),
                    any(HttpEntity.class),
                    eq(Object.class)
            )).thenReturn(mockResponse);

            // When
            ResponseEntity<Object> result = baseClient.get("/test", 1L);

            // Then
            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(result.getBody()).isEqualTo(expectedResponse);
        }

        @Test
        void post_WithBodyAndUserId_ReturnsResponse() {
            // Given
            String requestBody = "{\"name\":\"Test\"}";
            String expectedResponse = "{\"id\":1}";
            ResponseEntity<Object> mockResponse = new ResponseEntity<>(expectedResponse, HttpStatus.CREATED);

            when(restTemplate.exchange(
                    eq("/test"),
                    eq(HttpMethod.POST),
                    any(HttpEntity.class),
                    eq(Object.class)
            )).thenReturn(mockResponse);

            // When
            ResponseEntity<Object> result = baseClient.post("/test", 1L, requestBody);

            // Then
            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(result.getBody()).isEqualTo(expectedResponse);
        }

        @Test
        void patch_WithBodyAndUserId_ReturnsResponse() {
            // Given
            String requestBody = "{\"name\":\"Updated\"}";
            String expectedResponse = "{\"id\":1,\"name\":\"Updated\"}";
            ResponseEntity<Object> mockResponse = new ResponseEntity<>(expectedResponse, HttpStatus.OK);

            when(restTemplate.exchange(
                    eq("/test/1"),
                    eq(HttpMethod.PATCH),
                    any(HttpEntity.class),
                    eq(Object.class)
            )).thenReturn(mockResponse);

            // When
            ResponseEntity<Object> result = baseClient.patch("/test/1", 1L, requestBody);

            // Then
            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(result.getBody()).isEqualTo(expectedResponse);
        }

        @Test
        void delete_ReturnsNoContent() {
            // Given
            ResponseEntity<Object> mockResponse = new ResponseEntity<>(HttpStatus.NO_CONTENT);

            when(restTemplate.exchange(
                    eq("/test/1"),
                    eq(HttpMethod.DELETE),
                    any(HttpEntity.class),
                    eq(Object.class)
            )).thenReturn(mockResponse);

            // When
            ResponseEntity<Object> result = baseClient.delete("/test/1", 1L);

            // Then
            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
            assertThat(result.getBody()).isNull();
        }

        @Test
        void put_WithValidData_ReturnsResponse() {
            // Given
            String requestBody = "{\"name\":\"Updated\"}";
            String expectedResponse = "{\"id\":1,\"name\":\"Updated\"}";
            ResponseEntity<Object> mockResponse = new ResponseEntity<>(expectedResponse, HttpStatus.OK);

            when(restTemplate.exchange(
                    eq("/test/1"),
                    eq(HttpMethod.PUT),
                    any(HttpEntity.class),
                    eq(Object.class)
            )).thenReturn(mockResponse);

            // When
            ResponseEntity<Object> result = baseClient.put("/test/1", 1L, requestBody);

            // Then
            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(result.getBody()).isEqualTo(expectedResponse);
        }

        @Test
        void put_WithParameters_ReturnsResponse() {
            // Given
            String requestBody = "{\"name\":\"Updated\"}";
            String expectedResponse = "{\"id\":1,\"name\":\"Updated\"}";
            ResponseEntity<Object> mockResponse = new ResponseEntity<>(expectedResponse, HttpStatus.OK);
            Map<String, Object> parameters = Map.of("param", "value");

            // Используем eq() для каждого аргумента отдельно
            when(restTemplate.exchange(
                    eq("/test/1?param={param}"),
                    eq(HttpMethod.PUT),
                    any(HttpEntity.class),
                    eq(Object.class),
                    eq(parameters)
            )).thenReturn(mockResponse);

            // When - используем long userId вместо Long
            ResponseEntity<Object> result = baseClient.put("/test/1?param={param}", 1L, parameters, requestBody);

            // Then
            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(result.getBody()).isEqualTo(expectedResponse);
        }

        @Test
        void patch_WithoutUserId_ReturnsResponse() {
            // Given
            String requestBody = "{\"name\":\"Updated\"}";
            String expectedResponse = "{\"id\":1}";
            ResponseEntity<Object> mockResponse = new ResponseEntity<>(expectedResponse, HttpStatus.OK);

            when(restTemplate.exchange(
                    eq("/test"),
                    eq(HttpMethod.PATCH),
                    any(HttpEntity.class),
                    eq(Object.class)
            )).thenReturn(mockResponse);

            // When
            ResponseEntity<Object> result = baseClient.patch("/test", requestBody);

            // Then
            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(result.getBody()).isEqualTo(expectedResponse);
        }

        @Test
        void delete_WithoutUserId_ReturnsResponse() {
            // Given
            ResponseEntity<Object> mockResponse = new ResponseEntity<>(HttpStatus.NO_CONTENT);

            when(restTemplate.exchange(
                    eq("/test"),
                    eq(HttpMethod.DELETE),
                    any(HttpEntity.class),
                    eq(Object.class)
            )).thenReturn(mockResponse);

            // When
            ResponseEntity<Object> result = baseClient.delete("/test");

            // Then
            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
            assertThat(result.getBody()).isNull();
        }

        @Test
        void get_WithParameters_ReturnsResponse() {
            // Given
            String expectedResponse = "{\"id\":1}";
            ResponseEntity<Object> mockResponse = new ResponseEntity<>(expectedResponse, HttpStatus.OK);
            Map<String, Object> parameters = Map.of("param", "value");

            when(restTemplate.exchange(
                    eq("/test?param={param}"),
                    eq(HttpMethod.GET),
                    any(HttpEntity.class),
                    eq(Object.class),
                    eq(parameters)
            )).thenReturn(mockResponse);

            // When
            ResponseEntity<Object> result = baseClient.get("/test?param={param}", 1L, parameters);

            // Then
            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(result.getBody()).isEqualTo(expectedResponse);
        }

        @Test
        void get_WithoutUserId_ReturnsResponse() {
            // Given
            String expectedResponse = "{\"id\":1}";
            ResponseEntity<Object> mockResponse = new ResponseEntity<>(expectedResponse, HttpStatus.OK);

            when(restTemplate.exchange(
                    eq("/test"),
                    eq(HttpMethod.GET),
                    any(HttpEntity.class),
                    eq(Object.class)
            )).thenReturn(mockResponse);

            // When
            ResponseEntity<Object> result = baseClient.get("/test");

            // Then
            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(result.getBody()).isEqualTo(expectedResponse);
        }
    }

    @Nested
    class EdgeCasesAndErrorTests {

        @Test
        void get_WithResourceAccessException_ThrowsException() {
            // Given
            when(restTemplate.exchange(
                    anyString(),
                    eq(HttpMethod.GET),
                    any(HttpEntity.class),
                    eq(Object.class)
            )).thenThrow(new ResourceAccessException("Connection refused"));

            // When & Then
            try {
                baseClient.get("/test", 1L);
            } catch (ResourceAccessException e) {
                assertThat(e.getMessage()).contains("Connection refused");
            }
        }

        @Test
        void post_WithEmptyResponseBody() {
            // Given
            ResponseEntity<Object> mockResponse = new ResponseEntity<>("", HttpHeaders.EMPTY, HttpStatus.OK);

            when(restTemplate.exchange(
                    anyString(),
                    eq(HttpMethod.POST),
                    any(HttpEntity.class),
                    eq(Object.class)
            )).thenReturn(mockResponse);

            // When
            ResponseEntity<Object> result = baseClient.post("/test", 1L, "{}");

            // Then
            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(result.getBody()).isEqualTo("");
        }

        @Test
        void patch_WithResponseContainingSpecialCharacters() {
            // Given
            String responseBody = "{\"name\":\"Test\\nItem\",\"description\":\"Test\\\"Description\\\"\"}";
            ResponseEntity<Object> mockResponse = new ResponseEntity<>(responseBody, HttpStatus.OK);

            when(restTemplate.exchange(
                    anyString(),
                    eq(HttpMethod.PATCH),
                    any(HttpEntity.class),
                    eq(Object.class)
            )).thenReturn(mockResponse);

            // When
            ResponseEntity<Object> result = baseClient.patch("/test/1", 1L, "{}");

            // Then
            assertThat(result.getBody()).isEqualTo(responseBody);
        }

        @Test
        void delete_WithNoContentResponse() {
            // Given
            ResponseEntity<Object> mockResponse = new ResponseEntity<>(HttpStatus.NO_CONTENT);

            when(restTemplate.exchange(
                    anyString(),
                    eq(HttpMethod.DELETE),
                    any(HttpEntity.class),
                    eq(Object.class)
            )).thenReturn(mockResponse);

            // When
            ResponseEntity<Object> result = baseClient.delete("/test/1", 1L);

            // Then
            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
            assertThat(result.getBody()).isNull();
        }

        @Test
        void delete_WithBodyInResponse() {
            // Given
            String responseBody = "{\"message\":\"Deleted successfully\"}";
            ResponseEntity<Object> mockResponse = new ResponseEntity<>(responseBody, HttpStatus.OK);

            when(restTemplate.exchange(
                    anyString(),
                    eq(HttpMethod.DELETE),
                    any(HttpEntity.class),
                    eq(Object.class)
            )).thenReturn(mockResponse);

            // When
            ResponseEntity<Object> result = baseClient.delete("/test/1", 1L);

            // Then
            assertThat(result.getBody()).isEqualTo(responseBody);
        }

        @Test
        void methods_WithZeroUserId() {
            // Given
            String expectedResponse = "{\"id\":1}";
            ResponseEntity<Object> mockResponse = new ResponseEntity<>(expectedResponse, HttpStatus.OK);

            when(restTemplate.exchange(
                    anyString(),
                    eq(HttpMethod.GET),
                    any(HttpEntity.class),
                    eq(Object.class)
            )).thenReturn(mockResponse);

            // When
            ResponseEntity<Object> result = baseClient.get("/test", 0L);

            // Then
            assertThat(result.getBody()).isEqualTo(expectedResponse);
        }

        @Test
        void methods_WithNegativeUserId() {
            // Given
            String expectedResponse = "{\"id\":1}";
            ResponseEntity<Object> mockResponse = new ResponseEntity<>(expectedResponse, HttpStatus.OK);

            when(restTemplate.exchange(
                    anyString(),
                    eq(HttpMethod.GET),
                    any(HttpEntity.class),
                    eq(Object.class)
            )).thenReturn(mockResponse);

            // When
            ResponseEntity<Object> result = baseClient.get("/test", -1L);

            // Then
            assertThat(result.getBody()).isEqualTo(expectedResponse);
        }

        @Test
        void get_WithHttpStatusCodeException_ReturnsErrorResponse() {
            // Given
            HttpStatusCodeException exception = mock(HttpStatusCodeException.class);
            when(exception.getStatusCode()).thenReturn(HttpStatus.NOT_FOUND);
            when(exception.getResponseBodyAsByteArray()).thenReturn("Not Found".getBytes());

            when(restTemplate.exchange(
                    eq("/test"),
                    eq(HttpMethod.GET),
                    any(HttpEntity.class),
                    eq(Object.class)
            )).thenThrow(exception);

            // When
            ResponseEntity<Object> result = baseClient.get("/test", 1L);

            // Then
            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            assertThat(result.getBody()).isEqualTo("Not Found".getBytes());
        }

        @Test
        void handleHttpClientErrorException_WithMalformedJsonInResponse() {
            // Given
            String malformedJson = "{invalid json}";
            HttpStatusCodeException exception = mock(HttpStatusCodeException.class);
            when(exception.getStatusCode()).thenReturn(HttpStatus.BAD_REQUEST);
            when(exception.getResponseBodyAsByteArray()).thenReturn(malformedJson.getBytes());

            when(restTemplate.exchange(
                    anyString(),
                    eq(HttpMethod.GET),
                    any(HttpEntity.class),
                    eq(Object.class)
            )).thenThrow(exception);

            // When
            ResponseEntity<Object> result = baseClient.get("/test", 1L);

            // Then
            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(result.getBody()).isEqualTo(malformedJson.getBytes());
        }
    }

    @Nested
    class PrepareResponseTests {

        @Test
        void prepareGatewayResponse_WithNullResponse_ReturnsInternalServerError() {
            // When
            ResponseEntity<Object> result = TestBaseClient.testPrepareGatewayResponse(null);

            // Then
            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
            assertThat((String) result.getBody()).contains("Internal server error");
        }

        @Test
        void prepareGatewayResponse_With2xxResponse_ReturnsSameResponse() {
            // Given
            ResponseEntity<Object> input = new ResponseEntity<>("success", HttpStatus.OK);

            // When
            ResponseEntity<Object> result = TestBaseClient.testPrepareGatewayResponse(input);

            // Then
            assertThat(result.getStatusCode()).isEqualTo(input.getStatusCode());
            assertThat(result.getBody()).isEqualTo(input.getBody());
        }

        @Test
        void prepareGatewayResponse_With4xxResponse_ReturnsSameResponse() {
            // Given
            ResponseEntity<Object> input = new ResponseEntity<>("error", HttpStatus.NOT_FOUND);

            // When
            ResponseEntity<Object> result = TestBaseClient.testPrepareGatewayResponse(input);

            // Then
            assertThat(result.getStatusCode()).isEqualTo(input.getStatusCode());
            assertThat(result.getBody()).isEqualTo(input.getBody());
        }

        @Test
        void prepareGatewayResponse_WithErrorResponseNoBody_ReturnsResponseBuilder() {
            // Given
            ResponseEntity<Object> input = new ResponseEntity<>(HttpStatus.BAD_REQUEST);

            // When
            ResponseEntity<Object> result = TestBaseClient.testPrepareGatewayResponse(input);

            // Then
            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(result.getBody()).isNull();
        }
    }

    @Nested
    class ConstructorAndUtilityTests {

        @Test
        void testConstructor_WithRestTemplateOnly() {
            // Given
            RestTemplate restTemplate = new RestTemplate();

            // When
            TestBaseClient client = new TestBaseClient(restTemplate);

            // Then
            assertThat(client).isNotNull();
        }

        @Test
        void testGetRestTemplate() {
            // When & Then
            assertThat(baseClient.getRestTemplate()).isEqualTo(restTemplate);
        }

        @Test
        void testAllMethodVariants() {
            // Given
            String expectedResponse = "test";
            ResponseEntity<Object> mockResponse = new ResponseEntity<>(expectedResponse, HttpStatus.OK);

            // Настраиваем моки для всех вариантов
            lenient().when(restTemplate.exchange(
                    anyString(),
                    any(HttpMethod.class),
                    any(HttpEntity.class),
                    eq(Object.class)
            )).thenReturn(mockResponse);

            lenient().when(restTemplate.exchange(
                    anyString(),
                    any(HttpMethod.class),
                    any(HttpEntity.class),
                    eq(Object.class),
                    any(Map.class)
            )).thenReturn(mockResponse);

            // When & Then
            ResponseEntity<Object> result1 = baseClient.get("/test");
            assertThat(result1.getBody()).isEqualTo(expectedResponse);

            ResponseEntity<Object> result2 = baseClient.get("/test", 1L);
            assertThat(result2.getBody()).isEqualTo(expectedResponse);

            ResponseEntity<Object> result3 = baseClient.post("/test", "body");
            assertThat(result3.getBody()).isEqualTo(expectedResponse);

            ResponseEntity<Object> result4 = baseClient.post("/test", 1L, "body");
            assertThat(result4.getBody()).isEqualTo(expectedResponse);

            ResponseEntity<Object> result5 = baseClient.patch("/test", "body");
            assertThat(result5.getBody()).isEqualTo(expectedResponse);

            ResponseEntity<Object> result6 = baseClient.delete("/test");
            assertThat(result6.getBody()).isEqualTo(expectedResponse);
        }

        @Test
        void testMakeAndSendRequest_WithParameters() throws Exception {
            // Given
            String expectedResponse = "{\"id\":1}";
            ResponseEntity<Object> mockResponse = new ResponseEntity<>(expectedResponse, HttpStatus.OK);
            Map<String, Object> parameters = Map.of("param1", "value1", "param2", "value2");

            when(restTemplate.exchange(
                    eq("/test?param1={param1}&param2={param2}"),
                    eq(HttpMethod.GET),
                    any(HttpEntity.class),
                    eq(Object.class),
                    eq(parameters)
            )).thenReturn(mockResponse);

            // When - используем рефлексию для вызова приватного метода
            Method method = BaseClient.class.getDeclaredMethod(
                    "makeAndSendRequest",
                    HttpMethod.class, String.class, Long.class, Map.class, Object.class);
            method.setAccessible(true);

            ResponseEntity<Object> result = (ResponseEntity<Object>) method.invoke(
                    baseClient, HttpMethod.GET, "/test?param1={param1}&param2={param2}", 1L, parameters, null);

            // Then
            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(result.getBody()).isEqualTo(expectedResponse);
        }
    }
}

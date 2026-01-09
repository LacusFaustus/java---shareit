package ru.practicum.shareit.gateway.client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.*;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.lang.reflect.Method;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BaseClientTest {

    @Mock
    private RestTemplate restTemplate;
    private TestBaseClient baseClient;

    @BeforeEach
    void setUp() {
        when(restTemplate.getUriTemplateHandler()).thenReturn(null);
        baseClient = new TestBaseClient("http://localhost:9090", restTemplate);
    }

    @Nested
    class BasicMethodTests {

        @Test
        void get_ReturnsResponse() {
            String expectedResponse = "{\"id\":1,\"name\":\"Test\"}";
            ResponseEntity<Object> mockResponse = new ResponseEntity<>(expectedResponse, HttpStatus.OK);

            when(restTemplate.exchange(
                    eq("/test"),
                    eq(HttpMethod.GET),
                    any(HttpEntity.class),
                    eq(Object.class)
            )).thenReturn(mockResponse);

            ResponseEntity<Object> result = baseClient.get("/test", 1L);
            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(result.getBody()).isEqualTo(expectedResponse);
        }

        @Test
        void post_WithBodyAndUserId_ReturnsResponse() {
            String requestBody = "{\"name\":\"Test\"}";
            String expectedResponse = "{\"id\":1}";
            ResponseEntity<Object> mockResponse = new ResponseEntity<>(expectedResponse, HttpStatus.CREATED);

            when(restTemplate.exchange(
                    eq("/test"),
                    eq(HttpMethod.POST),
                    any(HttpEntity.class),
                    eq(Object.class)
            )).thenReturn(mockResponse);

            ResponseEntity<Object> result = baseClient.post("/test", 1L, requestBody);
            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(result.getBody()).isEqualTo(expectedResponse);
        }

        @Test
        void patch_WithBodyAndUserId_ReturnsResponse() {
            String requestBody = "{\"name\":\"Updated\"}";
            String expectedResponse = "{\"id\":1,\"name\":\"Updated\"}";
            ResponseEntity<Object> mockResponse = new ResponseEntity<>(expectedResponse, HttpStatus.OK);

            when(restTemplate.exchange(
                    eq("/test/1"),
                    eq(HttpMethod.PATCH),
                    any(HttpEntity.class),
                    eq(Object.class)
            )).thenReturn(mockResponse);

            ResponseEntity<Object> result = baseClient.patch("/test/1", 1L, requestBody);
            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(result.getBody()).isEqualTo(expectedResponse);
        }

        @Test
        void delete_ReturnsNoContent() {
            ResponseEntity<Object> mockResponse = new ResponseEntity<>(HttpStatus.NO_CONTENT);

            when(restTemplate.exchange(
                    eq("/test/1"),
                    eq(HttpMethod.DELETE),
                    any(HttpEntity.class),
                    eq(Object.class)
            )).thenReturn(mockResponse);

            ResponseEntity<Object> result = baseClient.delete("/test/1", 1L);
            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        }

        @Test
        void put_WithValidData_ReturnsResponse() {
            String requestBody = "{\"name\":\"Updated\"}";
            String expectedResponse = "{\"id\":1,\"name\":\"Updated\"}";
            ResponseEntity<Object> mockResponse = new ResponseEntity<>(expectedResponse, HttpStatus.OK);

            doReturn(mockResponse).when(restTemplate).exchange(
                    eq("/test/1"),
                    eq(HttpMethod.PUT),
                    any(HttpEntity.class),
                    eq(Object.class)
            );

            ResponseEntity<Object> result = baseClient.put("/test/1", 1L, requestBody);
            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(result.getBody()).isEqualTo(expectedResponse);
        }

        @Test
        void put_WithParameters_ReturnsResponse() {
            String requestBody = "{\"name\":\"Updated\"}";
            String expectedResponse = "{\"id\":1,\"name\":\"Updated\"}";
            ResponseEntity<Object> mockResponse = new ResponseEntity<>(expectedResponse, HttpStatus.OK);

            doReturn(mockResponse).when(restTemplate).exchange(
                    eq("/test/1"),
                    eq(HttpMethod.PUT),
                    any(HttpEntity.class),
                    eq(Object.class),
                    any(Map.class)
            );

            ResponseEntity<Object> result = baseClient.put("/test/1", 1L, Map.of("param", "value"), requestBody);
            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(result.getBody()).isEqualTo(expectedResponse);
        }

        @Test
        void patch_WithoutUserId_ReturnsResponse() {
            String requestBody = "{\"name\":\"Updated\"}";
            String expectedResponse = "{\"id\":1}";
            ResponseEntity<Object> mockResponse = new ResponseEntity<>(expectedResponse, HttpStatus.OK);

            doReturn(mockResponse).when(restTemplate).exchange(
                    eq("/test"),
                    eq(HttpMethod.PATCH),
                    any(HttpEntity.class),
                    eq(Object.class)
            );

            ResponseEntity<Object> result = baseClient.patch("/test", requestBody);
            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(result.getBody()).isEqualTo(expectedResponse);
        }

        @Test
        void delete_WithoutUserId_ReturnsResponse() {
            ResponseEntity<Object> mockResponse = new ResponseEntity<>(HttpStatus.NO_CONTENT);

            doReturn(mockResponse).when(restTemplate).exchange(
                    eq("/test"),
                    eq(HttpMethod.DELETE),
                    any(HttpEntity.class),
                    eq(Object.class)
            );

            ResponseEntity<Object> result = baseClient.delete("/test");
            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        }

        @Test
        void get_WithParameters_ReturnsResponse() {
            String expectedResponse = "{\"id\":1}";
            ResponseEntity<Object> mockResponse = new ResponseEntity<>(expectedResponse, HttpStatus.OK);

            doReturn(mockResponse).when(restTemplate).exchange(
                    eq("/test?param={param}"),
                    eq(HttpMethod.GET),
                    any(HttpEntity.class),
                    eq(Object.class),
                    any(Map.class)
            );

            ResponseEntity<Object> result = baseClient.get("/test?param={param}", 1L, Map.of("param", "value"));
            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(result.getBody()).isEqualTo(expectedResponse);
        }

        @Test
        void get_WithoutUserId_ReturnsResponse() {
            String expectedResponse = "{\"id\":1}";
            ResponseEntity<Object> mockResponse = new ResponseEntity<>(expectedResponse, HttpStatus.OK);

            when(restTemplate.exchange(
                    eq("/test"),
                    eq(HttpMethod.GET),
                    any(HttpEntity.class),
                    eq(Object.class)
            )).thenReturn(mockResponse);

            ResponseEntity<Object> result = baseClient.get("/test");
            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(result.getBody()).isEqualTo(expectedResponse);
        }
    }

    @Nested
    class EdgeCasesAndErrorTests {

        @Test
        @SuppressWarnings("unchecked")
        void get_WithResourceAccessException_ThrowsException() {
            when(restTemplate.exchange(
                    anyString(),
                    eq(HttpMethod.GET),
                    any(HttpEntity.class),
                    eq(Object.class)
            )).thenThrow(new org.springframework.web.client.ResourceAccessException("Connection refused"));

            try {
                baseClient.get("/test", 1L);
            } catch (org.springframework.web.client.ResourceAccessException e) {
                assertThat(e.getMessage()).contains("Connection refused");
            }
        }

        @Test
        void post_WithEmptyResponseBody() {
            ResponseEntity<Object> mockResponse = new ResponseEntity<>("", HttpHeaders.EMPTY, HttpStatus.OK);

            when(restTemplate.exchange(
                    anyString(),
                    eq(HttpMethod.POST),
                    any(HttpEntity.class),
                    eq(Object.class)
            )).thenReturn(mockResponse);

            ResponseEntity<Object> result = baseClient.post("/test", 1L, "{}");
            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(result.getBody()).isEqualTo("");
        }

        @Test
        void patch_WithResponseContainingSpecialCharacters() {
            String responseBody = "{\"name\":\"Test\\nItem\",\"description\":\"Test\\\"Description\\\"\"}";
            ResponseEntity<Object> mockResponse = new ResponseEntity<>(responseBody, HttpStatus.OK);

            when(restTemplate.exchange(
                    anyString(),
                    eq(HttpMethod.PATCH),
                    any(HttpEntity.class),
                    eq(Object.class)
            )).thenReturn(mockResponse);

            ResponseEntity<Object> result = baseClient.patch("/test/1", 1L, "{}");
            assertThat(result.getBody()).isEqualTo(responseBody);
        }

        @Test
        void delete_WithNoContentResponse() {
            ResponseEntity<Object> mockResponse = new ResponseEntity<>(HttpStatus.NO_CONTENT);

            when(restTemplate.exchange(
                    anyString(),
                    eq(HttpMethod.DELETE),
                    any(HttpEntity.class),
                    eq(Object.class)
            )).thenReturn(mockResponse);

            ResponseEntity<Object> result = baseClient.delete("/test/1", 1L);
            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
            assertThat(result.getBody()).isNull();
        }

        @Test
        void delete_WithBodyInResponse() {
            String responseBody = "{\"message\":\"Deleted successfully\"}";
            ResponseEntity<Object> mockResponse = new ResponseEntity<>(responseBody, HttpStatus.OK);

            when(restTemplate.exchange(
                    anyString(),
                    eq(HttpMethod.DELETE),
                    any(HttpEntity.class),
                    eq(Object.class)
            )).thenReturn(mockResponse);

            ResponseEntity<Object> result = baseClient.delete("/test/1", 1L);
            assertThat(result.getBody()).isEqualTo(responseBody);
        }

        @Test
        void methods_WithZeroUserId() {
            String expectedResponse = "{\"id\":1}";
            ResponseEntity<Object> mockResponse = new ResponseEntity<>(expectedResponse, HttpStatus.OK);

            when(restTemplate.exchange(
                    anyString(),
                    eq(HttpMethod.GET),
                    any(HttpEntity.class),
                    eq(Object.class)
            )).thenReturn(mockResponse);

            ResponseEntity<Object> result = baseClient.get("/test", 0L);
            assertThat(result.getBody()).isEqualTo(expectedResponse);
        }

        @Test
        void methods_WithNegativeUserId() {
            String expectedResponse = "{\"id\":1}";
            ResponseEntity<Object> mockResponse = new ResponseEntity<>(expectedResponse, HttpStatus.OK);

            when(restTemplate.exchange(
                    anyString(),
                    eq(HttpMethod.GET),
                    any(HttpEntity.class),
                    eq(Object.class)
            )).thenReturn(mockResponse);

            ResponseEntity<Object> result = baseClient.get("/test", -1L);
            assertThat(result.getBody()).isEqualTo(expectedResponse);
        }

        @Test
        void handleHttpClientErrorException_WithMalformedJsonInResponse() {
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

            ResponseEntity<Object> result = baseClient.get("/test", 1L);
            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(result.getBody()).isEqualTo(malformedJson.getBytes());
        }

        @Test
        void get_WithHttpStatusCodeException_ReturnsErrorResponse() {
            HttpStatusCodeException exception = mock(HttpStatusCodeException.class);
            when(exception.getStatusCode()).thenReturn(HttpStatus.NOT_FOUND);
            when(exception.getResponseBodyAsByteArray()).thenReturn("Not Found".getBytes());

            when(restTemplate.exchange(
                    eq("/test"),
                    eq(HttpMethod.GET),
                    any(HttpEntity.class),
                    eq(Object.class)
            )).thenThrow(exception);

            ResponseEntity<Object> result = baseClient.get("/test", 1L);
            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            assertThat(result.getBody()).isEqualTo("Not Found".getBytes());
        }
    }

    @Nested
    class PrepareResponseTests {

        @Test
        void prepareGatewayResponse_WithNullResponse_ReturnsInternalServerError() {
            ResponseEntity<Object> result = TestBaseClient.testPrepareGatewayResponse(null);
            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
            assertThat(result.getBody()).asString().contains("Internal server error");
        }

        @Test
        void prepareGatewayResponse_With2xxResponse_ReturnsSameResponse() {
            ResponseEntity<Object> input = new ResponseEntity<>("success", HttpStatus.OK);
            ResponseEntity<Object> result = TestBaseClient.testPrepareGatewayResponse(input);
            assertThat(result.getStatusCode()).isEqualTo(input.getStatusCode());
            assertThat(result.getBody()).isEqualTo(input.getBody());
        }

        @Test
        void prepareGatewayResponse_With4xxResponse_ReturnsSameResponse() {
            ResponseEntity<Object> input = new ResponseEntity<>("error", HttpStatus.NOT_FOUND);
            ResponseEntity<Object> result = TestBaseClient.testPrepareGatewayResponse(input);
            assertThat(result.getStatusCode()).isEqualTo(input.getStatusCode());
            assertThat(result.getBody()).isEqualTo(input.getBody());
        }

        @Test
        void prepareGatewayResponse_WithErrorResponseNoBody_ReturnsResponseBuilder() {
            ResponseEntity<Object> input = new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            ResponseEntity<Object> result = TestBaseClient.testPrepareGatewayResponse(input);
            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(result.getBody()).isNull();
        }
    }

    @Nested
    class ConstructorAndUtilityTests {

        @Test
        void testConstructor_WithRestTemplateOnly() {
            RestTemplate restTemplate = new RestTemplate();
            TestBaseClient client = new TestBaseClient(restTemplate);
            assertThat(client).isNotNull();
        }

        @Test
        void testGetRestTemplate() {
            assertThat(baseClient.getRestTemplate()).isEqualTo(restTemplate);
        }

        @Test
        void testAllMethodVariants() {
            String expectedResponse = "test";
            ResponseEntity<Object> mockResponse = new ResponseEntity<>(expectedResponse, HttpStatus.OK);

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

            var method = BaseClient.class.getDeclaredMethod(
                    "makeAndSendRequest",
                    HttpMethod.class, String.class, Long.class, Map.class, Object.class);
            method.setAccessible(true);

            ResponseEntity<Object> result = (ResponseEntity<Object>) method.invoke(
                    baseClient, HttpMethod.GET, "/test?param1={param1}&param2={param2}", 1L, parameters, null);

            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(result.getBody()).isEqualTo(expectedResponse);
        }
    }
}

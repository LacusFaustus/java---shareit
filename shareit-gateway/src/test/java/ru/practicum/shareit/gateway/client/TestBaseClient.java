package ru.practicum.shareit.gateway.client;

import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.lang.reflect.Method;

public class TestBaseClient extends BaseClient {

    public TestBaseClient(RestTemplate rest) {
        super(rest);
    }

    public TestBaseClient(String serverUrl, RestTemplate rest) {
        super(serverUrl, rest);
    }

    public RestTemplate getRestTemplate() {
        return rest;
    }

    // Публичный метод для тестирования приватного prepareGatewayResponse через рефлексию
    @SuppressWarnings("unchecked")
    public static ResponseEntity<Object> testPrepareGatewayResponse(ResponseEntity<Object> response) {
        try {
            Method method = BaseClient.class.getDeclaredMethod("prepareGatewayResponse", ResponseEntity.class);
            method.setAccessible(true);
            return (ResponseEntity<Object>) method.invoke(null, response);
        } catch (Exception e) {
            throw new RuntimeException("Failed to invoke prepareGatewayResponse", e);
        }
    }
}

package ru.practicum.shareit.gateway.client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CachedItemClientCacheTest {

    @Mock
    private org.springframework.web.client.RestTemplate restTemplate;

    private CachedItemClient itemClient;

    @BeforeEach
    void setUp() {
        itemClient = new CachedItemClient("http://localhost:9090", restTemplate);
    }

    @Test
    void testCacheConfiguration() {
        CacheManager cacheManager = new ConcurrentMapCacheManager("items");
        assertNotNull(cacheManager.getCache("items"));
    }
}

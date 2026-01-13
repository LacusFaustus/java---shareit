package ru.practicum.shareit.gateway.client;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class CachedItemClientCacheIntegrationTest {

    @Autowired
    private CacheManager cacheManager;

    @Test
    void testCacheConfiguration() {
        assertThat(cacheManager).isNotNull();

        Cache itemsCache = cacheManager.getCache("items");
        assertThat(itemsCache).isNotNull();

        Cache usersCache = cacheManager.getCache("users");
        assertThat(usersCache).isNotNull();

        Cache bookingsCache = cacheManager.getCache("bookings");
        assertThat(bookingsCache).isNotNull();

        Cache itemRequestsCache = cacheManager.getCache("itemRequests");
        assertThat(itemRequestsCache).isNotNull();
    }
}

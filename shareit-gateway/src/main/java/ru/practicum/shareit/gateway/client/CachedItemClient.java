package ru.practicum.shareit.gateway.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ru.practicum.shareit.dto.CommentDto;
import ru.practicum.shareit.dto.ItemDto;

import java.util.Map;

@Service
public class CachedItemClient extends ResilientBaseClient {
    private static final String API_PREFIX = "/items";

    @Autowired
    public CachedItemClient(@Value("${shareit.server.url}") String serverUrl, RestTemplate rest) {
        super(serverUrl + API_PREFIX, rest);
    }

    public ResponseEntity<Object> createItem(ItemDto itemDto, Long ownerId) {
        return post("", ownerId, itemDto);
    }

    public ResponseEntity<Object> updateItem(Long itemId, ItemDto itemDto, Long ownerId) {
        return patch("/" + itemId, ownerId, itemDto);
    }

    @Cacheable(value = "items", key = "#itemId + '_' + #userId", unless = "#result == null || #result.getStatusCode().isError()")
    public ResponseEntity<Object> getItemById(Long itemId, Long userId) {
        Map<String, Object> parameters = Map.of("userId", userId);
        return get("/" + itemId + "?userId={userId}", userId, parameters);
    }

    @Cacheable(value = "items", key = "'owner_' + #ownerId", unless = "#result == null || #result.getStatusCode().isError()")
    public ResponseEntity<Object> getAllItemsByOwner(Long ownerId) {
        return get("", ownerId);
    }

    @Cacheable(value = "items", key = "'search_' + #text.hashCode() + '_' + #userId", unless = "#result == null || #result.getStatusCode().isError()")
    public ResponseEntity<Object> searchItems(String text, Long userId) {
        Map<String, Object> parameters = Map.of("text", text, "userId", userId);
        return get("/search?text={text}&userId={userId}", userId, parameters);
    }

    public ResponseEntity<Object> addComment(Long itemId, CommentDto commentDto, Long userId) {
        return post("/" + itemId + "/comment", userId, commentDto);
    }
}

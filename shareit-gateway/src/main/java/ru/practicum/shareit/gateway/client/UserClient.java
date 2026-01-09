package ru.practicum.shareit.gateway.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ru.practicum.shareit.dto.UserDto;

@Slf4j
@Service
public class UserClient extends ResilientBaseClient {
    private static final String API_PREFIX = "/users";

    @Autowired
    public UserClient(@Value("${shareit.server.url}") String serverUrl, RestTemplate rest) {
        super(serverUrl + API_PREFIX, rest);
        log.info("UserClient initialized with URL: {}", serverUrl + API_PREFIX);
    }

    public ResponseEntity<Object> createUser(UserDto userDto) {
        return post("", userDto);
    }

    public ResponseEntity<Object> updateUser(Long userId, UserDto userDto) {
        return patch("/" + userId, userDto);
    }

    public ResponseEntity<Object> getUserById(Long userId) {
        return get("/" + userId);
    }

    public ResponseEntity<Object> getAllUsers() {
        return get("");
    }

    public ResponseEntity<Object> deleteUser(Long userId) {
        return delete("/" + userId);
    }
}

package ru.practicum.shareit.server;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class ShareItServerApplicationTests {

    @Test
    void contextLoads() {
        // Тест проверяет только загрузку контекста
    }
}

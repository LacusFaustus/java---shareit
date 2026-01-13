package ru.practicum.shareit.gateway;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ShareItGatewayAppTest {

    @Test
    void contextLoads() {
        // Этот тест проверяет, что контекст Spring загружается успешно
    }

    @Test
    void main_ShouldStartApplication() {
        // Проверяем, что метод main запускается без исключений
        ShareItGatewayApp.main(new String[]{});
    }
}

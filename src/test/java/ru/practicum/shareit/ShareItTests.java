package ru.practicum.shareit;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import ru.practicum.shareit.config.TestConfig;

@SpringBootTest
@Import(TestConfig.class)
class ShareItTests {

    @Test
    void contextLoads() {
        // This test will pass if the application context loads successfully
    }
}

package ru.practicum.shareit.server;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class ShareItServerAppTest {

    @Test
    void contextLoads(ApplicationContext context) {
        // Проверяем, что контекст Spring успешно загрузился
        assertThat(context).isNotNull();
    }

    @Test
    void mainMethodStartsApplication() {
        // Проверяем, что основной метод можно запустить
        ShareItServerApp.main(new String[]{});
        // Тест проходит, если приложение успешно стартует
        // (в реальности Spring Boot тест уже запускает контекст)
    }
}

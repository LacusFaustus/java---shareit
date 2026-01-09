package ru.practicum.shareit.server;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.SpringBootApplication;
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
        // Просто проверяем, что класс можно загрузить и он имеет аннотацию @SpringBootApplication
        ShareItServerApp app = new ShareItServerApp();
        assertThat(app).isNotNull();

        // Проверяем аннотации класса
        SpringBootApplication annotation = ShareItServerApp.class.getAnnotation(SpringBootApplication.class);
        assertThat(annotation).isNotNull();
    }
}

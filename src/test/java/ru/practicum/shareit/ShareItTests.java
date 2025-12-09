package ru.practicum.shareit;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.NONE, // Без веб-сервера для скорости
        classes = ShareItApp.class
)
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "spring.jpa.open-in-view=false",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.datasource.url=jdbc:h2:mem:testdb;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
        "spring.jpa.show-sql=false",
        "spring.main.lazy-initialization=true" // Ленивая инициализация для скорости
})
class ShareItTests {

    @Test
    void contextLoads() {
        // This test will pass if the application context loads successfully
    }
}

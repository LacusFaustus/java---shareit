package ru.practicum.shareit.server.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import javax.sql.DataSource;
import java.util.UUID;

@TestConfiguration
@Profile("test")
public class TestDatabaseConfig {

    @Bean
    @Primary
    public DataSource dataSource() {
        // Создаем уникальное имя базы данных для каждого тестового запуска
        String uniqueDbName = "testdb_" + UUID.randomUUID().toString().replace("-", "");

        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl("jdbc:h2:mem:" + uniqueDbName +
                ";DB_CLOSE_DELAY=-1" +
                ";DB_CLOSE_ON_EXIT=FALSE" +
                ";MODE=PostgreSQL" +
                ";INIT=CREATE SCHEMA IF NOT EXISTS shareit");
        dataSource.setDriverClassName("org.h2.Driver");
        dataSource.setUsername("sa");
        dataSource.setPassword("");
        dataSource.setMaximumPoolSize(5);
        dataSource.setMinimumIdle(1);
        dataSource.setConnectionTimeout(30000);
        dataSource.setIdleTimeout(600000);
        dataSource.setMaxLifetime(1800000);

        return dataSource;
    }

    @Bean
    public DataSource initializingDataSource() {
        // Этот DataSource используется только для инициализации
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl("jdbc:h2:mem:initdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE;MODE=PostgreSQL");
        dataSource.setDriverClassName("org.h2.Driver");
        dataSource.setUsername("sa");
        dataSource.setPassword("");

        return dataSource;
    }
}

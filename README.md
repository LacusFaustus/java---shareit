# ShareIt Project

Микросервисное приложение для шеринга вещей с архитектурой Gateway-Server.

## Архитектура

Проект разделен на три модуля:

### 1. ShareIt Gateway (порт 8080)
- Валидация входящих запросов
- REST клиенты для общения с сервером
- Обработка ошибок
- Кэширование
- Circuit Breaker для отказоустойчивости
- CORS конфигурация

### 2. ShareIt Server (порт 9090)
- Бизнес-логика приложения
- Работа с базой данных
- Сервисы и репозитории
- Модели данных

### 3. ShareIt DTO
- Общие DTO объекты
- Валидация данных
- Сериализация/десериализация JSON

## Технологии
- Java 21
- Spring Boot 3.2.5
- PostgreSQL / H2
- Maven (многомодульный проект)
- MapStruct
- Lombok
- Resilience4j (Circuit Breaker)
- Caffeine Cache
- JUnit 5
- MockMVC
- Jacoco

## Запуск проекта

### Требования:
- JDK 21
- Maven 3.9+
- Docker (опционально)

### 1. Сборка проекта:
```bash
mvn clean install

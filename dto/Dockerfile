FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /build
# CI скопирует весь проект сюда
COPY . .
RUN mvn clean package -DskipTests -pl shareit-gateway -am

FROM openjdk:21-slim
COPY --from=builder /build/shareit-gateway/target/*.jar /app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]

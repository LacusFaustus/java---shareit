# Multi-stage build
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
# CI скопирует весь проект
COPY . .
RUN mvn clean package -DskipTests -pl shareit-gateway -am

FROM openjdk:21-slim
VOLUME /tmp
COPY --from=build /app/shareit-gateway/target/*.jar app.jar
ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar /app.jar"]

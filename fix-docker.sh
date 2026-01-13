#!/bin/bash
echo "Fixing ALL Dockerfiles..."

# Удалить все старые Dockerfile
find . -name "Dockerfile" -type f -delete

# Создать только два нужных
mkdir -p gateway server tests/gateway tests/server

# Единый правильный Dockerfile
DOCKERFILE_CONTENT='FROM maven:3.8.4-openjdk-11 AS builder
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests

FROM openjdk:11-jre-slim
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]'

echo "$DOCKERFILE_CONTENT" > gateway/Dockerfile
echo "$DOCKERFILE_CONTENT" > server/Dockerfile
echo "$DOCKERFILE_CONTENT" > tests/gateway/Dockerfile  
echo "$DOCKERFILE_CONTENT" > tests/server/Dockerfile

echo "Dockerfiles updated!"

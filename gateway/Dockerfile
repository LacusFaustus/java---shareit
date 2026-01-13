# Multi-stage build - будет собирать jar во время CI
FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /workspace
COPY pom.xml .
COPY shareit-gateway/pom.xml shareit-gateway/
COPY shareit-dto/pom.xml shareit-dto/
RUN mvn dependency:go-offline

COPY . .
RUN mvn clean package -DskipTests -pl shareit-gateway -am

FROM openjdk:21-slim
VOLUME /tmp
COPY --from=builder /workspace/shareit-gateway/target/*.jar app.jar
ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar /app.jar"]

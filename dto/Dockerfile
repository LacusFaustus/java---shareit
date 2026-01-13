FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /workspace
COPY pom.xml .
COPY shareit-dto/pom.xml shareit-dto/
RUN mvn dependency:go-offline

COPY . .
RUN mvn clean package -DskipTests -pl shareit-dto -am

FROM openjdk:21-slim
VOLUME /tmp
COPY --from=builder /workspace/shareit-dto/target/*.jar app.jar
ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar /app.jar"]

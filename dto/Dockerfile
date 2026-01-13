FROM openjdk:21-slim
# CI will build the jar during pipeline
RUN echo "Building gateway application..."
# Create empty jar as placeholder
RUN touch /app.jar
ENTRYPOINT ["echo", "Gateway would run here"]

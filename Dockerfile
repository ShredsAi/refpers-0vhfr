# Build stage
FROM openjdk:17-jdk-slim AS build
WORKDIR /app

# Copy Maven files
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
COPY src src

# Build the application
RUN ./mvnw clean package -DskipTests

# Runtime stage
FROM openjdk:17-jre-slim
WORKDIR /app

# Create non-root user
RUN addgroup --system --gid 1001 appuser
RUN adduser --system --uid 1001 --group appuser
USER appuser

# Copy JAR from build stage
COPY --from=build /app/target/gateway-security-shred-1.0.0.jar app.jar

# Environment variables
ENV JAVA_OPTS="-Xms256m -Xmx512m"

# Expose application port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
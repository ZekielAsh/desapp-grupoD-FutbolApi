# Multi-stage build for Spring Boot application
FROM gradle:8.9-jdk21 AS builder

WORKDIR /app

# Copy gradle files
COPY build.gradle.kts settings.gradle.kts gradlew ./
COPY gradle gradle

# Download dependencies (cached layer)
RUN gradle dependencies --no-daemon || true

# Copy source code
COPY src ./src

# Build application (skip tests for faster builds)
RUN gradle clean bootJar -x test --no-daemon

# Production stage
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Create non-root user for security
RUN addgroup -S spring && adduser -S spring -G spring

# Create logs directory
RUN mkdir -p /app/logs && chown -R spring:spring /app/logs

# Copy JAR from builder
COPY --from=builder /app/build/libs/*.jar /app/app.jar

# Change ownership
RUN chown -R spring:spring /app

# Switch to non-root user
USER spring

# Expose port (Render will use PORT environment variable)
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# Run the application
ENTRYPOINT ["java", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-Dspring.profiles.active=prod", \
    "-jar", \
    "/app/app.jar"]

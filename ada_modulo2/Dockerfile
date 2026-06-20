# ─────────────────────────────────────────────────────────────────────────────
# Stage 1: Build
# ─────────────────────────────────────────────────────────────────────────────
FROM maven:3.9-eclipse-temurin-21-alpine AS build

WORKDIR /workspace/app

# Cache Maven dependencies before copying source code
COPY pom.xml .
RUN mvn dependency:go-offline -q

# Copy source and build (skipping tests — integration tests require containers)
COPY src src
RUN mvn package -DskipTests -q

# ─────────────────────────────────────────────────────────────────────────────
# Stage 2: Runtime image (minimal, non-root)
# ─────────────────────────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine AS runtime

# Create a non-root user for security
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

WORKDIR /app

# Copy the built jar from the build stage
COPY --from=build /workspace/app/target/*.jar app.jar

# Transfer ownership to the non-root user
RUN chown appuser:appgroup app.jar

USER appuser

# Main API port
EXPOSE 8080
# Actuator / management port
EXPOSE 8090

# JVM flags optimised for container environments (Java 21)
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+UseZGC -Djava.security.egd=file:/dev/./urandom"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]

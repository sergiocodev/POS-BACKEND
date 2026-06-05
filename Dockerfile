# ==========================================
# STAGE 1: Build
# ==========================================
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

# Copy Maven wrapper and pom.xml first (layer caching)
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

# Download dependencies (cached if pom.xml doesn't change)
RUN ./mvnw dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the JAR (skip tests for faster CI/CD builds)
RUN ./mvnw package -DskipTests -B

# ==========================================
# STAGE 2: Runtime
# ==========================================
FROM eclipse-temurin:21-jre-alpine AS runtime

WORKDIR /app

# Create non-root user for security
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Create uploads directory and set ownership
RUN mkdir -p /app/uploads && chown -R appuser:appgroup /app

# Copy the built JAR from builder stage
COPY --from=builder --chown=appuser:appgroup /app/target/*.jar app.jar

USER appuser

EXPOSE 8080

# JVM tuning for containers
ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar"]

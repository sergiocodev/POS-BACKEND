# ==========================================
# STAGE 1: Build
# ==========================================
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

# Copiar archivos de Maven primero para aprovechar la caché de capas
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

# Descargar dependencias (se cachea si pom.xml no cambia)
RUN ./mvnw dependency:go-offline -B

# Copiar el código fuente y compilar
COPY src ./src
RUN ./mvnw package -DskipTests -B

# ==========================================
# STAGE 2: Runtime
# ==========================================
FROM eclipse-temurin:21-jre-alpine AS runtime

# Crear usuario no-root por seguridad
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

WORKDIR /app

# Copiar solo el JAR generado desde la etapa de build
COPY --from=builder /app/target/*.jar app.jar

# Crear directorio para uploads y asignar permisos
RUN mkdir -p /app/uploads && chown -R appuser:appgroup /app

USER appuser

# Puerto expuesto (Railway inyecta $PORT dinámicamente)
EXPOSE 8080

# Health check usando el puerto dinámico de Railway
HEALTHCHECK --interval=30s --timeout=10s --start-period=90s --retries=5 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:${PORT:-8080}/actuator/health || exit 1

# Shell form para permitir expansión de variables de entorno ($PORT de Railway)
ENTRYPOINT ["sh", "-c", "java \
  -XX:+UseContainerSupport \
  -XX:MaxRAMPercentage=75.0 \
  -Djava.security.egd=file:/dev/./urandom \
  -Dserver.port=${PORT:-8080} \
  -jar app.jar"]

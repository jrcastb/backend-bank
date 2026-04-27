# Stage 1: Build
FROM gradle:8.14.3-jdk21-alpine AS build

ARG GRADLE_VERSION=8.14.3

WORKDIR /workspace

# Copiar archivos de configuración
COPY settings.gradle build.gradle ./
COPY gradle gradle/
COPY src src/

# Build con cache de dependencias
RUN gradle --no-daemon clean bootJar -x test

# Stage 2: Runtime
FROM eclipse-temurin:21-jre-alpine

LABEL maintainer="Devsu"
LABEL description="Backend Bank - Spring Boot 4.0 Application"

WORKDIR /app

# Copiar JAR from build stage
COPY --from=build /workspace/build/libs/*.jar app.jar

# Pequeñas mejoras de seguridad y performance
RUN addgroup -S backend && adduser -S -G backend backend && chown -R backend:backend /app
USER backend

# Health check (opcional, pero recomendado)
HEALTHCHECK --interval=30s --timeout=5s --retries=3 --start-period=40s \
    CMD java -cp app.jar org.springframework.boot.loader.JarLauncher \
             org.springframework.boot.actuate.health.HealthEndpoint

EXPOSE 8080

# Configuración JVM optimizada para contenedores
ENV JAVA_OPTS="-Xmx512m -Xms256m -XX:+UseG1GC -XX:+UseStringDeduplication"

ENTRYPOINT ["java", "-jar", "/app/app.jar"]

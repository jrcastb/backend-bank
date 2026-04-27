# 🐳 Guía Docker

Instrucciones detalladas para trabajar con Docker Compose, construir imágenes y ejecutar contenedores.

---

## 📋 Tabla de Contenidos

- [Quick Start](#quick-start)
- [Comandos Esenciales](#comandos-esenciales)
- [Estructura de Servicios](#estructura-de-servicios)
- [Construir Imagen Manualmente](#construir-imagen-manualmente)
- [Conectar Frontend](#conectar-frontend)
- [Monitoreo y Logs](#monitoreo-y-logs)
- [Troubleshooting](#troubleshooting)

---

## ⚡ Quick Start

### 1. Levantar Todos los Servicios (Recomendado)

```bash
docker-compose up -d
```

**¿Qué sucede?**
- ✅ Compila la imagen (si no existe)
- ✅ Levanta MySQL 8.4
- ✅ Espera a que MySQL esté "healthy"
- ✅ Levanta Backend en puerto 8080

**Verificar estado:**
```bash
docker-compose ps
```

**Output esperado:**
```
NAME                IMAGE              COMMAND                  STATUS
backend-bank-mysql   mysql:8.4          "docker-entrypoint..."  Up (healthy)
backend-bank-app     backend-bank:latest "java -jar /app/..."   Up (healthy)
```

### 2. Ver Logs

```bash
# Todos los servicios
docker-compose logs

# Solo backend
docker-compose logs backend-bank

# Seguir logs en tiempo real
docker-compose logs -f backend-bank
```

### 3. Detener Servicios

```bash
# Pausar (sin eliminar volúmenes)
docker-compose stop

# Eliminar contenedores
docker-compose down

# Eliminar TODO (⚠️ incluyendo datos)
docker-compose down -v
```

### 4. Probar Backend

```bash
# Health check
curl http://localhost:8080/actuator/health

# Documentación API
curl http://localhost:8080/swagger-ui/index.html

# Desde browser
open http://localhost:8080/swagger-ui/index.html
```

---

## 🔧 Comandos Esenciales

### Estado de Servicios

```bash
# Ver estado actual
docker-compose ps

# Ver logs detallados
docker-compose logs --tail=50 backend-bank

# Ver eventos en tiempo real
docker events
```

### Control de Servicios

```bash
# Levantar
docker-compose up -d

# Levantar sin cache (rebuild)
docker-compose up -d --build

# Levantar sin detach (ver logs en terminal)
docker-compose up

# Detener
docker-compose stop

# Reiniciar
docker-compose restart backend-bank

# Eliminar
docker-compose down
```

### MySQL - Útil para Debugging

```bash
# Conectar a MySQL desde terminal
docker exec -it backend-bank-mysql mysql -ubackend_user -pbackend_pass backend_bank

# Ver bases de datos
SHOW DATABASES;

# Ver tablas
USE backend_bank;
SHOW TABLES;

# Ver datos
SELECT * FROM client;

# Ver migraciones ejecutadas
SELECT * FROM flyway_schema_history;
```

### Backend - Ejecutar Comando en Contenedor

```bash
# Entrar al contenedor
docker exec -it backend-bank-app /bin/sh

# Ver archivos
docker exec backend-bank-app ls -la /app/

# Ver logs dentro del contenedor
docker exec backend-bank-app tail -f /app/logs/spring.log
```

---

## 🏗️ Estructura de Servicios

### Servicio MySQL

```yaml
mysql:
  image: mysql:8.4
  container_name: backend-bank-mysql
  ports:
    - "3306:3306"
  environment:
    MYSQL_DATABASE: backend_bank
    MYSQL_USER: backend_user
    MYSQL_PASSWORD: backend_pass
```

**Acceso:**
- Host: `localhost:3306` (desde tu máquina)
- Usuario: `backend_user`
- Contraseña: `backend_pass`
- Base de datos: `backend_bank`

**Health Check:**
- Intervalo: 10s
- Timeout: 5s
- Reintentos: 10

### Servicio Backend

```yaml
backend-bank:
  build:
    context: .
    dockerfile: Dockerfile
  container_name: backend-bank-app
  ports:
    - "8080:8080"
  depends_on:
    mysql:
      condition: service_healthy
```

**Acceso:**
- URL: `http://localhost:8080`
- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- Health: `http://localhost:8080/actuator/health`

**Health Check:**
- Intervalo: 30s
- Timeout: 5s
- Reintentos: 3
- Espera inicial: 40s

---

## 🔨 Construir Imagen Manualmente

### Build Completo

```bash
# Opción 1: Docker Compose (automático)
docker-compose build

# Opción 2: Dockerfile directo
docker build -t backend-bank:latest .

# Opción 3: Con argumentos
docker build --build-arg GRADLE_VERSION=8.14.3 -t backend-bank:latest .
```

### Verificar Imagen

```bash
# Ver imágenes locales
docker images

# Ver información detallada
docker inspect backend-bank:latest

# Ver capas
docker image history backend-bank:latest
```

### Publicar a Registro

```bash
# Tag para Docker Hub
docker tag backend-bank:latest your-registry/backend-bank:latest

# Push
docker push your-registry/backend-bank:latest

# Pull
docker pull your-registry/backend-bank:latest
```

---

## 🔌 Conectar Frontend

### Opción 1: Frontend Corriendo Localmente

Tu frontend (React, Vue, Angular) corre en `http://localhost:3000`.

**Configuración:**

```javascript
// .env del frontend
REACT_APP_API_URL=http://localhost:8080
```

```javascript
// En tus requests
const response = await fetch('http://localhost:8080/api/clientes');
```

**Nota:** El backend debe tener CORS habilitado para `http://localhost:3000`.

### Opción 2: Frontend en Contenedor

```yaml
# Agregar a docker-compose.yml
frontend:
  image: frontend-app:latest  # Tu imagen frontend
  container_name: frontend-app
  ports:
    - "3000:3000"
  environment:
    REACT_APP_API_URL: http://localhost:8080
  depends_on:
    - backend-bank
  networks:
    - backend-bank-network
```

Levantar:
```bash
docker-compose up -d
```

**Dentro del contenedor, usa:**
```javascript
const response = await fetch('http://backend-bank:8080/api/clientes');
```

### Opción 3: Mismo Contenedor (Nginx)

```docker
# Dockerfile.prod
FROM node:18 AS frontend
WORKDIR /app
COPY . .
RUN npm install && npm run build

FROM nginx:alpine
COPY --from=frontend /app/build /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
```

---

## 📊 Monitoreo y Logs

### Logs en Tiempo Real

```bash
# Todos los servicios
docker-compose logs -f

# Específico
docker-compose logs -f backend-bank

# Últimas 50 líneas
docker-compose logs --tail=50 backend-bank

# Con timestamps
docker-compose logs -f --timestamps backend-bank
```

### Métricas y Performance

```bash
# CPU, memoria, I/O
docker stats

# Específico
docker stats backend-bank-mysql backend-bank-app

# Uso de disco
docker system df

# Sin actualización en tiempo real
docker stats --no-stream
```

### Inspeccionar Volúmenes

```bash
# Ver volúmenes
docker volume ls

# Ver ubicación en host
docker volume inspect backend-bank_mysql_data

# Acceder a archivos del volumen
docker run -v backend-bank_mysql_data:/data -it alpine ls /data
```

---

## 🐛 Troubleshooting

### Error: `Cannot connect to Docker daemon`

**Causa:** Docker no está corriendo.

**Solución:**
```bash
# Mac: Abre Docker Desktop
open /Applications/Docker.app

# Linux: Inicia el servicio
sudo systemctl start docker

# Windows: Abre Docker Desktop
```

### Error: `Port 8080 is already allocated`

**Causa:** El puerto 8080 ya está en uso.

**Solución:**
```bash
# Opción 1: Cambiar puerto en docker-compose.yml
ports:
  - "8081:8080"  # Ahora acceso en 8081

# Opción 2: Matar proceso en 8080
# Mac/Linux
lsof -i :8080 | tail -1 | awk '{print $2}' | xargs kill -9

# Windows
netstat -ano | findstr :8080
taskkill /PID <PID> /F
```

### Error: `MySQL connection refused`

**Causa:** MySQL no está healthy.

**Solución:**
```bash
# Ver estado
docker-compose ps mysql

# Si no es "healthy", ver logs
docker-compose logs mysql

# Reiniciar
docker-compose restart mysql

# Limpiar y reiniciar
docker-compose down -v
docker-compose up -d mysql
```

### Backend no se conecta a MySQL

**Causa:** Host incorrecto o credenciales.

**Verificar:**
```bash
# Ver variables de entorno
docker-compose config | grep -A 10 "backend-bank:"

# Conectar manualmente desde backend
docker exec backend-bank-app \
  java -cp /app/app.jar \
  org.springframework.boot.loader.PropertiesLauncher
```

### Imagen muy grande (> 1GB)

**Causa:** Caché de Gradle en imagen.

**Solución (mejorar Dockerfile):**
```dockerfile
# En lugar de COPY . .
COPY .gradle .gradle/
RUN gradle downloadDependencies  # Pre-download

# Incluir solo fuentes necesarias
COPY src src/
COPY build.gradle settings.gradle ./
COPY gradle gradle/
```

### Base de datos vacía después de reiniciar

**Causa:** Volumen no persistido correctamente.

**Verificar:**
```bash
# Ver volumen
docker volume inspect backend-bank_mysql_data

# Si está vacío, crear backup antes de borrar
docker exec backend-bank-mysql mysqldump -ubackend_user -pbackend_pass backend_bank > backup.sql

# Limpiar
docker-compose down -v

# Restaurar
docker-compose up -d
docker exec -i backend-bank-mysql mysql -ubackend_user -pbackend_pass backend_bank < backup.sql
```

---

## 🚀 Deployment a Producción

### Consideraciones

```dockerfile
# Production-optimized Dockerfile
FROM gradle:8.14.3-jdk21-alpine AS build
WORKDIR /workspace
COPY . .
RUN gradle --no-daemon clean bootJar -Dspring.profiles.active=docker

FROM eclipse-temurin:21-jre-alpine
RUN addgroup -S appuser && adduser -S -G appuser appuser
USER appuser
COPY --from=build /workspace/build/libs/*.jar app.jar
HEALTHCHECK --interval=30s --timeout=5s --retries=3 \
    CMD wget --quiet --tries=1 --spider http://localhost:8080/actuator/health || exit 1
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### Docker Compose Producción

```yaml
# docker-compose.prod.yml
version: "3.9"

services:
  mysql:
    image: mysql:8.4
    restart: unless-stopped
    environment:
      MYSQL_DATABASE: backend_bank
      MYSQL_USER: ${MYSQL_USER}
      MYSQL_PASSWORD: ${MYSQL_PASSWORD}
      MYSQL_ROOT_PASSWORD: ${MYSQL_ROOT_PASSWORD}
    volumes:
      - mysql_data:/var/lib/mysql
    networks:
      - backend-bank-network

  backend-bank:
    image: your-registry/backend-bank:latest
    restart: unless-stopped
    depends_on:
      mysql:
        condition: service_healthy
    environment:
      SPRING_PROFILES_ACTIVE: docker
      JAVA_OPTS: "-Xmx1024m -Xms512m -XX:+UseG1GC"
    ports:
      - "8080:8080"
    networks:
      - backend-bank-network

volumes:
  mysql_data:
    driver: local

networks:
  backend-bank-network:
    driver: bridge
```

**Usar:**
```bash
docker-compose -f docker-compose.prod.yml up -d
```

---

## 📚 Referencias

- [Docker Documentation](https://docs.docker.com/)
- [Docker Compose Reference](https://docs.docker.com/compose/compose-file/)
- [MySQL Docker Image](https://hub.docker.com/_/mysql/)
- [OpenJDK Docker Image](https://hub.docker.com/_/eclipse-temurin/)

---

**Última actualización:** Abril 2024


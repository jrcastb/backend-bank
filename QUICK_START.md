# ⚡ Quick Start - Backend Bank

Instrucciones rápidas para levantar el proyecto en menos de 5 minutos.

---

## 🚀 Opción 1: Ejecución Local (Recomendado)

### Requisitos
- Java 21+
- MySQL 8.4 (local o Docker)

### Pasos

```bash
# 1. Clonar y navegar
git clone <repository-url>
cd backend-bank

# 2. Crear archivo .env
cp .env.example .env

# 3. Levantar MySQL (en otra terminal)
docker-compose up -d mysql

# 4. Ejecutar aplicación
./gradlew bootRun --args='--spring.profiles.active=local'
```

**Acceder a:** http://localhost:8080/swagger-ui/index.html

---

## 🐳 Opción 2: Docker (Completo)

### Requisitos
- Docker + Docker Compose

### Pasos

```bash
# 1. Crear .env
cp .env.example .env

# 2. Levantar servicios
docker-compose up -d

# 3. Esperar ~30 segundos a que MySQL esté listo

# 4. Acceder
# Backend API: http://localhost:8080
# Swagger: http://localhost:8080/swagger-ui/index.html
```

---

## 🔨 Script Automático (Linux/Mac)

```bash
# Hacer ejecutable
chmod +x setup.sh

# Ejecutar
./setup.sh

# Seguir el menú interactivo
```

---

## 📝 Configurar IntelliJ IDEA

1. **File → Open** → Selecciona `backend-bank`
2. **File → Project Structure**
   - SDK: Java 21
   - Language Level: 21
3. Espera a que sincronice Gradle (1-2 minutos)
4. En la barra superior: **Add Configuration** → **Spring Boot**
   - Main class: `com.devsu.backendbank.BackendBankApplication`
   - Active profiles: `local`
5. Presiona ▶️ para ejecutar

---

## 🧪 Ejecutar Tests

```bash
# Todos los tests
./gradlew test

# Solo unitarios
./gradlew test --tests "*" --excludes "*IT"

# Solo integración (requiere Docker)
./gradlew test --tests "*IT"
```

---

## 📚 Documentación

- **[README.md](README.md)** - Guía completa
- **[DOCKER.md](DOCKER.md)** - Docker Compose y comandos
- **[TESTING.md](TESTING.md)** - Tests unitarios e integración
- **[INTELLIJ_SETUP.md](INTELLIJ_SETUP.md)** - Configuración IntelliJ detailed

---

## 🆘 Problemas Comunes

### Puerto 8080 en uso
```bash
./gradlew bootRun --args='--server.port=8081'
```

### MySQL no responde
```bash
docker-compose down -v
docker-compose up -d mysql
```

### Gradle cache corrupto
```bash
rm -rf ~/.gradle/caches
./gradlew clean build -x test
```

---

## ✅ Validar Instalación

```bash
# Config
java -version      # Debe ser 21+
mvn --version      # Si usas Maven

# Compilación
./gradlew clean compileJava

# API
curl http://localhost:8080/actuator/health
# Esperado: {"status":"UP"}
```

---

**¿Necesitas más ayuda?** Consulta la documentación en `README.md` o `DOCKER.md`.


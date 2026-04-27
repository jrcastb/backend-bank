# Backend Bank 🏦

Aplicación backend para un sistema de gestión bancaria desarrollada con **Spring Boot 4.0**, **Java 21**, **MySQL 8.4** y arquitectura Clean Code.

---

## 📋 Tabla de Contenidos

- [Requisitos Previos](#requisitos-previos)
- [Instalación Local](#instalación-local)
- [Configuración en IntelliJ IDEA](#configuración-en-intellij-idea)
- [Perfiles de Configuración](#perfiles-de-configuración)
- [Ejecución del Proyecto](#ejecución-del-proyecto)
- [Docker y Docker Compose](#docker-y-docker-compose)
- [Estructura del Proyecto](#estructura-del-proyecto)
- [Testing](#testing)
- [API Documentation](#api-documentation)
- [Contribuciones](#contribuciones)

---

## 📦 Requisitos Previos

### Para Desarrollo Local

- **Java 21+** ([Descargar OpenJDK 21](https://www.oracle.com/java/technologies/downloads/#java21))
- **Gradle 8.14+** (incluido en el proyecto con `./gradlew`)
- **MySQL 8.4+** (local o en contenedor)
- **IntelliJ IDEA Community/Ultimate** o tu IDE favorito
- **Git**

### Para Docker

- **Docker 20.10+**
- **Docker Compose 2.0+**

---

## 🚀 Instalación Local

### 1. Clonar el Repositorio

```bash
git clone <repository-url>
cd backend-bank
```

### 2. Copiar Archivo de Configuración

```bash
cp .env.example .env
```

El archivo `.env` contiene variables de entorno para la conexión a MySQL. Valores por defecto:

```env
MYSQL_DATABASE=backend_bank
MYSQL_USER=backend_user
MYSQL_PASSWORD=backend_pass
MYSQL_ROOT_PASSWORD=root_pass
MYSQL_PORT=3306
```

### 3. Preparar Base de Datos MySQL

#### Opción A: MySQL en Contenedor Docker (Recomendado)

```bash
# Levanta solo la base de datos MySQL
docker-compose up -d mysql
```

Verificar que MySQL está corriendo:
```bash
docker-compose ps
# Deberías ver: backend-bank-mysql ... Up (healthy)
```

#### Opción B: MySQL en tu Máquina Local

Asegúrate de que MySQL está instalado y corriendo:

```bash
# En macOS con Homebrew
brew services start mysql-server
mysql -u root -p

# En Linux
sudo systemctl start mysql

# En Windows
net start MySQL80  # O el nombre del servicio correspondiente
```

Crear base de datos y usuario:

```sql
CREATE DATABASE backend_bank;
CREATE USER 'backend_user'@'localhost' IDENTIFIED BY 'backend_pass';
GRANT ALL PRIVILEGES ON backend_bank.* TO 'backend_user'@'localhost';
FLUSH PRIVILEGES;
```

### 4. Compilar el Proyecto

```bash
# Compilar y validar dependencias
./gradlew clean build -x test

# o sin tests (más rápido)
./gradlew clean compileJava
```

### 5. Ejecutar la Aplicación

```bash
./gradlew bootRun --args='--spring.profiles.active=local'
```

La aplicación estará disponible en: **http://localhost:8080**

---

## 🔧 Configuración en IntelliJ IDEA

### Paso 1: Importar el Proyecto

1. En IntelliJ: **File → Open** y selecciona la carpeta `backend-bank`
2. Elige **Open as Project**
3. Espera a que se sincronice con Gradle

### Paso 2: Configurar el JDK

1. **File → Project Structure → Project**
2. En **SDK**: Selecciona Java 21
   - Si no lo ves, haz clic en **Edit → Add SDK → Download JDK**
   - Elige **Version: 21**
   - Proveedor: **Eclipse Temurin** o **Oracle**

### Paso 3: Configurar Gradle

1. **File → Settings → Build, Execution, Deployment → Build Tools → Gradle**
2. En **Gradle JVM**: Selecciona Java 21
3. En **Build and run using**: Elige **Gradle** (no IntelliJ IDEA)

### Paso 4: Configurar el Perfil Local

#### Opción A: Run Configuration GUI (Recomendado)

1. En la barra superior, haz clic en **Add Configuration** (al lado de ▶️)
2. Haz clic en **+** → Selecciona **Spring Boot**
3. Configura:
   - **Name**: `Backend Bank (Local)`
   - **Main class**: `com.devsu.backendbank.BackendBankApplication`
   - **Active profiles**: `local` (esto es lo importante)
   - **VM options**: (opcional)
     ```
     -Dspring.profiles.active=local
     ```

4. Haz clic en **OK**
5. Ahora selecciona esta configuración en el dropdown y presiona **▶️**

#### Opción B: Archivo `run.xml` (Alternativo)

Crea/edita `.idea/runConfigurations/Backend_Bank_Local.xml`:

```xml
<component name="ProjectRunConfigurationManager">
  <configuration default="false" name="Backend Bank (Local)" type="SpringBootApplicationConfigurationType">
    <option name="SPRING_BOOT_MAIN_CLASS" value="com.devsu.backendbank.BackendBankApplication" />
    <envs>
      <env name="SPRING_PROFILES_ACTIVE" value="local" />
      <env name="MYSQL_HOST" value="localhost" />
      <env name="MYSQL_PORT" value="3306" />
      <env name="MYSQL_DATABASE" value="backend_bank" />
      <env name="MYSQL_USER" value="backend_user" />
      <env name="MYSQL_PASSWORD" value="backend_pass" />
    </envs>
    <method v="2" />
  </configuration>
</component>
```

### Paso 5: Variables de Entorno (Opcional en IntelliJ)

Si deseas que IntelliJ maneje las variables de entorno:

1. En la Run Configuration, ve a **Environment variables**
2. Agrega:
   ```
   MYSQL_HOST=localhost
   MYSQL_PORT=3306
   MYSQL_DATABASE=backend_bank
   MYSQL_USER=backend_user
   MYSQL_PASSWORD=backend_pass
   ```

---

## 📝 Perfiles de Configuración

El proyecto incluye 3 perfiles predefinidos:

| Perfil   | Uso                     | Base de Datos | SQL Debug | Ubicación           |
|----------|------------------------|---------------|-----------|---------------------|
| `local`  | Desarrollo local       | MySQL local   | Enabled   | `application-local.yml` |
| `docker` | Contenedor Docker      | MySQL docker  | Disabled  | `application-docker.yml` |
| `test`   | Tests de integración   | MySQL TC      | Enabled   | `application-test.yml`  |

### Cambiar Perfil

```bash
# Línea de comandos
./gradlew bootRun --args='--spring.profiles.active=local'

# Variable de entorno
export SPRING_PROFILES_ACTIVE=local
./gradlew bootRun

# IntelliJ: Ver sección anterior
```

---

## ▶️ Ejecución del Proyecto

### Desarrollo Local (Recomendado)

```bash
# Terminal
./gradlew bootRun --args='--spring.profiles.active=local'

# O desde IntelliJ: Usa la Run Configuration "Backend Bank (Local)"
```

**Requisitos previos:**
- ✅ MySQL 8.4+ corriendo en `localhost:3306`
- ✅ Base de datos `backend_bank` creada
- ✅ Usuario `backend_user` con credenciales correctas

**Logs esperados:**
```
...
2024-04-27 10:15:23.456 INFO  : Starting BackendBankApplication
2024-04-27 10:15:25.123 INFO  : Flyway migrations: SUCCESS (3 migrations applied)
2024-04-27 10:15:26.789 INFO  : Tomcat started on port 8080
...
```

**Validar que funciona:**
```bash
curl http://localhost:8080/actuator/health
# Deberías recibir:
# {"status":"UP"}
```

---

## 🐳 Docker y Docker Compose

### Opción 1: Docker Compose (Recomendado)

Levanta la aplicación completa (Backend + MySQL) en un solo comando:

```bash
# Levantar servicios
docker-compose up -d

# Ver logs
docker-compose logs -f backend-bank

# Detener
docker-compose down

# Limpiar volúmenes (⚠️ esto elimina datos)
docker-compose down -v
```

**Estructura de servicios:**

- **MySQL** (`backend-bank-mysql`)
  - Host: `mysql` (dentro de Docker network)
  - Puerto externo: `3306`
  - Base de datos: `backend_bank`

- **Backend** (`backend-bank-app`)
  - Host: `backend-bank` 
  - Puerto externo: `8080`
  - Perfil activo: `docker`

**URLs de acceso desde tu máquina:**

```
Backend API: http://localhost:8080
MySQL: localhost:3306 (usuario: backend_user, contraseña: backend_pass)
```

### Opción 2: Compilar Manual

```bash
# 1. Compilar JAR
./gradlew clean bootJar

# 2. Compilar imagen Docker
docker build -t backend-bank:latest .

# 3. Ejecutar con Docker Compose (solo backend, MySQL externo)
docker-compose up -d backend-bank

# O ejecutar directamente
docker run -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=docker \
  -e MYSQL_HOST=host.docker.internal \
  -e MYSQL_PORT=3306 \
  -e MYSQL_DATABASE=backend_bank \
  -e MYSQL_USER=backend_user \
  -e MYSQL_PASSWORD=backend_pass \
  backend-bank:latest
```

### Variables de Entorno Docker

Personaliza el comportamiento editando `.env` o directamente en `docker-compose.yml`:

```yaml
environment:
  MYSQL_DATABASE: backend_bank      # Nombre de BD
  MYSQL_USER: backend_user          # Usuario de conexión
  MYSQL_PASSWORD: backend_pass      # Contraseña
  MYSQL_ROOT_PASSWORD: root_pass    # Contraseña root
  SERVER_PORT: 8080                 # Puerto del backend
  SPRING_PROFILES_ACTIVE: docker    # Perfil activo
```

### Validar que Docker está corriendo

```bash
# Ver servicios
docker-compose ps

# Ver logs
docker-compose logs

# Verificar health de MySQL
docker-compose ps mysql
# Deberías ver: (healthy)

# Probar conexión al backend
curl http://localhost:8080/actuator/health
```

### Detener y Limpiar

```bash
# Detener servicios (mantiene volúmenes)
docker-compose stop

# Detener y eliminar contenedores
docker-compose down

# Eliminar todo incluyendo datos (⚠️)
docker-compose down -v

# Eliminar imagen construida
docker image rm backend-bank-mysql
docker image rm backend-bank-app
```

---

## Integración con Frontend

Si tienes un frontend que necesita conectarse a este backend:

### CORS Configuration

El backend está configurado con Spring Security. Para habilitar acceso desde el frontend:

**Frontend running on:** `http://localhost:3000` (o tu puerto)

**Backend es accesible en:** `http://localhost:8080`

**Configuración CORS en el backend** (si es necesario):

Edita `src/main/java/com/devsu/backendbank/infrastructure/config/SecurityConfig.java`:

```java
@Bean
public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
        .cors(cors -> cors.configurationSource(request -> {
            CorsConfiguration config = new CorsConfiguration();
            config.setAllowedOrigins(List.of(
                "http://localhost:3000",      // Frontend local
                "http://localhost:3001"       // Puerto alternativo
            ));
            config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
            config.setAllowedHeaders(List.of("*"));
            config.setAllowCredentials(true);
            return config;
        }))
        .csrf(csrf -> csrf.disable())
        ...;
    return http.build();
}
```

**Prueba la conexión desde el frontend:**

```javascript
// vanilla JS
fetch('http://localhost:8080/api/clientes')
  .then(res => res.json())
  .then(data => console.log(data))
  .catch(err => console.error('Error:', err));
```

---

## 📊 Estructura del Proyecto

```
backend-bank/
├── src/
│   ├── main/
│   │   ├── java/com/devsu/backendbank/
│   │   │   ├── application/          # Use cases y servicios
│   │   │   ├── domain/               # Entidades de dominio
│   │   │   ├── infrastructure/       # Repositorios, controllers, config
│   │   │   └── BackendBankApplication.java
│   │   └── resources/
│   │       ├── application.yml       # Config general
│   │       ├── application-local.yml # Perfil LOCAL
│   │       ├── application-docker.yml # Perfil DOCKER
│   │       └── db/migration/         # Scripts Flyway
│   └── test/
│       ├── java/com/devsu/backendbank/
│       │   ├── application/service/  # Tests unitarios servicios
│       │   └── *RepositoryMySqlIT.java # Tests integración
│       └── resources/
│           └── application-test.yml  # Perfil TEST
├── docker-compose.yml    # Orquestación Docker
├── Dockerfile            # Build multi-etapa
├── build.gradle          # Dependencias Gradle
├── .env.example          # Variables de entorno
└── README.md             # Este archivo
```

---

## ✅ Testing

### Tests Unitarios

```bash
# Ejecutar todos los tests unitarios
./gradlew test

# Ejecutar un test específico
./gradlew test --tests TransactionServiceTest

# Con reporte detallado
./gradlew test --reportsDir=build/reports/tests
```

**Suites disponibles:**
- `ClientServiceTest` (10 tests)
- `AccountServiceTest` (12 tests)
- `TransactionServiceTest` (18 tests)
- `ReportServiceTest` (14 tests)

### Tests de Integración

```bash
# Ejecutar tests de integración (requiere Docker o MySQL)
./gradlew test --tests "*IT"

# Tests específicos
./gradlew test --tests TransactionRepositoryMySqlIT
./gradlew test --tests AccountRepositoryMySqlIT
```

**Nota:** Los tests de integración usan **Testcontainers** con MySQL en contenedor Docker.

### Cobertura de Tests

```bash
# Generar reporte de cobertura (requiere JaCoCo)
./gradlew jacocoTestReport

# Ver reporte
open build/reports/jacoco/test/html/index.html
```

---

## 📡 API Documentation

### Swagger UI

Accede a la documentación interactiva en:

```
http://localhost:8080/swagger-ui/index.html
```

### OpenAPI YAML

Descarga la especificación en:

```
http://localhost:8080/v3/api-docs
http://localhost:8080/v3/api-docs.yaml
```

### Health Check

```bash
curl http://localhost:8080/actuator/health
```

### Métricas Prometheus

```bash
curl http://localhost:8080/actuator/metrics
```

---

## 🔐 Seguridad

### Credenciales por Defecto

El proyecto viene con Spring Security habilitado:

- **Usuario:** `admin`
- **Contraseña:** `admin123`

**⚠️ IMPORTANTE:** Cambia estas credenciales en producción editando:

```yaml
# application.yml
spring:
  security:
    user:
      name: ${SECURITY_USER:admin}          # Cambia aquí
      password: ${SECURITY_PASSWORD:admin123} # Cambia aquí
```

---

## 🛠️ Solución de Problemas

### Error: `Connection refused` a MySQL

```bash
# Verificar que MySQL está corriendo
docker-compose ps mysql

# Si no, levantarlo
docker-compose up -d mysql

# Ver logs
docker-compose logs mysql
```

### Error: `Column 'created_at' cannot be null`

Asegúrate de que:
1. ✅ Flyway migrations han ejecutado (`migration/*.sql`)
2. ✅ Auditoría JPA está habilitada (`@EnableJpaAuditing`)
3. ✅ Variables de entorno están correctas

### Puerto 8080 ya en uso

```bash
# Opción 1: Especificar puerto diferente
./gradlew bootRun --args='--server.port=8081'

# Opción 2: Matar proceso en puerto 8080
# En Linux/Mac
lsof -ti:8080 | xargs kill -9

# En Windows
netstat -ano | findstr :8080
taskkill /PID <PID> /F
```

### Tests fallan con `Testcontainers`

```bash
# Verificar que Docker está corriendo
docker ps

# Si no:
docker-compose up -d

# Ejecutar tests sin Docker
./gradlew test -x integrationTest
```

---

## 📚 Referencias

- [Spring Boot 4.0 Documentation](https://spring.io/projects/spring-boot)
- [Spring Data JPA](https://spring.io/projects/spring-data-jpa)
- [Flyway Migrations](https://flywaydb.org/)
- [Docker Documentation](https://docs.docker.com/)
- [Testcontainers](https://testcontainers.com/)

---

## 📄 Licencia

Este proyecto es propiedad de Devsu.

---

## 👥 Contribuciones

Para contribuir al proyecto:

1. Crea un branch: `git checkout -b feature/my-feature`
2. Commit: `git commit -am 'Add new feature'`
3. Push: `git push origin feature/my-feature`
4. Abre un Pull Request

---

## 📞 Soporte

Para reportar issues o pedir ayuda, contacta al equipo de desarrollo.

**Última actualización:** Abril 2024


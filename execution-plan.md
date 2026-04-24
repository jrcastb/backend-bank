# Plan de Ejecucion - Prueba Tecnica Backend (Spring Boot)

## Objetivo
Construir una API bancaria profesional en Java + Spring Boot con enfoque senior: arquitectura limpia por capas, reglas de negocio robustas, migraciones versionadas con Flyway, pruebas automatizadas de endpoints, despliegue en Docker y entregables completos (`BaseDatos.sql`, coleccion Postman, documentacion y OpenAPI).

## Alcance confirmado (Backend)
- CRUD completo para `clientes`, `cuentas`, `movimientos`.
- Reglas de negocio de movimientos:
  - Credito = valor positivo.
  - Debito = valor negativo.
  - Actualizacion de saldo por transaccion.
  - Mensaje `Saldo no disponible` cuando no exista saldo para debito.
  - Limite diario de retiro: `1000`.
  - Mensaje `Cupo diario Excedido` cuando supere tope diario.
- Reporte por cliente y rango de fechas en JSON y PDF (base64).
- Manejo centralizado de excepciones.
- Minimo 2 pruebas unitarias de endpoints (se recomienda mas para destacar).
- Docker para ejecucion local y evidencia de despliegue.

---

## Checklist maestro (vision ejecutiva)
- [ ] 1. Definir arquitectura tecnica y criterios de calidad.
- [ ] 2. Levantar base de datos MySQL en Docker (entorno principal).
- [ ] 3. Configurar perfiles y propiedades (`local`, `docker`, `test`).
- [ ] 4. Crear modelo relacional y migraciones Flyway (`V1+`).
- [ ] 5. Implementar entidades JPA, repositorios y servicios transaccionales.
- [ ] 6. Exponer endpoints REST (`/clientes`, `/cuentas`, `/movimientos`, `/reportes`).
- [ ] 7. Implementar validaciones y `@RestControllerAdvice`.
- [ ] 8. Implementar seguridad base y documentar estrategia.
- [ ] 9. Implementar observabilidad (Actuator + Prometheus).
- [ ] 10. Construir pruebas unitarias e integracion (incluye Testcontainers).
- [ ] 11. Generar `BaseDatos.sql` y coleccion Postman.
- [ ] 12. Empaquetar despliegue Docker y documentacion final.

---

## 1) Arquitectura objetivo (nivel senior)

### 1.1 Estructura de paquetes sugerida
```text
src/main/java/com/devsu/backendbank/
  application/
    dto/
    mapper/
    service/
    usecase/
  domain/
    model/
    repository/
    exception/
    rule/
  infrastructure/
    persistence/
      entity/
      repository/
    web/
      controller/
      advice/
    config/
    security/
    report/
```

### 1.2 Patrones y decisiones
- `Repository pattern`: interfaces en dominio e implementacion JPA en infraestructura.
- `Service/UseCase`: logica de negocio, transacciones y orquestacion.
- `DTO + Mapper`: desacoplar API de entidades persistentes.
- `ControllerAdvice`: errores consistentes.
- `Specification/Strategy` (extra puntaje): para reportes y reglas de movimientos.

### 1.3 Criterios no funcionales
- Trazabilidad: logs estructurados por request.
- Consistencia: operaciones de movimiento en transaccion atomica.
- Mantenibilidad: validaciones en DTO, reglas en dominio, consultas en repositorio.

---

## 2) Base de datos primero (recomendado en Docker)

> Recomendacion: usar Docker como entorno principal por reproducibilidad, facilidad de evaluacion y alineacion con Testcontainers.

### 2.1 Crear `docker-compose.yml` (MySQL + app opcional)
Usar imagen fija (ejemplo `mysql:8.4`) para evitar variaciones.

Ejemplo de arranque local de BD:
```bash
docker compose up -d mysql
```

### 2.2 Variables y seguridad minima
Definir en `.env` (no commitear secretos reales):
```bash
MYSQL_DATABASE=backend_bank
MYSQL_USER=backend_user
MYSQL_PASSWORD=backend_pass
MYSQL_ROOT_PASSWORD=root_pass
MYSQL_PORT=3306
```

### 2.3 Verificacion de conectividad
```bash
docker ps
mysql -h 127.0.0.1 -P 3306 -u backend_user -pbackend_pass -e "SHOW DATABASES;"
```

### 2.4 Alternativa sin Docker (solo si es requerido)
Instalar MySQL en Ubuntu local y mantener misma version que en contenedor para evitar drift.

---

## 3) Configuracion de Spring Boot por perfiles

### 3.1 Crear archivos
- `src/main/resources/application.yml`
- `src/main/resources/application-local.yml`
- `src/main/resources/application-docker.yml`
- `src/main/resources/application-test.yml`

### 3.2 Configuracion base recomendada
- `spring.datasource.*`
- `spring.jpa.hibernate.ddl-auto=validate`
- `spring.flyway.enabled=true`
- `spring.flyway.locations=classpath:db/migration`
- `management.endpoints.web.exposure.include=health,info,metrics,prometheus`

### 3.3 Activacion de perfil
```bash
./gradlew bootRun --args='--spring.profiles.active=local'
```

---

## 4) Modelo de datos y Flyway

### 4.1 Modelo relacional minimo
- `persona` (base): nombre, genero, edad, identificacion (unique), direccion, telefono.
- `cliente`: PK propia, FK a `persona`, contrasena, estado.
- `cuenta`: numero cuenta (unique), tipo cuenta, saldo inicial, estado, FK cliente.
- `movimiento`: fecha, tipo movimiento, valor, saldo, FK cuenta.

### 4.2 Migraciones versionadas
En `src/main/resources/db/migration`:
- `V1__create_core_tables.sql`
- `V2__add_indexes_and_constraints.sql`
- `V3__seed_initial_data.sql` (opcional para demo)

### 4.3 Constraints e indices clave
- Unique: identificacion, numero_cuenta.
- Indexes: `(cliente_id, fecha)` y `(cuenta_id, fecha)` en movimientos para reportes.
- Check constraints (si aplica): tipo de movimiento/valores permitidos.

### 4.4 Script entregable `BaseDatos.sql`
Generarlo al final consolidando estructura + datos base y alineado a Flyway.

Comando sugerido para backup:
```bash
mysqldump -h 127.0.0.1 -P 3306 -u backend_user -pbackend_pass backend_bank > BaseDatos.sql
```

---

## 5) Implementacion backend (API y negocio)

### 5.1 Endpoints requeridos
- `GET/POST/PUT/PATCH/DELETE /clientes`
- `GET/POST/PUT/PATCH/DELETE /cuentas`
- `GET/POST/PUT/PATCH/DELETE /movimientos`
- `GET /reportes?clienteId=...&fechaDesde=...&fechaHasta=...&formato=json|pdf`

### 5.2 Reglas criticas de movimientos
- Debito: valor negativo.
- Credito: valor positivo.
- Calculo de saldo disponible por movimiento.
- Si saldo insuficiente: lanzar excepcion de negocio `Saldo no disponible`.
- Limite diario de debitos por cliente o cuenta (definir y documentar): 1000.
- Si supera limite: excepcion `Cupo diario Excedido`.

### 5.3 Transaccionalidad y concurrencia
- `@Transactional` en servicio de movimientos.
- Evitar race conditions en debitos concurrentes:
  - opcion A: `SELECT ... FOR UPDATE` (bloqueo pesimista).
  - opcion B: `@Version` (bloqueo optimista) + retry controlado.

### 5.4 Manejo de excepciones
Definir jerarquia de excepciones de dominio:
- `BusinessException`
- `ResourceNotFoundException`
- `DailyLimitExceededException`
- `InsufficientBalanceException`

Estandarizar respuesta con `ProblemDetail` o esquema propio:
- `timestamp`, `status`, `error`, `message`, `path`, `traceId`.

### 5.5 Validacion
- Bean Validation en DTOs (`@NotNull`, `@Positive`, `@Size`, etc.).
- Mensajes claros para consumo por Postman/Front.

---

## 6) Reportes (JSON + PDF base64)

### 6.1 Respuesta JSON del reporte
Debe incluir por cuenta:
- fecha, cliente, numero cuenta, tipo, saldo inicial, estado,
- movimiento, saldo disponible.
- agregados por rango: total debitos y total creditos.

### 6.2 Generacion PDF
- Opcion recomendada: motor de reporte simple (OpenPDF o JasperReports).
- Exponer PDF en base64 dentro de la respuesta o endpoint dedicado.

Ejemplo de contrato:
```json
{
  "clienteId": 1,
  "fechaDesde": "2022-02-01",
  "fechaHasta": "2022-02-28",
  "resumen": {
    "totalDebitos": -250.00,
    "totalCreditos": 600.00
  },
  "items": [],
  "pdfBase64": "JVBERi0xLjcKJc..."
}
```

---

## 7) Seguridad y observabilidad (para destacar seniority)

### 7.1 Seguridad minima viable
- Spring Security con autenticacion basica o JWT (documentar eleccion).
- Proteger endpoints de negocio.
- Permitir sin auth: `/actuator/health`, opcional docs de OpenAPI en entorno dev.

### 7.2 Observabilidad
- Actuator habilitado (`health`, `info`, `metrics`, `prometheus`).
- Micrometer + Prometheus para scraping de metricas.
- Metrica custom sugerida: contador de movimientos por tipo y rechazos por limite.

---

## 8) Estrategia de pruebas (imprescindible)

### 8.1 Minimo exigido vs objetivo senior
- Exigido: al menos 2 pruebas unitarias de endpoints.
- Objetivo senior recomendado:
  - Unitarias de servicios de negocio.
  - Integracion de endpoints con `MockMvc`.
  - Integracion con BD real via Testcontainers MySQL.

### 8.2 Casos de prueba criticos
1. Crear cliente exitoso.
2. Crear cuenta exitoso.
3. Debito con saldo suficiente.
4. Debito sin saldo -> `Saldo no disponible`.
5. Debito supera limite diario -> `Cupo diario Excedido`.
6. Reporte por rango de fechas.

### 8.3 Comandos de ejecucion
```bash
./gradlew clean test
./gradlew clean check
```

---

## 9) Dockerizacion de la solucion

### 9.1 `Dockerfile` para la app
- Build con Gradle wrapper.
- Imagen runtime ligera (JRE 21).

### 9.2 `docker-compose.yml` final
Servicios:
- `mysql`
- `backend-bank` (depende de mysql)
- opcional `prometheus`

### 9.3 Validacion end-to-end
```bash
docker compose up -d --build
curl -s http://localhost:8080/actuator/health
```

---

## 10) Entregables finales (checklist de entrega)
- [ ] Codigo fuente en repositorio publico GitHub.
- [ ] `BaseDatos.sql` en raiz del proyecto.
- [ ] Migraciones Flyway en `src/main/resources/db/migration`.
- [ ] Coleccion Postman + environment exportado.
- [ ] Evidencia de pruebas (`./gradlew test`) y cobertura basica.
- [ ] `README.md` con:
  - arquitectura,
  - instrucciones locales,
  - instrucciones Docker,
  - endpoints,
  - credenciales de prueba,
  - decisiones tecnicas y trade-offs.
- [ ] (Opcional fuerte) Diagrama entidad-relacion y diagrama de secuencia de movimiento.

---

## 11) Roadmap sugerido por dias (plan realista)

### Dia 1
- Setup Docker MySQL + perfiles Spring.
- Flyway `V1` con tablas y constraints base.
- Entidades JPA + repositorios.

### Dia 2
- CRUD `clientes` y `cuentas` completos.
- Manejo global de excepciones + validaciones.
- OpenAPI operativo.

### Dia 3
- Logica de `movimientos` con reglas (saldo + limite diario).
- Pruebas unitarias/integracion de movimientos.

### Dia 4
- Reportes JSON + PDF base64.
- Metricas y ajustes de seguridad.
- Pulido de mensajes de error y contratos API.

### Dia 5
- Dockerizacion final.
- Generacion de `BaseDatos.sql`.
- Coleccion Postman y `README.md` final.
- Ensayo de defensa tecnica (preguntas tipicas de arquitectura y concurrencia).

---

## 12) Preguntas de definicion (cerrarlas antes de implementar)
1. El limite diario de retiro aplica por `cuenta` o por `cliente` (global entre cuentas)?
2. La entidad `persona` se implementara como tabla separada + relacion 1:1, o herencia JPA (`JOINED`)?
3. El reporte PDF se entrega embebido en JSON (`pdfBase64`) o endpoint separado para descarga?
4. Seguridad esperada por evaluador: Basic Auth suficiente o prefieren JWT?

---

## 13) Criterios de excelencia para defensa tecnica
- Justificar por que Flyway + Docker + Testcontainers reducen riesgo.
- Explicar control de concurrencia en debitos simultaneos.
- Mostrar estrategia de pruebas por capas (unit/integration).
- Demostrar consistencia de errores y contratos API.
- Presentar trade-offs tecnicos y mejoras futuras (CI/CD, auditoria, cache, rate limiting).

---

## Comandos rapidos de referencia
```bash
# Levantar base de datos
docker compose up -d mysql

# Ejecutar app local con perfil local
./gradlew bootRun --args='--spring.profiles.active=local'

# Ejecutar pruebas
./gradlew clean test

# Levantar stack completo
docker compose up -d --build
```

Este plan prioriza una entrega evaluable, robusta y defendible tecnicamente en entrevista, con foco en demostrar seniority en backend Java/Spring.


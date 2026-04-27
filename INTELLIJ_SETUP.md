# 🧠 Guía IntelliJ IDEA - Backend Bank

Configuración paso a paso para trabajar con **Backend Bank** en IntelliJ IDEA.

---

## 📋 Tabla de Contenidos

- [Importar Proyecto](#importar-proyecto)
- [Configurar JDK](#configurar-jdk)
- [Configurar Gradle](#configurar-gradle)
- [Run Configurations](#run-configurations)
- [Tips & Tricks](#tips--tricks)
- [Troubleshooting](#troubleshooting)

---

## 🚀 Importar Proyecto

### Paso 1: Abrir IntelliJ

1. Abre IntelliJ IDEA
2. Haz clic en **File** → **Open**

### Paso 2: Seleccionar Carpeta

1. Navega a la carpeta `backend-bank`
2. Selecciona la carpeta raíz (donde está `build.gradle`)
3. Haz clic en **Open** o **Open as Project**

### Paso 3: Trust Project

Una ventana pedirá confianza en el proyecto:
- Haz clic en **Trust Project**

### Paso 4: Esperar Sincronización

IntelliJ sincroniza automáticamente:

```
⏳ Syncing Gradle project 'backend-bank'...
✅ Gradle sync completed (puede tomar 1-2 minutos)
```

---

## ☕ Configurar JDK

### Paso 1: Abrir Project Structure

**File → Project Structure** (o `Ctrl+Alt+Shift+S` en Windows/Linux, `⌘+;` en Mac)

### Paso 2: Verificar/Cambiar SDK

1. En el panel izquierdo, selecciona **Project**
2. En **SDK**, verifica que esté Java 21
3. Si no lo ves:
   - Haz clic en **Edit**
   - Selecciona **Download JDK**
   - **Version:** 21
   - **Vendor:** Eclipse Temurin (recomendado)
   - Haz clic en **Download**

### Paso 3: Configurar Language Level

1. En **Project Language Level**, selecciona **21**
2. Haz clic en **OK**

---

## 🔧 Configurar Gradle

### Paso 1: Abrir Settings

**File → Settings** (o `Ctrl+Alt+S` en Windows/Linux, `⌘+,` en Mac)

### Paso 2: Ir a Gradle

Navega a: **Build, Execution, Deployment → Build Tools → Gradle**

### Paso 3: Configurar

1. **Gradle JVM:** Selecciona **Java 21**
2. **Build and run using:** Elige **Gradle** (en lugar de IntelliJ IDEA)
3. **Run tests using:** Elige **Gradle**
4. **Gradle user home:** Dejar por defecto
5. Haz clic en **OK**

---

## ▶️ Run Configurations

### Opción A: GUI (Recomendado)

#### Crear Configuration

1. En la barra superior, junto al botón ▶️ verde, hay un dropdown
2. Haz clic en **Add Configuration**
3. Haz clic en **+** en la esquina superior izquierda
4. Selecciona **Spring Boot** de la lista

#### Llenar Detalles

1. **Name:** `Backend Bank (Local)`
2. **Main class:** `com.devsu.backendbank.BackendBankApplication`
3. **Module:** `backend_bank.main`

#### Active Profiles

1. Ve a la sección **Environment**
2. En **Active profiles**, coloca: `local`
3. (Opcional) En **VM options**, agregar:
   ```
   -Dspring.profiles.active=local
   ```

#### Environment Variables (Opcional)

1. Haz clic en el botón pequeño al lado de **Environment variables**
2. Agrega variables si es necesario:
   ```
   MYSQL_HOST=localhost
   MYSQL_PORT=3306
   MYSQL_DATABASE=backend_bank
   MYSQL_USER=backend_user
   MYSQL_PASSWORD=backend_pass
   ```

#### Aplicar

1. Haz clic en **OK**
2. Ahora aparece en el dropdown con el nombre `Backend Bank (Local)`

#### Ejecutar

1. Selecciona la configuración en el dropdown
2. Presiona ▶️ (Run)
3. O presiona `Shift+F10` (Windows/Linux) o `⌃+R` (Mac)

### Opción B: Crear `.idea/runConfigurations/`

Si prefieres versionar la configuración, crea este archivo:

`.idea/runConfigurations/Backend_Bank_Local.xml`:

```xml
<component name="ProjectRunConfigurationManager">
  <configuration default="false" name="Backend Bank (Local)" type="SpringBootApplicationConfigurationType">
    <option name="SPRING_BOOT_MAIN_CLASS" value="com.devsu.backendbank.BackendBankApplication" />
    <option name="MODULE_NAME" value="backend_bank.main" />
    <module name="backend_bank.main" />
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

---

## 🧪 Configurar Tests

### Tests Unitarios

#### Ejecutar Todos

1. En el Project Explorer, click derecho en `src/test/java`
2. Selecciona **Run 'Tests in 'java''**

#### Ejecutar por Clase

1. Abre `src/test/java/com/devsu/backendbank/application/service/`
2. Click derecho en `ClientServiceTest`
3. Selecciona **Run 'ClientServiceTest'**

#### Ejecutar Método Específico

1. Abre el archivo de test
2. Dentro de la clase, junto al método, hay un pequeño triángulo verde ▶
3. Haz clic en él
4. Selecciona **Run 'nombreDelMetodo'()'**

### Tests de Integración

1. Asegúrate que MySQL está corriendo:
   ```bash
   docker-compose up -d mysql
   ```

2. Click derecho en `*IT.java`
3. Selecciona **Run** o presiona `Shift+F10`

---

## 💡 Tips & Tricks

### Autocompletad y Análisis

1. **File → Invalidate Caches** para limpiar caché si hay errores falsos
2. **Build → Rebuild Project** para recompilar desde cero

### Navegación Rápida

```
Ctrl+N (Windows/Linux) o ⌘+O (Mac)
→ Abre cualquier clase por nombre
Ejemplo: "ClientService"

Ctrl+F12 (Windows/Linux) o ⌘+F12 (Mac)
→ Abre estructura de la clase actual (métodos, campos)

Ctrl+/ (Windows/Linux) o ⌘+/ (Mac)
→ Comenta/descomenta línea
```

### Debug

1. Haz clic en el margen izquierdo para poner un **breakpoint** (punto rojo)
2. Presiona `Shift+F9` (Windows/Linux) o `⌃+D` (Mac) para **Debug**
3. Usa los controles Step Over/Into/Out

### Refactorización

```
Ctrl+Alt+M (Windows/Linux) o ⌘+⌥+M (Mac)
→ Extrae código en método

Shift+F6 (Windows/Linux) o ⇧+F6 (Mac)
→ Renombra variable/método en toda la clase
```

### Búsqueda Avanzada

```
Ctrl+Shift+F (Windows/Linux) o ⌘+⇧+F (Mac)
→ Busca texto en todos los archivos

Ctrl+Alt+Shift+F (Windows/Linux) o ⌘+⌥+⇧+F (Mac)
→ Busca reciente
```

---

## 🐛 Troubleshooting

### Error: `Cannot resolve symbol 'BackendBankApplication'`

**Causa:** Gradle no sincronizó.

**Solución:**
1. **File → Invalidate Caches and Restart**
2. Abre **Gradle** en el panel derecho
3. Haz clic en el botón de **Refresh** (↻)

### Error: `JDK not configured`

**Causa:** No hay JDK seleccionado.

**Solución:**
1. **File → Project Structure**
2. En **SDK**, haz clic en **Edit**
3. Selecciona **Download JDK**
4. **Version: 21**, **Vendor: Eclipse Temurin**
5. Haz clic en **Download**

### Error: `Gradle sync failed`

**Causa:** Actualizaciones de dependencias o Maven central inaccesible.

**Solución:**
```bash
# Limpiar cache de Gradle
rm -rf ~/.gradle/caches

# En IntelliJ:
# File → Invalidate Caches
# File → Sync with Gradle
```

### No aparece Main class en Run Configuration

**Causa:** Clase principal no está marcada como Spring Boot.

**Verificar:**
1. Abre `src/main/java/com/devsu/backendbank/BackendBankApplication.java`
2. Debe tener `@SpringBootApplication`
3. Si falta, **File → Invalidate Caches and Restart**

### Terminal en IntelliJ no funciona

**Solución:**
1. **View → Tool Windows → Terminal**
2. O presiona `Alt+F12`

---

## 🎯 Workflow Recomendado

### Desarrollo Local

```
1. Inicia MySQL (en terminal):
   docker-compose up -d mysql

2. En IntelliJ, selecciona "Backend Bank (Local)"

3. Presiona ▶ o Shift+F10

4. Accede a http://localhost:8080/swagger-ui/index.html

5. Para tests: Click derecho en la clase → Run
```

### Debug

```
1. Pon breakpoint (click en margen izquierdo)

2. Ejecuta en debug: Shift+F9

3. Usa controles:
   F8 → Step Over
   F7 → Step Into
   Shift+F8 → Step Out

4. Variables aparecen en panel "Debug"
```

### Cambiar Configuración

```
1. En el dropdown de Run, selecciona "Edit Configurations"

2. O presiona Ctrl+Shift+Alt+S → Run/Debug Configurations

3. Cambia Active profiles o variables de entorno

4. Click OK
```

---

## 📚 Referencias

- [IntelliJ IDEA | JetBrains](https://www.jetbrains.com/help/idea/)
- [Running and Debugging](https://www.jetbrains.com/help/idea/running-and-debugging-code.html)
- [Gradle Integration](https://www.jetbrains.com/help/idea/gradle.html)

---

**Última actualización:** Abril 2024


# Mercado Local EC 🛒📱

Aplicación móvil nativa para Android diseñada para conectar de forma directa a productores locales con consumidores, facilitando la comercialización comunitaria, la gestión de inventarios y una experiencia de compra interactiva.

---

## 🚀 Características Principales

- **Autenticación y Perfiles:** Registro e inicio de sesión seguro para productores y consumidores mediante Firebase Authentication.
- **Catálogo Interactivo y Pedidos:** Navegación fluida de productos locales, filtrado por categorías y gestión de solicitudes de compra.
- **Persistencia Local Offline-First:** Almacenamiento local estructurado mediante **Room Database** para consultas rápidas sin depender de conexión permanente.
- **Escáner de Código de Barras:** Módulo integrado para lectura e identificación rápida de productos en inventario.
- **Visualización en Realidad Aumentada (AR):** Integración con **ARCore** para proyectar modelos y dimensiones de productos en el entorno real del usuario.
- **Geocodificación y Ubicación:** Integración con servicios de red (API Nominatim / OpenStreetMap) para localización y despacho geográfico.
- **Generación de Reportes PDF:** Exportación local de comprobantes y resúmenes de pedidos para productores.

---

## 🛠️ Stack Tecnológico y Arquitectura

- **Lenguaje:** Kotlin
- **Interfaz de Usuario:** Jetpack Compose & Material 3
- **Arquitectura:** MVVM (Model - View - ViewModel) con separación de responsabilidades
- **Concurrencia:** Kotlin Coroutines & StateFlow
- **Base de Datos Local:** Android Jetpack Room (SQLite)
- **Consumo de APIs / Red:** Retrofit 2 & OkHttp
- **Backend & Cloud:** Firebase (Auth / Storage)
- **Realidad Aumentada:** Google ARCore
- **Gestor de Dependencias:** Gradle (Kotlin DSL con Version Catalogs - `libs.versions.toml`)

---

## 📂 Estructura del Proyecto

```text
app/src/main/java/com/example/holamundo/
├── model/                  # Entidades de datos y DAOs (Room Database)
├── network/                # Interfaces Retrofit y modelos de respuesta API
├── screens/                # UI con Jetpack Compose
│   ├── ar/                 # Vistas y lógica de Realidad Aumentada
│   ├── home/               # Vistas principales de catálogo
│   ├── productor/          # Panel de administración e inventario
│   └── scanner/            # Escáner de códigos de barra
├── viewmodel/              # Controladores de estado (Auth, Catálogo, Pedidos)
└── utils/                  # Generadores de PDF y helpers de sistema

---

## ⚙️ Requisitos del Entorno de Desarrollo

| Herramienta | Versión |
|---|---|
| Sistema Operativo | Ubuntu Linux |
| IDE | Android Studio (instalado vía Snap) |
| JDK para Gradle | OpenJDK 17 |
| Gradle Wrapper | 8.13 |
| Android Gradle Plugin (AGP) | 8.13.2 |
| Kotlin | 2.0.21 |
| Dispositivo de prueba | Samsung Galaxy A32 (SM-A325M) – Android 13 |

---

## 🔧 Solución de Problemas: Incompatibilidad Gradle ↔ JDK (Snap)

### El problema
Android Studio instalado por **Snap** usa su JDK integrado más reciente (**Java 25**). Gradle 8.x no soporta esa versión de Java, y además el wrapper del proyecto usaba **Gradle 8.7**, mientras que **AGP 8.13.2 exige Gradle 8.13 o superior**. Por eso la sincronización fallaba siempre.

### La solución aplicada
1. **Instalar un JDK LTS compatible (17):**
   ```bash
   sudo apt update && sudo apt install -y openjdk-17-jdk
   ```
   El Snap de Android Studio usa confinamiento *classic*, así que puede leer `/usr/lib/jvm` sin permisos extra.

2. **Actualizar el Gradle Wrapper** en `gradle/wrapper/gradle-wrapper.properties`:
   ```properties
   distributionUrl=https\://services.gradle.org/distributions/gradle-8.13-bin.zip
   ```

3. **Fijar el JDK de Gradle** en `gradle.properties` para que no dependa del JDK del Snap:
   ```properties
   org.gradle.java.home=/usr/lib/jvm/java-17-openjdk-amd64
   ```
   (También se eliminó una línea duplicada de `android.nonTransitiveRClass`).

4. **En Android Studio:** `File → Settings → Build, Execution, Deployment → Build Tools → Gradle → Gradle JDK` → seleccionar **java-17-openjdk-amd64**. Después, `File → Invalidate Caches → Invalidate and Restart` y `Sync Project with Gradle Files`.

✅ **Resultado:** la sincronización y la compilación terminan con `BUILD SUCCESSFUL`.

---

## 📲 Ejecución en Dispositivo Físico (USB)

1. **Activar las Opciones de desarrollador:** `Ajustes → Acerca del teléfono → Información de software` y tocar **Número de compilación** 7 veces.
2. **Activar la Depuración USB** en `Ajustes → Opciones de desarrollador`.
3. **Dar permisos USB en Linux (reglas udev):**
   ```bash
   sudo apt install -y android-sdk-platform-tools-common
   sudo usermod -aG plugdev $USER   # cerrar sesión y volver a entrar
   ```
4. **Conectar el teléfono** en modo *Transferencia de archivos* y aceptar el aviso **"¿Permitir depuración USB?"**.
5. **Comprobar la conexión:**
   ```bash
   ~/Android/Sdk/platform-tools/adb devices
   # RF8R21L6TZY    device
   ```
6. **Compilar e instalar la app:**
   - Desde Android Studio: elegir el dispositivo y pulsar **▶ Run** (`Shift+F10`).
   - Desde la terminal:
     ```bash
     ./gradlew installDebug
     adb shell monkey -p com.example.holamundo -c android.intent.category.LAUNCHER 1
     ```

✅ **Resultado:** el APK se instaló y abrió correctamente en un Samsung Galaxy A32 con Android 13.

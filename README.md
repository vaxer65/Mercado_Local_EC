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

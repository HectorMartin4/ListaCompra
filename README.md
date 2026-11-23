# 🛒 ListaCompra

App nativa de Android para gestionar las **listas de la compra**, con un catálogo de productos
organizado por **supermercado** y **categorías**, e **historial de precios** de cada producto.

Escrita en **Kotlin** con **Jetpack Compose** y una arquitectura por funcionalidades
(*feature-based*).

---

## ✨ Qué hace hoy

| Funcionalidad | Descripción |
|---|---|
| **Listas de la compra** | Crear, renombrar, borrar y vaciar listas. Marcar productos como comprados, ajustar cantidad y precio pagado. |
| **Listas agrupadas por supermercado** | Cada lista pertenece a un supermercado, y la pantalla principal las agrupa por él. |
| **Catálogo de productos** | Productos con nombre, precio por defecto (opcional), categoría y supermercado. Buscador con normalización de texto. |
| **Categorías** | Crear, renombrar, reordenar y borrar categorías por supermercado, con posición persistente. |
| **Supermercados** | Múltiples supermercados con selector del activo. |
| **Historial de precios** | Registro de cada precio visto de un producto, con gráfica de evolución y detección de subidas/bajadas. |
| **Deshacer acciones** | Snackbar de deshacer en borrados y cambios de lista. |
| **100 % offline** | Todos los datos viven en una base de datos local (Room). |

---

## 🧱 Arquitectura

Organización por *features* con separación en capas dentro de cada una:

```
com.hmc.listacompracompose/
├── app/                 → Application, DI (Koin), base de datos Room
├── ui/                  → tema (Material 3) y componentes compartidos
├── features/
│   ├── items/           → catálogo de productos
│   ├── lists/           → listas de la compra
│   ├── categories/      → categorías
│   ├── supermarkets/    → supermercados
│   ├── pricehistory/    → historial de precios
│   └── data/local/db/   → entidades y DAOs compartidas de Room
└── MainActivity.kt      → navegación y bottom bar
```

Cada *feature* sigue el mismo reparto:

- **`presentation/`** → composables + `ViewModel` (estado con `StateFlow`).
- **`domain/`** → modelos del dominio, casos de uso (`UseCases`) e interfaces de repositorio.
- **`data/`** → implementación de repositorios y fuentes de datos (local en Room, remota con Retrofit).

Flujo de datos unidireccional: `UI → ViewModel → UseCases → Repository → DataSource`.

### Modelo de datos (Room v6)

Tablas: `items`, `shopping_list`, `shopping_list_item`, `category`, `supermarket`, `price_history`.
Los IDs locales son `Int` autogenerados; las migraciones versionadas se guardan en `app/schemas/`.

---

## 📚 Librerías y tecnologías

**UI**
- [Jetpack Compose](https://developer.android.com/compose) + **Material 3** (BOM `2026.06.01`)
- [Material Icons Extended](https://developer.android.com/jetpack/androidx/releases/compose-material) — iconos
- [reorderable](https://github.com/Calvin-LL/Reorderable) — reordenación táctil de listas y categorías

**Navegación y ciclo de vida**
- [Navigation Compose](https://developer.android.com/jetpack/androidx/releases/navigation) `2.9.8`
- [Lifecycle / ViewModel KTX](https://developer.android.com/jetpack/androidx/releases/lifecycle) `2.10.0`
- [Activity Compose](https://developer.android.com/jetpack/androidx/releases/activity) `1.13.0` + edge-to-edge

**Datos y persistencia**
- [Room](https://developer.android.com/training/data-storage/room) `2.8.5` — BD local con migraciones explícitas y esquemas versionados
- [SharedPreferences](https://developer.android.com/reference/android/content/SharedPreferences) — preferencias ligeras (supermercado activo)

**Inyección de dependencias**
- [Koin](https://insert-koin.io/) `4.2.2` — `koin-core`, `koin-android`, `koin-androidx-compose`,
  `koin-annotations` y el plugin de compilación **Koin Compiler**

**Red (preparado para la nube)**
- [Retrofit](https://square.github.io/retrofit/) `3.0.0` + `converter-gson`
- [OkHttp](https://square.github.io/okhttp/) `5.4.0` + `logging-interceptor`

**Compilación y calidad**
- Kotlin `2.4.20` · AGP `8.13.2` · JVM target 17 · [KSP](https://developer.android.com/build/ksp-support-kotlin) `2.3.12` para los procesadores de anotaciones de Room y Koin
- [JUnit 4](https://junit.org/junit4/), [Espresso](https://developer.android.com/training/testing/espresso) y **Compose UI Test**

---

## 🚀 Cómo compilar

```bash
git clone https://github.com/HectorMartin4/ListaCompra.git
cd ListaCompra
./gradlew assembleDebug        # o installDebug para desplegar en un dispositivo
```

Requisitos: **JDK 17** y **Android SDK 36** (`minSdk` 26).

---

## 🔮 Próximos pasos

> [!NOTE]
> Esta sección se actualiza a medida que avanza el proyecto.

### Fase 1 · Backend y autenticación
- [ ] Decidir el stack del servidor (ASP.NET Core · Node.js · Supabase · Ktor).
- [ ] Definir la BD relacional del servidor (`users`, `shopping_list_members`, …).
- [ ] Integrar **Firebase Auth** (Google / email / anónimo) en la app.
- [ ] Interceptor OkHttp que añada `Authorization: Bearer <token>` a todas las llamadas.
- [ ] Endpoint `GET /api/me` y validación del token con Firebase Admin SDK.

### Fase 2 · Sincronización y listas compartidas
- [ ] Migración **Room 6→7**: columna `serverId` en todas las tablas.
- [ ] `POST /api/sync/import` + pantalla «subir mis datos» (orden padres→hijos).
- [ ] Endpoints de listas compartidas: `/api/lists`, `share` (código de invitación), `join`.
- [ ] Sincronización en tiempo real con **WebSocket / SSE**.
- [ ] Sincronización incremental en segundo plano con **WorkManager** (columnas `dirty` / `updatedAt`).

### Fase 3 · IA · Escáner de tickets
- [ ] Captura con **CameraX** / Photo Picker.
- [ ] **OCR local** con Google ML Kit Text Recognition.
- [ ] Proxy `POST /api/ai/ticket` (Gemini Flash en el servidor para no exponer la API key).
- [ ] Volcado de lo escaneado al catálogo y al historial de precios.

### Fase 4 · IA · Asistente de listas
- [ ] «Crear lista con IA» a partir de una descripción en lenguaje natural.
- [ ] Proxy `POST /api/ai/generate-list`, reutilizando productos del catálogo existente.

---

## 📄 Licencia

Proyecto personal de aprendizaje. Todos los derechos reservados por el autor.

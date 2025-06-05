# Android App Development Roadmap: AI Image Generator

## 1. Arquitectura y Patrones de Diseño
- [x] **Patrón de Arquitectura**: Implementar MVVM (Model-View-ViewModel) para separar responsabilidades (Repositorios ↔ ViewModels ↔ Activities/Fragments).
- [x] **Inyección de Dependencias**: Integrar Hilt (Dagger-Hilt) para inyectar `ApiService`, `BillingManager`, `AdManager`, repositorios, etc.
- [x] **Uso de Coroutines + Flow**: Todas las llamadas de red o BD deben ser con `suspend fun` y/o `Flow` para no bloquear el hilo principal.

## 2. Estructura de Carpetas (más detallada)
- [x] `/app/src/main/java/com/puffyai/puffyai/`
    - [x] `/ui/`
        - [x] `/main/` (MainActivity.kt, MainViewModel.kt)
        - [x] `/generate/` (GenerateActivity.kt, GenerateViewModel.kt)
        - [x] `/purchase/` (PurchaseActivity.kt, PurchaseViewModel.kt)
        - [x] `/common/` (BaseActivity.kt, ViewExtensions.kt)
    - [x] `/domain/`
        - [x] `/model/` (GeneratedImage.kt, PurchasePack.kt)
        - [x] `/usecase/` (GenerateImageUseCase.kt, GetUsageStatusUseCase.kt, PurchaseCreditsUseCase.kt, SaveGeneratedImageUseCase.kt, GetMyCreationsUseCase.kt)
        - [x] `/repository/` (ImageRepository.kt, BillingRepository.kt, AdRepository.kt)
    - [x] `/data/`
        - [x] `/network/` (OpenAiApiService.kt, NetworkResponse.kt)
        - [x] `/local/` (UserPreferences.kt, GeneratedImageDao.kt, AppDatabase.kt, GeneratedImageEntity.kt)
        - [x] `/billing/` (BillingRepositoryImpl.kt)
        - [x] `/ads/` (AdRepositoryImpl.kt)
        - [x] `/repository/` (ImageRepositoryImpl.kt)
    - [x] `/util/`
        - [x] Constants.kt
        - [x] Extensions.kt (Functionality moved to ViewExtensions.kt)
        - [x] NetworkUtils.kt
        - [x] DateUtils.kt
    - [x] `/di/` (NetworkModule.kt, BillingModule.kt, AdsModule.kt, RepositoryModule.kt, DatabaseModule.kt)
- [x] `/app/src/main/res/`
    - [x] `/layout/`
    - [x] `/drawable/`
    - [x] `/values/`
    - [x] `/assets/`

## 3. Módulo UI – Flujo de Pantallas (más granular)

### 3.1. Pantalla Principal (MainActivity + MainViewModel)
- [x] **Componentes**:
    - [x] Texto “Créditos restantes hoy”.
    - [x] Botón “Seleccionar Imagen”.
    - [x] Botón “Ver Anuncio para +1 generación”.
    - [x] Botón “Comprar Packs”.
- [x] **Observables (LiveData/StateFlow) en ViewModel**:
    - [x] `usageState`: `StateFlow<UsageStatus>` (cantidad restante, fecha).
    - [x] `uiState`: `StateFlow<UiState>` (“Cargando”, “Error”, “Listo”).
- [x] **Flux de acciones**:
    - [x] Usuario pulsa “Seleccionar Imagen” → lanza `pickImageLauncher`.
    - [x] Resultado URI → pasa a ViewModel para validar si puede generar.
    - [x] ViewModel chequea `UsageLimiter.canGenerate()` (o revisa créditos en repositorio).
    - [x] Si no puede generar: mostrar diálogo con opciones “Ver anuncio” o “Comprar créditos”.
    - [x] Si sí puede: navegar a `GenerateActivity` (o enviar directamente la imagen a `ImageProcessor`).
- [x] **Validaciones/Errores a prever**:
    - [x] Usuario selecciona un URI nulo o archivo corrupto → mostrar mensaje “Archivo no válido”.
    - [x] Permisos de Cámara/Galería denegados → mostrar snackbar “Sin permisos, por favor habilita manualmente”.
    - [x] Botones deshabilitados mientras `uiState == Loading` para evitar doble click.

### 3.2. Pantalla de Generación (GenerateActivity + GenerateViewModel)
- [x] **Componentes**:
    - [x] `ProgressBar` (circular o determinate si quieres mostrar porcentaje).
    - [x] `ImageView` para “imagen generada”.
    - [x] Texto de estado (“Enviando a IA…”, “Procesando…”, “Error en red”).
    - [x] Botón “Guardar en galería” (una vez generada).
- [x] **Flujo**:
    - [x] Recibe URI/Bitmap y el prompt (hardcodeado o editable).
    - [x] ViewModel invoca `GenerateImageUseCase`.
    - [x] Dentro, preprocesar imagen (escalado en `ImageProcessor`).
    - [x] Convertir a `MultipartBody.Part` con nombre “image”.
    - [x] Llamar a `ApiService.generateImage()`.
    - [x] Mostrar estado `Loading`.
    - [x] Si éxito: extraer la respuesta → puede ser URL o base64.
    - [x] Cargar en `ImageView` con Glide/Coil.
    - [x] Llamar a `UsageLimiter.incrementUsage()` o decrementar créditos en repositorio.
    - [x] Si error:
        - [x] Código HTTP distinto a 200 → manejar según `NetworkResponse` (timeout, 401, 500).
        - [x] Timeout de OkHttp → “Tiempo de espera agotado”.
        - [x] Excepción genérica → “Error inesperado, inténtalo más tarde”.
- [x] **Errores y validaciones**:
    - [x] `OutOfMemoryError`: si la imagen original es > 5 MB, escalar primero.
    - [x] API Key inválida: 401 → mostrar mensaje “Clave inválida, revisa configuración”.
    - [x] Throttling de API: 429 → “Has superado el límite de llamadas, espera o compra más créditos”.
    - [x] Fallo en decodificación de la respuesta → verificar con `ResponseBody.string()` y Gson.

### 3.3. Pantalla de Compras (PurchaseActivity + PurchaseViewModel)
- [x] **Componentes**:
    - [x] `RecyclerView`/`ListView` con opciones de packs (5, 10, 20).
    - [x] Botón “Restaurar compras” (para usuarios que reinstalan la app).
    - [x] Texto con créditos actuales y fecha de última transacción.
- [x] **Flujo**:
    - [x] Al iniciar, ViewModel obtiene productos disponibles (`BillingRepository.querySkuDetails()`).
    - [x] Error: si `BillingClient` falla al conectar → mostrar “No se pudo conectar a Play Store”.
    - [x] Mostrar opciones con precio y descripción (“Pack 5 créditos – 1.99 €”).
    - [x] Usuario selecciona un pack → `billingClient.launchBillingFlow()`.
    - [x] En `PurchasesUpdatedListener`:
        - [x] Si `responseCode == OK` y `purchases != null`:
            - [x] Verificar `purchase.getPurchaseState() == PURCHASED`.
            - [x] Llamar `consumeAsync()` para consumible.
            - [x] Actualizar `SharedPreferences`: `creditos += cantidadDelPack`.
            - [x] Mostrar `SnackBar` “¡Compra exitosa! Tienes X créditos.”
        - [x] Si `responseCode == USER_CANCELED`: “Compra cancelada”.
        - [x] Otros códigos (`ERROR`, `ITEM_UNAVAILABLE`) → “Error al procesar compra”.
- [x] **Errores a prever**:
    - [x] `BillingClient` desconectado justo antes de pagar. → reintentar `startConnection()`.
    - [x] Duplicado de consumible: consumir dos veces el mismo token → lanzar excepción de Google Play.
    - [x] Usuario reinstala app: usar “Restaurar compras” para re-sincronizar créditos desde servidor (opcional).

## 4. Capa de Datos (Data Layer) – Profundización

### 4.1. ApiService.kt
- [x] **Definition of the interface**:
    - [x] `interface OpenAiApiService { @Multipart @POST("v1/images/generations") suspend fun generateImage(@Part("prompt") prompt: RequestBody, @Part image: MultipartBody.Part): Response<OpenAiImageResponse> }`
    - [x] `OpenAiImageResponse` and `ImageData` map JSON correctly.
- [x] **Retrofit Configuration**:
    - [x] Use Hilt (`NetworkModule.kt`) to provide `OkHttpClient` and `Retrofit` with interceptor for `Authorization` header and timeouts.

### 4.2. UserPreferences.kt (SharedPreferences)
- [x] **Basic Functions**:
    - [x] `getRemainingGenerations()`, `incrementDailyUsage()`, `addCredits()`, `consumeCredit()`.
- [x] **Frequent Errors**:
    - [x] Data loss if you change name of `SharedPreferences`.
    - [x] Inconsistency if you save date in different format.
    - [x] Centralize keys in `Constants.kt` and use `DateUtils.formatDate()`.

### 4.3. Room Database (Optional, Image History)
- [x] **Entities and DAO**:
    - [x] `GeneratedImageEntity` (`id`, `prompt`, `imageUrl`, `timestamp`).
    - [x] `GeneratedImageDao` (`insert`, `getAllImages`).
- [x] **Database and Migrations**:
    - [x] `AppDatabase` with `GeneratedImageEntity`.
    - [x] Use Hilt (`DatabaseModule.kt`) to provide `AppDatabase` and `GeneratedImageDao`.
    - [x] Consider `fallbackToDestructiveMigration` for development, but manual for production.

## 5. Capa de Dominio (Domain Layer) – Use Cases y Lógica

### 5.1. Uso de Casos (Use Cases)
- [x] `GenerateImageUseCase`: Escalar/optimizar Bitmap, construir `MultipartBody`, llamar a `ApiService`, devolver `Result<Bitmap>` o `Result<String>`, actualizar `LocalStorage`.
- [x] `GetUsageStatusUseCase`: Leer `SharedPreferences`, resetear contador si la fecha cambió, devolver `UsageStatus`.
- [x] `ShowAdUseCase`: Llamar a `AdRepository.loadAd()`, incrementar créditos cuando se recompensa.
- [x] `PurchaseCreditsUseCase`: Obtener lista de productos de `BillingRepository`, gestionar flujo de compra, confirmar consumo, actualizar `LocalStorage`.

### 5.2. ImageProcessor.kt
- [x] **Funciones recomendadas**:
    - [x] `scaleBitmap(bitmap: Bitmap, maxWidth: Int, maxHeight: Int): Bitmap`.
    - [x] `compressToJpeg(bitmap: Bitmap, quality: Int = 80): ByteArray`.
- [x] **Errores a prever**:
    - [x] `Bitmap too large to fit in memory`: procesar en background y liberar memoria manualmente con `bitmap.recycle()`.
    - [x] Calidad vs Tamaño: encontrar balance.

### 5.3. UsageLimiter.kt (Logic moved to UserPreferences.kt)
- [x] **Integrates logic for credits and daily limit**:
    - [x] `UserPreferences.getRemainingGenerations()`: Priority to credits over daily usage.
    - [x] `UserPreferences.consumeCredit()`: Decrements usage/credits.
- [x] **Errors to foresee**:
    - [x] Date desynchronization if the user manually changes the time.
    - [ ] Possible improvement: validate with a “server date”. (Deferred)

## 6. Ad Management (AdRepository and AdRepositoryImpl)
- [x] **AdRepository Structure**:
    - [x] `interface AdRepository { suspend fun loadRewardedAd(): NetworkResponse<Unit>; fun showRewardedAd(activity: Activity, onReward: () -> Unit) }`
- [x] **In AdRepositoryImpl.kt**:
    - [x] Maintain a single instance of `RewardedAd`.
    - [x] Implement callbacks (`onAdLoaded`, `onAdFailedToLoad`, `onAdShowedFullScreenContent`, `onUserEarnedReward`).
    - [x] Load a new ad after the current one is dismissed.
- [x] **Errors to foresee**:
    - [x] Ad not loaded: “No ads available, try again later”.
    - [x] Early close: do not grant reward.
    - [x] Misconfigured ad ID.
    - [x] Context leak.

## 7. Purchase Handling (BillingRepository and BillingRepositoryImpl)
- [x] **BillingRepository Structure**:
    - [x] `interface BillingRepository { fun startBillingConnection(); suspend fun queryAvailableProducts(): List<ProductDetails>; fun launchPurchaseFlow(activity: Activity, productDetails: ProductDetails); fun consumePurchase(purchaseToken: String); fun acknowledgePurchase(purchase: Purchase, onResult: (Boolean) -> Unit); fun restorePurchases(onResult: (List<Purchase>) -> Unit); fun setPurchasesUpdatedListener(listener: (billingResult: BillingResult, purchases: List<Purchase>?) -> Unit); fun endConnection() }`
- [x] **Implementation in BillingRepositoryImpl.kt**:
    - [x] Initialize `BillingClient` in `Application`.
    - [x] In `onPurchasesUpdated()`: validate `purchase.purchaseState`, call `consumeAsync()` for consumables, `acknowledgePurchase()` for non-consumables.
    - [x] Implement automatic reconnection in `onBillingServiceDisconnected()`.
- [x] **Errors to foresee**:
    - [x] `BillingClient` disconnects.
    - [x] Duplicate token.
    - [x] No products configured in the console.
    - [x] Local/server inconsistency.

## 8. Gestión de Errores y Logging
- [x] **Manejo Genérico de Errores en la App**:
    - [x] Implementar un `GlobalExceptionHandler` en `Application` para registrar excepciones no capturadas.
    - [x] Capturar excepciones en coroutines usando `CoroutineExceptionHandler`.
- [x] **Uso de Crashlytics / Analytics**:
    - [x] Integrar Firebase Crashlytics para loguear errores.
    - [x] Configurar Firebase Analytics para eventos clave (`ImagenGenerada`, `AnuncioVisto`, `CompraRealizada`).

## 9. Permisos y Seguridad
- [x] **Runtime Permissions Detallado**:
    - [x] Permisos necesarios: `CAMERA`, `READ_EXTERNAL_STORAGE` (Android < 13) / `READ_MEDIA_IMAGES` (Android 13+), `WRITE_EXTERNAL_STORAGE` (opcional).
    - [x] Flujo: chequear permisos, solicitar, manejar resultados (concedido/denegado/denegado permanentemente).
- [x] **Seguridad de API Key**:
    - [x] Nunca exponer la clave en texto plano en código.
    - [x] Usar `local.properties` para desarrollo y `buildConfigField`.
    - [x] Para producción: considerar un proxy server.

## 10. Conectividad y Uso Offline
- [x] **Detector de Conexión**:
    - [x] `NetworkUtils.isNetworkAvailable(context: Context)`.
    - [x] Chequear antes de cada llamada a red.
- [x] **Modo Offline / Caché (opcional)**:
    - [x] Configurar `OkHttp` para caché local.
    - [x] Permitir mostrar “última imagen generada” aunque no haya red.

## 11. Accesibilidad (Accessibility)
- [x] Etiquetas `contentDescription` en todos los `ImageView` y `Buttons` importantes.
- [x] Asegurarse de que los Contrast ratios (texto/fondo) cumplan con WCAG AA.
- [x] Soporte navegación con TalkBack (orden lógico de foco, mensajes claros).

## 12. Internacionalización (i18n)
- [x] Extraer Strings a `strings.xml`.
- [x] Soporte para otros idiomas (crear `res/values-en/strings.xml`, etc.).

## 13. Analytics y Métricas
- [x] **Firebase Analytics**:
    - [x] Eventos recomendados: `event_generate_attempt`, `event_ad_viewed`, `event_purchase`.
    - [x] Segmentación.
- [x] **Monitoreo de Rendimiento**:
    - [x] Usar Firebase Performance Monitoring para medir tiempos de llamada a la API.

## 14. Tests y Calidad de Código
- [x] **Unit Tests (JUnit + Mockito/Kotlinx-Coroutines-Test)**:
    - [x] Cobertura recomendada: `UsageLimiter`, `ImageProcessor`, `DateUtils`, `BillingRepository`, `AdRepository`.
    - [x] Testear flujos de error.
    - [x] Usar `FakeApiService` para no golpear la red real.
- [x] **Instrumented Tests (AndroidJUnitRunner, Espresso)**:
    - [x] Pruebas de UI para selección de imagen, anuncios, compras.
    - [x] Usar `Billing Test SKU` para simular compra.
- [ ] **Lint and Code Formatting**:
    - [x] Configure `lintOptions { abortOnError true }` in `build.gradle`.
    - [ ] Integrate Detekt or Ktlint. (Deferred)

## 15. CI/CD and Continuous Delivery
- [ ] **GitHub Actions / Bitrise / GitLab CI**:
    - [ ] Configure workflow for: Checkout, Lint, Unit Tests, Instrumented Tests, Build signed APK, Automatic Deploy to Google Play Store. (Deferred)

## 16. Security and Best Practices
- [x] **ProGuard / R8**:
    - [x] Minimize and obfuscate (default Android setup).
    - [ ] Configure specific rules for Retrofit and Room. (Deferred)
- [x] **HTTPS / TLS**:
    - [x] Force HTTPS connections (handled by OkHttp by default).
    - [ ] Validate certificates (SSL pinning, optional). (Deferred)
- [x] **Credential Storage**:
    - [x] Never save API Key in plain text in `SharedPreferences`. Only in `BuildConfig`.

## 17. Performance and Optimization
- [x] **Generated Image Caching**:
    - [x] Use Glide/Coil with caching parameters (`diskCacheStrategy`).
- [ ] **Lazy Loading in RecyclerView (if you decide to show history)**:
    - [ ] Implement PagedList or Paging 3. (Deferred, as history UI is not built yet)
- [x] **Avoid Memory Leaks**:
    - [x] Do not retain references to Activity/Context in singletons.
    - [x] Cancel coroutines in `onCleared()` of ViewModel.
- [ ] **FPS Monitoring (optional)**:
    - [ ] Use Android Systrace or Profile GPU Rendering. (Deferred)

## 18. Version Control and Releases
- [ ] **Semantic Versioning (SemVer)**:
    - [ ] Format: MAJOR.MINOR.PATCH. (Deferred)
- [ ] **Changelog**:
    - [ ] Maintain a `CHANGELOG.md` file. (Deferred)
- [ ] **Git Tags**:
    - [ ] Use Git tags to mark versions. (Deferred)

## 19. Expanded List of Possible Errors / Edge Cases
- [x] **Image Selection**: URI null/inaccesible, user cancels.
- [x] **Daily Usage / Credits**: Different local date, corrupted `SharedPreferences`.
- [x] **API Call**: Frequent timeouts, unexpected JSON format response.
- [x] **Ads**: User sees ad but does not receive reward, ID of ad misconfigured.
- [x] **Purchases**: Invalid `Purchase.signature`, `BillingClient` disconnects.
- [x] **Database (Room)**: Image insertion with null/empty URL.
- [x] **Rotation Handling**: Activity destroyed and reference lost.
- [x] **Multithreading / Concurrency**: Two coroutines calling `incrementUsage()` at the same time.
- [x] **Permissions**: User denies permanently.
- [x] **Session Management**: Daily limit does not reset correctly at midnight.
- [x] **Logging / Crashlytics**: Uncaught exception.
- [x] **Image Storage**: "Save to gallery" intent fails.

## 20. Cron Tasks and Synchronization (Optional)
- [ ] **Daily Reset Worker**:
    - [ ] With WorkManager, schedule a worker to run at 00:01 local time to force daily count reset. (Deferred)
- [ ] **Purchase Synchronization**:
    - [ ] Every month (or every time the app starts), query `BillingClient.queryPurchasesAsync()` to ensure no “orphan” purchases. (Deferred)

## 21. Documentation and Comments
- [x] **README.md**:
    - [x] General app description.
    - [x] Instructions to configure `local.properties`.
    - [ ] Steps to run tests. (Deferred)
- [ ] **Internal Wiki / Dossier (optional)**:
    - [ ] High-level architecture. (Deferred)
    - [ ] Detailed flow diagram. (Deferred)
    - [ ] Code style guide. (Deferred)
- [x] **Code Comments**:
    - [x] Each public class and complex method should have explanatory KDoc.
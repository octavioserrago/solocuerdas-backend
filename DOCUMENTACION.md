# SoloCuerdas — Documentación del Proyecto

## Descripción general

SoloCuerdas es un marketplace mobile para la compra y venta de instrumentos de cuerda (guitarras acústicas, eléctricas, clásicas, bajos, ukeleles, violines, cellos, mandolinas, banjos, etc.) entre particulares en Argentina.

El proyecto fue desarrollado como tesis universitaria e implementa un flujo completo de compra-venta: publicación de anuncios, sistema de consultas entre comprador y vendedor, chat en tiempo real (polling), confirmación de transacción presencial mediante código de 6 dígitos, y reseñas obligatorias post-venta.

---

## Estructura del repositorio

```
tesis/
├── solocuerdas/           # App móvil — React Native + Expo
└── solocuerdas-backend/   # API REST — Spring Boot (Java)
```

---

## Frontend — `solocuerdas/`

### Stack tecnológico
- **React Native** con **Expo**
- **React Navigation** (Stack + Bottom Tabs)
- **Expo Notifications** para push notifications
- **expo-image-picker** para selección y compresión de imágenes

### Estructura de carpetas

```
solocuerdas/
├── App.js                  # Punto de entrada, navegación, listeners de notificaciones
├── AuthContext.js           # Estado global de autenticación (usuario, login, logout, etc.)
├── UnreadContext.js         # Contador global de mensajes no leídos (badge en tab)
├── apiConfig.js             # Endpoints centralizados y función buildApiUrl
├── screens/
│   ├── HomeScreen.js        # Feed de publicaciones, búsqueda y filtro por categoría
│   ├── DetallePublicacionScreen.js  # Detalle de una publicación
│   ├── VenderScreen.js      # Formulario para crear una publicación
│   ├── MisPublicacionesScreen.js    # Mis publicaciones (CRUD, cambio de estado)
│   ├── ConsultasScreen.js   # Lista de consultas como comprador y como vendedor
│   ├── ChatScreen.js        # Chat por consulta + flujo de transacción
│   ├── ResenaScreen.js      # Formulario de reseña post-transacción
│   ├── FavoritosScreen.js   # Publicaciones guardadas como favoritos
│   ├── CuentaScreen.js      # Perfil, suscripción, cambio de contraseña, reseñas recibidas
│   ├── IniciarSesionScreen.js  # Login
│   ├── RegistroScreen.js    # Registro de nuevo usuario
│   └── FaqScreen.js         # Preguntas frecuentes
├── services/
│   ├── apiClient.js         # parseResponse, protectedHeaders, ensureUserId
│   ├── publicationsService.js  # Funciones para publicaciones y favoritos
│   ├── inquiriesService.js  # Funciones para consultas, mensajes, transacciones y reseñas
│   ├── subscriptionService.js  # Funciones para planes de suscripción
│   └── notificationsService.js # Registro y limpieza del Expo Push Token
├── components/
│   ├── PaywallModal.js      # Modal para upgrade de plan cuando se supera el límite de publicaciones
│   └── UserAvatar.js        # Avatar de usuario con inicial
├── utils/
│   └── imageUtils.js        # Compresión de imágenes y permisos de galería
└── data/
    └── ubicaciones.js       # Provincias y ciudades de Argentina para el formulario de publicación
```

### Navegación

```
Stack (MainNavigator)
├── Si NO autenticado:
│   ├── IniciarSesion
│   └── Registro
└── Si autenticado:
    ├── MainTabs (Bottom Tab Navigator)
    │   ├── Inicio (HomeScreen)
    │   ├── Favoritos (FavoritosScreen)
    │   ├── ConsultasTab (ConsultasScreen) — badge con mensajes no leídos
    │   └── Cuenta (CuentaScreen)
    ├── Vender
    ├── MisPublicaciones
    ├── DetallePublicacion
    ├── Chat
    ├── Resena
    └── Faq
```

### Autenticación

No se usa JWT en las requests. La autenticación temporal se implementa mediante el header `X-User-Id` que se envía en todas las llamadas protegidas.

```js
// Ejemplo de header protegido
headers: { 'X-User-Id': String(userId) }
```

El usuario autenticado se almacena en `AuthContext` en memoria (sin persistencia en disco). Si la app se cierra, el usuario debe volver a iniciar sesión.

### Configuración de API

El archivo `apiConfig.js` centraliza la URL base y todos los endpoints. Para cambiar el servidor basta con modificar `API_BASE_URL`.

```js
export const API_BASE_URL = 'http://192.168.0.128:8080';
```

> Para dispositivo físico: cambiar la IP a la de la computadora en la red local.

---

## Backend — `solocuerdas-backend/`

### Stack tecnológico
- **Java 17** con **Spring Boot 3**
- **Spring Data JPA** + **Hibernate** (ORM)
- **Spring Security** (actualmente con toda autenticación deshabilitada para desarrollo)
- **BCrypt** para hashing de contraseñas
- **Cloudinary** para almacenamiento de imágenes
- **Expo Push Notifications** para notificaciones push a dispositivos móviles
- Base de datos: configurable vía `application.properties`

### Estructura del código

```
src/main/java/com/solocuerdas/solocuerdas_backend/
├── controller/
│   ├── UsuarioController.java        # /api/users
│   ├── PublicationController.java    # /api/publications
│   ├── InquiryController.java        # /api/inquiries
│   ├── MessageController.java        # /api/inquiries/{id}/messages
│   ├── TransactionController.java    # /api/inquiries/{id}/transaction, /api/transactions/{id}
│   └── ReviewController.java         # /api/transactions/{id}/review, /api/users/{id}/rating
├── service/
│   ├── UsuarioService.java
│   ├── PublicationService.java
│   ├── FavoriteService.java
│   ├── InquiryService.java
│   ├── MessageService.java
│   ├── TransactionService.java
│   ├── ReviewService.java
│   ├── CloudinaryService.java
│   └── PushNotificationService.java
├── model/
│   ├── Usuario.java
│   ├── Publication.java
│   ├── Inquiry.java
│   ├── Message.java
│   ├── Transaction.java
│   ├── Review.java
│   ├── Favorite.java
│   ├── Category.java          # Enum: ACOUSTIC_GUITAR, ELECTRIC_GUITAR, etc.
│   ├── Condition.java         # Enum: NEW, LIKE_NEW, EXCELLENT, GOOD, FAIR
│   ├── PublicationStatus.java # Enum: ACTIVE, PAUSED, SOLD, DELETED
│   ├── InquiryStatus.java     # Enum: OPEN, ACCEPTED, REJECTED, CANCELLED, CLOSED
│   ├── TransactionStatus.java # Enum: AWAITING_BUYER_CODE, COMPLETED, CANCELLED, EXPIRED
│   ├── ReviewType.java        # Enum: BUYER_TO_SELLER, SELLER_TO_BUYER
│   ├── Role.java              # Enum: USER, MODERATOR, ADMIN
│   ├── SubscriptionPlan.java  # Enum: FREE, HOBBY, BUSINESS
│   └── SubscriptionStatus.java # Enum: NONE, ACTIVE, GRACE_PERIOD, EXPIRED, CANCELLED
├── dto/                        # Request y Response objects
├── repository/                 # Interfaces JPA
├── config/
│   ├── SecurityConfig.java    # Security deshabilitada para desarrollo
│   └── CloudinaryConfig.java
└── exception/
    └── PublicationLimitConflictException.java
```

---

## API Reference

Todas las rutas protegidas requieren el header:
```
X-User-Id: {userId}
```

### Usuarios — `/api/users`

| Método | Endpoint | Descripción | Auth |
|--------|----------|-------------|------|
| POST | `/api/users/register` | Registrar usuario. Body: `{name, email, password}` | No |
| POST | `/api/users/login` | Login. Body: `{email, password}` | No |
| GET | `/api/users/{id}` | Obtener usuario por ID | No |
| GET | `/api/users/email/{email}` | Obtener usuario por email | No |
| PUT | `/api/users/{id}/profile` | Actualizar perfil. Body: `{name, phone}` | Sí |
| PUT | `/api/users/{id}/password` | Cambiar contraseña. Body: `{currentPassword, newPassword}` | Sí |
| GET | `/api/users/{id}/subscription` | Ver suscripción actual | Sí |
| GET | `/api/users/subscription/plans` | Listar planes disponibles | No |
| PUT | `/api/users/{id}/subscription/plan` | Cambiar plan. Body: `{plan: "HOBBY"\|"BUSINESS"\|"FREE"}` | Sí |
| POST | `/api/users/{id}/subscription/renew` | Renovar suscripción | Sí |
| POST | `/api/users/{id}/subscription/cancel` | Cancelar suscripción (puede retornar 409 si hay más publicaciones activas que el límite FREE) | Sí |
| POST | `/api/users/{id}/subscription/cancel/confirm` | Confirmar cancelación desactivando publicaciones. Body: `{publicationIdsToDeactivate: [1,2]}` | Sí |
| POST | `/api/users/{id}/push-token` | Registrar token Expo. Body: `{token: "ExponentPushToken[...]"}` | Sí |
| GET | `/api/users/{userId}/rating` | Ver rating del usuario | No |
| GET | `/api/users/{userId}/reviews/as-seller` | Reseñas recibidas como vendedor | No |
| GET | `/api/users/{userId}/reviews/as-buyer` | Reseñas recibidas como comprador | No |

**Respuesta de `/register` y `/login`:**
```json
{
  "id": 1,
  "name": "Juan Pérez",
  "email": "juan@example.com",
  "phone": null,
  "registrationDate": "2025-01-15T10:30:00",
  "role": "USER",
  "subscriptionPlan": "FREE",
  "subscriptionStatus": "NONE",
  "subscriptionEndDate": null,
  "isSuspended": false,
  "isDeleted": false
}
```

---

### Publicaciones — `/api/publications`

| Método | Endpoint | Descripción | Auth |
|--------|----------|-------------|------|
| GET | `/api/publications` | Listar publicaciones activas | No |
| GET | `/api/publications/{id}` | Obtener publicación por ID | No |
| POST | `/api/publications` | Crear publicación | Sí |
| PUT | `/api/publications/{id}` | Actualizar publicación | Sí |
| DELETE | `/api/publications/{id}` | Eliminar publicación | Sí |
| GET | `/api/publications/my-publications` | Mis publicaciones | Sí |
| GET | `/api/publications/user/{userId}` | Publicaciones de un usuario | No |
| PATCH | `/api/publications/{id}/status?status=PAUSED` | Cambiar estado | Sí |
| POST | `/api/publications/{id}/images` | Subir imágenes (multipart) | Sí |
| DELETE | `/api/publications/{id}/images?imageUrl=...` | Eliminar imagen | Sí |
| GET | `/api/publications/search?keyword=fender` | Buscar publicaciones | No |
| GET | `/api/publications/category/{category}` | Filtrar por categoría | No |
| POST | `/api/publications/{id}/favorite` | Agregar a favoritos | Sí |
| DELETE | `/api/publications/{id}/favorite` | Quitar de favoritos | Sí |
| GET | `/api/publications/{id}/favorite/status` | Ver si está en favoritos | Sí |
| GET | `/api/publications/my-favorites` | Mis favoritos | Sí |

**Categorías válidas:** `ACOUSTIC_GUITAR`, `ELECTRIC_GUITAR`, `CLASSICAL_GUITAR`, `BASS`, `UKULELE`, `VIOLIN`, `CELLO`, `MANDOLIN`, `BANJO`, `OTHER_STRING`

**Estados válidos:** `ACTIVE`, `PAUSED`, `SOLD`

**Condiciones válidas:** `NEW`, `LIKE_NEW`, `EXCELLENT`, `GOOD`, `FAIR`

**Body para crear publicación:**
```json
{
  "title": "Guitarra Acústica Fender",
  "description": "...",
  "price": 150000,
  "category": "ACOUSTIC_GUITAR",
  "condition": "EXCELLENT",
  "brand": "Fender",
  "year": 2020,
  "location": "Buenos Aires, CABA"
}
```

**Límites de publicaciones por plan:**
- FREE: 3 publicaciones activas
- HOBBY: 15 publicaciones activas
- BUSINESS: ilimitadas

---

### Indicadores de consultas pendientes

El sistema diferencia dos tipos de notificaciones en `ConsultasScreen`:

- **Mensajes no leídos** (`unreadMessages > 0`): dot naranja + contador "X sin leer" en la card
- **Consultas OPEN sin responder** (solo para el vendedor): borde izquierdo naranja en la card + texto "Requiere respuesta" + badge en la pestaña "Recibidas"

El badge del tab inferior (icono de chatbubbles) suma ambos: mensajes no leídos + consultas recibidas sin responder.

---

### Consultas — `/api/inquiries`

| Método | Endpoint | Descripción | Auth |
|--------|----------|-------------|------|
| POST | `/api/inquiries` | Crear consulta (comprador). Body: `{publicationId, message}` | Sí |
| GET | `/api/inquiries/{id}` | Obtener consulta por ID | Sí |
| GET | `/api/inquiries/as-buyer` | Mis consultas como comprador | Sí |
| GET | `/api/inquiries/as-seller` | Consultas recibidas como vendedor | Sí |
| POST | `/api/inquiries/{id}/accept` | Aceptar consulta (vendedor) | Sí |
| POST | `/api/inquiries/{id}/reject` | Rechazar consulta (vendedor) | Sí |
| POST | `/api/inquiries/{id}/cancel` | Cancelar consulta (comprador) | Sí |

**Estados de consulta:** `OPEN` → `ACCEPTED` / `REJECTED` / `CANCELLED` → `CLOSED`

---

### Mensajes — `/api/inquiries/{inquiryId}/messages`

| Método | Endpoint | Descripción | Auth |
|--------|----------|-------------|------|
| POST | `/api/inquiries/{inquiryId}/messages` | Enviar mensaje. Body: `{body}` | Sí |
| GET | `/api/inquiries/{inquiryId}/messages` | Obtener mensajes (marca como leídos) | Sí |

Solo disponible para consultas en estado `ACCEPTED`.

---

### Transacciones

| Método | Endpoint | Descripción | Auth |
|--------|----------|-------------|------|
| POST | `/api/inquiries/{inquiryId}/transaction` | Iniciar transacción (vendedor). Body: `{agreedPrice}` (opcional) | Sí |
| GET | `/api/inquiries/{inquiryId}/transaction` | Ver transacción por consulta | Sí |
| GET | `/api/transactions/{transactionId}` | Ver transacción por ID | Sí |
| POST | `/api/transactions/{transactionId}/confirm` | Confirmar con código (comprador). Body: `{confirmationCode}` | Sí |
| POST | `/api/transactions/{transactionId}/cancel` | Cancelar transacción (vendedor) | Sí |

**Flujo de transacción:**
1. Vendedor inicia transacción → recibe código de 6 dígitos para mostrar en persona
2. Comprador ingresa el código en la app → transacción completada
3. Publicación pasa a estado `SOLD`, consulta pasa a `CLOSED`
4. Ambos usuarios quedan obligados a dejar una reseña

**Estados de transacción:** `AWAITING_BUYER_CODE` → `COMPLETED` / `CANCELLED` / `EXPIRED`

---

### Reseñas

| Método | Endpoint | Descripción | Auth |
|--------|----------|-------------|------|
| POST | `/api/transactions/{transactionId}/review` | Crear reseña. Body: `{rating: 1-5, comment}` | Sí |
| GET | `/api/users/{userId}/reviews/as-seller` | Reseñas recibidas como vendedor | No |
| GET | `/api/users/{userId}/reviews/as-buyer` | Reseñas recibidas como comprador | No |
| GET | `/api/users/{userId}/rating` | Resumen de rating del usuario | No |

Las reseñas son obligatorias y deben enviarse dentro de los 7 días de completada la transacción. Los usuarios con reseña pendiente no pueden crear nuevas consultas (anti-fraude).

---

## Planes de suscripción

| Plan | Publicaciones activas | Precio |
|------|-----------------------|--------|
| FREE | 3 | Gratis |
| HOBBY | 15 | USD x/mes |
| BUSINESS | Ilimitadas | USD x/mes |

Los precios exactos se sirven desde el backend en `/api/users/subscription/plans`.

**Flujo de cancelación con conflicto:**  
Si el usuario tiene más de 3 publicaciones activas y quiere bajar a FREE, el backend responde `409 Conflict` con la lista de publicaciones activas. El frontend muestra un modal para que el usuario seleccione cuáles pausar, y luego llama a `/cancel/confirm`.

---

## Push Notifications

Se usan **Expo Push Notifications**. El token se registra en el backend al hacer login y se limpia al hacer logout.

**Eventos que disparan notificaciones:**
- Nueva consulta recibida (vendedor)
- Consulta aceptada / rechazada (comprador)
- Transacción iniciada — reunión confirmada (comprador)
- Transacción completada — recordatorio de reseña (ambos)

**Navegación desde notificación:**
- Si el payload tiene `inquiryId`: navega al `ChatScreen`
- Si el payload tiene `transactionId`: navega a `ResenaScreen`

---

## Modelos de datos

### Usuario
```
id, name, email, password (bcrypt), phone,
registrationDate, role (USER|MODERATOR|ADMIN),
subscriptionPlan, subscriptionStatus, subscriptionStartDate,
subscriptionEndDate, gracePeriodEndDate, extraPostsPurchased,
ratingAsSeller, ratingAsBuyer, totalSales, totalPurchases,
hasPendingReview, expoPushToken,
isSuspended, suspendedUntil, suspendedBy, suspensionReason,
isDeleted, deletedAt, deletedBy, deletionReason
```

### Publication
```
id, title, description, price, category, condition,
brand, year, location, images (List<String> URLs),
status, user, viewsCount, createdAt, updatedAt, soldAt
```

### Inquiry
```
id, buyer, seller, publication, message,
status, createdAt, updatedAt
```

### Message
```
id, inquiry, sender, body, sentAt, readAt
```

### Transaction
```
id, inquiry, buyer, seller, publication,
agreedPrice, confirmationCode (6 dígitos), status,
codeExpiresAt, sellerConfirmedAt, buyerConfirmedAt, createdAt
```

### Review
```
id, transaction, reviewer, reviewed,
rating (1-5), comment, type (BUYER_TO_SELLER|SELLER_TO_BUYER), createdAt
```

---

## Seguridad

**Estado actual (desarrollo):** Spring Security está configurado con `permitAll()` — todas las rutas son públicas. La autorización se hace manualmente en cada endpoint comparando el `X-User-Id` header con el recurso solicitado.

**Pendiente para producción:**
- Implementar JWT (JSON Web Tokens)
- Reemplazar el header `X-User-Id` por el token JWT para autenticación real
- Habilitar HTTPS

---

## Flujos principales

### Crear una publicación
1. Usuario completa el formulario en `VenderScreen`
2. Se llama a `POST /api/publications` (sin imágenes)
3. Si hay imágenes, se llaman a `POST /api/publications/{id}/images`
4. Si el backend responde 400 con mensaje de límite alcanzado, se muestra `PaywallModal` para hacer upgrade de plan

### Comprar un instrumento (flujo completo)
1. Comprador ve publicación en `HomeScreen` o `FavoritosScreen`
2. Entra al `DetallePublicacionScreen` y hace clic en "Consultar"
3. Se crea la consulta → aparece en `ConsultasScreen` del vendedor
4. Vendedor entra al `ChatScreen` → acepta o rechaza
5. Si acepta: se habilita el chat y el botón "Confirmar venta" para el vendedor
6. Vendedor inicia transacción → ve el código de 6 dígitos
7. En persona, comprador ingresa el código en su app
8. Transacción completada → publicación marcada como SOLD
9. Ambos son redirigidos a `ResenaScreen` para dejar su reseña

---

## Notas de plataforma

### Android — edge-to-edge
`app.json` tiene `"android": { "edgeToEdgeEnabled": true }`, lo que hace que la app se dibuje detrás de las barras del sistema (status bar y navigation bar). Por esto es obligatorio usar `useSafeAreaInsets` de `react-native-safe-area-context` en pantallas con contenido cerca del borde inferior.

En `ChatScreen.js` el `inputBar` usa `paddingBottom: 12 + insets.bottom` para quedar por encima de la barra de navegación del sistema, y `KeyboardAvoidingView` usa `behavior='height'` en Android para que el teclado desplace el input hacia arriba.

### iOS — safe area
En iPhone con home indicator, `insets.bottom` es ~34pt. El mismo `paddingBottom: 12 + insets.bottom` asegura que la barra de input no quede pegada al borde inferior. `KeyboardAvoidingView` usa `behavior='padding'` con `keyboardVerticalOffset={90}`.

---

## Variables de entorno / Configuración

### Frontend (`apiConfig.js`)
```js
API_BASE_URL = 'http://<IP_LOCAL>:8080'
```

### Backend (`application.properties`)
Configurar:
- URL y credenciales de base de datos
- Credenciales de Cloudinary (`cloud_name`, `api_key`, `api_secret`)
- Puerto del servidor (default: 8080)

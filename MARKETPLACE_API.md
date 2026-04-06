# Marketplace API — Flujo de compraventa

Base URL: `http://localhost:8080`

Todos los endpoints que requieren identidad usan el header `X-User-Id: <userId>` (el ID del usuario logueado, extraído del token JWT o session en el frontend).

---

## Flujo completo paso a paso

```
[Comprador] Ve publicación activa
    → POST /api/inquiries              (expresa interés)
    ← 201 InquiryResponse { id, status: "OPEN" }

[Vendedor] Recibe notificación / ve lista de consultas
    → POST /api/inquiries/{id}/accept
    ← 200 InquiryResponse { status: "ACCEPTED" }

[Ambos] Chat habilitado
    → POST /api/inquiries/{id}/messages   (enviar mensaje)
    → GET  /api/inquiries/{id}/messages   (ver hilo + marca leídos)

[Coordinan por chat: precio, lugar, hora]

[Vendedor] Confirma la venta en la app
    → POST /api/inquiries/{id}/transaction  { "agreedPrice": 1500.00 }
    ← 201 TransactionResponse { confirmationCode: "482931", status: "AWAITING_BUYER_CODE" }
       ⚠️ El código solo aparece en la respuesta del VENDEDOR. Mostrarlo en pantalla para que el comprador lo vea en persona.

[Se juntan en persona — el vendedor le muestra el código en su celular]

[Comprador] Ingresa el código en la app
    → POST /api/transactions/{transactionId}/confirm  { "confirmationCode": "482931" }
    ← 200 TransactionResponse { status: "COMPLETED" }
       → Publicación pasa automáticamente a SOLD
       → Ambos quedan con reseña pendiente obligatoria

[Ambos] Dejan su reseña (obligatoria — 7 días)
    → POST /api/transactions/{transactionId}/review  { "rating": 5, "comment": "..." }
    ← 201 ReviewResponse
       → Cuando ambos reseñan, la bandera "pendingReview" se limpia y pueden iniciar nuevas consultas
```

---

## 1. Inquiries (Consultas)

### Crear consulta
```
POST /api/inquiries
Header: X-User-Id: {buyerId}
Body:
{
  "publicationId": 12,
  "message": "Hola, me interesa la guitarra. ¿Sigue disponible?"   // opcional
}
Response 201:
{
  "id": 1,
  "buyerId": 5,
  "buyerName": "Juan Pérez",
  "sellerId": 3,
  "sellerName": "María García",
  "publicationId": 12,
  "publicationTitle": "Gibson Les Paul 2019",
  "message": "Hola, me interesa la guitarra...",
  "status": "OPEN",
  "createdAt": "2026-04-06T10:00:00",
  "updatedAt": "2026-04-06T10:00:00",
  "unreadMessages": 0
}
```
**Errores posibles:**
- `400` usuario tiene reseña pendiente → debe enviarla antes de crear nuevas consultas
- `400` ya tiene una consulta activa para esa publicación
- `400` la publicación no está ACTIVE
- `400` no puede consultar su propia publicación

---

### Ver consulta por ID
```
GET /api/inquiries/{id}
Header: X-User-Id: {userId}   // debe ser comprador o vendedor de esa consulta
Response 200: InquiryResponse (ver estructura arriba)
```

---

### Ver mis consultas como comprador
```
GET /api/inquiries/as-buyer
Header: X-User-Id: {buyerId}
Response 200: [ InquiryResponse, ... ]   // ordenadas por más reciente
```

---

### Ver mis consultas como vendedor
```
GET /api/inquiries/as-seller
Header: X-User-Id: {sellerId}
Response 200: [ InquiryResponse, ... ]   // ordenadas por más reciente
// El campo "unreadMessages" indica cuántos mensajes sin leer hay en cada hilo
```

---

### Aceptar consulta (vendedor)
```
POST /api/inquiries/{id}/accept
Header: X-User-Id: {sellerId}
Response 200: InquiryResponse { status: "ACCEPTED" }
// Habilita el chat para ambas partes
```

---

### Rechazar consulta (vendedor)
```
POST /api/inquiries/{id}/reject
Header: X-User-Id: {sellerId}
Response 200: InquiryResponse { status: "REJECTED" }
```

---

### Cancelar consulta (comprador)
```
POST /api/inquiries/{id}/cancel
Header: X-User-Id: {buyerId}
Response 200: InquiryResponse { status: "CANCELLED" }
```

---

### Posibles estados de una Inquiry
| Status | Descripción |
|---|---|
| `OPEN` | Consulta enviada, esperando respuesta del vendedor |
| `ACCEPTED` | Vendedor aceptó — chat habilitado |
| `REJECTED` | Vendedor rechazó |
| `CANCELLED` | Comprador canceló |
| `CLOSED` | Cerrada automáticamente al completarse la transacción |

---

## 2. Mensajes (Chat)

Solo disponible para consultas con status `ACCEPTED`.

### Enviar mensaje
```
POST /api/inquiries/{inquiryId}/messages
Header: X-User-Id: {senderId}
Body:
{
  "body": "¿Podemos vernos el sábado en Palermo?"
}
Response 201:
{
  "id": 1,
  "inquiryId": 1,
  "senderId": 5,
  "senderName": "Juan Pérez",
  "body": "¿Podemos vernos el sábado en Palermo?",
  "sentAt": "2026-04-06T10:15:00",
  "readAt": null
}
```

---

### Ver todos los mensajes de una consulta
```
GET /api/inquiries/{inquiryId}/messages
Header: X-User-Id: {requesterId}
Response 200: [ MessageResponse, ... ]   // ordenados cronológicamente
// Los mensajes no leídos del otro participante se marcan como leídos automáticamente
```

**Recomendación para el frontend:** hacer polling cada 5–10 segundos en la pantalla de chat.

---

## 3. Transacciones

### Iniciar transacción (vendedor)
```
POST /api/inquiries/{inquiryId}/transaction
Header: X-User-Id: {sellerId}
Body:
{
  "agreedPrice": 1500.00   // opcional — si no se envía, usa el precio de la publicación
}
Response 201:
{
  "id": 1,
  "inquiryId": 1,
  "buyerId": 5,
  "buyerName": "Juan Pérez",
  "sellerId": 3,
  "sellerName": "María García",
  "publicationId": 12,
  "publicationTitle": "Gibson Les Paul 2019",
  "agreedPrice": 1500.00,
  "status": "AWAITING_BUYER_CODE",
  "confirmationCode": "482931",   // ⚠️ SOLO aparece aquí. Mostrar en pantalla grande.
  "codeExpiresAt": "2026-04-07T10:00:00",
  "sellerConfirmedAt": "2026-04-06T10:00:00",
  "buyerConfirmedAt": null,
  "createdAt": "2026-04-06T10:00:00"
}
```
⚠️ **Importante:** el `confirmationCode` solo aparece en la respuesta del vendedor al iniciar. En consultas posteriores (GET) el código solo es visible para el vendedor mientras el status sea `AWAITING_BUYER_CODE`. **Nunca mostrarlo al comprador** — debe ingresarlo al ver la pantalla del vendedor en persona.

---

### Ver transacción de una consulta
```
GET /api/inquiries/{inquiryId}/transaction
Header: X-User-Id: {requesterId}
Response 200: TransactionResponse
// confirmationCode solo aparece si requesterId == sellerId y status == AWAITING_BUYER_CODE
```

---

### Confirmar transacción (comprador — en persona)
```
POST /api/transactions/{transactionId}/confirm
Header: X-User-Id: {buyerId}
Body:
{
  "confirmationCode": "482931"
}
Response 200: TransactionResponse { status: "COMPLETED" }
// La publicación pasa a SOLD automáticamente
// Ambos quedan con hasPendingReview = true
```
**Errores posibles:**
- `400` código incorrecto
- `400` código expirado (la transacción pasa a EXPIRED)
- `400` no sos el comprador

---

### Cancelar transacción (vendedor, antes de que el comprador confirme)
```
POST /api/transactions/{transactionId}/cancel
Header: X-User-Id: {sellerId}
Response 200: TransactionResponse { status: "CANCELLED" }
```

---

### Posibles estados de una Transaction
| Status | Descripción |
|---|---|
| `AWAITING_BUYER_CODE` | Transacción iniciada — esperando que el comprador ingrese el código |
| `COMPLETED` | Código ingresado correctamente — venta cerrada |
| `CANCELLED` | Cancelada por el vendedor antes de confirmarse |
| `EXPIRED` | El código venció sin ser usado (24 horas) |

---

## 4. Reseñas

### Enviar reseña (obligatoria — 7 días desde COMPLETED)
```
POST /api/transactions/{transactionId}/review
Header: X-User-Id: {reviewerId}   // puede ser comprador o vendedor
Body:
{
  "rating": 5,       // entero 1-5
  "comment": "Excelente vendedor, puntual y honesto."   // obligatorio
}
Response 201:
{
  "id": 1,
  "transactionId": 1,
  "reviewerId": 5,
  "reviewerName": "Juan Pérez",
  "reviewedId": 3,
  "reviewedName": "María García",
  "rating": 5,
  "comment": "Excelente vendedor...",
  "type": "BUYER_REVIEWS_SELLER",
  "createdAt": "2026-04-06T15:00:00"
}
```
**Errores posibles:**
- `400` ya enviaste tu reseña para esta transacción
- `400` el período de 7 días venció
- `400` la transacción no está COMPLETED
- `400` rating fuera de rango o comment vacío

---

### Ver reseñas de un usuario como vendedor
```
GET /api/users/{userId}/reviews/as-seller
Response 200: [ ReviewResponse, ... ]   // ordenadas por más reciente
```

---

### Ver reseñas de un usuario como comprador
```
GET /api/users/{userId}/reviews/as-buyer
Response 200: [ ReviewResponse, ... ]
```

---

### Ver puntaje de un usuario (perfil público)
```
GET /api/users/{userId}/rating
Response 200:
{
  "userId": 3,
  "userName": "María García",
  "ratingAsSeller": 4.80,    // null si no tiene ventas
  "totalSales": 12,
  "ratingAsBuyer": 5.00,     // null si no tiene compras
  "totalPurchases": 3
}
```
**Mostrar siempre en el perfil** — si ambos valores son null mostrar "Sin historial de transacciones".

---

## Reglas de negocio importantes para el frontend

| Situación | Qué hacer |
|---|---|
| Usuario con `hasPendingReview: true` | Bloquear creación de nuevas consultas. Mostrar banner: "Tenés una reseña pendiente" con link a la transacción correspondiente |
| Código de confirmación | Mostrarlo grande y claro en la pantalla del vendedor para que el comprador lo vea en persona. No copiar/pegar, no compartir por chat |
| Código expirado | Aviso al comprador: "El código venció. Pedile al vendedor que reinicie la transacción" |
| Consulta REJECTED | Opcional: permitir al comprador enviar una nueva consulta para la misma publicación si esta fue rechazada |
| Chat en consulta no ACCEPTED | Mostrar el campo de mensajes deshabilitado con texto "El chat se habilita cuando el vendedor acepte tu consulta" |

---

## Tipos de ReviewType
| Valor | Descripción |
|---|---|
| `BUYER_REVIEWS_SELLER` | El comprador califica al vendedor |
| `SELLER_REVIEWS_BUYER` | El vendedor califica al comprador |

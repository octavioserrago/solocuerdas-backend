# Push Notifications — Guía para el Frontend

Base URL: `http://localhost:8080`

---

## Qué tiene que hacer el frontend

### 1. Registrar el token al iniciar la app

Cada vez que la app arranca, obtener el `ExpoPushToken` con el SDK de Expo y enviarlo al backend:

```
POST /api/users/{id}/push-token
Header: X-User-Id: {userId}
Body:
{
  "token": "ExponentPushToken[xxxxxxxxxxxxxxxxxxxxxx]"
}
Response: 200 OK
```

> El token puede rotar entre sesiones, por eso hay que enviarlo siempre al abrir la app, no solo en el registro.

### 2. Limpiar el token al hacer logout

```
POST /api/users/{id}/push-token
Header: X-User-Id: {userId}
Body:
{
  "token": null
}
Response: 200 OK
```

---

## Nada más — el resto es automático

El backend dispara las notificaciones por su cuenta. El frontend no necesita llamar ningún endpoint extra para eso.

---

## Eventos que generan notificaciones

| Evento | Quién recibe | Título | Ejemplo de cuerpo |
|---|---|---|---|
| Comprador envía consulta | Vendedor | "Nueva consulta" | "Juan Pérez está interesado en tu publicación «Gibson Les Paul»" |
| Vendedor acepta consulta | Comprador | "¡Consulta aceptada!" | "¡María García aceptó tu consulta por «Gibson Les Paul»! Ya podés chatear." |
| Vendedor rechaza consulta | Comprador | "Consulta rechazada" | "María García no pudo atender tu consulta por «Gibson Les Paul»." |
| Nuevo mensaje en el chat | El otro participante | "Nuevo mensaje de {nombre}" | Primeros 60 caracteres del mensaje |
| Vendedor inicia transacción | Comprador | "¡Reunión confirmada!" | "María García está lista para cerrar la venta de «Gibson Les Paul». Mostrá el código cuando se junten." |
| Comprador confirma con código | Vendedor | "Venta confirmada ✅" | "Juan Pérez confirmó la compra de «Gibson Les Paul». Dejá tu reseña en los próximos 7 días." |
| Comprador confirma con código | Comprador | "Compra confirmada ✅" | "¡Compraste «Gibson Les Paul»! Dejá tu reseña en los próximos 7 días." |
| Alguien deja una reseña | El reseñado | "Recibió una nueva reseña" | "Juan Pérez te dejó una reseña de 5 estrellas." |
| Alguien deja una reseña | El que aún no reseñó | "Reseña pendiente" | "Juan Pérez ya dejó su reseña. Tenés hasta 7 días para enviar la tuya." |

---

## Notas técnicas

- Las notificaciones se envían a la API pública de Expo (`https://exp.host/--/api/v2/push/send`), no requieren Firebase ni APNs configurados en el backend.
- Si el usuario no tiene token registrado, la notificación se omite silenciosamente — no genera error.
- Los fallos de red al enviar a Expo también se ignoran silenciosamente para no interrumpir el flujo principal.
- Permisos de notificación los gestiona Expo en el frontend (`Notifications.requestPermissionsAsync()`).

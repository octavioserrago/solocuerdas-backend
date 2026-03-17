# 🎯 SISTEMA DE ROLES - SOLOCUERDAS
## Especificación Completa de Permisos y Jerarquía

---

## 📊 JERARQUÍA DE ROLES

```
SUPER_ADMIN (Fundador/Propietario)
    ↓
ADMIN (Administradores)
    ↓
MODERATOR (Soporte y Moderación)
    ↓
PRO_SELLER (Suscripción Premium - Ilimitado)
    ↓
VENDEDOR (Suscripción Básica - 10 instrumentos)
    ↓
USER (Gratis - 2 instrumentos)
```

---

## 🎖️ ROL 1: USER (Free)

### Características:
- **Precio:** Gratis
- **Publicaciones:** Máximo 2 instrumentos activos
- **Validez:** Permanente

### Permisos:
✅ Comprar instrumentos
✅ Vender instrumentos (máximo 2 publicaciones)
✅ Gestionar su perfil (nombre, teléfono, foto)
✅ Ver catálogo completo
✅ Comentar/calificar vendedores
✅ Chatear con otros usuarios (a través del sistema)
✅ Crear tickets de soporte
✅ Ver solo sus propias estadísticas de ventas/compras

❌ NO puede publicar más de 2 instrumentos
❌ NO puede ver emails/teléfonos de otros usuarios (solo nombre público)
❌ NO puede cambiar su propio email (debe solicitarlo)
❌ NO puede acceder a datos de otros usuarios

---

## 💎 ROL 2: VENDEDOR (Suscripción Básica)

### Características:
- **Precio:** $X/mes (pago mensual)
- **Publicaciones:** Máximo 10 instrumentos activos
- **Validez:** Mientras esté activa la suscripción
- **Gracia:** 10 días para renovar

### Permisos:
✅ Todo lo de USER +
✅ Publicar hasta 10 instrumentos
✅ Estadísticas básicas de ventas

### Al vencimiento:
- 10 días de gracia para pagar
- Después: Se ocultan todas las publicaciones excepto las 2 más antiguas
- Vuelve a estado FREE hasta renovar

---

## 🌟 ROL 3: PRO_SELLER (Suscripción Premium)

### Características:
- **Precio:** $Y/mes (pago mensual)
- **Publicaciones:** ILIMITADAS
- **Validez:** Mientras esté activa la suscripción
- **Gracia:** 10 días para renovar

### Permisos:
✅ Todo lo de VENDEDOR +
✅ Publicaciones ilimitadas
✅ Destacar hasta 3 productos
✅ Estadísticas avanzadas de ventas
✅ Insignia "Pro Seller"
✅ Soporte prioritario

### Al vencimiento:
- 10 días de gracia para pagar
- Después: Se ocultan todas las publicaciones excepto las 2 más antiguas
- Vuelve a estado FREE hasta renovar

---

## 🛡️ ROL 4: MODERATOR (Soporte y Moderación)

### Características:
- **Tipo:** Empleado/Staff
- **Suscripción:** NO incluye PREMIUM gratis
- **Puede tener:** USER/VENDEDOR/PRO_SELLER si paga

### PERMISOS SOBRE USUARIOS:

#### Suspender/Dessuspender:
✅ Puede suspender usuarios (USER, VENDEDOR, PRO_SELLER)
✅ Puede dessuspender usuarios
✅ Puede suspender otros MODERATOR
❌ NO puede suspender ADMIN ni SUPER_ADMIN

#### Modificar datos:
✅ Puede cambiar: nombre, teléfono, foto del usuario
✅ Puede cambiar email SOLO si el usuario lo solicita → **Requiere aprobación de ADMIN**
❌ NO puede cambiar contraseñas de usuarios

#### Eliminar usuarios:
❌ NO puede eliminar usuarios permanentemente
✅ Puede **elevar casos a ADMIN** para solicitar eliminación
✅ Debe indicar motivo/gravedad para escalamiento

#### Logs y auditoría:
✅ TODOS los cambios que hace MODERATOR quedan registrados:
   - Quién hizo el cambio
   - Qué cambió
   - Fecha y hora
   - Razón del cambio

### PERMISOS SOBRE PUBLICACIONES:

✅ Ver todas las publicaciones (incluso ocultas)
✅ **Ocultar** publicaciones (NO eliminar permanentemente)
✅ **Restaurar** publicaciones ocultas
✅ **DEBE dar una razón** al ocultar
❌ NO puede eliminar publicaciones permanentemente

### PERMISOS DE COMUNICACIÓN:

✅ Ver todas las conversaciones entre usuarios
✅ Intervenir en disputas si:
   - Es notificado por un usuario
   - El sistema detecta palabras clave (estafa, fraude, etc.)
   - Chat inapropiado detectado automáticamente
✅ Gestionar tickets de soporte
✅ Cerrar/resolver tickets

### PERMISOS GENERALES:

✅ Ver datos completos de usuarios (email, teléfono) para soporte
❌ NO puede ver reportes/analytics de la plataforma
❌ NO puede crear cupones
❌ NO puede hacer reembolsos
❌ NO puede cambiar roles de usuarios
❌ NO puede acceder a datos financieros

---

## 👑 ROL 5: ADMIN (Administrador)

### Características:
- **Tipo:** Administrador de la plataforma
- **Cantidad:** Puede haber múltiples ADMIN

### PERMISOS (Todo lo de MODERATOR +):

#### Sobre usuarios:
✅ **Eliminar usuarios PERMANENTEMENTE** (se marcan como "deleted" en BD)
✅ **Aprobar cambios de email** solicitados por MODERATOR
✅ Crear usuarios de cualquier tipo: USER, VENDEDOR, PRO_SELLER, MODERATOR
❌ NO puede crear otros ADMIN (solo SUPER_ADMIN)
❌ NO puede eliminar otros ADMIN

#### Sobre publicaciones:
✅ **Eliminar publicaciones PERMANENTEMENTE**
✅ Todo lo que puede MODERATOR (ocultar, restaurar)

#### Suscripciones:
✅ **Crear cupones** que dan PREMIUM gratis (con duración configurable)
✅ Asignar suscripciones manualmente
✅ Cancelar suscripciones
✅ **Hacer reembolsos**

#### Datos y reportes:
✅ **Ver todos los analytics de la plataforma:**
   - Total de usuarios
   - Total de ventas
   - Ingresos
   - Suscripciones activas
   - Métricas de crecimiento
✅ Exportar reportes (Excel, PDF)
✅ Ver logs de cambios de MODERATOR

#### Configuración:
✅ Cambiar parámetros del sistema:
   - Límite de publicaciones por nivel
   - Precios de suscripción
   - Comisiones
   - Reglas de la plataforma
✅ Crear/editar categorías de instrumentos

#### Protección:
❌ NO puede modificar/eliminar otros ADMIN
❌ NO puede modificar SUPER_ADMIN

---

## ⚡ ROL 6: SUPER_ADMIN (Fundador)

### Características:
- **Tipo:** Propietario/Fundador de la plataforma
- **Cantidad:** 1 único (o máximo 2-3)
- **Protección:** Intocable

### PERMISOS (Todo lo de ADMIN +):

✅ **Crear ADMIN**
✅ **Eliminar ADMIN**
✅ **Modificar cualquier ADMIN**
✅ **Control TOTAL** sobre todos los aspectos del sistema
✅ Acceso a configuraciones críticas de base de datos
✅ Puede modificar/eliminar otros SUPER_ADMIN (si hay más de uno)

---

## 🔒 MATRIZ DE PERMISOS

| Acción | USER | VENDEDOR | PRO | MODERATOR | ADMIN | SUPER |
|--------|------|----------|-----|-----------|-------|-------|
| **USUARIOS** |
| Ver perfil público | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| Ver email/teléfono | ❌ | ❌ | ❌ | ✅ | ✅ | ✅ |
| Modificar su perfil | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| Modificar otros usuarios | ❌ | ❌ | ❌ | ✅* | ✅ | ✅ |
| Suspender usuarios | ❌ | ❌ | ❌ | ✅ | ✅ | ✅ |
| Eliminar usuarios | ❌ | ❌ | ❌ | ❌ | ✅ | ✅ |
| Cambiar roles | ❌ | ❌ | ❌ | ❌ | ✅** | ✅ |
| **PUBLICACIONES** |
| Publicar (cantidad) | 2 | 10 | ∞ | 2*** | 2*** | ∞ |
| Ver publicaciones | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| Editar propias | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| Ocultar publicaciones | ❌ | ❌ | ❌ | ✅ | ✅ | ✅ |
| Eliminar publicaciones | ✅**** | ✅**** | ✅**** | ❌ | ✅ | ✅ |
| **SUSCRIPCIONES** |
| Pagar suscripción | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| Crear cupones | ❌ | ❌ | ❌ | ❌ | ✅ | ✅ |
| Hacer reembolsos | ❌ | ❌ | ❌ | ❌ | ✅ | ✅ |
| **COMUNICACIÓN** |
| Chatear con usuarios | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| Ver conversaciones ajenas | ❌ | ❌ | ❌ | ✅ | ✅ | ✅ |
| Intervenir en disputas | ❌ | ❌ | ❌ | ✅ | ✅ | ✅ |
| **REPORTES** |
| Ver propias stats | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| Ver analytics generales | ❌ | ❌ | ❌ | ❌ | ✅ | ✅ |
| Exportar reportes | ❌ | ❌ | ❌ | ❌ | ✅ | ✅ |
| **ADMINISTRACIÓN** |
| Crear MODERATOR | ❌ | ❌ | ❌ | ❌ | ✅ | ✅ |
| Crear ADMIN | ❌ | ❌ | ❌ | ❌ | ❌ | ✅ |
| Eliminar ADMIN | ❌ | ❌ | ❌ | ❌ | ❌ | ✅ |

**Notas:**
- \* MODERATOR: Solo nombre y teléfono. Email requiere aprobación de ADMIN
- \*\* ADMIN: Solo hasta MODERATOR, no puede crear ADMIN
- \*\*\* MODERATOR/ADMIN: Tienen límite FREE a menos que paguen suscripción
- \*\*\*\* Solo sus propias publicaciones

---

## 🔄 FLUJOS IMPORTANTES

### Cambio de Email (solicitado por usuario):
1. Usuario solicita cambio de email a MODERATOR
2. MODERATOR genera solicitud → envía a ADMIN
3. ADMIN revisa y aprueba/rechaza
4. Si aprueba: se cambia el email
5. Todo queda registrado en logs

### Eliminación de cuenta (escalamiento):
1. MODERATOR detecta infracción grave
2. MODERATOR eleva caso a ADMIN con:
   - Usuario a eliminar
   - Motivo/evidencias
   - Nivel de gravedad
3. ADMIN revisa el caso
4. ADMIN elimina permanentemente (soft delete: marca como "deleted")

### Vencimiento de suscripción:
1. Día 1: Vence la suscripción
2. Días 1-10: Período de gracia (todo funciona normal)
3. Día 11: Se ejecuta:
   - Se ocultan todas las publicaciones
   - Se dejan visibles solo las 2 MÁS ANTIGUAS
   - Usuario vuelve a estado FREE
4. Si paga: Se reactivan todas las publicaciones ocultas

### Detección automática de problemas en chat:
1. Sistema detecta palabras clave: "estafa", "fraude", "robo", etc.
2. Sistema notifica automáticamente a MODERATOR
3. MODERATOR puede leer la conversación completa
4. MODERATOR interviene si es necesario
5. Si es grave: escala a ADMIN para eliminación

---

## 🎯 IMPLEMENTACIÓN EN CÓDIGO

### Enum de Roles:
```java
public enum Role {
    USER,           // Free - 2 instrumentos
    VENDEDOR,       // Básico - 10 instrumentos
    PRO_SELLER,     // Premium - Ilimitado
    MODERATOR,      // Soporte y moderación
    ADMIN,          // Administrador
    SUPER_ADMIN     // Fundador
}
```

### Enum de Estado de Suscripción:
```java
public enum SubscriptionStatus {
    ACTIVE,         // Pagada y activa
    GRACE_PERIOD,   // En período de gracia (10 días)
    EXPIRED,        // Vencida (vuelve a FREE)
    CANCELLED       // Cancelada manualmente
}
```

---

## 📝 CAMPOS ADICIONALES EN USUARIO

```java
// Campos necesarios en la entidad Usuario:
- role: Role (USER, VENDEDOR, PRO_SELLER, MODERATOR, ADMIN, SUPER_ADMIN)
- subscriptionStatus: SubscriptionStatus
- subscriptionStartDate: LocalDateTime
- subscriptionEndDate: LocalDateTime
- maxPublications: Integer (calculado según rol: 2, 10, infinito)
- isSuspended: Boolean
- suspendedUntil: LocalDateTime (null si no está suspendido)
- suspendedBy: Long (ID del MODERATOR/ADMIN que suspendió)
- suspensionReason: String
- isDeleted: Boolean (soft delete)
- deletedAt: LocalDateTime
- deletedBy: Long (ID del ADMIN que eliminó)
```

---

**Fecha de creación:** 20 de enero de 2026
**Versión:** 1.0
**Estado:** DEFINITIVO - Listo para implementación

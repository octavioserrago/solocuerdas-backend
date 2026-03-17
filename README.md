# SoloCuerdas Backend

Backend de marketplace de instrumentos musicales construido con Spring Boot.

## 📋 Tabla de Contenidos

- [Tecnologías](#tecnologías)
- [Configuración Inicial](#configuración-inicial)
- [Arquitectura](#arquitectura)
- [Sistema de Roles](#sistema-de-roles)
- [Entidades](#entidades)
- [Endpoints API](#endpoints-api)
- [Pruebas con Postman](#pruebas-con-postman)
- [Base de Datos](#base-de-datos)

---

## 🛠️ Tecnologías

- **Java**: 24
- **Spring Boot**: 3.5.9
- **Base de datos**: MySQL (MariaDB 10.4.27 via XAMPP)
- **ORM**: Hibernate 6.6.39 (JPA)
- **Seguridad**: Spring Security + BCrypt
- **Build**: Maven
- **Puerto**: 8080

---

## ⚙️ Configuración Inicial

### Requisitos Previos

1. Java 24 instalado
2. XAMPP con MySQL corriendo
3. Maven (incluido con wrapper `./mvnw`)

### Base de Datos

```properties
# application.properties
spring.datasource.url=jdbc:mysql://localhost:3306/solocuerdas_db
spring.datasource.username=root
spring.datasource.password=
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
```

### Ejecutar la Aplicación

```bash
./mvnw spring-boot:run
```

La aplicación iniciará en `http://localhost:8080`

---

## 🏗️ Arquitectura

El proyecto sigue el patrón **MVC** con separación de capas:

```
src/main/java/com/solocuerdas/solocuerdas_backend/
├── model/              # Entidades JPA
│   ├── Usuario.java
│   ├── Role.java
│   └── SubscriptionStatus.java
├── repository/         # Capa de acceso a datos
│   └── UsuarioRepository.java
├── service/            # Lógica de negocio
│   └── UsuarioService.java
├── controller/         # Endpoints REST
│   └── UsuarioController.java
└── config/            # Configuración
    └── SecurityConfig.java
```

---

## 👥 Sistema de Roles

### Jerarquía de Roles (6 niveles)

| Rol | Tipo | Publicaciones | Precio | Descripción |
|-----|------|---------------|--------|-------------|
| **USER** | Gratuito | 2 | Gratis | Usuario básico con límite de 2 publicaciones |
| **VENDEDOR** | Suscripción | 10 | $X/mes | Vendedor casual con 10 publicaciones |
| **PRO_SELLER** | Suscripción | Ilimitadas | $Y/mes | Vendedor profesional sin límites |
| **MODERATOR** | Staff | - | - | Soporte/moderación (suspender usuarios, ocultar publicaciones) |
| **ADMIN** | Staff | - | - | Administrador (eliminar usuarios, crear cupones) |
| **SUPER_ADMIN** | Founder | - | - | Fundador (crear otros admins, acceso total) |

### Estados de Suscripción

| Estado | Descripción |
|--------|-------------|
| **NONE** | Sin suscripción (usuarios gratuitos) |
| **ACTIVE** | Suscripción activa |
| **GRACE_PERIOD** | Periodo de gracia (10 días tras vencimiento) |
| **EXPIRED** | Suscripción vencida |
| **CANCELLED** | Suscripción cancelada por el usuario |

### Permisos por Rol

#### USER
- ✅ Publicar hasta 2 instrumentos
- ✅ Comprar/vender instrumentos
- ✅ Enviar mensajes

#### VENDEDOR
- ✅ Publicar hasta 10 instrumentos
- ✅ Todas las capacidades de USER

#### PRO_SELLER
- ✅ Publicaciones ilimitadas
- ✅ Todas las capacidades de VENDEDOR

#### MODERATOR
- ✅ Suspender usuarios temporalmente
- ✅ Ocultar publicaciones
- ✅ Modificar información de usuarios
- ❌ NO puede eliminar permanentemente

#### ADMIN
- ✅ Eliminar usuarios (soft delete)
- ✅ Crear cupones de descuento
- ✅ Todas las capacidades de MODERATOR
- ❌ NO puede modificar otros ADMIN/SUPER_ADMIN

#### SUPER_ADMIN
- ✅ Crear otros ADMIN
- ✅ Acceso total al sistema
- ✅ Todas las capacidades de ADMIN

---

## 📊 Entidades

### Usuario

```java
@Entity
@Table(name = "users")
public class Usuario {
    // Campos básicos
    private Long id;
    private String name;        // max 100 caracteres
    private String email;       // único, max 150 caracteres
    private String password;    // BCrypt encrypted
    private String phone;       // max 20 caracteres
    private LocalDateTime registrationDate;
    
    // Sistema de roles
    private Role role;                          // USER por defecto
    private SubscriptionStatus subscriptionStatus;  // NONE por defecto
    private LocalDateTime subscriptionStartDate;
    private LocalDateTime subscriptionEndDate;
    private int maxPublications;               // 2, 10, o Integer.MAX_VALUE
    
    // Sistema de suspensión (moderación)
    private boolean isSuspended;               // false por defecto
    private LocalDateTime suspendedUntil;
    private Long suspendedBy;                  // ID del moderador
    private String suspensionReason;           // max 500 caracteres
    
    // Sistema de eliminación suave
    private boolean isDeleted;                 // false por defecto
    private LocalDateTime deletedAt;
    private Long deletedBy;                    // ID del admin
    private String deletionReason;             // max 500 caracteres
}
```

### Métodos de Negocio

```java
// Verificar si puede publicar más instrumentos
public boolean canPublish(int currentPublications)

// Verificar si el usuario está activo
public boolean isActive()

// Verificar si la suspensión ya expiró
public boolean isSuspensionExpired()
```

---

## 🔌 Endpoints API

### Usuarios

**Base URL**: `/api/users`

#### Crear Usuario
```http
POST /api/users
Content-Type: application/json

{
  "name": "Juan Pérez",
  "email": "juan@test.com",
  "password": "password123",
  "phone": "1234567890"
}
```

**Respuesta** (201 Created):
```json
{
  "id": 1,
  "name": "Juan Pérez",
  "email": "juan@test.com",
  "password": "$2a$10$...",
  "phone": "1234567890",
  "registrationDate": "2026-01-20T18:33:42.320413",
  "role": "USER",
  "subscriptionStatus": "NONE",
  "maxPublications": 2,
  "isSuspended": false,
  "isDeleted": false,
  "active": true
}
```

#### Obtener Todos los Usuarios
```http
GET /api/users
```

**Respuesta** (200 OK):
```json
[
  { usuario1 },
  { usuario2 }
]
```

#### Obtener Usuario por ID
```http
GET /api/users/{id}
```

**Respuesta** (200 OK):
```json
{ usuario }
```

#### Actualizar Usuario
```http
PUT /api/users/{id}
Content-Type: application/json

{
  "name": "Juan Pérez Actualizado",
  "email": "juan@test.com",
  "password": "newPassword123",
  "phone": "0987654321"
}
```

**Respuesta** (200 OK):
```json
{ usuario actualizado }
```

#### Eliminar Usuario
```http
DELETE /api/users/{id}
```

**Respuesta** (204 No Content)

---

## 🧪 Pruebas con Postman

### Configuración de Postman

1. **URL Base**: `http://localhost:8080`
2. **Headers**: 
   - `Content-Type: application/json`

### Casos de Prueba

#### 1. Crear Usuario Básico (USER)
```json
POST /api/users
{
  "name": "Test User",
  "email": "test@example.com",
  "password": "password123",
  "phone": "1234567890"
}
```

Resultado esperado:
- ✅ `role`: "USER"
- ✅ `maxPublications`: 2
- ✅ `subscriptionStatus`: "NONE"
- ✅ Password encriptado con BCrypt

#### 2. Verificar Email Único
```json
POST /api/users
{
  "email": "test@example.com"  // Email ya existente
}
```

Resultado esperado:
- ❌ Error 400: "Email ya registrado"

#### 3. Obtener Usuario por ID
```http
GET /api/users/1
```

Resultado esperado:
- ✅ 200 OK con datos del usuario

---

## 🗄️ Base de Datos

### Tabla `users`

```sql
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    
    -- Información básica
    name VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    phone VARCHAR(20),
    registration_date DATETIME(6) NOT NULL,
    
    -- Sistema de roles
    role ENUM('USER', 'VENDEDOR', 'PRO_SELLER', 'MODERATOR', 'ADMIN', 'SUPER_ADMIN') NOT NULL,
    subscription_status ENUM('NONE', 'ACTIVE', 'GRACE_PERIOD', 'EXPIRED', 'CANCELLED') NOT NULL,
    subscription_start_date DATETIME(6),
    subscription_end_date DATETIME(6),
    max_publications INT NOT NULL,
    
    -- Sistema de suspensión
    is_suspended BIT NOT NULL,
    suspended_until DATETIME(6),
    suspended_by BIGINT,
    suspension_reason VARCHAR(500),
    
    -- Sistema de eliminación suave
    is_deleted BIT NOT NULL,
    deleted_at DATETIME(6),
    deleted_by BIGINT,
    deletion_reason VARCHAR(500)
) ENGINE=InnoDB;
```

### Valores por Defecto

Al crear un usuario, se inicializan automáticamente:

```java
role = Role.USER
subscriptionStatus = SubscriptionStatus.NONE
maxPublications = 2
isSuspended = false
isDeleted = false
registrationDate = LocalDateTime.now()
```

---

## 🔐 Seguridad

### Encriptación de Contraseñas

- **Algoritmo**: BCrypt
- **Configuración**: `SecurityConfig.java`
- **Bean**: `PasswordEncoder` (BCryptPasswordEncoder)

```java
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
}
```

### Estado Actual de Seguridad

⚠️ **MODO DESARROLLO**: 
- CSRF deshabilitado
- Todas las rutas permitidas (`permitAll()`)
- **Antes de producción**: Implementar autenticación JWT o Spring Security completa

---

## 📝 Próximos Pasos

### Funcionalidades Pendientes

- [ ] Implementar autenticación JWT
- [ ] Agregar `@PreAuthorize` según roles
- [ ] Crear entidad `Instrumento/Publicacion`
- [ ] Crear sistema de mensajería (Conversaciones)
- [ ] Endpoints de moderación (suspender usuarios)
- [ ] Endpoints de administración (crear cupones)
- [ ] Sistema de pagos para suscripciones
- [ ] Lógica de periodo de gracia (10 días)
- [ ] Auto-ocultar publicaciones que excedan límite del tier

### Entidades por Crear

1. **Instrumento/Publicacion**
   - Título, descripción, precio
   - Categoría, condición, imágenes
   - Relación con Usuario (vendedor)
   - Estado: ACTIVO, PAUSADO, VENDIDO, OCULTO

2. **Conversacion/Mensaje**
   - Chat entre usuarios
   - Relación con Instrumento
   - Sistema de notificaciones

3. **Cupon**
   - Código de descuento
   - Porcentaje/monto
   - Fecha de expiración
   - Uso único/múltiple

4. **Transaccion**
   - Historial de pagos
   - Suscripciones
   - Compras de instrumentos

---

## 📚 Recursos

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Spring Data JPA](https://spring.io/projects/spring-data-jpa)
- [Spring Security](https://spring.io/projects/spring-security)
- [ROLES_SPECIFICATION.md](ROLES_SPECIFICATION.md) - Especificación completa del sistema de roles

---

## 👨‍💻 Desarrollo

### Estructura de Commits Recomendada

```
feat: agregar sistema de roles y suscripciones
fix: corregir validación de email único
docs: actualizar README con endpoints
refactor: reorganizar capa de servicios
```

### Convenciones de Código

- Nombres de clases: `PascalCase`
- Nombres de métodos: `camelCase`
- Constantes: `UPPER_SNAKE_CASE`
- Paquetes: `lowercase`

---

## 📞 Contacto

Proyecto: SoloCuerdas - Marketplace de Instrumentos Musicales
Versión: 0.0.1-SNAPSHOT

---

**Última actualización**: 20 de enero de 2026

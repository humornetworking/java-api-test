# Users API

API RESTful para el registro de usuarios, construida con Spring Boot 3, Java 21 y arquitectura hexagonal.

---

## Tabla de Contenidos

- [Requisitos](#requisitos)
- [Cómo ejecutar](#cómo-ejecutar)
- [Endpoints](#endpoints)
- [Ejemplos de uso](#ejemplos-de-uso)
- [Validaciones](#validaciones)
- [Arquitectura](#arquitectura)
- [Flujo de una petición](#flujo-de-una-petición)
- [Estructura del proyecto](#estructura-del-proyecto)
- [Tecnologías](#tecnologías)
- [Tests](#tests)
- [Esquema de base de datos](#esquema-de-base-de-datos)

---

## Requisitos

- Java 21+
- Gradle 8+ (o usar el wrapper incluido)

---

## Cómo ejecutar

### 1. Clonar el repositorio

```bash
git clone <repo-url>
cd java-api-test
```

### 2. Generar el Gradle Wrapper (primera vez)

```bash
gradle wrapper
```

### 3. Ejecutar la aplicación

```bash
# Linux / Mac
./gradlew bootRun

# Windows
gradlew.bat bootRun
```

La aplicación arranca en `http://localhost:8081`.

### 4. Construir el JAR

```bash
./gradlew build
java -jar build/libs/users-api-1.0.0-SNAPSHOT.jar
```

---

## Endpoints

| Método | URL | Auth requerida | Descripción |
|--------|-----|----------------|-------------|
| `POST` | `/api/users` | No | Registro de usuario |
| `GET` | `/swagger-ui.html` | No | Documentación interactiva (Swagger UI) |
| `GET` | `/v3/api-docs` | No | Especificación OpenAPI 3.0 (JSON) |
| `GET` | `/h2-console` | No | Consola web H2 (solo desarrollo) |

---

## Ejemplos de uso

### Registro exitoso

```bash
curl -X POST http://localhost:8081/api/users \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Juan Rodriguez",
    "email": "juan@rodriguez.cl",
    "password": "Hunter12",
    "phones": [
      {
        "number": "1234567",
        "citycode": "1",
        "contrycode": "57"
      }
    ]
  }'
```

**Respuesta `201 Created`:**

```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "name": "Juan Rodriguez",
  "email": "juan@rodriguez.cl",
  "phones": [
    {
      "number": "1234567",
      "citycode": "1",
      "contrycode": "57"
    }
  ],
  "created": "2024-01-15T10:30:00",
  "modified": "2024-01-15T10:30:00",
  "last_login": "2024-01-15T10:30:00",
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "isactive": true
}
```

---

### Correo ya registrado

```bash
curl -X POST http://localhost:8081/api/users \
  -H "Content-Type: application/json" \
  -d '{ "name": "Juan", "email": "juan@rodriguez.cl", "password": "Hunter12" }'
```

**Respuesta `409 Conflict`:**

```json
{
  "mensaje": "El correo ya registrado"
}
```

---

### Correo con formato inválido

**Respuesta `400 Bad Request`:**

```json
{
  "mensaje": "El correo no tiene un formato válido"
}
```

---

### Contraseña con formato inválido

**Respuesta `400 Bad Request`:**

```json
{
  "mensaje": "La contraseña no tiene un formato válido. Debe contener al menos una mayúscula, letras minúsculas y dos dígitos"
}
```

---

## Validaciones

### Correo electrónico

- **Regex:** `^[a-zA-Z0-9._%+\-]+@[a-zA-Z0-9.\-]+\.[a-zA-Z]{2,}$`
- Acepta cualquier dominio con TLD de 2 o más caracteres
- Ejemplos válidos: `juan@empresa.com`, `a.b+c@mi-empresa.org`, `user@mail.co.uk`
- Ejemplos inválidos: `juan@empresa`, `@empresa.com`, `juan@.com`, `juanempresa.com`

### Contraseña

- **Regex:** `^(?=(?:.*[0-9]){2})(?=.*[A-Z])(?=.*[a-z]).{6,}$`
- Debe contener **al menos una mayúscula**
- Debe contener **al menos una minúscula**
- Debe contener **al menos dos dígitos**
- Longitud mínima: **6 caracteres**
- Ejemplos válidos: `Hunter12`, `Abc123de`, `Test99x`
- Ejemplos inválidos: `hunter12` (sin mayúscula), `HUNTER12` (sin minúscula), `Hunter1` (un solo dígito)

---

## Arquitectura

El proyecto sigue **arquitectura hexagonal (Ports & Adapters)**:

```
┌────────────────────────────────────────────────────────────┐
│              FILTROS / INTERCEPTORES                       │
│                   JwtAuthFilter                            │
└──────────────────────────┬─────────────────────────────────┘
                           │
┌──────────────────────────▼─────────────────────────────────┐
│           ADAPTADOR PRIMARIO (Driving)                     │
│           UserController  +  GlobalExceptionHandler        │
└──────────────────────────┬─────────────────────────────────┘
                           │  RegisterUserCommand
┌──────────────────────────▼─────────────────────────────────┐
│                  NÚCLEO DE APLICACIÓN                      │
│                                                            │
│  ┌── DOMINIO ──────────────────────────────────────────┐  │
│  │  Models: User, Phone                                │  │
│  │  Ports IN:  RegisterUserUseCase                     │  │
│  │  Ports OUT: UserRepositoryPort                      │  │
│  │             TokenGeneratorPort                      │  │
│  │             PasswordEncoderPort                     │  │
│  └─────────────────────────────────────────────────────┘  │
│                                                            │
│  ┌── SERVICIO DE APLICACIÓN ───────────────────────────┐  │
│  │  RegisterUserApplicationService                     │  │
│  │  (valida, genera token, hashea pw, persiste)        │  │
│  └─────────────────────────────────────────────────────┘  │
└──────────┬─────────────────┬────────────────┬──────────────┘
           │                 │                │
┌──────────▼──────┐ ┌────────▼──────┐ ┌──────▼────────────┐
│ ADAPTADORES SECUNDARIOS (Driven)                         │
│                                                          │
│ UserPersistence  JwtService      BCryptPassword          │
│ Adapter          (JJWT HS256)    EncoderAdapter          │
│     │                                                    │
│     ▼                                                    │
│ H2 In-Memory DB                                          │
│ (users + phones)                                         │
└──────────────────────────────────────────────────────────┘
```

El diagrama completo en PlantUML está en [`docs/architecture.puml`](docs/architecture.puml).
La descripción detallada del flujo está en [`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md).

---

## Flujo de una petición

Ciclo de vida completo de `POST /api/users` a través de todos los componentes:

```
Request
  │
  ├─[1] Spring Security ──────────── 403 si ruta protegida sin token
  │
  ├─[2] JwtAuthenticationFilter ──── valida Bearer token (pasa si ruta pública)
  │
  ├─[3] UserController
  │       └─ @Valid ──────────────── 400 si campos requeridos están vacíos
  │
  ├─[4] UserValidator ────────────── 400 si nombre > 100 chars o más de 10 teléfonos
  │
  ├─[5] UserMapper  (DTO → Command)─ transforma sin lógica
  │
  ├─[6] RegisterUserService
  │       ├─ new Email(value) ─────── 400 si formato de correo inválido
  │       ├─ new Password(value) ──── 400 si formato de contraseña inválido
  │       ├─ existsByEmail ─────────── 409 si el correo ya existe en BD
  │       ├─ JwtService ────────────── genera JWT firmado HS256
  │       ├─ BCryptPasswordEncoder ─── hashea la contraseña
  │       └─ UserDomainService ─────── construye User (UUID + timestamps)
  │
  ├─[9] UserPersistenceAdapter
  │       ├─ UserPersistenceMapper ─── User → UserEntity + PhoneEntity[]
  │       └─ SpringDataUserRepository ─ INSERT en H2 (users + phones)
  │
  └─[8] UserMapper (User → ResponseDto)
          └─ HTTP 201 Created ✓
```

### Detalle por componente

| # | Componente | Capa | Responsabilidad | Fallo posible |
|---|-----------|------|-----------------|---------------|
| 1 | Spring Security | Infrastructure | Autorización de rutas | 403 |
| 2 | `JwtAuthenticationFilter` | Infrastructure | Valida y carga token JWT | 401 |
| 3 | `UserController` + `@Valid` | Infrastructure | Deserializa JSON y valida campos requeridos | 400 |
| 4 | `UserValidator` | Application | Valida reglas de negocio sobre el DTO | 400 |
| 5 | `UserMapper.toCommand()` | Application | Transforma DTO → Command (sin lógica) | — |
| 6 | `RegisterUserService` | Application | Orquesta el caso de uso | 400 / 409 |
| 6a | `Email` value object | Domain | Valida formato de correo en el constructor | 400 |
| 6b | `Password` value object | Domain | Valida formato de contraseña en el constructor | 400 |
| 6c | `UserDomainService` | Domain | Construye `User` con invariantes garantizados | — |
| 7 | `UserPersistenceAdapter` | Infrastructure | Persiste en H2 vía JPA | 500 |
| 8 | `UserMapper.toResponse()` | Application | Transforma `User` → ResponseDto | — |

### Excepciones — `GlobalExceptionHandler`

En cualquier punto del flujo 5→9, si se lanza una excepción Spring la intercepta aquí:

| Excepción | HTTP | Mensaje |
|-----------|------|---------|
| `EmailAlreadyRegisteredException` | 409 | "El correo ya registrado" |
| `InvalidEmailFormatException` | 400 | "El correo no tiene un formato válido" |
| `InvalidPasswordFormatException` | 400 | "La contraseña no tiene un formato válido..." |
| `BusinessValidationException` | 400 | mensaje dinámico |
| `MethodArgumentNotValidException` | 400 | mensaje del campo fallido |
| `HttpMessageNotReadableException` | 400 | "El cuerpo no es un JSON válido" |
| `Exception` (cualquier otra) | 500 | "Error interno del servidor" |

---

## Estructura del proyecto

```
java-api-test/
├── build.gradle
├── settings.gradle
├── docs/
│   ├── architecture.puml        # Diagrama PlantUML
│   └── ARCHITECTURE.md          # Descripción detallada
└── src/
    ├── main/
    │   ├── java/com/example/usersapi/
    │   │   ├── UsersApiApplication.java
    │   │   │
    │   │   ├── domain/                          # Núcleo de negocio
    │   │   │   ├── model/
    │   │   │   │   ├── User.java
    │   │   │   │   └── Phone.java
    │   │   │   ├── exception/
    │   │   │   │   ├── EmailAlreadyRegisteredException.java
    │   │   │   │   ├── InvalidEmailFormatException.java
    │   │   │   │   └── InvalidPasswordFormatException.java
    │   │   │   └── port/
    │   │   │       ├── in/
    │   │   │       │   ├── RegisterUserUseCase.java    # Puerto de entrada
    │   │   │       │   ├── RegisterUserCommand.java
    │   │   │       │   └── PhoneData.java
    │   │   │       └── out/
    │   │   │           ├── UserRepositoryPort.java     # Puerto de salida
    │   │   │           ├── TokenGeneratorPort.java
    │   │   │           └── PasswordEncoderPort.java
    │   │   │
    │   │   ├── application/                     # Casos de uso
    │   │   │   └── service/
    │   │   │       └── RegisterUserApplicationService.java
    │   │   │
    │   │   └── infrastructure/                  # Adaptadores
    │   │       ├── adapter/
    │   │       │   ├── in/web/                  # Adaptador REST (primario)
    │   │       │   │   ├── UserController.java
    │   │       │   │   ├── GlobalExceptionHandler.java
    │   │       │   │   ├── dto/
    │   │       │   │   └── mapper/
    │   │       │   └── out/persistence/         # Adaptador JPA (secundario)
    │   │       │       ├── UserPersistenceAdapter.java
    │   │       │       ├── entity/
    │   │       │       ├── mapper/
    │   │       │       └── repository/
    │   │       ├── config/
    │   │       │   └── SecurityConfig.java
    │   │       └── security/
    │   │           ├── JwtService.java          # Generación/validación JWT
    │   │           ├── JwtAuthenticationFilter.java
    │   │           └── BCryptPasswordEncoderAdapter.java
    │   └── resources/
    │       ├── application.yml
    │       └── openapi/
    │           └── api.yaml                     # Especificación OpenAPI 3.0
    └── test/
        └── java/com/example/usersapi/
            ├── application/service/
            │   └── RegisterUserApplicationServiceTest.java
            └── infrastructure/adapter/in/web/
                └── UserControllerIntegrationTest.java
```

---

## Tecnologías

| Componente | Tecnología |
|------------|------------|
| Lenguaje | Java 21 |
| Framework | Spring Boot 3.3.4 |
| Build | Gradle 8.x |
| Base de datos | H2 (in-memory) |
| Persistencia | Spring Data JPA + Hibernate |
| Servidor | Apache Tomcat (embedded) |
| Seguridad | Spring Security |
| Token | JWT — JJWT 0.12.3 (HS256) |
| API Docs | SpringDoc OpenAPI 3 + Swagger UI |
| Tests | JUnit 5 + Mockito + Spring MockMvc |
| Utilidades | Lombok |

---

## Tests

```bash
# Ejecutar todos los tests
./gradlew test

# Ver reporte HTML
open build/reports/tests/test/index.html
```

### Cobertura de tests

| Clase | Tipo | Casos cubiertos |
|-------|------|-----------------|
| `RegisterUserApplicationServiceTest` | Unitario | Registro exitoso, email duplicado, email inválido (5 casos), password inválida (5 casos), phones nulos, password encodeada |
| `UserControllerIntegrationTest` | Integración (MockMvc) | 201 Created, 409 Conflict, 400 email inválido, 400 password inválida, 400 campo vacío, 400 JSON inválido |

---

## Esquema de base de datos

El esquema es generado automáticamente por Hibernate (`ddl-auto: create-drop`). Equivalente SQL:

```sql
CREATE TABLE users (
    id          UUID          NOT NULL PRIMARY KEY,
    name        VARCHAR(255)  NOT NULL,
    email       VARCHAR(255)  NOT NULL UNIQUE,
    password    VARCHAR(255)  NOT NULL,
    token       VARCHAR(2048) NOT NULL,
    created     TIMESTAMP     NOT NULL,
    modified    TIMESTAMP     NOT NULL,
    last_login  TIMESTAMP     NOT NULL,
    is_active   BOOLEAN       NOT NULL DEFAULT TRUE
);

CREATE TABLE phones (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    number      VARCHAR(255),
    citycode    VARCHAR(255),
    contrycode  VARCHAR(255),
    user_id     UUID NOT NULL,
    CONSTRAINT fk_phones_user FOREIGN KEY (user_id) REFERENCES users(id)
);
```

La consola H2 está disponible en `http://localhost:8081/h2-console`:
- **JDBC URL:** `jdbc:h2:mem:usersdb`
- **Usuario:** `sa`
- **Contraseña:** *(vacía)*

---

## Seguridad

### Filtros aplicados a cada request

1. **JwtAuthenticationFilter** — Valida el token `Bearer` en el header `Authorization` para rutas protegidas.

### Token JWT

- Algoritmo: **HS256**
- Expiración: **24 horas** (configurable en `application.yml`)
- El token se persiste en la base de datos junto al usuario
- Se retorna en la respuesta del registro y debe usarse en el header `Authorization: Bearer <token>` para endpoints protegidos

### Configuración recomendada para producción

```yaml
jwt:
  secret: <base64-encoded-secret-de-minimo-256-bits>
  expiration-ms: 3600000  # 1 hora
```

---

## Principio API First

El contrato de la API está definido **primero** en OpenAPI 3.0 antes de la implementación:

- **Spec:** [`src/main/resources/openapi/api.yaml`](src/main/resources/openapi/api.yaml)
- **Swagger UI:** `http://localhost:8081/swagger-ui.html`
- **JSON spec:** `http://localhost:8081/v3/api-docs`

# Users API — Diagrama de Arquitectura

## Arquitectura Hexagonal (Ports & Adapters)

```
┌─────────────────────────────────────────────────────────────────────┐
│                        HTTP CLIENT                                  │
└─────────────────────────┬───────────────────────────────────────────┘
                          │ POST /api/users
                          ▼
┌─────────────────────────────────────────────────────────────────────┐
│  FILTERS / INTERCEPTORS (Servlet Filter Chain)                      │
│                                                                     │
│  1. JwtAuthenticationFilter (Spring Security)                       │
│     └── Valida Bearer token → SecurityContext                       │
└─────────────────────────┬───────────────────────────────────────────┘
                          │
                          ▼
┌─────────────────────────────────────────────────────────────────────┐
│  PRIMARY ADAPTER (Driving) — Infrastructure IN                      │
│                                                                     │
│  UserController  (@RestController /api/users)                       │
│  ├── RegisterUserRequestDto (name, email, password, phones[])       │
│  ├── UserWebMapper  →  RegisterUserCommand                          │
│  └── GlobalExceptionHandler (@RestControllerAdvice)                 │
│      ├── EmailAlreadyRegisteredException  → 409 Conflict            │
│      ├── InvalidEmailFormatException      → 400 Bad Request         │
│      ├── InvalidPasswordFormatException   → 400 Bad Request         │
│      ├── MethodArgumentNotValidException  → 400 Bad Request         │
│      └── Exception                        → 500 Internal Error      │
└─────────────────────────┬───────────────────────────────────────────┘
                          │ RegisterUserCommand
                          ▼
┌─────────────────────────────────────────────────────────────────────┐
│  APPLICATION CORE                                                   │
│                                                                     │
│  ┌─── DOMAIN ──────────────────────────────────────────────────┐   │
│  │  Models:  User,  Phone                                       │   │
│  │  Exceptions: EmailAlreadyRegistered, InvalidEmail,           │   │
│  │              InvalidPassword                                 │   │
│  │                                                              │   │
│  │  Ports IN:                                                   │   │
│  │  └── RegisterUserUseCase.registerUser(command): User         │   │
│  │                                                              │   │
│  │  Ports OUT:                                                  │   │
│  │  ├── UserRepositoryPort (existsByEmail, save, findByEmail)   │   │
│  │  ├── TokenGeneratorPort (generateToken)                      │   │
│  │  └── PasswordEncoderPort (encode)                           │   │
│  └──────────────────────────────────────────────────────────────┘  │
│                                                                     │
│  ┌─── APPLICATION SERVICE ────────────────────────────────────┐    │
│  │  RegisterUserApplicationService                             │    │
│  │  1. Validate email regex (aaaaaaa@dominio.cl)               │    │
│  │  2. Validate password regex (1 May + min + 2 dig)           │    │
│  │  3. Check email uniqueness via UserRepositoryPort           │    │
│  │  4. Generate JWT token via TokenGeneratorPort               │    │
│  │  5. Encode password via PasswordEncoderPort                 │    │
│  │  6. Build User domain object with UUID + timestamps         │    │
│  │  7. Persist via UserRepositoryPort.save()                   │    │
│  │  8. Return saved User domain object                         │    │
│  └─────────────────────────────────────────────────────────────┘   │
└────────────┬──────────────┬──────────────────┬──────────────────────┘
             │              │                  │
             ▼              ▼                  ▼
┌──────────────────┐ ┌───────────┐ ┌──────────────────────┐
│ SECONDARY ADAPTERS (Driven) — Infrastructure OUT         │
│                                                          │
│ UserPersistenceAdapter    JwtService    BCryptPassword   │
│  implements               implements   EncoderAdapter   │
│  UserRepositoryPort       TokenGen...  implements       │
│       │                   [JJWT        PasswordEncoder   │
│       │                    HS256]      [BCrypt]          │
│       ▼                                                  │
│ SpringDataUserRepository                                 │
│       │                                                  │
│       ▼                                                  │
│ ┌─────────────────────────────────────────────┐         │
│ │         H2 In-Memory Database               │         │
│ │  users table:                               │         │
│ │   id (UUID), name, email (UNIQUE),          │         │
│ │   password (BCrypt), token (JWT),           │         │
│ │   created, modified, last_login,            │         │
│ │   is_active                                 │         │
│ │                                             │         │
│ │  phones table:                              │         │
│ │   id (PK), number, citycode, contrycode,    │         │
│ │   user_id (FK → users.id)                  │         │
│ └─────────────────────────────────────────────┘         │
└──────────────────────────────────────────────────────────┘
```

## API First — OpenAPI 3.0

El contrato de la API está definido en [`src/main/resources/openapi/api.yaml`](../src/main/resources/openapi/api.yaml).
La documentación interactiva (Swagger UI) está disponible en: http://localhost:8080/swagger-ui.html

## Flujo de Registro

```
POST /api/users
Body: { name, email, password, phones[] }

     ┌─────────────────────────────────┐
     │  ¿Email formato válido?         │ ──NO───▶ 400 Bad Request
     │  (regex: xxx@dominio.cl)        │          {"mensaje": "..."}
     └─────────────┬───────────────────┘
                   │ SÍ
     ┌─────────────▼───────────────────┐
     │  ¿Password formato válido?      │ ──NO───▶ 400 Bad Request
     │  (1 May + min + 2 dígitos)      │          {"mensaje": "..."}
     └─────────────┬───────────────────┘
                   │ SÍ
     ┌─────────────▼───────────────────┐
     │  ¿Email ya registrado?          │ ──YES──▶ 409 Conflict
     │  (consulta BD)                  │          {"mensaje": "El correo ya registrado"}
     └─────────────┬───────────────────┘
                   │ NO
     ┌─────────────▼───────────────────┐
     │  Generar JWT token              │
     │  Hashear password (BCrypt)      │
     │  Crear User con UUID + fechas   │
     │  Persistir en H2                │
     └─────────────┬───────────────────┘
                   │
                   ▼
     201 Created: { id, name, email, phones[], created,
                    modified, last_login, token, isactive }
```

## Tecnologías

| Componente         | Tecnología                        |
|--------------------|-----------------------------------|
| Framework          | Spring Boot 3.3.4                 |
| Lenguaje           | Java 21                           |
| Build              | Gradle 8.x                        |
| Base de datos      | H2 (in-memory)                    |
| Persistencia       | Spring Data JPA + Hibernate       |
| Servidor           | Tomcat Embedded                   |
| Seguridad          | Spring Security + JWT (JJWT 0.12) |
| Documentación API  | SpringDoc OpenAPI 3 (Swagger UI)  |
| Tests              | JUnit 5 + Mockito + MockMvc       |
| Utilidades         | Lombok                            |

## Ejecutar la aplicación

```bash
# Generar wrapper de Gradle (si no existe)
gradle wrapper

# Ejecutar
./gradlew bootRun          # Linux/Mac
gradlew.bat bootRun        # Windows

# Ejecutar tests
./gradlew test

# Construir JAR
./gradlew build
java -jar build/libs/users-api-1.0.0-SNAPSHOT.jar
```

## Endpoints

| Método | URL         | Auth | Descripción          |
|--------|-------------|------|----------------------|
| POST   | /api/users  | No   | Registro de usuario  |
| GET    | /swagger-ui.html | No | Documentación API |
| GET    | /h2-console | No   | Consola H2 (dev)     |

## Esquema de BD

```sql
-- Generado automáticamente por Hibernate (ddl-auto: create-drop)
CREATE TABLE users (
    id          UUID         NOT NULL PRIMARY KEY,
    name        VARCHAR(255) NOT NULL,
    email       VARCHAR(255) NOT NULL UNIQUE,
    password    VARCHAR(255) NOT NULL,
    token       VARCHAR(2048) NOT NULL,
    created     TIMESTAMP    NOT NULL,
    modified    TIMESTAMP    NOT NULL,
    last_login  TIMESTAMP    NOT NULL,
    is_active   BOOLEAN      NOT NULL
);

CREATE TABLE phones (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    number      VARCHAR(255),
    citycode    VARCHAR(255),
    contrycode  VARCHAR(255),
    user_id     UUID NOT NULL REFERENCES users(id)
);
```

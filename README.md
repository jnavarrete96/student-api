# student-api

Microservicio reactivo para gestión de estudiantes.

---

## Stack

| Tecnología | Versión | Rol |
|---|---|---|
| Java | 17 | Lenguaje |
| Spring Boot | 3.4.1 | Framework base |
| Spring WebFlux | 3.4.1 | Endpoints reactivos (Reactor Netty) |
| Spring Data R2DBC | 3.4.1 | Persistencia reactiva |
| H2 | runtime | Base de datos en memoria |
| Lombok | latest | Reducción de boilerplate |
| JUnit 5 + Mockito | included | Tests unitarios |
| Gradle Groovy | 9.3.1 | Build tool |

---

## Arquitectura

Se eligió **arquitectura hexagonal (ports & adapters)** por las siguientes razones:

- El reto pide explícitamente un *microservicio* — hexagonal fue diseñada para eso.
- El dominio es completamente independiente de Spring, R2DBC y H2.
- Los puertos permiten testear cada capa de forma aislada sin levantar el contexto completo.
- Cambiar H2 por PostgreSQL solo requiere tocar el adaptador de salida, sin tocar el dominio.

```
src/main/java/com/student/api/
├── domain/
│   ├── model/
│   │   └── Student.java                  ← entidad de dominio pura (enum StudentStatus)
│   ├── port/
│   │   ├── in/  StudentUseCase.java       ← puerto de entrada (interfaz)
│   │   └── out/ StudentRepositoryPort.java ← puerto de salida (interfaz)
│   ├── service/
│   │   └── StudentService.java           ← lógica de negocio, sin @Service
│   └── exception/
│       └── DuplicateStudentException.java
├── adapter/
│   ├── in/web/
│   │   ├── StudentController.java        ← @RestController WebFlux
│   │   ├── StudentRequest.java           ← DTO entrada + validaciones + toDomain()
│   │   ├── StudentResponse.java          ← DTO salida + fromDomain()
│   │   ├── ApiResponse.java              ← wrapper estándar de respuesta
│   │   ├── ErrorResponse.java            ← estructura estándar de error
│   │   └── GlobalExceptionHandler.java   ← @RestControllerAdvice
│   └── out/persistence/
│       ├── StudentEntity.java            ← @Table R2DBC, implementa Persistable<String>
│       ├── StudentR2dbcRepository.java   ← extiende R2dbcRepository
│       └── StudentRepositoryAdapter.java ← implementa StudentRepositoryPort
└── config/
    └── BeanConfig.java                   ← conecta StudentService al contexto Spring
```

---

## Decisiones técnicas importantes

### ¿Por qué `StudentService` no tiene `@Service`?

Porque es dominio puro. Si le ponemos `@Service`, Spring se convierte en dependencia implícita del dominio.
En `BeanConfig` lo registramos como `@Bean` retornando `StudentUseCase` (la interfaz), no `StudentService` (la implementación).

### ¿Por qué `Persistable<String>` en `StudentEntity`?

Spring Data R2DBC decide entre `INSERT` y `UPDATE` según si el `@Id` es `null` o no.
Como el id es un `String` provisto por el cliente (nunca es `null`), R2DBC siempre haría `UPDATE`.
`Persistable<String>` con el flag `isNew` le dice explícitamente cuándo hacer `INSERT`.

### ¿Por qué R2DBC y no JPA?

JPA usa JDBC que es bloqueante. En WebFlux, una llamada bloqueante dentro de un flujo reactivo
ocupa el hilo del event loop completo, destruyendo la escalabilidad. R2DBC es el único stack
de persistencia relacional 100% reactivo.

### ¿Por qué Spring Boot 3.4.1 y no 4.0.x?

Spring Boot 4.0 es una versión major muy reciente con cambios breaking:
- `@JsonComponent` se movió a un módulo separado `spring-boot-jackson`
- `@WebFluxTest` cambió de paquete
- Aún no se recomienda para producción

3.4.1 es la versión LTS estable indicada en el reto.

---

## Endpoints

### `POST /api/students` — Crear estudiante

**Request body:**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "name": "John",
  "lastName": "Doe",
  "status": "ACTIVE",
  "age": 20
}
```

**Respuestas:**

| Código | Descripción |
|---|---|
| `201 Created` | Estudiante grabado exitosamente (body vacío) |
| `400 Bad Request` | Campos inválidos — incluye `fieldErrors` con detalle por campo |
| `409 Conflict` | El id ya existe |

### `GET /api/students/active` — Obtener estudiantes activos

**Respuesta `200 OK`:**
```json
{
  "timestamp": "2026-03-15T17:48:41",
  "status": 200,
  "message": "Active students retrieved successfully",
  "count": 1,
  "data": [
    {
      "id": "550e8400-e29b-41d4-a716-446655440000",
      "name": "John",
      "lastName": "Doe",
      "status": "ACTIVE",
      "age": 20
    }
  ]
}
```

---

## Validaciones

| Campo | Reglas |
|---|---|
| `id` | `@NotBlank` |
| `name` | `@NotBlank`, entre 2 y 100 caracteres |
| `lastName` | `@NotBlank`, entre 2 y 100 caracteres |
| `status` | `@NotNull`, valores: `ACTIVE` o `INACTIVE` |
| `age` | `@NotNull`, entre 0 y 120 |

---

## Tests

**21 tests, 0 failures, 100% successful**

| Clase | Tests | Herramienta | Qué verifica |
|---|---|---|---|
| `StudentServiceTest` | 5 | JUnit 5 + Mockito | Lógica de negocio, DuplicateStudentException, flujo reactivo |
| `StudentControllerTest` | 8 | @WebFluxTest + WebTestClient | HTTP status, JSON body, validaciones, manejo de errores |
| `StudentRepositoryAdapterTest` | 7 | @DataR2dbcTest + H2 | INSERT, findAllActive, existsById con BD real |
| `StudentApiApplicationTests` | 1 | @SpringBootTest | Contexto Spring levanta completo |

Cada capa mockea la capa inferior — nunca sube dependencias reales.

---

## Cómo correr el proyecto

```bash
# Levantar el servicio
./gradlew bootRun

# Correr todos los tests
./gradlew test

# Reporte de tests
build/reports/tests/test/index.html
```

El servicio levanta en `http://localhost:8080`.

---

## Schema de base de datos

```sql
CREATE TABLE IF NOT EXISTS student (
    id        VARCHAR(36)  NOT NULL PRIMARY KEY,
    name      VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    status    VARCHAR(10)  NOT NULL CHECK (status IN ('ACTIVE','INACTIVE')),
    age       INT          NOT NULL CHECK (age >= 0)
);
```

H2 in-memory — los datos se pierden al reiniciar. Reemplazable por PostgreSQL
cambiando solo el driver en `build.gradle` y la URL en `application.yml`.
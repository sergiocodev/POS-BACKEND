# Arquitectura del Proyecto - Spring Boot MVC

## 📁 Estructura de Paquetes

```
com.sergiocodev.app/
├── AppApplication.java          # Clase principal de Spring Boot
├── model/                       # Entidades JPA
├── repository/                  # Interfaces de acceso a datos
├── service/                     # Lógica de negocio
├── controller/                  # Controladores REST/MVC
├── dto/                         # Data Transfer Objects
├── config/                      # Configuraciones
├── exception/                   # Excepciones personalizadas
└── util/                        # Utilidades y helpers
```

## 🏗️ Patrón de Arquitectura: MVC en Capas

### Flujo de Datos

```
Cliente (HTTP Request)
        ↓
[Controller Layer] ← Maneja peticiones HTTP
        ↓
[Service Layer] ← Lógica de negocio
        ↓
[Repository Layer] ← Acceso a datos
        ↓
[Database (MySQL)]
```

## 📦 Descripción de Capas

### 1. **Model Layer** (`model/`)
- **Responsabilidad:** Representar las entidades de la base de datos
- **Tecnología:** JPA/Hibernate
- **Anotaciones clave:** `@Entity`, `@Table`, `@Id`, `@Column`

### 2. **Repository Layer** (`repository/`)
- **Responsabilidad:** Acceso a datos y operaciones CRUD
- **Tecnología:** Spring Data JPA
- **Patrón:** Repository Pattern
- **Hereda de:** `JpaRepository<T, ID>`

### 3. **Service Layer** (`service/`)
- **Responsabilidad:** Lógica de negocio y orquestación
- **Anotación:** `@Service`
- **Características:**
  - Transacciones (`@Transactional`)
  - Validaciones de negocio
  - Coordinación entre repositorios

### 4. **Controller Layer** (`controller/`)
- **Responsabilidad:** Manejar peticiones HTTP
- **Tipos:**
  - `@RestController` - APIs REST (JSON)
  - `@Controller` - Vistas MVC (Thymeleaf)
- **Anotaciones:** `@GetMapping`, `@PostMapping`, `@PutMapping`, `@DeleteMapping`

### 5. **DTO Layer** (`dto/`)
- **Responsabilidad:** Transferencia de datos entre capas
- **Beneficios:**
  - Desacopla la API de las entidades
  - Validación de entrada (`@Valid`)
  - Control de datos expuestos

### 6. **Config Layer** (`config/`)
- **Responsabilidad:** Configuración de la aplicación
- **Ejemplos:**
  - Configuración CORS
  - Beans personalizados
  - Configuración de seguridad

### 7. **Exception Layer** (`exception/`)
- **Responsabilidad:** Manejo centralizado de errores
- **Componentes:**
  - Excepciones personalizadas
  - `@RestControllerAdvice` para manejo global

### 8. **Util Layer** (`util/`)
- **Responsabilidad:** Funciones auxiliares reutilizables
- **Contenido:**
  - Constantes
  - Helpers
  - Utilidades comunes

## 🔄 Ejemplo de Flujo Completo

### Crear un Usuario

1. **Cliente** envía POST a `/api/usuarios`
2. **Controller** recibe `UsuarioDTO` y valida
3. **Service** aplica lógica de negocio (verificar email único)
4. **Repository** guarda en base de datos
5. **Response** retorna el usuario creado

```java
// 1. Controller
@PostMapping
public ResponseEntity<Usuario> crear(@Valid @RequestBody UsuarioDTO dto) {
    return ResponseEntity.ok(usuarioService.crear(dto));
}

// 2. Service
@Transactional
public Usuario crear(UsuarioDTO dto) {
    if (repository.existsByEmail(dto.getEmail())) {
        throw new EmailDuplicadoException();
    }
    Usuario usuario = mapearDtoAEntidad(dto);
    return repository.save(usuario);
}

// 3. Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    boolean existsByEmail(String email);
}
```

## 🎯 Principios Aplicados

- **Separación de Responsabilidades:** Cada capa tiene un propósito específico
- **Inyección de Dependencias:** Spring maneja las dependencias automáticamente
- **Inversión de Control:** Spring Boot controla el ciclo de vida de los beans
- **DRY (Don't Repeat Yourself):** Código reutilizable en servicios y utilidades

## 📚 Recursos

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Spring Data JPA](https://spring.io/projects/spring-data-jpa)
- [Thymeleaf](https://www.thymeleaf.org/)

# Pharmacy POS - Backend (Spring Boot)

Core operativo y API RESTful para el sistema de gestión farmacéutica.

## 🛠️ Tecnologías y Arquitectura
- **Spring Boot 3.4.2**: Base del ecosistema.
- **Spring Security & JWT**: Protección robusta de endpoints y manejo de sesiones sin estado.
- **MySQL & JPA/Hibernate**: Modelo relacional optimizado para catálogos farmacéuticos complejos.
- **Swagger/OpenAPI**: Documentación interactiva disponible en `/swagger-ui.html`.

## ⚙️ Configuración del Entorno

1. **Requisitos**: JDK 21 (según `pom.xml`) y Maven 3.6+.
2. **Clonación y Preparación**:
   - Clonar el repositorio: `git clone <url-del-repositorio>`
   - Crear el archivo `.env` a partir de `.env.example`.
   - Configurar las credenciales de base de datos y el token de API en el archivo `.env`.
3. **Ejecución**:
   ```bash
   ./mvnw spring-boot:run
   ```

## 🚀 ¿Cómo empezar a colaborar?


1. **Configurar el entorno**: Instalar JDK 21 y MySQL.
2. **Importar el proyecto**: Abrir la carpeta del proyecto en su IDE favorito (IntelliJ IDEA o VS Code recomendado).
3. **Lógica de trabajo**:
   - **Ramas (Branches)**: Se recomienda trabajar en ramas descriptivas (ej: `feat/fix-login`) y realizar Pull Requests.
   - **Base de datos**: El proyecto usa `ddl-auto: update`, por lo que las tablas se crearán automáticamente al arrancar.
   - **Documentación API**: Una vez en ejecución, puede ver los endpoints en `http://localhost:8080/swagger-ui.html`.

## 📂 Estructura del Código
- `config/`: Configuración de CORS, Bean definition y auditoría.
- `controller/`: Endpoints REST organizados por recursos.
- `dto/`: Objetos de transferencia desacoplados de las entidades.
- `security/`: Implementación de filtros y utilidades JWT.
- `service/`: Capa de lógica de negocio y transacciones.

## 👤 Autor
**Sergio Sabino Vasquez**
- Licencia: Privada / Derechos Reservados.

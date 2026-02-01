# Pharmacy POS - Backend (Spring Boot)

Core operativo y API RESTful para el sistema de gestión farmacéutica.

## 🛠️ Tecnologías y Arquitectura
- **Spring Boot 3.4.2**: Base del ecosistema.
- **Spring Security & JWT**: Protección robusta de endpoints y manejo de sesiones sin estado.
- **MySQL & JPA/Hibernate**: Modelo relacional optimizado para catálogos farmacéuticos complejos.
- **Swagger/OpenAPI**: Documentación interactiva disponible en `/swagger-ui.html`.

## ⚙️ Configuración del Entorno

1. **Requisitos**: JDK 25 y Maven 3.6+.
2. **Variables de Entorno**: Configure las siguientes variables o use un archivo `.env` (basado en `.env.example`):
   - `DB_URL`: URL de conexión JDBC.
   - `DB_USERNAME` / `DB_PASSWORD`: Credenciales de MySQL.
   - `JWT_SECRET`: Llave de firma para los tokens (mínimo 64 caracteres).

3. **Ejecución**:
   ```bash
   ./mvnw spring-boot:run
   ```

## 📂 Estructura del Código
- `config/`: Configuración de CORS, Bean definition y auditoría.
- `controller/`: Endpoints REST organizados por recursos.
- `dto/`: Objetos de transferencia desacoplados de las entidades.
- `security/`: Implementación de filtros y utilidades JWT.
- `service/`: Capa de lógica de negocio y transacciones.

## 👤 Autor
**Sergio Sabino Vasquez**
- Licencia: Privada / Derechos Reservados.

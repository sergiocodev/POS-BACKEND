# 🏥 Farmacia YuFarm - Sistema de Gestión

Sistema de gestión para farmacia desarrollado con Spring Boot, que incluye autenticación JWT, gestión de productos, categorías, clientes y usuarios.

## 🚀 Tecnologías

- **Backend**: Spring Boot 4.0.2
- **Base de Datos**: MySQL
- **Seguridad**: Spring Security + JWT
- **Documentación API**: Swagger/OpenAPI
- **ORM**: JPA/Hibernate
- **Build Tool**: Maven

## 📋 Requisitos Previos

- Java 25
- MySQL 8.0+
- Maven 3.6+

## ⚙️ Configuración

### 1. Clonar el Repositorio

```bash
git clone <url-del-repositorio>
cd app
```

### 2. Configurar Variables de Entorno

Copia el archivo `.env.example` a `.env` y configura tus credenciales:

```bash
cp .env.example .env
```

Edita el archivo `.env` con tus valores:

```properties
DB_URL=jdbc:mysql://localhost:3306/farmacia_yufarm
DB_USERNAME=tu_usuario
DB_PASSWORD=tu_password
JWT_SECRET=tu_secret_key_de_64_caracteres_hexadecimales
JWT_EXPIRATION=86400000
```

### 3. Configurar Application Properties

Copia el archivo de ejemplo:

```bash
cp src/main/resources/application.properties.example src/main/resources/application.properties
```

> **⚠️ IMPORTANTE**: El archivo `application.properties` está en `.gitignore` para proteger tus credenciales. Nunca lo subas al repositorio.

### 4. Crear la Base de Datos

```sql
CREATE DATABASE farmacia_yufarm;
```

### 5. Compilar y Ejecutar

```bash
# Compilar el proyecto
mvn clean install

# Ejecutar la aplicación
mvn spring-boot:run
```

La aplicación estará disponible en: `http://localhost:8080`

## 📚 Documentación API

Una vez iniciada la aplicación, accede a la documentación Swagger:

```
http://localhost:8080/swagger-ui.html
```

## 🔐 Autenticación

El sistema utiliza JWT para autenticación. Para acceder a los endpoints protegidos:

1. **Registrar usuario** (POST `/api/auth/registro`)
2. **Login** (POST `/api/auth/login`) - Obtendrás un token JWT
3. **Usar el token** en el header `Authorization: Bearer <token>`

## 📁 Estructura del Proyecto

```
src/main/java/com/sergiocodev/app/
├── config/          # Configuración de seguridad y JWT
├── controller/      # Controladores REST
├── dto/            # Data Transfer Objects
├── exception/      # Manejo de excepciones
├── model/          # Entidades JPA
├── repository/     # Repositorios JPA
├── security/       # Filtros y utilidades de seguridad
└── service/        # Lógica de negocio
```

## 🛣️ Endpoints Principales

### Autenticación
- `POST /api/auth/registro` - Registrar nuevo usuario
- `POST /api/auth/login` - Iniciar sesión

### Productos
- `GET /api/productos` - Listar productos
- `POST /api/productos` - Crear producto
- `GET /api/productos/{id}` - Obtener producto
- `PUT /api/productos/{id}` - Actualizar producto
- `DELETE /api/productos/{id}` - Eliminar producto

### Categorías
- `GET /api/categorias` - Listar categorías
- `POST /api/categorias` - Crear categoría
- `PUT /api/categorias/{id}` - Actualizar categoría
- `DELETE /api/categorias/{id}` - Eliminar categoría

### Clientes
- `GET /api/customers` - Listar clientes
- `POST /api/customers` - Crear cliente
- `GET /api/customers/{id}` - Obtener cliente
- `PUT /api/customers/{id}` - Actualizar cliente
- `DELETE /api/customers/{id}` - Eliminar cliente

## 🔧 Desarrollo

### Ejecutar en Modo Desarrollo

```bash
mvn spring-boot:run
```

### Compilar para Producción

```bash
mvn clean package
java -jar target/app-0.0.1-SNAPSHOT.jar
```

## 🧪 Testing

```bash
mvn test
```

## 📝 Licencia

Este proyecto está bajo la Licencia MIT - ver el archivo [LICENSE](LICENSE) para más detalles.

## 👤 Autor

**Sergio Vasquez**

## 🤝 Contribuciones

Las contribuciones son bienvenidas. Por favor:

1. Fork el proyecto
2. Crea una rama para tu feature (`git checkout -b feature/AmazingFeature`)
3. Commit tus cambios (`git commit -m 'Add some AmazingFeature'`)
4. Push a la rama (`git push origin feature/AmazingFeature`)
5. Abre un Pull Request

## ⚠️ Notas de Seguridad

- **NUNCA** subas el archivo `application.properties` con credenciales reales
- **NUNCA** subas el archivo `.env` al repositorio
- Cambia el `JWT_SECRET` en producción por uno generado de forma segura
- Usa contraseñas fuertes para la base de datos
- Mantén las dependencias actualizadas

---

⭐ Si este proyecto te fue útil, considera darle una estrella en GitHub

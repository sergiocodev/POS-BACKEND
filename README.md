# Farmacia YuFarm - Sistema de Gestión

Sistema integral de gestión para farmacia desarrollado con Spring Boot, que proporciona funcionalidades completas para la administración de productos, categorías, clientes y usuarios con autenticación segura mediante JWT.

## Tecnologías Utilizadas

- **Framework Backend**: Spring Boot 4.0.2
- **Base de Datos**: MySQL 8.0+
- **Seguridad**: Spring Security con autenticación JWT
- **Documentación API**: Swagger/OpenAPI 3.0
- **Persistencia**: JPA/Hibernate
- **Herramienta de Construcción**: Apache Maven

## Requisitos del Sistema

- Java Development Kit (JDK) 25 o superior
- MySQL Server 8.0 o superior
- Apache Maven 3.6 o superior

## Instalación y Configuración

### 1. Clonar el Repositorio

```bash
git clone <url-del-repositorio>
cd app
```

### 2. Configuración de Variables de Entorno

El proyecto utiliza variables de entorno para gestionar información sensible. Copie el archivo de plantilla y configure sus credenciales:

```bash
cp .env.example .env
```

Configure las siguientes variables en el archivo `.env`:

```properties
DB_URL=jdbc:mysql://localhost:3306/farmacia_yufarm
DB_USERNAME=su_usuario_mysql
DB_PASSWORD=su_contraseña_mysql
JWT_SECRET=clave_secreta_jwt_64_caracteres_hexadecimales
JWT_EXPIRATION=86400000
```

### 3. Configuración de Propiedades de la Aplicación

Copie el archivo de configuración de ejemplo:

```bash
cp src/main/resources/application.properties.example src/main/resources/application.properties
```

### 4. Preparación de la Base de Datos

Cree la base de datos en MySQL:

```sql
CREATE DATABASE farmacia_yufarm CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 5. Compilación y Ejecución

Compile el proyecto:

```bash
mvn clean install
```

Ejecute la aplicación:

```bash
mvn spring-boot:run
```

La aplicación estará disponible en `http://localhost:8080`

## Documentación de la API

La documentación interactiva de la API está disponible a través de Swagger UI una vez que la aplicación esté en ejecución:

```
http://localhost:8080/swagger-ui.html
```

## Autenticación y Autorización

El sistema implementa autenticación basada en tokens JWT (JSON Web Tokens). Para acceder a los endpoints protegidos:

1. Registre un nuevo usuario mediante `POST /api/auth/registro`
2. Inicie sesión con `POST /api/auth/login` para obtener el token JWT
3. Incluya el token en el encabezado de autorización: `Authorization: Bearer <token>`

## Estructura del Proyecto

```
src/main/java/com/sergiocodev/app/
├── config/          # Configuraciones de seguridad y beans
├── controller/      # Controladores REST
├── dto/            # Objetos de transferencia de datos
├── exception/      # Excepciones personalizadas y manejadores
├── model/          # Entidades del modelo de datos
├── repository/     # Interfaces de repositorio JPA
├── security/       # Componentes de seguridad y filtros JWT
└── service/        # Capa de lógica de negocio
```

## Entorno de Desarrollo

Para ejecutar la aplicación en modo de desarrollo:

```bash
mvn spring-boot:run
```

## Compilación para Producción

Para generar el archivo JAR ejecutable:

```bash
mvn clean package
```

Ejecutar el JAR generado:

```bash
java -jar target/app-0.0.1-SNAPSHOT.jar
```

## Pruebas

Ejecutar la suite de pruebas:

```bash
mvn test
```

## Licencia

Este proyecto está protegido por derechos de autor. Consulte el archivo `LICENSE` para más información sobre los términos de uso.

## Autor

Sergio Sabino Vasquez

## Contribuciones

Para contribuir al proyecto:

1. Realice un fork del repositorio
2. Cree una rama para su funcionalidad (`git checkout -b feature/nueva-funcionalidad`)
3. Realice commit de sus cambios (`git commit -m 'Descripción de los cambios'`)
4. Envíe los cambios a su fork (`git push origin feature/nueva-funcionalidad`)
5. Abra un Pull Request para revisión

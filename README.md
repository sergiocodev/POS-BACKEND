# POS YuFarm - Sistema de Gestión

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

# POS Farmacia - Sistema Punto de Venta

Sistema integral de gestión para farmacias desarrollado con Spring Boot, que incorpora autenticación segura mediante JWT y control de acceso basado en roles y permisos (RBAC). La solución incluye módulos para la gestión de catálogos farmacéuticos (ingredientes activos, marcas, categorías, laboratorios y presentaciones), un maestro de productos con sus relaciones (código, registro DIGEMID, marca, categoría, laboratorio, presentación, tipo de unidad, factor de compra y etiquetas de fraccionamiento), y la asociación de ingredientes mediante. Dispone de un inventario detallado por lotes y establecimientos, control de caja y sesiones, así como módulos de compras y ventas que soportan la creación automática de lotes, el registro de ítems, métodos de pago y la integración con la facturación electrónica.

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

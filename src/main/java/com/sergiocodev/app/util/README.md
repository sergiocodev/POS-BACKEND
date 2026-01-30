# Util Package

Este paquete contiene **clases utilitarias** y helpers.

## Propósito
- Proporcionar métodos de utilidad reutilizables
- Funciones auxiliares comunes
- Constantes de la aplicación

## Ejemplo
```java
public class DateUtils {
    
    public static LocalDateTime convertirAZonaHoraria(
            LocalDateTime fecha, String zonaHoraria) {
        ZoneId zona = ZoneId.of(zonaHoraria);
        return fecha.atZone(ZoneId.systemDefault())
            .withZoneSameInstant(zona)
            .toLocalDateTime();
    }
}
```

```java
public class AppConstants {
    public static final String API_VERSION = "/api/v1";
    public static final int PAGE_SIZE_DEFAULT = 10;
    public static final String DATE_FORMAT = "yyyy-MM-dd";
}
```

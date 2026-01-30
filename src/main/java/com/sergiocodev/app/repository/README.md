# Repository Package

Este paquete contiene las **interfaces de repositorio** para acceso a datos.

## Propósito
- Definir interfaces que extienden JpaRepository
- Proporcionar métodos CRUD automáticos
- Definir consultas personalizadas con @Query

## Ejemplo
```java
@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByEmail(String email);
    
    @Query("SELECT u FROM Usuario u WHERE u.nombre LIKE %:nombre%")
    List<Usuario> buscarPorNombre(@Param("nombre") String nombre);
}
```

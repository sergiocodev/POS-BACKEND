# Service Package

Este paquete contiene la **lógica de negocio** de la aplicación.

## Propósito
- Implementar la lógica de negocio
- Coordinar operaciones entre repositorios
- Aplicar validaciones y reglas de negocio
- Manejar transacciones

## Ejemplo
```java
@Service
@RequiredArgsConstructor
public class UsuarioService {
    private final UsuarioRepository usuarioRepository;
    
    @Transactional
    public Usuario crearUsuario(UsuarioDTO dto) {
        // Validaciones
        if (usuarioRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new EmailDuplicadoException();
        }
        
        // Lógica de negocio
        Usuario usuario = new Usuario();
        usuario.setNombre(dto.getNombre());
        usuario.setEmail(dto.getEmail());
        
        return usuarioRepository.save(usuario);
    }
}
```

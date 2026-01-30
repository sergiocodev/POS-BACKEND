# Model Package

Este paquete contiene las **entidades JPA** (clases de modelo de datos).

## Propósito
- Definir las entidades de la base de datos
- Mapear tablas usando anotaciones JPA (@Entity, @Table, @Id, etc.)
- Definir relaciones entre entidades (@OneToMany, @ManyToOne, etc.)

## Ejemplo
```java
@Entity
@Table(name = "usuarios")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String nombre;
    private String email;
}
```

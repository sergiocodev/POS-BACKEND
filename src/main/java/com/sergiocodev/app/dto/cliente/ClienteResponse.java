package com.sergiocodev.app.dto.cliente;

import com.sergiocodev.app.model.Cliente;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClienteResponse {

    private Long id;
    private String dni;
    private String nombre;
    private String telefono;
    private String direccion;

    public ClienteResponse(Cliente cliente) {
        this.id = cliente.getId();
        this.dni = cliente.getDni();
        this.nombre = cliente.getNombre();
        this.telefono = cliente.getTelefono();
        this.direccion = cliente.getDireccion();
    }
}

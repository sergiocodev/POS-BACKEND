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
    private String ruc;
    private String firstName;
    private String lastName;
    private String phone;
    private String email;
    private String address;
    private java.time.LocalDateTime createdAt;

    public ClienteResponse(Cliente cliente) {
        this.id = cliente.getId();
        this.dni = cliente.getDni();
        this.ruc = cliente.getRuc();
        this.firstName = cliente.getFirstName();
        this.lastName = cliente.getLastName();
        this.phone = cliente.getPhone();
        this.email = cliente.getEmail();
        this.address = cliente.getAddress();
        this.createdAt = cliente.getCreatedAt();
    }
}

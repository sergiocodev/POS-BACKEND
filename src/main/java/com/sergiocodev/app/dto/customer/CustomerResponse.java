package com.sergiocodev.app.dto.customer;

import com.sergiocodev.app.model.Customer;
import com.sergiocodev.app.model.DocumentType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerResponse {

    private Long id;
    private DocumentType documentType;
    private String documentNumber;
    private String name;
    private String phone;
    private String email;
    private String address;
    private LocalDateTime createdAt;

    public CustomerResponse(Customer customer) {
        this.id = customer.getId();
        this.documentType = customer.getDocumentType();
        this.documentNumber = customer.getDocumentNumber();
        this.name = customer.getName();
        this.phone = customer.getPhone();
        this.email = customer.getEmail();
        this.address = customer.getAddress();
        this.createdAt = customer.getCreatedAt();
    }
}

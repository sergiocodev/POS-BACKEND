package com.sergiocodev.app.dto.supplier;

import com.sergiocodev.app.model.Supplier;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SupplierResponse {

    private Long id;
    private String name;
    private String ruc;
    private String phone;
    private String email;
    private String address;
    private boolean active;

    public SupplierResponse(Supplier supplier) {
        this.id = supplier.getId();
        this.name = supplier.getName();
        this.ruc = supplier.getRuc();
        this.phone = supplier.getPhone();
        this.email = supplier.getEmail();
        this.address = supplier.getAddress();
        this.active = supplier.isActive();
    }
}

package com.sergiocodev.app.dto.marca;

import com.sergiocodev.app.model.Marca;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MarcaResponse {

    private Long id;
    private String name;
    private boolean active;
    private java.time.LocalDateTime createdAt;
    private java.time.LocalDateTime updatedAt;

    public MarcaResponse(Marca marca) {
        this.id = marca.getId();
        this.name = marca.getName();
        this.active = marca.isActive();
        this.createdAt = marca.getCreatedAt();
        this.updatedAt = marca.getUpdatedAt();
    }
}

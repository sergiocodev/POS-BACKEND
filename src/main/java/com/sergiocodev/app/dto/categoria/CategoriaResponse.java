package com.sergiocodev.app.dto.categoria;

import com.sergiocodev.app.model.Categoria;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoriaResponse {

    private Long id;
    private String name;
    private boolean active;
    private java.time.LocalDateTime createdAt;
    private java.time.LocalDateTime updatedAt;

    public CategoriaResponse(Categoria categoria) {
        this.id = categoria.getId();
        this.name = categoria.getName();
        this.active = categoria.isActive();
        this.createdAt = categoria.getCreatedAt();
        this.updatedAt = categoria.getUpdatedAt();
    }
}

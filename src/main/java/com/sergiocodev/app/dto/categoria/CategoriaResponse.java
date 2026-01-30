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
    private String nombreCategoria;

    public CategoriaResponse(Categoria categoria) {
        this.id = categoria.getId();
        this.nombreCategoria = categoria.getNombreCategoria();
    }
}

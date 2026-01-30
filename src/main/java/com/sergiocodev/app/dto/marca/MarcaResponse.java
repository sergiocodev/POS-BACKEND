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
    private String nombreMarca;

    public MarcaResponse(Marca marca) {
        this.id = marca.getId();
        this.nombreMarca = marca.getNombreMarca();
    }
}

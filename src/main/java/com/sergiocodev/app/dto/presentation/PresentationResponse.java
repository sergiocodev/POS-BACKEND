package com.sergiocodev.app.dto.presentation;

import com.sergiocodev.app.model.Presentation;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PresentationResponse {

    private Long id;
    private String description;

    public PresentationResponse(Presentation presentation) {
        this.id = presentation.getId();
        this.description = presentation.getDescription();
    }
}

package com.sergiocodev.app.dto.category;

import com.sergiocodev.app.model.Category;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryResponse {

    private Long id;
    private String name;
    private boolean active;

    public CategoryResponse(Category category) {
        this.id = category.getId();
        this.name = category.getName();
        this.active = category.isActive();
    }
}

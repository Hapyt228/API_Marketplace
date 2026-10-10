package com.example.marketplace.dto.category;


import com.example.marketplace.entity.category.Category;


public class CategoryResponse {
    private String name;
    private String description;

    public CategoryResponse() {
    }

    public CategoryResponse(Category category) {
        name = category.getName();
        description = category.getDescription();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}

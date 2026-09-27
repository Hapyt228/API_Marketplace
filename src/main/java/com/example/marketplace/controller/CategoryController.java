package com.example.marketplace.controller;

import com.example.marketplace.entity.Category;
import com.example.marketplace.entity.Product;
import com.example.marketplace.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class CategoryController {

    private CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping("/api/category")
    public ResponseEntity<List<Category>> getAllProduct(){
        return ResponseEntity.ok(categoryService.getAllCategory());
    }

    @GetMapping("/api/category/{id}")
    public ResponseEntity<Category> getProductById(
            @PathVariable("id") Long id
    ){
        return ResponseEntity.ok(categoryService.getCategoryById(id));
    }

    @PostMapping("/api/category")
    public ResponseEntity<Category> createProduct(
            @RequestBody @Valid Category category
    ){
        return ResponseEntity.status(HttpStatus.CREATED).body(
                categoryService.addCategory(category)
        );
    }


    @PutMapping("/api/category/{id}")
    public ResponseEntity<Category> updateProduct(
            @PathVariable("id") Long id
    ){
        return ResponseEntity.status(HttpStatus.OK).body(
                categoryService.updateCategory(id)
        );
    }

    @DeleteMapping("/api/category/{id}")
    public ResponseEntity<Category> deleteProduct(
            @PathVariable("id") Long id
    ){
        categoryService.deleteCategory(id);
        return ResponseEntity.status(HttpStatus.OK)
                .build();
    }
}

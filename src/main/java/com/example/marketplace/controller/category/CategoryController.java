package com.example.marketplace.controller.category;

import com.example.marketplace.dto.category.CategoryRequest;
import com.example.marketplace.dto.category.CategoryResponse;
import com.example.marketplace.service.category.CategoryService;
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

    @GetMapping("/api/categories")
    public ResponseEntity<List<CategoryResponse>> getAllProduct(){
        return ResponseEntity.ok(categoryService.getAllCategory());
    }

    @GetMapping("/api/categories/{id}")
    public ResponseEntity<CategoryResponse> getProductById(
            @PathVariable("id") Long id
    ){
        return ResponseEntity.ok(categoryService.getCategoryById(id));
    }

    @PostMapping("/api/categories")
    public ResponseEntity<CategoryResponse> createProduct(
            @RequestBody @Valid CategoryRequest categoryRequest
    ){
        return ResponseEntity.status(HttpStatus.CREATED).body(
                categoryService.addCategory(categoryRequest)
        );
    }


    @PutMapping("/api/categories/{id}")
    public ResponseEntity<CategoryResponse> updateProduct(
            @PathVariable("id") Long id,
            @RequestBody @Valid CategoryRequest categoryRequest
    ){
        return ResponseEntity.status(HttpStatus.OK).body(
                categoryService.updateCategory(id, categoryRequest)
        );
    }

    @DeleteMapping("/api/categories/{id}")
    public ResponseEntity<Void> deleteProduct(
            @PathVariable("id") Long id
    ){
        categoryService.deleteCategory(id);
        return ResponseEntity.status(HttpStatus.OK)
                .build();
    }
}

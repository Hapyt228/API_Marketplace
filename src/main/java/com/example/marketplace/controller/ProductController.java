package com.example.marketplace.controller;

import com.example.marketplace.entity.Product;
import com.example.marketplace.repository.CategoryRepository;
import com.example.marketplace.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class ProductController {

    private ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/api/products")
    public ResponseEntity<List<Product>> getAllProduct(){
        return ResponseEntity.ok(productService.getAllProduct());
    }

    @GetMapping("/api/products/{id}")
    public ResponseEntity<Product> getProductById(
            @PathVariable("id") Long id
    ){
        return ResponseEntity.ok(productService.getProductById(id));
    }

    @PostMapping("/api/products")
    public ResponseEntity<Product> createProduct(
            @RequestBody @Valid Product product
    ){
        return ResponseEntity.status(HttpStatus.CREATED).body(
                productService.addProduct(product)
        );
    }


    @PutMapping("/api/products/{id}")
    public ResponseEntity<Product> updateProduct(
            @PathVariable("id") Long id
    ){
        return ResponseEntity.status(HttpStatus.OK).body(
                productService.updateProduct(id)
        );
    }

    @DeleteMapping("/api/products/{id}")
    public ResponseEntity<Void> deleteProduct(
            @PathVariable("id") Long id
    ){
        productService.deleteProduct(id);
        return ResponseEntity.status(HttpStatus.OK)
                .build();
    }
}

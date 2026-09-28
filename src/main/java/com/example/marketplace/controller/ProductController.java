package com.example.marketplace.controller;

import com.example.marketplace.dto.ProductRequest;
import com.example.marketplace.dto.ProductResponse;
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
    public ResponseEntity<List<ProductResponse>> getAllProduct(){
        return ResponseEntity.ok(productService.getAllProduct());
    }

    @GetMapping("/api/products/{id}")
    public ResponseEntity<ProductResponse> getProductById(
            @PathVariable("id") Long id
    ){
        return ResponseEntity.ok(productService.getProductById(id));
    }

    @PostMapping("/api/products")
    public ResponseEntity<ProductResponse> createProduct(
            @RequestBody @Valid ProductRequest productRequest
    ){

        return ResponseEntity.status(HttpStatus.CREATED).body(
                productService.addProduct(productRequest)
        );
    }


    @PutMapping("/api/products/{id}")
    public ResponseEntity<ProductResponse> updateProduct(
            @PathVariable("id") Long id,
            @RequestBody @Valid ProductRequest productRequest
    ){
        return ResponseEntity.status(HttpStatus.OK).body(
                productService.updateProduct(id, productRequest)
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

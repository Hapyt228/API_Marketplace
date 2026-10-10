package com.example.marketplace.controller.product;

import com.example.marketplace.dto.product.ProductRequest;
import com.example.marketplace.dto.product.ProductResponse;
import com.example.marketplace.entity.user.User;
import com.example.marketplace.service.product.ProductService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
            @AuthenticationPrincipal User user,
            @RequestBody @Valid ProductRequest productRequest
    ){
        return ResponseEntity.status(HttpStatus.CREATED).body(
                productService.addProduct(productRequest, user.getId())
        );
    }


    @PutMapping("/api/products/{id}")
    public ResponseEntity<ProductResponse> updateProduct(
            @PathVariable("id") Long id,
            @RequestBody @Valid ProductRequest productRequest,
            @AuthenticationPrincipal User user
    ){
        return ResponseEntity.status(HttpStatus.OK).body(
                productService.updateProduct(id, productRequest,  user.getId())
        );
    }

    @DeleteMapping("/api/products/{id}")
    public ResponseEntity<Void> deleteProduct(
            @PathVariable("id") Long id,
            @AuthenticationPrincipal User user
    ){
        productService.deleteProduct(id, user.getId());
        return ResponseEntity.status(HttpStatus.OK)
                .build();
    }
}

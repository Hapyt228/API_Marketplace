package com.example.marketplace.controller.product;

import com.example.marketplace.dto.product.ProductPageResponse;
import com.example.marketplace.dto.product.ProductRequest;
import com.example.marketplace.dto.product.ProductResponse;
import com.example.marketplace.entity.user.User;
import com.example.marketplace.exception.InvalidPageRequestException;
import com.example.marketplace.service.product.ProductService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public ResponseEntity<ProductPageResponse> getAllProduct(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int pageSize,
            @RequestParam(name = "sort", required = false) String sort
    ){
        if(page < 0){
            throw new InvalidPageRequestException("Page must be 0 or greater");
        }
        if (pageSize <= 0){
            throw new InvalidPageRequestException("Size must be 1 or greater");
        }
        if (pageSize > 50){
            pageSize = 50;
        }
        return ResponseEntity.ok(productService.getAllProduct(page, pageSize, sort));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(
            @PathVariable("id") Long id
    ){
        return ResponseEntity.ok(productService.getProductById(id));
    }

    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(
            @AuthenticationPrincipal User user,
            @RequestBody @Valid ProductRequest productRequest
    ){
        return ResponseEntity.status(HttpStatus.CREATED).body(
                productService.addProduct(productRequest, user.getId())
        );
    }


    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> updateProduct(
            @PathVariable("id") Long id,
            @RequestBody @Valid ProductRequest productRequest,
            @AuthenticationPrincipal User user
    ){
        return ResponseEntity.status(HttpStatus.OK).body(
                productService.updateProduct(id, productRequest,  user.getId())
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(
            @PathVariable("id") Long id,
            @AuthenticationPrincipal User user
    ){
        productService.deleteProduct(id, user.getId());
        return ResponseEntity.status(HttpStatus.OK)
                .build();
    }
}

package com.example.marketplace.service;

import com.example.marketplace.dto.ProductRequest;
import com.example.marketplace.dto.ProductResponse;
import com.example.marketplace.entity.Category;
import com.example.marketplace.entity.Product;
import com.example.marketplace.entity.User;
import com.example.marketplace.repository.CategoryRepository;
import com.example.marketplace.repository.ProductRepository;
import com.example.marketplace.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    public ProductService(ProductRepository productRepository, CategoryRepository categoryRepository, UserRepository userRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
    }

    public ProductResponse getProductById(Long id){
     Product product = productRepository.findById(id).orElseThrow(() -> new NoSuchElementException("Not found by id = " + id));
     return new  ProductResponse(product);
    }

    public List<ProductResponse> getAllProduct(){
        return productRepository.findAll()
                .stream()
                .map(ProductResponse::new)
                .toList();
    }

    public ProductResponse addProduct(ProductRequest productRequest){
        Category category = categoryRepository.findById(productRequest.getCategoryId())
                .orElseThrow(() -> new EntityNotFoundException("Category not found: " + productRequest.getCategoryId()));
        User user = userRepository.findById(productRequest.getSellerId())
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + productRequest.getCategoryId()));

        Product product = new Product();
        product.setName(productRequest.getName());
        product.setDescription(productRequest.getDescription());
        product.setPrice(productRequest.getPrice());
        product.setCategory(category);
        product.setSeller(user);

        return new  ProductResponse(productRepository.save(product));
    }

    public ProductResponse updateProduct(Long id, ProductRequest productRequest){
        Product productByUpdate = productRepository.findById(id).orElseThrow(() -> new NoSuchElementException("Not found by id = " + id));
        productByUpdate.setName(productRequest.getName());
        productByUpdate.setDescription(productRequest.getDescription());
        productByUpdate.setPrice(productRequest.getPrice());
        if (!Objects.equals(productByUpdate.getCategory().getId(), productRequest.getCategoryId())){
            Category category = categoryRepository.findById(productRequest.getCategoryId())
                    .orElseThrow(() -> new EntityNotFoundException("Category not found: " + productRequest.getCategoryId()));
            productByUpdate.setCategory(category);
        }

        if (!Objects.equals(productByUpdate.getSeller().getId(), productRequest.getSellerId())){
            User user = userRepository.findById(productRequest.getSellerId())
                    .orElseThrow(() -> new EntityNotFoundException("User not found: " + productRequest.getCategoryId()));
            productByUpdate.setSeller(user);
        }

        return new  ProductResponse(productRepository.save(productByUpdate));
    }

    public void deleteProduct(Long id){
        Product product = productRepository.findById(id).orElseThrow(() -> new NoSuchElementException("Not found by id = " + id));
        productRepository.delete(product);
    }

}

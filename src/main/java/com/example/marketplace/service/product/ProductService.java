package com.example.marketplace.service.product;

import com.example.marketplace.dto.product.ProductRequest;
import com.example.marketplace.dto.product.ProductResponse;
import com.example.marketplace.entity.category.Category;
import com.example.marketplace.entity.product.Product;
import com.example.marketplace.entity.user.User;
import com.example.marketplace.repository.category.CategoryRepository;
import com.example.marketplace.repository.product.ProductRepository;
import com.example.marketplace.repository.user.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.access.AccessDeniedException;
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

    public ProductResponse addProduct(ProductRequest productRequest, Long userId){
        Category category = categoryRepository.findById(productRequest.getCategoryId())
                .orElseThrow(() -> new EntityNotFoundException("Category not found: " + productRequest.getCategoryId()));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + userId));

        Product product = new Product();
        product.setName(productRequest.getName());
        product.setDescription(productRequest.getDescription());
        product.setPrice(productRequest.getPrice());
        product.setCategory(category);
        product.setSeller(user);

        return new  ProductResponse(productRepository.save(product));
    }

    public ProductResponse updateProduct(Long id, ProductRequest productRequest, Long currentUserId){
        Product productByUpdate = productRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Not found by id = " + id));

        if (!Objects.equals(productByUpdate.getSeller().getId(), currentUserId)) {
            throw new AccessDeniedException("You are not the owner of this product");
        }

        productByUpdate.setName(productRequest.getName());
        productByUpdate.setDescription(productRequest.getDescription());
        productByUpdate.setPrice(productRequest.getPrice());
        if (!Objects.equals(productByUpdate.getCategory().getId(), productRequest.getCategoryId())){
            Category category = categoryRepository.findById(productRequest.getCategoryId())
                    .orElseThrow(() -> new EntityNotFoundException("Category not found: " + productRequest.getCategoryId()));
            productByUpdate.setCategory(category);
        }

        return new ProductResponse(productRepository.save(productByUpdate));
    }

    public void deleteProduct(Long id, Long currentUserId){
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Not found by id = " + id));
        if (!Objects.equals(product.getSeller().getId(), currentUserId)) {
            throw new AccessDeniedException("You are not the owner of this product");
        }
        productRepository.delete(product);
    }

}

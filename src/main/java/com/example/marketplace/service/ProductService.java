package com.example.marketplace.service;

import com.example.marketplace.dto.ProductRequest;
import com.example.marketplace.dto.ProductResponse;
import com.example.marketplace.entity.Category;
import com.example.marketplace.entity.Product;
import com.example.marketplace.repository.CategoryRepository;
import com.example.marketplace.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class ProductService {

    private ProductRepository productRepository;
    private CategoryRepository categoryRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
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

        Product product = new Product();
        product.setName(productRequest.getName());
        product.setDescription(productRequest.getDescription());
        product.setPrice(productRequest.getPrice());
        product.setCategory(category);


        return new  ProductResponse(productRepository.save(product));
    }

    public ProductResponse updateProduct(Long id, ProductRequest productRequest){
        Product productByUpdate = productRepository.findById(id).orElseThrow(() -> new NoSuchElementException("Not found by id = " + id));
        productByUpdate.setName(productRequest.getName());
        productByUpdate.setDescription(productRequest.getDescription());
        productByUpdate.setPrice(productRequest.getPrice());
        if (productByUpdate.getCategory().getId() != productRequest.getCategoryId()){
            Category category = categoryRepository.findById(productRequest.getCategoryId())
                    .orElseThrow(() -> new EntityNotFoundException("Category not found: " + productRequest.getCategoryId()));
            productByUpdate.setCategory(category);
        }
        // Потом будет как сделаем регистрацию
        //        if (productByUpdate.getSeller().getId() != productRequest.getSeller().getId()){
//            User user = productRequest.getSeller();
//            productByUpdate.setSeller();
//        }

        return new  ProductResponse(productRepository.save(productByUpdate));
    }

    public void deleteProduct(Long id){
        Product product = productRepository.findById(id).orElseThrow(() -> new NoSuchElementException("Not found by id = " + id));
        productRepository.delete(product);
    }

}

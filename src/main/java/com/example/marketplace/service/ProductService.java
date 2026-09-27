package com.example.marketplace.service;

import com.example.marketplace.entity.Product;
import com.example.marketplace.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {

    private ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public Product getProductById(Long id){
        return productRepository.getReferenceById(id);
    }

    public List<Product> getAllProduct(){
        return productRepository.findAll()
                .stream()
                .toList();
    }

    public Product updateProduct(Long id){
        Product product = productRepository.getReferenceById(id);
        return productRepository.save(product);
    }

    public Product addProduct(Product product){
        return productRepository.save(product);
    }

    public void deleteProduct(Long id){
        Product product = productRepository.getReferenceById(id);
        productRepository.delete(product);
    }

}

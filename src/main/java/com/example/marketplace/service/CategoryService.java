package com.example.marketplace.service;

import com.example.marketplace.entity.Category;
import com.example.marketplace.entity.Product;
import com.example.marketplace.repository.CategoryRepository;
import com.example.marketplace.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService {

    private CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public Category getCategoryById(Long id){
        return categoryRepository.getReferenceById(id);
    }

    public List<Category> getAllCategory(){
        return categoryRepository.findAll()
                .stream()
                .toList();
    }

    public Category addCategory(Category category){
        return categoryRepository.save(category);
    }

    public Category updateCategory(Long id){
        Category category = categoryRepository.getReferenceById(id);
        return categoryRepository.save(category);
    }

    public void deleteCategory(Long id){
        Category category = categoryRepository.getReferenceById(id);
        categoryRepository.delete(category);
    }

}

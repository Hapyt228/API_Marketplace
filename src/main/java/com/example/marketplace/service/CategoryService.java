package com.example.marketplace.service;

import com.example.marketplace.dto.CategoryRequest;
import com.example.marketplace.dto.CategoryResponse;
import com.example.marketplace.entity.Category;
import com.example.marketplace.entity.Product;
import com.example.marketplace.repository.CategoryRepository;
import com.example.marketplace.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class CategoryService {

    private CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public CategoryResponse getCategoryById(Long id){
        Category category = categoryRepository.findById(id).orElseThrow(() -> new NoSuchElementException("Not found by id = " + id));
        return new  CategoryResponse(category);
    }

    public List<CategoryResponse> getAllCategory(){
        return categoryRepository.findAll()
                .stream()
                .map(CategoryResponse::new)
                .toList();
    }

    public CategoryResponse addCategory(CategoryRequest categoryRequest){
        Category category = new Category();
        category.setName(categoryRequest.getName());
        category.setDescription(categoryRequest.getDescription());

        return new CategoryResponse(categoryRepository.save(category));
    }

    public CategoryResponse updateCategory(Long id, CategoryRequest categoryRequest){
        Category categoryByUpdate = categoryRepository.findById(id).orElseThrow(() -> new NoSuchElementException("Not found by id = " + id));
        categoryByUpdate.setName(categoryRequest.getName());
        categoryByUpdate.setDescription(categoryRequest.getDescription());
        return new CategoryResponse(categoryRepository.save(categoryByUpdate));
    }

    public void deleteCategory(Long id){
        Category category = categoryRepository.findById(id).orElseThrow(() -> new NoSuchElementException("Not found by id = " + id));
        categoryRepository.delete(category);
    }

}

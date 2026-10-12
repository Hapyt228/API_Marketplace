package com.example.marketplace.service.category;

import com.example.marketplace.dto.category.CategoryRequest;
import com.example.marketplace.dto.category.CategoryResponse;
import com.example.marketplace.entity.category.Category;
import com.example.marketplace.exception.ResourceNotFoundException;
import com.example.marketplace.repository.category.CategoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService {

    private CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public CategoryResponse getCategoryById(Long id){
        Category category = categoryRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Not found by id = " + id));
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
        Category categoryByUpdate = categoryRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Not found by id = " + id));
        categoryByUpdate.setName(categoryRequest.getName());
        categoryByUpdate.setDescription(categoryRequest.getDescription());
        return new CategoryResponse(categoryRepository.save(categoryByUpdate));
    }

    public void deleteCategory(Long id){
        Category category = categoryRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Not found by id = " + id));
        categoryRepository.delete(category);
    }

}

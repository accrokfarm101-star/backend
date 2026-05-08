package com.greenfood.serviceImpl;

import com.greenfood.exception.ResourceNotFoundException;
import com.greenfood.model.dto.request.CategoryRequest;
import com.greenfood.model.dto.response.CategoryResponse;
import com.greenfood.model.entity.Category;
import com.greenfood.repository.CategoryRepository;
import com.greenfood.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    @Override
    public List<CategoryResponse> getAllCategories(String search) {
        List<Category> categories;
        if (search != null && !search.isBlank()) {
            categories = categoryRepository.findByNameContainingIgnoreCase(search);
        } else {
            categories = categoryRepository.findAll();
        }
        return categories.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public CategoryResponse getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Danh mục", id));
        return mapToResponse(category);
    }

    @Override
    public CategoryResponse createCategory(CategoryRequest categoryRequest) {
        Category category = Category.builder()
                .name(categoryRequest.getName())
                .description(categoryRequest.getDescription())
                .build();
        Category saved = categoryRepository.save(category);
        return mapToResponse(saved);
    }

    @Override
    public CategoryResponse updateCategory(Long id, CategoryRequest categoryRequest) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Danh mục", id));
        category.setName(categoryRequest.getName());
        category.setDescription(categoryRequest.getDescription());
        Category updated = categoryRepository.save(category);
        return mapToResponse(updated);
    }

    @Override
    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Danh mục", id));
        categoryRepository.delete(category);
    }

    private CategoryResponse mapToResponse(Category category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .createdAt(category.getCreatedAt())
                .updatedAt(category.getUpdatedAt())
                .build();
    }
}

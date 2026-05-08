package com.greenfood.service;

import com.greenfood.model.dto.request.CategoryRequest;
import com.greenfood.model.dto.response.CategoryResponse;

import java.util.List;

public interface CategoryService {

    List<CategoryResponse> getAllCategories(String search);

    CategoryResponse getCategoryById(Long id);

    CategoryResponse createCategory(CategoryRequest categoryRequest);

    CategoryResponse updateCategory(Long id, CategoryRequest categoryRequest);

    void deleteCategory(Long id);
}

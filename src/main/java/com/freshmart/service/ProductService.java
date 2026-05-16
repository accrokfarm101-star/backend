package com.freshmart.service;

import com.freshmart.model.dto.request.ProductRequest;
import com.freshmart.model.dto.response.ProductResponse;

import java.util.List;

public interface ProductService {

    List<ProductResponse> getAllProducts(String search);

    ProductResponse getProductById(Long id);

    ProductResponse createProduct(ProductRequest productRequest);

    ProductResponse updateProduct(Long id, ProductRequest productRequest);

    ProductResponse updateProductStatus(Long id, com.freshmart.model.enums.ProductStatus status);

    void deleteProduct(Long id);
}

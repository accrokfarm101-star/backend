package com.greenfood.service;

import com.greenfood.model.dto.request.ProductRequest;
import com.greenfood.model.dto.response.ProductResponse;

import java.util.List;

public interface ProductService {

    List<ProductResponse> getAllProducts(String search);

    ProductResponse getProductById(Long id);

    ProductResponse createProduct(ProductRequest productRequest);

    ProductResponse updateProduct(Long id, ProductRequest productRequest);

    ProductResponse updateProductStatus(Long id, com.greenfood.model.enums.ProductStatus status);

    void deleteProduct(Long id);
}

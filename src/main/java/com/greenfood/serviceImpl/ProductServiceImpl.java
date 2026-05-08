package com.greenfood.serviceImpl;

import com.greenfood.exception.ResourceNotFoundException;
import com.greenfood.model.dto.request.ProductRequest;
import com.greenfood.model.dto.response.ProductResponse;
import com.greenfood.model.entity.Product;
import com.greenfood.repository.ProductRepository;
import com.greenfood.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Override
    public List<ProductResponse> getAllProducts(String search) {
        List<Product> products;
        if (search != null && !search.isBlank()) {
            products = productRepository.findByNameContainingIgnoreCase(search);
        } else {
            products = productRepository.findAll();
        }
        return products.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public ProductResponse getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sản phẩm", id));
        return mapToResponse(product);
    }

    @Override
    public ProductResponse createProduct(ProductRequest productRequest) {
        Product product = Product.builder()
                .name(productRequest.getName())
                .description(productRequest.getDescription())
                .price(productRequest.getPrice())
                .stock(productRequest.getStock())
                .imageUrl(productRequest.getImageUrl())
                .category(productRequest.getCategory())
                .status(productRequest.getStatus() == null ? com.greenfood.model.enums.ProductStatus.IN_STOCK : productRequest.getStatus())
                .build();
        Product saved = productRepository.save(product);
        return mapToResponse(saved);
    }

    @Override
    public ProductResponse updateProduct(Long id, ProductRequest productRequest) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sản phẩm", id));
        product.setName(productRequest.getName());
        product.setDescription(productRequest.getDescription());
        product.setPrice(productRequest.getPrice());
        product.setStock(productRequest.getStock());
        product.setImageUrl(productRequest.getImageUrl());
        product.setCategory(productRequest.getCategory());
        if (productRequest.getStatus() != null) {
            product.setStatus(productRequest.getStatus());
        }
        Product updated = productRepository.save(product);
        return mapToResponse(updated);
    }

    @Override
    public ProductResponse updateProductStatus(Long id, com.greenfood.model.enums.ProductStatus status) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sản phẩm", id));
        product.setStatus(status);
        Product updated = productRepository.save(product);
        return mapToResponse(updated);
    }

    @Override
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sản phẩm", id));
        productRepository.delete(product);
    }

    private ProductResponse mapToResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .stock(product.getStock())
                .imageUrl(product.getImageUrl())
                .category(product.getCategory())
                .status(product.getStatus())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }
}

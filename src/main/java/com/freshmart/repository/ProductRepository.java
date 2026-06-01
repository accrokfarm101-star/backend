package com.freshmart.repository;

import com.freshmart.model.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {

    // Dùng cho ô tìm kiếm sản phẩm
    List<Product> findByNameContainingIgnoreCase(String name);
}
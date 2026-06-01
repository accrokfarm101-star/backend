package com.freshmart.repository;

import com.freshmart.model.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    // Dùng cho chức năng tìm kiếm trên giao diện
    List<Category> findByNameContainingIgnoreCase(String name);

    // Dùng cho DataSeeder để kiểm tra xem danh mục đã tồn tại chưa
    Optional<Category> findByName(String name);
}
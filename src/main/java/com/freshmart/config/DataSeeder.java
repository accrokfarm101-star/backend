package com.freshmart.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.freshmart.model.entity.Category;
import com.freshmart.model.entity.Product;
import com.freshmart.model.enums.ProductStatus;
import com.freshmart.repository.CategoryRepository;
import com.freshmart.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Component
public class DataSeeder implements CommandLineRunner {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Override
    public void run(String... args) throws Exception {
        // Chỉ nạp dữ liệu nếu bảng Product đang trống rỗng
        if (productRepository.count() == 0) {
            System.out.println("Bắt đầu nạp dữ liệu sản phẩm mồi (Seed Data) vào Database...");

            try {
                // Đọc file products.json từ thư mục resources
                ObjectMapper mapper = new ObjectMapper();
                InputStream inputStream = new ClassPathResource("products.json").getInputStream();

                // Đọc JSON thành dạng List<Map> để dễ bóc tách linh hoạt
                List<Map<String, Object>> productsJson = mapper.readValue(inputStream, new TypeReference<List<Map<String, Object>>>() {});

                for (Map<String, Object> item : productsJson) {
                    // 1. Tìm hoặc tạo mới Danh mục (Category)
                    String categoryName = (String) item.get("category");
                    Category category = categoryRepository.findByName(categoryName).orElseGet(() -> {
                        Category newCat = new Category();
                        newCat.setName(categoryName);
                        newCat.setDescription("Danh mục các loại " + categoryName.toLowerCase());
                        return categoryRepository.save(newCat); // Lưu danh mục mới vào DB
                    });

                    // 2. Chuyển đổi dữ liệu JSON thành Entity Product
                    Product product = new Product();
                    product.setName((String) item.get("name"));
                    // Sử dụng fullDesc làm description chính
                    product.setDescription((String) item.get("fullDesc"));
                    product.setPrice(new BigDecimal(item.get("price").toString()));
                    product.setStock(100); // Mặc định cho tồn kho 100 cái để dễ test mua hàng
                    product.setImageUrl((String) item.get("image"));
                    product.setStatus(ProductStatus.IN_STOCK); // Set trạng thái CÒN HÀNG
                    product.setCategory(categoryName); // Nối khóa ngoại (Foreign Key)

                    // Lưu sản phẩm vào DB
                    productRepository.save(product);
                }

                System.out.println("Thành công! Đã bơm " + productsJson.size() + " sản phẩm vào cơ sở dữ liệu.");

            } catch (Exception e) {
                System.err.println("Lỗi khi nạp dữ liệu mồi: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.out.println("Cơ sở dữ liệu đã có sẵn sản phẩm. Bỏ qua bước nạp Seed Data.");
        }
    }
}
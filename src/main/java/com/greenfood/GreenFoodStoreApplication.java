package com.greenfood;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Điểm khởi động chính của ứng dụng Green Food Store.
 * Chạy lệnh: mvn spring-boot:run để khởi động server.
 */
@SpringBootApplication
public class GreenFoodStoreApplication {
    public static void main(String[] args) {
        SpringApplication.run(GreenFoodStoreApplication.class, args);
    }
}

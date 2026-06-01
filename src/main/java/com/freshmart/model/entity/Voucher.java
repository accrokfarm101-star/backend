package com.freshmart.model.entity;

import com.freshmart.model.enums.DiscountType;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "vouchers")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Voucher {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DiscountType discountType;

    @Column(nullable = false)
    private Double discountValue;

    private Double minOrderAmount; // Đơn tối thiểu để áp dụng

    @Column(nullable = false)
    private LocalDateTime expiryDate;

    @Column(nullable = false)
    private String status; // ACTIVE hoặc INACTIVE

    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
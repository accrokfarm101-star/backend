package com.freshmart.model.dto.request;

import com.freshmart.model.enums.DiscountType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class VoucherRequest {
    @NotBlank(message = "Mã voucher không được để trống")
    private String code;

    @NotNull(message = "Loại giảm giá là bắt buộc")
    private DiscountType discountType;

    @NotNull(message = "Giá trị giảm giá là bắt buộc")
    private Double discountValue;

    private Double minOrderAmount;

    @NotNull(message = "Ngày hết hạn là bắt buộc")
    private LocalDateTime expiryDate;

    private String status;
}
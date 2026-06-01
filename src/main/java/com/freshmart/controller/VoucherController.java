package com.freshmart.controller;

import com.freshmart.model.dto.request.VoucherRequest;
import com.freshmart.model.dto.response.ApiResponse;
import com.freshmart.model.entity.Voucher;
import com.freshmart.repository.VoucherRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vouchers")
@CrossOrigin(origins = "*")
public class VoucherController {

    @Autowired
    private VoucherRepository voucherRepository;

    // Lấy danh sách Voucher (Admin & Customer đều xem được)
    @GetMapping
    public ResponseEntity<ApiResponse<List<Voucher>>> getAllVouchers() {
        List<Voucher> vouchers = voucherRepository.findAll();
        return ResponseEntity.ok(ApiResponse.success(vouchers, "Lấy danh sách thành công"));
    }

    // Thêm Voucher mới (Chỉ Admin)
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Voucher>> createVoucher(@Valid @RequestBody VoucherRequest request) {
        if (voucherRepository.existsByCode(request.getCode())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Mã Voucher đã tồn tại!"));
        }

        Voucher voucher = Voucher.builder()
                .code(request.getCode())
                .discountType(request.getDiscountType())
                .discountValue(request.getDiscountValue())
                .minOrderAmount(request.getMinOrderAmount())
                .expiryDate(request.getExpiryDate())
                .status(request.getStatus() != null ? request.getStatus() : "ACTIVE")
                .build();

        Voucher savedVoucher = voucherRepository.save(voucher);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(savedVoucher, "Tạo Voucher thành công"));
    }

    // Xóa Voucher (Chỉ Admin)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteVoucher(@PathVariable Long id) {
        if (!voucherRepository.existsById(id)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Không tìm thấy Voucher"));
        }
        voucherRepository.deleteById(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Xóa Voucher thành công"));
    }
}
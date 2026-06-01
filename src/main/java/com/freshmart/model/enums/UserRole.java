package com.freshmart.model.enums;

/**
 * Vai trò người dùng trong hệ thống Fresh Mart
 */
public enum UserRole {
    ROLE_ADMIN,      // Toàn quyền hệ thống
    ROLE_STAFF,      // Nhân viên tổng hợp (Quản lý kho, đơn hàng, CSKH, khuyến mãi)
    ROLE_CUSTOMER    // Khách hàng mua sắm
}
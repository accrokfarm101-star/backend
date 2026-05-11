package com.freshmart.controller;

import com.freshmart.model.dto.response.ApiResponse;
import com.freshmart.model.dto.response.ProductResponse;
import com.freshmart.service.FavoriteService;
import com.freshmart.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/favorites")
public class FavoriteController {

    @Autowired
    private FavoriteService favoriteService;

    @Autowired
    private UserService userService;

    @GetMapping
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getFavorites(Principal principal) {
        try {
            Long userId = getCurrentUserId(principal);
            List<ProductResponse> favorites = favoriteService.getFavorites(userId);
            return ResponseEntity.ok(ApiResponse.success(favorites, "Danh sách sản phẩm yêu thích"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/{productId}")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> addFavorite(
            @PathVariable Long productId,
            Principal principal) {
        try {
            Long userId = getCurrentUserId(principal);
            List<ProductResponse> favorites = favoriteService.addFavorite(userId, productId);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(favorites, "Thêm sản phẩm yêu thích thành công"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/{productId}")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> removeFavorite(
            @PathVariable Long productId,
            Principal principal) {
        try {
            Long userId = getCurrentUserId(principal);
            List<ProductResponse> favorites = favoriteService.removeFavorite(userId, productId);
            return ResponseEntity.ok(ApiResponse.success(favorites, "Xóa sản phẩm yêu thích thành công"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> clearFavorites(Principal principal) {
        try {
            Long userId = getCurrentUserId(principal);
            List<ProductResponse> favorites = favoriteService.clearFavorites(userId);
            return ResponseEntity.ok(ApiResponse.success(favorites, "Đã xóa toàn bộ sản phẩm yêu thích"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(e.getMessage()));
        }
    }

    private Long getCurrentUserId(Principal principal) {
        return userService.findByUsername(principal.getName()).getId();
    }
}

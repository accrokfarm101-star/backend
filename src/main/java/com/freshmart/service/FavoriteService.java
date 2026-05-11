package com.freshmart.service;

import com.freshmart.model.dto.response.ProductResponse;

import java.util.List;

public interface FavoriteService {

    List<ProductResponse> getFavorites(Long userId);

    List<ProductResponse> addFavorite(Long userId, Long productId);

    List<ProductResponse> removeFavorite(Long userId, Long productId);

    List<ProductResponse> clearFavorites(Long userId);
}

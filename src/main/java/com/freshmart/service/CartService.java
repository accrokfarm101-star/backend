package com.freshmart.service;

import com.freshmart.model.dto.request.CartItemRequest;
import com.freshmart.model.dto.response.CartResponse;

public interface CartService {

    CartResponse getCart(Long userId);

    CartResponse addItem(Long userId, CartItemRequest cartItemRequest);

    CartResponse updateItem(Long userId, Long cartItemId, Integer quantity);

    CartResponse removeItem(Long userId, Long cartItemId);

    CartResponse clearCart(Long userId);
}

package com.freshmart.serviceImpl;

import com.freshmart.exception.ResourceNotFoundException;
import com.freshmart.model.dto.request.CartItemRequest;
import com.freshmart.model.dto.response.CartItemResponse;
import com.freshmart.model.dto.response.CartResponse;
import com.freshmart.model.entity.Cart;
import com.freshmart.model.entity.CartItem;
import com.freshmart.model.entity.Product;
import com.freshmart.model.entity.User;
import com.freshmart.repository.CartItemRepository;
import com.freshmart.repository.CartRepository;
import com.freshmart.repository.ProductRepository;
import com.freshmart.repository.UserRepository;
import com.freshmart.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class CartServiceImpl implements CartService {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Override
    public CartResponse getCart(Long userId) {
        Cart cart = getOrCreateCart(userId);
        return mapToResponse(cart);
    }

    @Override
    public CartResponse addItem(Long userId, CartItemRequest cartItemRequest) {
        Cart cart = getOrCreateCart(userId);
        Product product = productRepository.findById(cartItemRequest.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Sản phẩm", cartItemRequest.getProductId()));

        int newQuantity = cartItemRequest.getQuantity();
        Optional<CartItem> existingItem = cart.getCartItems().stream()
                .filter(item -> item.getProduct().getId().equals(product.getId()))
                .findFirst();

        if (existingItem.isPresent()) {
            CartItem cartItem = existingItem.get();
            int totalQuantity = cartItem.getQuantity() + newQuantity;
            validateStock(product, totalQuantity);
            cartItem.setQuantity(totalQuantity);
            cartItem.calculateTotalPrice();
        } else {
            validateStock(product, newQuantity);
            CartItem cartItem = CartItem.builder()
                    .cart(cart)
                    .product(product)
                    .quantity(newQuantity)
                    .unitPrice(product.getPrice())
                    .build();
            cartItem.calculateTotalPrice();
            cart.getCartItems().add(cartItem);
        }

        recalculateCart(cart);
        Cart saved = cartRepository.save(cart);
        return mapToResponse(saved);
    }

    @Override
    public CartResponse updateItem(Long userId, Long cartItemId, Integer quantity) {
        if (quantity == null || quantity < 1) {
            throw new IllegalArgumentException("Số lượng phải lớn hơn hoặc bằng 1");
        }

        Cart cart = getOrCreateCart(userId);
        CartItem cartItem = cartItemRepository.findByIdAndCartId(cartItemId, cart.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Mục giỏ hàng", cartItemId));

        validateStock(cartItem.getProduct(), quantity);
        cartItem.setQuantity(quantity);
        cartItem.calculateTotalPrice();
        recalculateCart(cart);
        Cart saved = cartRepository.save(cart);
        return mapToResponse(saved);
    }

    @Override
    public CartResponse removeItem(Long userId, Long cartItemId) {
        Cart cart = getOrCreateCart(userId);
        CartItem cartItem = cartItemRepository.findByIdAndCartId(cartItemId, cart.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Mục giỏ hàng", cartItemId));

        cart.getCartItems().remove(cartItem);
        cartItemRepository.delete(cartItem);
        recalculateCart(cart);
        Cart saved = cartRepository.save(cart);
        return mapToResponse(saved);
    }

    @Override
    public CartResponse clearCart(Long userId) {
        Cart cart = getOrCreateCart(userId);
        cart.getCartItems().clear();
        cart.setTotalAmount(BigDecimal.ZERO);
        Cart saved = cartRepository.save(cart);
        return mapToResponse(saved);
    }

    private Cart getOrCreateCart(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Người dùng", userId));
        return cartRepository.findByUserId(userId)
                .orElseGet(() -> Cart.builder()
                        .user(user)
                        .cartItems(new ArrayList<>())
                        .totalAmount(BigDecimal.ZERO)
                        .build());
    }

    private void recalculateCart(Cart cart) {
        BigDecimal totalAmount = cart.getCartItems().stream()
                .map(CartItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        cart.setTotalAmount(totalAmount);
    }

    private void validateStock(Product product, int quantity) {
        if (product.getStock() < quantity) {
            throw new IllegalArgumentException("Sản phẩm " + product.getName() + " không đủ tồn kho. Hiện có: " + product.getStock());
        }
    }

    private CartResponse mapToResponse(Cart cart) {
        List<CartItemResponse> cartItems = new ArrayList<>();
        if (cart.getCartItems() != null) {
            cartItems = cart.getCartItems().stream()
                    .map(item -> CartItemResponse.builder()
                            .id(item.getId())
                            .productId(item.getProduct().getId())
                            .productName(item.getProduct().getName())
                            .quantity(item.getQuantity())
                            .unitPrice(item.getUnitPrice())
                            .totalPrice(item.getTotalPrice())
                            .build())
                    .collect(Collectors.toList());
        }

        return CartResponse.builder()
                .id(cart.getId())
                .userId(cart.getUser().getId())
                .username(cart.getUser().getUsername())
                .cartItems(cartItems)
                .totalAmount(cart.getTotalAmount())
                .createdAt(cart.getCreatedAt())
                .updatedAt(cart.getUpdatedAt())
                .build();
    }
}

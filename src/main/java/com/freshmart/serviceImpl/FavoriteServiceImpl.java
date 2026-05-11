package com.freshmart.serviceImpl;

import com.freshmart.exception.ResourceNotFoundException;
import com.freshmart.model.dto.response.ProductResponse;
import com.freshmart.model.entity.Favorite;
import com.freshmart.model.entity.Product;
import com.freshmart.model.entity.User;
import com.freshmart.repository.FavoriteRepository;
import com.freshmart.repository.ProductRepository;
import com.freshmart.repository.UserRepository;
import com.freshmart.service.FavoriteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class FavoriteServiceImpl implements FavoriteService {

    @Autowired
    private FavoriteRepository favoriteRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Override
    public List<ProductResponse> getFavorites(Long userId) {
        return favoriteRepository.findByUserId(userId).stream()
                .map(favorite -> mapProductToResponse(favorite.getProduct()))
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductResponse> addFavorite(Long userId, Long productId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Người dùng", userId));
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Sản phẩm", productId));

        favoriteRepository.findByUserIdAndProductId(userId, productId)
                .orElseGet(() -> favoriteRepository.save(Favorite.builder()
                        .user(user)
                        .product(product)
                        .build()));

        return getFavorites(userId);
    }

    @Override
    public List<ProductResponse> removeFavorite(Long userId, Long productId) {
        Favorite favorite = favoriteRepository.findByUserIdAndProductId(userId, productId)
                .orElseThrow(() -> new ResourceNotFoundException("Mục yêu thích", productId));
        favoriteRepository.delete(favorite);
        return getFavorites(userId);
    }

    @Override
    public List<ProductResponse> clearFavorites(Long userId) {
        List<Favorite> favorites = favoriteRepository.findByUserId(userId);
        favoriteRepository.deleteAll(favorites);
        return getFavorites(userId);
    }

    private ProductResponse mapProductToResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .stock(product.getStock())
                .imageUrl(product.getImageUrl())
                .category(product.getCategory())
                .status(product.getStatus())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }
}

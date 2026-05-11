package com.freshmart.repository;

import com.freshmart.model.entity.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

    Optional<Favorite> findByUserIdAndProductId(Long userId, Long productId);

    List<Favorite> findByUserId(Long userId);

    void deleteByUserIdAndProductId(Long userId, Long productId);
}

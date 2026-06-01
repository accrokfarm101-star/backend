package com.freshmart.repository;

import com.freshmart.model.entity.Voucher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VoucherRepository extends JpaRepository<Voucher, Long> {
    Optional<Voucher> findByCode(String code);
    boolean existsByCode(String code);
}
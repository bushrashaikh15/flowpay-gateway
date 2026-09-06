package com.flowpay.flowpay.repository;

import com.flowpay.flowpay.entity.Merchant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MerchantRepository extends JpaRepository<Merchant, Long> {

    Optional<Merchant> findByApiKey(String apiKey);

}
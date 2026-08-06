package com.flowpay.flowpay.repository;

import com.flowpay.flowpay.entity.Merchant;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MerchantRepository extends JpaRepository<Merchant, Long> {

}
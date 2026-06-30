package com.zorysa.finance.installments.repository;

import com.zorysa.finance.installments.entity.CardPurchase;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface CardPurchaseRepository extends JpaRepository<CardPurchase, UUID>, JpaSpecificationExecutor<CardPurchase> {

    Optional<CardPurchase> findByIdAndUserId(UUID id, UUID userId);

    Page<CardPurchase> findAllByUserIdAndCreditCardId(UUID userId, UUID creditCardId, Pageable pageable);
}

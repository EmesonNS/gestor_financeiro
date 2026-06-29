package com.zorysa.finance.creditcards.repository;

import com.zorysa.finance.creditcards.entity.CreditCard;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CreditCardRepository extends JpaRepository<CreditCard, UUID> {

    Optional<CreditCard> findByIdAndUserId(UUID id, UUID userId);

    Page<CreditCard> findAllByUserId(UUID userId, Pageable pageable);

    Page<CreditCard> findAllByUserIdAndArchived(UUID userId, Boolean archived, Pageable pageable);
}

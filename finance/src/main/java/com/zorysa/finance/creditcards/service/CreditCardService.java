package com.zorysa.finance.creditcards.service;

import com.zorysa.finance.creditcards.dto.CreateCreditCardRequest;
import com.zorysa.finance.creditcards.dto.CreditCardResponse;
import com.zorysa.finance.creditcards.dto.UpdateCreditCardRequest;
import com.zorysa.finance.creditcards.entity.CreditCard;
import com.zorysa.finance.creditcards.mapper.CreditCardMapper;
import com.zorysa.finance.creditcards.repository.CreditCardRepository;
import com.zorysa.finance.shared.exception.BadRequestException;
import com.zorysa.finance.shared.exception.NotFoundException;
import java.math.BigDecimal;
import java.util.UUID;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreditCardService {

    private final ObjectProvider<CreditCardRepository> creditCardRepository;
    private final CreditCardMapper creditCardMapper;

    public CreditCardService(ObjectProvider<CreditCardRepository> creditCardRepository, CreditCardMapper creditCardMapper) {
        this.creditCardRepository = creditCardRepository;
        this.creditCardMapper = creditCardMapper;
    }

    @Transactional(readOnly = true)
    public Page<CreditCardResponse> listCreditCards(UUID userId, Boolean archived, Pageable pageable) {
        Page<CreditCard> cards = archived == null
                ? creditCardRepository().findAllByUserId(userId, pageable)
                : creditCardRepository().findAllByUserIdAndArchived(userId, archived, pageable);
        return cards.map(card -> creditCardMapper.toResponse(card, calculateUsedLimit(userId, card.getId())));
    }

    @Transactional
    public CreditCardResponse createCreditCard(UUID userId, CreateCreditCardRequest request) {
        CreditCard creditCard = new CreditCard(
                userId,
                request.name().trim(),
                request.limitAmount(),
                request.closingDay(),
                request.dueDay()
        );
        CreditCard saved = creditCardRepository().save(creditCard);
        return creditCardMapper.toResponse(saved, calculateUsedLimit(userId, saved.getId()));
    }

    @Transactional(readOnly = true)
    public CreditCardResponse getCreditCard(UUID userId, UUID creditCardId) {
        CreditCard creditCard = findOwnedCreditCard(userId, creditCardId);
        return creditCardMapper.toResponse(creditCard, calculateUsedLimit(userId, creditCard.getId()));
    }

    @Transactional
    public CreditCardResponse updateCreditCard(UUID userId, UUID creditCardId, UpdateCreditCardRequest request) {
        CreditCard creditCard = findOwnedCreditCard(userId, creditCardId);
        creditCard.updateDetails(
                request.name().trim(),
                request.limitAmount(),
                request.closingDay(),
                request.dueDay()
        );
        CreditCard saved = creditCardRepository().save(creditCard);
        return creditCardMapper.toResponse(saved, calculateUsedLimit(userId, saved.getId()));
    }

    @Transactional
    public void deleteCreditCard(UUID userId, UUID creditCardId) {
        CreditCard creditCard = findOwnedCreditCard(userId, creditCardId);
        creditCardRepository().delete(creditCard);
    }

    @Transactional
    public CreditCardResponse archiveCreditCard(UUID userId, UUID creditCardId) {
        CreditCard creditCard = findOwnedCreditCard(userId, creditCardId);
        creditCard.archive();
        CreditCard saved = creditCardRepository().save(creditCard);
        return creditCardMapper.toResponse(saved, calculateUsedLimit(userId, saved.getId()));
    }

    @Transactional(readOnly = true)
    public BigDecimal calculateUsedLimit(UUID userId, UUID creditCardId) {
        findOwnedCreditCard(userId, creditCardId);
        return BigDecimal.ZERO;
    }

    public BigDecimal calculateAvailableLimit(BigDecimal limitAmount, BigDecimal usedLimit) {
        BigDecimal available = limitAmount.subtract(usedLimit);
        return available.signum() < 0 ? BigDecimal.ZERO : available;
    }

    @Transactional(readOnly = true)
    public void validateCanReceivePurchases(UUID userId, UUID creditCardId) {
        CreditCard creditCard = findOwnedCreditCard(userId, creditCardId);
        if (!creditCard.canReceivePurchases()) {
            throw new BadRequestException("Cartao arquivado nao aceita compras");
        }
    }

    private CreditCard findOwnedCreditCard(UUID userId, UUID creditCardId) {
        return creditCardRepository().findByIdAndUserId(creditCardId, userId)
                .orElseThrow(() -> new NotFoundException("Cartao de credito nao encontrado"));
    }

    private CreditCardRepository creditCardRepository() {
        return creditCardRepository.getIfAvailable(() -> {
            throw new IllegalStateException("CreditCardRepository nao disponivel");
        });
    }
}

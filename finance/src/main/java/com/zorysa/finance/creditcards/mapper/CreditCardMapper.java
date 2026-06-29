package com.zorysa.finance.creditcards.mapper;

import com.zorysa.finance.creditcards.dto.CreditCardResponse;
import com.zorysa.finance.creditcards.entity.CreditCard;
import java.math.BigDecimal;
import org.springframework.stereotype.Component;

@Component
public class CreditCardMapper {

    public CreditCardResponse toResponse(CreditCard creditCard, BigDecimal usedLimit) {
        return new CreditCardResponse(
                creditCard.getId(),
                creditCard.getName(),
                creditCard.getLimitAmount(),
                creditCard.usedLimit(usedLimit),
                creditCard.availableLimit(usedLimit),
                creditCard.getClosingDay(),
                creditCard.getDueDay(),
                creditCard.isArchived(),
                creditCard.getCreatedAt(),
                creditCard.getUpdatedAt()
        );
    }
}

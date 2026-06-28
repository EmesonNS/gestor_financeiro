package com.zorysa.finance.transactions.mapper;

import com.zorysa.finance.transactions.dto.TransactionResponse;
import com.zorysa.finance.transactions.entity.Transaction;
import org.springframework.stereotype.Component;

@Component
public class TransactionMapper {

    public TransactionResponse toResponse(Transaction transaction) {
        return new TransactionResponse(
                transaction.getId(),
                transaction.getDescription(),
                transaction.getAmount(),
                transaction.getType(),
                transaction.getTransactionDate(),
                transaction.getCategoryId(),
                transaction.getAccountId(),
                transaction.getStatus(),
                transaction.getNotes(),
                transaction.getCreatedAt(),
                transaction.getUpdatedAt()
        );
    }
}

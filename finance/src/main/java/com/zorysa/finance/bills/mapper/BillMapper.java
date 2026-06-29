package com.zorysa.finance.bills.mapper;

import com.zorysa.finance.bills.dto.BillResponse;
import com.zorysa.finance.bills.entity.Bill;
import java.time.LocalDate;
import org.springframework.stereotype.Component;

@Component
public class BillMapper {

    public BillResponse toResponse(Bill bill) {
        return new BillResponse(
                bill.getId(),
                bill.getDescription(),
                bill.getAmount(),
                bill.getDueDate(),
                bill.getCategoryId(),
                bill.getAccountId(),
                bill.getStatus(),
                bill.getPaidAt(),
                bill.getTransactionId(),
                bill.isOverdue(LocalDate.now()),
                bill.getCreatedAt(),
                bill.getUpdatedAt()
        );
    }
}

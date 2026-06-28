package com.zorysa.finance.accounts.mapper;

import com.zorysa.finance.accounts.dto.AccountResponse;
import com.zorysa.finance.accounts.entity.Account;
import org.springframework.stereotype.Component;

@Component
public class AccountMapper {

    public AccountResponse toResponse(Account account) {
        return new AccountResponse(
                account.getId(),
                account.getName(),
                account.getType(),
                account.getInitialBalance(),
                account.getCurrentBalance(),
                account.isArchived(),
                account.getCreatedAt(),
                account.getUpdatedAt()
        );
    }
}

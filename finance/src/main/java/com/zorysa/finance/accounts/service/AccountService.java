package com.zorysa.finance.accounts.service;

import com.zorysa.finance.accounts.dto.AccountResponse;
import com.zorysa.finance.accounts.dto.CreateAccountRequest;
import com.zorysa.finance.accounts.dto.UpdateAccountRequest;
import com.zorysa.finance.accounts.entity.Account;
import com.zorysa.finance.accounts.entity.AccountType;
import com.zorysa.finance.accounts.mapper.AccountMapper;
import com.zorysa.finance.accounts.repository.AccountRepository;
import com.zorysa.finance.shared.exception.NotFoundException;
import java.util.UUID;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AccountService {

    private final ObjectProvider<AccountRepository> accountRepository;
    private final AccountMapper accountMapper;

    public AccountService(ObjectProvider<AccountRepository> accountRepository, AccountMapper accountMapper) {
        this.accountRepository = accountRepository;
        this.accountMapper = accountMapper;
    }

    @Transactional(readOnly = true)
    public Page<AccountResponse> listAccounts(UUID userId, Boolean archived, AccountType type, Pageable pageable) {
        Page<Account> accounts;
        if (archived != null && type != null) {
            accounts = repository().findAllByUserIdAndArchivedAndType(userId, archived, type, pageable);
        } else if (archived != null) {
            accounts = repository().findAllByUserIdAndArchived(userId, archived, pageable);
        } else if (type != null) {
            accounts = repository().findAllByUserIdAndType(userId, type, pageable);
        } else {
            accounts = repository().findAllByUserId(userId, pageable);
        }
        return accounts.map(accountMapper::toResponse);
    }

    @Transactional
    public AccountResponse createAccount(UUID userId, CreateAccountRequest request) {
        Account account = new Account(
                userId,
                request.name().trim(),
                request.type(),
                request.initialBalance()
        );
        return accountMapper.toResponse(repository().save(account));
    }

    @Transactional(readOnly = true)
    public AccountResponse getAccount(UUID userId, UUID accountId) {
        return accountMapper.toResponse(findOwnedAccount(userId, accountId));
    }

    @Transactional
    public AccountResponse updateAccount(UUID userId, UUID accountId, UpdateAccountRequest request) {
        Account account = findOwnedAccount(userId, accountId);
        account.updateDetails(request.name().trim(), request.type());
        return accountMapper.toResponse(repository().save(account));
    }

    @Transactional
    public AccountResponse archiveAccount(UUID userId, UUID accountId) {
        Account account = findOwnedAccount(userId, accountId);
        account.archive();
        return accountMapper.toResponse(repository().save(account));
    }

    @Transactional
    public void deleteAccount(UUID userId, UUID accountId) {
        Account account = findOwnedAccount(userId, accountId);
        repository().delete(account);
    }

    private Account findOwnedAccount(UUID userId, UUID accountId) {
        return repository().findByIdAndUserId(accountId, userId)
                .orElseThrow(() -> new NotFoundException("Conta nao encontrada"));
    }

    private AccountRepository repository() {
        return accountRepository.getIfAvailable(() -> {
            throw new IllegalStateException("AccountRepository nao disponivel");
        });
    }
}

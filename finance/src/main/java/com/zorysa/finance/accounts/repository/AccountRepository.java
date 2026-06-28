package com.zorysa.finance.accounts.repository;

import com.zorysa.finance.accounts.entity.Account;
import com.zorysa.finance.accounts.entity.AccountType;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<Account, UUID> {

    Optional<Account> findByIdAndUserId(UUID id, UUID userId);

    Page<Account> findAllByUserId(UUID userId, Pageable pageable);

    Page<Account> findAllByUserIdAndArchived(UUID userId, boolean archived, Pageable pageable);

    Page<Account> findAllByUserIdAndType(UUID userId, AccountType type, Pageable pageable);

    Page<Account> findAllByUserIdAndArchivedAndType(UUID userId, boolean archived, AccountType type, Pageable pageable);
}

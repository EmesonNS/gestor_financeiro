package com.zorysa.finance.transactions.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.zorysa.finance.accounts.entity.Account;
import com.zorysa.finance.accounts.entity.AccountType;
import com.zorysa.finance.accounts.repository.AccountRepository;
import com.zorysa.finance.categories.entity.Category;
import com.zorysa.finance.categories.entity.CategoryType;
import com.zorysa.finance.categories.repository.CategoryRepository;
import com.zorysa.finance.shared.exception.BadRequestException;
import com.zorysa.finance.transactions.dto.CreateTransactionRequest;
import com.zorysa.finance.transactions.dto.MarkTransactionAsPaidRequest;
import com.zorysa.finance.transactions.dto.UpdateTransactionRequest;
import com.zorysa.finance.transactions.entity.Transaction;
import com.zorysa.finance.transactions.entity.TransactionStatus;
import com.zorysa.finance.transactions.entity.TransactionType;
import com.zorysa.finance.transactions.mapper.TransactionMapper;
import com.zorysa.finance.transactions.repository.TransactionRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;

@ExtendWith(MockitoExtension.class)
class TransactionInsufficientBalanceServiceTest {

    private static final UUID USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID ACCOUNT_ID = UUID.fromString("00000000-0000-0000-0000-000000000002");
    private static final UUID CATEGORY_ID = UUID.fromString("00000000-0000-0000-0000-000000000003");
    private static final UUID TRANSACTION_ID = UUID.fromString("00000000-0000-0000-0000-000000000004");

    @Mock
    private ObjectProvider<TransactionRepository> transactionRepositoryProvider;

    @Mock
    private ObjectProvider<AccountRepository> accountRepositoryProvider;

    @Mock
    private ObjectProvider<CategoryRepository> categoryRepositoryProvider;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private CategoryRepository categoryRepository;

    private TransactionService service;

    @BeforeEach
    void setUp() {
        when(transactionRepositoryProvider.getIfAvailable(any(Supplier.class))).thenReturn(transactionRepository);
        when(accountRepositoryProvider.getIfAvailable(any(Supplier.class))).thenReturn(accountRepository);
        when(categoryRepositoryProvider.getIfAvailable(any(Supplier.class))).thenReturn(categoryRepository);
        lenient().when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));
        lenient().when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));
        service = new TransactionService(
                transactionRepositoryProvider,
                accountRepositoryProvider,
                categoryRepositoryProvider,
                new TransactionMapper()
        );
    }

    @Test
    void shouldBlockPaidExpenseCreationWhenAccountBalanceIsInsufficient() {
        Account account = accountWithBalance("100.00");
        allowExpenseCategory();
        when(accountRepository.findByIdAndUserId(ACCOUNT_ID, USER_ID)).thenReturn(Optional.of(account));

        assertThatThrownBy(() -> service.createTransaction(USER_ID, createPaidExpense("150.00")))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Saldo insuficiente");

        assertThat(account.getCurrentBalance()).isEqualByComparingTo("100.00");
        verify(transactionRepository, never()).save(any(Transaction.class));
        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    void shouldBlockMarkingPendingExpenseAsPaidWhenAccountBalanceIsInsufficient() {
        Account account = accountWithBalance("40.00");
        Transaction transaction = pendingExpense("75.00");
        when(transactionRepository.findByIdAndUserId(TRANSACTION_ID, USER_ID)).thenReturn(Optional.of(transaction));
        when(accountRepository.findByIdAndUserId(ACCOUNT_ID, USER_ID)).thenReturn(Optional.of(account));

        assertThatThrownBy(() -> service.markAsPaid(
                USER_ID,
                TRANSACTION_ID,
                new MarkTransactionAsPaidRequest(ACCOUNT_ID, LocalDate.of(2026, 6, 20))
        ))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Saldo insuficiente");

        assertThat(account.getCurrentBalance()).isEqualByComparingTo("40.00");
        assertThat(transaction.getStatus()).isEqualTo(TransactionStatus.PENDING);
        verify(transactionRepository, never()).save(any(Transaction.class));
        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    void shouldBlockUpdatingPaidExpenseWhenAvailableBalanceAfterReversalIsInsufficient() {
        Account account = accountWithBalance("10.00");
        Transaction transaction = paidExpense("40.00");
        allowExpenseCategory();
        when(transactionRepository.findByIdAndUserId(TRANSACTION_ID, USER_ID)).thenReturn(Optional.of(transaction));
        when(accountRepository.findByIdAndUserId(ACCOUNT_ID, USER_ID)).thenReturn(Optional.of(account));

        assertThatThrownBy(() -> service.updateTransaction(USER_ID, TRANSACTION_ID, updatePaidExpense("80.00")))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Saldo insuficiente");

        assertThat(account.getCurrentBalance()).isEqualByComparingTo("10.00");
        assertThat(transaction.getAmount()).isEqualByComparingTo("40.00");
        verify(transactionRepository, never()).save(any(Transaction.class));
        verify(accountRepository, never()).save(any(Account.class));
    }

    private void allowExpenseCategory() {
        Category category = new Category(USER_ID, "Mercado", CategoryType.EXPENSE, "#111111", "shopping-cart", false);
        when(categoryRepository.findByIdAndUserIdOrDefault(CATEGORY_ID, USER_ID)).thenReturn(Optional.of(category));
    }

    private Account accountWithBalance(String balance) {
        return new Account(USER_ID, "Carteira", AccountType.CHECKING_ACCOUNT, new BigDecimal(balance));
    }

    private CreateTransactionRequest createPaidExpense(String amount) {
        return new CreateTransactionRequest(
                "Mercado",
                new BigDecimal(amount),
                TransactionType.EXPENSE,
                LocalDate.of(2026, 6, 20),
                CATEGORY_ID,
                ACCOUNT_ID,
                TransactionStatus.PAID,
                null
        );
    }

    private UpdateTransactionRequest updatePaidExpense(String amount) {
        return new UpdateTransactionRequest(
                "Mercado",
                new BigDecimal(amount),
                TransactionType.EXPENSE,
                LocalDate.of(2026, 6, 21),
                CATEGORY_ID,
                ACCOUNT_ID,
                TransactionStatus.PAID,
                null
        );
    }

    private Transaction pendingExpense(String amount) {
        return new Transaction(
                USER_ID,
                "Mercado",
                new BigDecimal(amount),
                TransactionType.EXPENSE,
                LocalDate.of(2026, 6, 20),
                CATEGORY_ID,
                null,
                TransactionStatus.PENDING,
                null
        );
    }

    private Transaction paidExpense(String amount) {
        return new Transaction(
                USER_ID,
                "Mercado",
                new BigDecimal(amount),
                TransactionType.EXPENSE,
                LocalDate.of(2026, 6, 20),
                CATEGORY_ID,
                ACCOUNT_ID,
                TransactionStatus.PAID,
                null
        );
    }
}

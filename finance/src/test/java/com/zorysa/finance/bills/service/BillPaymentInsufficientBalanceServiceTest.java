package com.zorysa.finance.bills.service;

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
import com.zorysa.finance.bills.dto.PayBillRequest;
import com.zorysa.finance.bills.entity.Bill;
import com.zorysa.finance.bills.entity.BillStatus;
import com.zorysa.finance.bills.mapper.BillMapper;
import com.zorysa.finance.bills.repository.BillRepository;
import com.zorysa.finance.categories.entity.Category;
import com.zorysa.finance.categories.entity.CategoryType;
import com.zorysa.finance.categories.repository.CategoryRepository;
import com.zorysa.finance.shared.exception.BadRequestException;
import com.zorysa.finance.transactions.entity.Transaction;
import com.zorysa.finance.transactions.mapper.TransactionMapper;
import com.zorysa.finance.transactions.repository.TransactionRepository;
import com.zorysa.finance.transactions.service.TransactionService;
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
class BillPaymentInsufficientBalanceServiceTest {

    private static final UUID USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000011");
    private static final UUID BILL_ID = UUID.fromString("00000000-0000-0000-0000-000000000012");
    private static final UUID ACCOUNT_ID = UUID.fromString("00000000-0000-0000-0000-000000000013");
    private static final UUID CATEGORY_ID = UUID.fromString("00000000-0000-0000-0000-000000000014");

    @Mock
    private ObjectProvider<BillRepository> billRepositoryProvider;

    @Mock
    private ObjectProvider<AccountRepository> accountRepositoryProvider;

    @Mock
    private ObjectProvider<CategoryRepository> categoryRepositoryProvider;

    @Mock
    private ObjectProvider<TransactionRepository> transactionRepositoryProvider;

    @Mock
    private BillRepository billRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private TransactionRepository transactionRepository;

    private BillService service;

    @BeforeEach
    void setUp() {
        when(billRepositoryProvider.getIfAvailable(any(Supplier.class))).thenReturn(billRepository);
        when(accountRepositoryProvider.getIfAvailable(any(Supplier.class))).thenReturn(accountRepository);
        when(categoryRepositoryProvider.getIfAvailable(any(Supplier.class))).thenReturn(categoryRepository);
        when(transactionRepositoryProvider.getIfAvailable(any(Supplier.class))).thenReturn(transactionRepository);
        lenient().when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));
        lenient().when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));
        lenient().when(billRepository.save(any(Bill.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TransactionService transactionService = new TransactionService(
                transactionRepositoryProvider,
                accountRepositoryProvider,
                categoryRepositoryProvider,
                new TransactionMapper()
        );
        service = new BillService(
                billRepositoryProvider,
                accountRepositoryProvider,
                categoryRepositoryProvider,
                transactionService,
                new BillMapper()
        );
    }

    @Test
    void shouldBlockPayingBillWhenAccountBalanceIsInsufficient() {
        Account account = new Account(USER_ID, "Carteira", AccountType.CHECKING_ACCOUNT, new BigDecimal("50.00"));
        Bill bill = new Bill(
                USER_ID,
                "Energia",
                new BigDecimal("120.00"),
                LocalDate.of(2026, 6, 25),
                CATEGORY_ID,
                null,
                BillStatus.PENDING,
                null,
                null
        );
        Category category = new Category(USER_ID, "Moradia", CategoryType.EXPENSE, "#111111", "home", false);

        when(billRepository.findByIdAndUserId(BILL_ID, USER_ID)).thenReturn(Optional.of(bill));
        when(categoryRepository.findByIdAndUserIdOrDefault(CATEGORY_ID, USER_ID)).thenReturn(Optional.of(category));
        when(accountRepository.findByIdAndUserId(ACCOUNT_ID, USER_ID)).thenReturn(Optional.of(account));

        assertThatThrownBy(() -> service.payBill(
                USER_ID,
                BILL_ID,
                new PayBillRequest(ACCOUNT_ID, LocalDate.of(2026, 6, 26))
        ))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Saldo insuficiente");

        assertThat(account.getCurrentBalance()).isEqualByComparingTo("50.00");
        assertThat(bill.getStatus()).isEqualTo(BillStatus.PENDING);
        verify(transactionRepository, never()).save(any(Transaction.class));
        verify(accountRepository, never()).save(any(Account.class));
        verify(billRepository, never()).save(any(Bill.class));
    }
}

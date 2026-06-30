package com.zorysa.finance.invoices.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest(properties = {
        "spring.flyway.enabled=true",
        "spring.jpa.hibernate.ddl-auto=validate"
})
@Testcontainers
class InvoiceSchemaPersistenceTest {

    @Container
    private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine")
            .withDatabaseName("finance")
            .withUsername("finance")
            .withPassword("finance");

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @DynamicPropertySource
    static void configureDataSource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Test
    void shouldCreateInvoiceStatusEnumValues() {
        assertThat(enumValues("invoice_status")).containsExactly("OPEN", "CLOSED", "PAID", "OVERDUE");
    }

    @Test
    void shouldCreateCreditCardInvoicesTableWithReferenceDatesTotalStatusAndPaymentColumns() {
        List<String> columns = columnsOf("credit_card_invoices");

        assertThat(columns).contains(
                "id",
                "user_id",
                "credit_card_id",
                "reference_month",
                "reference_year",
                "closing_date",
                "due_date",
                "total_amount",
                "status",
                "paid_at",
                "payment_account_id",
                "created_at",
                "updated_at"
        );
    }

    @Test
    void shouldCreateInvoiceIndexesForOwnerCardPeriodStatusAndDueDateFilters() {
        List<String> definitions = indexDefinitions();

        assertThat(definitions).anySatisfy(index -> assertThat(index).contains("(user_id, credit_card_id)"));
        assertThat(definitions).anySatisfy(index -> assertThat(index).contains("(user_id, reference_year, reference_month)"));
        assertThat(definitions).anySatisfy(index -> assertThat(index).contains("(user_id, status)"));
        assertThat(definitions).anySatisfy(index -> assertThat(index).contains("(user_id, due_date)"));
    }

    @Test
    void shouldCreateUniqueConstraintForCardReferenceMonthAndYear() {
        List<String> definitions = indexDefinitions();

        assertThat(definitions)
                .as("credit_card_invoices deve impedir fatura duplicada por cartao, mes e ano")
                .anySatisfy(index -> assertThat(index)
                        .contains("UNIQUE")
                        .contains("credit_card_id")
                        .contains("reference_month")
                        .contains("reference_year"));
    }

    @Test
    void shouldRequireNonNegativeTotalAmount() {
        List<String> checks = checksForColumn("credit_card_invoices", "total_amount");

        assertThat(checks)
                .as("credit_card_invoices.total_amount deve ter check constraint >= 0")
                .anySatisfy(check -> assertThat(check).contains(">="));
    }

    @Test
    void shouldRestrictReferenceMonthBetweenOneAndTwelve() {
        List<String> checks = checksForColumn("credit_card_invoices", "reference_month");

        assertThat(checks)
                .as("credit_card_invoices.reference_month deve limitar valores entre 1 e 12")
                .anySatisfy(check -> assertThat(check).contains("1").contains("12"));
    }

    private List<String> enumValues(String enumName) {
        return jdbcTemplate.queryForList("""
                select enumlabel
                from pg_enum
                join pg_type on pg_enum.enumtypid = pg_type.oid
                where pg_type.typname = ?
                order by enumsortorder
                """, String.class, enumName);
    }

    private List<String> columnsOf(String tableName) {
        return jdbcTemplate.queryForList("""
                select column_name
                from information_schema.columns
                where table_schema = 'public'
                  and table_name = ?
                order by ordinal_position
                """, String.class, tableName);
    }

    private List<String> indexDefinitions() {
        return jdbcTemplate.queryForList("""
                select indexdef
                from pg_indexes
                where schemaname = 'public'
                  and tablename = 'credit_card_invoices'
                order by indexname
                """, String.class);
    }

    private List<String> checksForColumn(String tableName, String columnName) {
        return jdbcTemplate.queryForList("""
                select cc.check_clause
                from information_schema.check_constraints cc
                join information_schema.constraint_column_usage ccu
                  on cc.constraint_schema = ccu.constraint_schema
                 and cc.constraint_name = ccu.constraint_name
                where ccu.table_schema = 'public'
                  and ccu.table_name = ?
                  and ccu.column_name = ?
                """, String.class, tableName, columnName);
    }
}

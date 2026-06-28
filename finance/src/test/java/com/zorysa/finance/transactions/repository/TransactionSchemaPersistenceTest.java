package com.zorysa.finance.transactions.repository;

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
class TransactionSchemaPersistenceTest {

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
    void shouldCreateTransactionTypeAndStatusEnumValues() {
        assertThat(enumValues("transaction_type")).containsExactly("INCOME", "EXPENSE");
        assertThat(enumValues("transaction_status")).containsExactly("PENDING", "PAID", "RECEIVED", "CANCELED");
    }

    @Test
    void shouldCreateTransactionsTableWithFinancialColumns() {
        List<String> columns = columnsOf("transactions");

        assertThat(columns).contains(
                "id",
                "user_id",
                "account_id",
                "category_id",
                "description",
                "type",
                "amount",
                "transaction_date",
                "status",
                "notes",
                "created_at",
                "updated_at"
        );
    }

    @Test
    void shouldCreateTransactionsIndexesForOwnerFilters() {
        List<String> definitions = indexDefinitions();

        assertThat(definitions).anySatisfy(index -> assertThat(index).contains("(user_id, transaction_date)"));
        assertThat(definitions).anySatisfy(index -> assertThat(index).contains("(user_id, category_id)"));
        assertThat(definitions).anySatisfy(index -> assertThat(index).contains("(user_id, account_id)"));
        assertThat(definitions).anySatisfy(index -> assertThat(index).contains("(user_id, status)"));
    }

    @Test
    void shouldRequirePositiveTransactionAmount() {
        List<String> checks = jdbcTemplate.queryForList("""
                select check_clause
                from information_schema.check_constraints
                where constraint_schema = 'public'
                  and check_clause ilike '%amount%'
                """, String.class);

        assertThat(checks)
                .as("transactions.amount deve ter check constraint > 0")
                .anySatisfy(check -> assertThat(check).contains(">"));
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
                  and tablename = 'transactions'
                order by indexname
                """, String.class);
    }
}

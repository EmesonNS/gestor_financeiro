package com.zorysa.finance.bills.repository;

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
class BillSchemaPersistenceTest {

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
    void shouldCreateBillStatusEnumValues() {
        assertThat(enumValues("bill_status")).containsExactly("PENDING", "PAID", "OVERDUE", "CANCELED");
    }

    @Test
    void shouldCreateBillsTableWithPaymentAndTransactionColumns() {
        List<String> columns = columnsOf("bills");

        assertThat(columns).contains(
                "id",
                "user_id",
                "account_id",
                "category_id",
                "transaction_id",
                "description",
                "amount",
                "due_date",
                "paid_at",
                "status",
                "recurrence_id",
                "created_at",
                "updated_at"
        );
    }

    @Test
    void shouldCreateBillsIndexesForOwnerFilters() {
        List<String> definitions = indexDefinitions();

        assertThat(definitions).anySatisfy(index -> assertThat(index).contains("(user_id, due_date)"));
        assertThat(definitions).anySatisfy(index -> assertThat(index).contains("(user_id, status)"));
        assertThat(definitions).anySatisfy(index -> assertThat(index).contains("(user_id, category_id)"));
    }

    @Test
    void shouldRequirePositiveBillAmount() {
        List<String> checks = jdbcTemplate.queryForList("""
                select cc.check_clause
                from information_schema.check_constraints cc
                join information_schema.constraint_column_usage ccu
                  on cc.constraint_schema = ccu.constraint_schema
                 and cc.constraint_name = ccu.constraint_name
                where ccu.table_schema = 'public'
                  and ccu.table_name = 'bills'
                  and ccu.column_name = 'amount'
                """, String.class);

        assertThat(checks)
                .as("bills.amount deve ter check constraint > 0")
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
                  and tablename = 'bills'
                order by indexname
                """, String.class);
    }
}

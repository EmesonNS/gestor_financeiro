package com.zorysa.finance.accounts.repository;

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
class AccountSchemaPersistenceTest {

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
    void shouldCreateAccountTypeEnumValues() {
        List<String> values = jdbcTemplate.queryForList("""
                select enumlabel
                from pg_enum
                join pg_type on pg_enum.enumtypid = pg_type.oid
                where pg_type.typname = 'account_type'
                order by enumsortorder
                """, String.class);

        assertThat(values).containsExactly(
                "CHECKING_ACCOUNT",
                "SAVINGS_ACCOUNT",
                "CASH_WALLET",
                "DIGITAL_ACCOUNT",
                "INVESTMENT",
                "MEAL_VOUCHER",
                "OTHER"
        );
    }

    @Test
    void shouldCreateAccountsTableWithOwnerBalancesAndArchiveColumns() {
        List<String> columns = columnsOf("accounts");

        assertThat(columns).contains(
                "id",
                "user_id",
                "name",
                "type",
                "initial_balance",
                "current_balance",
                "archived",
                "created_at",
                "updated_at"
        );
    }

    @Test
    void shouldCreateAccountsIndexesForOwnerArchiveAndTypeFilters() {
        List<String> definitions = jdbcTemplate.queryForList("""
                select indexdef
                from pg_indexes
                where schemaname = 'public'
                  and tablename = 'accounts'
                order by indexname
                """, String.class);

        assertThat(definitions).anySatisfy(index -> assertThat(index).contains("(user_id)"));
        assertThat(definitions).anySatisfy(index -> assertThat(index).contains("(user_id, archived)"));
        assertThat(definitions).anySatisfy(index -> assertThat(index).contains("(user_id, type)"));
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
}

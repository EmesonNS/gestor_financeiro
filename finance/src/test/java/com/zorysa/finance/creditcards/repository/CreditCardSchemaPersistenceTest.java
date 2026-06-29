package com.zorysa.finance.creditcards.repository;

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
class CreditCardSchemaPersistenceTest {

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
    void shouldCreateCreditCardsTableWithLimitClosingDueAndArchivedColumns() {
        List<String> columns = columnsOf("credit_cards");

        assertThat(columns).contains(
                "id",
                "user_id",
                "name",
                "limit_amount",
                "closing_day",
                "due_day",
                "archived",
                "created_at",
                "updated_at"
        );
    }

    @Test
    void shouldCreateCreditCardsIndexesForOwnerAndArchivedFilters() {
        List<String> definitions = indexDefinitions();

        assertThat(definitions).anySatisfy(index -> assertThat(index).contains("(user_id)"));
        assertThat(definitions).anySatisfy(index -> assertThat(index).contains("(user_id, archived)"));
    }

    @Test
    void shouldRequireNonNegativeLimitAmount() {
        List<String> checks = checksForColumn("credit_cards", "limit_amount");

        assertThat(checks)
                .as("credit_cards.limit_amount deve ter check constraint >= 0")
                .anySatisfy(check -> assertThat(check).contains(">="));
    }

    @Test
    void shouldRestrictClosingAndDueDaysBetweenOneAndThirtyOne() {
        assertThat(checksForColumn("credit_cards", "closing_day"))
                .as("credit_cards.closing_day deve limitar valores entre 1 e 31")
                .anySatisfy(check -> assertThat(check).contains("1").contains("31"));
        assertThat(checksForColumn("credit_cards", "due_day"))
                .as("credit_cards.due_day deve limitar valores entre 1 e 31")
                .anySatisfy(check -> assertThat(check).contains("1").contains("31"));
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
                  and tablename = 'credit_cards'
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

package com.zorysa.finance.budgets.repository;

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
class BudgetSchemaPersistenceTest {

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
    void shouldCreateBudgetsTableWithCategoryPeriodLimitAndTimestamps() {
        List<String> columns = columnsOf("budgets");

        assertThat(columns).contains(
                "id",
                "user_id",
                "category_id",
                "start_month",
                "start_year",
                "end_month",
                "end_year",
                "limit_amount",
                "created_at",
                "updated_at"
        );
    }

    @Test
    void shouldCreateBudgetsIndexesForOwnerPeriodAndCategoryFilters() {
        List<String> definitions = indexDefinitions();

        assertThat(definitions).anySatisfy(index -> assertThat(index).contains("(user_id, start_year, start_month)"));
        assertThat(definitions).anySatisfy(index -> assertThat(index).contains("(user_id, category_id)"));
        assertThat(definitions).anySatisfy(index -> assertThat(index)
                .contains("user_id")
                .contains("category_id")
                .contains("start_year")
                .contains("start_month")
                .contains("end_year")
                .contains("end_month"));
    }

    @Test
    void shouldNotKeepUniqueConstraintLimitedToSingleMonth() {
        List<String> definitions = indexDefinitions();

        assertThat(definitions)
                .as("budgets nao deve depender de unicidade simples por mes, pois agora periodos podem cobrir varios meses")
                .noneSatisfy(index -> assertThat(index)
                        .contains("UNIQUE")
                        .contains("category_id")
                        .contains("month")
                        .contains("year"));
    }

    @Test
    void shouldRequirePositiveLimitAmount() {
        List<String> checks = checksForColumn("budgets", "limit_amount");

        assertThat(checks)
                .as("budgets.limit_amount deve ter check constraint > 0")
                .anySatisfy(check -> assertThat(check).contains(">"));
    }

    @Test
    void shouldRestrictStartAndEndMonthsBetweenOneAndTwelve() {
        assertThat(checksForColumn("budgets", "start_month"))
                .as("budgets.start_month deve limitar valores entre 1 e 12")
                .anySatisfy(check -> assertThat(check).contains("1").contains("12"));
        assertThat(checksForColumn("budgets", "end_month"))
                .as("budgets.end_month deve limitar valores entre 1 e 12 quando informado")
                .anySatisfy(check -> assertThat(check).contains("1").contains("12"));
    }

    @Test
    void shouldRequireCompleteAndOrderedEndPeriodWhenBudgetIsNotOpenEnded() {
        List<String> checks = tableChecks("budgets");

        assertThat(checks)
                .as("fim do periodo deve ser completamente nulo ou completamente preenchido")
                .anySatisfy(check -> assertThat(check).contains("end_month").contains("end_year"));
        assertThat(checks)
                .as("fim do periodo deve ser igual ou posterior ao inicio")
                .anySatisfy(check -> assertThat(check)
                        .contains("start_year")
                        .contains("start_month")
                        .contains("end_year")
                        .contains("end_month"));
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
                  and tablename = 'budgets'
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

    private List<String> tableChecks(String tableName) {
        return jdbcTemplate.queryForList("""
                select cc.check_clause
                from information_schema.check_constraints cc
                join information_schema.table_constraints tc
                  on cc.constraint_schema = tc.constraint_schema
                 and cc.constraint_name = tc.constraint_name
                where tc.table_schema = 'public'
                  and tc.table_name = ?
                """, String.class, tableName);
    }
}

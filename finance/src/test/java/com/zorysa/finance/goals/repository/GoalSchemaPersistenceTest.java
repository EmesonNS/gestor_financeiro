package com.zorysa.finance.goals.repository;

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
class GoalSchemaPersistenceTest {

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
    void shouldCreateGoalStatusEnumValues() {
        List<String> values = jdbcTemplate.queryForList("""
                select enumlabel
                from pg_enum
                join pg_type on pg_enum.enumtypid = pg_type.oid
                where pg_type.typname = 'goal_status'
                order by enumsortorder
                """, String.class);

        assertThat(values).containsExactly("ACTIVE", "COMPLETED", "CANCELED");
    }

    @Test
    void shouldCreateGoalsTableWithAmountsDeadlineStatusAndDescription() {
        List<String> columns = columnsOf("goals");

        assertThat(columns).contains(
                "id",
                "user_id",
                "name",
                "target_amount",
                "current_amount",
                "deadline",
                "description",
                "status",
                "created_at",
                "updated_at"
        );
    }

    @Test
    void shouldCreateGoalsIndexesForOwnerAndStatusFilters() {
        List<String> definitions = indexDefinitions();

        assertThat(definitions).anySatisfy(index -> assertThat(index).contains("(user_id)"));
        assertThat(definitions).anySatisfy(index -> assertThat(index).contains("(user_id, status)"));
    }

    @Test
    void shouldRequirePositiveTargetAmount() {
        List<String> checks = checksForColumn("goals", "target_amount");

        assertThat(checks)
                .as("goals.target_amount deve ter check constraint > 0")
                .anySatisfy(check -> assertThat(check).contains(">"));
    }

    @Test
    void shouldRequireNonNegativeCurrentAmount() {
        List<String> checks = checksForColumn("goals", "current_amount");

        assertThat(checks)
                .as("goals.current_amount deve ter check constraint >= 0")
                .anySatisfy(check -> assertThat(check).contains(">="));
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
                  and tablename = 'goals'
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

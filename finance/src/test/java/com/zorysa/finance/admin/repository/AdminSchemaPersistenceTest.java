package com.zorysa.finance.admin.repository;

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
class AdminSchemaPersistenceTest {

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
    void shouldCreateAdministrativeUserColumns() {
        List<String> userColumns = columnsOf("users");

        assertThat(userColumns).contains(
                "role",
                "status",
                "approved_at",
                "rejected_at",
                "suspended_at",
                "deleted_at"
        );
    }

    @Test
    void shouldCreateUserStatusHistoryTableForAdministrativeAudit() {
        List<String> historyColumns = columnsOf("user_status_history");

        assertThat(historyColumns).contains(
                "id",
                "user_id",
                "admin_user_id",
                "previous_status",
                "new_status",
                "action",
                "reason",
                "created_at"
        );
    }

    @Test
    void shouldCreateUserStatusActionEnumValues() {
        List<String> values = jdbcTemplate.queryForList("""
                select enumlabel
                from pg_enum
                join pg_type on pg_enum.enumtypid = pg_type.oid
                where pg_type.typname = 'user_status_action'
                order by enumsortorder
                """, String.class);

        assertThat(values).containsExactly(
                "REGISTERED",
                "APPROVED",
                "REJECTED",
                "SUSPENDED",
                "REACTIVATED",
                "DELETED"
        );
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

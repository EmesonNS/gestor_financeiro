package com.zorysa.finance.installments.repository;

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
class InstallmentSchemaPersistenceTest {

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
    void shouldCreatePurchaseAndInstallmentStatusEnumValues() {
        assertThat(enumValues("purchase_status")).containsExactly("ACTIVE", "CANCELED");
        assertThat(enumValues("installment_status")).containsExactly("OPEN", "PAID", "CANCELED");
    }

    @Test
    void shouldCreateCreditCardPurchasesTableWithDocumentedColumns() {
        List<String> columns = columnsOf("credit_card_purchases");

        assertThat(columns).contains(
                "id",
                "user_id",
                "credit_card_id",
                "category_id",
                "description",
                "total_amount",
                "purchase_date",
                "installment_count",
                "status",
                "notes",
                "created_at",
                "updated_at"
        );
    }

    @Test
    void shouldCreateCreditCardInstallmentsTableWithDocumentedColumns() {
        List<String> columns = columnsOf("credit_card_installments");

        assertThat(columns).contains(
                "id",
                "user_id",
                "purchase_id",
                "invoice_id",
                "installment_number",
                "total_installments",
                "amount",
                "competence_month",
                "competence_year",
                "status",
                "created_at",
                "updated_at"
        );
    }

    @Test
    void shouldCreatePurchaseIndexesForOwnerCardDateAndCategoryFilters() {
        List<String> definitions = indexDefinitions("credit_card_purchases");

        assertThat(definitions).anySatisfy(index -> assertThat(index).contains("(user_id, credit_card_id)"));
        assertThat(definitions).anySatisfy(index -> assertThat(index).contains("(user_id, purchase_date)"));
        assertThat(definitions).anySatisfy(index -> assertThat(index).contains("(user_id, category_id)"));
    }

    @Test
    void shouldCreateInstallmentIndexesForOwnerInvoicePurchaseCompetenceAndStatusFilters() {
        List<String> definitions = indexDefinitions("credit_card_installments");

        assertThat(definitions).anySatisfy(index -> assertThat(index).contains("(user_id, invoice_id)"));
        assertThat(definitions).anySatisfy(index -> assertThat(index).contains("(user_id, purchase_id)"));
        assertThat(definitions).anySatisfy(index -> assertThat(index).contains("(user_id, competence_year, competence_month)"));
        assertThat(definitions).anySatisfy(index -> assertThat(index).contains("(user_id, status)"));
    }

    @Test
    void shouldRequirePositivePurchaseAmountAndInstallmentCount() {
        assertThat(checksForColumn("credit_card_purchases", "total_amount"))
                .as("credit_card_purchases.total_amount deve ter check constraint > 0")
                .anySatisfy(check -> assertThat(check).contains(">"));
        assertThat(checksForColumn("credit_card_purchases", "installment_count"))
                .as("credit_card_purchases.installment_count deve ter check constraint > 0")
                .anySatisfy(check -> assertThat(check).contains(">"));
    }

    @Test
    void shouldRequirePositiveInstallmentAmountAndNumbers() {
        assertThat(checksForColumn("credit_card_installments", "amount"))
                .as("credit_card_installments.amount deve ter check constraint > 0")
                .anySatisfy(check -> assertThat(check).contains(">"));
        assertThat(checksForColumn("credit_card_installments", "installment_number"))
                .as("credit_card_installments.installment_number deve ter check constraint > 0")
                .anySatisfy(check -> assertThat(check).contains(">"));
        assertThat(checksForColumn("credit_card_installments", "total_installments"))
                .as("credit_card_installments.total_installments deve ter check constraint > 0")
                .anySatisfy(check -> assertThat(check).contains(">"));
    }

    @Test
    void shouldRestrictCompetenceMonthBetweenOneAndTwelve() {
        assertThat(checksForColumn("credit_card_installments", "competence_month"))
                .as("credit_card_installments.competence_month deve limitar valores entre 1 e 12")
                .anySatisfy(check -> assertThat(check).contains("1").contains("12"));
    }

    @Test
    void shouldCreateUniqueConstraintForPurchaseAndInstallmentNumber() {
        List<String> definitions = indexDefinitions("credit_card_installments");

        assertThat(definitions)
                .as("credit_card_installments deve impedir parcela duplicada na mesma compra")
                .anySatisfy(index -> assertThat(index)
                        .contains("UNIQUE")
                        .contains("purchase_id")
                        .contains("installment_number"));
    }

    @Test
    void shouldEnsureInstallmentNumberDoesNotExceedTotalInstallments() {
        List<String> checks = checksForTable("credit_card_installments");

        assertThat(checks)
                .as("installment_number deve ser menor ou igual a total_installments")
                .anySatisfy(check -> assertThat(check).contains("installment_number").contains("total_installments"));
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

    private List<String> indexDefinitions(String tableName) {
        return jdbcTemplate.queryForList("""
                select indexdef
                from pg_indexes
                where schemaname = 'public'
                  and tablename = ?
                order by indexname
                """, String.class, tableName);
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

    private List<String> checksForTable(String tableName) {
        return jdbcTemplate.queryForList("""
                select cc.check_clause
                from information_schema.check_constraints cc
                join information_schema.constraint_table_usage ctu
                  on cc.constraint_schema = ctu.constraint_schema
                 and cc.constraint_name = ctu.constraint_name
                where ctu.table_schema = 'public'
                  and ctu.table_name = ?
                """, String.class, tableName);
    }
}

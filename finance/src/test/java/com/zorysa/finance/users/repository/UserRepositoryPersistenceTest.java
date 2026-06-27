package com.zorysa.finance.users.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.zorysa.finance.users.entity.User;
import com.zorysa.finance.users.entity.UserRole;
import com.zorysa.finance.users.entity.UserStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
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
class UserRepositoryPersistenceTest {

    @Container
    private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine")
            .withDatabaseName("finance")
            .withUsername("finance")
            .withPassword("finance");

    @Autowired
    private UserRepository userRepository;

    @DynamicPropertySource
    static void configureDataSource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Test
    void shouldPersistUserWithPostgresqlNativeEnums() {
        User user = new User("Maria", "maria@email.com", "stored-password-hash");
        user.markPendingApproval();

        User savedUser = userRepository.saveAndFlush(user);

        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getRole()).isEqualTo(UserRole.USER);
        assertThat(savedUser.getStatus()).isEqualTo(UserStatus.PENDING_APPROVAL);
    }
}

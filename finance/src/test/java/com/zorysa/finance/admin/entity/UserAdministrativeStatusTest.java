package com.zorysa.finance.admin.entity;

import static org.assertj.core.api.Assertions.assertThat;

import com.zorysa.finance.users.entity.User;
import com.zorysa.finance.users.entity.UserRole;
import com.zorysa.finance.users.entity.UserStatus;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.util.ReflectionUtils;

class UserAdministrativeStatusTest {

    private static final Instant NOW = Instant.parse("2026-06-27T12:00:00Z");

    @Test
    void shouldCreatePublicRegistrationAsUserPendingApproval() {
        User user = new User("Maria", "maria@email.com", "stored-password-hash");

        assertThat(user.getRole()).isEqualTo(UserRole.USER);
        assertThat(user.getStatus()).isEqualTo(UserStatus.PENDING_APPROVAL);
        assertThat(user.isActive()).isTrue();
    }

    @Test
    void shouldApprovePendingUserAndRecordApprovalTimestamp() {
        User user = pendingUser();

        invokeAdministrativeTransition(user, "approve", NOW);

        assertThat(user.getStatus()).isEqualTo(UserStatus.APPROVED);
        assertThat(readInstant(user, "approvedAt")).isEqualTo(NOW);
        assertThat(user.isActive()).isTrue();
    }

    @Test
    void shouldRejectPendingUserAndRecordRejectionTimestamp() {
        User user = pendingUser();

        invokeAdministrativeTransition(user, "reject", NOW);

        assertThat(user.getStatus()).isEqualTo(UserStatus.REJECTED);
        assertThat(readInstant(user, "rejectedAt")).isEqualTo(NOW);
        assertThat(user.isActive()).isTrue();
    }

    @Test
    void shouldSuspendApprovedUserAndRecordSuspensionTimestamp() {
        User user = pendingUser();
        invokeAdministrativeTransition(user, "approve", NOW);

        invokeAdministrativeTransition(user, "suspend", NOW.plusSeconds(60));

        assertThat(user.getStatus()).isEqualTo(UserStatus.SUSPENDED);
        assertThat(readInstant(user, "suspendedAt")).isEqualTo(NOW.plusSeconds(60));
        assertThat(user.isActive()).isTrue();
    }

    @Test
    void shouldReactivateSuspendedOrRejectedUserAsApproved() {
        User user = pendingUser();
        invokeAdministrativeTransition(user, "reject", NOW);

        invokeAdministrativeTransition(user, "reactivate", NOW.plusSeconds(60));

        assertThat(user.getStatus()).isEqualTo(UserStatus.APPROVED);
        assertThat(readInstant(user, "approvedAt")).isEqualTo(NOW.plusSeconds(60));
        assertThat(user.isActive()).isTrue();
    }

    @Test
    void shouldMarkUserAsDeletedAndInactive() {
        User user = pendingUser();
        invokeAdministrativeTransition(user, "approve", NOW);

        invokeAdministrativeTransition(user, "delete", NOW.plusSeconds(60));

        assertThat(user.getStatus()).isEqualTo(UserStatus.DELETED);
        assertThat(readInstant(user, "deletedAt")).isEqualTo(NOW.plusSeconds(60));
        assertThat(user.isActive()).isFalse();
    }

    private User pendingUser() {
        User user = new User("Maria", "maria@email.com", "stored-password-hash");
        ReflectionTestUtils.setField(user, "status", UserStatus.PENDING_APPROVAL);
        return user;
    }

    private void invokeAdministrativeTransition(User user, String methodName, Instant instant) {
        Method method = ReflectionUtils.findMethod(User.class, methodName, Instant.class);
        assertThat(method)
                .as("User deve expor transicao administrativa " + methodName + "(Instant)")
                .isNotNull();
        ReflectionUtils.makeAccessible(method);
        ReflectionUtils.invokeMethod(method, user, instant);
    }

    private Instant readInstant(User user, String fieldName) {
        Field field = ReflectionUtils.findField(User.class, fieldName);
        assertThat(field)
                .as("User deve registrar timestamp administrativo " + fieldName)
                .isNotNull();
        ReflectionUtils.makeAccessible(field);
        return (Instant) ReflectionUtils.getField(field, user);
    }
}

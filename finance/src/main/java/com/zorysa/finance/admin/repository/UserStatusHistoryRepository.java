package com.zorysa.finance.admin.repository;

import com.zorysa.finance.admin.entity.UserStatusHistory;
import com.zorysa.finance.users.entity.User;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserStatusHistoryRepository extends JpaRepository<UserStatusHistory, UUID> {

    List<UserStatusHistory> findByUserOrderByCreatedAtDesc(User user);
}

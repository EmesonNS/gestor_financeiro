package com.zorysa.finance.users.mapper;

import com.zorysa.finance.users.dto.UserResponse;
import com.zorysa.finance.users.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserResponse toResponse(User user) {
        return new UserResponse(user.getId(), user.getName(), user.getEmail(), user.getCreatedAt());
    }
}

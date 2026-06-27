package com.zorysa.finance.users.service;

import com.zorysa.finance.shared.exception.ConflictException;
import com.zorysa.finance.users.dto.UserResponse;
import com.zorysa.finance.users.entity.User;
import com.zorysa.finance.users.mapper.UserMapper;
import com.zorysa.finance.users.repository.UserRepository;
import java.util.Locale;
import java.util.Optional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, UserMapper userMapper, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UserResponse createUserResponse(String name, String email, String password) {
        return userMapper.toResponse(createUser(name, email, password));
    }

    @Transactional
    public User createUser(String name, String email, String password) {
        String normalizedEmail = normalizeEmail(email);
        if (userRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new ConflictException("E-mail ja cadastrado");
        }
        User user = new User(name.trim(), normalizedEmail, passwordEncoder.encode(password));
        user.markPendingApproval();
        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public Optional<User> findActiveByEmail(String email) {
        return userRepository.findByEmailIgnoreCase(normalizeEmail(email))
                .filter(User::isActive);
    }

    @Transactional
    public void updatePassword(User user, String newPassword) {
        user.updatePasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}

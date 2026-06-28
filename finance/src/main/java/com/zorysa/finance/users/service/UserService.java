package com.zorysa.finance.users.service;

import com.zorysa.finance.shared.exception.ConflictException;
import com.zorysa.finance.shared.exception.UnauthorizedException;
import com.zorysa.finance.users.dto.ChangePasswordRequest;
import com.zorysa.finance.users.dto.UpdateUserProfileRequest;
import com.zorysa.finance.users.dto.UserResponse;
import com.zorysa.finance.users.entity.User;
import com.zorysa.finance.users.mapper.UserMapper;
import com.zorysa.finance.users.repository.UserRepository;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
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


    @Transactional(readOnly = true)
    public UserResponse getAuthenticatedProfile(UUID authenticatedUserId) {
        return userMapper.toResponse(findActiveById(authenticatedUserId));
    }

    @Transactional
    public UserResponse updateAuthenticatedProfile(UUID authenticatedUserId, UpdateUserProfileRequest request) {
        User user = findActiveById(authenticatedUserId);
        user.updateName(request.name().trim());
        return userMapper.toResponse(userRepository.save(user));
    }

    @Transactional
    public void changeAuthenticatedPassword(UUID authenticatedUserId, ChangePasswordRequest request) {
        User user = findActiveById(authenticatedUserId);
        if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            throw new UnauthorizedException("Senha atual invalida");
        }
        updatePassword(user, request.newPassword());
    }

    private User findActiveById(UUID userId) {
        return userRepository.findById(userId)
                .filter(User::isActive)
                .orElseThrow(() -> new UnauthorizedException("Usuario nao autenticado"));
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

package com.zorysa.finance.goals.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.zorysa.finance.auth.repository.PasswordResetTokenRepository;
import com.zorysa.finance.auth.repository.RefreshTokenRepository;
import com.zorysa.finance.users.repository.UserRepository;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class GoalApiSecurityIntegrationTest {

    private static final UUID GOAL_ID = UUID.fromString("12121212-1212-1212-1212-121212121212");

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private RefreshTokenRepository refreshTokenRepository;

    @MockitoBean
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Test
    void shouldRequireAuthenticationForListingGoals() throws Exception {
        mockMvc.perform(get("/api/goals")
                        .param("page", "0")
                        .param("size", "20")
                        .param("sort", "deadline,asc")
                        .param("status", "ACTIVE"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRequireAuthenticationForCreatingGoal() throws Exception {
        mockMvc.perform(post("/api/goals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Reserva",
                                  "targetAmount": 10000.00,
                                  "currentAmount": 1000.00,
                                  "deadline": "2026-12-31",
                                  "description": "Reserva de emergencia"
                                }
                                """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRequireAuthenticationForReadingUpdatingDeletingAndProgress() throws Exception {
        mockMvc.perform(get("/api/goals/{id}", GOAL_ID))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(put("/api/goals/{id}", GOAL_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Reserva",
                                  "targetAmount": 12000.00,
                                  "currentAmount": 2500.00,
                                  "deadline": "2026-12-31",
                                  "description": "Reserva atualizada"
                                }
                                """))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(patch("/api/goals/{id}/progress", GOAL_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "currentAmount": 2500.00
                                }
                                """))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(delete("/api/goals/{id}", GOAL_ID))
                .andExpect(status().isUnauthorized());
    }
}

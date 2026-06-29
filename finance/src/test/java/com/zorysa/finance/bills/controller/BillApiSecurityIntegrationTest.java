package com.zorysa.finance.bills.controller;

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
class BillApiSecurityIntegrationTest {

    private static final UUID BILL_ID = UUID.fromString("77777777-7777-7777-7777-777777777777");
    private static final UUID CATEGORY_ID = UUID.fromString("55555555-5555-5555-5555-555555555555");
    private static final UUID ACCOUNT_ID = UUID.fromString("66666666-6666-6666-6666-666666666666");

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private RefreshTokenRepository refreshTokenRepository;

    @MockitoBean
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Test
    void shouldRequireAuthenticationForListingBills() throws Exception {
        mockMvc.perform(get("/api/bills")
                        .param("page", "0")
                        .param("size", "20")
                        .param("sort", "dueDate,asc")
                        .param("status", "PENDING")
                        .param("overdue", "false"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRequireAuthenticationForCreatingBill() throws Exception {
        mockMvc.perform(post("/api/bills")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRequireAuthenticationForReadingUpdatingDeletingAndPayingBill() throws Exception {
        mockMvc.perform(get("/api/bills/{id}", BILL_ID))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(put("/api/bills/{id}", BILL_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody()))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(patch("/api/bills/{id}/pay", BILL_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "accountId": "%s",
                                  "paidAt": "2026-06-20"
                                }
                                """.formatted(ACCOUNT_ID)))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(delete("/api/bills/{id}", BILL_ID))
                .andExpect(status().isUnauthorized());
    }

    private String requestBody() {
        return """
                {
                  "description": "Energia",
                  "amount": 180.00,
                  "dueDate": "2026-06-25",
                  "categoryId": "%s",
                  "accountId": "%s",
                  "status": "PENDING"
                }
                """.formatted(CATEGORY_ID, ACCOUNT_ID);
    }
}

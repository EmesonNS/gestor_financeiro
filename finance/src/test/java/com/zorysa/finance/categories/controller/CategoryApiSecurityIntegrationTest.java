package com.zorysa.finance.categories.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
class CategoryApiSecurityIntegrationTest {

    private static final UUID CATEGORY_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private RefreshTokenRepository refreshTokenRepository;

    @MockitoBean
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Test
    void shouldRequireAuthenticationForListingCategories() throws Exception {
        mockMvc.perform(get("/api/categories")
                        .param("page", "0")
                        .param("size", "20")
                        .param("sort", "name,asc")
                        .param("type", "EXPENSE"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRequireAuthenticationForCountingCustomCategories() throws Exception {
        mockMvc.perform(get("/api/categories/custom/count"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRequireAuthenticationForCountingCategoriesByType() throws Exception {
        mockMvc.perform(get("/api/categories/type-counts"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRequireAuthenticationForCreatingCategory() throws Exception {
        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Alimentacao",
                                  "type": "EXPENSE",
                                  "color": "#16a34a",
                                  "icon": "utensils"
                                }
                                """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRequireAuthenticationForReadingUpdatingAndDeletingCategory() throws Exception {
        mockMvc.perform(get("/api/categories/{id}", CATEGORY_ID))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(put("/api/categories/{id}", CATEGORY_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Mercado",
                                  "type": "EXPENSE",
                                  "color": "#15803d",
                                  "icon": "shopping-cart"
                                }
                                """))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(delete("/api/categories/{id}", CATEGORY_ID))
                .andExpect(status().isUnauthorized());
    }
}

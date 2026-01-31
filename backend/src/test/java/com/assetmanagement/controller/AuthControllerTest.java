package com.assetmanagement.controller;

import com.assetmanagement.dto.request.ChangePasswordRequest;
import com.assetmanagement.dto.request.LoginRequest;
import com.assetmanagement.dto.response.LoginResponse;
import com.assetmanagement.exception.ApplicationException;
import com.assetmanagement.security.CustomUserDetailsService;
import com.assetmanagement.security.JwtUtil;
import com.assetmanagement.service.AuthService;
import tools.jackson.databind.json.JsonMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@EnableMethodSecurity
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private JsonMapper jsonMapper;

    @MockitoBean
    private AuthService authService;

    @BeforeEach
    void setUp() {
        jsonMapper = JsonMapper.builder().build();
    }

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @Nested
    @DisplayName("POST /api/v1/auth/login")
    class LoginTests {

        @Test
        @DisplayName("Should return 200 OK with token on successful login")
        @WithMockUser
        void shouldReturnTokenOnSuccessfulLogin() throws Exception {
            LoginRequest request = new LoginRequest();
            request.setEmail("user@example.com");
            request.setPassword("password123");

            LoginResponse response = new LoginResponse("jwt.token.here");
            when(authService.login(any(LoginRequest.class))).thenReturn(response);

            mockMvc.perform(post("/api/v1/auth/login")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.token").value("jwt.token.here"));

            verify(authService).login(any(LoginRequest.class));
        }

        @Test
        @DisplayName("Should return 401 Unauthorized on invalid credentials")
        @WithMockUser
        void shouldReturn401OnInvalidCredentials() throws Exception {
            LoginRequest request = new LoginRequest();
            request.setEmail("user@example.com");
            request.setPassword("wrongpassword");

            when(authService.login(any(LoginRequest.class)))
                    .thenThrow(new ApplicationException(HttpStatus.UNAUTHORIZED, "Nieprawidłowy email lub hasło"));

            mockMvc.perform(post("/api/v1/auth/login")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Should return 400 Bad Request when email is missing")
        @WithMockUser
        void shouldReturn400WhenEmailMissing() throws Exception {
            LoginRequest request = new LoginRequest();
            request.setPassword("password123");

            mockMvc.perform(post("/api/v1/auth/login")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());

            verify(authService, never()).login(any());
        }

        @Test
        @DisplayName("Should return 400 Bad Request when password is missing")
        @WithMockUser
        void shouldReturn400WhenPasswordMissing() throws Exception {
            LoginRequest request = new LoginRequest();
            request.setEmail("user@example.com");

            mockMvc.perform(post("/api/v1/auth/login")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());

            verify(authService, never()).login(any());
        }
    }

    @Nested
    @DisplayName("POST /api/v1/auth/change-password")
    class ChangePasswordTests {

        @Test
        @DisplayName("Should return 204 No Content on successful password change")
        @WithMockUser
        void shouldReturn204OnSuccessfulPasswordChange() throws Exception {
            ChangePasswordRequest request = new ChangePasswordRequest();
            request.setEmail("user@example.com");
            request.setCurrentPassword("oldPassword");
            request.setNewPassword("newPassword123");

            doNothing().when(authService).changePassword(any(ChangePasswordRequest.class));

            mockMvc.perform(post("/api/v1/auth/change-password")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonMapper.writeValueAsString(request)))
                    .andExpect(status().isNoContent());

            verify(authService).changePassword(any(ChangePasswordRequest.class));
        }

        @Test
        @DisplayName("Should return 400 Bad Request on invalid current password")
        @WithMockUser
        void shouldReturn400OnInvalidCurrentPassword() throws Exception {
            ChangePasswordRequest request = new ChangePasswordRequest();
            request.setEmail("user@example.com");
            request.setCurrentPassword("wrongPassword");
            request.setNewPassword("newPassword123");

            doThrow(new ApplicationException(HttpStatus.BAD_REQUEST, "Nieprawidłowy email lub hasło"))
                    .when(authService).changePassword(any(ChangePasswordRequest.class));

            mockMvc.perform(post("/api/v1/auth/change-password")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 400 Bad Request when new password is missing")
        @WithMockUser
        void shouldReturn400WhenNewPasswordMissing() throws Exception {
            ChangePasswordRequest request = new ChangePasswordRequest();
            request.setEmail("user@example.com");
            request.setCurrentPassword("oldPassword");

            mockMvc.perform(post("/api/v1/auth/change-password")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());

            verify(authService, never()).changePassword(any());
        }
    }
}

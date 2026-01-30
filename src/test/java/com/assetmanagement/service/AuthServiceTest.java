package com.assetmanagement.service;

import com.assetmanagement.dto.request.ChangePasswordRequest;
import com.assetmanagement.dto.request.LoginRequest;
import com.assetmanagement.dto.response.LoginResponse;
import com.assetmanagement.exception.ApplicationException;
import com.assetmanagement.model.Employee;
import com.assetmanagement.model.Role;
import com.assetmanagement.repository.EmployeeRepository;
import com.assetmanagement.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    private Employee testEmployee;
    private LoginRequest loginRequest;
    private ChangePasswordRequest changePasswordRequest;

    @BeforeEach
    void setUp() {
        testEmployee = new Employee();
        testEmployee.setId(1L);
        testEmployee.setFullName("Jan Kowalski");
        testEmployee.setEmail("jan.kowalski@example.com");
        testEmployee.setPassword("encodedPassword123");
        testEmployee.setRole(Role.EMPLOYEE);

        loginRequest = new LoginRequest();
        loginRequest.setEmail("jan.kowalski@example.com");
        loginRequest.setPassword("rawPassword123");

        changePasswordRequest = new ChangePasswordRequest();
        changePasswordRequest.setEmail("jan.kowalski@example.com");
        changePasswordRequest.setCurrentPassword("currentPassword");
        changePasswordRequest.setNewPassword("newPassword123");
    }

    @Nested
    @DisplayName("Login Tests")
    class LoginTests {

        @Test
        @DisplayName("Should login successfully and return JWT token")
        void shouldLoginSuccessfullyAndReturnJwtToken() {
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenReturn(null); // Authentication doesn't throw exception
            when(employeeRepository.findByEmail(loginRequest.getEmail()))
                    .thenReturn(Optional.of(testEmployee));
            when(jwtUtil.generateToken(testEmployee.getEmail(), testEmployee.getRole().name()))
                    .thenReturn("generated.jwt.token");

            LoginResponse response = authService.login(loginRequest);

            assertNotNull(response);
            assertEquals("generated.jwt.token", response.getToken());
            verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
            verify(jwtUtil).generateToken(testEmployee.getEmail(), testEmployee.getRole().name());
        }

        @Test
        @DisplayName("Should login admin user successfully")
        void shouldLoginAdminUserSuccessfully() {
            testEmployee.setRole(Role.ADMIN);
            when(authenticationManager.authenticate(any())).thenReturn(null);
            when(employeeRepository.findByEmail(loginRequest.getEmail()))
                    .thenReturn(Optional.of(testEmployee));
            when(jwtUtil.generateToken(testEmployee.getEmail(), "ADMIN"))
                    .thenReturn("admin.jwt.token");

            LoginResponse response = authService.login(loginRequest);

            assertEquals("admin.jwt.token", response.getToken());
            verify(jwtUtil).generateToken(testEmployee.getEmail(), "ADMIN");
        }

        @Test
        @DisplayName("Should throw exception when authentication fails")
        void shouldThrowExceptionWhenAuthenticationFails() {
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenThrow(new BadCredentialsException("Bad credentials"));

            ApplicationException exception = assertThrows(ApplicationException.class,
                    () -> authService.login(loginRequest));

            assertEquals(HttpStatus.UNAUTHORIZED, exception.getHttpStatus());
            assertEquals("Nieprawidłowy email lub hasło", exception.getMessage());
            verify(employeeRepository, never()).findByEmail(anyString());
            verify(jwtUtil, never()).generateToken(anyString(), anyString());
        }

        @Test
        @DisplayName("Should throw exception when employee not found after authentication")
        void shouldThrowExceptionWhenEmployeeNotFoundAfterAuthentication() {
            when(authenticationManager.authenticate(any())).thenReturn(null);
            when(employeeRepository.findByEmail(loginRequest.getEmail()))
                    .thenReturn(Optional.empty());

            ApplicationException exception = assertThrows(ApplicationException.class,
                    () -> authService.login(loginRequest));

            assertEquals(HttpStatus.UNAUTHORIZED, exception.getHttpStatus());
            verify(jwtUtil, never()).generateToken(anyString(), anyString());
        }

        @Test
        @DisplayName("Should authenticate with correct credentials")
        void shouldAuthenticateWithCorrectCredentials() {
            when(authenticationManager.authenticate(any())).thenReturn(null);
            when(employeeRepository.findByEmail(loginRequest.getEmail()))
                    .thenReturn(Optional.of(testEmployee));
            when(jwtUtil.generateToken(anyString(), anyString())).thenReturn("token");

            authService.login(loginRequest);

            verify(authenticationManager).authenticate(
                    argThat(auth -> {
                        UsernamePasswordAuthenticationToken token = (UsernamePasswordAuthenticationToken) auth;
                        return token.getPrincipal().equals(loginRequest.getEmail()) &&
                                token.getCredentials().equals(loginRequest.getPassword());
                    })
            );
        }
    }

    @Nested
    @DisplayName("Change Password Tests")
    class ChangePasswordTests {

        @Test
        @DisplayName("Should change password successfully")
        void shouldChangePasswordSuccessfully() {
            when(employeeRepository.findByEmail(changePasswordRequest.getEmail()))
                    .thenReturn(Optional.of(testEmployee));
            when(passwordEncoder.matches(changePasswordRequest.getCurrentPassword(), testEmployee.getPassword()))
                    .thenReturn(true);
            when(passwordEncoder.encode(changePasswordRequest.getNewPassword()))
                    .thenReturn("encodedNewPassword");

            assertDoesNotThrow(() -> authService.changePassword(changePasswordRequest));

            verify(passwordEncoder).encode(changePasswordRequest.getNewPassword());
            verify(employeeRepository).save(testEmployee);
            assertEquals("encodedNewPassword", testEmployee.getPassword());
        }

        @Test
        @DisplayName("Should throw exception when employee not found")
        void shouldThrowExceptionWhenEmployeeNotFoundForPasswordChange() {
            when(employeeRepository.findByEmail(changePasswordRequest.getEmail()))
                    .thenReturn(Optional.empty());

            ApplicationException exception = assertThrows(ApplicationException.class,
                    () -> authService.changePassword(changePasswordRequest));

            assertEquals(HttpStatus.BAD_REQUEST, exception.getHttpStatus());
            verify(passwordEncoder, never()).matches(anyString(), anyString());
            verify(employeeRepository, never()).save(any(Employee.class));
        }

        @Test
        @DisplayName("Should throw exception when current password is incorrect")
        void shouldThrowExceptionWhenCurrentPasswordIsIncorrect() {
            when(employeeRepository.findByEmail(changePasswordRequest.getEmail()))
                    .thenReturn(Optional.of(testEmployee));
            when(passwordEncoder.matches(changePasswordRequest.getCurrentPassword(), testEmployee.getPassword()))
                    .thenReturn(false);

            ApplicationException exception = assertThrows(ApplicationException.class,
                    () -> authService.changePassword(changePasswordRequest));

            assertEquals(HttpStatus.BAD_REQUEST, exception.getHttpStatus());
            assertEquals("Nieprawidłowy email lub hasło", exception.getMessage());
            verify(passwordEncoder, never()).encode(anyString());
            verify(employeeRepository, never()).save(any(Employee.class));
        }

        @Test
        @DisplayName("Should encode new password before saving")
        void shouldEncodeNewPasswordBeforeSaving() {
            when(employeeRepository.findByEmail(changePasswordRequest.getEmail()))
                    .thenReturn(Optional.of(testEmployee));
            when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
            when(passwordEncoder.encode("newPassword123")).thenReturn("encodedNewPassword123");

            authService.changePassword(changePasswordRequest);

            verify(passwordEncoder).encode("newPassword123");
            assertEquals("encodedNewPassword123", testEmployee.getPassword());
        }
    }

    @Nested
    @DisplayName("Login Request Validation Tests")
    class LoginRequestValidationTests {

        @Test
        @DisplayName("Should handle login with different email formats")
        void shouldHandleLoginWithDifferentEmailFormats() {
            loginRequest.setEmail("ADMIN@COMPANY.COM");
            when(authenticationManager.authenticate(any())).thenReturn(null);
            when(employeeRepository.findByEmail("ADMIN@COMPANY.COM"))
                    .thenReturn(Optional.of(testEmployee));
            when(jwtUtil.generateToken(anyString(), anyString())).thenReturn("token");

            LoginResponse response = authService.login(loginRequest);

            assertNotNull(response);
            verify(employeeRepository).findByEmail("ADMIN@COMPANY.COM");
        }
    }
}

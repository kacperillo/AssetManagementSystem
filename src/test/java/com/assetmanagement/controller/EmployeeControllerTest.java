package com.assetmanagement.controller;

import com.assetmanagement.dto.request.CreateEmployeeRequest;
import com.assetmanagement.dto.response.EmployeeResponse;
import com.assetmanagement.exception.ApplicationException;
import com.assetmanagement.model.Role;
import com.assetmanagement.security.CustomUserDetailsService;
import com.assetmanagement.security.JwtUtil;
import com.assetmanagement.service.EmployeeService;
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

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EmployeeController.class)
@EnableMethodSecurity
class EmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private JsonMapper jsonMapper;

    @MockitoBean
    private EmployeeService employeeService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @BeforeEach
    void setUp() {
        jsonMapper = JsonMapper.builder().build();
    }

    @Nested
    @DisplayName("POST /api/v1/admin/employees")
    class CreateEmployeeTests {

        @Test
        @DisplayName("Should create employee and return 201 Created")
        @WithMockUser(roles = "ADMIN")
        void shouldCreateEmployeeAndReturn201() throws Exception {
            CreateEmployeeRequest request = new CreateEmployeeRequest();
            request.setFullName("Jan Kowalski");
            request.setEmail("jan.kowalski@example.com");
            request.setPassword("password123");
            request.setRole(Role.EMPLOYEE);
            request.setHiredFrom(LocalDate.of(2024, 1, 15));

            EmployeeResponse response = new EmployeeResponse();
            response.setId(1L);
            response.setFullName("Jan Kowalski");
            response.setEmail("jan.kowalski@example.com");
            response.setRole(Role.EMPLOYEE);
            response.setHiredFrom(LocalDate.of(2024, 1, 15));

            when(employeeService.createEmployee(any(CreateEmployeeRequest.class))).thenReturn(response);

            mockMvc.perform(post("/api/v1/admin/employees")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.fullName").value("Jan Kowalski"))
                    .andExpect(jsonPath("$.email").value("jan.kowalski@example.com"));

            verify(employeeService).createEmployee(any(CreateEmployeeRequest.class));
        }

        @Test
        @DisplayName("Should return 400 when email already exists")
        @WithMockUser(roles = "ADMIN")
        void shouldReturn400WhenEmailExists() throws Exception {
            CreateEmployeeRequest request = new CreateEmployeeRequest();
            request.setFullName("Jan Kowalski");
            request.setEmail("existing@example.com");
            request.setPassword("password123");
            request.setRole(Role.EMPLOYEE);
            request.setHiredFrom(LocalDate.of(2024, 1, 15));

            when(employeeService.createEmployee(any(CreateEmployeeRequest.class)))
                    .thenThrow(new ApplicationException(HttpStatus.BAD_REQUEST, "Ten adres email jest już używany"));

            mockMvc.perform(post("/api/v1/admin/employees")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 403 Forbidden for non-admin user")
        @WithMockUser(roles = "EMPLOYEE")
        void shouldReturn403ForNonAdmin() throws Exception {
            CreateEmployeeRequest request = new CreateEmployeeRequest();
            request.setFullName("Jan Kowalski");
            request.setEmail("jan.kowalski@example.com");
            request.setPassword("password123");
            request.setRole(Role.EMPLOYEE);
            request.setHiredFrom(LocalDate.of(2024, 1, 15));

            mockMvc.perform(post("/api/v1/admin/employees")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden());

            verify(employeeService, never()).createEmployee(any());
        }

        @Test
        @DisplayName("Should return 400 when required fields are missing")
        @WithMockUser(roles = "ADMIN")
        void shouldReturn400WhenRequiredFieldsMissing() throws Exception {
            CreateEmployeeRequest request = new CreateEmployeeRequest();
            request.setFullName("Jan Kowalski");
            // Missing email, password, role, hiredFrom

            mockMvc.perform(post("/api/v1/admin/employees")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());

            verify(employeeService, never()).createEmployee(any());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/admin/employees")
    class GetAllEmployeesTests {

        @Test
        @DisplayName("Should return all employees for admin")
        @WithMockUser(roles = "ADMIN")
        void shouldReturnAllEmployeesForAdmin() throws Exception {
            EmployeeResponse employee1 = new EmployeeResponse();
            employee1.setId(1L);
            employee1.setFullName("Jan Kowalski");
            employee1.setEmail("jan@example.com");
            employee1.setRole(Role.EMPLOYEE);

            EmployeeResponse employee2 = new EmployeeResponse();
            employee2.setId(2L);
            employee2.setFullName("Anna Nowak");
            employee2.setEmail("anna@example.com");
            employee2.setRole(Role.ADMIN);

            when(employeeService.getAllEmployees()).thenReturn(List.of(employee1, employee2));

            mockMvc.perform(get("/api/v1/admin/employees"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].id").value(1))
                    .andExpect(jsonPath("$[0].fullName").value("Jan Kowalski"))
                    .andExpect(jsonPath("$[1].id").value(2))
                    .andExpect(jsonPath("$[1].fullName").value("Anna Nowak"));

            verify(employeeService).getAllEmployees();
        }

        @Test
        @DisplayName("Should return empty list when no employees")
        @WithMockUser(roles = "ADMIN")
        void shouldReturnEmptyListWhenNoEmployees() throws Exception {
            when(employeeService.getAllEmployees()).thenReturn(List.of());

            mockMvc.perform(get("/api/v1/admin/employees"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$").isEmpty());
        }

        @Test
        @DisplayName("Should return 403 for non-admin user")
        @WithMockUser(roles = "EMPLOYEE")
        void shouldReturn403ForNonAdmin() throws Exception {
            mockMvc.perform(get("/api/v1/admin/employees"))
                    .andExpect(status().isForbidden());

            verify(employeeService, never()).getAllEmployees();
        }
    }

    @Nested
    @DisplayName("GET /api/v1/admin/employees/{id}")
    class GetEmployeeByIdTests {

        @Test
        @DisplayName("Should return employee by id")
        @WithMockUser(roles = "ADMIN")
        void shouldReturnEmployeeById() throws Exception {
            EmployeeResponse response = new EmployeeResponse();
            response.setId(1L);
            response.setFullName("Jan Kowalski");
            response.setEmail("jan@example.com");
            response.setRole(Role.EMPLOYEE);
            response.setHiredFrom(LocalDate.of(2024, 1, 15));

            when(employeeService.getEmployeeById(1L)).thenReturn(response);

            mockMvc.perform(get("/api/v1/admin/employees/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.fullName").value("Jan Kowalski"))
                    .andExpect(jsonPath("$.email").value("jan@example.com"));

            verify(employeeService).getEmployeeById(1L);
        }

        @Test
        @DisplayName("Should return 404 when employee not found")
        @WithMockUser(roles = "ADMIN")
        void shouldReturn404WhenEmployeeNotFound() throws Exception {
            when(employeeService.getEmployeeById(999L))
                    .thenThrow(new ApplicationException(HttpStatus.NOT_FOUND, "Pracownik nie został znaleziony"));

            mockMvc.perform(get("/api/v1/admin/employees/999"))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should return 403 for non-admin user")
        @WithMockUser(roles = "EMPLOYEE")
        void shouldReturn403ForNonAdmin() throws Exception {
            mockMvc.perform(get("/api/v1/admin/employees/1"))
                    .andExpect(status().isForbidden());

            verify(employeeService, never()).getEmployeeById(any());
        }
    }
}

package com.assetmanagement.service;

import com.assetmanagement.dto.request.CreateEmployeeRequest;
import com.assetmanagement.dto.response.EmployeeResponse;
import com.assetmanagement.exception.ApplicationException;
import com.assetmanagement.model.Employee;
import com.assetmanagement.model.Role;
import com.assetmanagement.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private EmployeeService employeeService;

    private Employee testEmployee;
    private CreateEmployeeRequest createEmployeeRequest;

    @BeforeEach
    void setUp() {
        testEmployee = new Employee();
        testEmployee.setId(1L);
        testEmployee.setFullName("Jan Kowalski");
        testEmployee.setEmail("jan.kowalski@example.com");
        testEmployee.setPassword("encodedPassword123");
        testEmployee.setRole(Role.EMPLOYEE);
        testEmployee.setHiredFrom(LocalDate.of(2024, 1, 15));
        testEmployee.setHiredUntil(null);

        createEmployeeRequest = new CreateEmployeeRequest();
        createEmployeeRequest.setFullName("Jan Kowalski");
        createEmployeeRequest.setEmail("jan.kowalski@example.com");
        createEmployeeRequest.setPassword("rawPassword123");
        createEmployeeRequest.setRole(Role.EMPLOYEE);
        createEmployeeRequest.setHiredFrom(LocalDate.of(2024, 1, 15));
        createEmployeeRequest.setHiredUntil(null);
    }

    @Nested
    @DisplayName("Create Employee Tests")
    class CreateEmployeeTests {

        @Test
        @DisplayName("Should create employee successfully")
        void shouldCreateEmployeeSuccessfully() {
            when(employeeRepository.existsByEmail(createEmployeeRequest.getEmail())).thenReturn(false);
            when(passwordEncoder.encode(createEmployeeRequest.getPassword())).thenReturn("encodedPassword123");
            when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);

            EmployeeResponse response = employeeService.createEmployee(createEmployeeRequest);

            assertNotNull(response);
            assertEquals(testEmployee.getId(), response.getId());
            assertEquals(testEmployee.getFullName(), response.getFullName());
            assertEquals(testEmployee.getEmail(), response.getEmail());
            assertEquals(testEmployee.getRole(), response.getRole());
            assertEquals(testEmployee.getHiredFrom(), response.getHiredFrom());
            assertNull(response.getHiredUntil());
        }

        @Test
        @DisplayName("Should encode password before saving")
        void shouldEncodePasswordBeforeSaving() {
            when(employeeRepository.existsByEmail(anyString())).thenReturn(false);
            when(passwordEncoder.encode("rawPassword123")).thenReturn("encodedPassword123");
            when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);

            employeeService.createEmployee(createEmployeeRequest);

            ArgumentCaptor<Employee> employeeCaptor = ArgumentCaptor.forClass(Employee.class);
            verify(employeeRepository).save(employeeCaptor.capture());
            assertEquals("encodedPassword123", employeeCaptor.getValue().getPassword());
        }

        @Test
        @DisplayName("Should throw exception when email already exists")
        void shouldThrowExceptionWhenEmailAlreadyExists() {
            when(employeeRepository.existsByEmail(createEmployeeRequest.getEmail())).thenReturn(true);

            ApplicationException exception = assertThrows(ApplicationException.class,
                    () -> employeeService.createEmployee(createEmployeeRequest));

            assertEquals(HttpStatus.BAD_REQUEST, exception.getHttpStatus());
            assertEquals("Ten adres email jest już używany", exception.getMessage());
            verify(employeeRepository, never()).save(any(Employee.class));
            verify(passwordEncoder, never()).encode(anyString());
        }

        @Test
        @DisplayName("Should create admin employee")
        void shouldCreateAdminEmployee() {
            createEmployeeRequest.setRole(Role.ADMIN);
            testEmployee.setRole(Role.ADMIN);

            when(employeeRepository.existsByEmail(createEmployeeRequest.getEmail())).thenReturn(false);
            when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
            when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);

            EmployeeResponse response = employeeService.createEmployee(createEmployeeRequest);

            assertEquals(Role.ADMIN, response.getRole());
        }

        @Test
        @DisplayName("Should create employee with hiredUntil date")
        void shouldCreateEmployeeWithHiredUntilDate() {
            LocalDate hiredUntil = LocalDate.of(2025, 12, 31);
            createEmployeeRequest.setHiredUntil(hiredUntil);
            testEmployee.setHiredUntil(hiredUntil);

            when(employeeRepository.existsByEmail(anyString())).thenReturn(false);
            when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
            when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);

            EmployeeResponse response = employeeService.createEmployee(createEmployeeRequest);

            assertEquals(hiredUntil, response.getHiredUntil());
        }
    }

    @Nested
    @DisplayName("Get All Employees Tests")
    class GetAllEmployeesTests {

        @Test
        @DisplayName("Should return all employees")
        void shouldReturnAllEmployees() {
            Employee employee2 = new Employee();
            employee2.setId(2L);
            employee2.setFullName("Anna Nowak");
            employee2.setEmail("anna.nowak@example.com");
            employee2.setRole(Role.ADMIN);
            employee2.setHiredFrom(LocalDate.of(2023, 6, 1));

            when(employeeRepository.findAll()).thenReturn(Arrays.asList(testEmployee, employee2));

            List<EmployeeResponse> response = employeeService.getAllEmployees();

            assertEquals(2, response.size());
            assertEquals("Jan Kowalski", response.get(0).getFullName());
            assertEquals("Anna Nowak", response.get(1).getFullName());
        }

        @Test
        @DisplayName("Should return empty list when no employees")
        void shouldReturnEmptyListWhenNoEmployees() {
            when(employeeRepository.findAll()).thenReturn(List.of());

            List<EmployeeResponse> response = employeeService.getAllEmployees();

            assertTrue(response.isEmpty());
        }
    }

    @Nested
    @DisplayName("Get Employee By Id Tests")
    class GetEmployeeByIdTests {

        @Test
        @DisplayName("Should return employee when found")
        void shouldReturnEmployeeWhenFound() {
            when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));

            EmployeeResponse response = employeeService.getEmployeeById(1L);

            assertNotNull(response);
            assertEquals(testEmployee.getId(), response.getId());
            assertEquals(testEmployee.getFullName(), response.getFullName());
            assertEquals(testEmployee.getEmail(), response.getEmail());
            assertEquals(testEmployee.getRole(), response.getRole());
        }

        @Test
        @DisplayName("Should throw exception when employee not found")
        void shouldThrowExceptionWhenEmployeeNotFound() {
            when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

            ApplicationException exception = assertThrows(ApplicationException.class,
                    () -> employeeService.getEmployeeById(999L));

            assertEquals(HttpStatus.NOT_FOUND, exception.getHttpStatus());
            assertEquals("Pracownik nie został znaleziony", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("Employee Response Mapping Tests")
    class EmployeeResponseMappingTests {

        @Test
        @DisplayName("Should correctly map all employee fields to response")
        void shouldCorrectlyMapAllEmployeeFieldsToResponse() {
            testEmployee.setHiredUntil(LocalDate.of(2025, 12, 31));

            when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));

            EmployeeResponse response = employeeService.getEmployeeById(1L);

            assertAll(
                    () -> assertEquals(testEmployee.getId(), response.getId()),
                    () -> assertEquals(testEmployee.getFullName(), response.getFullName()),
                    () -> assertEquals(testEmployee.getEmail(), response.getEmail()),
                    () -> assertEquals(testEmployee.getRole(), response.getRole()),
                    () -> assertEquals(testEmployee.getHiredFrom(), response.getHiredFrom()),
                    () -> assertEquals(testEmployee.getHiredUntil(), response.getHiredUntil())
            );
        }
    }
}

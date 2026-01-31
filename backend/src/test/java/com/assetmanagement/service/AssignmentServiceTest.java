package com.assetmanagement.service;

import com.assetmanagement.dto.request.CreateAssignmentRequest;
import com.assetmanagement.dto.request.EndAssignmentRequest;
import com.assetmanagement.dto.response.AssignmentResponse;
import com.assetmanagement.dto.response.PagedResponse;
import com.assetmanagement.exception.ApplicationException;
import com.assetmanagement.model.Asset;
import com.assetmanagement.model.AssetType;
import com.assetmanagement.model.Assignment;
import com.assetmanagement.model.Employee;
import com.assetmanagement.model.Role;
import com.assetmanagement.repository.AssetRepository;
import com.assetmanagement.repository.AssignmentRepository;
import com.assetmanagement.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AssignmentServiceTest {

    @Mock
    private AssignmentRepository assignmentRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private AssetRepository assetRepository;

    @InjectMocks
    private AssignmentService assignmentService;

    private Employee testEmployee;
    private Asset testAsset;
    private Assignment testAssignment;
    private CreateAssignmentRequest createAssignmentRequest;

    @BeforeEach
    void setUp() {
        testEmployee = new Employee();
        testEmployee.setId(1L);
        testEmployee.setFullName("Jan Kowalski");
        testEmployee.setEmail("jan.kowalski@example.com");
        testEmployee.setRole(Role.EMPLOYEE);

        testAsset = new Asset();
        testAsset.setId(1L);
        testAsset.setAssetType(AssetType.LAPTOP);
        testAsset.setVendor("Dell");
        testAsset.setModel("XPS 15");
        testAsset.setSeriesNumber("SN12345");
        testAsset.setActive(true);

        testAssignment = new Assignment();
        testAssignment.setId(1L);
        testAssignment.setEmployee(testEmployee);
        testAssignment.setAsset(testAsset);
        testAssignment.setAssignedFrom(LocalDate.of(2024, 1, 15));
        testAssignment.setAssignedUntil(null);

        createAssignmentRequest = new CreateAssignmentRequest();
        createAssignmentRequest.setEmployeeId(1L);
        createAssignmentRequest.setAssetId(1L);
        createAssignmentRequest.setAssignedFrom(LocalDate.of(2024, 1, 15));
    }

    @Nested
    @DisplayName("Create Assignment Tests")
    class CreateAssignmentTests {

        @Test
        @DisplayName("Should create assignment successfully")
        void shouldCreateAssignmentSuccessfully() {
            when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
            when(assetRepository.findById(1L)).thenReturn(Optional.of(testAsset));
            when(assignmentRepository.findActiveByAssetId(1L)).thenReturn(Optional.empty());
            when(assignmentRepository.save(any(Assignment.class))).thenReturn(testAssignment);

            AssignmentResponse response = assignmentService.createAssignment(createAssignmentRequest);

            assertNotNull(response);
            assertEquals(testAssignment.getId(), response.getId());
            assertEquals(testAsset.getId(), response.getAssetId());
            assertEquals(testEmployee.getId(), response.getEmployeeId());
            assertEquals(testEmployee.getFullName(), response.getEmployeeFullName());
            assertTrue(response.isActive());
            verify(assignmentRepository).save(any(Assignment.class));
        }

        @Test
        @DisplayName("Should throw exception when employee not found")
        void shouldThrowExceptionWhenEmployeeNotFound() {
            when(employeeRepository.findById(999L)).thenReturn(Optional.empty());
            createAssignmentRequest.setEmployeeId(999L);

            ApplicationException exception = assertThrows(ApplicationException.class,
                    () -> assignmentService.createAssignment(createAssignmentRequest));

            assertEquals(HttpStatus.NOT_FOUND, exception.getHttpStatus());
            assertEquals("Pracownik nie został znaleziony", exception.getMessage());
            verify(assignmentRepository, never()).save(any(Assignment.class));
        }

        @Test
        @DisplayName("Should throw exception when asset not found")
        void shouldThrowExceptionWhenAssetNotFound() {
            when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
            when(assetRepository.findById(999L)).thenReturn(Optional.empty());
            createAssignmentRequest.setAssetId(999L);

            ApplicationException exception = assertThrows(ApplicationException.class,
                    () -> assignmentService.createAssignment(createAssignmentRequest));

            assertEquals(HttpStatus.NOT_FOUND, exception.getHttpStatus());
            assertEquals("Zasób nie został znaleziony", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception when asset is inactive")
        void shouldThrowExceptionWhenAssetIsInactive() {
            testAsset.setActive(false);
            when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
            when(assetRepository.findById(1L)).thenReturn(Optional.of(testAsset));

            ApplicationException exception = assertThrows(ApplicationException.class,
                    () -> assignmentService.createAssignment(createAssignmentRequest));

            assertEquals(HttpStatus.BAD_REQUEST, exception.getHttpStatus());
            assertEquals("Nie można przypisać nieaktywnego zasobu", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception when asset is already assigned")
        void shouldThrowExceptionWhenAssetIsAlreadyAssigned() {
            when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
            when(assetRepository.findById(1L)).thenReturn(Optional.of(testAsset));
            when(assignmentRepository.findActiveByAssetId(1L)).thenReturn(Optional.of(testAssignment));

            ApplicationException exception = assertThrows(ApplicationException.class,
                    () -> assignmentService.createAssignment(createAssignmentRequest));

            assertEquals(HttpStatus.BAD_REQUEST, exception.getHttpStatus());
            assertEquals("Zasób jest już przypisany do innego pracownika", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("End Assignment Tests")
    class EndAssignmentTests {

        @Test
        @DisplayName("Should end assignment successfully")
        void shouldEndAssignmentSuccessfully() {
            EndAssignmentRequest endRequest = new EndAssignmentRequest();
            endRequest.setAssignedUntil(LocalDate.of(2024, 6, 30));

            Assignment endedAssignment = new Assignment();
            endedAssignment.setId(1L);
            endedAssignment.setEmployee(testEmployee);
            endedAssignment.setAsset(testAsset);
            endedAssignment.setAssignedFrom(LocalDate.of(2024, 1, 15));
            endedAssignment.setAssignedUntil(LocalDate.of(2024, 6, 30));

            when(assignmentRepository.findById(1L)).thenReturn(Optional.of(testAssignment));
            when(assignmentRepository.save(any(Assignment.class))).thenReturn(endedAssignment);

            AssignmentResponse response = assignmentService.endAssignment(1L, endRequest);

            assertNotNull(response);
            assertFalse(response.isActive());
            assertEquals(LocalDate.of(2024, 6, 30), response.getAssignedUntil());
        }

        @Test
        @DisplayName("Should throw exception when assignment not found")
        void shouldThrowExceptionWhenAssignmentNotFound() {
            EndAssignmentRequest endRequest = new EndAssignmentRequest();
            endRequest.setAssignedUntil(LocalDate.of(2024, 6, 30));

            when(assignmentRepository.findById(999L)).thenReturn(Optional.empty());

            ApplicationException exception = assertThrows(ApplicationException.class,
                    () -> assignmentService.endAssignment(999L, endRequest));

            assertEquals(HttpStatus.NOT_FOUND, exception.getHttpStatus());
            assertEquals("Przydział nie został znaleziony", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception when assignment already ended")
        void shouldThrowExceptionWhenAssignmentAlreadyEnded() {
            testAssignment.setAssignedUntil(LocalDate.of(2024, 5, 31));
            EndAssignmentRequest endRequest = new EndAssignmentRequest();
            endRequest.setAssignedUntil(LocalDate.of(2024, 6, 30));

            when(assignmentRepository.findById(1L)).thenReturn(Optional.of(testAssignment));

            ApplicationException exception = assertThrows(ApplicationException.class,
                    () -> assignmentService.endAssignment(1L, endRequest));

            assertEquals(HttpStatus.BAD_REQUEST, exception.getHttpStatus());
            assertEquals("Przydział został już zakończony", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception when end date is before start date")
        void shouldThrowExceptionWhenEndDateBeforeStartDate() {
            EndAssignmentRequest endRequest = new EndAssignmentRequest();
            endRequest.setAssignedUntil(LocalDate.of(2024, 1, 1)); // Before assignedFrom

            when(assignmentRepository.findById(1L)).thenReturn(Optional.of(testAssignment));

            ApplicationException exception = assertThrows(ApplicationException.class,
                    () -> assignmentService.endAssignment(1L, endRequest));

            assertEquals(HttpStatus.BAD_REQUEST, exception.getHttpStatus());
            assertEquals("Data zakończenia nie może być przed datą rozpoczęcia", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("Get All Assignments Tests")
    class GetAllAssignmentsTests {

        @Test
        @DisplayName("Should return all assignments")
        void shouldReturnAllAssignments() {
            Assignment assignment2 = new Assignment();
            assignment2.setId(2L);
            assignment2.setEmployee(testEmployee);
            assignment2.setAsset(testAsset);
            assignment2.setAssignedFrom(LocalDate.of(2023, 6, 1));
            assignment2.setAssignedUntil(LocalDate.of(2023, 12, 31));

            when(assignmentRepository.findAll()).thenReturn(Arrays.asList(testAssignment, assignment2));

            List<AssignmentResponse> response = assignmentService.getAllAssignments();

            assertEquals(2, response.size());
        }

        @Test
        @DisplayName("Should return empty list when no assignments")
        void shouldReturnEmptyListWhenNoAssignments() {
            when(assignmentRepository.findAll()).thenReturn(List.of());

            List<AssignmentResponse> response = assignmentService.getAllAssignments();

            assertTrue(response.isEmpty());
        }
    }

    @Nested
    @DisplayName("Get All Assignments Paged Tests")
    class GetAllAssignmentsPagedTests {

        @Test
        @DisplayName("Should return paged assignments without filters")
        void shouldReturnPagedAssignmentsWithoutFilters() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Assignment> assignmentPage = new PageImpl<>(List.of(testAssignment), pageable, 1);

            when(assignmentRepository.findAll(pageable)).thenReturn(assignmentPage);

            PagedResponse<AssignmentResponse> response = assignmentService.getAllAssignments(pageable, null, null, null);

            assertEquals(1, response.getContent().size());
            assertEquals(0, response.getPage());
            assertEquals(10, response.getSize());
        }

        @Test
        @DisplayName("Should filter assignments by active status")
        void shouldFilterAssignmentsByActiveStatus() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Assignment> assignmentPage = new PageImpl<>(List.of(testAssignment), pageable, 1);

            when(assignmentRepository.findActive(pageable)).thenReturn(assignmentPage);

            assignmentService.getAllAssignments(pageable, true, null, null);

            verify(assignmentRepository).findActive(pageable);
        }

        @Test
        @DisplayName("Should filter assignments by employee id")
        void shouldFilterAssignmentsByEmployeeId() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Assignment> assignmentPage = new PageImpl<>(List.of(testAssignment), pageable, 1);

            when(assignmentRepository.findByEmployeeId(1L, pageable)).thenReturn(assignmentPage);

            assignmentService.getAllAssignments(pageable, null, 1L, null);

            verify(assignmentRepository).findByEmployeeId(1L, pageable);
        }

        @Test
        @DisplayName("Should filter assignments by asset id")
        void shouldFilterAssignmentsByAssetId() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Assignment> assignmentPage = new PageImpl<>(List.of(testAssignment), pageable, 1);

            when(assignmentRepository.findByAssetId(1L, pageable)).thenReturn(assignmentPage);

            assignmentService.getAllAssignments(pageable, null, null, 1L);

            verify(assignmentRepository).findByAssetId(1L, pageable);
        }

        @Test
        @DisplayName("Should filter assignments by multiple criteria")
        void shouldFilterAssignmentsByMultipleCriteria() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Assignment> assignmentPage = new PageImpl<>(List.of(testAssignment), pageable, 1);

            when(assignmentRepository.findActiveByEmployeeIdPaged(1L, pageable)).thenReturn(assignmentPage);

            assignmentService.getAllAssignments(pageable, true, 1L, null);

            verify(assignmentRepository).findActiveByEmployeeIdPaged(1L, pageable);
        }
    }

    @Nested
    @DisplayName("Get Assignments By Employee Id Tests")
    class GetAssignmentsByEmployeeIdTests {

        @Test
        @DisplayName("Should return assignments for employee")
        void shouldReturnAssignmentsForEmployee() {
            when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
            when(assignmentRepository.findByEmployee(testEmployee)).thenReturn(List.of(testAssignment));

            List<AssignmentResponse> response = assignmentService.getAssignmentsByEmployeeId(1L);

            assertEquals(1, response.size());
            assertEquals(testEmployee.getId(), response.get(0).getEmployeeId());
        }

        @Test
        @DisplayName("Should throw exception when employee not found")
        void shouldThrowExceptionWhenEmployeeNotFoundForAssignments() {
            when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

            ApplicationException exception = assertThrows(ApplicationException.class,
                    () -> assignmentService.getAssignmentsByEmployeeId(999L));

            assertEquals(HttpStatus.NOT_FOUND, exception.getHttpStatus());
        }
    }

    @Nested
    @DisplayName("Get Assignments By Asset Id Tests")
    class GetAssignmentsByAssetIdTests {

        @Test
        @DisplayName("Should return assignments for asset")
        void shouldReturnAssignmentsForAsset() {
            when(assetRepository.findById(1L)).thenReturn(Optional.of(testAsset));
            when(assignmentRepository.findByAsset(testAsset)).thenReturn(List.of(testAssignment));

            List<AssignmentResponse> response = assignmentService.getAssignmentsByAssetId(1L);

            assertEquals(1, response.size());
            assertEquals(testAsset.getId(), response.get(0).getAssetId());
        }

        @Test
        @DisplayName("Should throw exception when asset not found")
        void shouldThrowExceptionWhenAssetNotFoundForAssignments() {
            when(assetRepository.findById(999L)).thenReturn(Optional.empty());

            ApplicationException exception = assertThrows(ApplicationException.class,
                    () -> assignmentService.getAssignmentsByAssetId(999L));

            assertEquals(HttpStatus.NOT_FOUND, exception.getHttpStatus());
        }
    }

    @Nested
    @DisplayName("Get Assignment History By Employee Email Tests")
    class GetAssignmentHistoryByEmployeeEmailTests {

        @Test
        @DisplayName("Should return assignment history for employee by email")
        void shouldReturnAssignmentHistoryForEmployeeByEmail() {
            when(employeeRepository.findByEmail(testEmployee.getEmail())).thenReturn(Optional.of(testEmployee));
            when(assignmentRepository.findByEmployee(testEmployee)).thenReturn(List.of(testAssignment));

            List<AssignmentResponse> response = assignmentService.getAssignmentHistoryByEmployeeEmail(testEmployee.getEmail());

            assertEquals(1, response.size());
        }

        @Test
        @DisplayName("Should throw exception when employee not found by email")
        void shouldThrowExceptionWhenEmployeeNotFoundByEmail() {
            when(employeeRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

            ApplicationException exception = assertThrows(ApplicationException.class,
                    () -> assignmentService.getAssignmentHistoryByEmployeeEmail("unknown@example.com"));

            assertEquals(HttpStatus.NOT_FOUND, exception.getHttpStatus());
            assertEquals("Pracownik nie został znaleziony", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("Assignment Response Mapping Tests")
    class AssignmentResponseMappingTests {

        @Test
        @DisplayName("Should correctly map active assignment to response")
        void shouldCorrectlyMapActiveAssignmentToResponse() {
            when(assignmentRepository.findAll()).thenReturn(List.of(testAssignment));

            List<AssignmentResponse> responses = assignmentService.getAllAssignments();

            AssignmentResponse response = responses.get(0);
            assertAll(
                    () -> assertEquals(testAssignment.getId(), response.getId()),
                    () -> assertEquals(testAsset.getId(), response.getAssetId()),
                    () -> assertEquals(testAsset.getAssetType().name(), response.getAssetType()),
                    () -> assertEquals(testAsset.getVendor(), response.getVendor()),
                    () -> assertEquals(testAsset.getModel(), response.getModel()),
                    () -> assertEquals(testAsset.getSeriesNumber(), response.getSeriesNumber()),
                    () -> assertEquals(testEmployee.getId(), response.getEmployeeId()),
                    () -> assertEquals(testEmployee.getFullName(), response.getEmployeeFullName()),
                    () -> assertEquals(testAssignment.getAssignedFrom(), response.getAssignedFrom()),
                    () -> assertNull(response.getAssignedUntil()),
                    () -> assertTrue(response.isActive())
            );
        }

        @Test
        @DisplayName("Should correctly map ended assignment to response")
        void shouldCorrectlyMapEndedAssignmentToResponse() {
            testAssignment.setAssignedUntil(LocalDate.of(2024, 6, 30));
            when(assignmentRepository.findAll()).thenReturn(List.of(testAssignment));

            List<AssignmentResponse> responses = assignmentService.getAllAssignments();

            AssignmentResponse response = responses.get(0);
            assertFalse(response.isActive());
            assertEquals(LocalDate.of(2024, 6, 30), response.getAssignedUntil());
        }
    }
}

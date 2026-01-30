package com.assetmanagement.service;

import com.assetmanagement.dto.request.CreateAssetRequest;
import com.assetmanagement.dto.response.AssetResponse;
import com.assetmanagement.dto.response.EmployeeAssetResponse;
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
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AssetServiceTest {

    @Mock
    private AssetRepository assetRepository;

    @Mock
    private AssignmentRepository assignmentRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private AssetService assetService;

    private Asset testAsset;
    private Employee testEmployee;
    private CreateAssetRequest createAssetRequest;

    @BeforeEach
    void setUp() {
        testAsset = new Asset();
        testAsset.setId(1L);
        testAsset.setAssetType(AssetType.LAPTOP);
        testAsset.setVendor("Dell");
        testAsset.setModel("XPS 15");
        testAsset.setSeriesNumber("SN12345");
        testAsset.setActive(true);

        testEmployee = new Employee();
        testEmployee.setId(1L);
        testEmployee.setFullName("Jan Kowalski");
        testEmployee.setEmail("jan.kowalski@example.com");
        testEmployee.setRole(Role.EMPLOYEE);

        createAssetRequest = new CreateAssetRequest();
        createAssetRequest.setAssetType(AssetType.LAPTOP);
        createAssetRequest.setVendor("Dell");
        createAssetRequest.setModel("XPS 15");
        createAssetRequest.setSeriesNumber("SN12345");
    }

    @Nested
    @DisplayName("Create Asset Tests")
    class CreateAssetTests {

        @Test
        @DisplayName("Should create asset successfully")
        void shouldCreateAssetSuccessfully() {
            when(assetRepository.existsBySeriesNumber(createAssetRequest.getSeriesNumber())).thenReturn(false);
            when(assetRepository.save(any(Asset.class))).thenReturn(testAsset);

            AssetResponse response = assetService.createAsset(createAssetRequest);

            assertNotNull(response);
            assertEquals(testAsset.getId(), response.getId());
            assertEquals(testAsset.getAssetType(), response.getAssetType());
            assertEquals(testAsset.getVendor(), response.getVendor());
            assertEquals(testAsset.getModel(), response.getModel());
            assertEquals(testAsset.getSeriesNumber(), response.getSeriesNumber());
            assertTrue(response.isActive());
            verify(assetRepository).save(any(Asset.class));
        }

        @Test
        @DisplayName("Should throw exception when series number already exists")
        void shouldThrowExceptionWhenSeriesNumberExists() {
            when(assetRepository.existsBySeriesNumber(createAssetRequest.getSeriesNumber())).thenReturn(true);

            ApplicationException exception = assertThrows(ApplicationException.class,
                    () -> assetService.createAsset(createAssetRequest));

            assertEquals(HttpStatus.BAD_REQUEST, exception.getHttpStatus());
            assertEquals("Numer seryjny jest już używany", exception.getMessage());
            verify(assetRepository, never()).save(any(Asset.class));
        }
    }

    @Nested
    @DisplayName("Get Asset By Id Tests")
    class GetAssetByIdTests {

        @Test
        @DisplayName("Should return asset when found")
        void shouldReturnAssetWhenFound() {
            when(assetRepository.findById(1L)).thenReturn(Optional.of(testAsset));
            when(assignmentRepository.findActiveByAssetId(1L)).thenReturn(Optional.empty());

            AssetResponse response = assetService.getAssetById(1L);

            assertNotNull(response);
            assertEquals(testAsset.getId(), response.getId());
            assertNull(response.getAssignedEmployeeId());
        }

        @Test
        @DisplayName("Should return asset with assigned employee info")
        void shouldReturnAssetWithAssignedEmployeeInfo() {
            Assignment assignment = new Assignment();
            assignment.setEmployee(testEmployee);
            assignment.setAsset(testAsset);

            when(assetRepository.findById(1L)).thenReturn(Optional.of(testAsset));
            when(assignmentRepository.findActiveByAssetId(1L)).thenReturn(Optional.of(assignment));

            AssetResponse response = assetService.getAssetById(1L);

            assertNotNull(response);
            assertEquals(testEmployee.getId(), response.getAssignedEmployeeId());
            assertEquals(testEmployee.getFullName(), response.getAssignedEmployeeFullName());
            assertEquals(testEmployee.getEmail(), response.getAssignedEmployeeEmail());
        }

        @Test
        @DisplayName("Should throw exception when asset not found")
        void shouldThrowExceptionWhenAssetNotFound() {
            when(assetRepository.findById(999L)).thenReturn(Optional.empty());

            ApplicationException exception = assertThrows(ApplicationException.class,
                    () -> assetService.getAssetById(999L));

            assertEquals(HttpStatus.NOT_FOUND, exception.getHttpStatus());
            assertEquals("Zasób nie został znaleziony", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("Get All Assets Tests")
    class GetAllAssetsTests {

        @Test
        @DisplayName("Should return all assets")
        void shouldReturnAllAssets() {
            Asset asset2 = new Asset();
            asset2.setId(2L);
            asset2.setAssetType(AssetType.SMARTPHONE);
            asset2.setVendor("Apple");
            asset2.setModel("iPhone 15");
            asset2.setSeriesNumber("SN67890");
            asset2.setActive(true);

            when(assetRepository.findAll()).thenReturn(Arrays.asList(testAsset, asset2));
            when(assignmentRepository.findActiveByAssetId(anyLong())).thenReturn(Optional.empty());

            List<AssetResponse> response = assetService.getAllAssets();

            assertEquals(2, response.size());
            verify(assetRepository).findAll();
        }

        @Test
        @DisplayName("Should return empty list when no assets")
        void shouldReturnEmptyListWhenNoAssets() {
            when(assetRepository.findAll()).thenReturn(List.of());

            List<AssetResponse> response = assetService.getAllAssets();

            assertTrue(response.isEmpty());
        }
    }

    @Nested
    @DisplayName("Get All Assets Paged Tests")
    class GetAllAssetsPagedTests {

        @Test
        @DisplayName("Should return paged assets without filters")
        void shouldReturnPagedAssetsWithoutFilters() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Asset> assetPage = new PageImpl<>(List.of(testAsset), pageable, 1);

            when(assetRepository.findAll(pageable)).thenReturn(assetPage);
            when(assignmentRepository.findActiveByAssetId(anyLong())).thenReturn(Optional.empty());

            PagedResponse<AssetResponse> response = assetService.getAllAssets(pageable, null, null, null);

            assertEquals(1, response.getContent().size());
            assertEquals(0, response.getPage());
            assertEquals(10, response.getSize());
            assertEquals(1, response.getTotalElements());
        }

        @Test
        @DisplayName("Should filter assets by active status")
        void shouldFilterAssetsByActiveStatus() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Asset> assetPage = new PageImpl<>(List.of(testAsset), pageable, 1);

            when(assetRepository.findByIsActive(true, pageable)).thenReturn(assetPage);
            when(assignmentRepository.findActiveByAssetId(anyLong())).thenReturn(Optional.empty());

            PagedResponse<AssetResponse> response = assetService.getAllAssets(pageable, true, null, null);

            assertEquals(1, response.getContent().size());
            verify(assetRepository).findByIsActive(true, pageable);
        }

        @Test
        @DisplayName("Should filter assets by asset type")
        void shouldFilterAssetsByAssetType() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Asset> assetPage = new PageImpl<>(List.of(testAsset), pageable, 1);

            when(assetRepository.findByAssetType(AssetType.LAPTOP, pageable)).thenReturn(assetPage);
            when(assignmentRepository.findActiveByAssetId(anyLong())).thenReturn(Optional.empty());

            PagedResponse<AssetResponse> response = assetService.getAllAssets(pageable, null, AssetType.LAPTOP, null);

            assertEquals(1, response.getContent().size());
            verify(assetRepository).findByAssetType(AssetType.LAPTOP, pageable);
        }

        @Test
        @DisplayName("Should filter assets by assigned status")
        void shouldFilterAssetsByAssignedStatus() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Asset> assetPage = new PageImpl<>(List.of(testAsset), pageable, 1);

            when(assetRepository.findAssigned(pageable)).thenReturn(assetPage);
            when(assignmentRepository.findActiveByAssetId(anyLong())).thenReturn(Optional.empty());

            assetService.getAllAssets(pageable, null, null, true);

            verify(assetRepository).findAssigned(pageable);
        }

        @Test
        @DisplayName("Should filter assets with multiple criteria")
        void shouldFilterAssetsWithMultipleCriteria() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Asset> assetPage = new PageImpl<>(List.of(testAsset), pageable, 1);

            when(assetRepository.findByIsActiveAndAssetType(true, AssetType.LAPTOP, pageable)).thenReturn(assetPage);
            when(assignmentRepository.findActiveByAssetId(anyLong())).thenReturn(Optional.empty());

            assetService.getAllAssets(pageable, true, AssetType.LAPTOP, null);

            verify(assetRepository).findByIsActiveAndAssetType(true, AssetType.LAPTOP, pageable);
        }
    }

    @Nested
    @DisplayName("Deactivate Asset Tests")
    class DeactivateAssetTests {

        @Test
        @DisplayName("Should deactivate asset successfully")
        void shouldDeactivateAssetSuccessfully() {
            when(assetRepository.findById(1L)).thenReturn(Optional.of(testAsset));
            when(assignmentRepository.findActiveByAssetId(1L)).thenReturn(Optional.empty());

            assetService.deactivateAsset(1L);

            assertFalse(testAsset.isActive());
            verify(assetRepository).save(testAsset);
        }

        @Test
        @DisplayName("Should throw exception when asset is assigned")
        void shouldThrowExceptionWhenAssetIsAssigned() {
            Assignment activeAssignment = new Assignment();
            activeAssignment.setAsset(testAsset);
            activeAssignment.setEmployee(testEmployee);

            when(assetRepository.findById(1L)).thenReturn(Optional.of(testAsset));
            when(assignmentRepository.findActiveByAssetId(1L)).thenReturn(Optional.of(activeAssignment));

            ApplicationException exception = assertThrows(ApplicationException.class,
                    () -> assetService.deactivateAsset(1L));

            assertEquals(HttpStatus.BAD_REQUEST, exception.getHttpStatus());
            assertEquals("Nie można dezaktywować zasobu, który jest przypisany do pracownika", exception.getMessage());
            verify(assetRepository, never()).save(any(Asset.class));
        }

        @Test
        @DisplayName("Should throw exception when asset not found")
        void shouldThrowExceptionWhenAssetNotFoundForDeactivation() {
            when(assetRepository.findById(999L)).thenReturn(Optional.empty());

            ApplicationException exception = assertThrows(ApplicationException.class,
                    () -> assetService.deactivateAsset(999L));

            assertEquals(HttpStatus.NOT_FOUND, exception.getHttpStatus());
        }
    }

    @Nested
    @DisplayName("Get Active Assets By Employee Email Tests")
    class GetActiveAssetsByEmployeeEmailTests {

        @Test
        @DisplayName("Should return active assets for employee")
        void shouldReturnActiveAssetsForEmployee() {
            Assignment assignment = new Assignment();
            assignment.setAsset(testAsset);
            assignment.setEmployee(testEmployee);
            assignment.setAssignedFrom(LocalDate.now().minusDays(30));

            when(employeeRepository.findByEmail(testEmployee.getEmail())).thenReturn(Optional.of(testEmployee));
            when(assignmentRepository.findActiveByEmployeeId(testEmployee.getId())).thenReturn(List.of(assignment));

            List<EmployeeAssetResponse> response = assetService.getActiveAssetsByEmployeeEmail(testEmployee.getEmail());

            assertEquals(1, response.size());
            assertEquals(testAsset.getAssetType(), response.get(0).getAssetType());
            assertEquals(testAsset.getVendor(), response.get(0).getVendor());
            assertEquals(testAsset.getModel(), response.get(0).getModel());
        }

        @Test
        @DisplayName("Should throw exception when employee not found")
        void shouldThrowExceptionWhenEmployeeNotFound() {
            when(employeeRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

            ApplicationException exception = assertThrows(ApplicationException.class,
                    () -> assetService.getActiveAssetsByEmployeeEmail("unknown@example.com"));

            assertEquals(HttpStatus.NOT_FOUND, exception.getHttpStatus());
            assertEquals("Pracownik nie został znaleziony", exception.getMessage());
        }

        @Test
        @DisplayName("Should return empty list when employee has no active assets")
        void shouldReturnEmptyListWhenNoActiveAssets() {
            when(employeeRepository.findByEmail(testEmployee.getEmail())).thenReturn(Optional.of(testEmployee));
            when(assignmentRepository.findActiveByEmployeeId(testEmployee.getId())).thenReturn(List.of());

            List<EmployeeAssetResponse> response = assetService.getActiveAssetsByEmployeeEmail(testEmployee.getEmail());

            assertTrue(response.isEmpty());
        }
    }
}

package com.assetmanagement.controller;

import com.assetmanagement.dto.request.CreateAssetRequest;
import com.assetmanagement.dto.response.AssetResponse;
import com.assetmanagement.dto.response.EmployeeAssetResponse;
import com.assetmanagement.dto.response.PagedResponse;
import com.assetmanagement.exception.ApplicationException;
import com.assetmanagement.model.AssetType;
import com.assetmanagement.security.CustomUserDetailsService;
import com.assetmanagement.security.JwtUtil;
import com.assetmanagement.service.AssetService;
import tools.jackson.databind.json.JsonMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AssetController.class)
@EnableMethodSecurity
class AssetControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private JsonMapper jsonMapper;

    @MockitoBean
    private AssetService assetService;

    @BeforeEach
    void setUp() {
        jsonMapper = JsonMapper.builder().build();
    }

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @Nested
    @DisplayName("POST /api/v1/admin/assets")
    class CreateAssetTests {

        @Test
        @DisplayName("Should create asset and return 201 Created")
        @WithMockUser(roles = "ADMIN")
        void shouldCreateAssetAndReturn201() throws Exception {
            CreateAssetRequest request = new CreateAssetRequest();
            request.setAssetType(AssetType.LAPTOP);
            request.setVendor("Dell");
            request.setModel("XPS 15");
            request.setSeriesNumber("SN12345");

            AssetResponse response = new AssetResponse();
            response.setId(1L);
            response.setAssetType(AssetType.LAPTOP);
            response.setVendor("Dell");
            response.setModel("XPS 15");
            response.setSeriesNumber("SN12345");
            response.setActive(true);

            when(assetService.createAsset(any(CreateAssetRequest.class))).thenReturn(response);

            mockMvc.perform(post("/api/v1/admin/assets")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.vendor").value("Dell"))
                    .andExpect(jsonPath("$.model").value("XPS 15"));

            verify(assetService).createAsset(any(CreateAssetRequest.class));
        }

        @Test
        @DisplayName("Should return 400 when series number already exists")
        @WithMockUser(roles = "ADMIN")
        void shouldReturn400WhenSeriesNumberExists() throws Exception {
            CreateAssetRequest request = new CreateAssetRequest();
            request.setAssetType(AssetType.LAPTOP);
            request.setVendor("Dell");
            request.setModel("XPS 15");
            request.setSeriesNumber("SN12345");

            when(assetService.createAsset(any(CreateAssetRequest.class)))
                    .thenThrow(new ApplicationException(HttpStatus.BAD_REQUEST, "Numer seryjny jest już używany"));

            mockMvc.perform(post("/api/v1/admin/assets")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 403 Forbidden for non-admin user")
        @WithMockUser(roles = "EMPLOYEE")
        void shouldReturn403ForNonAdmin() throws Exception {
            CreateAssetRequest request = new CreateAssetRequest();
            request.setAssetType(AssetType.LAPTOP);
            request.setVendor("Dell");
            request.setModel("XPS 15");
            request.setSeriesNumber("SN12345");

            mockMvc.perform(post("/api/v1/admin/assets")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden());

            verify(assetService, never()).createAsset(any());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/admin/assets")
    class GetAllAssetsTests {

        @Test
        @DisplayName("Should return paged assets for admin")
        @WithMockUser(roles = "ADMIN")
        void shouldReturnPagedAssetsForAdmin() throws Exception {
            AssetResponse asset = new AssetResponse();
            asset.setId(1L);
            asset.setAssetType(AssetType.LAPTOP);
            asset.setVendor("Dell");
            asset.setModel("XPS 15");
            asset.setActive(true);

            PagedResponse<AssetResponse> pagedResponse = new PagedResponse<>(
                    List.of(asset), 0, 20, 1, 1, true
            );

            when(assetService.getAllAssets(any(Pageable.class), any(), any(), any()))
                    .thenReturn(pagedResponse);

            mockMvc.perform(get("/api/v1/admin/assets"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[0].id").value(1))
                    .andExpect(jsonPath("$.content[0].vendor").value("Dell"))
                    .andExpect(jsonPath("$.totalElements").value(1));
        }

        @Test
        @DisplayName("Should filter assets by active status")
        @WithMockUser(roles = "ADMIN")
        void shouldFilterAssetsByActiveStatus() throws Exception {
            PagedResponse<AssetResponse> pagedResponse = new PagedResponse<>(
                    List.of(), 0, 20, 0, 0, true
            );

            when(assetService.getAllAssets(any(Pageable.class), eq(true), any(), any()))
                    .thenReturn(pagedResponse);

            mockMvc.perform(get("/api/v1/admin/assets")
                            .param("isActive", "true"))
                    .andExpect(status().isOk());

            verify(assetService).getAllAssets(any(Pageable.class), eq(true), any(), any());
        }

        @Test
        @DisplayName("Should filter assets by asset type")
        @WithMockUser(roles = "ADMIN")
        void shouldFilterAssetsByAssetType() throws Exception {
            PagedResponse<AssetResponse> pagedResponse = new PagedResponse<>(
                    List.of(), 0, 20, 0, 0, true
            );

            when(assetService.getAllAssets(any(Pageable.class), any(), eq(AssetType.LAPTOP), any()))
                    .thenReturn(pagedResponse);

            mockMvc.perform(get("/api/v1/admin/assets")
                            .param("assetType", "LAPTOP"))
                    .andExpect(status().isOk());

            verify(assetService).getAllAssets(any(Pageable.class), any(), eq(AssetType.LAPTOP), any());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/admin/assets/{id}")
    class GetAssetByIdTests {

        @Test
        @DisplayName("Should return asset by id")
        @WithMockUser(roles = "ADMIN")
        void shouldReturnAssetById() throws Exception {
            AssetResponse response = new AssetResponse();
            response.setId(1L);
            response.setAssetType(AssetType.LAPTOP);
            response.setVendor("Dell");
            response.setModel("XPS 15");
            response.setSeriesNumber("SN12345");
            response.setActive(true);

            when(assetService.getAssetById(1L)).thenReturn(response);

            mockMvc.perform(get("/api/v1/admin/assets/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.vendor").value("Dell"));
        }

        @Test
        @DisplayName("Should return 404 when asset not found")
        @WithMockUser(roles = "ADMIN")
        void shouldReturn404WhenAssetNotFound() throws Exception {
            when(assetService.getAssetById(999L))
                    .thenThrow(new ApplicationException(HttpStatus.NOT_FOUND, "Zasób nie został znaleziony"));

            mockMvc.perform(get("/api/v1/admin/assets/999"))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/admin/assets/{id}/deactivate")
    class DeactivateAssetTests {

        @Test
        @DisplayName("Should deactivate asset and return 204")
        @WithMockUser(roles = "ADMIN")
        void shouldDeactivateAssetAndReturn204() throws Exception {
            doNothing().when(assetService).deactivateAsset(1L);

            mockMvc.perform(put("/api/v1/admin/assets/1/deactivate")
                            .with(csrf()))
                    .andExpect(status().isNoContent());

            verify(assetService).deactivateAsset(1L);
        }

        @Test
        @DisplayName("Should return 400 when asset is assigned")
        @WithMockUser(roles = "ADMIN")
        void shouldReturn400WhenAssetIsAssigned() throws Exception {
            doThrow(new ApplicationException(HttpStatus.BAD_REQUEST,
                    "Nie można dezaktywować zasobu, który jest przypisany do pracownika"))
                    .when(assetService).deactivateAsset(1L);

            mockMvc.perform(put("/api/v1/admin/assets/1/deactivate")
                            .with(csrf()))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/employee/assets")
    class GetMyActiveAssetsTests {

        @Test
        @DisplayName("Should return active assets for authenticated employee")
        @WithMockUser(username = "employee@example.com", roles = "EMPLOYEE")
        void shouldReturnActiveAssetsForEmployee() throws Exception {
            EmployeeAssetResponse asset = new EmployeeAssetResponse();
            asset.setAssetType(AssetType.LAPTOP);
            asset.setVendor("Dell");
            asset.setModel("XPS 15");
            asset.setSeriesNumber("SN12345");
            asset.setAssignedFrom(LocalDate.now());

            when(assetService.getActiveAssetsByEmployeeEmail("employee@example.com"))
                    .thenReturn(List.of(asset));

            mockMvc.perform(get("/api/v1/employee/assets"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].vendor").value("Dell"))
                    .andExpect(jsonPath("$[0].model").value("XPS 15"));

            verify(assetService).getActiveAssetsByEmployeeEmail("employee@example.com");
        }

        @Test
        @DisplayName("Should return active assets for admin")
        @WithMockUser(username = "admin@example.com", roles = "ADMIN")
        void shouldReturnActiveAssetsForAdmin() throws Exception {
            when(assetService.getActiveAssetsByEmployeeEmail("admin@example.com"))
                    .thenReturn(List.of());

            mockMvc.perform(get("/api/v1/employee/assets"))
                    .andExpect(status().isOk());

            verify(assetService).getActiveAssetsByEmployeeEmail("admin@example.com");
        }
    }
}

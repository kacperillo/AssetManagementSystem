package com.assetmanagement.controller;

import com.assetmanagement.dto.request.CreateAssignmentRequest;
import com.assetmanagement.dto.request.EndAssignmentRequest;
import com.assetmanagement.dto.response.AssignmentResponse;
import com.assetmanagement.dto.response.PagedResponse;
import com.assetmanagement.exception.ApplicationException;
import com.assetmanagement.security.CustomUserDetailsService;
import com.assetmanagement.security.JwtUtil;
import com.assetmanagement.service.AssignmentService;
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

@WebMvcTest(AssignmentController.class)
@EnableMethodSecurity
class AssignmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private JsonMapper jsonMapper;

    @MockitoBean
    private AssignmentService assignmentService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @BeforeEach
    void setUp() {
        jsonMapper = JsonMapper.builder().build();
    }

    private AssignmentResponse createTestAssignmentResponse() {
        AssignmentResponse response = new AssignmentResponse();
        response.setId(1L);
        response.setAssetId(1L);
        response.setAssetType("LAPTOP");
        response.setVendor("Dell");
        response.setModel("XPS 15");
        response.setSeriesNumber("SN12345");
        response.setEmployeeId(1L);
        response.setEmployeeFullName("Jan Kowalski");
        response.setAssignedFrom(LocalDate.of(2024, 1, 15));
        response.setActive(true);
        return response;
    }

    @Nested
    @DisplayName("POST /api/v1/admin/assignments")
    class CreateAssignmentTests {

        @Test
        @DisplayName("Should create assignment and return 201 Created")
        @WithMockUser(roles = "ADMIN")
        void shouldCreateAssignmentAndReturn201() throws Exception {
            CreateAssignmentRequest request = new CreateAssignmentRequest();
            request.setEmployeeId(1L);
            request.setAssetId(1L);
            request.setAssignedFrom(LocalDate.of(2024, 1, 15));

            AssignmentResponse response = createTestAssignmentResponse();
            when(assignmentService.createAssignment(any(CreateAssignmentRequest.class))).thenReturn(response);

            mockMvc.perform(post("/api/v1/admin/assignments")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.assetId").value(1))
                    .andExpect(jsonPath("$.employeeId").value(1))
                    .andExpect(jsonPath("$.isActive").value(true));

            verify(assignmentService).createAssignment(any(CreateAssignmentRequest.class));
        }

        @Test
        @DisplayName("Should return 400 when asset is already assigned")
        @WithMockUser(roles = "ADMIN")
        void shouldReturn400WhenAssetAlreadyAssigned() throws Exception {
            CreateAssignmentRequest request = new CreateAssignmentRequest();
            request.setEmployeeId(1L);
            request.setAssetId(1L);
            request.setAssignedFrom(LocalDate.of(2024, 1, 15));

            when(assignmentService.createAssignment(any(CreateAssignmentRequest.class)))
                    .thenThrow(new ApplicationException(HttpStatus.BAD_REQUEST, "Zasób jest już przypisany do innego pracownika"));

            mockMvc.perform(post("/api/v1/admin/assignments")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 400 when asset is inactive")
        @WithMockUser(roles = "ADMIN")
        void shouldReturn400WhenAssetInactive() throws Exception {
            CreateAssignmentRequest request = new CreateAssignmentRequest();
            request.setEmployeeId(1L);
            request.setAssetId(1L);
            request.setAssignedFrom(LocalDate.of(2024, 1, 15));

            when(assignmentService.createAssignment(any(CreateAssignmentRequest.class)))
                    .thenThrow(new ApplicationException(HttpStatus.BAD_REQUEST, "Nie można przypisać nieaktywnego zasobu"));

            mockMvc.perform(post("/api/v1/admin/assignments")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 403 Forbidden for non-admin user")
        @WithMockUser(roles = "EMPLOYEE")
        void shouldReturn403ForNonAdmin() throws Exception {
            CreateAssignmentRequest request = new CreateAssignmentRequest();
            request.setEmployeeId(1L);
            request.setAssetId(1L);
            request.setAssignedFrom(LocalDate.of(2024, 1, 15));

            mockMvc.perform(post("/api/v1/admin/assignments")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden());

            verify(assignmentService, never()).createAssignment(any());
        }

        @Test
        @DisplayName("Should return 404 when employee not found")
        @WithMockUser(roles = "ADMIN")
        void shouldReturn404WhenEmployeeNotFound() throws Exception {
            CreateAssignmentRequest request = new CreateAssignmentRequest();
            request.setEmployeeId(999L);
            request.setAssetId(1L);
            request.setAssignedFrom(LocalDate.of(2024, 1, 15));

            when(assignmentService.createAssignment(any(CreateAssignmentRequest.class)))
                    .thenThrow(new ApplicationException(HttpStatus.NOT_FOUND, "Pracownik nie został znaleziony"));

            mockMvc.perform(post("/api/v1/admin/assignments")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/admin/assignments/{id}/end")
    class EndAssignmentTests {

        @Test
        @DisplayName("Should end assignment and return 200 OK")
        @WithMockUser(roles = "ADMIN")
        void shouldEndAssignmentAndReturn200() throws Exception {
            EndAssignmentRequest request = new EndAssignmentRequest();
            request.setAssignedUntil(LocalDate.of(2024, 6, 30));

            AssignmentResponse response = createTestAssignmentResponse();
            response.setAssignedUntil(LocalDate.of(2024, 6, 30));
            response.setActive(false);

            when(assignmentService.endAssignment(eq(1L), any(EndAssignmentRequest.class))).thenReturn(response);

            mockMvc.perform(put("/api/v1/admin/assignments/1/end")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.isActive").value(false));

            verify(assignmentService).endAssignment(eq(1L), any(EndAssignmentRequest.class));
        }

        @Test
        @DisplayName("Should return 400 when assignment already ended")
        @WithMockUser(roles = "ADMIN")
        void shouldReturn400WhenAssignmentAlreadyEnded() throws Exception {
            EndAssignmentRequest request = new EndAssignmentRequest();
            request.setAssignedUntil(LocalDate.of(2024, 6, 30));

            when(assignmentService.endAssignment(eq(1L), any(EndAssignmentRequest.class)))
                    .thenThrow(new ApplicationException(HttpStatus.BAD_REQUEST, "Przydział został już zakończony"));

            mockMvc.perform(put("/api/v1/admin/assignments/1/end")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 400 when end date is before start date")
        @WithMockUser(roles = "ADMIN")
        void shouldReturn400WhenEndDateBeforeStartDate() throws Exception {
            EndAssignmentRequest request = new EndAssignmentRequest();
            request.setAssignedUntil(LocalDate.of(2024, 1, 1));

            when(assignmentService.endAssignment(eq(1L), any(EndAssignmentRequest.class)))
                    .thenThrow(new ApplicationException(HttpStatus.BAD_REQUEST, "Data zakończenia nie może być przed datą rozpoczęcia"));

            mockMvc.perform(put("/api/v1/admin/assignments/1/end")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 404 when assignment not found")
        @WithMockUser(roles = "ADMIN")
        void shouldReturn404WhenAssignmentNotFound() throws Exception {
            EndAssignmentRequest request = new EndAssignmentRequest();
            request.setAssignedUntil(LocalDate.of(2024, 6, 30));

            when(assignmentService.endAssignment(eq(999L), any(EndAssignmentRequest.class)))
                    .thenThrow(new ApplicationException(HttpStatus.NOT_FOUND, "Przydział nie został znaleziony"));

            mockMvc.perform(put("/api/v1/admin/assignments/999/end")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/admin/assignments")
    class GetAllAssignmentsTests {

        @Test
        @DisplayName("Should return paged assignments for admin")
        @WithMockUser(roles = "ADMIN")
        void shouldReturnPagedAssignmentsForAdmin() throws Exception {
            AssignmentResponse assignment = createTestAssignmentResponse();
            PagedResponse<AssignmentResponse> pagedResponse = new PagedResponse<>(
                    List.of(assignment), 0, 20, 1, 1, true
            );

            when(assignmentService.getAllAssignments(any(Pageable.class), any(), any(), any()))
                    .thenReturn(pagedResponse);

            mockMvc.perform(get("/api/v1/admin/assignments"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[0].id").value(1))
                    .andExpect(jsonPath("$.content[0].employeeFullName").value("Jan Kowalski"))
                    .andExpect(jsonPath("$.totalElements").value(1));
        }

        @Test
        @DisplayName("Should filter assignments by active status")
        @WithMockUser(roles = "ADMIN")
        void shouldFilterAssignmentsByActiveStatus() throws Exception {
            PagedResponse<AssignmentResponse> pagedResponse = new PagedResponse<>(
                    List.of(), 0, 20, 0, 0, true
            );

            when(assignmentService.getAllAssignments(any(Pageable.class), eq(true), any(), any()))
                    .thenReturn(pagedResponse);

            mockMvc.perform(get("/api/v1/admin/assignments")
                            .param("isActive", "true"))
                    .andExpect(status().isOk());

            verify(assignmentService).getAllAssignments(any(Pageable.class), eq(true), any(), any());
        }

        @Test
        @DisplayName("Should filter assignments by employee id")
        @WithMockUser(roles = "ADMIN")
        void shouldFilterAssignmentsByEmployeeId() throws Exception {
            PagedResponse<AssignmentResponse> pagedResponse = new PagedResponse<>(
                    List.of(), 0, 20, 0, 0, true
            );

            when(assignmentService.getAllAssignments(any(Pageable.class), any(), eq(1L), any()))
                    .thenReturn(pagedResponse);

            mockMvc.perform(get("/api/v1/admin/assignments")
                            .param("employeeId", "1"))
                    .andExpect(status().isOk());

            verify(assignmentService).getAllAssignments(any(Pageable.class), any(), eq(1L), any());
        }

        @Test
        @DisplayName("Should return 403 for non-admin user")
        @WithMockUser(roles = "EMPLOYEE")
        void shouldReturn403ForNonAdmin() throws Exception {
            mockMvc.perform(get("/api/v1/admin/assignments"))
                    .andExpect(status().isForbidden());

            verify(assignmentService, never()).getAllAssignments(any(), any(), any(), any());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/employee/assignments")
    class GetMyAssignmentHistoryTests {

        @Test
        @DisplayName("Should return assignment history for authenticated employee")
        @WithMockUser(username = "employee@example.com", roles = "EMPLOYEE")
        void shouldReturnAssignmentHistoryForEmployee() throws Exception {
            AssignmentResponse assignment = createTestAssignmentResponse();

            when(assignmentService.getAssignmentHistoryByEmployeeEmail("employee@example.com"))
                    .thenReturn(List.of(assignment));

            mockMvc.perform(get("/api/v1/employee/assignments"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].id").value(1))
                    .andExpect(jsonPath("$[0].employeeFullName").value("Jan Kowalski"));

            verify(assignmentService).getAssignmentHistoryByEmployeeEmail("employee@example.com");
        }

        @Test
        @DisplayName("Should return assignment history for admin")
        @WithMockUser(username = "admin@example.com", roles = "ADMIN")
        void shouldReturnAssignmentHistoryForAdmin() throws Exception {
            when(assignmentService.getAssignmentHistoryByEmployeeEmail("admin@example.com"))
                    .thenReturn(List.of());

            mockMvc.perform(get("/api/v1/employee/assignments"))
                    .andExpect(status().isOk());

            verify(assignmentService).getAssignmentHistoryByEmployeeEmail("admin@example.com");
        }

        @Test
        @DisplayName("Should return empty list when no assignments")
        @WithMockUser(username = "new@example.com", roles = "EMPLOYEE")
        void shouldReturnEmptyListWhenNoAssignments() throws Exception {
            when(assignmentService.getAssignmentHistoryByEmployeeEmail("new@example.com"))
                    .thenReturn(List.of());

            mockMvc.perform(get("/api/v1/employee/assignments"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$").isEmpty());
        }
    }
}

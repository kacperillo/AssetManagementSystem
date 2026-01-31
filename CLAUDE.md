# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Full-stack IT asset management system with Spring Boot 4 backend (Java 21) and React 19 frontend (TypeScript). Manages employees, hardware assets (laptops, phones, etc.), and asset assignments with role-based access control (ADMIN/EMPLOYEE).

## Commands

### Backend (Maven - from backend/ directory)
```bash
cd backend
mvn clean verify -DskipITs          # Run unit tests
mvn spring-boot:run                  # Start backend (port 8080)
mvn spring-boot:run -Dspring-boot.run.profiles=e2e  # Start with e2e profile
```

### Frontend (from frontend/ directory)
```bash
npm run dev              # Development server (port 5173)
npm run build            # Production build
npm run lint             # ESLint check
npm run test             # Unit tests (Vitest)
npm run test:coverage    # Unit tests with coverage
npm run e2e              # Playwright E2E tests (requires backend running)
npm run e2e:headed       # E2E tests with visible browser
```

### Docker
```bash
docker-compose up -d     # Start full stack (MySQL, backend, frontend)
```

## Architecture

### Backend Structure (backend/src/main/java/com/assetmanagement/)
- **controller/** - REST endpoints: AuthController, EmployeeController, AssetController, AssignmentController
- **service/** - Business logic with @Transactional operations
- **repository/** - Spring Data JPA with custom queries
- **model/** - JPA entities: Employee, Asset, Assignment, Role (enum), AssetType (enum)
- **dto/request/** and **dto/response/** - Validated request/response DTOs
- **security/** - JWT authentication (JwtUtil, JwtAuthenticationFilter, SecurityConfig)
- **exception/** - GlobalExceptionHandler with ApplicationException

### Frontend Structure (frontend/src/)
- **api/** - Axios clients with JWT interceptor (client.ts is the base instance)
- **pages/** - Route-based page components
- **components/** - Reusable UI: data tables, forms, feedback components
- **hooks/** - Custom React hooks
- **types/** - TypeScript type definitions

### Entity Relationships
```
Employee (1) <-- (Many) Assignment (Many) --> (1) Asset
```
- Only one active assignment per asset at a time (enforced in AssignmentService)
- Employees have roles: ADMIN (full access) or EMPLOYEE (own data only)
- Assets have isActive flag for soft deletion

## Key Patterns

### Authentication
- JWT tokens with Bearer prefix, 24-hour expiration
- Login: POST /api/v1/auth/login returns token
- All other endpoints require Authorization header
- Employee isolation: EMPLOYEE role sees only own data

### Testing
- Backend: JUnit 5 + Mockito, H2 in-memory database (application-test.yml)
- Frontend: Vitest for unit tests, Playwright for E2E
- E2E tests start both backend (port 8080, e2e profile) and frontend (port 5173)

### Error Handling
- Backend: GlobalExceptionHandler returns ErrorDetails DTO
- HTTP 400 validation errors, 401 unauthorized, 403 forbidden, 404 not found, 409 conflicts

## Configuration

- **Backend config:** backend/src/main/resources/application.yaml (MySQL, JWT secret)
- **Test config:** backend/src/main/resources/application-test.yml (H2 database)
- **Environment:** .env file for Docker (DB credentials, JWT secret)
- **API base URL:** http://localhost:8080/api/v1

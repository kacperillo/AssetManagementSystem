# REST API Plan - Asset Management System

## 1. Resources

| Resource | Database Table | Description |
|----------|---------------|-------------|
| Employees | `employees` | Organization employees who can be assigned assets |
| Assets | `assets` | IT equipment (laptops, smartphones, tablets, etc.) |
| Assignments | `assignments` | Asset-to-employee assignment records with time periods |
| Authentication | `employees` | Login and password management |

---

## 2. Endpoints

### 2.1 Authentication Endpoints

#### POST /api/v1/auth/login
**Description:** Authenticate user and obtain JWT token

**Access:** Public

**Request Body:**
```json
{
  "email": "string",
  "password": "string"
}
```

**Success Response (200 OK):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**Error Responses:**
| Code | Message |
|------|---------|
| 400 | Email is required |
| 400 | Email must be valid |
| 400 | Password is required |
| 401 | Invalid email or password |

---

#### POST /api/v1/auth/change-password
**Description:** Change user password

**Access:** Public (requires valid credentials in body)

**Request Body:**
```json
{
  "email": "string",
  "currentPassword": "string",
  "newPassword": "string"
}
```

**Success Response (200 OK):**
```json
{
  "message": "Password changed successfully"
}
```

**Error Responses:**
| Code | Message |
|------|---------|
| 400 | Email is required |
| 400 | Email must be valid |
| 400 | Current password is required |
| 400 | New password is required |
| 401 | Invalid current password |
| 404 | Employee not found |

---

### 2.2 Employee Endpoints

#### POST /api/v1/admin/employees
**Description:** Create a new employee

**Access:** ADMIN only

**Request Body:**
```json
{
  "fullName": "string",
  "email": "string",
  "password": "string",
  "role": "ADMIN | EMPLOYEE",
  "hiredFrom": "YYYY-MM-DD",
  "hiredUntil": "YYYY-MM-DD | null"
}
```

**Success Response (201 Created):**
```json
{
  "id": 1000,
  "fullName": "John Doe",
  "email": "john.doe@company.com",
  "role": "EMPLOYEE",
  "hiredFrom": "2024-01-15",
  "hiredUntil": null
}
```

**Error Responses:**
| Code | Message |
|------|---------|
| 400 | Full name is required |
| 400 | Email is required |
| 400 | Email must be valid |
| 400 | Password is required |
| 400 | Role is required |
| 400 | Hired from date is required |
| 401 | Unauthorized |
| 403 | Access denied |
| 409 | Employee with this email already exists |

---

#### GET /api/v1/admin/employees
**Description:** Retrieve all employees

**Access:** ADMIN only

**Query Parameters:** None

**Success Response (200 OK):**
```json
[
  {
    "id": 1000,
    "fullName": "John Doe",
    "email": "john.doe@company.com",
    "role": "EMPLOYEE",
    "hiredFrom": "2024-01-15",
    "hiredUntil": null
  },
  {
    "id": 1001,
    "fullName": "Jane Smith",
    "email": "jane.smith@company.com",
    "role": "ADMIN",
    "hiredFrom": "2023-06-01",
    "hiredUntil": null
  }
]
```

**Error Responses:**
| Code | Message |
|------|---------|
| 401 | Unauthorized |
| 403 | Access denied |

---

#### GET /api/v1/admin/employees/{id}
**Description:** Retrieve employee by ID

**Access:** ADMIN only

**Path Parameters:**
| Parameter | Type | Description |
|-----------|------|-------------|
| id | Long | Employee ID |

**Success Response (200 OK):**
```json
{
  "id": 1000,
  "fullName": "John Doe",
  "email": "john.doe@company.com",
  "role": "EMPLOYEE",
  "hiredFrom": "2024-01-15",
  "hiredUntil": null
}
```

**Error Responses:**
| Code | Message |
|------|---------|
| 401 | Unauthorized |
| 403 | Access denied |
| 404 | Employee not found |

---

### 2.3 Asset Endpoints

#### POST /api/v1/admin/assets
**Description:** Create a new asset

**Access:** ADMIN only

**Request Body:**
```json
{
  "assetType": "LAPTOP | SMARTPHONE | TABLET | PRINTER | HEADPHONES",
  "vendor": "string",
  "model": "string",
  "seriesNumber": "string"
}
```

**Success Response (201 Created):**
```json
{
  "id": 1000,
  "assetType": "LAPTOP",
  "vendor": "Dell",
  "model": "XPS 15",
  "seriesNumber": "SN-12345-ABCDE",
  "isActive": true,
  "assignedEmployeeId": null,
  "assignedEmployeeFullName": null,
  "assignedEmployeeEmail": null
}
```

**Error Responses:**
| Code | Message |
|------|---------|
| 400 | Asset type is required |
| 400 | Vendor is required |
| 400 | Model is required |
| 400 | Series number is required |
| 401 | Unauthorized |
| 403 | Access denied |
| 409 | Asset with this series number already exists |

---

#### GET /api/v1/admin/assets
**Description:** Retrieve all assets with pagination and sorting

**Access:** ADMIN only

**Query Parameters:**
| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| page | int | 0 | Page number (0-indexed) |
| size | int | 10 | Page size |
| sort | string | id,asc | Sort field and direction |

**Success Response (200 OK):**
```json
{
  "content": [
    {
      "id": 1000,
      "assetType": "LAPTOP",
      "vendor": "Dell",
      "model": "XPS 15",
      "seriesNumber": "SN-12345-ABCDE",
      "isActive": true,
      "assignedEmployeeId": 1000,
      "assignedEmployeeFullName": "John Doe",
      "assignedEmployeeEmail": "john.doe@company.com"
    }
  ],
  "page": 0,
  "size": 10,
  "totalElements": 50,
  "totalPages": 5,
  "last": false
}
```

**Error Responses:**
| Code | Message |
|------|---------|
| 401 | Unauthorized |
| 403 | Access denied |

---

#### GET /api/v1/admin/assets/{id}
**Description:** Retrieve asset by ID

**Access:** ADMIN only

**Path Parameters:**
| Parameter | Type | Description |
|-----------|------|-------------|
| id | Long | Asset ID |

**Success Response (200 OK):**
```json
{
  "id": 1000,
  "assetType": "LAPTOP",
  "vendor": "Dell",
  "model": "XPS 15",
  "seriesNumber": "SN-12345-ABCDE",
  "isActive": true,
  "assignedEmployeeId": 1000,
  "assignedEmployeeFullName": "John Doe",
  "assignedEmployeeEmail": "john.doe@company.com"
}
```

**Error Responses:**
| Code | Message |
|------|---------|
| 401 | Unauthorized |
| 403 | Access denied |
| 404 | Asset not found |

---

#### PUT /api/v1/admin/assets/{id}/deactivate
**Description:** Deactivate an asset (mark as inactive)

**Access:** ADMIN only

**Path Parameters:**
| Parameter | Type | Description |
|-----------|------|-------------|
| id | Long | Asset ID |

**Request Body:** None

**Success Response (200 OK):**
```json
{
  "id": 1000,
  "assetType": "LAPTOP",
  "vendor": "Dell",
  "model": "XPS 15",
  "seriesNumber": "SN-12345-ABCDE",
  "isActive": false,
  "assignedEmployeeId": null,
  "assignedEmployeeFullName": null,
  "assignedEmployeeEmail": null
}
```

**Error Responses:**
| Code | Message |
|------|---------|
| 401 | Unauthorized |
| 403 | Access denied |
| 404 | Asset not found |
| 409 | Cannot deactivate asset that is currently assigned |

---

#### GET /api/v1/employee/assets
**Description:** Get active assets assigned to the current authenticated employee

**Access:** ADMIN or EMPLOYEE

**Query Parameters:** None

**Success Response (200 OK):**
```json
[
  {
    "assetType": "LAPTOP",
    "vendor": "Dell",
    "model": "XPS 15",
    "seriesNumber": "SN-12345-ABCDE",
    "assignedFrom": "2024-01-15"
  },
  {
    "assetType": "SMARTPHONE",
    "vendor": "Apple",
    "model": "iPhone 15 Pro",
    "seriesNumber": "SN-67890-FGHIJ",
    "assignedFrom": "2024-02-01"
  }
]
```

**Error Responses:**
| Code | Message |
|------|---------|
| 401 | Unauthorized |
| 403 | Access denied |

---

### 2.4 Assignment Endpoints

#### POST /api/v1/admin/assignments
**Description:** Create a new asset assignment

**Access:** ADMIN only

**Request Body:**
```json
{
  "employeeId": 1000,
  "assetId": 1000,
  "assignedFrom": "YYYY-MM-DD"
}
```

**Success Response (201 Created):**
```json
{
  "id": 1000,
  "assetId": 1000,
  "assetType": "LAPTOP",
  "vendor": "Dell",
  "model": "XPS 15",
  "seriesNumber": "SN-12345-ABCDE",
  "employeeId": 1000,
  "employeeFullName": "John Doe",
  "assignedFrom": "2024-01-15",
  "assignedUntil": null,
  "isActive": true
}
```

**Error Responses:**
| Code | Message |
|------|---------|
| 400 | Employee ID is required |
| 400 | Asset ID is required |
| 400 | Assigned from date is required |
| 401 | Unauthorized |
| 403 | Access denied |
| 404 | Employee not found |
| 404 | Asset not found |
| 409 | Asset is not active |
| 409 | Asset is already assigned to another employee |

---

#### PUT /api/v1/admin/assignments/{id}/end
**Description:** End an active assignment

**Access:** ADMIN only

**Path Parameters:**
| Parameter | Type | Description |
|-----------|------|-------------|
| id | Long | Assignment ID |

**Request Body:**
```json
{
  "assignedUntil": "YYYY-MM-DD"
}
```

**Success Response (200 OK):**
```json
{
  "id": 1000,
  "assetId": 1000,
  "assetType": "LAPTOP",
  "vendor": "Dell",
  "model": "XPS 15",
  "seriesNumber": "SN-12345-ABCDE",
  "employeeId": 1000,
  "employeeFullName": "John Doe",
  "assignedFrom": "2024-01-15",
  "assignedUntil": "2024-06-30",
  "isActive": false
}
```

**Error Responses:**
| Code | Message |
|------|---------|
| 400 | Assigned until date is required |
| 401 | Unauthorized |
| 403 | Access denied |
| 404 | Assignment not found |
| 409 | Assignment is already ended |

---

#### GET /api/v1/admin/assignments
**Description:** Retrieve all assignments with optional filters

**Access:** ADMIN only

**Query Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| page | int | No | Page number (default: 0) |
| size | int | No | Page size (default: 10) |
| sort | string | No | Sort field and direction (default: id,asc) |
| employeeId | Long | No | Filter by employee ID |
| assetId | Long | No | Filter by asset ID |

**Success Response (200 OK):**

*When no filters are applied:*
```json
{
  "content": [
    {
      "id": 1000,
      "assetId": 1000,
      "assetType": "LAPTOP",
      "vendor": "Dell",
      "model": "XPS 15",
      "seriesNumber": "SN-12345-ABCDE",
      "employeeId": 1000,
      "employeeFullName": "John Doe",
      "assignedFrom": "2024-01-15",
      "assignedUntil": null,
      "isActive": true
    }
  ],
  "page": 0,
  "size": 10,
  "totalElements": 25,
  "totalPages": 3,
  "last": false
}
```

*When employeeId or assetId filter is applied:*
```json
[
  {
    "id": 1000,
    "assetId": 1000,
    "assetType": "LAPTOP",
    "vendor": "Dell",
    "model": "XPS 15",
    "seriesNumber": "SN-12345-ABCDE",
    "employeeId": 1000,
    "employeeFullName": "John Doe",
    "assignedFrom": "2024-01-15",
    "assignedUntil": null,
    "isActive": true
  }
]
```

**Error Responses:**
| Code | Message |
|------|---------|
| 401 | Unauthorized |
| 403 | Access denied |

---

#### GET /api/v1/employee/assignments
**Description:** Get assignment history for the current authenticated employee

**Access:** ADMIN or EMPLOYEE

**Query Parameters:** None

**Success Response (200 OK):**
```json
[
  {
    "id": 1000,
    "assetId": 1000,
    "assetType": "LAPTOP",
    "vendor": "Dell",
    "model": "XPS 15",
    "seriesNumber": "SN-12345-ABCDE",
    "employeeId": 1000,
    "employeeFullName": "John Doe",
    "assignedFrom": "2024-01-15",
    "assignedUntil": null,
    "isActive": true
  },
  {
    "id": 999,
    "assetId": 1001,
    "assetType": "SMARTPHONE",
    "vendor": "Samsung",
    "model": "Galaxy S23",
    "seriesNumber": "SN-99999-XXXXX",
    "employeeId": 1000,
    "employeeFullName": "John Doe",
    "assignedFrom": "2023-06-01",
    "assignedUntil": "2024-01-14",
    "isActive": false
  }
]
```

**Error Responses:**
| Code | Message |
|------|---------|
| 401 | Unauthorized |
| 403 | Access denied |

---

## 3. Authentication and Authorization

### 3.1 Authentication Mechanism

The API uses **JWT (JSON Web Token)** based authentication with the following characteristics:

| Property | Value |
|----------|-------|
| Token Type | Bearer Token |
| Algorithm | HMAC-SHA256 |
| Token Expiration | 24 hours (86400000 ms) |
| Session Management | Stateless |

### 3.2 Authentication Flow

1. **Login Request:** Client sends credentials to `POST /api/v1/auth/login`
2. **Token Generation:** Server validates credentials and returns JWT token
3. **Authenticated Requests:** Client includes token in `Authorization` header:
   ```
   Authorization: Bearer <jwt_token>
   ```
4. **Token Validation:** Server validates token on each request via `JwtAuthenticationFilter`

### 3.3 JWT Token Structure

```json
{
  "sub": "user@email.com",
  "iat": 1704067200,
  "exp": 1704153600
}
```

### 3.4 Role-Based Access Control (RBAC)

| Role | Description | Permissions |
|------|-------------|-------------|
| ADMIN | System administrator | Full access to all endpoints |
| EMPLOYEE | Regular employee | Access to `/api/v1/employee/**` endpoints only |

### 3.5 Endpoint Security Rules

| URL Pattern | Access Rule |
|-------------|-------------|
| `/api/v1/auth/**` | Public (no authentication required) |
| `/api/v1/admin/**` | ADMIN role required |
| `/api/v1/employee/**` | ADMIN or EMPLOYEE role required |
| All other endpoints | Authenticated user required |

### 3.6 Security Implementation Details

- **Password Storage:** BCrypt hashing algorithm
- **CSRF Protection:** Disabled (stateless API)
- **CORS:** Configured for `http://localhost:3000`
- **Access Denied Response:** Custom JSON response with 403 status

### 3.7 Error Response Format

```json
{
  "timeStamp": "2024-01-15T10:30:00",
  "code": "Forbidden",
  "message": "Access denied. You don't have permission to access this resource."
}
```

---

## 4. Validation and Business Logic

### 4.1 Employee Validation Rules

| Field | Validation Rules |
|-------|-----------------|
| fullName | Required, not blank |
| email | Required, valid email format, unique in system |
| password | Required, not blank, stored as BCrypt hash |
| role | Required, must be ADMIN or EMPLOYEE |
| hiredFrom | Required, valid date |
| hiredUntil | Optional, valid date or null |

**Business Logic:**
- Email uniqueness is validated before creation
- Passwords are hashed using BCryptPasswordEncoder before storage
- Employee with `hiredUntil = null` is considered an active employee

---

### 4.2 Asset Validation Rules

| Field | Validation Rules |
|-------|-----------------|
| assetType | Required, must be one of: LAPTOP, SMARTPHONE, TABLET, PRINTER, HEADPHONES |
| vendor | Required, not blank |
| model | Required, not blank |
| seriesNumber | Required, not blank, unique in system |
| isActive | Defaults to true on creation |

**Business Logic:**
- Series number uniqueness is validated before creation
- Asset can only be deactivated if not currently assigned to any employee
- Only active assets can be assigned to employees
- Asset response includes current assignment information (if any)

---

### 4.3 Assignment Validation Rules

| Field | Validation Rules |
|-------|-----------------|
| employeeId | Required, must reference existing employee |
| assetId | Required, must reference existing active asset |
| assignedFrom | Required, valid date |
| assignedUntil | Optional, valid date or null (null = active assignment) |

**Business Logic:**

1. **Assignment Creation:**
   - Employee must exist in the system
   - Asset must exist in the system
   - Asset must be active (`isActive = true`)
   - Asset cannot have an existing active assignment (no double assignment)
   - An active assignment is one where `assignedUntil IS NULL`

2. **Assignment Termination:**
   - Assignment must exist
   - Assignment cannot already be ended (`assignedUntil` must be null)
   - Sets `assignedUntil` to provided date

3. **Active Assignment Logic:**
   - Assignment is considered active when `assignedUntil IS NULL`
   - `isActive` field in response is calculated based on current date and `assignedUntil`

---

### 4.4 Authentication Validation Rules

**Login:**
| Field | Validation Rules |
|-------|-----------------|
| email | Required, valid email format |
| password | Required, not blank |

**Change Password:**
| Field | Validation Rules |
|-------|-----------------|
| email | Required, valid email format, must exist in system |
| currentPassword | Required, must match stored password hash |
| newPassword | Required, not blank |

---

### 4.5 Global Error Response Format

All validation and business logic errors return a consistent format:

```json
{
  "timeStamp": "2024-01-15T10:30:00",
  "code": "Bad Request",
  "message": "Detailed error message"
}
```

### 4.6 HTTP Status Code Summary

| Code | Usage |
|------|-------|
| 200 OK | Successful GET, PUT requests |
| 201 Created | Successful POST (resource creation) |
| 400 Bad Request | Validation errors |
| 401 Unauthorized | Invalid or missing authentication |
| 403 Forbidden | Insufficient permissions |
| 404 Not Found | Resource not found |
| 409 Conflict | Business rule violation (duplicate, invalid state) |
| 500 Internal Server Error | Unexpected server errors |

---

## 5. Data Types Reference

### 5.1 Enumerations

**Role:**
```
ADMIN | EMPLOYEE
```

**AssetType:**
```
LAPTOP | SMARTPHONE | TABLET | PRINTER | HEADPHONES
```

### 5.2 Date Format

All dates use ISO 8601 format: `YYYY-MM-DD`

Example: `2024-01-15`

### 5.3 DateTime Format

Timestamps use ISO 8601 format: `YYYY-MM-DDTHH:mm:ss`

Example: `2024-01-15T10:30:00`

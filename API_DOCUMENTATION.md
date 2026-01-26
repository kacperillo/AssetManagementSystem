# AssetManagement System - API Documentation

## Przegląd

AssetManagement System to REST API do zarządzania zasobami firmowymi (laptopy, smartfony, tablety, drukarki, słuchawki) oraz ich przydziałami pracownikom.

## Technologie

- **Backend**: Spring Boot 4.0.1, Java 21
- **Baza danych**: MySQL 8.0+
- **Bezpieczeństwo**: Spring Security + JWT
- **ORM**: Spring Data JPA / Hibernate
- **Walidacja**: Jakarta Validation

## Wymagania

- Java 21+
- Maven 3.6+
- MySQL 8.0+

## Konfiguracja bazy danych

1. Uruchom MySQL Server
2. Aplikacja automatycznie utworzy bazę danych `assetmanagement` przy pierwszym uruchomieniu
3. Możesz zmienić dane dostępowe w `src/main/resources/application.yaml`:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/assetmanagement?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC
    username: root
    password: root
```

## Uruchomienie aplikacji

```bash
# Sklonuj repozytorium lub przejdź do katalogu projektu
cd AssetManagement

# Skompiluj projekt
mvn clean install

# Uruchom aplikację
mvn spring-boot:run
```

Aplikacja będzie dostępna pod adresem: `http://localhost:8080`

## Endpointy API

### Autentykacja

#### POST /api/v1/auth/login
Logowanie użytkownika (Administrator lub Pracownik)

**Request Body:**
```json
{
  "email": "admin@example.com",
  "password": "password123"
}
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "email": "admin@example.com",
  "fullName": "Jan Kowalski",
  "role": "ADMIN"
}
```

**Uwaga**: Token należy dołączyć do nagłówka Authorization jako `Bearer {token}` w kolejnych zapytaniach.

#### POST /api/v1/auth/change-password
Zmiana hasła użytkownika

**Request Body:**
```json
{
  "email": "jan.kowalski@example.com",
  "currentPassword": "oldPassword123",
  "newPassword": "newSecurePassword456"
}
```

**Response:** (204 NO CONTENT)

Brak treści odpowiedzi w przypadku sukcesu.

**Możliwe błędy:**
- `400 BAD REQUEST` - "Invalid email or password" - nieprawidłowy email lub aktualne hasło

---

### Zarządzanie Pracownikami (ADMIN)

#### POST /api/v1/admin/employees
Dodawanie nowego pracownika

**Headers:** `Authorization: Bearer {token}`

**Request Body:**
```json
{
  "fullName": "Jan Kowalski",
  "email": "jan.kowalski@example.com",
  "password": "securePassword123",
  "role": "EMPLOYEE",
  "hiredFrom": "2024-01-15",
  "hiredUntil": null
}
```

**Response:** (201 CREATED)
```json
{
  "id": 1,
  "fullName": "Jan Kowalski",
  "email": "jan.kowalski@example.com",
  "role": "EMPLOYEE",
  "hiredFrom": "2024-01-15",
  "hiredUntil": null
}
```

#### GET /api/v1/admin/employees
Wyświetlanie wszystkich pracowników

**Headers:** `Authorization: Bearer {token}`

**Response:**
```json
[
  {
    "id": 1,
    "fullName": "Jan Kowalski",
    "email": "jan.kowalski@example.com",
    "role": "EMPLOYEE",
    "hiredFrom": "2024-01-15",
    "hiredUntil": null
  },
  {
    "id": 2,
    "fullName": "Anna Nowak",
    "email": "anna.nowak@example.com",
    "role": "ADMIN",
    "hiredFrom": "2023-06-01",
    "hiredUntil": null
  }
]
```

#### GET /api/v1/admin/employees/{id}
Wyświetlanie pojedynczego pracownika

**Headers:** `Authorization: Bearer {token}`

---

### Zarządzanie Zasobami (ADMIN)

#### POST /api/v1/admin/assets
Dodawanie nowego zasobu

**Headers:** `Authorization: Bearer {token}`

**Request Body:**
```json
{
  "assetType": "LAPTOP",
  "vendor": "Dell",
  "model": "Latitude 7420",
  "seriesNumber": "SN123456789"
}
```

**Dostępne typy zasobów:**
- `LAPTOP`
- `SMARTPHONE`
- `TABLET`
- `PRINTER`
- `HEADPHONES`

**Response:** (201 CREATED)
```json
{
  "id": 1,
  "assetType": "LAPTOP",
  "vendor": "Dell",
  "model": "Latitude 7420",
  "seriesNumber": "SN123456789",
  "isActive": true
}
```

#### GET /api/v1/admin/assets
Wyświetlanie wszystkich zasobów

**Headers:** `Authorization: Bearer {token}`

**Response:**
```json
[
  {
    "id": 1,
    "assetType": "LAPTOP",
    "vendor": "Dell",
    "model": "Latitude 7420",
    "seriesNumber": "SN123456789",
    "isActive": true
  },
  {
    "id": 2,
    "assetType": "SMARTPHONE",
    "vendor": "Apple",
    "model": "iPhone 13",
    "seriesNumber": "SN987654321",
    "isActive": false
  }
]
```

#### GET /api/v1/admin/assets/{id}
Wyświetlanie pojedynczego zasobu

**Headers:** `Authorization: Bearer {token}`

#### PUT /api/v1/admin/assets/{id}/deactivate
Oznaczanie zasobu jako nieaktywny

**Headers:** `Authorization: Bearer {token}`

**Response:** (204 NO CONTENT)

**Uwaga**: Nie można dezaktywować zasobu, który jest obecnie przydzielony pracownikowi.

---

### Zarządzanie Przydziałami (ADMIN)

#### POST /api/v1/admin/assignments
Tworzenie przydziału zasobu do pracownika

**Headers:** `Authorization: Bearer {token}`

**Request Body:**
```json
{
  "employeeId": 1,
  "assetId": 2,
  "assignedFrom": "2024-01-20"
}
```

**Response:** (201 CREATED)
```json
{
  "id": 1,
  "assetId": 2,
  "assetType": "LAPTOP",
  "vendor": "Dell",
  "model": "Latitude 7420",
  "seriesNumber": "SN123456789",
  "employeeId": 1,
  "employeeFullName": "Jan Kowalski",
  "assignedFrom": "2024-01-20",
  "assignedUntil": null,
  "isActive": true
}
```

#### PUT /api/v1/admin/assignments/{id}/end
Kończenie przydziału

**Headers:** `Authorization: Bearer {token}`

**Request Body:**
```json
{
  "assignedUntil": "2024-06-15"
}
```

**Response:**
```json
{
  "id": 1,
  "assetId": 2,
  "assetType": "LAPTOP",
  "vendor": "Dell",
  "model": "Latitude 7420",
  "seriesNumber": "SN123456789",
  "employeeId": 1,
  "employeeFullName": "Jan Kowalski",
  "assignedFrom": "2024-01-20",
  "assignedUntil": "2024-06-15",
  "isActive": false
}
```

#### GET /api/v1/admin/assignments
Wyświetlanie całej historii przydziałów

**Headers:** `Authorization: Bearer {token}`

**Query Parameters (opcjonalne):**
- `employeeId` - filtrowanie po ID pracownika
- `assetId` - filtrowanie po ID zasobu

**Przykłady:**
- `/api/v1/admin/assignments` - wszystkie przydziały
- `/api/v1/admin/assignments?employeeId=1` - przydziały dla pracownika o ID=1
- `/api/v1/admin/assignments?assetId=2` - przydziały dla zasobu o ID=2

**Response:**
```json
[
  {
    "id": 1,
    "assetId": 2,
    "assetType": "LAPTOP",
    "vendor": "Dell",
    "model": "Latitude 7420",
    "seriesNumber": "SN123456789",
    "employeeId": 1,
    "employeeFullName": "Jan Kowalski",
    "assignedFrom": "2024-01-20",
    "assignedUntil": "2024-06-15",
    "isActive": false
  },
  {
    "id": 2,
    "assetId": 3,
    "assetType": "SMARTPHONE",
    "vendor": "Apple",
    "model": "iPhone 13",
    "seriesNumber": "SN555666777",
    "employeeId": 1,
    "employeeFullName": "Jan Kowalski",
    "assignedFrom": "2024-01-22",
    "assignedUntil": null,
    "isActive": true
  }
]
```

---

### Funkcje Pracownika (EMPLOYEE)

#### GET /api/v1/employee/assets
Wyświetlanie aktywnych zasobów zalogowanego pracownika

**Headers:** `Authorization: Bearer {token}`

**Response:**
```json
[
  {
    "assetType": "LAPTOP",
    "vendor": "Dell",
    "model": "Latitude 7420",
    "seriesNumber": "SN123456789",
    "assignedFrom": "2024-01-20"
  },
  {
    "assetType": "SMARTPHONE",
    "vendor": "Apple",
    "model": "iPhone 13",
    "seriesNumber": "SN555666777",
    "assignedFrom": "2024-01-22"
  }
]
```

#### GET /api/v1/employee/assignments
Wyświetlanie historii wszystkich przydziałów zalogowanego pracownika

**Headers:** `Authorization: Bearer {token}`

**Response:**
```json
[
  {
    "id": 1,
    "assetId": 2,
    "assetType": "LAPTOP",
    "vendor": "Dell",
    "model": "Latitude 7420",
    "seriesNumber": "SN123456789",
    "employeeId": 1,
    "employeeFullName": "Jan Kowalski",
    "assignedFrom": "2024-01-20",
    "assignedUntil": "2024-06-15",
    "isActive": false
  },
  {
    "id": 2,
    "assetType": "SMARTPHONE",
    "vendor": "Apple",
    "model": "iPhone 13",
    "seriesNumber": "SN555666777",
    "employeeId": 1,
    "employeeFullName": "Jan Kowalski",
    "assignedFrom": "2024-01-22",
    "assignedUntil": null,
    "isActive": true
  }
]
```

---

## Obsługa błędów

Wszystkie błędy zwracane są w formacie JSON:

```json
{
  "timeStamp": "2024-01-22",
  "httpStatusCode": 400,
  "message": "Email is already in use"
}
```

### Kody błędów

- `400 BAD REQUEST` - Błędne dane wejściowe, naruszenie reguł biznesowych
- `401 UNAUTHORIZED` - Nieprawidłowe dane logowania
- `403 FORBIDDEN` - Brak uprawnień do zasobu
- `404 NOT FOUND` - Zasób nie został znaleziony
- `500 INTERNAL SERVER ERROR` - Błąd serwera

### Przykładowe komunikaty błędów

- "Email is already in use" - Email pracownika już istnieje
- "Series number is already in use" - Numer seryjny zasobu już istnieje
- "Invalid email or password" - Nieprawidłowe dane logowania
- "Employee not found" - Pracownik o podanym ID nie istnieje
- "Asset not found" - Zasób o podanym ID nie istnieje
- "Cannot deactivate asset that is currently assigned to an employee" - Próba dezaktywacji przydzielonego zasobu
- "Asset is already assigned to another employee" - Próba przydzielenia zasobu, który jest już przydzielony
- "Cannot assign inactive asset" - Próba przydzielenia nieaktywnego zasobu
- "Assignment is already ended" - Próba zakończenia już zakończonego przydziału

---

## Role i uprawnienia

### ADMIN
- Zarządzanie pracownikami (dodawanie, przeglądanie)
- Zarządzanie zasobami (dodawanie, przeglądanie, dezaktywacja)
- Zarządzanie przydziałami (tworzenie, kończenie, przeglądanie z filtrowaniem)
- Dostęp do zasobów i przydziałów dowolnego pracownika przez filtrowanie w endpointach `/admin`

### EMPLOYEE
- Przeglądanie własnych aktywnych zasobów
- Przeglądanie historii własnych przydziałów
- Brak dostępu do danych innych pracowników
- Brak możliwości zarządzania zasobami i przydziałami

---

## Reguły biznesowe

1. **Unikalność emaila** - Każdy pracownik musi mieć unikalny adres email
2. **Unikalność numeru seryjnego** - Każdy zasób musi mieć unikalny numer seryjny
3. **Przydzielenie zasobu** - Zasób może być przydzielony tylko jednej osobie jednocześnie
4. **Dezaktywacja zasobu** - Nie można dezaktywować zasobu, który jest obecnie przydzielony
5. **Aktywność zasobu** - Można przydzielić tylko aktywny zasób
6. **Kończenie przydziału** - Nie można zakończyć już zakończonego przydziału
7. **Kontrola dostępu** - Pracownik ma dostęp tylko do swoich zasobów i historii

---

## Testowanie API

### Przykładowy przepływ pracy

1. **Logowanie jako administrator:**
```bash
POST /api/v1/auth/login
{
  "email": "admin@example.com",
  "password": "admin123"
}
```

2. **Dodanie pracownika:**
```bash
POST /api/v1/admin/employees
Authorization: Bearer {token}
{
  "fullName": "Jan Kowalski",
  "email": "jan.kowalski@example.com",
  "password": "password123",
  "role": "EMPLOYEE",
  "hiredFrom": "2024-01-15"
}
```

3. **Dodanie zasobu:**
```bash
POST /api/v1/admin/assets
Authorization: Bearer {token}
{
  "assetType": "LAPTOP",
  "vendor": "Dell",
  "model": "Latitude 7420",
  "seriesNumber": "SN123456789"
}
```

4. **Przydzielenie zasobu pracownikowi:**
```bash
POST /api/v1/admin/assignments
Authorization: Bearer {token}
{
  "employeeId": 1,
  "assetId": 1,
  "assignedFrom": "2024-01-20"
}
```

5. **Logowanie jako pracownik:**
```bash
POST /api/v1/auth/login
{
  "email": "jan.kowalski@example.com",
  "password": "password123"
}
```

6. **Przeglądanie swoich aktywnych zasobów:**
```bash
GET /api/v1/employee/assets
Authorization: Bearer {token}
```

---

## Kontakt i wsparcie

W razie pytań lub problemów, proszę o kontakt poprzez system zgłoszeń projektu.

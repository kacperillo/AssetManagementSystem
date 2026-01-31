# AssetManagement System

System do zarządzania zasobami firmowymi (sprzęt IT) oraz ich przydziałami pracownikom.

## Opis projektu

AssetManagement to aplikacja webowa REST API zbudowana w Spring Boot, która umożliwia:
- Zarządzanie pracownikami (Administrator)
- Zarządzanie zasobami IT (laptopy, smartfony, tablety, drukarki, słuchawki)
- Przydzielanie zasobów pracownikom z pełną historią
- Dostęp pracowników do swoich przydzielonych zasobów
- Uwierzytelnianie JWT z kontrolą dostępu opartą na rolach

## Technologie

- **Java**: 21
- **Spring Boot**: 4.0.1
- **Spring Security**: JWT Authentication
- **Spring Data JPA**: ORM / Hibernate
- **MySQL**: 8.0+
- **Maven**: Zarządzanie zależnościami
- **Lombok**: Redukcja boilerplate code

## Wymagania

Przed uruchomieniem aplikacji upewnij się, że masz zainstalowane:
- Java Development Kit (JDK) 21 lub nowszy
- Maven 3.6 lub nowszy
- MySQL Server 8.0 lub nowszy

## Instalacja i uruchomienie

### 1. Sklonuj repozytorium

```bash
git clone <repository-url>
cd AssetManagement
```

### 2. Konfiguracja bazy danych

Uruchom MySQL Server i upewnij się, że działa na porcie 3306.

Aplikacja automatycznie utworzy bazę danych `assetmanagement` przy pierwszym uruchomieniu.

Jeśli potrzebujesz zmienić dane dostępowe do bazy, edytuj plik:
```
backend/src/main/resources/application.yaml
```

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/assetmanagement?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC
    username: root  # Zmień na swoje dane
    password: root  # Zmień na swoje hasło
```

### 3. Kompilacja projektu

```bash
cd backend
mvn clean install
```

### 4. Uruchomienie aplikacji

```bash
cd backend
mvn spring-boot:run
```

Aplikacja będzie dostępna pod adresem: **http://localhost:8080**

## Testowanie API

### Domyślni użytkownicy

Po pierwszym uruchomieniu możesz zalogować się używając:

**Administrator:**
- Email: `admin@example.com`
- Hasło: `admin123`

**Pracownik:**
- Email: `jan.kowalski@example.com`
- Hasło: `password123`

### Przykładowe żądania

#### 1. Logowanie (Admin)

```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@example.com",
    "password": "admin123"
  }'
```

**Odpowiedź:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "email": "admin@example.com",
  "fullName": "Administrator",
  "role": "ADMIN"
}
```

#### 2. Dodawanie pracownika (Admin)

```bash
curl -X POST http://localhost:8080/api/v1/admin/employees \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN_HERE" \
  -d '{
    "fullName": "Anna Nowak",
    "email": "anna.nowak@example.com",
    "password": "password123",
    "role": "EMPLOYEE",
    "hiredFrom": "2024-01-15"
  }'
```

#### 3. Dodawanie zasobu (Admin)

```bash
curl -X POST http://localhost:8080/api/v1/admin/assets \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN_HERE" \
  -d '{
    "assetType": "LAPTOP",
    "vendor": "Dell",
    "model": "Latitude 7420",
    "seriesNumber": "SN123456789"
  }'
```

#### 4. Tworzenie przydziału (Admin)

```bash
curl -X POST http://localhost:8080/api/v1/admin/assignments \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN_HERE" \
  -d '{
    "employeeId": 2,
    "assetId": 1,
    "assignedFrom": "2024-01-20"
  }'
```

#### 5. Wyświetlanie swoich aktywnych zasobów (Pracownik)

```bash
curl -X GET http://localhost:8080/api/v1/employee/assets \
  -H "Authorization: Bearer EMPLOYEE_TOKEN_HERE"
```

## Dokumentacja API

Pełna dokumentacja API znajduje się w pliku [API_DOCUMENTATION.md](API_DOCUMENTATION.md)

### Główne endpointy

**Autentykacja:**
- `POST /api/v1/auth/login` - Logowanie użytkownika
- `POST /api/v1/auth/change-password` - Zmiana hasła

**Zarządzanie pracownikami (ADMIN):**
- `POST /api/v1/admin/employees` - Dodawanie pracownika
- `GET /api/v1/admin/employees` - Lista wszystkich pracowników
- `GET /api/v1/admin/employees/{id}` - Szczegóły pracownika

**Zarządzanie zasobami (ADMIN):**
- `POST /api/v1/admin/assets` - Dodawanie zasobu
- `GET /api/v1/admin/assets` - Lista wszystkich zasobów (z paginacją i filtrowaniem)
- `GET /api/v1/admin/assets/{id}` - Szczegóły zasobu
- `PUT /api/v1/admin/assets/{id}/deactivate` - Dezaktywacja zasobu

**Zarządzanie przydziałami (ADMIN):**
- `POST /api/v1/admin/assignments` - Tworzenie przydziału
- `PUT /api/v1/admin/assignments/{id}/end` - Kończenie przydziału
- `GET /api/v1/admin/assignments` - Historia przydziałów (z paginacją i filtrowaniem)

**Funkcje pracownika (EMPLOYEE/ADMIN):**
- `GET /api/v1/employee/assets` - Aktywne zasoby pracownika
- `GET /api/v1/employee/assignments` - Historia przydziałów pracownika

## Struktura projektu

```
AssetManagement/
├── backend/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/assetmanagement/
│   │   │   │   ├── controller/        # REST Controllers
│   │   │   │   ├── dto/               # Data Transfer Objects
│   │   │   │   │   ├── request/       # Request DTOs
│   │   │   │   │   └── response/      # Response DTOs
│   │   │   │   ├── exception/         # Exception handling
│   │   │   │   ├── model/             # JPA Entities
│   │   │   │   ├── repository/        # Data access layer
│   │   │   │   ├── security/          # Security configuration & JWT
│   │   │   │   └── service/           # Business logic
│   │   │   └── resources/
│   │   │       ├── application.yaml   # Application configuration
│   │   │       └── data.sql          # Sample data
│   │   └── test/                      # Unit tests
│   └── pom.xml                        # Maven configuration
├── frontend/                          # React application
├── README.md                          # This file
└── API_DOCUMENTATION.md               # API documentation
```

## Bezpieczeństwo

- Hasła są szyfrowane przy użyciu BCrypt
- Autentykacja oparta na JWT tokenach
- Kontrola dostępu oparta na rolach (RBAC)
- CORS skonfigurowany dla localhost:3000 (React frontend)
- Sesje stateless

## Role użytkowników

### ADMIN
- Pełny dostęp do zarządzania pracownikami
- Zarządzanie zasobami
- Zarządzanie przydziałami
- Dostęp do całej historii przydziałów

### EMPLOYEE
- Dostęp tylko do własnych aktywnych zasobów
- Przeglądanie historii własnych przydziałów
- Brak dostępu do danych innych pracowników

## Reguły biznesowe

1. **Unikalność emaila** - Każdy pracownik musi mieć unikalny adres email
2. **Unikalność numeru seryjnego** - Każdy zasób musi mieć unikalny numer seryjny
3. **Jedno przydzielenie** - Zasób może być przydzielony tylko jednej osobie jednocześnie
4. **Dezaktywacja** - Nie można dezaktywować zasobu, który jest przydzielony
5. **Aktywność zasobu** - Można przydzielić tylko aktywne zasoby

## Rozwijanie aplikacji

### Dodawanie nowych funkcji

1. Dodaj nową encję w pakiecie `model`
2. Stwórz repository w `repository`
3. Zaimplementuj logikę biznesową w `service`
4. Dodaj DTOs w `dto/request` i `dto/response`
5. Stwórz controller w `controller`

### Uruchamianie testów

```bash
cd backend
mvn test
```

### Budowanie JAR

```bash
cd backend
mvn clean package
```

Plik JAR będzie dostępny w `backend/target/assetmanagement-0.0.1.jar`

### Uruchomienie aplikacji z JAR

```bash
java -jar backend/target/assetmanagement-0.0.1.jar
```

## Troubleshooting

### Problem: Aplikacja nie może połączyć się z bazą danych

**Rozwiązanie:**
- Upewnij się, że MySQL Server jest uruchomiony
- Sprawdź dane dostępowe w `backend/src/main/resources/application.yaml`
- Sprawdź, czy port 3306 jest dostępny

### Problem: Błąd kompilacji Maven

**Rozwiązanie:**
```bash
cd backend
mvn clean install -U
```

### Problem: JWT token wygasł

**Rozwiązanie:**
- Zaloguj się ponownie, aby otrzymać nowy token
- Domyślny czas wygaśnięcia: 24 godziny (86400000 ms)

## Licencja

Ten projekt został stworzony jako MVP (Minimum Viable Product) dla systemu zarządzania zasobami.

## Kontakt

W razie pytań lub problemów, proszę o kontakt poprzez system zgłoszeń projektu.

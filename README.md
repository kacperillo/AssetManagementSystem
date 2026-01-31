# Asset Management System

System do zarządzania zasobami firmowymi (sprzęt IT) oraz ich przydziałami pracownikom.

## O projekcie

AssetManagement to pełna aplikacja webowa umożliwiająca:
- Zarządzanie pracownikami i ich rolami (Administrator/Pracownik)
- Zarządzanie zasobami IT (laptopy, smartfony, tablety, drukarki, słuchawki)
- Przydzielanie zasobów pracownikom z pełną historią przydziałów
- Kontrolę dostępu opartą na rolach (RBAC)

## Stack technologiczny

| Warstwa | Technologie |
|---------|-------------|
| **Backend** | Java 21, Spring Boot 4, Spring Security, Spring Data JPA, JWT |
| **Frontend** | React 19, TypeScript, Material-UI 7, React Query, Vite |
| **Baza danych** | MySQL 8.0 |
| **Testy** | JUnit 5, Mockito (backend), Vitest, Playwright (frontend) |
| **Konteneryzacja** | Docker, Docker Compose |

## Struktura projektu

```
AssetManagement/
├── backend/                 # Spring Boot REST API
│   ├── src/
│   ├── pom.xml
│   ├── Dockerfile
│   └── README.md            # Szczegółowa dokumentacja backendu
├── frontend/                # React SPA
│   ├── src/
│   ├── package.json
│   ├── Dockerfile
│   └── README.md
├── .ai/                     # Dokumentacja projektowa
│   ├── prd.md               # Wymagania produktu
│   ├── api-plan.md          # Dokumentacja API
│   ├── db-plan.md           # Schemat bazy danych
│   ├── ui-plan.md           # Architektura UI
│   └── tech-stack.md        # Stack technologiczny
├── docker-compose.yml       # Orkiestracja kontenerów
├── .env                     # Zmienne środowiskowe
└── README.md                # Ten plik
```

---

## Szybki start z Docker (zalecane)

Najprostszy sposób uruchomienia całej aplikacji.

### Wymagania
- Docker
- Docker Compose

### Uruchomienie

```bash
# 1. Sklonuj repozytorium
git clone https://github.com/kacperillo/AssetManagementSystem
cd AssetManagementSystem

# 2. Uruchom wszystkie serwisy
docker-compose up -d

# 3. Sprawdź status kontenerów
docker-compose ps
```

### Dostęp do aplikacji

| Serwis | URL |
|--------|-----|
| Frontend | http://localhost |
| Backend API | http://localhost:8080/api/v1 |

## Domyślni użytkownicy

Po uruchomieniu aplikacji dostępni są testowi użytkownicy:

| Rola | Email | Hasło |
|------|-------|-------|
| Administrator | `admin@example.com` | `admin123` |
| Pracownik | `jan.kowalski@example.com` | `password123` |

---

### Zatrzymanie

```bash
docker-compose down

# Aby usunąć również dane (volumes):
docker-compose down -v
```

---

## Uruchomienie lokalne (development)

### Wymagania
- Java 21 (JDK)
- Maven 3.6+
- Node.js 18+
- MySQL 8.0+

### 1. Baza danych

Uruchom MySQL Server na porcie 3306. Aplikacja automatycznie utworzy bazę `assetmanagement`.

### 2. Backend

```bash
cd backend

# Uruchom aplikację (port 8080)
mvn spring-boot:run
```

Domyślna konfiguracja używa `root:root` do połączenia z MySQL.
Możesz to zmienić w `backend/src/main/resources/application.yaml`.

### 3. Frontend

```bash
cd frontend

# Zainstaluj zależności
npm install

# Uruchom dev server (port 5173)
npm run dev
```

### Dostęp do aplikacji

| Serwis | URL |
|--------|-----|
| Frontend | http://localhost:5173 |
| Backend API | http://localhost:8080/api/v1 |

---

## Testy

### Backend
```bash
cd backend
mvn clean test     # Testy jednostkowe
```

### Frontend
```bash
cd frontend
npm install
npm run test                   # Testy jednostkowe (Vitest)
npm run test:coverage          # Testy z pokryciem
npm run e2e                    # Testy E2E (Playwright)
npm run e2e:headed             # Testy E2E z widoczną przeglądarką
```

---

## Komendy użytkowe

### Docker
```bash
docker-compose up -d           # Uruchom w tle
docker-compose logs -f         # Podgląd logów
docker-compose restart backend # Restart pojedynczego serwisu
docker-compose down            # Zatrzymaj
```

### Backend (Maven)
```bash
mvn spring-boot:run            # Uruchom
mvn clean package              # Zbuduj JAR
mvn test                       # Testy
```

### Frontend (npm)
```bash
npm run dev                    # Dev server
npm run build                  # Build produkcyjny
```

---

## API Endpoints

Główne endpointy REST API:

### Autentykacja
- `POST /api/v1/auth/login` - Logowanie
- `POST /api/v1/auth/change-password` - Zmiana hasła

### Admin (wymagana rola ADMIN)
- `GET/POST /api/v1/admin/employees` - Lista/dodawanie pracowników
- `GET/POST /api/v1/admin/assets` - Lista/dodawanie zasobów
- `PUT /api/v1/admin/assets/{id}/deactivate` - Dezaktywacja zasobu
- `GET/POST /api/v1/admin/assignments` - Lista/tworzenie przydziałów
- `PUT /api/v1/admin/assignments/{id}/end` - Zakończenie przydziału

### Pracownik (ADMIN lub EMPLOYEE)
- `GET /api/v1/employee/assets` - Moje aktywne zasoby
- `GET /api/v1/employee/assignments` - Moja historia przydziałów

Szczegółowa dokumentacja API: [.ai/api-plan.md](.ai/api-plan.md)

---

## Konfiguracja

### Zmienne środowiskowe (.env)

```env
# Baza danych
DB_ROOT_PASSWORD=root
DB_NAME=assetmanagement
DB_USER=user
DB_PASSWORD=password123

# JWT (min. 32 znaki)
JWT_SECRET=your-secret-key-here

# Porty (opcjonalne)
BACKEND_PORT=8080
FRONTEND_PORT=80
```

---

## Dokumentacja

| Dokument | Opis |
|----------|------|
| [backend/README.md](backend/README.md) | Dokumentacja backendu, API, przykłady curl |
| [.ai/prd.md](.ai/prd.md) | Wymagania produktu i user stories |
| [.ai/api-plan.md](.ai/api-plan.md) | Pełna dokumentacja REST API |
| [.ai/db-plan.md](.ai/db-plan.md) | Schemat bazy danych |
| [.ai/ui-plan.md](.ai/ui-plan.md) | Architektura interfejsu użytkownika |
| [.ai/tech-stack.md](.ai/tech-stack.md) | Szczegółowy stack technologiczny |

---

## Licencja

Projekt MVP do zarządzania zasobami firmowymi.

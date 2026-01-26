# Architektura UI dla Asset Management System

## 1. Przegląd struktury UI

### 1.1 Koncepcja ogólna

Aplikacja Asset Management System to webowa aplikacja biznesowa typu SPA (Single Page Application) służąca do zarządzania zasobami firmowymi i ich przydziałami pracownikom. Interfejs użytkownika jest zbudowany w oparciu o:

- **Framework:** React z Vite
- **Biblioteka UI:** Material-UI (MUI) v5
- **Routing:** React Router v6
- **Zarządzanie stanem:** React Query (TanStack Query) + React Context dla autentykacji
- **Walidacja:** React Hook Form + Zod
- **HTTP Client:** Axios z interceptorami

### 1.2 Architektura dostępu

System implementuje dwupoziomową kontrolę dostępu:

| Rola | Zakres dostępu | Strona startowa |
|------|---------------|-----------------|
| ADMIN | Pełny dostęp do wszystkich funkcji zarządzania | `/assets` |
| EMPLOYEE | Wyłącznie własne zasoby i historia przydziałów | `/my-assets` |

### 1.3 Zasady projektowe

- **Prostota:** UI ma być funkcjonalny, nie musi być piękny
- **Desktop-first:** Aplikacja biznesowa, priorytet dla widoku desktop
- **Polskie komunikaty:** Wszystkie etykiety i komunikaty w języku polskim
- **Modalne formularze:** Operacje CRUD przez okna dialogowe (użytkownik pozostaje w kontekście)
- **Minimalizm:** Brak dodatkowych funkcji poza MVP

---

## 2. Lista widoków

### 2.1 Widok logowania (LoginPage)

| Właściwość | Wartość |
|------------|---------|
| **Ścieżka** | `/login` |
| **Dostęp** | Publiczny |
| **Cel** | Uwierzytelnienie użytkownika w systemie |

**Kluczowe informacje do wyświetlenia:**
- Formularz logowania (email, hasło)
- Komunikat błędu przy niepoprawnych danych
- Nazwa/logo systemu

**Kluczowe komponenty:**
- `LoginForm` - formularz z polami email i hasło
- `ErrorMessage` - komunikat o błędzie logowania

**Powiązanie z User Stories:** US-001, US-019

**Względy UX/Dostępności/Bezpieczeństwa:**
- Hasło maskowane podczas wpisywania
- Komunikat błędu ogólny: "Nieprawidłowy email lub hasło" (bez wskazywania, które pole jest błędne)
- Obsługa klawiatury (Enter do zatwierdzenia)
- Autofocus na polu email

---

### 2.2 Lista pracowników (EmployeesPage)

| Właściwość | Wartość |
|------------|---------|
| **Ścieżka** | `/employees` |
| **Dostęp** | ADMIN |
| **Cel** | Przegląd i zarządzanie pracownikami |

**Kluczowe informacje do wyświetlenia:**
- Tabela pracowników: ID, imię i nazwisko, email, rola, data zatrudnienia (od/do)
- Przycisk dodawania nowego pracownika
- Komunikat przy pustej liście

**Kluczowe komponenty:**
- `DataTable` - tabela z listą pracowników (bez paginacji)
- `AddEmployeeModal` - modal z formularzem dodawania pracownika
- `EmptyState` - komunikat "Brak pracowników w systemie"

**Powiązanie z User Stories:** US-003, US-004, US-017, US-020

**Względy UX/Dostępności/Bezpieczeństwa:**
- Walidacja unikalności emaila przed zapisem
- Walidacja formatu emaila
- Wyświetlanie roli w czytelny sposób (ADMIN/EMPLOYEE)
- Data "do" opcjonalna (null = aktywny pracownik)

---

### 2.3 Lista zasobów (AssetsPage)

| Właściwość | Wartość |
|------------|---------|
| **Ścieżka** | `/assets` |
| **Dostęp** | ADMIN |
| **Cel** | Przegląd i zarządzanie zasobami firmowymi |

**Kluczowe informacje do wyświetlenia:**
- Tabela zasobów: ID, typ, producent, model, numer seryjny, status (aktywny/nieaktywny)
- Informacja o przypisanym pracowniku (jeśli przypisany): ID, imię i nazwisko, email
- Kontrolki paginacji i sortowania
- Przycisk dodawania nowego zasobu
- Przycisk dezaktywacji zasobu (dla nieprzypisanych)

**Kluczowe komponenty:**
- `DataTable` - tabela z paginacją i sortowaniem
- `Pagination` - kontrolka paginacji (page, size)
- `AddAssetModal` - modal z formularzem dodawania zasobu
- `ConfirmDialog` - dialog potwierdzenia dezaktywacji
- `EmptyState` - komunikat "Brak zasobów w systemie"

**Powiązanie z User Stories:** US-005, US-006, US-007, US-016, US-020

**Względy UX/Dostępności/Bezpieczeństwa:**
- Przycisk dezaktywacji nieaktywny (disabled) dla zasobów przypisanych
- Tooltip wyjaśniający dlaczego nie można dezaktywować
- Wizualne rozróżnienie zasobów aktywnych/nieaktywnych
- Walidacja unikalności numeru seryjnego

---

### 2.4 Historia przydziałów (AssignmentsPage)

| Właściwość | Wartość |
|------------|---------|
| **Ścieżka** | `/assignments` |
| **Dostęp** | ADMIN |
| **Cel** | Zarządzanie przydziałami zasobów do pracowników |

**Kluczowe informacje do wyświetlenia:**
- Tabela przydziałów: ID przydziału, dane zasobu (typ, producent, model, numer seryjny), dane pracownika (ID, imię i nazwisko), data od, data do, status
- Filtry: dropdown wyboru pracownika, dropdown wyboru zasobu
- Kontrolki paginacji (tylko bez filtrów)
- Przycisk tworzenia nowego przydziału
- Przycisk zakończenia przydziału (dla aktywnych)

**Kluczowe komponenty:**
- `DataTable` - tabela z paginacją (wyłączona przy filtrach)
- `FilterDropdown` - dropdown do filtrowania po pracowniku lub zasobie
- `CreateAssignmentModal` - modal z formularzem tworzenia przydziału
- `EndAssignmentModal` - modal z formularzem zakończenia przydziału
- `EmptyState` - komunikat "Brak przydziałów w systemie"

**Powiązanie z User Stories:** US-008, US-009, US-010, US-011, US-012, US-018, US-020

**Względy UX/Dostępności/Bezpieczeństwa:**
- Dropdown zasobów pokazuje tylko aktywne i nieprzypisane zasoby
- Wizualne rozróżnienie przydziałów aktywnych/zakończonych
- Przycisk "Zakończ" tylko dla aktywnych przydziałów
- Walidacja zapobiegająca podwójnemu przypisaniu

---

### 2.5 Moje zasoby (MyAssetsPage)

| Właściwość | Wartość |
|------------|---------|
| **Ścieżka** | `/my-assets` |
| **Dostęp** | ADMIN, EMPLOYEE |
| **Cel** | Wyświetlenie aktywnych zasobów zalogowanego użytkownika |

**Kluczowe informacje do wyświetlenia:**
- Lista aktywnych zasobów: typ, producent, model, numer seryjny, data przydziału
- Komunikat przy braku przydzielonych zasobów

**Kluczowe komponenty:**
- `DataTable` - prosta tabela bez paginacji
- `EmptyState` - komunikat "Nie masz przydzielonych zasobów"

**Powiązanie z User Stories:** US-013, US-015, US-020

**Względy UX/Dostępności/Bezpieczeństwa:**
- Brak dostępu do danych innych pracowników
- Wyświetlane tylko aktywne przydziały
- Czytelna prezentacja danych

---

### 2.6 Moja historia przydziałów (MyHistoryPage)

| Właściwość | Wartość |
|------------|---------|
| **Ścieżka** | `/my-history` |
| **Dostęp** | ADMIN, EMPLOYEE |
| **Cel** | Wyświetlenie historii wszystkich przydziałów zalogowanego użytkownika |

**Kluczowe informacje do wyświetlenia:**
- Lista wszystkich przydziałów (aktywnych i zakończonych): ID przydziału, ID zasobu, typ, producent, model, numer seryjny, data od, data do, status
- Komunikat przy braku historii

**Kluczowe komponenty:**
- `DataTable` - tabela bez paginacji, sortowanie chronologiczne
- `EmptyState` - komunikat "Brak historii przydziałów"

**Powiązanie z User Stories:** US-014, US-015, US-020

**Względy UX/Dostępności/Bezpieczeństwa:**
- Brak dostępu do historii innych pracowników
- Wizualne rozróżnienie przydziałów aktywnych/zakończonych
- Sortowanie chronologiczne (najnowsze na górze)

---

## 3. Mapa podróży użytkownika

### 3.1 Flow logowania

```
┌─────────────────┐
│   /login        │
│  Formularz      │
│  email + hasło  │
└────────┬────────┘
         │
         ▼
    ┌────────────┐
    │ Walidacja  │
    │ credentials│
    └─────┬──────┘
          │
    ┌─────┴─────┐
    │           │
    ▼           ▼
┌───────┐   ┌───────┐
│ ADMIN │   │EMPLOYEE│
└───┬───┘   └───┬───┘
    │           │
    ▼           ▼
 /assets    /my-assets
```

### 3.2 Flow administratora - zarządzanie zasobami

```
┌─────────────────────────────────────────────────────────┐
│                    /assets                               │
│  Lista zasobów z paginacją                              │
├─────────────────────────────────────────────────────────┤
│  [+ Dodaj zasób]  →  Modal: AddAssetModal               │
│                      └─> Formularz → Zapis → Refresh    │
├─────────────────────────────────────────────────────────┤
│  [Dezaktywuj] (dla nieprzypisanych)                     │
│       └─> ConfirmDialog → Potwierdzenie → Refresh       │
└─────────────────────────────────────────────────────────┘
```

### 3.3 Flow administratora - tworzenie przydziału

```
┌─────────────────────────────────────────────────────────┐
│                  /assignments                            │
│  Lista przydziałów z paginacją i filtrami               │
├─────────────────────────────────────────────────────────┤
│  [+ Utwórz przydział]  →  Modal: CreateAssignmentModal  │
│                            ├─ Dropdown: Pracownik       │
│                            ├─ Dropdown: Zasób (dostępne)│
│                            ├─ Date picker: Data od      │
│                            └─> Zapis → Refresh          │
├─────────────────────────────────────────────────────────┤
│  [Zakończ] (dla aktywnych)                              │
│       └─> Modal: EndAssignmentModal                     │
│           ├─ Date picker: Data do                       │
│           └─> Zapis → Refresh                           │
└─────────────────────────────────────────────────────────┘
```

### 3.4 Flow administratora - filtrowanie przydziałów

```
┌─────────────────────────────────────────────────────────┐
│                  /assignments                            │
├─────────────────────────────────────────────────────────┤
│  Filtr: [Wybierz pracownika ▼] [Wybierz zasób ▼]        │
│                                                          │
│  Brak filtra:     Lista z paginacją                     │
│  Z filtrem:       Lista bez paginacji (wszystkie)       │
│                                                          │
│  [Wyczyść filtry] → Powrót do widoku z paginacją        │
└─────────────────────────────────────────────────────────┘
```

### 3.5 Flow pracownika

```
┌─────────────────────────────────────────────────────────┐
│  Logowanie → /my-assets                                  │
├─────────────────────────────────────────────────────────┤
│  Nawigacja: [Moje zasoby] [Historia przydziałów]        │
├─────────────────────────────────────────────────────────┤
│                                                          │
│  /my-assets         /my-history                         │
│  ┌─────────────┐    ┌─────────────────────────────┐     │
│  │ Aktywne     │    │ Wszystkie przydziały        │     │
│  │ zasoby      │    │ (aktywne + zakończone)      │     │
│  │             │    │                             │     │
│  │ Read-only   │    │ Read-only                   │     │
│  └─────────────┘    └─────────────────────────────┘     │
│                                                          │
└─────────────────────────────────────────────────────────┘
```

### 3.6 Flow wylogowania

```
  Dowolny widok
       │
       ▼
  [Wyloguj] (w nawigacji)
       │
       ▼
  Usunięcie tokena z pamięci
       │
       ▼
  Przekierowanie → /login
```

---

## 4. Układ i struktura nawigacji

### 4.1 Layout główny

```
┌─────────────────────────────────────────────────────────────────┐
│  ┌─────────────────────────────────────────────────────────┐   │
│  │  [Logo/Nazwa]  │  Nav Items (zależne od roli)  │ [Wyloguj]│   │
│  └─────────────────────────────────────────────────────────┘   │
│                                                                 │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │                                                          │   │
│  │                    CONTENT AREA                          │   │
│  │                    (Router Outlet)                       │   │
│  │                                                          │   │
│  └─────────────────────────────────────────────────────────┘   │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

### 4.2 Nawigacja dla roli ADMIN

```
┌───────────────────────────────────────────────────────────────────────────┐
│  Asset Management  │  Pracownicy  │  Zasoby  │  Przydziały  │  [Konto ▼]  │
└───────────────────────────────────────────────────────────────────────────┘
                                                                      │
                                                               ┌──────┴──────┐
                                                               │ Zmień hasło │
                                                               │ Wyloguj     │
                                                               └─────────────┘
Mapowanie:
- Pracownicy  → /employees
- Zasoby      → /assets
- Przydziały  → /assignments
- Zmień hasło → Modal: ChangePasswordModal
- Wyloguj     → logout()
```

### 4.3 Nawigacja dla roli EMPLOYEE

```
┌──────────────────────────────────────────────────────────────────────────────┐
│  Asset Management  │  Moje zasoby  │  Historia przydziałów  │  [Konto ▼]     │
└──────────────────────────────────────────────────────────────────────────────┘
                                                                        │
                                                                 ┌──────┴──────┐
                                                                 │ Zmień hasło │
                                                                 │ Wyloguj     │
                                                                 └─────────────┘
Mapowanie:
- Moje zasoby           → /my-assets
- Historia przydziałów  → /my-history
- Zmień hasło           → Modal: ChangePasswordModal
- Wyloguj               → logout()
```

### 4.4 Ochrona tras (Route Guards)

| Ścieżka | Warunek dostępu | Przekierowanie przy braku dostępu |
|---------|-----------------|-----------------------------------|
| `/login` | Niezalogowany | Zalogowany ADMIN → `/assets`, EMPLOYEE → `/my-assets` |
| `/employees` | ADMIN | `/login` lub `/my-assets` |
| `/assets` | ADMIN | `/login` lub `/my-assets` |
| `/assignments` | ADMIN | `/login` lub `/my-assets` |
| `/my-assets` | ADMIN lub EMPLOYEE | `/login` |
| `/my-history` | ADMIN lub EMPLOYEE | `/login` |

### 4.5 Hierarchia routingu

```
<Router>
  <Routes>
    <!-- Trasa publiczna -->
    <Route path="/login" element={<LoginPage />} />

    <!-- Trasy chronione -->
    <Route element={<ProtectedRoute />}>
      <Route element={<Layout />}>

        <!-- Trasy ADMIN -->
        <Route element={<AdminRoute />}>
          <Route path="/employees" element={<EmployeesPage />} />
          <Route path="/assets" element={<AssetsPage />} />
          <Route path="/assignments" element={<AssignmentsPage />} />
        </Route>

        <!-- Trasy ADMIN + EMPLOYEE -->
        <Route path="/my-assets" element={<MyAssetsPage />} />
        <Route path="/my-history" element={<MyHistoryPage />} />

      </Route>
    </Route>

    <!-- Domyślne przekierowanie -->
    <Route path="*" element={<Navigate to="/login" />} />
  </Routes>
</Router>
```

---

## 5. Kluczowe komponenty

### 5.1 Komponenty layoutu

#### Layout
- **Cel:** Główny wrapper aplikacji z nawigacją
- **Zawartość:** Top navbar + Router Outlet
- **Użycie:** Wszystkie strony chronione

#### TopNavbar
- **Cel:** Górny pasek nawigacyjny
- **Zawartość:** Logo, linki nawigacyjne (zależne od roli), przycisk wylogowania
- **Props:** `userRole`, `onLogout`

#### ProtectedRoute
- **Cel:** HOC chroniący trasy wymagające autentykacji
- **Logika:** Sprawdzenie tokena, przekierowanie do `/login` przy braku

#### AdminRoute
- **Cel:** HOC chroniący trasy dostępne tylko dla ADMIN
- **Logika:** Sprawdzenie roli, przekierowanie do `/my-assets` dla EMPLOYEE

### 5.2 Komponenty danych

#### DataTable
- **Cel:** Reużywalna tabela z opcjonalną paginacją i sortowaniem
- **Props:** `columns`, `data`, `loading`, `emptyMessage`, `pagination?`, `onPageChange?`, `onSort?`
- **Stany:** loading (spinner), empty (komunikat), error (alert), data (tabela)
- **Użycie:** Wszystkie widoki listowe

#### Pagination
- **Cel:** Kontrolka paginacji
- **Props:** `page`, `size`, `totalElements`, `totalPages`, `onPageChange`, `onSizeChange`
- **Użycie:** AssetsPage, AssignmentsPage (bez filtrów)

#### FilterDropdown
- **Cel:** Dropdown do filtrowania danych
- **Props:** `label`, `options`, `value`, `onChange`, `onClear`
- **Użycie:** AssignmentsPage (filtr pracownika/zasobu)

### 5.3 Komponenty formularzy

#### Modal
- **Cel:** Reużywalne okno modalne
- **Props:** `open`, `onClose`, `title`, `children`, `actions`
- **Bazuje na:** MUI Dialog
- **Użycie:** Wszystkie formularze CRUD

#### FormField
- **Cel:** Pole formularza z etykietą i obsługą błędów
- **Props:** `label`, `name`, `type`, `register`, `error`, `required`, `options?`
- **Typy:** text, email, password, date, select
- **Użycie:** Wszystkie formularze

#### DatePicker
- **Cel:** Wybór daty
- **Implementacja:** Natywny `<input type="date">`
- **Format:** YYYY-MM-DD (zgodny z API)

### 5.4 Komponenty stanu

#### LoadingSpinner
- **Cel:** Wskaźnik ładowania danych
- **Użycie:** Podczas pobierania danych z API

#### ErrorMessage
- **Cel:** Wyświetlanie komunikatów błędów
- **Props:** `message`
- **Styl:** Alert/banner nad zawartością

#### EmptyState
- **Cel:** Komunikat przy pustych listach
- **Props:** `message`
- **Komunikaty PL:**
  - "Brak pracowników w systemie"
  - "Brak zasobów w systemie"
  - "Brak przydziałów w systemie"
  - "Nie masz przydzielonych zasobów"
  - "Brak historii przydziałów"

#### ConfirmDialog
- **Cel:** Dialog potwierdzenia akcji
- **Props:** `open`, `title`, `message`, `onConfirm`, `onCancel`
- **Użycie:** Dezaktywacja zasobu, zakończenie przydziału

### 5.5 Komponenty modalne (formularze)

#### AddEmployeeModal
- **Cel:** Formularz dodawania pracownika
- **Pola:** imię i nazwisko, email, hasło, rola (dropdown), data od, data do (opcjonalna)
- **Walidacja:** wymagane pola, format email, unikalność email
- **Endpoint:** POST /api/v1/admin/employees

#### AddAssetModal
- **Cel:** Formularz dodawania zasobu
- **Pola:** typ (dropdown), producent, model, numer seryjny
- **Walidacja:** wymagane pola, unikalność numeru seryjnego
- **Typy zasobów:** LAPTOP, SMARTPHONE, TABLET, PRINTER, HEADPHONES
- **Endpoint:** POST /api/v1/admin/assets

#### CreateAssignmentModal
- **Cel:** Formularz tworzenia przydziału
- **Pola:** pracownik (dropdown), zasób (dropdown - tylko dostępne), data od
- **Walidacja:** wymagane pola, zasób aktywny i nieprzypisany
- **Endpoint:** POST /api/v1/admin/assignments

#### EndAssignmentModal
- **Cel:** Formularz zakończenia przydziału
- **Pola:** data do
- **Walidacja:** wymagane pole, przydział aktywny
- **Endpoint:** PUT /api/v1/admin/assignments/{id}/end

---

## 6. Mapowanie User Stories na elementy UI

| User Story | Widok | Komponenty | Endpoint API |
|------------|-------|------------|--------------|
| US-001 Logowanie | LoginPage | LoginForm, ErrorMessage | POST /auth/login |
| US-002 Wylogowanie | Layout (TopNavbar) | Przycisk Wyloguj | - (client-side) |
| US-003 Dodawanie pracownika | EmployeesPage | AddEmployeeModal, FormField | POST /admin/employees |
| US-004 Lista pracowników | EmployeesPage | DataTable, EmptyState | GET /admin/employees |
| US-005 Dodawanie zasobu | AssetsPage | AddAssetModal, FormField | POST /admin/assets |
| US-006 Lista zasobów | AssetsPage | DataTable, Pagination, EmptyState | GET /admin/assets |
| US-007 Dezaktywacja zasobu | AssetsPage | ConfirmDialog | PUT /admin/assets/{id}/deactivate |
| US-008 Tworzenie przydziału | AssignmentsPage | CreateAssignmentModal, FormField | POST /admin/assignments |
| US-009 Kończenie przydziału | AssignmentsPage | EndAssignmentModal, FormField | PUT /admin/assignments/{id}/end |
| US-010 Historia przydziałów | AssignmentsPage | DataTable, Pagination, EmptyState | GET /admin/assignments |
| US-011 Filtr po zasobie | AssignmentsPage | FilterDropdown | GET /admin/assignments?assetId= |
| US-012 Filtr po pracowniku | AssignmentsPage | FilterDropdown | GET /admin/assignments?employeeId= |
| US-013 Moje aktywne zasoby | MyAssetsPage | DataTable, EmptyState | GET /employee/assets |
| US-014 Moja historia | MyHistoryPage | DataTable, EmptyState | GET /employee/assignments |
| US-015 Ochrona danych | ProtectedRoute, AdminRoute | - | Autoryzacja JWT |
| US-016 Walidacja numeru seryjnego | AddAssetModal | FormField, ErrorMessage | 409 Conflict |
| US-017 Walidacja emaila | AddEmployeeModal | FormField, ErrorMessage | 409 Conflict |
| US-018 Zapobieganie podwójnemu przydziałowi | CreateAssignmentModal | ErrorMessage | 409 Conflict |
| US-019 Błędne logowanie | LoginPage | ErrorMessage | 401 Unauthorized |
| US-020 Puste listy | Wszystkie widoki listowe | EmptyState | 200 OK (empty array) |

---

## 7. Struktura projektu

```
src/
├── api/
│   ├── client.js          # Axios instance + interceptory JWT
│   ├── auth.js            # login(), changePassword()
│   ├── employees.js       # getAll(), getById(), create()
│   ├── assets.js          # getAll(), getById(), create(), deactivate()
│   └── assignments.js     # getAll(), create(), end()
├── components/
│   ├── layout/
│   │   ├── Layout.jsx
│   │   └── TopNavbar.jsx
│   ├── routing/
│   │   ├── ProtectedRoute.jsx
│   │   └── AdminRoute.jsx
│   ├── data/
│   │   ├── DataTable.jsx
│   │   ├── Pagination.jsx
│   │   └── FilterDropdown.jsx
│   ├── forms/
│   │   ├── Modal.jsx
│   │   ├── FormField.jsx
│   │   └── ConfirmDialog.jsx
│   ├── feedback/
│   │   ├── LoadingSpinner.jsx
│   │   ├── ErrorMessage.jsx
│   │   └── EmptyState.jsx
│   └── modals/
│       ├── AddEmployeeModal.jsx
│       ├── AddAssetModal.jsx
│       ├── CreateAssignmentModal.jsx
│       └── EndAssignmentModal.jsx
├── context/
│   └── AuthContext.jsx    # token, user, login(), logout()
├── hooks/
│   └── useAuth.js         # Hook dostępu do AuthContext
├── pages/
│   ├── LoginPage.jsx
│   ├── EmployeesPage.jsx
│   ├── AssetsPage.jsx
│   ├── AssignmentsPage.jsx
│   ├── MyAssetsPage.jsx
│   └── MyHistoryPage.jsx
├── App.jsx                # Routing + providers
└── main.jsx               # Entry point
```

---

## 8. Obsługa błędów i stanów brzegowych

### 8.1 Błędy autentykacji

| Scenariusz | Obsługa |
|------------|---------|
| Błędne dane logowania | Komunikat "Nieprawidłowy email lub hasło" |
| Wygaśnięcie tokena (401) | Automatyczne wylogowanie, przekierowanie do /login |
| Brak tokena | Przekierowanie do /login |
| Brak uprawnień (403) | Komunikat "Brak uprawnień do tej operacji" |

### 8.2 Błędy walidacji

| Scenariusz | Obsługa |
|------------|---------|
| Email już istnieje (409) | Komunikat "Pracownik z tym adresem email już istnieje" |
| Numer seryjny już istnieje (409) | Komunikat "Zasób z tym numerem seryjnym już istnieje" |
| Zasób już przypisany (409) | Komunikat "Zasób jest już przypisany innemu pracownikowi" |
| Zasób nieaktywny (409) | Komunikat "Nie można przypisać nieaktywnego zasobu" |
| Nie można dezaktywować (409) | Komunikat "Nie można dezaktywować przypisanego zasobu" |
| Przydział już zakończony (409) | Komunikat "Przydział jest już zakończony" |

### 8.3 Stany puste

| Scenariusz | Komunikat |
|------------|-----------|
| Brak pracowników | "Brak pracowników w systemie" |
| Brak zasobów | "Brak zasobów w systemie" |
| Brak przydziałów | "Brak przydziałów w systemie" |
| Brak moich zasobów | "Nie masz przydzielonych zasobów" |
| Brak mojej historii | "Brak historii przydziałów" |

### 8.4 Stany ładowania

- Spinner podczas pobierania danych
- Dezaktywacja przycisków podczas zapisywania
- Informacja "Zapisywanie..." w modalach

### 8.5 Błędy sieciowe

| Scenariusz | Obsługa |
|------------|---------|
| Brak połączenia | Komunikat "Błąd połączenia z serwerem" |
| Timeout | Komunikat "Serwer nie odpowiada" |
| 500 Internal Error | Komunikat "Wystąpił błąd serwera" |

---

## 9. Integracja z API

### 9.1 Konfiguracja Axios

```javascript
// Interceptor request - dodawanie tokena
config.headers.Authorization = `Bearer ${token}`

// Interceptor response - obsługa 401
if (response.status === 401) {
  logout()
  redirect('/login')
}
```

### 9.2 React Query - klucze i invalidacja

| Query Key | Endpoint | Invalidacja po |
|-----------|----------|----------------|
| `['employees']` | GET /admin/employees | Dodaniu pracownika |
| `['assets', {page, size, sort}]` | GET /admin/assets | Dodaniu/dezaktywacji zasobu, zmianie przydziału |
| `['asset', id]` | GET /admin/assets/{id} | Dezaktywacji zasobu |
| `['assignments', {page, size, sort, filters}]` | GET /admin/assignments | Utworzeniu/zakończeniu przydziału |
| `['my-assets']` | GET /employee/assets | - (read-only) |
| `['my-history']` | GET /employee/assignments | - (read-only) |

### 9.3 Format odpowiedzi błędów

```json
{
  "timeStamp": "2024-01-15T10:30:00",
  "code": "Bad Request",
  "message": "Szczegółowy komunikat błędu"
}
```

---

## 10. Nierozwiązane kwestie (do decyzji podczas implementacji)

1. **Walidacja dat biznesowych:** Czy walidować client-side?
   - Rekomendacja: Tak, w Zod (data "do" >= data "od")

2. **Szczegóły pracownika:** Czy potrzebny modal ze szczegółami?
   - Rekomendacja: Nie w MVP, dane widoczne w tabeli

3. **Obsługa błędu 404 (pracownik/zasób):** Przy próbie operacji na nieistniejącym obiekcie
   - Rekomendacja: Komunikat "Nie znaleziono zasobu/pracownika"

# Podsumowanie planowania architektury UI - Asset Management System MVP

## Decyzje

1. **Nawigacja:** Górny pasek nawigacyjny (top navbar), uproszczony dla roli EMPLOYEE w stosunku do ADMIN
2. **Zarządzanie stanem:** Najprostsze rozwiązanie zgodne z dobrymi praktykami - React Query do komunikacji z API + React Context dla autentykacji
3. **Paginacja i sortowanie:** Po stronie serwera, zgodnie z API (tylko dla zasobów i przydziałów)
4. **Biblioteka UI:** Najpopularniejsze, typowe i proste narzędzia (Material-UI jako rekomendacja)
5. **Walidacja formularzy:** Zgodnie z regułami API, najprostsze narzędzia (React Hook Form + Zod)
6. **Komponenty:** Reużywalne, skupienie na funkcjonalności, prosty UI - ma działać, nie musi być piękny
7. **Filtry przydziałów:** Dropdown na jednym widoku, zgodnie z dobrymi praktykami
8. **Token JWT:** Przechowywanie w pamięci, automatyczne dołączanie do requestów API
9. **Blokada akcji:** Dezaktywacja przycisków dla niedozwolonych operacji
10. **Tryb ciemny/dostępność:** Pominięte w MVP, skupienie na funkcjonalności
11. **Formularze:** Modalne okna dialogowe (prostsze niż osobne strony)
12. **Strona główna:** ADMIN → lista zasobów, EMPLOYEE → moje zasoby (bez dedykowanego dashboardu)
13. **Wybór w formularzach:** Prosty dropdown z pełną listą (bez autocomplete)
14. **Szczegóły zasobu:** Informacje o przypisanym pracowniku w kolumnach tabeli, bez osobnych widoków
15. **Wybór daty:** Natywny input HTML5 `<input type="date">`
16. **Potwierdzenia akcji:** Prosty dialog potwierdzenia (window.confirm lub bazowy Dialog)
17. **Struktura projektu:** Prosta, płaska struktura folderów
18. **Lista pracowników:** Bez paginacji, wyświetlanie wszystkich
19. **Wylogowanie:** Przycisk w nawigacji, usunięcie tokena, przekierowanie do logowania
20. **Komunikaty błędów/sukcesu:** Prosty alert/banner nad formularzem/tabelą, bez dodatkowych bibliotek

---

## Dopasowane rekomendacje

1. **Nawigacja dynamiczna:** Górny pasek z elementami zależnymi od roli użytkownika - ADMIN widzi: Pracownicy, Zasoby, Przydziały; EMPLOYEE widzi: Moje zasoby, Historia przydziałów
2. **React Query + Context:** React Query (TanStack Query) do cache i synchronizacji z API, React Context wyłącznie dla stanu autentykacji - bez Redux
3. **Paginacja serwerowa:** Wykorzystanie parametrów page, size, sort z API dla list zasobów i przydziałów
4. **Material-UI (MUI):** Dojrzała biblioteka z gotowymi komponentami (Table, Modal, Form, Button)
5. **React Hook Form + Zod:** Prosta walidacja schematów odpowiadająca regułom API
6. **Reużywalne komponenty:** LoadingSpinner, ErrorMessage, EmptyState z komunikatami po polsku
7. **Filtry jako dropdown:** Na jednym widoku historii przydziałów, dynamiczne przełączanie między widokiem z paginacją a listą
8. **Token w pamięci + axios interceptor:** Automatyczne dołączanie Bearer token do każdego requestu, obsługa 401 z przekierowaniem do logowania
9. **Disabled buttons z tooltipem:** Dezaktywacja przycisków dla niedozwolonych akcji z wyjaśnieniem powodu
10. **Modalne formularze:** Dialog dla dodawania pracownika, zasobu, przydziału - użytkownik pozostaje w kontekście listy
11. **Natywny date picker:** `<input type="date">` zwracający format YYYY-MM-DD zgodny z API
12. **Prosta struktura folderów:** components/, pages/, api/, context/, App.jsx

---

## Podsumowanie planowania architektury UI

### A. Główne wymagania dotyczące architektury UI

#### Stos technologiczny
- **Framework:** React (Create React App lub Vite)
- **Biblioteka UI:** Material-UI (MUI) v5
- **Zarządzanie stanem serwera:** React Query (TanStack Query)
- **Zarządzanie stanem autentykacji:** React Context
- **Routing:** React Router v6
- **Walidacja formularzy:** React Hook Form + Zod
- **HTTP Client:** Axios z interceptorami

#### Zasady przewodnie
- Prostota ponad elegancją - UI ma działać, nie musi być piękny
- Wykorzystanie najpopularniejszych, dobrze udokumentowanych narzędzi
- Reużywalne komponenty dla spójności
- Minimalna liczba zewnętrznych zależności
- Zgodność z API - walidacja i struktury danych odpowiadające backendowi

---

### B. Kluczowe widoki, ekrany i przepływy użytkownika

#### Struktura nawigacji (Top Navbar)

**Dla roli ADMIN:**
```
[Logo/Nazwa] | Pracownicy | Zasoby | Przydziały |                    [Wyloguj]
```

**Dla roli EMPLOYEE:**
```
[Logo/Nazwa] | Moje zasoby | Historia przydziałów |                   [Wyloguj]
```

#### Lista widoków (Pages)

| Widok | Ścieżka | Rola | Opis |
|-------|---------|------|------|
| Login | `/login` | Public | Formularz logowania (email + hasło) |
| Lista pracowników | `/employees` | ADMIN | Tabela wszystkich pracowników (bez paginacji) |
| Lista zasobów | `/assets` | ADMIN | Tabela z paginacją i sortowaniem, info o przypisaniu |
| Historia przydziałów | `/assignments` | ADMIN | Tabela z paginacją, filtry dropdown |
| Moje zasoby | `/my-assets` | EMPLOYEE | Lista aktywnych zasobów użytkownika |
| Moja historia | `/my-history` | EMPLOYEE | Lista wszystkich przydziałów użytkownika |

#### Modalne okna dialogowe

| Modal | Wyzwalacz | Zawartość |
|-------|-----------|-----------|
| Dodaj pracownika | Przycisk na liście pracowników | Formularz: imię, email, hasło, rola, daty zatrudnienia |
| Dodaj zasób | Przycisk na liście zasobów | Formularz: typ, producent, model, numer seryjny |
| Utwórz przydział | Przycisk na liście przydziałów | Formularz: dropdown pracownik, dropdown zasób, data od |
| Zakończ przydział | Przycisk w wierszu przydziału | Formularz: data zakończenia + potwierdzenie |
| Dezaktywuj zasób | Przycisk w wierszu zasobu | Dialog potwierdzenia |

#### Przepływy użytkownika

**Flow 1: Logowanie**
```
Strona logowania → Wprowadzenie email/hasło → Walidacja →
→ Sukces: Przekierowanie do strony głównej (zależnej od roli)
→ Błąd: Komunikat "Nieprawidłowy email lub hasło"
```

**Flow 2: Admin - Dodanie zasobu i przydzielenie pracownikowi**
```
Lista zasobów → [Dodaj zasób] → Modal z formularzem → Zapisz →
→ Lista zasobów (odświeżona) → Przejdź do Przydziały →
→ [Utwórz przydział] → Wybierz pracownika (dropdown) →
→ Wybierz zasób (dropdown - tylko dostępne) → Data od → Zapisz
```

**Flow 3: Admin - Zakończenie przydziału**
```
Lista przydziałów → Filtruj po pracowniku (opcjonalnie) →
→ [Zakończ] przy aktywnym przydziale → Modal z datą →
→ Potwierdzenie → Przydział oznaczony jako nieaktywny
```

**Flow 4: Employee - Przeglądanie swoich zasobów**
```
Logowanie → Automatyczne przekierowanie do "Moje zasoby" →
→ Lista aktywnych zasobów z datami przydziału
```

---

### C. Strategia integracji z API i zarządzania stanem

#### Struktura API Layer (`src/api/`)

```javascript
// api/client.js - Axios instance z interceptorami
// api/auth.js - login(), changePassword()
// api/employees.js - getAll(), getById(), create()
// api/assets.js - getAll(page, size, sort), getById(), create(), deactivate()
// api/assignments.js - getAll(filters), create(), end()
// api/myAssets.js - getMyAssets(), getMyHistory()
```

#### Zarządzanie tokenem JWT

```javascript
// AuthContext zapewnia:
- token (przechowywany w state, nie w localStorage)
- user (dane z tokena: email, role)
- login(email, password) → zapisuje token
- logout() → czyści token, przekierowuje do /login

// Axios interceptor:
- Request: dodaje header "Authorization: Bearer {token}"
- Response: przy 401 → wywołuje logout()
```

#### React Query - Cache i synchronizacja

```javascript
// Klucze query:
['employees'] - lista pracowników
['assets', { page, size, sort }] - lista zasobów
['assignments', { page, size, sort, employeeId?, assetId? }] - przydziały
['my-assets'] - zasoby zalogowanego pracownika
['my-history'] - historia zalogowanego pracownika

// Invalidacja po mutacjach:
- Po dodaniu pracownika → invalidate(['employees'])
- Po dodaniu zasobu → invalidate(['assets'])
- Po utworzeniu/zakończeniu przydziału → invalidate(['assignments'], ['assets'])
```

---

### D. Kwestie responsywności, dostępności i bezpieczeństwa

#### Responsywność
- **Priorytet:** Desktop-first (aplikacja biznesowa)
- **Minimum:** Tabele z poziomym scrollem na mniejszych ekranach
- **MUI Grid:** Wykorzystanie dla podstawowego layoutu
- **Brak dedykowanego widoku mobilnego** w MVP

#### Dostępność (a11y)
- **Pominięta w MVP** - skupienie na funkcjonalności
- Bazowe wsparcie z MUI (aria-labels, keyboard navigation) bez dodatkowej pracy

#### Bezpieczeństwo

| Aspekt | Implementacja |
|--------|---------------|
| Token storage | W pamięci (React state), nie w localStorage |
| Auto-logout | Przy odpowiedzi 401 z API |
| Protected routes | React Router z sprawdzeniem tokena i roli |
| Walidacja | Client-side (Zod) + server-side (API) |
| Komunikaty błędów | Ogólne "Nieprawidłowy email lub hasło" (bez ujawniania szczegółów) |

#### Ochrona tras (Protected Routes)

```javascript
// Trasy publiczne: /login
// Trasy ADMIN: /employees, /assets, /assignments
// Trasy EMPLOYEE: /my-assets, /my-history
// Przekierowania:
- Niezalogowany → /login
- EMPLOYEE próbuje /admin/* → /my-assets
- ADMIN po logowaniu → /assets
- EMPLOYEE po logowaniu → /my-assets
```

---

### E. Struktura projektu

```
src/
├── api/
│   ├── client.js          # Axios instance + interceptory
│   ├── auth.js            # login, changePassword
│   ├── employees.js       # CRUD pracowników
│   ├── assets.js          # CRUD zasobów
│   └── assignments.js     # CRUD przydziałów
├── components/
│   ├── Layout.jsx         # Top navbar + outlet
│   ├── ProtectedRoute.jsx # Sprawdzanie autentykacji i roli
│   ├── DataTable.jsx      # Reużywalna tabela z paginacją
│   ├── Modal.jsx          # Wrapper na MUI Dialog
│   ├── FormField.jsx      # Input + label + error message
│   ├── LoadingSpinner.jsx
│   ├── ErrorMessage.jsx
│   └── EmptyState.jsx
├── context/
│   └── AuthContext.jsx    # Token, user, login, logout
├── pages/
│   ├── LoginPage.jsx
│   ├── EmployeesPage.jsx
│   ├── AssetsPage.jsx
│   ├── AssignmentsPage.jsx
│   ├── MyAssetsPage.jsx
│   └── MyHistoryPage.jsx
├── hooks/
│   └── useAuth.js         # Hook do AuthContext
├── App.jsx                # Routing + providers
└── index.jsx              # Entry point
```

---

### F. Komponenty UI - specyfikacja

#### DataTable (reużywalny)
- Props: columns, data, loading, emptyMessage, pagination?, onPageChange?, onSort?
- Obsługa stanów: loading (spinner), empty (komunikat), error (alert)
- Paginacja opcjonalna (dla assets, assignments)

#### Modal (reużywalny)
- Props: open, onClose, title, children, actions
- Bazuje na MUI Dialog
- Zamykanie przez X lub kliknięcie poza modal

#### FormField (reużywalny)
- Props: label, name, type, register, error, required
- Obsługa typów: text, email, password, date, select
- Wyświetlanie błędu walidacji pod polem

#### Komunikaty (EmptyState, ErrorMessage)
- Polskie komunikaty zgodne z PRD:
  - "Brak pracowników w systemie"
  - "Brak zasobów w systemie"
  - "Brak przydziałów w systemie"
  - "Nie masz przydzielonych zasobów"
  - "Brak historii przydziałów"

---

## Nierozwiązane kwestie

1. **Wybór narzędzia do budowania projektu:** Nie ustalono czy użyć Create React App (prostsze, ale przestarzałe) czy Vite (nowsze, szybsze) - rekomendacja: Vite dla lepszej wydajności developerskiej.

2. **Zmiana hasła:** Endpoint `/api/v1/auth/change-password` istnieje w API, ale nie określono gdzie w UI umieścić tę funkcjonalność - rekomendacja: link "Zmień hasło" w dropdown przy przycisku wylogowania lub osobna strona `/settings`.

3. **Obsługa wygaśnięcia tokena (24h):** Nie określono czy pokazywać ostrzeżenie przed wygaśnięciem - rekomendacja dla MVP: po prostu wylogować przy 401 bez ostrzeżenia.

4. **Szczegóły pracownika:** Endpoint `GET /api/v1/admin/employees/{id}` istnieje, ale ustalono brak osobnych widoków szczegółów - do rozważenia czy potrzebny jest modal ze szczegółami pracownika.

5. **Walidacja dat biznesowych:** Nie określono czy walidować logikę dat (np. data zatrudnienia "do" nie może być przed "od", data zakończenia przydziału nie może być przed rozpoczęciem) - rekomendacja: dodać walidację client-side w Zod.

6. **Język interfejsu:** Wszystkie komunikaty i etykiety w języku polskim - do potwierdzenia, czy dotyczy to również etykiet formularzy (Imię i nazwisko vs Full name).

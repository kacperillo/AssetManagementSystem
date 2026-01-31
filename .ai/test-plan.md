# Plan Testowania Aplikacji Asset Management

## 1. Wprowadzenie i Cel Dokumentu

Niniejszy dokument określa strategię, zakres, cele i procesy testowania aplikacji Asset Management. Celem jest zapewnienie wysokiej jakości, niezawodności, wydajności i bezpieczeństwa systemu poprzez zintegrowane podejście do testowania na wszystkich poziomach aplikacji – od kodu źródłowego po gotowe środowisko produkcyjne. Plan ten stanowi fundament dla działań zespołów deweloperskich (Dev) oraz zapewnienia jakości (QA).

## 2. Analiza Architektury i Identyfikacja Ryzyk

### 2.1. Analiza Backendu (Spring Boot)

*   **Architektura:** Warstwowa (Controller → Service → Repository), co ułatwia izolowane testowanie poszczególnych komponentów.
*   **API:** RESTful, oparte na DTO (Data Transfer Objects), co definiuje jasne kontrakty do testowania.
*   **Bezpieczeństwo:** Zaimplementowane z użyciem Spring Security i JWT. Obejmuje uwierzytelnianie (login) oraz autoryzację opartą na rolach (Role.java, AdminRoute.tsx sugeruje istnienie ról USER i ADMIN).
*   **Dostęp do danych:** Spring Data JPA, co oznacza, że logika zapytań (zarówno domyślna, jak i customowa w `*Repository`) jest krytycznym punktem testów integracyjnych.
*   **Obsługa błędów:** Scentralizowana w `GlobalExceptionHandler`, co ułatwia testowanie spójnych odpowiedzi na błędy (np. 400, 404, 403).
*   **Zależności:** `pom.xml` definiuje zależności, które muszą być skanowane pod kątem luk bezpieczeństwa.

### 2.2. Analiza Frontendu (React)

*   **Struktura:** Komponentowa, z wyraźnym podziałem na strony (`pages`), komponenty reużywalne (`components`), logikę API (`api`) i zarządzanie stanem (`context`).
*   **Zarządzanie stanem:** `AuthContext` do globalnego stanu uwierzytelnienia. Pozostały stan prawdopodobnie zarządzany jest lokalnie lub przez hooki.
*   **Komunikacja z API:** Scentralizowana w `api/*.ts`, co ułatwia mockowanie i testowanie integracji z backendem.
*   **Routing:** React Router z logiką ochrony tras (`ProtectedRoute`, `AdminRoute`), co jest kluczowe dla testów E2E i bezpieczeństwa.
*   **UI/UX:** System zawiera komponenty do obsługi różnych stanów (ładowanie, błędy, brak danych), które muszą być przetestowane wizualnie i funkcjonalnie.

### 2.3. Krytyczne Obszary Ryzyka i Priorytety Testowe

1.  **Moduł Uwierzytelniania i Autoryzacji (NAJWYŻSZY PRIORYTET):** Błędy w tym obszarze mogą prowadzić do wycieku danych lub nieautoryzowanego dostępu.
2.  **Operacje CRUD na Zasobach (Assets, Employees, Assignments):** Rdzeń funkcjonalności aplikacji. Kluczowa jest integralność danych i poprawność logiki biznesowej.
3.  **Logika Biznesowa Przypisań:** Poprawne zarządzanie cyklem życia przypisania (tworzenie, kończenie), walidacja (np. niemożność przypisania już zajętego zasobu).
4.  **Walidacja Danych (Frontend + Backend):** Zabezpieczenie przed niepoprawnymi danymi na obu końcach.
5.  **Paginacja i Filtrowanie Danych:** Kluczowe dla wydajności i użyteczności przy dużej ilości danych.

## 3. Poziomy i Typy Testów

Poniżej znajduje się szczegółowy plan dla każdego typu testów.

### 3.1. Testy Jednostkowe (Unit Tests)

*   **Cel:** Weryfikacja poprawności działania najmniejszych, izolowanych fragmentów kodu (klas, metod, komponentów).
*   **Odpowiedzialność:** Deweloperzy.
*   **Kryteria wejścia:** Napisany kod funkcjonalny.
*   **Kryteria wyjścia:** 100% pokrycia kodu dla krytycznej logiki biznesowej, >80% dla całej bazy kodu. Testy uruchamiane automatycznie przy każdym `commicie`.

**Backend (Spring Boot):**
*   **Zakres:**
    *   Logika biznesowa w klasach `*Service`.
    *   Logika mapowania w `*Controller`.
    *   Klasy pomocnicze (`JwtUtil`).
*   **Przykładowe Scenariusze:**
    *   `AssetService`: Sprawdzenie, czy próba stworzenia zasobu z pustą nazwą rzuca wyjątek.
    *   `EmployeeService`: Weryfikacja, czy metoda `createEmployee` poprawnie hashuje hasło przed zapisem.
    *   `JwtUtil`: Testowanie generowania i walidacji tokena dla różnych ról.
*   **Narzędzia:** JUnit 5, Mockito. Zależności (np. repozytoria) muszą być mockowane.

**Frontend (React):**
*   **Zakres:**
    *   Reużywalne komponenty UI (`FormField`, `Pagination`).
    *   Funkcje pomocnicze i hooki (`useAuth`).
    *   Logika formatowania danych.
*   **Przykładowe Scenariusze:**
    *   Komponent `FormField`: Sprawdzenie, czy poprawnie wyświetla komunikat błędu, gdy otrzyma `error` w propsach.
    *   Komponent `EmptyState`: Weryfikacja, czy renderuje poprawny tekst i obrazek.
    *   Hook `useAuth`: Testowanie logiki `login` i `logout`, sprawdzając zmiany w zwracanym kontekście.
*   **Narzędzia:** Vitest, React Testing Library (RTL). Zależności (API, konteksty) muszą być mockowane.

### 3.2. Testy Integracyjne (Integration Tests) - Backend

*   **Cel:** Weryfikacja poprawnej współpracy między warstwami aplikacji backendowej (Controller ↔ Service ↔ Repository ↔ Baza Danych).
*   **Odpowiedzialność:** Deweloperzy.
*   **Kryteria wejścia:** Działające testy jednostkowe dla powiązanych komponentów.
*   **Kryteria wyjścia:** Kluczowe ścieżki przepływu danych zostały zweryfikowane.
*   **Zakres:**
    *   Pełen przepływ dla operacji CRUD (od żądania HTTP do zapisu w bazie).
    *   Działanie customowych zapytań JPA w `*Repository`.
    *   Integracja z mechanizmami Spring Security.
*   **Przykładowe Scenariusze:**
    *   Wysłanie żądania `POST /api/assets` i weryfikacja, czy w bazie danych (Testcontainers lub H2) pojawił się nowy rekord z poprawnymi danymi.
    *   Test customowego zapytania w `AssignmentRepository` do wyszukiwania aktywnych przypisań dla danego pracownika.
    *   Próba dostępu do endpointu admina jako zwykły użytkownik i weryfikacja odpowiedzi `403 Forbidden`.
*   **Narzędzia:** Spring Boot Test (`@SpringBootTest`), Testcontainers (dla realistycznej bazy danych, np. PostgreSQL), H2 (jako prostsza alternatywa).

### 3.3. Testy API (REST)

*   **Cel:** Weryfikacja kontraktu API z perspektywy klienta. Testowanie logiki biznesowej, walidacji i bezpieczeństwa bez dotykania UI.
*   **Odpowiedzialność:** QA (głównie), Deweloperzy.
*   **Kryteria wejścia:** Działający i wdrożony backend na środowisku testowym.
*   **Kryteria wyjścia:** Wszystkie endpointy API zostały zweryfikowane pod kątem kontraktu i logiki biznesowej.
*   **Zakres:** Wszystkie publiczne endpointy REST API.
*   **Przykładowe Scenariusze:**
    *   `POST /api/auth/login`: Wysłanie poprawnych i błędnych danych uwierzytelniających, weryfikacja odpowiedzi (token JWT / błąd 401).
    *   `GET /api/employees`: Sprawdzenie, czy odpowiedź jest zgodna ze schematem `PagedResponse<EmployeeResponse>`, czy paginacja (`?page=1&size=10`) działa poprawnie.
    *   `POST /api/assignments`: Próba stworzenia przypisania dla nieistniejącego zasobu i weryfikacja odpowiedzi `404 Not Found`.
    *   `DELETE /api/assets/{id}`: Sprawdzenie, czy po usunięciu zasobu, żądanie `GET` na jego ID zwraca `404`.
*   **Narzędzia:** Postman/Insomnia (do testów manualnych/eksploracyjnych), RestAssured (Java) lub Playwright (`request`) do automatyzacji.

### 3.4. Testy End-to-End (E2E)

*   **Cel:** Symulacja rzeczywistych scenariuszy użytkownika w przeglądarce, weryfikująca współpracę całego systemu (Frontend ↔ Backend ↔ Baza Danych).
*   **Odpowiedzialność:** QA.
*   **Kryteria wejścia:** W pełni zintegrowana i wdrożona aplikacja na środowisku testowym/stagingowym.
*   **Kryteria wyjścia:** Krytyczne ścieżki użytkownika (Happy Paths) działają poprawnie.
*   **Zakres:** Najważniejsze przepływy biznesowe w aplikacji.
*   **Przykładowe Scenariusze:**
    1.  **Logowanie i wylogowanie:** Użytkownik wchodzi na stronę, loguje się, widzi stronę główną, a następnie klika "Wyloguj".
    2.  **Cykl życia zasobu (rola Admin):**
        *   Admin loguje się.
        *   Nawiguje do strony "Assets".
        *   Klika "Add Asset", wypełnia formularz i zatwierdza.
        *   Sprawdza, czy nowy zasób pojawił się w tabeli.
        *   Edytuje zasób, zmieniając jego nazwę.
        *   Deaktywuje zasób i potwierdza operację w oknie dialogowym.
    3.  **Tworzenie przypisania (rola Admin):**
        *   Admin loguje się.
        *   Nawiguje do strony "Assignments", klika "Create Assignment".
        *   Wybiera pracownika i dostępny zasób z list, zatwierdza.
        *   Weryfikuje, czy nowe przypisanie jest widoczne na liście.
    4.  **Przeglądanie własnych zasobów (rola User):**
        *   Użytkownik loguje się.
        *   Nawiguje do "My Assets" i widzi tylko przypisane do niego zasoby.
*   **Narzędzia:** Playwright.

### 3.5. Testy Wydajnościowe (Performance Tests)

*   **Cel:** Ocena szybkości, responsywności i stabilności aplikacji pod obciążeniem.
*   **Odpowiedzialność:** QA / DevOps.
*   **Kryteria wejścia:** Stabilna wersja aplikacji na dedykowanym środowisku wydajnościowym.
*   **Kryteria wyjścia:** Aplikacja spełnia zdefiniowane wskaźniki (np. czas odpowiedzi < 200ms pod obciążeniem X).
*   **Zakres:**
    *   **Backend:** Kluczowe endpointy API (szczególnie te do odczytu list), endpoint logowania.
    *   **Frontend:** Czas ładowania strony (LCP), interaktywność (TBT), wydajność renderowania list.
*   **Przykładowe Scenariusze:**
    *   **Load Test:** Symulacja 100 jednoczesnych użytkowników przeglądających listy zasobów z paginacją.
    *   **Stress Test:** Stopniowe zwiększanie obciążenia aż do znalezienia punktu krytycznego aplikacji.
    *   **Frontend Analysis:** Pomiar metryk Core Web Vitals dla kluczowych stron.
*   **Narzędzia:** JMeter, Gatling (dla backendu), Google Lighthouse, WebPageTest (dla frontendu).

### 3.6. Testy Bezpieczeństwa (Security Tests)

*   **Cel:** Identyfikacja i eliminacja luk w zabezpieczeniach aplikacji.
*   **Odpowiedzialność:** QA / Specjalista ds. Bezpieczeństwa.
*   **Kryteria wejścia:** Działająca aplikacja na środowisku testowym.
*   **Kryteria wyjścia:** Znalezione podatności są zaraportowane i spriorytetyzowane.
*   **Zakres:** Uwierzytelnianie, autoryzacja, walidacja wejść, zarządzanie sesją, bezpieczeństwo zależności.
*   **Przykładowe Scenariusze:**
    *   **Broken Access Control:** Próba uzyskania dostępu do endpointu `/api/employees` (dla adminów) z tokenem JWT zwykłego użytkownika.
    *   **SQL Injection / XSS:** Próba wstrzyknięcia złośliwego kodu w polach formularzy (np. w nazwie zasobu).
    *   **Dependency Scanning:** Automatyczne skanowanie `pom.xml` i `package.json` w poszukiwaniu bibliotek ze znanymi podatnościami (CVE).
    *   **JWT Token Manipulation:** Próba modyfikacji payloadu tokena (np. zmiana roli) i użycia go w systemie.
*   **Narzędzia:** OWASP ZAP (dynamiczna analiza), Snyk / Dependabot (skanowanie zależności), testy manualne (pen-testing).

### 3.7. Testy Odporności i Obsługi Błędów (Resilience & Error Handling)

*   **Cel:** Weryfikacja, czy aplikacja zachowuje się w przewidywalny i przyjazny dla użytkownika sposób w sytuacjach awaryjnych.
*   **Odpowiedzialność:** QA, Deweloperzy.
*   **Kryteria wejścia:** Działająca aplikacja, możliwość symulowania błędów.
*   **Kryteria wyjścia:** Scenariusze awaryjne są obsługiwane bez "wykrzaczania" aplikacji.
*   **Zakres:** Reakcja frontendu na błędy API, zachowanie backendu przy niedostępności bazy danych.
*   **Przykładowe Scenariusze:**
    *   **Frontend:** Za pomocą mocka (np. w Playwright) symulacja odpowiedzi API z kodem `500 Internal Server Error` i weryfikacja, czy na UI pojawił się komponent `ErrorMessage`.
    *   **Frontend:** Symulacja powolnej odpowiedzi sieci i sprawdzenie, czy wyświetla się `LoadingSpinner`.
    *   **Backend:** Zatrzymanie kontenera z bazą danych i sprawdzenie, czy API zwraca spójny błąd `500` lub `503`, zamiast kończyć pracę.
*   **Narzędzia:** Playwright Intercepts, Mockito (do symulacji wyjątków w testach integracyjnych), Chaos Engineering tools (opcjonalnie).

## 4. Podsumowanie i Rekomendacje CI/CD

Przedstawiony plan opiera się na piramidzie testów automatycznych, kładąc nacisk na solidne fundamenty w postaci szybkich i tanich testów jednostkowych oraz integracyjnych. Testy E2E, choć kluczowe, powinny być ograniczone do najważniejszych ścieżek użytkownika ze względu na koszt ich utrzymania.

**Rekomendacje dotyczące integracji z CI/CD (np. GitHub Actions, Jenkins):**

*   **On Commit / Pull Request:**
    *   Uruchomienie wszystkich testów jednostkowych (backend + frontend).
    *   Uruchomienie testów integracyjnych (backend).
    *   Analiza statyczna kodu (linting) i skanowanie zależności (Snyk/Dependabot).
    *   **Build nie powinien przejść, jeśli którykolwiek z tych kroków zawiedzie.**
*   **On Merge to develop/main:**
    *   Uruchomienie testów API na świeżo zbudowanym środowisku.
    *   Uruchomienie testów E2E.
*   **Nightly / Weekly Builds:**
    *   Uruchomienie testów wydajnościowych w celu monitorowania trendów.
    *   Pełny skan bezpieczeństwa za pomocą OWASP ZAP.

Wdrożenie tej strategii zapewni systematyczne budowanie jakości na każdym etapie cyklu życia oprogramowania i pozwoli na szybkie, pewne dostarczanie wartościowych zmian na produkcję.
# Stack Technologiczny - AssetManagement

## Przegląd Architektury

Aplikacja AssetManagement to system zarządzania zasobami zbudowany w architekturze klient-serwer:
- **Backend**: REST API w Spring Boot (Java)
- **Frontend**: SPA w React (TypeScript)
- **Baza danych**: MySQL (produkcja) / H2 (testy)

---

## Backend

### Platforma i Język

| Technologia | Wersja | Opis |
|-------------|--------|------|
| **Java** | 21 | LTS, wirtualne wątki, pattern matching |
| **Maven** | - | Narzędzie budowania i zarządzania zależnościami |

### Spring Boot i Spring Framework

| Moduł | Wersja | Przeznaczenie |
|-------|--------|---------------|
| **Spring Boot** | 4.0.1 | Framework aplikacji |
| **Spring Framework** | 7.0.2 | Rdzeń (spring-context, spring-web, spring-webmvc, etc.) |
| **Spring Security** | 7.0.2 | Autentykacja i autoryzacja |
| **Spring Data JPA** | 4.0.1 | Warstwa persystencji, repozytoria |
| **Spring Data Commons** | 4.0.1 | Wspólne abstrakcje dla repozytoriów |

### Spring Boot Starters

| Starter | Wersja | Przeznaczenie |
|---------|--------|---------------|
| **spring-boot-starter-webmvc** | 4.0.1 | REST API, kontrolery HTTP |
| **spring-boot-starter-data-jpa** | 4.0.1 | JPA + Hibernate |
| **spring-boot-starter-security** | 4.0.1 | Bezpieczeństwo |
| **spring-boot-starter-validation** | 4.0.1 | Walidacja Bean Validation |
| **spring-boot-starter-actuator** | 4.0.1 | Monitoring i health checks |
| **spring-boot-devtools** | 4.0.1 | Hot reload podczas developmentu |

### Persystencja (JPA/Hibernate)

| Biblioteka | Wersja | Przeznaczenie |
|------------|--------|---------------|
| **Hibernate ORM** | 7.2.0.Final | Implementacja JPA |
| **Hibernate Validator** | 9.0.1.Final | Bean Validation |
| **Jakarta Persistence API** | 3.2.0 | Specyfikacja JPA |
| **Jakarta Validation API** | 3.1.1 | Specyfikacja walidacji |
| **HikariCP** | 7.0.2 | Connection pool |

### Bazy Danych

| Baza | Wersja Drivera | Dialect | Środowisko |
|------|----------------|---------|------------|
| **MySQL** | 9.5.0 (mysql-connector-j) | MySQLDialect | Produkcja |
| **H2** | 2.4.240 | H2Dialect | Testy E2E |

**Konfiguracja JPA/Hibernate:**
- DDL Auto: `update` (produkcja) / `create-drop` (testy)
- Show SQL: włączone
- Format SQL: włączone

### Bezpieczeństwo i Autoryzacja

| Biblioteka | Wersja | Przeznaczenie |
|------------|--------|---------------|
| **Spring Security Core** | 7.0.2 | Rdzeń security |
| **Spring Security Web** | 7.0.2 | Filtrowanie HTTP |
| **Spring Security Config** | 7.0.2 | Konfiguracja DSL |
| **jjwt-api** | 0.12.3 | JWT API (JSON Web Tokens) |
| **jjwt-impl** | 0.12.3 | Implementacja JWT |
| **jjwt-jackson** | 0.12.3 | Serializacja JWT z Jackson |

**Konfiguracja JWT:**
- Czas wygaśnięcia tokenu: 24h (86400000 ms)
- Algorytm: HMAC-SHA256

### Serwer WWW

| Biblioteka | Wersja | Przeznaczenie |
|------------|--------|---------------|
| **Apache Tomcat (embedded)** | 11.0.15 | Serwer HTTP |

### Serializacja JSON

| Biblioteka | Wersja | Przeznaczenie |
|------------|--------|---------------|
| **Jackson (tools.jackson)** | 3.0.3 | JSON dla Spring |
| **Jackson (fasterxml)** | 2.20.1 | JSON dla JJWT |

### Logging

| Biblioteka | Wersja | Przeznaczenie |
|------------|--------|---------------|
| **SLF4J** | 2.0.17 | Fasada logowania |
| **Logback** | 1.5.22 | Implementacja logowania |

### Monitoring

| Biblioteka | Wersja | Przeznaczenie |
|------------|--------|---------------|
| **Micrometer** | 1.16.1 | Metryki aplikacji |

### Narzędzia Developerskie

| Biblioteka | Wersja | Przeznaczenie |
|------------|--------|---------------|
| **Lombok** | 1.18.42 | Redukcja boilerplate (@Data, @Builder, etc.) |
| **JaCoCo** | 0.8.11 | Raportowanie pokrycia kodu testami |

---

## Frontend

### Platforma i Język

| Technologia | Wersja | Opis |
|-------------|--------|------|
| **TypeScript** | 5.9.3 | Statyczne typowanie dla JavaScript |
| **ES Target** | ES2022 | Docelowa wersja ECMAScript |
| **Node.js** | - | Runtime (wymagany do budowania) |

### Build Tool i Bundler

| Technologia | Wersja | Przeznaczenie |
|-------------|--------|---------------|
| **Vite** | 7.2.4 | Bundler i dev server |
| **@vitejs/plugin-react** | 5.1.1 | Plugin React dla Vite (Fast Refresh) |

**Konfiguracja Vite:**
- Port dev server: 5173
- Proxy API: `/api` -> `http://localhost:8080`

### Framework UI

| Biblioteka | Wersja | Przeznaczenie |
|------------|--------|---------------|
| **React** | 19.2.0 | Biblioteka UI (Concurrent Mode, Server Components) |
| **React DOM** | 19.2.0 | Renderer dla przeglądarki |

### Komponenty i Stylowanie

| Biblioteka | Wersja | Przeznaczenie |
|------------|--------|---------------|
| **@mui/material** | 7.3.7 | Komponenty Material Design |
| **@mui/icons-material** | 7.3.7 | Ikony Material Design |
| **@emotion/react** | 11.14.0 | CSS-in-JS (wymagane przez MUI) |
| **@emotion/styled** | 11.14.1 | Styled components (wymagane przez MUI) |

### Routing

| Biblioteka | Wersja | Przeznaczenie |
|------------|--------|---------------|
| **react-router-dom** | 7.13.0 | Routing SPA (data router, lazy loading) |

### Zarządzanie Stanem i Danymi

| Biblioteka | Wersja | Przeznaczenie |
|------------|--------|---------------|
| **@tanstack/react-query** | 5.90.20 | Cache serwera, synchronizacja danych |
| **axios** | 1.13.3 | Klient HTTP |

### Formularze i Walidacja

| Biblioteka | Wersja | Przeznaczenie |
|------------|--------|---------------|
| **react-hook-form** | 7.71.1 | Zarządzanie formularzami |
| **zod** | 3.25.76 | Walidacja schematów TypeScript-first |
| **@hookform/resolvers** | 5.2.2 | Integracja Zod z React Hook Form |

### Autoryzacja (Frontend)

| Biblioteka | Wersja | Przeznaczenie |
|------------|--------|---------------|
| **jwt-decode** | 4.0.0 | Dekodowanie tokenów JWT |

---

## Testowanie

### Testy Jednostkowe (Frontend)

| Biblioteka | Wersja | Przeznaczenie |
|------------|--------|---------------|
| **Vitest** | 3.2.3 | Test runner (kompatybilny z Jest API) |
| **@vitest/coverage-v8** | 3.2.4 | Raportowanie pokrycia kodu |
| **@testing-library/react** | 16.3.0 | Testowanie komponentów React |
| **@testing-library/user-event** | 14.6.1 | Symulacja interakcji użytkownika |
| **@testing-library/jest-dom** | 6.6.3 | Custom matchers dla DOM |
| **jsdom** | 26.1.0 | Implementacja DOM dla Node.js |

**Konfiguracja Vitest:**
- Environment: jsdom
- Globals: włączone
- Setup file: `./src/test/setup.ts`

### Testy E2E

| Biblioteka | Wersja | Przeznaczenie |
|------------|--------|---------------|
| **@playwright/test** | 1.58.0 | Framework testów E2E |

**Konfiguracja Playwright:**
- Test directory: `./e2e`
- Browser: Chromium (Desktop Chrome)
- Base URL: `http://localhost:5173`
- Parallel: wyłączone (workers: 1)
- Retries: 2 (CI) / 0 (local)
- Artifacts: screenshots on failure, video retain-on-failure

**Web Servers (automatyczne uruchamianie):**
1. Backend: `mvn spring-boot:run -Dspring-boot.run.profiles=e2e` (port 8080)
2. Frontend: `npm run dev` (port 5173)

### Testy Jednostkowe (Backend)

| Biblioteka | Wersja | Przeznaczenie |
|------------|--------|---------------|
| **JUnit Jupiter** | 6.0.1 | Framework testów |
| **Mockito** | 5.20.0 | Mockowanie zależności |
| **AssertJ** | 3.27.6 | Fluent assertions |
| **Spring Test** | 7.0.2 | Wsparcie testów Spring |
| **Spring Security Test** | 7.0.2 | Testowanie security (@WithMockUser) |

**Spring Boot Test Starters (4.0.1):**
- `spring-boot-starter-test` - podstawowy starter testowy
- `spring-boot-starter-data-jpa-test` - testy repozytoriów (@DataJpaTest)
- `spring-boot-starter-security-test` - testy security
- `spring-boot-starter-webmvc-test` - testy kontrolerów (@WebMvcTest)

---

## Narzędzia Developerskie

### Linting i Formatowanie (Frontend)

| Narzędzie | Wersja | Przeznaczenie |
|-----------|--------|---------------|
| **ESLint** | 9.39.1 | Linter JavaScript/TypeScript |
| **typescript-eslint** | 8.46.4 | Parser i reguły dla TypeScript |
| **eslint-plugin-react-hooks** | 7.0.1 | Reguły dla React Hooks |
| **eslint-plugin-react-refresh** | 0.4.24 | Reguły dla Fast Refresh |
| **globals** | 16.5.0 | Definicje globalnych zmiennych |

### Typy TypeScript

| Pakiet | Wersja | Przeznaczenie |
|--------|--------|---------------|
| **@types/react** | 19.2.5 | Typy dla React |
| **@types/react-dom** | 19.2.3 | Typy dla React DOM |
| **@types/node** | 24.10.1 | Typy dla Node.js API |

---

## Skrypty NPM

```bash
# Development
npm run dev          # Uruchom dev server (Vite)
npm run build        # Buduj produkcję (tsc + vite build)
npm run preview      # Podgląd buildu produkcyjnego
npm run lint         # Uruchom ESLint

# Testy jednostkowe
npm run test         # Uruchom testy (watch mode)
npm run test:ui      # Testy z UI Vitest
npm run test:coverage # Testy z raportem pokrycia

# Testy E2E
npm run e2e          # Uruchom testy Playwright
npm run e2e:ui       # Testy z UI Playwright
npm run e2e:headed   # Testy w widocznej przeglądarce
npm run e2e:debug    # Testy w trybie debug
npm run e2e:report   # Pokaż raport HTML
```

---

## Komendy Maven

```bash
# Development
mvn spring-boot:run                              # Uruchom aplikację
mvn spring-boot:run -Dspring-boot.run.profiles=e2e  # Uruchom z profilem e2e

# Build
mvn clean package                                # Zbuduj JAR
mvn clean package -DskipTests                    # Zbuduj bez testów

# Testy
mvn test                                         # Uruchom testy
mvn verify                                       # Testy + raport JaCoCo

# Dependency info
mvn dependency:tree                              # Pokaż drzewo zależności
```

---

## Porty i URL-e

| Serwis | Port | URL |
|--------|------|-----|
| Backend API | 8080 | `http://localhost:8080/api/v1/` |
| Frontend Dev | 5173 | `http://localhost:5173` |
| H2 Console (e2e) | 8080 | `http://localhost:8080/h2-console` |
| MySQL | 3306 | `jdbc:mysql://localhost:3306/assetmanagement` |

---

## Podsumowanie Wersji Głównych

| Kategoria | Technologia | Wersja |
|-----------|-------------|--------|
| Backend | Java | 21 |
| Backend | Spring Boot | 4.0.1 |
| Backend | Spring Framework | 7.0.2 |
| Backend | Spring Security | 7.0.2 |
| Backend | Hibernate ORM | 7.2.0.Final |
| Backend | Tomcat | 11.0.15 |
| Backend | Lombok | 1.18.42 |
| DB | MySQL Connector | 9.5.0 |
| DB | H2 | 2.4.240 |
| Frontend | React | 19.2.0 |
| Frontend | TypeScript | 5.9.3 |
| Frontend | Vite | 7.2.4 |
| UI | Material UI | 7.3.7 |
| State | TanStack Query | 5.90.20 |
| Routing | React Router | 7.13.0 |
| Unit Tests (FE) | Vitest | 3.2.3 |
| Unit Tests (BE) | JUnit Jupiter | 6.0.1 |
| Unit Tests (BE) | Mockito | 5.20.0 |
| E2E Tests | Playwright | 1.58.0 |

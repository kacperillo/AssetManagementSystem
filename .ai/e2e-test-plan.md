# Plan Testów E2E - Asset Management

## Spis treści
1. [Przegląd](#1-przegląd)
2. [Konfiguracja środowiska testowego](#2-konfiguracja-środowiska-testowego)
3. [Konfiguracja Playwright](#3-konfiguracja-playwright)
4. [Scenariusze testowe E2E](#4-scenariusze-testowe-e2e)
5. [Instrukcja implementacji krok po kroku](#5-instrukcja-implementacji-krok-po-kroku)
6. [Potencjalne kolejne scenariusze](#6-potencjalne-kolejne-scenariusze)
7. [Funkcjonalności nieobjęte testami](#7-funkcjonalności-nieobjęte-testami)

---

## 1. Przegląd

### Cel dokumentu
Dokument opisuje plan wdrożenia testów End-to-End (E2E) dla aplikacji Asset Management z wykorzystaniem narzędzia Playwright.

### Zakres testów
- **Maksymalnie 5 scenariuszy** pokrywających krytyczne ścieżki użytkownika
- **Środowisko testowe** z bazą H2 zamiast MySQL
- **Narzędzie**: Playwright

### Stan obecny
- Brak konfiguracji Playwright
- Brak testów E2E
- Brak profilu testowego z H2
- Istniejące testy jednostkowe (JUnit + Vitest)

---

## 2. Konfiguracja środowiska testowego

### 2.1. Dodanie zależności H2 do backendu

**Plik**: `pom.xml`

Dodać w sekcji `<dependencies>`:

```xml
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>runtime</scope>
</dependency>
```

> **Uwaga**: Scope `runtime` pozwala używać H2 zarówno w profilu e2e jak i do uruchomienia aplikacji testowo.

### 2.2. Utworzenie profilu testowego

**Plik do utworzenia**: `src/main/resources/application-e2e.yaml`

```yaml
spring:
  datasource:
    url: jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE;MODE=MySQL
    username: sa
    password:
    driver-class-name: org.h2.Driver

  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.H2Dialect

  h2:
    console:
      enabled: true
      path: /h2-console

  sql:
    init:
      mode: always
      data-locations: classpath:data-e2e.sql

jwt:
  secret: eec6fbed57322cfb2fff873b8495121553bd4b0cbf7153f067a8edf3cba41da2
  expiration: 86400000

server:
  port: 8080
```

### 2.3. Dane testowe (seed)

**Plik do utworzenia**: `src/main/resources/data-e2e.sql`

```sql
-- =====================================================
-- DANE TESTOWE E2E - Asset Management
-- =====================================================

-- Admin (hasło: admin123)
INSERT INTO employees (id, full_name, email, password, role, hired_from, hired_until)
VALUES (1, 'Test Admin', 'admin@test.com',
        '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi',
        'ADMIN', '2023-01-01', NULL);

-- Pracownik (hasło: employee123)
INSERT INTO employees (id, full_name, email, password, role, hired_from, hired_until)
VALUES (2, 'Test Employee', 'employee@test.com',
        '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
        'EMPLOYEE', '2023-06-15', NULL);

-- Zasoby testowe
INSERT INTO assets (id, asset_type, vendor, model, series_number, is_active)
VALUES (1, 'LAPTOP', 'Dell', 'XPS 15', 'SN-LAPTOP-001', true);

INSERT INTO assets (id, asset_type, vendor, model, series_number, is_active)
VALUES (2, 'SMARTPHONE', 'Apple', 'iPhone 14', 'SN-PHONE-001', true);

INSERT INTO assets (id, asset_type, vendor, model, series_number, is_active)
VALUES (3, 'TABLET', 'Samsung', 'Galaxy Tab S9', 'SN-TABLET-001', true);

INSERT INTO assets (id, asset_type, vendor, model, series_number, is_active)
VALUES (4, 'PRINTER', 'HP', 'LaserJet Pro', 'SN-PRINTER-001', true);

INSERT INTO assets (id, asset_type, vendor, model, series_number, is_active)
VALUES (5, 'HEADPHONES', 'Sony', 'WH-1000XM5', 'SN-HEADPHONES-001', false);

-- Przypisanie (laptop przypisany do pracownika)
INSERT INTO assignments (id, employee_id, asset_id, assigned_from, assigned_until)
VALUES (1, 2, 1, '2024-01-15', NULL);
```

### 2.4. Weryfikacja środowiska

Uruchomienie backendu z profilem e2e:
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=e2e
```

Sprawdzenie:
- H2 Console: `http://localhost:8080/h2-console`
- Login endpoint: `POST http://localhost:8080/api/v1/auth/login`

---

## 3. Konfiguracja Playwright

### 3.1. Instalacja Playwright

```bash
cd frontend
npm install -D @playwright/test
npx playwright install chromium firefox
```

### 3.2. Konfiguracja Playwright

**Plik do utworzenia**: `frontend/playwright.config.ts`

```typescript
import { defineConfig, devices } from '@playwright/test';

export default defineConfig({
  testDir: './e2e',
  fullyParallel: false,
  forbidOnly: !!process.env.CI,
  retries: process.env.CI ? 2 : 0,
  workers: 1,
  reporter: [
    ['html', { outputFolder: 'playwright-report' }],
    ['list'],
  ],
  use: {
    baseURL: 'http://localhost:5173',
    trace: 'on-first-retry',
    screenshot: 'only-on-failure',
    video: 'retain-on-failure',
  },
  projects: [
    {
      name: 'chromium',
      use: { ...devices['Desktop Chrome'] },
    },
  ],
  webServer: [
    {
      command: 'cd .. && mvn spring-boot:run -Dspring-boot.run.profiles=e2e',
      url: 'http://localhost:8080/api/v1/auth/login',
      reuseExistingServer: !process.env.CI,
      timeout: 120000,
    },
    {
      command: 'npm run dev',
      url: 'http://localhost:5173',
      reuseExistingServer: !process.env.CI,
      timeout: 60000,
    },
  ],
  timeout: 30000,
  expect: { timeout: 10000 },
});
```

### 3.3. Skrypty w package.json

Dodać do `frontend/package.json`:

```json
{
  "scripts": {
    "e2e": "playwright test",
    "e2e:ui": "playwright test --ui",
    "e2e:headed": "playwright test --headed",
    "e2e:debug": "playwright test --debug",
    "e2e:report": "playwright show-report"
  }
}
```

### 3.4. Struktura katalogów

```
frontend/
└── e2e/
    ├── fixtures/
    │   ├── auth.fixture.ts      # Fixture do logowania
    │   └── test-data.ts         # Stałe dane testowe
    ├── pages/
    │   ├── login.page.ts        # Page Object - Login
    │   ├── assets.page.ts       # Page Object - Assets
    │   └── assignments.page.ts  # Page Object - Assignments
    └── tests/
        ├── 01-authentication.spec.ts
        ├── 02-asset-management.spec.ts
        ├── 03-employee-management.spec.ts
        ├── 04-assignment-workflow.spec.ts
        └── 05-employee-view.spec.ts
```

### 3.5. Pliki pomocnicze

**Plik**: `frontend/e2e/fixtures/test-data.ts`

```typescript
export const TEST_USERS = {
  admin: {
    email: 'admin@test.com',
    password: 'admin123',
    role: 'ADMIN' as const,
  },
  employee: {
    email: 'employee@test.com',
    password: 'employee123',
    role: 'EMPLOYEE' as const,
  },
};

export const TEST_ASSETS = {
  laptop: { id: 1, seriesNumber: 'SN-LAPTOP-001', vendor: 'Dell', model: 'XPS 15' },
  smartphone: { id: 2, seriesNumber: 'SN-PHONE-001', vendor: 'Apple', model: 'iPhone 14' },
  tablet: { id: 3, seriesNumber: 'SN-TABLET-001', vendor: 'Samsung', model: 'Galaxy Tab S9' },
};
```

**Plik**: `frontend/e2e/fixtures/auth.fixture.ts`

```typescript
import { test as base, Page } from '@playwright/test';
import { TEST_USERS } from './test-data';

export const test = base.extend<{
  adminPage: Page;
  employeePage: Page;
}>({
  adminPage: async ({ page }, use) => {
    await page.goto('/login');
    await page.getByLabel('Email').fill(TEST_USERS.admin.email);
    await page.getByLabel(/Hasło/i).fill(TEST_USERS.admin.password);
    await page.getByRole('button', { name: /zaloguj/i }).click();
    await page.waitForURL('**/assets');
    await use(page);
  },
  employeePage: async ({ page }, use) => {
    await page.goto('/login');
    await page.getByLabel('Email').fill(TEST_USERS.employee.email);
    await page.getByLabel(/Hasło/i).fill(TEST_USERS.employee.password);
    await page.getByRole('button', { name: /zaloguj/i }).click();
    await page.waitForURL('**/my-assets');
    await use(page);
  },
});

export { expect } from '@playwright/test';
```

---

## 4. Scenariusze testowe E2E

### Scenariusz 1: Uwierzytelnianie użytkownika

**Plik**: `frontend/e2e/tests/01-authentication.spec.ts`

| ID | Przypadek testowy | Kroki | Oczekiwany rezultat |
|----|-------------------|-------|---------------------|
| AUTH-01 | Poprawne logowanie jako Admin | 1. Otwórz /login<br>2. Wpisz admin@test.com<br>3. Wpisz hasło admin123<br>4. Kliknij "Zaloguj" | Przekierowanie na /assets |
| AUTH-02 | Poprawne logowanie jako Pracownik | 1. Otwórz /login<br>2. Wpisz employee@test.com<br>3. Wpisz hasło employee123<br>4. Kliknij "Zaloguj" | Przekierowanie na /my-assets |
| AUTH-03 | Błędne dane logowania | 1. Otwórz /login<br>2. Wpisz nieprawidłowe dane<br>3. Kliknij "Zaloguj" | Komunikat błędu, pozostanie na /login |
| AUTH-04 | Wylogowanie | 1. Zaloguj się<br>2. Kliknij "Wyloguj" | Przekierowanie na /login |

```typescript
import { test, expect } from '@playwright/test';
import { TEST_USERS } from '../fixtures/test-data';

test.describe('Uwierzytelnianie', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/login');
    await page.evaluate(() => localStorage.clear());
  });

  test('AUTH-01: Poprawne logowanie jako Admin', async ({ page }) => {
    await page.getByLabel('Email').fill(TEST_USERS.admin.email);
    await page.getByLabel(/Hasło/i).fill(TEST_USERS.admin.password);
    await page.getByRole('button', { name: /zaloguj/i }).click();

    await expect(page).toHaveURL(/\/assets/);
    await expect(page.getByRole('heading', { name: 'Zasoby' })).toBeVisible();
  });

  test('AUTH-02: Poprawne logowanie jako Pracownik', async ({ page }) => {
    await page.getByLabel('Email').fill(TEST_USERS.employee.email);
    await page.getByLabel(/Hasło/i).fill(TEST_USERS.employee.password);
    await page.getByRole('button', { name: /zaloguj/i }).click();

    await expect(page).toHaveURL(/\/my-assets/);
  });

  test('AUTH-03: Błędne dane logowania', async ({ page }) => {
    await page.getByLabel('Email').fill('wrong@email.com');
    await page.getByLabel(/Hasło/i).fill('wrongpassword');
    await page.getByRole('button', { name: /zaloguj/i }).click();

    await expect(page.getByRole('alert')).toBeVisible();
    await expect(page).toHaveURL(/\/login/);
  });

  test('AUTH-04: Wylogowanie', async ({ page }) => {
    // Logowanie
    await page.getByLabel('Email').fill(TEST_USERS.admin.email);
    await page.getByLabel(/Hasło/i).fill(TEST_USERS.admin.password);
    await page.getByRole('button', { name: /zaloguj/i }).click();
    await expect(page).toHaveURL(/\/assets/);

    // Wylogowanie
    await page.getByRole('button', { name: /wyloguj/i }).click();
    await expect(page).toHaveURL(/\/login/);
  });
});
```

---

### Scenariusz 2: Zarządzanie zasobami (Admin)

**Plik**: `frontend/e2e/tests/02-asset-management.spec.ts`

| ID | Przypadek testowy | Kroki | Oczekiwany rezultat |
|----|-------------------|-------|---------------------|
| ASSET-01 | Wyświetlenie listy zasobów | 1. Zaloguj jako Admin<br>2. Przejdź do /assets | Tabela z zasobami widoczna |
| ASSET-02 | Dodanie nowego zasobu | 1. Kliknij "Dodaj zasób"<br>2. Wypełnij formularz<br>3. Zatwierdź | Zasób pojawia się w tabeli |
| ASSET-03 | Filtrowanie zasobów | 1. Wybierz filtr "Aktywne"<br>2. Sprawdź wyniki | Tylko aktywne zasoby widoczne |

```typescript
import { test, expect } from '../fixtures/auth.fixture';

test.describe('Zarządzanie zasobami (Admin)', () => {
  test('ASSET-01: Wyświetlenie listy zasobów', async ({ adminPage: page }) => {
    await expect(page.getByRole('heading', { name: 'Zasoby' })).toBeVisible();
    await expect(page.locator('table')).toBeVisible();
    await expect(page.getByRole('button', { name: /dodaj zasób/i })).toBeVisible();
  });

  test('ASSET-02: Dodanie nowego zasobu', async ({ adminPage: page }) => {
    const uniqueSerial = `SN-E2E-${Date.now()}`;

    // Otwórz modal
    await page.getByRole('button', { name: /dodaj zasób/i }).click();
    await expect(page.getByRole('heading', { name: /dodaj zasób/i })).toBeVisible();

    // Wypełnij formularz
    await page.getByLabel('Typ zasobu').click();
    await page.getByRole('option', { name: /tablet/i }).click();
    await page.getByLabel('Producent').fill('Microsoft');
    await page.getByLabel('Model').fill('Surface Pro');
    await page.getByLabel('Numer seryjny').fill(uniqueSerial);

    // Zatwierdź
    await page.getByRole('button', { name: /^dodaj$/i }).click();

    // Weryfikacja
    await expect(page.getByRole('cell', { name: uniqueSerial })).toBeVisible({ timeout: 5000 });
  });

  test('ASSET-03: Filtrowanie zasobów po statusie', async ({ adminPage: page }) => {
    // Filtruj aktywne
    await page.getByRole('radio', { name: /aktywne/i }).click();
    await page.waitForTimeout(500);

    // Sprawdź czy nieaktywne zasoby nie są widoczne
    await expect(page.getByText('SN-HEADPHONES-001')).not.toBeVisible();
  });
});
```

---

### Scenariusz 3: Zarządzanie pracownikami (Admin)

**Plik**: `frontend/e2e/tests/03-employee-management.spec.ts`

| ID | Przypadek testowy | Kroki | Oczekiwany rezultat |
|----|-------------------|-------|---------------------|
| EMP-01 | Wyświetlenie listy pracowników | 1. Przejdź do /employees | Tabela z pracownikami widoczna |
| EMP-02 | Dodanie nowego pracownika | 1. Kliknij "Dodaj pracownika"<br>2. Wypełnij formularz<br>3. Zatwierdź | Pracownik pojawia się w tabeli |

```typescript
import { test, expect } from '../fixtures/auth.fixture';

test.describe('Zarządzanie pracownikami (Admin)', () => {
  test('EMP-01: Wyświetlenie listy pracowników', async ({ adminPage: page }) => {
    await page.goto('/employees');

    await expect(page.getByRole('heading', { name: 'Pracownicy' })).toBeVisible();
    await expect(page.locator('table')).toBeVisible();
    await expect(page.getByRole('button', { name: /dodaj pracownika/i })).toBeVisible();
  });

  test('EMP-02: Dodanie nowego pracownika', async ({ adminPage: page }) => {
    await page.goto('/employees');
    const uniqueEmail = `e2e.user.${Date.now()}@test.com`;

    // Otwórz modal
    await page.getByRole('button', { name: /dodaj pracownika/i }).click();

    // Wypełnij formularz
    await page.getByLabel(/imię i nazwisko/i).fill('Nowy Pracownik E2E');
    await page.getByLabel('Email').fill(uniqueEmail);
    await page.getByLabel(/hasło/i).fill('testpass123');
    await page.getByLabel('Rola').click();
    await page.getByRole('option', { name: /pracownik/i }).click();
    await page.getByLabel(/data zatrudnienia od/i).fill('2024-01-01');

    // Zatwierdź
    await page.getByRole('button', { name: /^dodaj$/i }).click();

    // Weryfikacja
    await expect(page.getByRole('cell', { name: uniqueEmail })).toBeVisible({ timeout: 5000 });
  });
});
```

---

### Scenariusz 4: Workflow przypisań (Admin)

**Plik**: `frontend/e2e/tests/04-assignment-workflow.spec.ts`

| ID | Przypadek testowy | Kroki | Oczekiwany rezultat |
|----|-------------------|-------|---------------------|
| ASSIGN-01 | Wyświetlenie listy przypisań | 1. Przejdź do /assignments | Tabela z przypisaniami widoczna |
| ASSIGN-02 | Utworzenie nowego przypisania | 1. Kliknij "Utwórz przydział"<br>2. Wybierz pracownika i zasób<br>3. Zatwierdź | Przydział pojawia się w tabeli |

```typescript
import { test, expect } from '../fixtures/auth.fixture';

test.describe('Workflow przypisań (Admin)', () => {
  test('ASSIGN-01: Wyświetlenie listy przypisań', async ({ adminPage: page }) => {
    await page.goto('/assignments');

    await expect(page.getByRole('heading', { name: 'Przydziały' })).toBeVisible();
    await expect(page.locator('table')).toBeVisible();
    await expect(page.getByRole('button', { name: /utwórz przydział/i })).toBeVisible();
  });

  test('ASSIGN-02: Utworzenie nowego przypisania', async ({ adminPage: page }) => {
    await page.goto('/assignments');

    // Otwórz modal
    await page.getByRole('button', { name: /utwórz przydział/i }).click();
    await expect(page.getByRole('heading', { name: /utwórz przydział/i })).toBeVisible();

    // Wybierz pracownika
    await page.getByLabel('Pracownik').click();
    await page.getByRole('option', { name: /test employee/i }).click();

    // Wybierz dostępny zasób (smartphone - nieprzypisany)
    await page.getByLabel('Zasób').click();
    await page.getByRole('option', { name: /iphone/i }).click();

    // Zatwierdź
    await page.getByRole('button', { name: /^utwórz$/i }).click();

    // Weryfikacja
    await expect(page.locator('table').getByText('iPhone 14')).toBeVisible({ timeout: 5000 });
  });
});
```

---

### Scenariusz 5: Widok pracownika (My Assets)

**Plik**: `frontend/e2e/tests/05-employee-view.spec.ts`

| ID | Przypadek testowy | Kroki | Oczekiwany rezultat |
|----|-------------------|-------|---------------------|
| VIEW-01 | Wyświetlenie własnych zasobów | 1. Zaloguj jako Pracownik | Widoczna lista przypisanych zasobów |
| VIEW-02 | Brak dostępu do stron admina | 1. Zaloguj jako Pracownik<br>2. Spróbuj wejść na /employees | Przekierowanie lub brak dostępu |

```typescript
import { test, expect } from '../fixtures/auth.fixture';
import { TEST_ASSETS } from '../fixtures/test-data';

test.describe('Widok pracownika', () => {
  test('VIEW-01: Wyświetlenie własnych zasobów', async ({ employeePage: page }) => {
    await expect(page.getByRole('heading', { name: /moje zasoby/i })).toBeVisible();
    await expect(page.locator('table')).toBeVisible();

    // Pracownik ma przypisany laptop Dell XPS 15
    await expect(page.getByText(TEST_ASSETS.laptop.seriesNumber)).toBeVisible();
  });

  test('VIEW-02: Brak dostępu do stron admina', async ({ employeePage: page }) => {
    // Próba wejścia na stronę admina
    await page.goto('/employees');

    // Powinno przekierować na /my-assets
    await expect(page).not.toHaveURL(/\/employees$/);
    await expect(page).toHaveURL(/\/my-assets/);
  });
});
```

---

## 5. Instrukcja implementacji krok po kroku

### Faza 1: Konfiguracja backendu

| # | Krok | Opis |
|---|------|------|
| 1 | Dodać zależność H2 | W `pom.xml` dodać dependency H2 ze scope `runtime` |
| 2 | Utworzyć profil e2e | Utworzyć plik `src/main/resources/application-e2e.yaml` |
| 3 | Utworzyć dane seedowe | Utworzyć plik `src/main/resources/data-e2e.sql` |
| 4 | Zweryfikować backend | Uruchomić backend z profilem e2e i przetestować logowanie |

**Weryfikacja:**
```bash
# Uruchom backend z profilem e2e
mvn spring-boot:run -Dspring-boot.run.profiles=e2e

# Test logowania (w osobnym terminalu)
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@test.com","password":"admin123"}'
```

### Faza 2: Konfiguracja Playwright

| # | Krok | Opis |
|---|------|------|
| 1 | Zainstalować Playwright | `npm install -D @playwright/test` |
| 2 | Zainstalować przeglądarki | `npx playwright install chromium firefox` |
| 3 | Utworzyć konfigurację | Utworzyć plik `frontend/playwright.config.ts` |
| 4 | Dodać skrypty | Dodać skrypty e2e do `package.json` |
| 5 | Utworzyć strukturę | Utworzyć katalogi `e2e/fixtures`, `e2e/pages`, `e2e/tests` |
| 6 | Utworzyć fixtures | Utworzyć pliki `test-data.ts` i `auth.fixture.ts` |

### Faza 3: Implementacja testów

| # | Krok | Opis |
|---|------|------|
| 1 | Test uwierzytelniania | Zaimplementować `01-authentication.spec.ts` |
| 2 | Test zasobów | Zaimplementować `02-asset-management.spec.ts` |
| 3 | Test pracowników | Zaimplementować `03-employee-management.spec.ts` |
| 4 | Test przypisań | Zaimplementować `04-assignment-workflow.spec.ts` |
| 5 | Test widoku pracownika | Zaimplementować `05-employee-view.spec.ts` |

**Uruchamianie testów:**
```bash
npm run e2e                              # Wszystkie testy
npm run e2e -- 01-authentication.spec.ts # Konkretny plik
npm run e2e:ui                           # Z interfejsem graficznym
npm run e2e:debug                        # Tryb debug
```

### Faza 4: Finalizacja

| # | Krok | Opis |
|---|------|------|
| 1 | Naprawić błędy | Rozwiązać ewentualne problemy w testach |
| 2 | Code review | Przegląd kodu testów |
| 3 | Dokumentacja | Zaktualizować README projektu |
| 4 | CI/CD (opcjonalnie) | Przygotować workflow GitHub Actions |

---

## 6. Potencjalne kolejne scenariusze

### Wysoki priorytet

| ID | Scenariusz | Opis |
|----|------------|------|
| NEXT-01 | Zmiana hasła | Test procesu zmiany hasła przez użytkownika |
| NEXT-02 | Dezaktywacja zasobu | Weryfikacja że przypisany zasób nie może być dezaktywowany |
| NEXT-03 | Zakończenie przypisania | Test workflow zakończenia aktywnego przypisania |
| NEXT-04 | Paginacja | Testowanie przewijania stron w tabelach |

### Średni priorytet

| ID | Scenariusz | Opis |
|----|------------|------|
| NEXT-05 | Walidacja formularzy | Kompleksowe testy walidacji pól |
| NEXT-06 | Wygaśnięcie sesji | Zachowanie po wygaśnięciu tokena JWT |
| NEXT-07 | Kombinacje filtrów | Testowanie złożonych filtrów |
| NEXT-08 | Historia przypisań pracownika | Test strony /my-history |

### Niski priorytet

| ID | Scenariusz | Opis |
|----|------------|------|
| NEXT-09 | Dostępność (a11y) | Testy nawigacji klawiaturą |
| NEXT-10 | Responsywność | Testy na różnych rozdzielczościach |
| NEXT-11 | Wydajność | Pomiar czasów ładowania stron |

---

## 7. Funkcjonalności nieobjęte testami

### Świadomie wyłączone z zakresu E2E

| Funkcjonalność | Powód wyłączenia | Pokrycie alternatywne |
|----------------|------------------|----------------------|
| Logika biznesowa serwisów | Zbyt niskopoziomowe dla E2E | Testy jednostkowe (JUnit) |
| Walidacja DTO | Szczegóły implementacji | Testy integracyjne |
| Hashowanie haseł | Bezpieczeństwo infrastruktury | Testy jednostkowe |
| Generowanie JWT | Szczegóły implementacji | Testy JwtUtilTest |
| Zapytania SQL/JPA | Warstwa dostępu do danych | Testy integracyjne |

### Wymagające osobnych narzędzi

| Funkcjonalność | Rekomendowane narzędzie |
|----------------|-------------------------|
| Testy obciążeniowe | JMeter, k6 |
| Testy bezpieczeństwa | OWASP ZAP |
| Testy regresji wizualnej | Percy, Playwright Visual Comparisons |
| Skanowanie zależności | Snyk, Dependabot |

### Nieistniejące w aplikacji

| Funkcjonalność | Status |
|----------------|--------|
| Upload/download plików | Brak w aplikacji |
| Powiadomienia email | Brak w aplikacji |
| Integracje zewnętrzne | Brak w aplikacji |
| WebSockets / real-time | Brak w aplikacji |

---

## Podsumowanie

Plan obejmuje **5 scenariuszy E2E** pokrywających:
1. Uwierzytelnianie (logowanie/wylogowanie)
2. Zarządzanie zasobami (CRUD)
3. Zarządzanie pracownikami (CRUD)
4. Workflow przypisań
5. Widok pracownika i kontrola dostępu

**Kluczowe pliki do utworzenia/modyfikacji:**
- `pom.xml` - dodanie H2
- `src/main/resources/application-e2e.yaml` - profil testowy
- `src/main/resources/data-e2e.sql` - dane seedowe
- `frontend/playwright.config.ts` - konfiguracja Playwright
- `frontend/package.json` - skrypty e2e
- `frontend/e2e/**` - testy i pliki pomocnicze


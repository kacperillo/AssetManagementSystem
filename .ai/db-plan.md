# Dokumentacja Bazy Danych - Asset Management System

## 1. Lista Tabel

### 1.1 Tabela `employees`

Przechowuje informacje o pracownikach organizacji.

| Kolumna | Typ danych | Ograniczenia | Opis |
|---------|------------|--------------|------|
| `id` | BIGINT | PRIMARY KEY, NOT NULL | Unikalny identyfikator pracownika (generowany sekwencją) |
| `full_name` | VARCHAR(255) | NOT NULL | Pełne imię i nazwisko pracownika |
| `email` | VARCHAR(255) | NOT NULL, UNIQUE | Adres email pracownika (unikalny w systemie) |
| `password` | VARCHAR(255) | NOT NULL | Zahashowane hasło pracownika |
| `role` | VARCHAR(50) | NOT NULL | Rola pracownika w systemie (ENUM: ADMIN, EMPLOYEE) |
| `hired_from` | DATE | NOT NULL | Data rozpoczęcia zatrudnienia |
| `hired_until` | DATE | NULL | Data zakończenia zatrudnienia (NULL = aktywny pracownik) |

**Sekwencja:** `employee_sequence` (wartość początkowa: 1000, krok alokacji: 1)

---

### 1.2 Tabela `assets`

Przechowuje informacje o zasobach IT (sprzęcie) w organizacji.

| Kolumna | Typ danych | Ograniczenia | Opis |
|---------|------------|--------------|------|
| `id` | BIGINT | PRIMARY KEY, NOT NULL | Unikalny identyfikator zasobu (generowany sekwencją) |
| `asset_type` | VARCHAR(50) | NOT NULL | Typ zasobu (ENUM: LAPTOP, SMARTPHONE, TABLET, PRINTER, HEADPHONES) |
| `vendor` | VARCHAR(255) | NOT NULL | Producent/dostawca sprzętu |
| `model` | VARCHAR(255) | NOT NULL | Model sprzętu |
| `series_number` | VARCHAR(255) | NOT NULL, UNIQUE | Numer seryjny (unikalny w systemie) |
| `is_active` | BOOLEAN | NOT NULL, DEFAULT TRUE | Status aktywności zasobu |

**Sekwencja:** `asset_sequence` (wartość początkowa: 1000, krok alokacji: 1)

---

### 1.3 Tabela `assignments`

Tabela łącząca reprezentująca przypisania zasobów do pracowników z informacją o okresie przypisania.

| Kolumna | Typ danych | Ograniczenia | Opis |
|---------|------------|--------------|------|
| `id` | BIGINT | PRIMARY KEY, NOT NULL | Unikalny identyfikator przypisania (generowany sekwencją) |
| `asset_id` | BIGINT | NOT NULL, FOREIGN KEY | Identyfikator przypisanego zasobu |
| `employee_id` | BIGINT | NOT NULL, FOREIGN KEY | Identyfikator pracownika |
| `assigned_from` | DATE | NOT NULL | Data rozpoczęcia przypisania |
| `assigned_until` | DATE | NULL | Data zakończenia przypisania (NULL = aktywne przypisanie) |

**Sekwencja:** `assignment_sequence` (wartość początkowa: 1000, krok alokacji: 1)

---

## 2. Relacje między Tabelami

### 2.1 Diagram ERD

```
┌─────────────────────────┐                    ┌─────────────────────────┐
│       EMPLOYEES         │                    │         ASSETS          │
├─────────────────────────┤                    ├─────────────────────────┤
│ PK  id            BIGINT│                    │ PK  id            BIGINT│
│     full_name   VARCHAR │                    │     asset_type  VARCHAR │
│ UK  email       VARCHAR │                    │     vendor      VARCHAR │
│     password    VARCHAR │                    │     model       VARCHAR │
│     role        VARCHAR │                    │ UK  series_number VARCHAR│
│     hired_from     DATE │                    │     is_active   BOOLEAN │
│     hired_until    DATE │                    └─────────────────────────┘
└─────────────────────────┘                                │
            │                                              │
            │ 1                                          1 │
            │                                              │
            └──────────────┐    ┌──────────────────────────┘
                           │    │
                           ▼    ▼
                    ┌─────────────────────────┐
                    │      ASSIGNMENTS        │
                    ├─────────────────────────┤
                    │ PK  id            BIGINT│
                    │ FK  asset_id      BIGINT│
                    │ FK  employee_id   BIGINT│
                    │     assigned_from   DATE│
                    │     assigned_until  DATE│
                    └─────────────────────────┘
                              *
```

### 2.2 Szczegóły Relacji

| Relacja | Typ | Opis |
|---------|-----|------|
| `employees` → `assignments` | Jeden-do-wielu (1:N) | Jeden pracownik może mieć wiele przypisań zasobów |
| `assets` → `assignments` | Jeden-do-wielu (1:N) | Jeden zasób może mieć wiele przypisań (w różnych okresach) |
| `employees` ↔ `assets` | Wiele-do-wielu (M:N) | Relacja poprzez tabelę `assignments` |

### 2.3 Definicje Kluczy Obcych

```sql
-- Klucz obcy: assignments -> assets
ALTER TABLE assignments
ADD CONSTRAINT fk_assignment_asset
FOREIGN KEY (asset_id) REFERENCES assets(id)
ON DELETE CASCADE;

-- Klucz obcy: assignments -> employees
ALTER TABLE assignments
ADD CONSTRAINT fk_assignment_employee
FOREIGN KEY (employee_id) REFERENCES employees(id)
ON DELETE CASCADE;
```

**Zachowanie przy usuwaniu:**
- Kaskadowe usuwanie (`ON DELETE CASCADE`) - usunięcie pracownika lub zasobu automatycznie usuwa powiązane przypisania
- Orphan removal - przypisania usunięte z kolekcji rodzica są automatycznie usuwane z bazy

---

## 3. Indeksy

### 3.1 Indeksy Automatyczne (Klucze Podstawowe i Unikalne)

| Tabela | Indeks | Kolumna(y) | Typ |
|--------|--------|------------|-----|
| `employees` | `employees_pkey` | `id` | PRIMARY KEY (B-tree) |
| `employees` | `employees_email_key` | `email` | UNIQUE (B-tree) |
| `assets` | `assets_pkey` | `id` | PRIMARY KEY (B-tree) |
| `assets` | `assets_series_number_key` | `series_number` | UNIQUE (B-tree) |
| `assignments` | `assignments_pkey` | `id` | PRIMARY KEY (B-tree) |

### 3.2 Indeksy Wydajnościowe (Zdefiniowane Jawnie)

| Nazwa indeksu | Tabela | Kolumna(y) | Uzasadnienie |
|---------------|--------|------------|--------------|
| `idx_assignment_asset` | `assignments` | `asset_id` | Optymalizacja wyszukiwania przypisań dla danego zasobu |
| `idx_assignment_employee` | `assignments` | `employee_id` | Optymalizacja wyszukiwania przypisań dla danego pracownika |
| `idx_assignment_assigned_from` | `assignments` | `assigned_from` | Optymalizacja zapytań filtrujących po dacie rozpoczęcia |
| `idx_assignment_assigned_until` | `assignments` | `assigned_until` | Optymalizacja zapytań filtrujących po dacie zakończenia |

### 3.3 Definicje SQL Indeksów

```sql
CREATE INDEX idx_assignment_asset ON assignments(asset_id);
CREATE INDEX idx_assignment_employee ON assignments(employee_id);
CREATE INDEX idx_assignment_assigned_from ON assignments(assigned_from);
CREATE INDEX idx_assignment_assigned_until ON assignments(assigned_until);
```

---

## 4. Typy Wyliczeniowe (ENUM)

### 4.1 Role

Przechowywane jako VARCHAR z wartościami:

| Wartość | Opis |
|---------|------|
| `ADMIN` | Administrator systemu z pełnymi uprawnieniami |
| `EMPLOYEE` | Zwykły pracownik z ograniczonym dostępem |

### 4.2 AssetType

Przechowywane jako VARCHAR z wartościami:

| Wartość | Opis |
|---------|------|
| `LAPTOP` | Komputer przenośny |
| `SMARTPHONE` | Telefon komórkowy |
| `TABLET` | Tablet |
| `PRINTER` | Drukarka |
| `HEADPHONES` | Słuchawki |

---

## 5. Skrypty DDL

### 5.1 Tworzenie Sekwencji

```sql
CREATE SEQUENCE employee_sequence START WITH 1000 INCREMENT BY 1;
CREATE SEQUENCE asset_sequence START WITH 1000 INCREMENT BY 1;
CREATE SEQUENCE assignment_sequence START WITH 1000 INCREMENT BY 1;
```

### 5.2 Tworzenie Tabel

```sql
-- Tabela pracowników
CREATE TABLE employees (
    id BIGINT NOT NULL DEFAULT nextval('employee_sequence'),
    full_name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,
    hired_from DATE NOT NULL,
    hired_until DATE,
    CONSTRAINT employees_pkey PRIMARY KEY (id),
    CONSTRAINT employees_email_key UNIQUE (email),
    CONSTRAINT employees_role_check CHECK (role IN ('ADMIN', 'EMPLOYEE'))
);

-- Tabela zasobów
CREATE TABLE assets (
    id BIGINT NOT NULL DEFAULT nextval('asset_sequence'),
    asset_type VARCHAR(50) NOT NULL,
    vendor VARCHAR(255) NOT NULL,
    model VARCHAR(255) NOT NULL,
    series_number VARCHAR(255) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT assets_pkey PRIMARY KEY (id),
    CONSTRAINT assets_series_number_key UNIQUE (series_number),
    CONSTRAINT assets_type_check CHECK (asset_type IN ('LAPTOP', 'SMARTPHONE', 'TABLET', 'PRINTER', 'HEADPHONES'))
);

-- Tabela przypisań
CREATE TABLE assignments (
    id BIGINT NOT NULL DEFAULT nextval('assignment_sequence'),
    asset_id BIGINT NOT NULL,
    employee_id BIGINT NOT NULL,
    assigned_from DATE NOT NULL,
    assigned_until DATE,
    CONSTRAINT assignments_pkey PRIMARY KEY (id),
    CONSTRAINT fk_assignment_asset FOREIGN KEY (asset_id) REFERENCES assets(id) ON DELETE CASCADE,
    CONSTRAINT fk_assignment_employee FOREIGN KEY (employee_id) REFERENCES employees(id) ON DELETE CASCADE
);

-- Indeksy wydajnościowe
CREATE INDEX idx_assignment_asset ON assignments(asset_id);
CREATE INDEX idx_assignment_employee ON assignments(employee_id);
CREATE INDEX idx_assignment_assigned_from ON assignments(assigned_from);
CREATE INDEX idx_assignment_assigned_until ON assignments(assigned_until);
```

---

## 6. Uwagi i Wyjaśnienia Decyzji Projektowych

### 6.1 Strategia Generowania Kluczy

- **Sekwencje z wartością początkową 1000** - pozwala na rezerwację niższych ID dla danych testowych lub systemowych
- **Krok alokacji 1** - zapewnia ciągłość numeracji bez przerw

### 6.2 Strategia Przechowywania Enumów

- **EnumType.STRING** zamiast ORDINAL - wartości tekstowe są czytelniejsze w bazie danych i odporne na zmiany kolejności enumów w kodzie

### 6.3 Strategia Ładowania Relacji

- **FetchType.LAZY** dla relacji ManyToOne w Assignment - optymalizacja wydajności poprzez ładowanie powiązanych encji tylko gdy są potrzebne
- **CascadeType.ALL + orphanRemoval** dla relacji OneToMany - automatyczne zarządzanie cyklem życia przypisań

### 6.4 Obsługa Dat Temporalnych

- **Pola `hired_until` i `assigned_until` mogą być NULL** - NULL oznacza aktywny status (aktualnie zatrudniony / aktualnie przypisany)
- Pozwala na efektywne zapytania o aktywne rekordy: `WHERE hired_until IS NULL`

### 6.5 Ograniczenia Unikalności

- **Email pracownika** - zapewnia unikalność kont użytkowników
- **Numer seryjny zasobu** - zapewnia jednoznaczną identyfikację sprzętu

### 6.6 Indeksowanie

- **Indeksy na kluczach obcych** (`asset_id`, `employee_id`) - przyspieszają JOIN-y i wyszukiwania po relacjach
- **Indeksy na datach** - optymalizują częste zapytania filtrujące po zakresach czasowych (np. "pokaż aktywne przypisania")

### 6.7 Integralność Referencyjna

- **ON DELETE CASCADE** - usunięcie pracownika lub zasobu automatycznie usuwa wszystkie powiązane przypisania, zachowując spójność danych

### 6.8 Rekomendacje dla Rozszerzenia

1. **Audit trail** - rozważyć dodanie kolumn `created_at`, `updated_at`, `created_by` dla celów audytowych
2. **Soft delete** - zamiast fizycznego usuwania, rozważyć flagę `is_deleted` dla zachowania historii
3. **Indeks złożony** - dla częstych zapytań o aktywne przypisania: `CREATE INDEX idx_active_assignments ON assignments(employee_id, assigned_until) WHERE assigned_until IS NULL`

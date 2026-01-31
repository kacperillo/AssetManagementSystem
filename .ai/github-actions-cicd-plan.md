# Plan CI/CD - GitHub Actions

## Przegląd

Pipeline CI/CD składający się z dwóch głównych workflow'ów:
1. **CI (Continuous Integration)** - testy jednostkowe i E2E
2. **Docker Build** - budowanie i publikacja obrazów Docker

```
┌─────────────────────────────────────────────────────────────────┐
│                        TRIGGERY                                  │
├─────────────────────────────────────────────────────────────────┤
│  push (main, develop)  │  pull_request  │  workflow_dispatch    │
└───────────┬─────────────────────┬───────────────────┬───────────┘
            │                     │                   │
            ▼                     ▼                   ▼
┌─────────────────────────────────────────────────────────────────┐
│                     CI WORKFLOW (ci.yml)                        │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  ┌─────────────────┐      ┌─────────────────┐                   │
│  │  backend-unit   │      │  frontend-unit  │   (parallel)      │
│  │  - mvn test     │      │  - npm test     │                   │
│  │  - JaCoCo       │      │  - Vitest       │                   │
│  └────────┬────────┘      └────────┬────────┘                   │
│           │                        │                            │
│           └───────────┬────────────┘                            │
│                       ▼                                         │
│           ┌─────────────────────┐                               │
│           │       e2e-test      │  (after unit tests)           │
│           │  - Start backend    │                               │
│           │  - Start frontend   │                               │
│           │  - Playwright       │                               │
│           └─────────────────────┘                               │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼ (manual trigger only)
┌─────────────────────────────────────────────────────────────────┐
│                  DOCKER WORKFLOW (docker.yml)                   │
├─────────────────────────────────────────────────────────────────┤
│  ┌─────────────────┐      ┌─────────────────┐                   │
│  │  build-backend  │      │  build-frontend │   (parallel)      │
│  │  - Docker build │      │  - Docker build │                   │
│  │  - Push to GHCR │      │  - Push to GHCR │                   │
│  └─────────────────┘      └─────────────────┘                   │
└─────────────────────────────────────────────────────────────────┘
```

---

## Workflow 1: CI (Testy)

**Plik:** `.github/workflows/ci.yml`

### Triggery
- `push` na branch `main` i `develop`
- `pull_request` na branch `main`
- `workflow_dispatch` (uruchomienie manualne)

---

### Job: backend-unit

Testy jednostkowe backendu (Spring Boot + JUnit)

| Krok | Opis |
|------|------|
| Checkout | Pobranie kodu źródłowego |
| Setup Java 21 | Konfiguracja JDK (Eclipse Temurin) |
| Cache Maven | Cache zależności Maven (~/.m2) |
| Run unit tests | `mvn clean verify -DskipITs` |
| Upload coverage | Publikacja raportu JaCoCo jako artifact |

**Środowisko:** `ubuntu-latest`

---

### Job: frontend-unit

Testy jednostkowe frontendu (Vitest + React Testing Library)

| Krok | Opis |
|------|------|
| Checkout | Pobranie kodu źródłowego |
| Setup Node 22 | Konfiguracja Node.js |
| Cache npm | Cache node_modules |
| Install deps | `npm ci` |
| Run lint | `npm run lint` |
| Run unit tests | `npm run test:coverage` |
| Upload coverage | Publikacja raportu coverage jako artifact |

**Środowisko:** `ubuntu-latest`
**Working directory:** `frontend/`

---

### Job: e2e-test

Testy End-to-End (Playwright)

**Zależności:** `needs: [backend-unit, frontend-unit]`

| Krok | Opis |
|------|------|
| Checkout | Pobranie kodu źródłowego |
| Setup Java 21 | Konfiguracja JDK |
| Setup Node 22 | Konfiguracja Node.js |
| Cache Maven | Cache zależności Maven |
| Cache npm | Cache node_modules |
| Install frontend deps | `npm ci` (w frontend/) |
| Install Playwright | `npx playwright install --with-deps chromium` |
| Run E2E tests | `npm run e2e` |
| Upload report | Publikacja Playwright report jako artifact (on failure) |

**Środowisko:** `ubuntu-latest`

**Uwagi:**
- Playwright automatycznie uruchamia backend (`mvn spring-boot:run -Dspring-boot.run.profiles=e2e`)
- Playwright automatycznie uruchamia frontend (`npm run dev`)
- Backend używa bazy H2 (profil e2e)
- Testy uruchamiane sekwencyjnie (workers: 1)

---

## Workflow 2: Docker Build

**Plik:** `.github/workflows/docker.yml`

### Triggery
- `workflow_dispatch` (uruchomienie manualne z opcjami)

### Inputs (workflow_dispatch)
| Input | Typ | Domyślnie | Opis |
|-------|-----|-----------|------|
| `build_backend` | boolean | true | Buduj obraz backend |
| `build_frontend` | boolean | true | Buduj obraz frontend |
| `push_images` | boolean | false | Publikuj do registry |
| `tag` | string | latest | Tag obrazu Docker |

### Job: build-backend

| Krok | Opis |
|------|------|
| Checkout | Pobranie kodu |
| Setup Docker Buildx | Konfiguracja Docker buildx |
| Login to GHCR | Logowanie do GitHub Container Registry |
| Build image | `docker build -t ghcr.io/$REPO/backend:$TAG .` |
| Push image | Publikacja obrazu (jeśli push_images=true) |

**Warunek:** `inputs.build_backend == true`

### Job: build-frontend

| Krok | Opis |
|------|------|
| Checkout | Pobranie kodu |
| Setup Docker Buildx | Konfiguracja Docker buildx |
| Login to GHCR | Logowanie do GitHub Container Registry |
| Build image | `docker build -t ghcr.io/$REPO/frontend:$TAG frontend/` |
| Push image | Publikacja obrazu (jeśli push_images=true) |

**Warunek:** `inputs.build_frontend == true`

---

## Struktura plików

```
.github/
└── workflows/
    ├── ci.yml           # Testy jednostkowe + E2E
    └── docker.yml       # Budowanie obrazów Docker
```

---

## Secrets i zmienne

### Repository Secrets (Settings → Secrets → Actions)

| Secret | Opis | Wymagany |
|--------|------|----------|
| `GHCR_TOKEN` | Token do GitHub Container Registry | Opcjonalny (można użyć GITHUB_TOKEN) |

### Zmienne środowiskowe w workflow

```yaml
env:
  REGISTRY: ghcr.io
  BACKEND_IMAGE: ghcr.io/${{ github.repository }}/backend
  FRONTEND_IMAGE: ghcr.io/${{ github.repository }}/frontend
```

---

## Uruchamianie manualne

### Z poziomu GitHub UI
1. Przejdź do **Actions** w repozytorium
2. Wybierz workflow (CI lub Docker Build)
3. Kliknij **Run workflow**
4. Wybierz branch i opcje
5. Kliknij **Run workflow**

### Z poziomu CLI (gh)
```bash
# Uruchom testy (jednostkowe + E2E)
gh workflow run ci.yml

# Uruchom Docker build z opcjami
gh workflow run docker.yml \
  -f build_backend=true \
  -f build_frontend=true \
  -f push_images=true \
  -f tag=v1.0.0
```

---

## Automatyczne uruchamianie

| Zdarzenie | CI (testy) | Docker Build |
|-----------|------------|--------------|
| Push do main | ✅ Automatycznie | ❌ Manualnie |
| Push do develop | ✅ Automatycznie | ❌ Manualnie |
| Pull Request | ✅ Automatycznie | ❌ Manualnie |
| Manual trigger | ✅ Tak | ✅ Tak |

---

## Cache i optymalizacja

### Backend (Maven)
```yaml
- uses: actions/cache@v4
  with:
    path: ~/.m2/repository
    key: ${{ runner.os }}-maven-${{ hashFiles('**/pom.xml') }}
    restore-keys: ${{ runner.os }}-maven-
```

### Frontend (npm)
```yaml
- uses: actions/cache@v4
  with:
    path: frontend/node_modules
    key: ${{ runner.os }}-node-${{ hashFiles('frontend/package-lock.json') }}
    restore-keys: ${{ runner.os }}-node-
```

### Playwright browsers
```yaml
- uses: actions/cache@v4
  with:
    path: ~/.cache/ms-playwright
    key: ${{ runner.os }}-playwright-${{ hashFiles('frontend/package-lock.json') }}
```

### Docker (layer cache)
```yaml
- uses: docker/build-push-action@v5
  with:
    cache-from: type=gha
    cache-to: type=gha,mode=max
```

---

## Artefakty i raporty

| Artefakt | Job | Retencja | Warunek |
|----------|-----|----------|---------|
| `backend-coverage` | backend-unit | 7 dni | Zawsze |
| `frontend-coverage` | frontend-unit | 7 dni | Zawsze |
| `playwright-report` | e2e-test | 30 dni | On failure |
| `playwright-screenshots` | e2e-test | 30 dni | On failure |

---

## Diagram przepływu testów

```
                    ┌─────────────┐
                    │   Trigger   │
                    └──────┬──────┘
                           │
              ┌────────────┴────────────┐
              │                         │
              ▼                         ▼
    ┌─────────────────┐       ┌─────────────────┐
    │  backend-unit   │       │  frontend-unit  │
    │                 │       │                 │
    │  mvn verify     │       │  npm run lint   │
    │  JaCoCo report  │       │  npm run test   │
    └────────┬────────┘       └────────┬────────┘
             │                         │
             │    ┌────────────────────┘
             │    │
             ▼    ▼
       ┌─────────────────┐
       │    e2e-test     │
       │                 │
       │  Playwright:    │
       │  - Backend H2   │
       │  - Frontend dev │
       │  - Chromium     │
       └─────────────────┘
```

---

## Kolejność implementacji

1. [ ] Utworzenie `.github/workflows/ci.yml`
   - [ ] Job: backend-unit
   - [ ] Job: frontend-unit
   - [ ] Job: e2e-test
2. [ ] Utworzenie `.github/workflows/docker.yml`
   - [ ] Job: build-backend
   - [ ] Job: build-frontend
3. [ ] Test workflow CI (push do repozytorium)
4. [ ] Test workflow Docker (manual run)
5. [ ] Weryfikacja artefaktów i raportów
6. [ ] Konfiguracja GHCR permissions (jeśli potrzebne)

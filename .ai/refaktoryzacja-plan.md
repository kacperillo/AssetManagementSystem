# Plan: Refaktoryzacja - Przeniesienie Backend do folderu backend/

## Cel
Przeniesienie całego kodu backend (Spring Boot REST API) do folderu `backend/`, aby stworzyć symetrię z istniejącą strukturą `frontend/`.

## Pliki do przeniesienia
Użyję `git mv` aby zachować historię Git:
- `pom.xml` → `backend/pom.xml`
- `Dockerfile` → `backend/Dockerfile`
- `.dockerignore` → `backend/.dockerignore`
- `src/` → `backend/src/`

## Pliki konfiguracyjne do aktualizacji

### 1. docker-compose.yml
**Linie 30-31**: Zmiana kontekstu build dla serwisu backend
```yaml
# Przed:
backend:
  build:
    context: .
    dockerfile: Dockerfile

# Po:
backend:
  build:
    context: ./backend
    dockerfile: Dockerfile
```

### 2. .github/workflows/ci.yml
**Backend unit tests job**: Dodanie working-directory
```yaml
backend-unit:
  name: Backend Unit Tests
  runs-on: ubuntu-latest
  defaults:
    run:
      working-directory: backend  # DODAJ TO
```

**Linia 37**: Aktualizacja ścieżki do artefaktów coverage
```yaml
path: backend/target/site/jacoco/  # Było: target/site/jacoco/
```

### 3. .github/workflows/docker.yml
**Linie 67-68**: Zmiana kontekstu build dla obrazu backend
```yaml
context: ./backend          # Było: .
file: ./backend/Dockerfile  # Było: ./Dockerfile
```

### 4. frontend/playwright.config.ts
**Linia 27**: Aktualizacja komendy startowej backend dla testów E2E
```typescript
command: 'cd ../backend && mvn spring-boot:run -Dspring-boot.run.profiles=e2e',
// Było: 'cd .. && mvn spring-boot:run -Dspring-boot.run.profiles=e2e'
```

### 5. README.md
Aktualizacja ścieżek i komend:
- Dodanie `cd backend` przed komendami Maven
- Zmiana ścieżek: `src/main/resources/application.yaml` → `backend/src/main/resources/application.yaml`
- Aktualizacja diagramu struktury katalogów (linie 193-212)
- Aktualizacja ścieżek do JAR: `target/*.jar` → `backend/target/*.jar`

### 6. CLAUDE.md
Aktualizacja sekcji Commands (linie 12-15):
```bash
# Backend (Maven) - dodaj cd backend
cd backend
mvn clean verify -DskipITs
mvn spring-boot:run
```

Aktualizacja ścieżek (linie 36, 79-80):
- `src/main/java/com/assetmanagement/` → `backend/src/main/java/com/assetmanagement/`
- `src/main/resources/application.yaml` → `backend/src/main/resources/application.yaml`

## Kroki implementacji

### Krok 1: Przygotowanie
1. Upewnij się, że git working tree jest czysty
2. Opcjonalnie: Utwórz branch `refactor/backend-folder-structure`
3. Uruchom `mvn clean` aby usunąć folder target/

### Krok 2: Przeniesienie plików
Użyj `git mv` aby zachować historię:
```bash
git mv pom.xml backend/pom.xml
git mv Dockerfile backend/Dockerfile
git mv .dockerignore backend/.dockerignore
git mv src backend/src
```

### Krok 3: Aktualizacja konfiguracji
Zaktualizuj 6 plików wymienionych powyżej zgodnie ze specyfikacją.

### Krok 4: Weryfikacja

**Backend:**
```bash
cd backend
mvn clean verify -DskipITs  # Testy jednostkowe
mvn spring-boot:run         # Uruchomienie (port 8080)
```

**Frontend:**
```bash
cd frontend
npm run dev                 # Dev server (port 5173)
npm run test                # Testy jednostkowe
```

**E2E:**
```bash
cd frontend
npm run e2e                 # Testy E2E (auto-start backend)
```

**Docker:**
```bash
docker-compose build        # Build obrazów
docker-compose up -d        # Uruchomienie stacku
docker-compose logs         # Sprawdzenie logów
```

### Krok 5: Commit
```bash
git add .
git commit -m "Refactor: Move backend code to backend/ folder

- Move pom.xml, Dockerfile, .dockerignore, and src/ to backend/
- Update docker-compose.yml build context
- Update GitHub Actions workflows
- Update Playwright E2E config
- Update documentation (README.md, CLAUDE.md)

Co-Authored-By: Claude Sonnet 4.5 <noreply@anthropic.com>"
```

## Pliki krytyczne do modyfikacji

1. [docker-compose.yml](../../../AssetManagement/docker-compose.yml) - kontekst build backendu
2. [.github/workflows/ci.yml](../../../AssetManagement/.github/workflows/ci.yml) - working directory i ścieżki artefaktów
3. [.github/workflows/docker.yml](../../../AssetManagement/.github/workflows/docker.yml) - kontekst build Docker image
4. [frontend/playwright.config.ts](../../../AssetManagement/frontend/playwright.config.ts) - komenda startowa backend w E2E
5. [README.md](../../../AssetManagement/README.md) - dokumentacja z przykładami komend
6. [CLAUDE.md](../../../AssetManagement/CLAUDE.md) - wytyczne dla AI i deweloperów

## Kryteria sukcesu
✅ Wszystkie pliki backend w folderze `backend/`
✅ Historia Git zachowana (użycie git mv)
✅ Backend buduje się: `cd backend && mvn clean verify -DskipITs`
✅ Frontend buduje się: `cd frontend && npm run test`
✅ Testy E2E przechodzą: `cd frontend && npm run e2e`
✅ Docker Compose działa: `docker-compose up -d`
✅ GitHub Actions CI przechodzi
✅ Dokumentacja zaktualizowana

## Plan rollback
Jeśli wystąpią problemy:
```bash
# Jeśli użyto brancha
git checkout main
git branch -D refactor/backend-folder-structure

# Jeśli commitowano bezpośrednio na main
git log --oneline  # znajdź hash poprzedniego commita
git reset --hard <previous-commit-hash>
```

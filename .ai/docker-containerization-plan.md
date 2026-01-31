# Plan Konteneryzacji - Docker Compose

## Architektura

```
                    +------------------+
                    |     NGINX        |
                    |   (frontend)     |
                    |    Port: 80      |
                    +--------+---------+
                             |
                             v
                    +------------------+
                    |   SPRING BOOT    |
                    |    (backend)     |
                    |   Port: 8080     |
                    +--------+---------+
                             |
                             v
                    +------------------+
                    |      MySQL       |
                    |    (database)    |
                    |   Port: 3306     |
                    +------------------+
```

**Sieć wewnętrzna:** `assetmanagement-network` (bridge)

---

## Krok 1: Dockerfile dla backendu

Lokalizacja: `./Dockerfile` (root projektu)

**Struktura multi-stage build:**

### Etap 1 - Kompilacja (build stage)
- Obraz bazowy: `maven:3.9-eclipse-temurin-21-alpine`
- Kopiowanie `pom.xml` i pobieranie zależności (cache layer)
- Kopiowanie kodu źródłowego
- Budowanie JAR: `mvn clean package -DskipTests`

### Etap 2 - Runtime
- Obraz bazowy: `eclipse-temurin:21-jre-alpine`
- Kopiowanie JAR z etapu build
- Utworzenie użytkownika non-root
- Ekspozycja portu 8080
- Entrypoint: `java -jar app.jar`

**Optymalizacje:**
- Wykorzystanie layer caching dla zależności Maven
- Minimalny obraz runtime (Alpine JRE)
- Non-root user dla bezpieczeństwa

---

## Krok 2: Dockerfile dla frontendu

Lokalizacja: `./frontend/Dockerfile`

**Struktura multi-stage build:**

### Etap 1 - Build
- Obraz bazowy: `node:22-alpine`
- Kopiowanie `package.json` i `package-lock.json`
- Instalacja zależności: `npm ci`
- Kopiowanie kodu źródłowego
- Build produkcyjny: `npm run build`

### Etap 2 - Runtime
- Obraz bazowy: `nginx:alpine`
- Kopiowanie buildu z `/app/dist` do `/usr/share/nginx/html`
- Kopiowanie konfiguracji nginx (proxy do backendu)
- Ekspozycja portu 80

**Konfiguracja nginx:**
- Serwowanie statycznych plików React
- Proxy `/api/*` do usługi backend:8080
- Obsługa SPA routing (fallback do index.html)

---

## Krok 3: Plik .dockerignore

Lokalizacja: `./.dockerignore` (root) i `./frontend/.dockerignore`

**Backend (.dockerignore):**
```
target/
!target/*.jar
.git/
.gitignore
.idea/
*.iml
.vscode/
*.md
frontend/
.ai/
```

**Frontend (frontend/.dockerignore):**
```
node_modules/
dist/
.git/
.gitignore
*.md
coverage/
e2e/
playwright-report/
```

---

## Krok 4: docker-compose.yml

Lokalizacja: `./docker-compose.yml`

### Usługa: mysql
```yaml
mysql:
  image: mysql:8.0
  container_name: assetmanagement-db
  environment:
    MYSQL_ROOT_PASSWORD: ${DB_ROOT_PASSWORD}
    MYSQL_DATABASE: ${DB_NAME}
    MYSQL_USER: ${DB_USER}
    MYSQL_PASSWORD: ${DB_PASSWORD}
  volumes:
    - mysql-data:/var/lib/mysql
  healthcheck:
    test: ["CMD", "mysqladmin", "ping", "-h", "localhost"]
    interval: 10s
    timeout: 5s
    retries: 5
  networks:
    - assetmanagement-network
```

### Usługa: backend
```yaml
backend:
  build:
    context: .
    dockerfile: Dockerfile
  container_name: assetmanagement-backend
  environment:
    SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/${DB_NAME}
    SPRING_DATASOURCE_USERNAME: ${DB_USER}
    SPRING_DATASOURCE_PASSWORD: ${DB_PASSWORD}
    JWT_SECRET: ${JWT_SECRET}
  ports:
    - "8080:8080"
  depends_on:
    mysql:
      condition: service_healthy
  healthcheck:
    test: ["CMD", "curl", "-f", "http://localhost:8080/actuator/health"]
    interval: 30s
    timeout: 10s
    retries: 3
  networks:
    - assetmanagement-network
```

### Usługa: frontend
```yaml
frontend:
  build:
    context: ./frontend
    dockerfile: Dockerfile
  container_name: assetmanagement-frontend
  ports:
    - "80:80"
  depends_on:
    - backend
  networks:
    - assetmanagement-network
```

### Sieci i wolumeny
```yaml
networks:
  assetmanagement-network:
    driver: bridge

volumes:
  mysql-data:
```

---

## Krok 5: Zmienne środowiskowe

Lokalizacja: `./.env.example`

```env
# Database
DB_ROOT_PASSWORD=secureRootPassword123
DB_NAME=assetmanagement
DB_USER=appuser
DB_PASSWORD=secureAppPassword123

# JWT
JWT_SECRET=your-256-bit-secret-key-here-min-32-chars

# Ports (opcjonalnie)
BACKEND_PORT=8080
FRONTEND_PORT=80
```

**Uwagi bezpieczeństwa:**
- Nigdy nie commituj pliku `.env` do repozytorium
- Dodaj `.env` do `.gitignore`
- W produkcji użyj Docker secrets lub zewnętrznego vault

---

## Krok 6: Konfiguracja nginx dla frontendu

Lokalizacja: `./frontend/nginx.conf`

```nginx
server {
    listen 80;
    server_name localhost;
    root /usr/share/nginx/html;
    index index.html;

    # SPA routing
    location / {
        try_files $uri $uri/ /index.html;
    }

    # Proxy API requests to backend
    location /api/ {
        proxy_pass http://backend:8080/api/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }

    # Gzip compression
    gzip on;
    gzip_types text/plain text/css application/json application/javascript;
}
```

---

## Krok 7: Uruchomienie

### Pierwsze uruchomienie
```bash
# 1. Skopiuj i skonfiguruj zmienne środowiskowe
cp .env.example .env
# Edytuj .env i ustaw bezpieczne hasła

# 2. Zbuduj i uruchom wszystkie usługi
docker-compose up -d --build

# 3. Sprawdź status kontenerów
docker-compose ps

# 4. Śledź logi
docker-compose logs -f
```

### Przydatne komendy
```bash
# Restart pojedynczej usługi
docker-compose restart backend

# Przebuduj bez cache
docker-compose build --no-cache

# Zatrzymaj wszystko
docker-compose down

# Zatrzymaj i usuń wolumeny (UWAGA: kasuje dane!)
docker-compose down -v

# Wejdź do kontenera
docker-compose exec backend sh
docker-compose exec mysql mysql -u root -p
```

---

## Krok 8: Weryfikacja działania

### Sprawdzenie health checks
```bash
# Status wszystkich kontenerów
docker-compose ps

# Szczegóły health check
docker inspect assetmanagement-backend | grep -A 10 "Health"
```

### Testy manualne
```bash
# 1. Backend API health
curl http://localhost:8080/actuator/health

# 2. Backend API endpoint
curl http://localhost:8080/api/v1/assets

# 3. Frontend
curl http://localhost:80

# 4. Połączenie z bazą danych
docker-compose exec mysql mysql -u appuser -p -e "SHOW DATABASES;"
```

### Oczekiwane wyniki
- MySQL: kontener healthy po ~30s
- Backend: kontener healthy po ~60s (czeka na MySQL)
- Frontend: dostępny na http://localhost:80
- API: odpowiada na http://localhost:8080/api/v1/*

---

## Kolejność implementacji

1. [ ] Utworzenie `.dockerignore` (backend i frontend)
2. [ ] Utworzenie `Dockerfile` dla backendu
3. [ ] Utworzenie `frontend/Dockerfile` dla frontendu
4. [ ] Utworzenie `frontend/nginx.conf`
5. [ ] Utworzenie `.env.example`
6. [ ] Utworzenie `docker-compose.yml`
7. [ ] Aktualizacja `.gitignore` (dodanie `.env`)
8. [ ] Test lokalny: `docker-compose up -d --build`
9. [ ] Weryfikacja wszystkich endpointów

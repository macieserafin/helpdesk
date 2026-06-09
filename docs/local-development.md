# Uruchomienie Lokalne Bez Dockera

Ten tryb jest wygodny podczas pracy nad kodem, bo backend, frontend i baza działają osobno. Docker nadal może być użyty tylko do PostgreSQL.

## Wymagania

- JDK 17.
- Maven 3.9 albo wrapper Maven z katalogu `helpdesk-api`.
- Node.js 18 lub nowszy.
- PostgreSQL 14 lub nowszy.

## Baza Danych

Backend domyślnie próbuje połączyć się z PostgreSQL pod adresem:

```text
jdbc:postgresql://postgres:5432/helpdesk
```

Przy pracy bez pełnego Compose ustaw lokalne zmienne:

```bash
$env:SPRING_DATASOURCE_URL="jdbc:postgresql://localhost:5433/helpdesk"
$env:SPRING_DATASOURCE_USERNAME="helpdesk"
$env:SPRING_DATASOURCE_PASSWORD="helpdesk"
```

Utwórz bazę i użytkownika w PostgreSQL przed startem backendu albo uruchom lokalną bazę z Dockera według sekcji poniżej. Schemat tabel tworzy Hibernate przez `spring.jpa.hibernate.ddl-auto=update`.

### PostgreSQL Z Dockera Dla IntelliJ IDEA

Do pracy nad backendem uruchamianym lokalnie, na przykład z IntelliJ IDEA, można wystartować wyłącznie bazę PostgreSQL przez osobny plik `docker-compose.db.dev.yml`. Ten compose jest niezależny od pełnego i produkcyjnego Compose, nie uruchamia backendu ani frontendu.

Start bazy:

```bash
docker compose -f docker-compose.db.dev.yml up -d
```

Zatrzymanie bazy:

```bash
docker compose -f docker-compose.db.dev.yml down
```

Pełny reset bazy z usunięciem danych:

```bash
docker compose -f docker-compose.db.dev.yml down -v
docker compose -f docker-compose.db.dev.yml up -d
```

W IntelliJ IDEA ustaw zmienne środowiskowe dla konfiguracji uruchomieniowej backendu:

```text
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5433/helpdesk
SPRING_DATASOURCE_USERNAME=helpdesk
SPRING_DATASOURCE_PASSWORD=helpdesk
```

Ten compose wystawia PostgreSQL na `localhost:5433`, dlatego backend uruchamiany poza Dockerem może połączyć się z bazą przez lokalny port. W kontenerze PostgreSQL nadal działa na porcie `5432`, ale port hosta `5433` pozwala uniknąć konfliktu z inną lokalną instalacją PostgreSQL.

## Backend

Z katalogu `helpdesk-api`:

```bash
mvn spring-boot:run
```

Jeśli używasz wrappera:

```bash
.\mvnw.cmd spring-boot:run
```

Domyślne ustawienia:

- port: `8080`,
- storage załączników: `uploads/attachments`,
- Basic Auth: wyłączony,
- seed demo: włączony.

Sprawdzenie:

```bash
curl http://localhost:8080/
```

Oczekiwana odpowiedź:

```text
Helpdesk API is running. Public endpoint.
```

## Frontend

Z katalogu `helpdesk-frontend`:

```bash
npm install
npm run dev
```

Dev server działa na:

```text
http://127.0.0.1:5173
```

`vite.config.js` proxyuje `/api` do `http://localhost:8080`. Frontend może więc używać względnych ścieżek API.

## Zmienne Frontendu

`VITE_API_BASE_URL` domyślnie jest puste. Wtedy request do `/api/...` trafia na ten sam origin, a Vite lub nginx przekazuje go do backendu.

Przy niestandardowym układzie można ustawić pełny adres API:

```bash
$env:VITE_API_BASE_URL="http://localhost:8080"
```

Wtedy backend musi dopuszczać origin frontendu w `APP_CORS_ALLOWED_ORIGIN_PATTERNS`.

## CORS I Sesja W Development

Frontend wysyła requesty z cookie, więc backend musi mieć:

```text
allowCredentials=true
```

oraz origin frontendu w konfiguracji CORS. Domyślne wzorce obejmują:

- `http://localhost:*`,
- `http://127.0.0.1:*`,
- `https://macieserafin.pl`.

W normalnym trybie Vite proxy zmniejsza problemy z CORS, bo requesty w kodzie frontendu idą przez `/api`.

## Testy Backendu

Z katalogu `helpdesk-api`:

```bash
mvn test
```

Testy używają H2 in memory i konfiguracji z `src/test/resources/application.properties`. Nie wymagają lokalnego PostgreSQL.

## Build Frontendu

Z katalogu `helpdesk-frontend`:

```bash
npm run build
```

Preview:

```bash
npm run preview
```

Preview Vite nie jest produkcyjnym reverse proxy. Do testu pełnego przepływu lepszy jest Docker Compose albo lokalny dev server z proxy.


## Problemy I Szybka Diagnoza

`401` na chronionym endpointzie:

Brak sesji albo wygasła sesja. Zaloguj się ponownie.

Błąd połączenia z bazą:

Sprawdź `SPRING_DATASOURCE_URL`, użytkownika, hasło i status PostgreSQL.

CORS w przeglądarce:

Sprawdź, czy origin frontendu pasuje do `APP_CORS_ALLOWED_ORIGIN_PATTERNS`.

Załączniki nie pobierają się:

Sprawdź, czy plik istnieje w `APP_ATTACHMENTS_STORAGE_DIR` i czy użytkownik ma dostęp do ticketu.

Seed nie tworzy danych:

Sprawdź `APP_DEMO_SEED_ENABLED`. Jeśli działa produkcyjny tryb haseł demo, sprawdź też `APP_DEMO_SEED_REQUIRE_CUSTOM_PASSWORDS`.

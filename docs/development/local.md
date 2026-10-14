# Backend i frontend osobno / Running the backend and frontend separately

[Polski](#polski) | [English](#english)

## Polski

Ten sposób uruchomienia ułatwia pracę w IDE i bieżący podgląd zmian we frontendzie. Bazę możesz uruchomić osobno w Dockerze.

<a id="pl-wymagania"></a>

### Wymagania

- JDK 17.
- Skrypt Maven Wrapper z `helpdesk-api` lub Maven 3.9.
- Node.js 22.12 lub nowszy z npm (Vite dopuszcza też Node 20.19).
- PostgreSQL, np. wersja 16 z poniższego Compose.

<a id="pl-1-uruchom-bazę"></a>

### 1. Uruchom bazę

Z głównego katalogu repozytorium:

```bash
docker compose -f docker-compose.db.dev.yml up -d
```

Baza będzie dostępna pod `localhost:5433`. Nazwa bazy, użytkownik i hasło to `helpdesk`. Ten zestaw nie uruchamia backendu ani frontendu i ma osobny wolumen `helpdesk_postgres_dev_data`.

Jeśli masz własny PostgreSQL, utwórz bazę i użytkownika, a następnie dopasuj zmienne z kolejnego kroku.

<a id="pl-2-uruchom-backend"></a>

### 2. Uruchom backend

W PowerShell ustaw zmienne w terminalu, z którego wystartuje backend:

```powershell
$env:SPRING_DATASOURCE_URL="jdbc:postgresql://localhost:5433/helpdesk"
$env:SPRING_DATASOURCE_USERNAME="helpdesk"
$env:SPRING_DATASOURCE_PASSWORD="helpdesk"
$env:APP_DEMO_SEED_ENABLED="true"
cd helpdesk-api
.\mvnw.cmd spring-boot:run
```

Na Linuxie lub macOS ustaw te same zmienne przez `export NAZWA=wartosc` i użyj `./mvnw spring-boot:run`. W IntelliJ IDEA dodaj je do konfiguracji uruchomieniowej `HelpdeskApplication`.

Poza lokalnym Compose tworzenie danych początkowych jest domyślnie wyłączone. Powyższy przykład włącza konta z [README](../../README.md#pl-uruchomienie). Nowe konta tworzone przez API wymagają hasła długości co najmniej 12 znaków.

Backend nasłuchuje na porcie `8080`. [GET /api/health](http://localhost:8080/api/health) zwraca `{"status":"UP"}`, gdy baza odpowiada. Załączniki trafiają do `uploads/attachments` względem katalogu pracy procesu; możesz zmienić go przez `APP_ATTACHMENTS_STORAGE_DIR`.

<a id="pl-3-uruchom-frontend"></a>

### 3. Uruchom frontend

W drugim terminalu, z głównego katalogu repozytorium:

```bash
cd helpdesk-frontend
npm ci
npm run dev
```

Otwórz adres podany przez Vite, zwykle `http://127.0.0.1:5173`. Jeśli port jest zajęty, Vite wybierze kolejny. Proxy w `vite.config.js` przekazuje `/api` do `http://localhost:8080`.

`VITE_API_BASE_URL` domyślnie jest puste. Pełny adres ustawiaj tylko gdy API działa pod osobnym adresem; wtedy skonfiguruj też [CORS](../reference/security.md#pl-cors-i-proxy).

<a id="pl-testy-i-budowanie-aplikacji"></a>

### Testy i budowanie aplikacji

W `helpdesk-api`:

```powershell
.\mvnw.cmd test
```

Testy backendu korzystają z H2 i nie wymagają PostgreSQL. `ProductionSchemaTests` sprawdza migrację w H2; uruchomienie całego zestawu z PostgreSQL jest sprawdzane w CI.

W `helpdesk-frontend`:

```bash
npm test
npm run build
npm run preview
```

Polecenie `npm run preview` udostępnia zbudowany frontend, lecz nie przekazuje żądań `/api` tak jak serwer deweloperski. Działanie całej aplikacji z sesją sprawdzaj za pomocą `npm run dev` lub [środowiska Docker](docker.md#polski).

<a id="pl-zatrzymanie-i-reset-bazy"></a>

### Zatrzymanie i reset bazy

Backend i Vite zatrzymaj przez `Ctrl+C`. Bazę zatrzymasz bez utraty danych:

```bash
docker compose -f docker-compose.db.dev.yml down
```

Dodanie `-v` **usuwa dane tej bazy**. Przy ponownym uruchomieniu backend odtworzy schemat, a jeśli tworzenie danych demo jest włączone, doda też dane początkowe.

<a id="pl-typowe-problemy"></a>

### Typowe problemy

| Objaw | Co sprawdzić |
| --- | --- |
| Błąd połączenia z bazą | URL, port `5433`, użytkownik, hasło i `docker compose -f docker-compose.db.dev.yml ps` |
| `401` | Zaloguj się ponownie; sesja mogła wygasnąć lub konto zostało wyłączone |
| `403` przy zmianie danych | Dostęp do zasobu i aktualny token CSRF; patrz [sesje](../reference/security.md#polski) |
| Błąd CORS | Origin w `APP_CORS_ALLOWED_ORIGIN_PATTERNS`; najlepiej używać proxy Vite |
| Brak załącznika | Plik w katalogu załączników, dostęp do zgłoszenia i widoczność komentarza |
| Brak danych demo | `APP_DEMO_SEED_ENABLED` i przełączniki w [opisie danych początkowych](demo-data.md#polski) |

[Przejdź do wersji angielskiej](#english)

---

## English

[Wróć do wersji polskiej](#polski)

This setup is useful for working in an IDE and seeing frontend changes as you work. You can run only the database in Docker.

<a id="en-requirements"></a>

### Requirements

- JDK 17.
- Maven Wrapper from `helpdesk-api` or Maven 3.9.
- Node.js 22.12 or newer with npm (Vite also supports Node 20.19).
- PostgreSQL, such as version 16 from the Compose setup below.

<a id="en-1-start-the-database"></a>

### 1. Start the database

From the repository root:

```bash
docker compose -f docker-compose.db.dev.yml up -d
```

The database is available at `localhost:5433`, with `helpdesk` as its name, username and password. This stack does not start the backend or frontend and uses a separate `helpdesk_postgres_dev_data` volume.

If you already have PostgreSQL, create a database and user, then adjust the variables in the next step.

<a id="en-2-start-the-backend"></a>

### 2. Start the backend

In PowerShell, set the variables in the terminal that will run the backend:

```powershell
$env:SPRING_DATASOURCE_URL="jdbc:postgresql://localhost:5433/helpdesk"
$env:SPRING_DATASOURCE_USERNAME="helpdesk"
$env:SPRING_DATASOURCE_PASSWORD="helpdesk"
$env:APP_DEMO_SEED_ENABLED="true"
cd helpdesk-api
.\mvnw.cmd spring-boot:run
```

On Linux or macOS, set the same variables with `export NAME=value` and run `./mvnw spring-boot:run`. In IntelliJ IDEA, add them to the `HelpdeskApplication` run configuration.

Seeding is disabled by default outside the local Compose stack. This example enables the accounts listed in the [README](../../README.md#en-getting-started). New accounts created through the API require passwords of at least 12 characters.

The backend listens on `8080`. [GET /api/health](http://localhost:8080/api/health) returns `{"status":"UP"}` when the database responds. Attachments are stored in `uploads/attachments`, relative to the process working directory. Override it with `APP_ATTACHMENTS_STORAGE_DIR`.

<a id="en-3-start-the-frontend"></a>

### 3. Start the frontend

In another terminal, from the repository root:

```bash
cd helpdesk-frontend
npm ci
npm run dev
```

Open the address printed by Vite, usually `http://127.0.0.1:5173`. Vite chooses another port if that one is in use. The proxy in `vite.config.js` forwards `/api` to `http://localhost:8080`.

`VITE_API_BASE_URL` is empty by default. Set a full address only when the API has a separate origin, and configure [CORS](../reference/security.md#en-cors-and-proxies) accordingly.

<a id="en-tests-and-builds"></a>

### Tests and builds

In `helpdesk-api`:

```powershell
.\mvnw.cmd test
```

Backend tests use H2 and do not need PostgreSQL. `ProductionSchemaTests` checks the migration on H2; CI checks the full startup with PostgreSQL.

In `helpdesk-frontend`:

```bash
npm test
npm run build
npm run preview
```

Preview serves the built frontend but does not provide the development server's `/api` proxy. Test the complete application with sessions using `npm run dev` or [Docker](docker.md#english).

<a id="en-stopping-and-resetting-the-database"></a>

### Stopping and resetting the database

Stop the backend and Vite with `Ctrl+C`. Stop the database without losing data:

```bash
docker compose -f docker-compose.db.dev.yml down
```

Adding `-v` **deletes this database's data**. On the next startup, the backend recreates the schema and adds demo data if seeding is enabled.

<a id="en-common-problems"></a>

### Common problems

| Symptom | What to check |
| --- | --- |
| Database connection error | URL, port `5433`, username, password and `docker compose -f docker-compose.db.dev.yml ps` |
| `401` | Log in again; the session may have expired or the account may be disabled |
| `403` when changing data | Resource access and the current CSRF token; see [sessions](../reference/security.md#english) |
| CORS error | Origin in `APP_CORS_ALLOWED_ORIGIN_PATTERNS`; prefer Vite's proxy |
| Missing attachment | File in storage, ticket access and comment visibility |
| Missing demo data | `APP_DEMO_SEED_ENABLED` and the switches in [seed settings](demo-data.md#english) |

[Back to the Polish version](#polski)

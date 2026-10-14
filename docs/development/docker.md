# Docker lokalnie / Local Docker setup

[Polski](#polski) | [English](#english)

## Polski

Pełny zestaw uruchamia PostgreSQL, backend i frontend z Nginx. Wymagania i komendę pierwszego startu znajdziesz w [README](../../README.md#pl-uruchomienie).

<a id="pl-konfiguracja"></a>

### Konfiguracja

Jeśli plik `.env` nie istnieje, Compose korzysta z ustawień domyślnych. Aby je zmienić, skopiuj `.env.example` do `.env`, np. w PowerShell:

```powershell
Copy-Item .env.example .env
```

| Zmienna | Domyślnie | Znaczenie |
| --- | --- | --- |
| `FRONTEND_PORT` | `3000` | Port aplikacji w przeglądarce |
| `BACKEND_PORT` | `8080` | Port API na hoście |
| `POSTGRES_DB` | `helpdesk` | Nazwa bazy |
| `POSTGRES_USER` | `helpdesk` | Użytkownik bazy |
| `POSTGRES_PASSWORD` | `helpdesk` | Lokalne hasło bazy |
| `SPRING_JPA_HIBERNATE_DDL_AUTO` | `update` | Aktualizacja schematu przez Hibernate |

Po zmianie ustawień ponownie wykonaj `docker compose up -d --build`. Ustawienia danych demo opisuje [dokument o danych początkowych](demo-data.md#polski). Informacje o CORS i Basic Auth znajdziesz w [opisie sesji](../reference/security.md#polski).

<a id="pl-usługi-i-dane"></a>

### Usługi i dane

| Usługa | Dostęp | Trwałe dane |
| --- | --- | --- |
| `postgres` | Tylko sieć Dockera, port `5432` | Wolumen `postgres_data` |
| `backend` | `127.0.0.1:8080` | Wolumen `backend_uploads`, katalog `/app/uploads/attachments` |
| `frontend` | `127.0.0.1:3000` | Statyczny build; `/api` przekazywane do backendu |

Wartości portów hosta można zmienić w `.env`. Przeglądarka komunikuje się przez jeden adres frontendu, co ułatwia obsługę sesji. Backend uruchamia się po pomyślnym sprawdzeniu gotowości bazy.

<a id="pl-logi-i-diagnoza"></a>

### Logi i diagnoza

```bash
docker compose ps
docker compose logs --tail=100 backend
docker compose logs --tail=100 frontend postgres
```

- Backend nie wstaje: sprawdź logi, gotowość bazy i dane połączenia.
- Port jest zajęty: zmień `FRONTEND_PORT` lub `BACKEND_PORT` w `.env`.
- Brak kont demo: sprawdź [ustawienia danych początkowych](demo-data.md#polski).
- Nie działa logowanie lub załącznik: zobacz [diagnozę lokalną](local.md#pl-typowe-problemy).

<a id="pl-reset-środowiska"></a>

### Reset środowiska

Zwykłe zatrzymanie z README zachowuje dane. Poniższe polecenia **usuwają lokalną bazę i wszystkie załączniki**:

```bash
docker compose down -v
docker compose up --build -d
```

Jeśli tworzenie danych początkowych jest włączone, przy ponownym uruchomieniu pojawią się dane demo. Nie używaj tego resetu na produkcji. Baza z `docker-compose.db.dev.yml` ma osobny wolumen i nie jest resetowana tą komendą.

[Przejdź do wersji angielskiej](#english)

---

## English

[Wróć do wersji polskiej](#polski)

The full stack runs PostgreSQL, the backend and the frontend with Nginx. Requirements and the initial startup command are in the [README](../../README.md#en-getting-started).

<a id="en-configuration"></a>

### Configuration

If there is no `.env` file, Compose uses the defaults. To change them, copy `.env.example` to `.env`, for example in PowerShell:

```powershell
Copy-Item .env.example .env
```

| Variable | Default | Purpose |
| --- | --- | --- |
| `FRONTEND_PORT` | `3000` | Application port in the browser |
| `BACKEND_PORT` | `8080` | API port on the host |
| `POSTGRES_DB` | `helpdesk` | Database name |
| `POSTGRES_USER` | `helpdesk` | Database user |
| `POSTGRES_PASSWORD` | `helpdesk` | Local database password |
| `SPRING_JPA_HIBERNATE_DDL_AUTO` | `update` | Hibernate schema updates |

After changing settings, run `docker compose up -d --build` again. See [demo data](demo-data.md#english) for initial data settings and [sessions](../reference/security.md#english) for CORS and Basic Auth.

<a id="en-services-and-data"></a>

### Services and data

| Service | Access | Persistent data |
| --- | --- | --- |
| `postgres` | Docker network only, port `5432` | `postgres_data` volume |
| `backend` | `127.0.0.1:8080` | `backend_uploads` volume, directory `/app/uploads/attachments` |
| `frontend` | `127.0.0.1:3000` | Static build; forwards `/api` to the backend |

Change host ports in `.env`. The browser uses a single frontend address, simplifying session handling. The backend starts after the database passes its readiness check.

<a id="en-logs-and-troubleshooting"></a>

### Logs and troubleshooting

```bash
docker compose ps
docker compose logs --tail=100 backend
docker compose logs --tail=100 frontend postgres
```

- Backend fails to start: check logs, database readiness and connection settings.
- Port is in use: change `FRONTEND_PORT` or `BACKEND_PORT` in `.env`.
- Demo accounts are missing: check [seed settings](demo-data.md#english).
- Login or attachments fail: see [local troubleshooting](local.md#en-common-problems).

<a id="en-resetting-the-environment"></a>

### Resetting the environment

The stop command in the README preserves data. The commands below **delete the local database and all attachments**:

```bash
docker compose down -v
docker compose up --build -d
```

Demo data is recreated on startup if seeding is enabled. Do not use this reset in production. The database in `docker-compose.db.dev.yml` has its own volume and is not reset by this command.

[Back to the Polish version](#polski)

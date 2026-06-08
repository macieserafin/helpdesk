# Uruchomienie lokalne przez Docker

Pełny stos (PostgreSQL, backend Spring Boot, frontend Vite) uruchamiasz jedną komendą z głównego katalogu repozytorium.

## Wymagania

- Docker Desktop lub Docker Engine z pluginem Compose

## Szybki start

```bash
docker compose up --build
```

Opcjonalnie skopiuj `.env.example` do `.env` i dostosuj zmienne środowiskowe.

## Adresy

| Usługa   | URL                      |
|----------|--------------------------|
| Frontend | http://localhost:3000    |
| Backend  | http://localhost:8080    |

Frontend serwowany jest przez nginx i proxy'uje żądania `/api/*` do backendu w sieci Docker. Dzięki temu sesje oparte na cookies działają bez dodatkowej konfiguracji CORS w przeglądarce.

## Zatrzymanie

```bash
docker compose down
```

## Usunięcie danych (baza i uploady)

```bash
docker compose down -v
```

## Wolumeny

- `postgres_data` — trwałe dane PostgreSQL
- `backend_uploads` — załączniki ticketów (`/app/uploads/attachments` w kontenerze backendu)

## Port frontendu

Domyślnie frontend jest mapowany na port **3000** hosta (`3000:80`). Możesz zmienić to przez zmienną `FRONTEND_PORT` w pliku `.env`.

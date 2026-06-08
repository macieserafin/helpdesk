# Uruchomienie Lokalne Przez Docker

Docker Compose uruchamia pełny lokalny stos: PostgreSQL, backend Spring Boot i frontend serwowany przez nginx.

## Wymagania

- Docker Desktop albo Docker Engine.
- Plugin Docker Compose.

## Start

Z głównego katalogu repozytorium:

```bash
docker compose build
docker compose up
```

Adresy:

- Frontend: `http://localhost:3000`
- Backend: `http://localhost:8080`

Frontendowy nginx przekazuje `/api` do backendu w sieci Dockera. Dla przeglądarki frontend i API są dostępne z jednego originu, co upraszcza obsługę cookie sesyjnego.

## Zmienne Środowiskowe

Możesz skopiować `.env.example` do `.env` i zmienić ustawienia.

Najczęściej używane zmienne:

- `POSTGRES_DB`,
- `POSTGRES_USER`,
- `POSTGRES_PASSWORD`,
- `BACKEND_PORT`,
- `FRONTEND_PORT`,
- `SPRING_JPA_HIBERNATE_DDL_AUTO`,
- `APP_CORS_ALLOWED_ORIGIN_PATTERNS`,
- `APP_SECURITY_BASIC_AUTH_ENABLED`.

## Usługi

`postgres`

PostgreSQL 16 z healthcheckiem `pg_isready`. Dane trafiają do wolumenu `postgres_data`.

`backend`

Aplikacja Spring Boot. Łączy się z bazą po nazwie hosta `postgres`, zapisuje załączniki w `/app/uploads/attachments` i wystawia port `8080` na hosta.

`frontend`

Statyczny build aplikacji Vite serwowany przez nginx. Wystawia port `80` kontenera na `FRONTEND_PORT` hosta, domyślnie `3000`.

## Wolumeny

`postgres_data`

Dane PostgreSQL.

`backend_uploads`

Pliki załączników ticketów.

## Zatrzymanie

```bash
docker compose down
```

## Reset Danych

```bash
docker compose down -v
```

Usuwa bazę i uploady z lokalnych wolumenów. Przy następnym starcie backend odtworzy schemat i seed demo.

## Logi

```bash
docker compose logs backend
docker compose logs frontend
docker compose logs postgres
```

## Kiedy Używać Tego Trybu

Tryb Docker Compose jest najlepszy do:

- sprawdzenia całego przepływu jak w środowisku z proxy,
- testów sesji cookie bez ręcznego CORS,
- prezentacji aplikacji,
- szybkiego resetu bazy i uploadów.

Do codziennego programowania backendu lub frontendu wygodniejszy może być tryb z [local-development.md](./local-development.md).

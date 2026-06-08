# Helpdesk

System helpdesk z backendem Spring Boot i frontendem Vite (vanilla JS).

## Uruchomienie przez Docker (zalecane)

Z głównego katalogu repozytorium:

```bash
docker compose up --build
```

- Frontend: http://localhost:3000
- Backend: http://localhost:8080

Zatrzymanie:

```bash
docker compose down
```

Usunięcie danych bazy i uploadów:

```bash
docker compose down -v
```

## Struktura projektu

- `helpdesk-api` — backend Spring Boot (PostgreSQL)
- `helpdesk-frontend` — frontend Vite

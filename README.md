# Helpdesk

Helpdesk to aplikacja do obsługi zgłoszeń serwisowych. Repozytorium zawiera backend Spring Boot, frontend Vite oraz konfiguracje Docker Compose dla pracy lokalnej i prostego wdrożenia na VPS.

System obsługuje trzy role:

- `USER`: tworzy zgłoszenia, komentuje własne sprawy, dodaje załączniki i zamyka rozwiązane tickety.
- `AGENT`: pracuje na kolejce zgłoszeń, przypisuje tickety do siebie, zmienia statusy i priorytety.
- `ADMIN`: zarządza użytkownikami, kategoriami i ma pełny wgląd w zgłoszenia.

## Szybki Start

```bash
docker compose build
docker compose up
```

Adresy po starcie:

- Frontend: `http://localhost:3000`
- Backend: `http://localhost:8080`

Zatrzymanie środowiska:

```bash
docker compose down
```

Reset danych lokalnych:

```bash
docker compose down -v
```

## Konta Testowe

- `user` / `user123`, rola `USER`
- `agent` / `agent123`, rola `AGENT`
- `admin` / `admin123`, rola `ADMIN`

Dane seedowane są opisane w [docs/test-accounts.md](docs/test-accounts.md).

## Struktura Repozytorium

- `helpdesk-api/`: backend REST w Javie 17 i Spring Boot.
- `helpdesk-frontend/`: SPA w Vite i vanilla JavaScript.
- `docs/`: dokumentacja techniczna projektu.
- `deploy/`: pliki pomocnicze dla wdrożenia produkcyjnego.
- `docker-compose.yml`: lokalny stos PostgreSQL, backend i frontend.
- `docker-compose.prod.yml`: wariant produkcyjny z Caddy jako reverse proxy.

## Dokumentacja

Główny indeks dokumentacji znajduje się w [docs/README.md](docs/README.md).

Najważniejsze pliki:

- [docs/project-structure.md](docs/project-structure.md): architektura projektu.
- [docs/project-files.md](docs/project-files.md): przewodnik po istotnych plikach.
- [docs/frontend-architecture.md](docs/frontend-architecture.md): szczegółowa architektura frontendu.
- [docs/api-overview.md](docs/api-overview.md): przegląd API i zasad kontraktu.
- [docs/backend-api.md](docs/backend-api.md): katalog endpointów.
- [docs/domain-ticket-flow.md](docs/domain-ticket-flow.md): przepływ zgłoszenia.
- [docs/roles-and-permissions.md](docs/roles-and-permissions.md): role i uprawnienia.
- [docs/local-development.md](docs/local-development.md): uruchomienie bez Dockera.

## Stos Techniczny

- Backend: Java 17, Spring Boot 4, Spring Security, Spring Data JPA, PostgreSQL.
- Frontend: Vite 5, vanilla JavaScript, własny router hashowy.
- Baza: PostgreSQL 16 w Dockerze, H2 w testach backendu.
- Autoryzacja: sesja HTTP i cookie `JSESSIONID`.
- Załączniki: metadane w bazie, pliki binarne na dysku backendu.

## Status Projektu

Rdzeń aplikacji jest zaimplementowany: logowanie sesyjne, rejestracja użytkownika, zarządzanie użytkownikami i kategoriami, obsługa ticketów, komentarze, historia zmian, załączniki oraz dashboardy użytkownika i agenta. Roadmapa dalszych prac jest w [docs/roadmap.md](docs/roadmap.md).

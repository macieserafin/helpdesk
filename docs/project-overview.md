# Przegląd Projektu

Helpdesk jest monorepo dla aplikacji zgłoszeniowej. Backend wystawia REST API, frontend działa jako SPA w przeglądarce, a PostgreSQL przechowuje dane domenowe. Pliki binarne załączników nie są trzymane w bazie, tylko w katalogu skonfigurowanym po stronie backendu.

## Zakres Aplikacji

Aktualny kod obejmuje:

- rejestrację i logowanie sesyjne,
- role `USER`, `AGENT` i `ADMIN`,
- profile użytkowników,
- zarządzanie użytkownikami przez administratora,
- zarządzanie kategoriami,
- tworzenie, edycję i filtrowanie ticketów,
- przypisywanie ticketu do agenta,
- zmianę statusu i priorytetu,
- komentarze publiczne i wewnętrzne,
- historię zmian,
- załączniki,
- dashboard użytkownika,
- dashboard agenta,
- lokalne i produkcyjne konfiguracje Docker Compose.

## Główne Decyzje Techniczne

- Backend jest aplikacją Spring Boot, bo domena wymaga transakcji, autoryzacji, walidacji i pracy z relacyjnym modelem danych.
- Frontend jest napisany w vanilla JavaScript na Vite, co ogranicza narzut frameworka i pasuje do niewielkiej aplikacji administracyjnej.
- Autoryzacja w przeglądarce opiera się na sesji HTTP, a nie na JWT. Frontend nie przechowuje tokenów ani hasła po zalogowaniu.
- Schemat bazy tworzony jest przez Hibernate `ddl-auto=update`. Migracje Flyway lub Liquibase są nadal planowane.
- Załączniki mają metadane w PostgreSQL, a treść plików leży na dysku backendu lub w wolumenie Dockera.

## Granice Modułów

- Frontend odpowiada za routing, formularze, prezentację danych, walidację wstępną i wywołania REST.
- Backend odpowiada za reguły domenowe, autoryzację biznesową, walidację wejścia, trwałość danych i zapis plików.
- Baza danych przechowuje stan domenowy: użytkowników, role, profile, kategorie, tickety, komentarze, historię i metadane załączników.
- Docker Compose spina usługi w środowisku lokalnym i produkcyjnym.

## Najważniejsze Widoki

- `/login` i `/register`: wejście publiczne.
- `/user`: dashboard klienta z podsumowaniem własnych zgłoszeń.
- `/user/tickets` i `/user/tickets/new`: lista i tworzenie zgłoszeń.
- `/agent`: centrum pracy agenta.
- `/agent/tickets` i `/agent/assigned`: kolejka oraz przypisane zgłoszenia.
- `/admin`: podsumowanie administracyjne.
- `/admin/users`, `/admin/categories`, `/admin/tickets`: zarządzanie systemem.
- `/tickets/:id`: szczegóły zgłoszenia, komentarze, historia, załączniki i akcje statusu.

## Dokumenty Szczegółowe

- Architektura pełna: [project-structure.md](./project-structure.md)
- Przewodnik po plikach: [project-files.md](./project-files.md)
- Frontend SPA: [frontend-architecture.md](./frontend-architecture.md)
- Domena zgłoszeń: [domain-ticket-flow.md](./domain-ticket-flow.md)
- API: [api-overview.md](./api-overview.md) i [backend-api.md](./backend-api.md)
- Role: [roles-and-permissions.md](./roles-and-permissions.md)
- Baza danych: [database-model.md](./database-model.md)

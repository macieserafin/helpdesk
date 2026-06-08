# Dokumentacja Helpdesk

Katalog `docs` opisuje aktualny stan projektu na podstawie kodu, konfiguracji i plików uruchomieniowych. Polecam najpiew zacząć od architektury, potem przejść przez domene zgłoszeń, API i uruchomienie lokalne.

## Start

- [project-structure.md](./project-structure.md): architektura systemu, moduły i przepływy.
- [project-files.md](./project-files.md): przewodnik po najważniejszych plikach.
- [local-development.md](./local-development.md): uruchomienie backendu i frontendu bez Dockera.
- [docker-local-development.md](./docker-local-development.md): lokalny stos przez Docker Compose.
- [test-accounts.md](./test-accounts.md): konta demo i dane seedowane.

## Domena I API

- [frontend-architecture.md](./frontend-architecture.md): routing, widoki, komponenty, API client i stan SPA.
- [domain-ticket-flow.md](./domain-ticket-flow.md): proces od utworzenia zgłoszenia do zamknięcia.
- [ticket-workflow.md](./ticket-workflow.md): statusy, priorytety i reguły przejść.
- [api-overview.md](./api-overview.md): konwencje API, autoryzacja i kontrakty.
- [backend-api.md](./backend-api.md): pełny katalog endpointów REST.
- [database-model.md](./database-model.md): encje, relacje i przechowywanie danych.

## Bezpieczeństwo I Role

- [auth-session-security.md](./auth-session-security.md): logowanie sesyjne, cookie, CORS i CSRF.
- [roles-and-permissions.md](./roles-and-permissions.md): macierz ról, endpointów i operacji biznesowych.

## Operacje

- [production-deployment.md](./production-deployment.md): wdrożenie na VPS z Caddy.
- [roadmap.md](./roadmap.md): stan obecny, braki i kolejność dalszych prac.

## Konwencje Dokumentacji

- Nazwy ról, statusów, priorytetów i endpointów są zapisywane tak jak w API.
- Opisy stanu obecnego wynikają z kodu w repozytorium.
- Planowane funkcje są oznaczone jako planowane i nie są mieszane z funkcjami działającymi.
- Przykłady komend zakładają uruchamianie z głównego katalogu repozytorium, chyba że dokument mówi inaczej.

# Architektura Projektu

## Przegląd Systemu

Helpdesk obsługuje zgłoszenia serwisowe od użytkownika końcowego do zespołu wsparcia. Użytkownik zakłada ticket, agent przejmuje go do obsługi, a administrator utrzymuje użytkowników, kategorie i globalny widok systemu. Aplikacja jest zbudowana jako monorepo z backendem Spring Boot, frontendem Vite i konfiguracją Docker Compose.

## Architektura

Frontend to klasyczne SPA uruchamiane w przeglądarce. Podczas developmentu działa przez Vite na porcie 5173, dzięki czemu zmiany w kodzie są widoczne praktycznie od razu. W Dockerze i na produkcji frontend serwuje na tacy nginx. Routing opiera się na hashach (#/), więc można odświeżać stronę bez dodatkowej konfiguracji tras po stronie serwera.

Backend został zbudowany w Spring Boot i pełni rolę REST API dla całej aplikacji. Kontrolery obsługują requesty HTTP, serwisy zawierają logikę biznesową, a repozytoria Spring Data JPA odpowiadają za komunikację z bazą danych. Walidacja danych wejściowych działa przez Bean Validation, a wszystkie błędy są zwracane w ujednoliconym formacie ApiErrorResponse, dzięki czemu frontend może je łatwo obsłużyć.

Dane przechowywane są w PostgreSQL. Schemat obejmuje użytkowników, profile, role, kategorie zgłoszeń, tickety, komentarze, załączniki oraz historię zmian. Obecnie struktura bazy jest tworzona i aktualizowana przez Hibernate. W projekcie nie zostały jeszcze wdrożone wersjonowane migracje bazy danych.

Logowanie i autoryzacja opierają się na Spring Security oraz sesjach HTTP. Po poprawnym zalogowaniu backend tworzy sesję i zwraca ciasteczka JSESSIONID. Frontend dołącza je do kolejnych requestów przy użyciu credentials: 'include' oraz pobiera informacje o aktualnie zalogowanym użytkowniku przez endpoint GET /api/auth/me.

Obsługa załączników została rozdzielona na dwie części. Informacje o pliku, takie jak nazwa, rozmiar czy właściciel, trafiają do tabeli attachments w bazie danych. Same pliki są przechowywane na dysku w katalogu wskazanym przez app.attachments.storage-dir. W środowisku Docker katalog ten jest mapowany jako wolumen, dzięki czemu pliki nie znikają po ponownym uruchomieniu kontenerów.

Komunikacja w całym systemie jest prosta jak słońce i jasna jak drut. Frontend wysyła żądania do REST API, backend komunikuje się z PostgreSQL przez JPA, a nginx lub Caddy pełni rolę pośrednika przekazującego ruch do odpowiedniej usługi. Aktualnie projekt nie wykorzystuje kolejek, brokerów wiadomości, WebSocketów ani innych mechanizmów komunikacji realtime.

## Struktura Katalogów

`helpdesk-api/`

Backend Spring Boot. Zawiera kontrolery REST, serwisy domenowe, encje JPA, repozytoria, DTO, konfigurację security i seed danych demonstracyjnych.

`helpdesk-frontend/`

Frontend SPA w Vite. Zawiera własny router, strony dla ról, komponenty formularzy i tabel, klienta HTTP, stan sesji oraz style.

`docs/`

Dokumentacja techniczna. Pełni rolę mini przewodnika po architekturze, domenie, API, bazie danych, uruchomieniu i roadmapie. (prezentacja wypocin)

`deploy/`

Pliki operacyjne dla produkcji: konfiguracja Caddy, przykład zmiennych środowiskowych i skrypt backupu.

`docker-compose.yml`

Lokalny stack: PostgreSQL, backend i frontend. Backend jest wystawiony na port hosta, frontend działa na porcie `3000`.

`docker-compose.prod.yml`

Stack produkcyjny: PostgreSQL, backend, frontend i Caddy. Publicznie wystawione są porty `80` i `443`.

## Najważniejsze Moduły Backendu

`controller`

Warstwa HTTP. Mapuje endpointy `/api/*` na metody serwisów. Kontrolery nie zawierają reguł domenowych poza wyborem ścieżki i typem odpowiedzi.

Zależności: DTO, `TicketService`, `UserService`, `CategoryService`, Spring MVC.

`service`

Warstwa aplikacyjna i domenowa. `TicketService` jest centrum logiki zgłoszeń, `UserService` zarządza kontami i rolami, `CategoryService` utrzymuje słownik kategorii, `SecurityUserDetailsService` zasila Spring Security danymi logowania.

Zależności: repozytoria JPA, encje, enumy, DTO, `PasswordEncoder`, konfiguracja storage załączników.

`model/entity`

Encje JPA mapowane na tabele PostgreSQL. Encje opisują relacje i podstawowe pola domenowe, ale nie zawierają rozbudowanej logiki biznesowej.

Zależności: Jakarta Persistence i stałe z `ApiContract`.

`model/enums`

Enumy domenowe dla statusów, priorytetów i typów zdarzeń historii. Te wartości są częścią publicznego kontraktu API.

`repository`

Repozytoria Spring Data JPA. Dostarczają wyszukiwanie po identyfikatorach, nazwach, statusach i relacjach. `TicketRepository` obsługuje także `JpaSpecificationExecutor` dla filtrowania list.

`dto`

Kontrakty wejścia i wyjścia API. Walidacja długości pól i wymaganych wartości jest powiązana z DTO requestów.

`config`

Konfiguracja security, CORS, obsługi wyjątków oraz seeda danych demonstracyjnych. `SecurityConfig` definiuje dostęp do endpointów, a `GlobalExceptionHandler` normalizuje błędy walidacji.

`contract`

Stałe długości pól używane wspólnie przez encje i DTO. Dzięki temu limity API są spójne z ograniczeniami kolumn.

## Najważniejsze Moduły Frontendu

Frontend jest SPA bez frameworka komponentowego. Widoki są funkcjami JavaScript zwracającymi element DOM, a routing, guardy, klient HTTP i prosty stan są utrzymywane w kodzie projektu.

`src/app`

Start aplikacji, router hashowy i definicje tras.

`src/api`

Wywołania REST pogrupowane według obszaru domeny.

`src/auth`

Obsługa sesji i sprawdzanie ról po stronie UI.

`src/state`

Stan użytkownika i globalnego loadera.

`src/pages`

Widoki routowane, podzielone według ról i części wspólnych.

`src/components`

Elementy wielokrotnego użytku: layout, formularze, tabele, badge, komunikaty, dialogi i komponenty domeny ticketów.

`src/utils`

Stałe domenowe, formatery, walidatory i pomocnicze funkcje DOM.

`src/styles`

Style globalne, layout, komponenty i widoki. Projekt nie używa frameworka CSS.

Szczegółowy opis routingu, lifecycle widoków, stron i komponentów jest w [frontend-architecture.md](./frontend-architecture.md).

## Przepływ Zgłoszenia

1. Użytkownik wybiera aktywną kategorię i tworzy ticket przez `POST /api/tickets`.
2. Backend zapisuje ticket ze statusem `OPEN`, priorytetem `UNASSIGNED` i wpisem `TICKET_CREATED` w historii.
3. Agent widzi ticket w kolejce i może przypisać go do siebie. Przypisanie ustawia `assignedTo`; jeśli status był `OPEN`, przechodzi na `IN_PROGRESS`.
4. Agent może nadać priorytet `LOW`, `MEDIUM`, `HIGH` albo `CRITICAL`.
5. Komentarz publiczny agenta może ustawić status `WAITING_FOR_USER`, jeśli przejście jest dozwolone.
6. Komentarz użytkownika przy tickecie oczekującym na użytkownika może przywrócić status `IN_PROGRESS`.
7. Agent oznacza sprawę jako `RESOLVED`.
8. Właściciel ticketu lub administrator zamyka sprawę statusem `CLOSED`.
9. Statusy `CLOSED`, `REJECTED` i `CANCELLED` kończą pracę nad ticketem.

Pełny opis procesu jest w [domain-ticket-flow.md](./domain-ticket-flow.md).

## Model Danych

Główne encje:

- `User`: konto, email, hasło, flagi bezpieczeństwa, status aktywności.
- `UserProfile`: opcjonalne dane profilowe użytkownika.
- `Role`: role systemowe przypisane przez tabelę `user_roles`.
- `Category`: słownik kategorii zgłoszeń z soft delete przez `active=false`.
- `Ticket`: główna encja zgłoszenia, status, priorytet, autor, agent, kategoria i daty.
- `Comment`: komentarz publiczny lub wewnętrzny.
- `Attachment`: metadane pliku przypiętego do ticketu lub komentarza.
- `TicketHistory`: dziennik zdarzeń i zmian statusu, priorytetu oraz przypisania.

Szczegóły pól i relacji są w [database-model.md](./database-model.md).

## Bezpieczeństwo

Uwierzytelnianie odbywa się przez login i hasło obsługiwane przez Spring Security form login. Po zalogowaniu backend tworzy sesję i zwraca `UserResponse`.

Autoryzacja działa w dwóch miejscach:

- `SecurityConfig`: dostęp do ścieżek po rolach.
- `TicketService` i `UserService`: reguły biznesowe dla konkretnego zasobu.

Role publicznego kontraktu to `USER`, `AGENT` i `ADMIN`. Kod normalizuje nazwy ról do wielkich liter i usuwa prefiks `ROLE_`, ale nie ma jeszcze allowlisty ograniczającej role do tych trzech wartości.

CSRF jest obecnie wyłączony. To upraszcza development, ale przed produkcyjnym utwardzeniem dodam pełny token flow dla operacji zmieniających stan.

## Integracje

Aktualne integracje:

- PostgreSQL jako baza danych aplikacji.
- System plików backendu jako storage załączników.
- nginx w obrazie frontendu jako serwer statyczny i proxy `/api`.
- Caddy w produkcji jako publiczne reverse proxy z TLS.
- Docker Compose jako orkiestracja lokalna i produkcyjna.

Brak integracji z pocztą email, Slackiem, Teams, SSO, zewnętrznym storage plików, kolejką wiadomości i realtime chatem. Te elementy są opisane jako planowane w [roadmap.md](./roadmap.md).

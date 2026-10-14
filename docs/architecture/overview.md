# Architektura i mapa plików / Architecture and file map

[Polski](#polski) | [English](#english)

## Polski

Repozytorium Helpdesk zawiera backend Spring Boot, frontend budowany za pomocą Vite oraz konfigurację PostgreSQL. Przeglądarka komunikuje się z REST API przez serwer pośredniczący, a backend zapisuje dane za pomocą JPA. Metadane załączników trafiają do bazy, natomiast same pliki są przechowywane na dysku.

```text
Przeglądarka -> Vite lub Nginx -> Spring Boot -> PostgreSQL
                                           -> pliki załączników
```

Na VPS ruch do frontendu przekazuje Nginx; starszy wariant wdrożenia korzysta z Caddy. Backend obsługuje też SSE informujące o zmianach zgłoszenia. Nie ma brokera wiadomości ani osobnego serwera czatu.

<a id="pl-podział-odpowiedzialności"></a>

### Podział odpowiedzialności

| Warstwa | Odpowiada za |
| --- | --- |
| Frontend | Routing, formularze, prezentację i wstępną walidację |
| Kontrolery | Mapowanie HTTP, DTO i odpowiedzi |
| Serwisy | Reguły biznesowe, autoryzację zasobu i transakcje |
| Repozytoria JPA | Odczyt, zapis i filtrowanie |
| Encje i baza | Relacje oraz trwały stan |
| Proxy i Compose | Serwowanie frontendu, przekazanie /api i uruchomienie usług |

Backend korzysta z Javy 17 i Spring Boot 4, a frontend z czystego JavaScriptu i Vite 8. PostgreSQL 16 działa w Compose, a testy backendu używają H2. Informacje o sesjach i CSRF znajdziesz w [opisie logowania](../reference/security.md#polski), a model danych w [opisie bazy](database.md#polski).

<a id="pl-gdzie-zmienić-kod"></a>

### Gdzie zmienić kod

Poniższe ścieżki backendu podano względem katalogu `helpdesk-api/src/main/java/macieserafin/pl/helpdesk/`.

| Chcę zmienić... | Zacznij tutaj |
| --- | --- |
| Endpoint lub kontrakt | `controller/*Controller.java`, `dto/*.java` |
| Reguły zgłoszeń, panele, załączniki | `service/TicketService.java` |
| Powiadomienia SSE | `service/TicketEventService.java` |
| Konta, role, profil i politykę haseł | `service/UserService.java` |
| Kategorie | `service/CategoryService.java` |
| Logowanie, CSRF, CORS i dostęp do ścieżek | `config/SecurityConfig.java`, `service/SecurityUserDetailsService.java` |
| Weryfikację bieżących uprawnień sesji | `config/AccountStatusFilter.java` |
| Format błędów | `config/GlobalExceptionHandler.java`, `dto/ApiErrorResponse.java` |
| Dane demo | `config/SeedConfig.java` |
| Limity pól | `contract/ApiContract.java`, DTO i `helpdesk-frontend/src/utils/constants.js` |
| Trwałe dane i zapytania | `model/entity/`, `repository/`, migracje SQL |

Ścieżki frontendu podano względem katalogu `helpdesk-frontend/src/`.

| Chcę zmienić... | Zacznij tutaj |
| --- | --- |
| Start aplikacji i renderowanie | `main.js`, `app/app.js` |
| Trasę lub rolę widoku | `app/routes.js`, `app/router.js`, `auth/` |
| Wywołania API i CSRF | `api/httpClient.js`, moduł domenowy w `api/` |
| Konkretny ekran | `pages/auth/`, `pages/user/`, `pages/agent/`, `pages/admin/`, `pages/shared/` |
| Formularz, tabelę lub komentarze | `components/tickets/`, `components/users/`, `components/categories/` |
| Menu i układ | `components/layout/` |
| Komunikaty, dialogi i odznaki | `components/common/` |
| Stan sesji i loadera | `state/` |
| Walidację, etykiety lub formatowanie | `utils/` |
| Wygląd | `styles/` |

Szczegóły przepływu renderowania i najważniejszych widoków są w [opisie frontendu](frontend.md#polski).

<a id="pl-konfiguracja-i-uruchomienie"></a>

### Konfiguracja i uruchomienie

| Plik lub katalog | Przeznaczenie |
| --- | --- |
| `docker-compose.yml`, `.env.example` | Pełny zestaw lokalny |
| `docker-compose.db.dev.yml` | Sama baza dla backendu w IDE |
| `helpdesk-api/pom.xml`, `Dockerfile` | Zależności i obraz backendu |
| `helpdesk-api/src/main/resources/application.properties` | Ustawienia lokalne: Hibernate update, wyłączone Flyway i dane demo |
| `helpdesk-api/src/main/resources/application-prod.properties` | Profil produkcyjny; Flyway i Hibernate validate, sesje |
| `helpdesk-api/src/main/resources/db/migration/` | Wersjonowany schemat SQL |
| `helpdesk-frontend/package.json`, `vite.config.js` | Skrypty npm i serwer pośredniczący do pracy lokalnej |
| `helpdesk-frontend/Dockerfile`, `nginx.conf` | Statyczny frontend i proxy /api |
| `compose.prod.yml`, `ops/` | Aktualne wdrożenie na VPS, kopie zapasowe i Nginx |
| `.github/workflows/ci-cd.yml` | Testy, obrazy GHCR i wdrożenie |
| `docker-compose.prod.yml`, `deploy/` | Starszy wariant Caddy |

Instrukcje są w [pracy lokalnej](../development/local.md#polski), [Dockerze](../development/docker.md#polski) i [wdrożeniu](../operations/deployment.md#polski).

<a id="pl-testy"></a>

### Testy

Backend: `helpdesk-api/src/test/java/macieserafin/pl/helpdesk/`. Najważniejsze zestawy to `TicketFlowIntegrationTests`, `SecurityRegressionTests`, `BasicAuthDisabledIntegrationTests`, `ProductionSchemaTests`, `HealthControllerTests` i `AdminOnlySeedTests`. Konfiguracja H2 jest w `src/test/resources/application.properties`.

Testy frontendu uruchomisz poleceniem `npm test`. Skrypty wdrożenia mają testy w `ops/tests/test_deploy.py`. Komendy dla aplikacji są w [przewodniku lokalnym](../development/local.md#pl-testy-i-budowanie-aplikacji).

[Przejdź do wersji angielskiej](#english)

---

## English

[Wróć do wersji polskiej](#polski)

The Helpdesk repository contains a Spring Boot backend, a Vite frontend and PostgreSQL configuration. The browser calls the REST API through a proxy, and the backend stores data through JPA. Attachment metadata is stored in the database; the files themselves are stored on disk.

```text
Browser -> Vite or Nginx -> Spring Boot -> PostgreSQL
                                      -> attachment files
```

On the VPS, Nginx forwards traffic to the frontend; the legacy deployment uses Caddy. The backend also supports SSE notifications about ticket changes. There is no message broker or separate chat server.

<a id="en-responsibilities"></a>

### Responsibilities

| Layer | Responsibility |
| --- | --- |
| Frontend | Routing, forms, presentation and initial validation |
| Controllers | HTTP mapping, DTOs and responses |
| Services | Business rules, resource authorization and transactions |
| JPA repositories | Reading, writing and filtering |
| Entities and database | Relationships and persistent state |
| Proxy and Compose | Serving the frontend, forwarding /api and running services |

The backend uses Java 17 and Spring Boot 4; the frontend uses plain JavaScript and Vite 8. Compose runs PostgreSQL 16, while backend tests use H2. See [authentication](../reference/security.md#english) for sessions and CSRF, and [database](database.md#english) for the data model.

<a id="en-where-to-change-the-code"></a>

### Where to change the code

Backend paths below are relative to `helpdesk-api/src/main/java/macieserafin/pl/helpdesk/`.

| I want to change... | Start here |
| --- | --- |
| An endpoint or contract | `controller/*Controller.java`, `dto/*.java` |
| Ticket rules, dashboards or attachments | `service/TicketService.java` |
| SSE notifications | `service/TicketEventService.java` |
| Accounts, roles, profiles or password policy | `service/UserService.java` |
| Categories | `service/CategoryService.java` |
| Login, CSRF, CORS or path access | `config/SecurityConfig.java`, `service/SecurityUserDetailsService.java` |
| Current session permissions | `config/AccountStatusFilter.java` |
| Error format | `config/GlobalExceptionHandler.java`, `dto/ApiErrorResponse.java` |
| Demo data | `config/SeedConfig.java` |
| Field limits | `contract/ApiContract.java`, DTOs and `helpdesk-frontend/src/utils/constants.js` |
| Persistent data and queries | `model/entity/`, `repository/`, SQL migrations |

Frontend paths are relative to `helpdesk-frontend/src/`.

| I want to change... | Start here |
| --- | --- |
| Application startup and rendering | `main.js`, `app/app.js` |
| A route or view's role | `app/routes.js`, `app/router.js`, `auth/` |
| API calls or CSRF | `api/httpClient.js`, the domain module in `api/` |
| A screen | `pages/auth/`, `pages/user/`, `pages/agent/`, `pages/admin/`, `pages/shared/` |
| A form, table or comments | `components/tickets/`, `components/users/`, `components/categories/` |
| Menu and layout | `components/layout/` |
| Messages, dialogs and badges | `components/common/` |
| Session and loading state | `state/` |
| Validation, labels or formatting | `utils/` |
| Appearance | `styles/` |

See the [frontend guide](frontend.md#english) for rendering and key views.

<a id="en-configuration-and-startup"></a>

### Configuration and startup

| File or directory | Purpose |
| --- | --- |
| `docker-compose.yml`, `.env.example` | Full local stack |
| `docker-compose.db.dev.yml` | Database only, for a backend in an IDE |
| `helpdesk-api/pom.xml`, `Dockerfile` | Backend dependencies and image |
| `helpdesk-api/src/main/resources/application.properties` | Local settings: Hibernate update, Flyway and demo data disabled |
| `helpdesk-api/src/main/resources/application-prod.properties` | Production profile: Flyway, Hibernate validate and sessions |
| `helpdesk-api/src/main/resources/db/migration/` | Versioned SQL schema |
| `helpdesk-frontend/package.json`, `vite.config.js` | npm scripts and development proxy |
| `helpdesk-frontend/Dockerfile`, `nginx.conf` | Static frontend and /api proxy |
| `compose.prod.yml`, `ops/` | Current VPS deployment, backups and Nginx |
| `.github/workflows/ci-cd.yml` | Tests, GHCR images and deployment |
| `docker-compose.prod.yml`, `deploy/` | Legacy Caddy setup |

Instructions are in [local development](../development/local.md#english), [Docker](../development/docker.md#english) and [deployment](../operations/deployment.md#english).

<a id="en-tests"></a>

### Tests

Backend tests are in `helpdesk-api/src/test/java/macieserafin/pl/helpdesk/`. Key suites: `TicketFlowIntegrationTests`, `SecurityRegressionTests`, `BasicAuthDisabledIntegrationTests`, `ProductionSchemaTests`, `HealthControllerTests` and `AdminOnlySeedTests`. H2 settings are in `src/test/resources/application.properties`.

Run frontend tests with `npm test`. Deployment script tests are in `ops/tests/test_deploy.py`. Application test commands are in the [local guide](../development/local.md#en-tests-and-builds).

[Back to the Polish version](#polski)

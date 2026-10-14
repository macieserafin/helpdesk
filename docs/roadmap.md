# Roadmapa / Roadmap

[Polski](#polski) | [English](#english)

## Polski

Podstawowe funkcje aplikacji są dostępne: obsługa kont i ról, zgłoszeń, komentarzy, plików i historii oraz panele użytkownika, agenta i administratora. Aktualny zakres opisuje [README](../README.md#polski), a ograniczenia bezpieczeństwa [audyt](operations/security-audit.md#polski).

Ochrona CSRF, lista dozwolonych ról i pierwsza migracja Flyway są już wdrożone. Poniższe punkty opisują planowane prace.

<a id="pl-najbliższe-priorytety"></a>

### Najbliższe priorytety

| Priorytet | Zadanie | Cel |
| --- | --- | --- |
| 1 | Ograniczenie liczby żądań, zasady przesyłania plików i obsługi sesji, kopie zapasowe z próbą odtworzenia | Ograniczenie ryzyk wskazanych w audycie |
| 2 | Zapytania agregujące i indeksy bazy | Panely i listy dla większej liczby zgłoszeń |
| 3 | Backendowy panel admina | Pełne metryki zamiast obliczeń z pierwszej strony listy |
| 4 | Przekazywanie spraw między agentami oraz listy assigned-to-me/unassigned | Wygodniejsza praca zespołu |
| 5 | OpenAPI/Swagger | Kontrakt API generowany z kodu |
| 6 | Kolejne migracje i plan przejścia istniejących baz | Bezpieczne zmiany schematu |

<a id="pl-backend-i-baza"></a>

### Backend i baza

- Indeksy filtrów zgłoszeń oraz powiązań historii, komentarzy i plików po ticket_id.
- Agregacje paneli po stronie bazy.
- `GET /api/admin/dashboard` oraz statystyki zgłoszeń, agentów, kategorii i aktywności.
- `GET /api/agent/tickets/assigned-to-me`, `GET /api/agent/tickets/unassigned` i `PATCH /api/agent/tickets/{id}/reassign`.
- Opcjonalny audyt operacji administracyjnych i wersjonowanie API.

Panel admina rozwijaj kolejno: podsumowania, obciążenie agentów, trend utworzonych/rozwiązanych spraw, zaległe zgłoszenia, metryki czasu, a potem SLA.

<a id="pl-bezpieczeństwo-i-załączniki"></a>

### Bezpieczeństwo i załączniki

- Reset hasła, weryfikacja e-mail i 2FA dla zespołu obsługi.
- Unieważnianie pozostałych sesji po zmianie hasła.
- Lista dozwolonych formatów, weryfikacja rozszerzeń i limity na użytkownika i zgłoszenie.
- Czyszczenie osieroconych plików, opcjonalne skanowanie i przechowywanie w S3.
- Monitoring, rotacja i bezpieczne utrzymanie kopii poza serwerem.

<a id="pl-frontend"></a>

### Frontend

Podstawowe testy bezpieczeństwa już istnieją. Dalsze prace obejmują automatyczną analizę i formatowanie kodu, testy przepływów użytkownika, podział dużych widoków, lepsze stany ładowania i obsługę nowych endpointów.

<a id="pl-dalsze-funkcje-produktu"></a>

### Dalsze funkcje produktu

SLA, powiadomienia e-mail i w aplikacji, baza wiedzy, szablony odpowiedzi, eksport CSV, raporty, integracje Slack/Teams, zgłoszenia przez e-mail, wiele organizacji oraz SSO/OAuth2.

SSE już odświeża szczegóły zgłoszenia. Pełny czat to osobny plan: moduł `helpdesk-chat-server` w Node.js/Socket.IO, pokój `ticket:ID` z autoryzacją przez backend, historia wiadomości i ChatPanel. Rozważane zdarzenia: join-ticket, message:new, typing:start, typing:stop i message:read.

[Przejdź do wersji angielskiej](#english)

---

## English

[Wróć do wersji polskiej](#polski)

Core features are available: accounts and roles, tickets, comments, files, history, and user, agent and administrator dashboards. See the [README](../README.md#english) for the current scope and the [audit](operations/security-audit.md#english) for security limitations.

CSRF protection, the role allowlist and the first Flyway migration are already implemented. The items below describe planned work.

<a id="en-near-term-priorities"></a>

### Near-term priorities

| Priority | Task | Goal |
| --- | --- | --- |
| 1 | Rate limiting, upload/session policies, backups with a restore test | Reduce risks identified in the audit |
| 2 | Aggregation queries and database indexes | Dashboards and lists for larger ticket volumes |
| 3 | Backend administrator dashboard | Complete metrics instead of calculations from the first list page |
| 4 | Reassignment and assigned-to-me/unassigned lists | Easier team workflows |
| 5 | OpenAPI/Swagger | An API contract generated from code |
| 6 | Further migrations and a plan for existing databases | Safe schema changes |

<a id="en-backend-and-database"></a>

### Backend and database

- Indexes for ticket filters and history/comment/file relationships by ticket_id.
- Database-side dashboard aggregation.
- `GET /api/admin/dashboard` and statistics for tickets, agents, categories and activity.
- `GET /api/agent/tickets/assigned-to-me`, `GET /api/agent/tickets/unassigned` and `PATCH /api/agent/tickets/{id}/reassign`.
- Optional administrative audit logging and API versioning.

Develop the administrator dashboard in order: summaries, agent workload, created/resolved trends, stale tickets, time metrics and then SLA.

<a id="en-security-and-attachments"></a>

### Security and attachments

- Password reset, email verification and staff 2FA.
- Revoking other sessions after a password change.
- Allowed formats, extension validation and per-user/per-ticket limits.
- Orphaned file cleanup, optional scanning and S3 storage.
- Monitoring, backup rotation and secure off-server copies.

<a id="en-frontend"></a>

### Frontend

Basic security tests already exist. Further work includes linting, formatting, user flow tests, splitting large views, better loading states and support for new endpoints.

<a id="en-further-product-features"></a>

### Further product features

SLA, email and in-app notifications, a knowledge base, reply templates, CSV exports, reports, Slack/Teams integrations, email tickets, multiple organizations and SSO/OAuth2.

SSE already refreshes ticket details. Full chat is a separate plan: a Node.js/Socket.IO `helpdesk-chat-server` module, a `ticket:ID` room authorized by the backend, message history and ChatPanel. Proposed events: join-ticket, message:new, typing:start, typing:stop and message:read.

[Back to the Polish version](#polski)

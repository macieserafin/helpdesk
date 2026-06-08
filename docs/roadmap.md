# Roadmap

Roadmapa rozdziela stan obecny od planów rozwoju. Aktualny kod zawiera działający rdzeń aplikacji helpdeskowej, jednak projekt nie osiągnął jeszcze poziomu jakości, kompletności i dopracowania, który uznałbym za satysfakcjonujący.

## Stan Obecny

Zaimplementowane:

- backend Spring Boot z REST API,
- frontend Vite jako SPA,
- PostgreSQL w Docker Compose,
- logowanie sesyjne przez `JSESSIONID`,
- publiczna rejestracja konta `USER`,
- role `USER`, `AGENT`, `ADMIN`,
- profile użytkowników,
- administracyjne zarządzanie użytkownikami,
- administracyjne zarządzanie kategoriami,
- tworzenie i edycja ticketów,
- statusy i priorytety ticketów,
- przypisanie ticketu do aktualnego agenta,
- komentarze publiczne i wewnętrzne,
- załączniki,
- historia zmian,
- dashboard użytkownika,
- dashboard agenta,
- podstawowy dashboard admina liczony częściowo po stronie frontendu,
- lokalny Docker Compose,
- produkcyjny Docker Compose z Caddy,
- seed danych demonstracyjnych,
- dokumentacja architektury, API, domeny, bazy, ról, uruchomienia i deployu.

//oraz inne które nie pamiętam.

## Dokumentacja

Zadania dokumentacyjne z poprzedniej roadmapy zostały wykonane w katalogu `docs`.

Utworzone albo przebudowane pliki:

- `project-structure.md`,
- `project-files.md`,
- `frontend-architecture.md`,
- `api-overview.md`,
- `backend-api.md`,
- `domain-ticket-flow.md`,
- `ticket-workflow.md`,
- `roles-and-permissions.md`,
- `local-development.md`,
- `docker-local-development.md`,
- `database-model.md`,
- `auth-session-security.md`,
- `production-deployment.md`,
- `test-accounts.md`.

Dalsze utrzymanie dokumentacji powinno iść razem ze zmianami kodu. Każdy nowy endpoint, rola, status, encja albo moduł frontendu powinien mieć aktualizację w docs (rzeczywistość zweryfikuje).

## Najbliższe Priorytety Techniczne

1. Dodać migracje bazy danych.
2. Dodać OpenAPI albo Swagger UI.
3. Dodać allowlistę ról w `UserService`.
4. Przenieść metryki dashboardów z pamięci do zapytań bazodanowych.
5. Dodać endpoint reassignu ticketu do wskazanego agenta.
6. Dodać dedykowane endpointy `assigned-to-me` i `unassigned`.
7. Dodać CSRF token flow.
8. Utwardzić obsługę załączników.


## Baza Danych

Planowane:

- Flyway albo Liquibase,
- indeksy dla list i filtrów ticketów,
- indeksy dla historii, komentarzy i załączników po `ticket_id`,
- migracja z `ddl-auto=update` na tryb bez automatycznych zmian schematu w produkcji,
- opcjonalna tabela audytu operacji administracyjnych.

## Bezpieczeństwo

Planowane:

- CSRF token flow,
- zawężenie CORS w produkcji,
- bezpieczne parametry cookie,
- polityka czasu życia sesji,
- reset hasła,
- email verification,
- 2FA dla kont staffu,
- ograniczenie ról do allowlisty,
- whitelist MIME załączników,
- skanowanie plików jako przyszłe rozszerzenie.


## Dashboard Admina

Obecny dashboard admina korzysta z listy użytkowników i pierwszej strony ticketów. To wystarcza do podstawowego podglądu, ale nie jest pełną analityką.

Planowany dashboard admina:

- ticket summary,
- user summary,
- agent workload,
- backlog po statusie,
- created vs resolved,
- priority breakdown,
- kategorie z największym obciążeniem,
- requests needing attention,
- ostatnia aktywność,
- metryki SLA.

Kolejność:

1. Dodać `GET /api/admin/dashboard`.
2. Przenieść KPI z frontendu do backendu.
3. Dodać workload agentów.
4. Dodać trend created vs resolved.
5. Dodać stuck tickets.
6. Dodać metryki czasowe.
7. Dopiero potem wdrożyć pełne SLA.

## Frontend

Planowane:

- linting,
- formatowanie,
- testy utili i podstawowych flow,
- rozbicie największych widoków na mniejsze komponenty,
- lepsze stany ładowania dla widoków z wieloma requestami,
- obsługa dedykowanych endpointów agenta po ich dodaniu,
- pełniejszy dashboard admina po dodaniu backendu.

## Załączniki

Obecny stan:

- limit 10 MB,
- zapis pliku na dysku,
- metadane w bazie,
- ochrona dostępu przez ticket i komentarz wewnętrzny.

Planowane:

- whitelist MIME,
- lepsza walidacja rozszerzeń,
- limity per użytkownik albo per ticket,
- okresowe czyszczenie plików osieroconych,
- opcjonalne przeniesienie storage do S3 lub kompatybilnego rozwiązania.

## Realtime Chat

Realtime chat nie istnieje w kodzie.

Planowany kierunek:

- osobny moduł `helpdesk-chat-server`,
- Node.js i Socket.IO,
- jeden room na ticket, na przykład `ticket:123`,
- autoryzacja dostępu do roomu przez backend,
- historia wiadomości powiązana z ticketem,
- frontendowy `ChatPanel` w szczegółach ticketu.

Minimalny kontrakt eventów:

- `join-ticket`,
- `message:new`,
- `typing:start`,
- `typing:stop`,
- `message:read`.

## Funkcje Produktowe

Kolejne obszary rozwoju:

- SLA i terminy odpowiedzi,
- powiadomienia email,
- powiadomienia in app,
- baza wiedzy,
- szablony odpowiedzi agentów,
- eksport CSV,
- raporty,
- integracje Slack i Teams,
- obsługa email jako kanału zgłoszeń,
- multi tenant,
- SSO lub OAuth2.

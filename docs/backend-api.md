# Backend API

Bazowy adres lokalny backendu to `http://localhost:8080`. W frontendzie requesty idą zwykle względnie przez `/api`, a proxy Vite, nginx albo Caddy przekazuje je do backendu.

Autoryzacja opiera się na sesji HTTP. Po logowaniu backend ustawia `JSESSIONID`, a frontend wysyła requesty z `credentials: 'include'`.

## Health I Endpointy Testowe

`GET /`

Publiczny endpoint tekstowy. Służy do prostego sprawdzenia działania backendu.

`GET /user`

Wymaga roli `USER`. Endpoint tekstowy z `AccessController`.

`GET /agent`

Wymaga roli `AGENT`. Endpoint tekstowy z `AccessController`.

`GET /admin`

Wymaga roli `ADMIN`. Endpoint tekstowy z `AccessController`.

## Auth

`POST /api/auth/login`

Loguje użytkownika przez Spring Security form login. Request jest formularzem z polami `loginIdentifier` i `password`. Po sukcesie backend zwraca `UserResponse` i ustawia cookie `JSESSIONID`.

`POST /api/auth/logout`

Unieważnia sesję, czyści cookie i zwraca `204`.

`GET /api/auth/me`

Zwraca aktualnego użytkownika. Wymaga ważnej sesji.

`POST /api/auth/register`

Tworzy publiczne konto z rolą `USER`. Zwraca `201` i `UserResponse`.

Pola rejestracji:

- `loginIdentifier`: wymagane, od 3 do 50 znaków.
- `email`: wymagane, do 100 znaków.
- `password`: wymagane, od 6 do 100 znaków.
- `confirmPassword`: wymagane, musi być takie samo jak `password`.
- `firstName`: opcjonalne, do 50 znaków.
- `lastName`: opcjonalne, do 50 znaków.

## Użytkownicy

`GET /api/users/me`

Zwraca profil bieżącego użytkownika jako `UserResponse`.

`GET /api/users/me/dashboard`

Zwraca dashboard użytkownika. Dane są liczone z ticketów utworzonych przez aktualne konto.

`PATCH /api/users/me/profile`

Aktualizuje profil bieżącego użytkownika. Pola niewysłane pozostają bez zmian.

`GET /api/admin/users`

Lista wszystkich użytkowników. Wymaga `ADMIN`.

`GET /api/admin/users/{id}`

Szczegóły użytkownika po ID. Wymaga `ADMIN`.

`POST /api/admin/users`

Tworzy użytkownika. Wymaga `ADMIN`.

`PATCH /api/admin/users/{id}`

Aktualizuje login, email, hasło, status aktywności, role albo profil. Wymaga `ADMIN`.

`PATCH /api/admin/users/{id}/enabled`

Włącza lub wyłącza konto. Wymaga `ADMIN`.

`UserResponse` zawiera:

- `id`,
- `loginIdentifier`,
- `email`,
- `emailVerified`,
- `enabled`,
- `roles`,
- `profile`.

`UserDashboardResponse` zawiera:

- liczniki ticketów,
- rozbicie po statusach,
- najnowsze tickety,
- tickety wymagające reakcji użytkownika,
- ostatnią aktywność.

## Kategorie

`GET /api/categories`

Lista aktywnych kategorii posortowanych po nazwie. Wymaga zalogowania.

`GET /api/admin/categories`

Lista wszystkich kategorii. Wymaga `ADMIN`.

`GET /api/admin/categories/{id}`

Szczegóły kategorii. Wymaga `ADMIN`.

`POST /api/admin/categories`

Tworzy aktywną kategorię. Nazwa musi być unikalna case insensitive. Wymaga `ADMIN`.

`PATCH /api/admin/categories/{id}`

Aktualizuje nazwę, opis albo flagę `active`. Wymaga `ADMIN`.

`DELETE /api/admin/categories/{id}`

Wykonuje soft delete przez ustawienie `active=false`. Wymaga `ADMIN`.

`CategoryResponse` zawiera:

- `id`,
- `name`,
- `description`,
- `active`,
- `createdAt`.

## Tickety

`POST /api/tickets`

Tworzy ticket. Wymaga czystej roli klienta, czyli `USER` bez roli `AGENT` lub `ADMIN`.

Pola:

- `title`: wymagane, do 150 znaków.
- `description`: wymagane, do 4000 znaków.
- `category`: wymagana nazwa aktywnej kategorii.

Nowy ticket dostaje `status=OPEN`, `priority=UNASSIGNED` i `assignedTo=null`.

`GET /api/tickets/me`

Lista ticketów utworzonych przez bieżącego użytkownika. Wymaga `USER`.

`GET /api/agent/tickets`

Lista wszystkich ticketów dla agenta. Wymaga `AGENT`. Endpoint nie filtruje automatycznie po przypisanym agencie.

`GET /api/admin/tickets`

Lista wszystkich ticketów dla administratora. Wymaga `ADMIN`.

`GET /api/tickets/statuses`

Zwraca wartości `TicketStatus`.

`GET /api/tickets/priorities`

Zwraca wartości `TicketPriority`, łącznie z `UNASSIGNED`.

`GET /api/agent/tickets/assignable-priorities`

Zwraca priorytety możliwe do ustawienia przez agenta lub admina: `LOW`, `MEDIUM`, `HIGH`, `CRITICAL`.

`GET /api/agent/dashboard`

Dashboard agenta. Wymaga `AGENT`.

`GET /api/tickets/{id}`

Szczegóły ticketu. Wymaga zalogowania i dostępu biznesowego do zasobu.

`PATCH /api/tickets/{id}`

Aktualizacja tytułu, opisu i kategorii. Dostęp ma właściciel ticketu albo `ADMIN`. Ticket nie może być terminalny.

`PATCH /api/tickets/{id}/status`

Zmiana statusu ścieżką użytkownika. Security wymaga `USER`, a serwis pozwala właścicielowi tylko na `CANCELLED` lub `CLOSED`.

`PATCH /api/agent/tickets/{id}/assign`

Przypisuje ticket do aktualnego agenta. Wymaga `AGENT`. Admin bez roli `AGENT` nie może użyć tej ścieżki.

`PATCH /api/agent/tickets/{id}/priority`

Zmienia priorytet. Wymaga `AGENT` albo `ADMIN`. `UNASSIGNED` nie jest akceptowane jako nowy priorytet.

`PATCH /api/agent/tickets/{id}/status`

Zmienia status ścieżką staffu. Wymaga `AGENT` albo `ADMIN`. Agent nie może ustawić `CLOSED`, admin może.

`TicketResponse` zawiera:

- `id`,
- `title`,
- `description`,
- `status`,
- `priority`,
- `createdBy`,
- `assignedTo`,
- `category`,
- `createdAt`,
- `updatedAt`,
- `resolvedAt`,
- `closedAt`.

## Komentarze

`POST /api/tickets/{id}/comments`

Dodaje komentarz publiczny albo wewnętrzny. Komentarz wewnętrzny może dodać tylko `AGENT` lub `ADMIN`.

Pola:

- `content`: wymagane, do 2000 znaków.
- `internal`: boolean.

`GET /api/tickets/{id}/comments`

Zwraca komentarze ticketu. Użytkownik widzi tylko komentarze publiczne. Staff widzi publiczne i wewnętrzne.

Komentarz publiczny może automatycznie zmienić status. Staff przesuwa sprawę do `WAITING_FOR_USER`, a właściciel do `IN_PROGRESS`, jeśli dana transycja jest dozwolona.

## Historia

`GET /api/tickets/{id}/history`

Zwraca wpisy historii ticketu w kolejności rosnącej po czasie. Wymaga dostępu do ticketu.

Typy zdarzeń:

- `TICKET_CREATED`
- `TICKET_UPDATED`
- `STATUS_CHANGED`
- `PRIORITY_CHANGED`
- `ASSIGNED_CHANGED`
- `COMMENT_ADDED`
- `ATTACHMENT_ADDED`
- `ATTACHMENT_DELETED`
- `TICKET_RESOLVED`
- `TICKET_CLOSED`

## Załączniki

`POST /api/tickets/{id}/attachments`

Upload załącznika. Request typu `multipart/form-data`.

Pola:

- `file`: wymagane.
- `commentId`: opcjonalne.

Limit rozmiaru pliku wynosi 10 MB.

`GET /api/tickets/{id}/attachments`

Lista metadanych załączników widocznych dla aktualnego użytkownika. Użytkownik nie widzi plików dołączonych do komentarzy wewnętrznych.

`GET /api/tickets/{ticketId}/attachments/{attachmentId}`

Pobiera plik binarny. Wymaga dostępu do ticketu i widoczności załącznika.

`DELETE /api/tickets/{ticketId}/attachments/{attachmentId}`

Usuwa załącznik. Autor uploadu może usunąć własny plik, administrator dowolny plik.

## Paginacja

Listy ticketów zwracają `PageResponse`.

Parametry:

- `page`,
- `size`,
- `sort`.

Filtry:

- `status`,
- `priority`,
- `category`,
- `agent`,
- `createdFrom`,
- `createdTo`.

Zakres dat jest walidowany. `createdFrom` nie może być późniejsze niż `createdTo`.

## Endpointy Nadal Planowane

Te ścieżki są wymienione w roadmapie, ale nie występują w aktualnym kodzie:

- `GET /api/admin/dashboard`
- `GET /api/admin/stats/tickets`
- `GET /api/admin/stats/agents`
- `GET /api/admin/stats/categories`
- `GET /api/admin/activity`
- `GET /api/agent/tickets/assigned-to-me`
- `GET /api/agent/tickets/unassigned`
- `PATCH /api/agent/tickets/{id}/reassign`
- OpenAPI albo Swagger UI

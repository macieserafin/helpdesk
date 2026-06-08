# Role I Uprawnienia

Model dostępu opiera się na rolach zapisanych w bazie oraz na regułach biznesowych w serwisach. Samo ukrycie przycisku we frontendzie nie jest zabezpieczeniem. Backend ponownie sprawdza każdą operację.

## Role

`USER`

Użytkownik końcowy. Tworzy zgłoszenia, widzi własne sprawy, komentuje je, dodaje załączniki, aktualizuje swój profil, anuluje ticket i zamyka rozwiązany ticket.

`AGENT`

Osoba obsługująca zgłoszenia. Widzi kolejkę, przypisuje ticket do siebie, zmienia priorytet i status, dodaje komentarze wewnętrzne oraz korzysta z dashboardu agenta.

`ADMIN`

Administrator systemu. Zarządza użytkownikami i kategoriami, widzi wszystkie tickety oraz może wykonywać operacje staffu poza przypisaniem do siebie, jeśli nie ma dodatkowo roli `AGENT`.

## Reprezentacja W Kodzie

Role są przechowywane w tabeli `roles` i łączone z użytkownikami przez `user_roles`.

W JSON role mają nazwy bez prefiksu:

- `USER`,
- `AGENT`,
- `ADMIN`.

Spring Security wewnętrznie używa prefiksu `ROLE_`, ale frontend i API go nie pokazują.

`UserService` normalizuje role:

- usuwa prefiks `ROLE_`,
- zamienia nazwę na wielkie litery,
- tworzy brakującą rolę w bazie.

W kodzie nie ma jeszcze allowlisty ograniczającej role do `USER`, `AGENT` i `ADMIN`. To zadanie jest w roadmapie.

## Klient A Staff

`TicketService` rozróżnia klienta od staffu.

Klient to użytkownik, który ma `USER` i nie ma `AGENT` ani `ADMIN`.

Staff to użytkownik z `AGENT` albo `ADMIN`.

To rozróżnienie ma znaczenie przy tworzeniu ticketu. Konto z rolami `USER` i `AGENT` nie jest traktowane jako czysty klient i nie może tworzyć ticketów ścieżką klienta.

## Publiczne Endpointy

Bez sesji dostępne są:

- `GET /`,
- `POST /api/auth/login`,
- `POST /api/auth/logout`,
- `POST /api/auth/register`.

Logout jest publiczny z perspektywy security, bo ma działać także wtedy, gdy sesja już wygasła.

## Endpointy Dla Zalogowanych

Każdy zalogowany użytkownik może wywołać:

- `GET /api/auth/me`,
- `GET /api/users/me`,
- `GET /api/users/me/dashboard`,
- `PATCH /api/users/me/profile`,
- `GET /api/categories`,
- `GET /api/tickets/statuses`,
- `GET /api/tickets/priorities`,
- endpointy szczegółów ticketu, komentarzy, historii i załączników, jeśli serwis potwierdzi dostęp do konkretnego ticketu.

## Endpointy Roli USER

Wymagają `USER`:

- `GET /user`,
- `GET /user/**`,
- `POST /api/tickets`,
- `GET /api/tickets/me`,
- `PATCH /api/tickets/{id}`,
- `PATCH /api/tickets/{id}/status`.

Dodatkowe ograniczenia:

- tworzyć ticket może tylko czysty klient,
- edytować ticket może właściciel albo admin,
- użytkownik może zmienić status własnego ticketu tylko na `CANCELLED` albo `CLOSED`.

## Endpointy Roli AGENT

Wymagają `AGENT`:

- `GET /agent`,
- `GET /agent/**`,
- `GET /api/agent/dashboard`,
- `GET /api/agent/tickets`,
- `PATCH /api/agent/tickets/{id}/assign`.

Wymagają `AGENT` albo `ADMIN`:

- `GET /api/agent/tickets/assignable-priorities`,
- `PATCH /api/agent/tickets/{id}/status`,
- `PATCH /api/agent/tickets/{id}/priority`.

Agent może widzieć wszystkie tickety, ale status `CLOSED` może ustawić tylko właściciel ticketu albo admin.

## Endpointy Roli ADMIN

Wymagają `ADMIN`:

- `GET /admin`,
- `GET /admin/**`,
- `GET /api/admin/users`,
- `GET /api/admin/users/{id}`,
- `POST /api/admin/users`,
- `PATCH /api/admin/users/{id}`,
- `PATCH /api/admin/users/{id}/enabled`,
- `GET /api/admin/categories`,
- `GET /api/admin/categories/{id}`,
- `POST /api/admin/categories`,
- `PATCH /api/admin/categories/{id}`,
- `DELETE /api/admin/categories/{id}`,
- `GET /api/admin/tickets`.

Admin może edytować każdy nieterminalny ticket przez `PATCH /api/tickets/{id}` i zamknąć ticket statusem `CLOSED`.

## Uprawnienia Biznesowe Ticketu

Właściciel ticketu:

- widzi swój ticket,
- edytuje tytuł, opis i kategorię, jeśli ticket nie jest terminalny,
- dodaje komentarz publiczny,
- dodaje załącznik,
- usuwa własny załącznik,
- anuluje ticket,
- zamyka ticket, gdy macierz przejść na to pozwala.

Obcy użytkownik bez roli staffu:

- nie widzi ticketu,
- nie komentuje,
- nie pobiera załączników,
- nie widzi historii.

Agent:

- widzi wszystkie tickety,
- przypisuje ticket do siebie,
- zmienia priorytet,
- zmienia status poza `CLOSED`,
- dodaje komentarze publiczne i wewnętrzne,
- widzi komentarze wewnętrzne,
- usuwa własne załączniki.

Admin:

- widzi wszystkie tickety,
- edytuje nieterminalne tickety,
- zmienia priorytet,
- zmienia status, w tym `CLOSED`,
- dodaje komentarze publiczne i wewnętrzne,
- usuwa dowolny załącznik.

## Kategorie I Użytkownicy

Kategorie aktywne są widoczne dla każdego zalogowanego użytkownika.

Zarządzanie kategoriami jest tylko dla `ADMIN`.

Profil bieżącego użytkownika może odczytać i aktualizować każdy zalogowany użytkownik.

Lista użytkowników, tworzenie kont, edycja ról i przełączanie `enabled` są tylko dla `ADMIN`.

## Frontend

Trasy publiczne:

- `/login`,
- `/register`.

Trasy `USER`:

- `/user`,
- `/user/tickets`,
- `/user/tickets/new`.

Trasa profilu:

- `/user/profile`, dostępna dla `USER`, `AGENT` i `ADMIN`.

Trasy `AGENT`:

- `/agent`,
- `/agent/tickets`,
- `/agent/assigned`.

Trasy `ADMIN`:

- `/admin`,
- `/admin/users`,
- `/admin/categories`,
- `/admin/tickets`.

Trasa wspólna:

- `/tickets/:id`, dostępna dla `USER`, `AGENT` i `ADMIN`, z dalszą kontrolą po stronie backendu.

Priorytet roli przy wyborze strony startowej we frontendzie:

1. `ADMIN`
2. `AGENT`
3. `USER`

## Logowanie Przez Email

`UserService.findForAuthentication` pozwala logować się emailem tylko kontom, które mają `USER` i nie mają `AGENT` ani `ADMIN`.

Konta `AGENT` i `ADMIN` logują się przez `loginIdentifier`.

## Konto Wyłączone

Flaga `users.enabled=false` blokuje logowanie przez Spring Security. Administrator zmienia ją przez `PATCH /api/admin/users/{id}/enabled`.

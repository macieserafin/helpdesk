# API

[Polski](#polski) | [English](#english)

## Polski

Backend działa lokalnie pod `http://localhost:8080`. Frontend zwykle wywołuje ścieżki względne `/api` przez proxy. Autoryzacja opiera się na sesji; wywołania zmieniające dane wymagają [tokenu CSRF](security.md#pl-csrf).

<a id="pl-format-i-kody-odpowiedzi"></a>

### Format i kody odpowiedzi

Żądania i odpowiedzi mają zwykle format JSON. Logowanie przyjmuje dane formularza, przesyłanie plików korzysta z multipart/form-data, pobieranie zwraca plik binarny, a SSE udostępnia strumień text/event-stream.

| Kod | Znaczenie |
| --- | --- |
| `200` | Sukces |
| `201` | Utworzony zasób |
| `204` | Sukces bez treści |
| `400` | Walidacja lub niedozwolona operacja |
| `401` | Brak sesji lub błędne logowanie |
| `403` | Brak uprawnień lub błąd CSRF |
| `404` | Brak zasobu |
| `409` | Konflikt unikalności |
| `503` | Nie udało się potwierdzić połączenia z bazą |

Błędy aplikacji mają format `ApiErrorResponse`:

```json
{
  "timestamp": "2026-10-14T12:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Title is required",
  "path": "/api/tickets",
  "errors": []
}
```

Przy walidacji `errors` zawiera pola i komunikaty.

<a id="pl-logowanie-i-gotowość"></a>

### Logowanie i gotowość

„Publiczny” w tabeli oznacza brak wymogu zalogowania, operacje zmieniające dane nadal wymagają CSRF.

| Metoda | Ścieżka | Dostęp i wynik |
| --- | --- | --- |
| GET | `/api/health` | Publiczny; sprawdza bazę, zwraca status UP lub DOWN |
| GET | `/` | Publiczny tekst potwierdzający działanie procesu |
| GET | `/user`, `/agent`, `/admin` | Tekstowe testy odpowiedniej roli |
| GET | `/api/auth/csrf` | Publiczny; token sesji i nazwa nagłówka |
| POST | `/api/auth/login` | Publiczny; formularz loginIdentifier/password, odpowiedź UserResponse |
| POST | `/api/auth/logout` | Publiczny; unieważnienie sesji, 204 |
| POST | `/api/auth/register` | Publiczny; nowe konto USER, 201 |
| GET | `/api/auth/me` | Sesja; UserResponse |

Rejestracja wymaga `loginIdentifier`, `email`, `password` i zgodnego `confirmPassword`. Opcjonalnie przyjmuje `firstName` i `lastName`.

<a id="pl-użytkownicy-i-kategorie"></a>

### Użytkownicy i kategorie

| Metoda | Ścieżka | Dostęp i działanie |
| --- | --- | --- |
| GET | `/api/users/me` | Sesja; własny profil |
| GET | `/api/users/me/dashboard` | Sesja; liczniki, statusy, ostatnie sprawy, reakcje i aktywność |
| PATCH | `/api/users/me/profile` | Sesja; zmiana profilu, pominięte pola pozostają bez zmian |
| GET | `/api/admin/users` | ADMIN; wszystkie konta |
| GET | `/api/admin/users/{id}` | ADMIN; szczegóły konta |
| POST | `/api/admin/users` | ADMIN; utworzenie konta |
| PATCH | `/api/admin/users/{id}` | ADMIN; login, e-mail, hasło, aktywność, role lub profil |
| PATCH | `/api/admin/users/{id}/enabled` | ADMIN; włączenie/wyłączenie konta |
| GET | `/api/categories` | Sesja; aktywne kategorie, sortowane po nazwie |
| GET | `/api/admin/categories` | ADMIN; wszystkie kategorie |
| GET | `/api/admin/categories/{id}` | ADMIN; szczegóły kategorii |
| POST | `/api/admin/categories` | ADMIN; aktywna kategoria, nazwa unikalna bez względu na wielkość liter |
| PATCH | `/api/admin/categories/{id}` | ADMIN; name, description lub active |
| DELETE | `/api/admin/categories/{id}` | ADMIN; dezaktywacja przez active=false |

`UserResponse`: id, loginIdentifier, email, emailVerified, enabled, roles, profile.
`CategoryResponse`: id, name, description, active, createdAt.

<a id="pl-zgłoszenia"></a>

### Zgłoszenia

| Metoda | Ścieżka | Dostęp i działanie |
| --- | --- | --- |
| POST | `/api/tickets` | Klient USER bez AGENT/ADMIN; nowe zgłoszenie, 201 |
| GET | `/api/tickets/me` | USER; własna lista |
| GET | `/api/agent/tickets` | AGENT; pełna kolejka, można filtrować po agencie |
| GET | `/api/admin/tickets` | ADMIN; wszystkie zgłoszenia |
| GET | `/api/tickets/statuses` | Sesja; wartości statusów |
| GET | `/api/tickets/priorities` | Sesja; wszystkie priorytety |
| GET | `/api/agent/tickets/assignable-priorities` | AGENT/ADMIN; priorytety do ustawienia |
| GET | `/api/agent/dashboard` | AGENT; metryki i listy robocze |
| GET | `/api/tickets/{id}` | Dostęp do zgłoszenia; szczegóły |
| GET | `/api/tickets/{id}/events` | Dostęp do zgłoszenia; SSE |
| PATCH | `/api/tickets/{id}` | Właściciel lub ADMIN; tytuł, opis i kategoria |
| PATCH | `/api/tickets/{id}/status` | Właściciel USER; anulowanie/zamknięcie |
| PATCH | `/api/agent/tickets/{id}/assign` | AGENT; przypisanie do siebie, bez treści żądania |
| PATCH | `/api/agent/tickets/{id}/priority` | AGENT/ADMIN; treść żądania z polem priority |
| PATCH | `/api/agent/tickets/{id}/status` | AGENT/ADMIN; treść żądania z polem status |

Tworzenie wymaga `title`, `description` i `category` (nazwa aktywnej kategorii). Reguły stanów początkowych, przypisania i zmian opisuje [przebieg zgłoszenia](tickets.md#polski).

`TicketResponse`: id, title, description, status, priority, createdBy, assignedTo, category, createdAt, updatedAt, resolvedAt, closedAt.

SSE wysyła `connected` oraz po zatwierdzonej zmianie `ticket-change`, z polami `ticketId` i `timestamp`. Klient ponownie pobiera dane przez REST. Subskrypcje działają w obrębie jednego procesu backendu i mają limit czasu 30 minut.

<a id="pl-komentarze-historia-i-pliki"></a>

### Komentarze, historia i pliki

Wszystkie ścieżki wymagają dostępu do zgłoszenia. Widoczność komentarzy i plików zależy dodatkowo od [roli](roles.md#polski).

| Metoda | Ścieżka | Działanie |
| --- | --- | --- |
| POST | `/api/tickets/{id}/comments` | content i internal; komentarz, 201 |
| GET | `/api/tickets/{id}/comments` | Lista widocznych komentarzy |
| GET | `/api/tickets/{id}/history` | Historia od najstarszego wpisu |
| POST | `/api/tickets/{id}/attachments` | Multipart: file i opcjonalny commentId, 201 |
| GET | `/api/tickets/{id}/attachments` | Widoczne metadane plików |
| GET | `/api/tickets/{ticketId}/attachments/{attachmentId}` | Plik z Content-Disposition: attachment |
| DELETE | `/api/tickets/{ticketId}/attachments/{attachmentId}` | Własny plik lub dowolny plik dla ADMIN, 204 |

<a id="pl-paginacja-i-filtry"></a>

### Paginacja i filtry

Listy zgłoszeń zwracają `PageResponse`: content, page, size, totalElements, totalPages, first, last.

| Parametr | Wartość lub znaczenie |
| --- | --- |
| `page` | Od zera, domyślnie 0 |
| `size` | Domyślnie 20 |
| `sort` | Domyślnie createdAt,desc |
| `status`, `priority` | Wartości enumów |
| `category` | Nazwa kategorii |
| `agent` | Login przypisanego agenta |
| `createdFrom`, `createdTo` | Data i czas ISO; początek nie może być późniejszy niż koniec |

Przykład: `GET /api/agent/tickets?status=OPEN&priority=HIGH&page=0&size=10`.

<a id="pl-limity-i-walidacja"></a>

### Limity i walidacja

| Pole | Limit |
| --- | --- |
| Login | 3-50 znaków |
| E-mail | 100 znaków |
| Nowe/zmieniane hasło | Domyślnie min. 12 znaków, maks. 72 bajty UTF-8 |
| Imię i nazwisko | Po 50 znaków |
| Telefon / miasto / adres / kod pocztowy | 30 / 100 / 150 / 20 znaków |
| Tytuł / opis zgłoszenia | 150 / 4000 znaków |
| Nazwa / opis kategorii | 100 / 1000 znaków |
| Komentarz | 2000 znaków |
| Plik / całe żądanie multipart | 10 MB / 10 MB |

Walidacja DTO dopuszcza hasło o długości 6-100 znaków, ale `UserService` stosuje bardziej rygorystyczne wymagania opisane powyżej. Minimalną długość określa właściwość `app.security.min-password-length`; testy używają niższej wartości. Pozostałe limity pochodzą z `ApiContract.java`; frontend ma odpowiadające im `FIELD_LIMITS`.

Pełny zestaw pól żądań i odpowiedzi znajdziesz w [DTO backendu](../../helpdesk-api/src/main/java/macieserafin/pl/helpdesk/dto). Nie ma jeszcze OpenAPI; planowane endpointy są w [roadmapie](../roadmap.md#polski).

[Przejdź do wersji angielskiej](#english)

---

## English

[Wróć do wersji polskiej](#polski)

The backend is available locally at `http://localhost:8080`. The frontend usually calls relative `/api` paths through a proxy. Authentication uses sessions; state-changing requests require a [CSRF token](security.md#en-csrf).

<a id="en-formats-and-response-codes"></a>

### Formats and response codes

Requests and responses usually use JSON. Login accepts form data, uploads use multipart/form-data, downloads return binary files, and SSE uses text/event-stream.

| Code | Meaning |
| --- | --- |
| `200` | Success |
| `201` | Resource created |
| `204` | Success without content |
| `400` | Validation failure or invalid operation |
| `401` | Missing session or invalid login |
| `403` | Access denied or CSRF failure |
| `404` | Resource not found |
| `409` | Uniqueness conflict |
| `503` | Database readiness check failed |

Application errors use `ApiErrorResponse`:

```json
{
  "timestamp": "2026-10-14T12:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Title is required",
  "path": "/api/tickets",
  "errors": []
}
```

For validation errors, `errors` contains fields and messages.

<a id="en-authentication-and-readiness"></a>

### Authentication and readiness

Public paths do not require authentication. State-changing operations still require CSRF.

| Method | Path | Access and result |
| --- | --- | --- |
| GET | `/api/health` | Public; checks the database, returns UP or DOWN |
| GET | `/` | Public text confirming the process is running |
| GET | `/user`, `/agent`, `/admin` | Text checks for the corresponding role |
| GET | `/api/auth/csrf` | Public; session token and header name |
| POST | `/api/auth/login` | Public; loginIdentifier/password form, UserResponse |
| POST | `/api/auth/logout` | Public; invalidates the session, 204 |
| POST | `/api/auth/register` | Public; new USER account, 201 |
| GET | `/api/auth/me` | Session; UserResponse |

Registration requires `loginIdentifier`, `email`, `password` and a matching `confirmPassword`. `firstName` and `lastName` are optional.

<a id="en-users-and-categories"></a>

### Users and categories

| Method | Path | Access and operation |
| --- | --- | --- |
| GET | `/api/users/me` | Session; own profile |
| GET | `/api/users/me/dashboard` | Session; counts, statuses, recent tickets, action items and activity |
| PATCH | `/api/users/me/profile` | Session; updates profile, omitted fields stay unchanged |
| GET | `/api/admin/users` | ADMIN; all accounts |
| GET | `/api/admin/users/{id}` | ADMIN; account details |
| POST | `/api/admin/users` | ADMIN; creates an account |
| PATCH | `/api/admin/users/{id}` | ADMIN; login, email, password, activity, roles or profile |
| PATCH | `/api/admin/users/{id}/enabled` | ADMIN; enables/disables an account |
| GET | `/api/categories` | Session; active categories sorted by name |
| GET | `/api/admin/categories` | ADMIN; all categories |
| GET | `/api/admin/categories/{id}` | ADMIN; category details |
| POST | `/api/admin/categories` | ADMIN; active category with a case-insensitive unique name |
| PATCH | `/api/admin/categories/{id}` | ADMIN; name, description or active |
| DELETE | `/api/admin/categories/{id}` | ADMIN; deactivates with active=false |

`UserResponse`: id, loginIdentifier, email, emailVerified, enabled, roles, profile.
`CategoryResponse`: id, name, description, active, createdAt.

<a id="en-tickets"></a>

### Tickets

| Method | Path | Access and operation |
| --- | --- | --- |
| POST | `/api/tickets` | Customer USER without AGENT/ADMIN; new ticket, 201 |
| GET | `/api/tickets/me` | USER; own list |
| GET | `/api/agent/tickets` | AGENT; full queue, filterable by agent |
| GET | `/api/admin/tickets` | ADMIN; all tickets |
| GET | `/api/tickets/statuses` | Session; status values |
| GET | `/api/tickets/priorities` | Session; all priorities |
| GET | `/api/agent/tickets/assignable-priorities` | AGENT/ADMIN; assignable priorities |
| GET | `/api/agent/dashboard` | AGENT; metrics and work lists |
| GET | `/api/tickets/{id}` | Ticket access; details |
| GET | `/api/tickets/{id}/events` | Ticket access; SSE |
| PATCH | `/api/tickets/{id}` | Owner or ADMIN; title, description and category |
| PATCH | `/api/tickets/{id}/status` | Owner USER; cancel/close |
| PATCH | `/api/agent/tickets/{id}/assign` | AGENT; assigns to self, no request body |
| PATCH | `/api/agent/tickets/{id}/priority` | AGENT/ADMIN; body with priority |
| PATCH | `/api/agent/tickets/{id}/status` | AGENT/ADMIN; body with status |

Creation requires `title`, `description` and `category` (the name of an active category). See the [workflow](tickets.md#english) for initial states, assignment and transitions.

`TicketResponse`: id, title, description, status, priority, createdBy, assignedTo, category, createdAt, updatedAt, resolvedAt, closedAt.

SSE sends `connected` and, after a committed change, `ticket-change`, with `ticketId` and `timestamp`. The client fetches data again through REST. Subscriptions are local to the backend process and have a 30-minute timeout.

<a id="en-comments-history-and-files"></a>

### Comments, history and files

All paths require ticket access. Comment and file visibility also depends on the [role](roles.md#english).

| Method | Path | Operation |
| --- | --- | --- |
| POST | `/api/tickets/{id}/comments` | content and internal; creates a comment, 201 |
| GET | `/api/tickets/{id}/comments` | Visible comments |
| GET | `/api/tickets/{id}/history` | History, oldest first |
| POST | `/api/tickets/{id}/attachments` | Multipart: file and optional commentId, 201 |
| GET | `/api/tickets/{id}/attachments` | Visible file metadata |
| GET | `/api/tickets/{ticketId}/attachments/{attachmentId}` | File with Content-Disposition: attachment |
| DELETE | `/api/tickets/{ticketId}/attachments/{attachmentId}` | Own upload, or any for ADMIN, 204 |

<a id="en-pagination-and-filters"></a>

### Pagination and filters

Ticket lists return `PageResponse`: content, page, size, totalElements, totalPages, first, last.

| Parameter | Value or meaning |
| --- | --- |
| `page` | Zero-based, defaults to 0 |
| `size` | Defaults to 20 |
| `sort` | Defaults to createdAt,desc |
| `status`, `priority` | Enum values |
| `category` | Category name |
| `agent` | Assigned agent's login |
| `createdFrom`, `createdTo` | ISO date and time; start must not be later than end |

Example: `GET /api/agent/tickets?status=OPEN&priority=HIGH&page=0&size=10`.

<a id="en-limits-and-validation"></a>

### Limits and validation

| Field | Limit |
| --- | --- |
| Login | 3-50 characters |
| Email | 100 characters |
| New/changed password | At least 12 characters by default, at most 72 UTF-8 bytes |
| First and last name | 50 characters each |
| Phone / city / address / postal code | 30 / 100 / 150 / 20 characters |
| Ticket title / description | 150 / 4000 characters |
| Category name / description | 100 / 1000 characters |
| Comment | 2000 characters |
| File / entire multipart request | 10 MB / 10 MB |

DTOs allow passwords of 6-100 characters, but `UserService` enforces the stronger policy above. `app.security.min-password-length` sets the minimum; tests use a lower value. Other limits come from `ApiContract.java`; the frontend has matching `FIELD_LIMITS`.

The complete request and response fields are in the [backend DTOs](../../helpdesk-api/src/main/java/macieserafin/pl/helpdesk/dto). OpenAPI is not implemented yet; planned endpoints are in the [roadmap](../roadmap.md#english).

[Back to the Polish version](#polski)

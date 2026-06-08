# Przegląd API

Backend wystawia REST API pod prefiksem `/api`. Wyjątkiem jest `GET /`, który zwraca prosty tekst potwierdzający, że aplikacja działa, oraz testowe ścieżki `/user`, `/agent` i `/admin`.

## Konwencje

API używa JSON dla requestów i odpowiedzi. Upload załączników działa przez `multipart/form-data`. Pobieranie załącznika zwraca strumień binarny z nagłówkiem `Content-Disposition`.

Autoryzacja przeglądarkowa działa przez sesję HTTP:

- logowanie: `POST /api/auth/login`,
- aktualny użytkownik: `GET /api/auth/me`,
- wylogowanie: `POST /api/auth/logout`,
- cookie sesji: `JSESSIONID`.

Frontend wysyła wszystkie requesty przez `fetch` z `credentials: 'include'`. Dzięki temu przeglądarka automatycznie dołącza cookie sesyjne.

## Główne Grupy Endpointów

Auth:

- `POST /api/auth/login`
- `POST /api/auth/logout`
- `GET /api/auth/me`
- `POST /api/auth/register`

Użytkownik:

- `GET /api/users/me`
- `GET /api/users/me/dashboard`
- `PATCH /api/users/me/profile`

Admin:

- `/api/admin/users`
- `/api/admin/categories`
- `/api/admin/tickets`

Kategorie:

- `GET /api/categories`

Tickety:

- `/api/tickets`
- `/api/tickets/me`
- `/api/tickets/{id}`
- `/api/tickets/{id}/comments`
- `/api/tickets/{id}/history`
- `/api/tickets/{id}/attachments`

Agent:

- `/api/agent/dashboard`
- `/api/agent/tickets`
- `/api/agent/tickets/{id}/assign`
- `/api/agent/tickets/{id}/status`
- `/api/agent/tickets/{id}/priority`

Pełna lista endpointów jest w [backend-api.md](./backend-api.md).

## Błędy

Backend zwraca błędy w formacie `ApiErrorResponse`.

```json
{
  "timestamp": "2026-06-08T12:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Title is required",
  "path": "/api/tickets",
  "errors": []
}
```

Przy błędach walidacji pole `errors` zawiera listę pól i komunikatów.

Typowe kody:

- `200`: operacja zakończona sukcesem.
- `201`: zasób utworzony.
- `204`: sukces bez body.
- `400`: błąd walidacji albo niedozwolona operacja domenowa.
- `401`: brak sesji albo niepoprawne dane logowania.
- `403`: zalogowany użytkownik nie ma dostępu.
- `404`: brak zasobu.
- `409`: konflikt unikalności.

## Paginacja I Filtrowanie

Listy ticketów zwracają `PageResponse<T>`.

Pola odpowiedzi:

- `content`: lista elementów.
- `page`: numer strony liczony od zera.
- `size`: rozmiar strony.
- `totalElements`: liczba wszystkich rekordów.
- `totalPages`: liczba stron.
- `first`: czy to pierwsza strona.
- `last`: czy to ostatnia strona.

Parametry paginacji:

- `page`: domyślnie `0`.
- `size`: domyślnie `20`.
- `sort`: domyślnie `createdAt,desc`.

Filtry ticketów:

- `status`: wartość `TicketStatus`.
- `priority`: wartość `TicketPriority`.
- `category`: nazwa kategorii.
- `agent`: `loginIdentifier` przypisanego agenta.
- `createdFrom`: data i czas ISO.
- `createdTo`: data i czas ISO.

Przykład:

```text
GET /api/agent/tickets?status=OPEN&priority=HIGH&page=0&size=10
```

## Kontrakty DTO

DTO requestów walidują wymagane pola i długości. Limity są współdzielone z encjami przez `ApiContract.java`, a frontend trzyma analogiczne wartości w `FIELD_LIMITS`.

Najważniejsze limity:

- login: od 3 do 50 znaków,
- email: do 100 znaków,
- hasło: od 6 do 100 znaków,
- tytuł ticketu: do 150 znaków,
- opis ticketu: do 4000 znaków,
- nazwa kategorii: do 100 znaków,
- komentarz: do 2000 znaków,
- załącznik: do 10 MB.

## Zasady Bezpieczeństwa API

`SecurityConfig` chroni ścieżki po rolach. Serwisy dodatkowo sprawdzają dostęp do konkretnego zasobu. Przykład: sam fakt zalogowania nie wystarcza, żeby użytkownik zobaczył cudzy ticket.

Model ról:

- `USER`: operacje własnego konta i własnych ticketów.
- `AGENT`: kolejka, obsługa ticketów, komentarze wewnętrzne.
- `ADMIN`: administracja systemem i pełny wgląd w tickety.

Szczegóły są w [roles-and-permissions.md](./roles-and-permissions.md).

## Planowane Rozszerzenia API

W kodzie nie ma jeszcze:

- OpenAPI lub Swagger UI,
- wersjonowania endpointów,
- migracji DB powiązanych z kontraktem,
- dedykowanego dashboardu admina po stronie backendu,
- endpointu reassignu do wskazanego agenta,
- dedykowanych list `assigned-to-me` i `unassigned`,
- token flow CSRF.

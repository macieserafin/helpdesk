# Frontend

[Polski](#polski) | [English](#english)

## Polski

Frontend jest aplikacją jednostronicową (SPA), napisaną w czystym JavaScripcie i budowaną za pomocą Vite. Widoki są funkcjami zwracającymi DOM, a router i stan są modułami projektu. Backend weryfikuje uprawnienia niezależnie od UI.

<a id="pl-start-i-renderowanie"></a>

### Start i renderowanie

`main.js` uruchamia `startApp()` z `app/app.js`. Aplikacja dodaje obszar powiadomień, nasłuchuje `hashchange` i renderuje widok w `#app`.

Dla chronionej trasy:

1. `requireAuth()` odświeża użytkownika z backendu.
2. Jeśli sesja jest nieaktywna, aplikacja czyści stan i wyświetla stronę logowania.
3. `requireRole()` sprawdza role trasy; brak dostępu kieruje na stronę startową.
4. `ShellLayout` pokazuje stan ładowania.
5. Asynchroniczna funkcja strony pobiera dane i zwraca DOM.
6. Gotowy widok zastępuje wskaźnik ładowania.

Po wejściu na stronę logowania lub rejestracji użytkownik z aktywną sesją jest przekierowywany do panelu swojej roli. `authService.primaryRole()` wybiera ADMIN, AGENT, a potem USER.

<a id="pl-trasy"></a>

### Trasy

Router korzysta z `window.location.hash`, np. `#/tickets/12`. `navigate()` zmienia hash, `currentPath()` odczytuje ścieżkę, a `matchRoute()` dopasowuje parametry takie jak `:id`. Definicje są w `app/routes.js`.

| Dostęp | Ścieżki |
| --- | --- |
| Publiczny | `/login`, `/register` |
| USER | `/user`, `/user/tickets`, `/user/tickets/new` |
| AGENT | `/agent`, `/agent/tickets`, `/agent/assigned` |
| ADMIN | `/admin`, `/admin/users`, `/admin/categories`, `/admin/tickets` |
| Wszystkie trzy role | `/user/profile`, `/tickets/:id` |
| Niedopasowana trasa | `NotFoundPage` |

<a id="pl-api-i-stan"></a>

### API i stan

`api/httpClient.js` buduje adresy URL i parametry zapytań, serializuje JSON, dołącza cookie, obsługuje CSRF, wskaźnik ładowania, odpowiedzi JSON i tekstowe oraz kod 204 i błędy `ApiError`. Pobrane pliki są reprezentowane jako obiekty Blob. `VITE_API_BASE_URL` jest domyślnie puste, więc żądania korzystają z proxy.

| Moduł API | Obszar |
| --- | --- |
| `authApi.js` | Logowanie, rejestracja, wylogowanie i bieżący użytkownik |
| `userApi.js` | Profil i panel użytkownika |
| `ticketApi.js` | Zgłoszenia użytkownika, komentarze, historia i SSE |
| `agentApi.js` | Kolejka, panel i akcje agenta |
| `adminApi.js` | Konta i lista zgłoszeń administratora |
| `categoryApi.js` | Kategorie aktywne i administracja |
| `attachmentApi.js` | Lista, upload, download i usuwanie plików |

`authStore.js` trzyma użytkownika w pamięci. `uiStore.js` liczy aktywne żądania i steruje wskaźnikiem ładowania. Dane domenowe są lokalne dla widoków i pobierane ponownie po zmianach. Szczegóły sesji są w [bezpieczeństwie](../reference/security.md#polski).

<a id="pl-najważniejsze-widoki"></a>

### Najważniejsze widoki

| Widok | Działanie i ograniczenia |
| --- | --- |
| `UserDashboardPage` | Własne metryki, statusy, ostatnie sprawy, reakcje i aktywność |
| `MyTicketsPage`, `TicketQueuePage`, `AllTicketsPage` | Listy z filtrami i paginacją dla odpowiedniej roli |
| `AssignedTicketsPage` | Kolejka z filtrem agent po loginie; pierwsza strona, do 100 rekordów |
| `AgentDashboardPage` | Listy robocze i szybkie akcje przypisania, statusu i priorytetu |
| `AdminDashboardPage` | KPI liczone z kont i pierwszej strony zgłoszeń; brak pełnej analityki backendowej |
| `UsersManagementPage`, `CategoriesManagementPage` | Formularze i tabele administracyjne |
| `CreateTicketPage`, `ProfilePage` | Tworzenie sprawy i aktualizacja profilu |
| `TicketDetailsPage` | Dane zgłoszenia, komentarze, historia, pliki i akcje |

Widok szczegółów zgłoszenia pobiera dane równolegle i odświeża je po każdej zmianie. Zdarzenie `ticket-change`, odebrane przez EventSource, uruchamia ponowne pobranie danych z REST API.

<a id="pl-komentarze-i-załączniki"></a>

### Komentarze i załączniki

`CommentPanel` grupuje pliki po `commentId`; pliki bez komentarza pokazuje osobno. Widoczność komentarzy wewnętrznych wynika z backendu.

Dodanie komentarza z plikami przebiega kolejno: walidacja plików, zapis komentarza, przesłanie każdego pliku z identyfikatorem komentarza, odświeżenie. Jeśli nie uda się przesłać części plików, komentarz pozostaje zapisany, a interfejs informuje, które operacje się powiodły.

<a id="pl-komponenty-i-style"></a>

### Komponenty i style

| Katalog | Zawartość |
| --- | --- |
| `components/layout` | ShellLayout, Sidebar z menu dopasowanym do roli, Navbar z wylogowaniem i wskaźnikiem ładowania |
| `components/tickets` | Tabela, filtry, paginacja, formularze, CommentPanel, HistoryTimeline i attachmentUpload |
| `components/users`, `components/categories` | Formularze i tabele administracyjne |
| `components/common` | Badges, Feedback, ConfirmDialog, PageHeader i Toast |
| `utils` | Stałe, walidacja, bezpieczne wstawianie tekstu do DOM, format dat/plików, odczyt paginacji i błędów |

`styles/variables.css` definiuje kolory i odstępy. `main.css` zawiera importy i style podstawowe, a `layout.css`, `components.css` i `pages.css` odpowiadają za układ, komponenty i poszczególne widoki. Nie ma frameworka CSS.

Przy dodaniu funkcji zaktualizuj właściwy moduł API, widok i komponenty. Zmiany limitów uzgodnij z `ApiContract.java`. Dane użytkownika wstawiaj za pomocą bezpiecznych funkcji DOM lub funkcji kodujących znaki specjalne z `utils/dom.js`.

[Przejdź do wersji angielskiej](#english)

---

## English

[Wróć do wersji polskiej](#polski)

The frontend is a single-page application (SPA) written in plain JavaScript and built with Vite. Views are functions returning DOM elements; routing and state are project modules. The backend checks permissions independently of the UI.

<a id="en-startup-and-rendering"></a>

### Startup and rendering

`main.js` calls `startApp()` from `app/app.js`. The application adds the notification host, listens for `hashchange` and renders into `#app`.

For a protected route:

1. `requireAuth()` refreshes the user from the backend.
2. If the session is inactive, the application clears state and shows login.
3. `requireRole()` checks route roles; denied access redirects to the home page.
4. `ShellLayout` shows a loading state.
5. The asynchronous page function fetches data and returns DOM.
6. The completed view replaces the loading indicator.

An active session on login or registration redirects to the role's dashboard. `authService.primaryRole()` chooses ADMIN, then AGENT, then USER.

<a id="en-routes"></a>

### Routes

The router uses `window.location.hash`, for example `#/tickets/12`. `navigate()` changes the hash, `currentPath()` reads the path, and `matchRoute()` matches parameters such as `:id`. Definitions are in `app/routes.js`.

| Access | Paths |
| --- | --- |
| Public | `/login`, `/register` |
| USER | `/user`, `/user/tickets`, `/user/tickets/new` |
| AGENT | `/agent`, `/agent/tickets`, `/agent/assigned` |
| ADMIN | `/admin`, `/admin/users`, `/admin/categories`, `/admin/tickets` |
| All three roles | `/user/profile`, `/tickets/:id` |
| Unmatched route | `NotFoundPage` |

<a id="en-api-and-state"></a>

### API and state

`api/httpClient.js` builds URLs and query parameters, serializes JSON, sends cookies and handles CSRF, loading state, JSON/text/204 responses and `ApiError`. Downloads use Blob objects. `VITE_API_BASE_URL` is empty by default, so requests use the proxy.

| API module | Area |
| --- | --- |
| `authApi.js` | Login, registration, logout and current user |
| `userApi.js` | Profile and user dashboard |
| `ticketApi.js` | User tickets, comments, history and SSE |
| `agentApi.js` | Queue, dashboard and agent actions |
| `adminApi.js` | Accounts and administrator ticket list |
| `categoryApi.js` | Active categories and administration |
| `attachmentApi.js` | Listing, uploading, downloading and deleting files |

`authStore.js` keeps the user in memory. `uiStore.js` counts active requests and controls the loading indicator. Domain data belongs to views and is fetched again after changes. See [sessions](../reference/security.md#english) for details.

<a id="en-key-views"></a>

### Key views

| View | Behavior and limitations |
| --- | --- |
| `UserDashboardPage` | Own metrics, statuses, recent tickets, action items and activity |
| `MyTicketsPage`, `TicketQueuePage`, `AllTicketsPage` | Filtered, paginated lists for each role |
| `AssignedTicketsPage` | Queue filtered by agent login; first page, up to 100 records |
| `AgentDashboardPage` | Work lists and quick assignment, status and priority actions |
| `AdminDashboardPage` | KPIs calculated from accounts and the first ticket page; no complete backend analytics |
| `UsersManagementPage`, `CategoriesManagementPage` | Administration forms and tables |
| `CreateTicketPage`, `ProfilePage` | Ticket creation and profile updates |
| `TicketDetailsPage` | Ticket data, comments, history, files and actions |

The ticket details view fetches data in parallel and refreshes it after each change. A `ticket-change` event received through EventSource triggers another REST fetch.

<a id="en-comments-and-attachments"></a>

### Comments and attachments

`CommentPanel` groups files by `commentId` and shows files without comments separately. The backend determines internal comment visibility.

Posting a comment with files proceeds in order: validate files, save the comment, upload each file with the comment ID, then refresh. If some uploads fail, the comment remains saved and the UI reports which operations succeeded.

<a id="en-components-and-styles"></a>

### Components and styles

| Directory | Contents |
| --- | --- |
| `components/layout` | ShellLayout, role-based Sidebar, Navbar with logout and loading indicator |
| `components/tickets` | Table, filters, pagination, forms, CommentPanel, HistoryTimeline and attachmentUpload |
| `components/users`, `components/categories` | Administration forms and tables |
| `components/common` | Badges, Feedback, ConfirmDialog, PageHeader and Toast |
| `utils` | Constants, validation, safe DOM text handling, date/file formatting, pagination and errors |

`styles/variables.css` defines colors and spacing. `main.css` holds imports and base styles; `layout.css`, `components.css` and `pages.css` cover layouts, components and views. There is no CSS framework.

When adding a feature, update its API module, view and components. Keep limits aligned with `ApiContract.java`. Insert user data through safe DOM functions or escaping helpers in `utils/dom.js`.

[Back to the Polish version](#polski)

# Architektura Frontendu

Frontend jest jednoplikową aplikacją SPA zbudowaną na Vite i vanilla JavaScript. Nie używa Reacta, Vue ani zewnętrznej biblioteki routingu. Kod jest podzielony na małe moduły: router, auth, API client, stan, strony, komponenty i utilsy.

## Rola Frontendu W Systemie

Frontend odpowiada za:

- logowanie, rejestrację i odświeżanie sesji,
- routing po rolach,
- prezentację dashboardów,
- formularze ticketów, użytkowników i kategorii,
- listy z filtrami i paginacją,
- szczegóły ticketu,
- komentarze, historię i załączniki,
- podstawową walidację po stronie przeglądarki,
- spójne etykiety ról, statusów i priorytetów.

Reguły bezpieczeństwa nie kończą się w UI. Frontend ukrywa niedostępne akcje, ale backend nadal weryfikuje role i dostęp do konkretnego zasobu.

## Wejście Aplikacji

`src/main.js` uruchamia `startApp()` z `src/app/app.js`.

`startApp()`:

- montuje host toastów,
- podpina listener `hashchange`,
- wywołuje pierwszy render.

Frontend używa jednego elementu root:

```html
<div id="app"></div>
```

Każda zmiana trasy renderuje nowy widok w tym kontenerze.

## Routing

Routing działa na `window.location.hash`.

Przykłady:

- `#/login`,
- `#/user`,
- `#/tickets/12`.

`src/app/router.js` udostępnia:

- `navigate(path)`: zmienia hash,
- `currentPath()`: zwraca bieżącą ścieżkę albo `/login`,
- `matchRoute(path)`: dopasowuje trasę z parametrami.

Parametry są obsługiwane przez segmenty z dwukropkiem, na przykład `/tickets/:id`.

## Mapa Tras

`src/app/routes.js` wiąże ścieżki z widokami i rolami.

Publiczne:

- `/login`,
- `/register`.

Użytkownik:

- `/user`,
- `/user/tickets`,
- `/user/tickets/new`.

Agent:

- `/agent`,
- `/agent/tickets`,
- `/agent/assigned`.

Admin:

- `/admin`,
- `/admin/users`,
- `/admin/categories`,
- `/admin/tickets`.

Wspólne:

- `/user/profile`,
- `/tickets/:id`.

Trasa `*` renderuje stronę `NotFoundPage`.

## Lifecycle Renderowania

`src/app/app.js` jest centralnym miejscem renderowania.

Dla trasy publicznej `/login` albo `/register` aplikacja próbuje odświeżyć aktualnego użytkownika. Jeśli sesja jest aktywna, użytkownik trafia na dashboard swojej roli.

Dla trasy chronionej aplikacja:

1. Wywołuje `requireAuth()`.
2. Przy braku sesji czyści stan i przenosi na `/login`.
3. Sprawdza role przez `requireRole()`.
4. Przy braku roli przenosi na stronę startową użytkownika.
5. Montuje `ShellLayout` ze stanem ładowania.
6. Wywołuje asynchroniczną funkcję strony.
7. Montuje gotowy widok w layoucie.

Ten model pozwala stronie pobrać dane przed oddaniem gotowego DOM. Widoki są zwykłymi funkcjami zwracającymi element HTML.

## Sesja I Auth

`authService.js` jest fasadą dla sesji:

- `login()` wywołuje API logowania i zapisuje użytkownika w pamięci.
- `refreshCurrentUser()` pobiera `GET /api/auth/me`.
- `logout()` wywołuje backend i czyści stan.
- `primaryRole()` ustala priorytet roli.
- `homeRouteFor()` zwraca dashboard właściwy dla użytkownika.

`authStore.js` przechowuje tylko bieżącego użytkownika w zmiennej modułu. Nie ma zapisu do `localStorage` ani `sessionStorage`.

`authGuard.js` wymusza odświeżenie sesji przed wejściem na trasę chronioną.

`roleGuard.js` sprawdza, czy użytkownik ma jedną z ról przypisanych do trasy.

## Klient HTTP

`src/api/httpClient.js` jest wspólną warstwą komunikacji z backendem.

Odpowiada za:

- budowanie URL,
- budowanie query stringów,
- JSON serialization,
- `credentials: 'include'`,
- globalny loader,
- parsowanie JSON, tekstu i odpowiedzi `204`,
- zamianę błędów na `ApiError`,
- pobieranie plików jako blob.

`API_BASE_URL` pochodzi z `VITE_API_BASE_URL`. Domyślnie jest pusty, więc requesty idą do tego samego originu i korzystają z proxy Vite albo nginx.

## Moduły API

`authApi.js`

Logowanie, logout, rejestracja i aktualny użytkownik.

`ticketApi.js`

Operacje użytkownika na ticketach: tworzenie, własna lista, szczegóły, edycja, status, komentarze i historia.

`agentApi.js`

Kolejka agenta, dashboard agenta, przypisanie, status, priorytet i lista priorytetów możliwych do ustawienia.

`adminApi.js`

Użytkownicy i administracyjna lista ticketów.

`categoryApi.js`

Lista aktywnych kategorii oraz administracyjne zarządzanie słownikiem.

`attachmentApi.js`

Lista załączników, upload, download i usuwanie plików.

`userApi.js`

Profil bieżącego użytkownika i aktualizacja danych profilu.

## Stan Aplikacji

Stan jest celowo prosty.

`authStore.js`:

- trzyma bieżącego użytkownika,
- czyści użytkownika przy wygaśnięciu sesji albo logoutcie.

`uiStore.js`:

- liczy aktywne requesty,
- powiadamia subskrybentów,
- steruje globalnym loaderem w navbarze.

Brak globalnego store dla danych domenowych. Widoki pobierają dane przy wejściu i odświeżają je po mutacjach.

## Layout

`ShellLayout.js` składa ekran po zalogowaniu z sidebara, topbara i kontenera treści.

`Sidebar.js` buduje menu na podstawie roli użytkownika. Każda rola dostaje inne wejścia:

- użytkownik: dashboard, moje tickety, nowe zgłoszenie, profil,
- agent: dashboard agenta, kolejka, przypisane tickety, profil,
- admin: dashboard admina, użytkownicy, kategorie, tickety.

`Navbar.js` pokazuje aktualnego użytkownika, przycisk wylogowania i globalny loader.

## Strony Użytkownika

`UserDashboardPage.js`

Pobiera dashboard użytkownika i aktywne kategorie. Pokazuje KPI, ostatnie tickety, zgłoszenia wymagające reakcji, rozbicie po statusach, aktywność i szybkie ścieżki tworzenia ticketu.

`MyTicketsPage.js`

Wyświetla własne tickety z filtrami i paginacją.

`CreateTicketPage.js`

Pobiera aktywne kategorie i renderuje formularz tworzenia ticketu.

`ProfilePage.js`

Aktualizuje profil bieżącego użytkownika.

## Strony Agenta

`AgentDashboardPage.js`

Jest centrum pracy agenta. Pobiera dashboard i priorytety, a potem pokazuje:

- KPI agenta,
- moje zgłoszenia,
- tickety do przejęcia,
- tickety z odpowiedzią klienta,
- wysokie priorytety,
- zgłoszenia rozwiązane dziś.

Widok ma szybkie akcje: przypisanie do siebie, zmiana statusu, zmiana priorytetu i przejście do szczegółów.

`TicketQueuePage.js`

Pełna kolejka agenta z filtrami i paginacją. Korzysta z `GET /api/agent/tickets`.

`AssignedTicketsPage.js`

Pokazuje tickety przypisane do aktualnego agenta. Ponieważ backend nie ma jeszcze endpointu `assigned-to-me`, widok korzysta z kolejki agenta i filtruje dane po stronie frontendu.

## Strony Admina

`AdminDashboardPage.js`

Pobiera użytkowników i pierwszą stronę ticketów. Liczy podstawowe KPI po stronie frontendu: liczba użytkowników, aktywni użytkownicy, liczba ticketów i aktywne procesy. To rozwiązanie tymczasowe do czasu backendowego `GET /api/admin/dashboard`.

`UsersManagementPage.js`

Łączy tabelę użytkowników z formularzem tworzenia i edycji. Obsługuje zmianę aktywności konta.

`CategoriesManagementPage.js`

Obsługuje słownik kategorii: tworzenie, edycję, dezaktywację i aktywację.

`AllTicketsPage.js`

Administracyjna lista wszystkich ticketów z filtrami i paginacją.

## Szczegóły Ticketu

`TicketDetailsPage.js` jest najbogatszym widokiem frontendu.

Po wejściu pobiera równolegle:

- szczegóły ticketu,
- komentarze,
- historię,
- załączniki.

Widok składa się z:

- nagłówka ticketu,
- danych ticketu,
- opisu,
- panelu komentarzy,
- panelu akcji,
- historii zmian.

Akcje zależą od roli i stanu ticketu:

- właściciel albo admin mogą edytować nieterminalny ticket,
- agent może przypisać otwarty ticket do siebie,
- użytkownik i staff mają różne ścieżki zmiany statusu,
- staff może zmieniać priorytet,
- właściciel może zamknąć rozwiązany ticket.

Po każdej mutacji widok wywołuje lokalne `load()` i odświeża dane.

## Komentarze I Załączniki W UI

`CommentPanel.js` grupuje załączniki po `commentId`. Pliki bez komentarza trafiają do osobnej sekcji.

Panel obsługuje:

- dodanie komentarza,
- opcjonalny upload wielu załączników do nowego komentarza,
- pobranie pliku,
- usunięcie pliku po potwierdzeniu,
- widoczność komentarzy wewnętrznych dla staffu.

Upload komentarza działa sekwencyjnie:

1. Walidacja plików po stronie frontendu.
2. Utworzenie komentarza.
3. Upload każdego pliku z `commentId` nowego komentarza.
4. Odświeżenie panelu.

Jeśli komentarz został dodany, ale upload pliku się nie uda, UI pokazuje komunikat częściowego sukcesu i odświeża dane.

## Komponenty Ticketów

`TicketTable.js`

Tabela ticketów używana przez listy użytkownika, agenta i admina.

`TicketFilters.js`

Formularz filtrów dla statusu, priorytetu, kategorii, agenta i zakresu dat. Wspiera warianty dla różnych widoków.

`Pagination.js`

Obsługuje `PageResponse` z backendu.

`TicketForm.js`

Tworzenie ticketu z kategorią i opcjonalnymi załącznikami.

`TicketEditForm.js`

Edycja tytułu, opisu i kategorii.

`TicketStatusForm.js`

Pokazuje statusy dostępne dla aktualnej roli i aktualnego statusu.

`TicketPriorityForm.js`

Pozwala staffowi zmienić priorytet.

`HistoryTimeline.js`

Renderuje wpisy `TicketHistoryResponse`.

`attachmentUpload.js`

Waliduje limit pliku i opakowuje upload załącznika.

## Komponenty Admina I Słowników

`UserTable.js` i `UserForm.js`

Lista, wybór, tworzenie i edycja użytkowników.

`CategoryTable.js` i `CategoryForm.js`

Lista, wybór, tworzenie, edycja i aktywacja lub dezaktywacja kategorii.

## Komponenty Wspólne

`Badges.js`

Etykiety statusów, priorytetów i ról.

`Feedback.js`

Stany ładowania, braku danych i błędu.

`ConfirmDialog.js`

Modal potwierdzenia akcji destrukcyjnej, używany przy usuwaniu załącznika i dezaktywacji.

`PageHeader.js`

Spójny nagłówek widoku.

`Toast.js`

Globalne komunikaty sukcesu, ostrzeżenia i błędu.

## Utils

`constants.js`

Źródło etykiet i limitów po stronie frontendu. Musi pozostać zgodne z enumami backendu i `ApiContract.java`.

`dom.js`

Tworzenie elementów z HTML, escapowanie tekstu, selektory, zamiana zawartości i odczyt formularzy.

`dateFormatter.js`

Format dat i czasu.

`fileFormatter.js`

Format rozmiaru pliku.

`pageResponse.js`

Bezpieczny odczyt `content` i metadanych paginacji.

`errorMessage.js`

Wydobywa czytelny komunikat z `ApiError` albo zwykłego wyjątku.

`userDisplay.js`

Buduje nazwę wyświetlaną użytkownika i login.

`validators.js`

Pomocnicza walidacja pól i normalizacja ról.

## Style

Style są podzielone na:

- `variables.css`: zmienne kolorów, spacingu i cieni,
- `main.css`: importy i globalne bazowe style,
- `layout.css`: shell, sidebar, topbar i układ stron,
- `components.css`: formularze, tabele, przyciski, badge i dialogi,
- `pages.css`: style widoków domenowych i dashboardów.

Projekt nie używa żadnego frameworka CSS. Klasy są kontrolowane przez komponenty JS.

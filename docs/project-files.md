# Katalog Plików Projektu

Ten przewodnik opisuje pliki ważne dla zrozumienia projektu.

## Katalog Główny

### README.md

Wejście do projektu. Zawiera szybki start, role, konta testowe i linki do dokumentacji szczegółowej.

Współpracuje z:

- `docs/README.md`
- `docker-compose.yml`

### .env.example

Przykład lokalnych zmiennych środowiskowych. Używany przy Docker Compose, gdy developer chce nadpisać porty, dane PostgreSQL, CORS albo ustawienia seeda.

Współpracuje z:

- `docker-compose.yml`
- `helpdesk-api/src/main/resources/application.properties`

### docker-compose.yml

Lokalny stack aplikacji. Uruchamia PostgreSQL, backend i frontendowy nginx. Mapuje frontend na `FRONTEND_PORT` albo `3000`, backend na `BACKEND_PORT` albo `8080`, a dane bazy i uploady trzyma w wolumenach.

Współpracuje z:

- `helpdesk-api/Dockerfile`
- `helpdesk-frontend/Dockerfile`
- `.env.example`

### docker-compose.prod.yml

Produkcyjny wariant stosu. Dodaje Caddy, ukrywa backend i PostgreSQL w sieci Dockera, używa jawnych zmiennych z pliku `.env` i wymusza konfigurację haseł demo przez zmienne aplikacyjne.

Współpracuje z:

- `deploy/Caddyfile`
- `deploy/production.env.example`
- `deploy/backup.sh`

## Deploy

### deploy/Caddyfile

Konfiguracja publicznego reverse proxy. Caddy przyjmuje ruch dla `APP_DOMAIN`, kompresuje odpowiedzi i przekazuje requesty do kontenera `frontend:80`.

Współpracuje z:

- `docker-compose.prod.yml`
- `helpdesk-frontend/nginx.conf`

### deploy/production.env.example

Szablon produkcyjnego `.env`. Określa domenę, dane PostgreSQL, CORS, tryb seeda i hasła kont demo.

Współpracuje z:

- `docker-compose.prod.yml`
- `SeedConfig`

### deploy/backup.sh

Skrypt backupu bazy danych i uploadów. Przydatny na VPS, gdzie dane są trzymane w wolumenach Dockera.

Współpracuje z:

- wolumenem PostgreSQL
- wolumenem `backend_uploads`

## Backend

### helpdesk-api/pom.xml

Definicja projektu Maven. Ustala Java 17, Spring Boot 4.0.5 oraz zależności do Web MVC, Jacksona, JPA, Security, Validation, PostgreSQL i testów.

Współpracuje z:

- `HelpdeskApplication.java`
- testami integracyjnymi

### helpdesk-api/Dockerfile

Buduje obraz backendu. Uruchamia aplikację Spring Boot w kontenerze używanym przez Docker Compose.

Współpracuje z:

- `docker-compose.yml`
- `docker-compose.prod.yml`

### helpdesk-api/src/main/resources/application.properties

Centralna konfiguracja backendu. Definiuje datasource, Hibernate, multipart, storage załączników, CORS, Basic Auth i seed demo.

Współpracuje z:

- `SecurityConfig`
- `TicketService`
- `SeedConfig`
- plikami Docker Compose

### helpdesk-api/src/main/java/macieserafin/pl/helpdesk/HelpdeskApplication.java

Klasa startowa Spring Boot. Uruchamia kontekst aplikacji i rejestruje komponenty z pakietu `macieserafin.pl.helpdesk`.

Współpracuje z:

- całą konfiguracją Spring

### controller/AccessController.java

Udostępnia proste endpointy tekstowe `/`, `/user`, `/agent` i `/admin`. Służy jako lekki test dostępności i ról.

Współpracuje z:

- `SecurityConfig`

### controller/AuthController.java

Obsługuje rejestrację i odczyt aktualnego użytkownika. Samo logowanie i logout są realizowane przez Spring Security na ścieżkach skonfigurowanych w `SecurityConfig`.

Współpracuje z:

- `UserService`
- `SecurityConfig`
- `RegisterUserRequest`
- `UserResponse`

### controller/UserController.java

Udostępnia endpointy profilu bieżącego użytkownika, dashboard użytkownika oraz administracyjne zarządzanie kontami.

Współpracuje z:

- `UserService`
- `TicketService`
- DTO użytkowników i profilu

### controller/CategoryController.java

Obsługuje publiczną listę aktywnych kategorii dla zalogowanych użytkowników oraz administracyjny CRUD kategorii. Usunięcie kategorii jest soft delete przez `active=false`.

Współpracuje z:

- `CategoryService`
- DTO kategorii

### controller/TicketController.java

Główny kontroler domeny zgłoszeń. Udostępnia listy ticketów, tworzenie, edycję, statusy, priorytety, przypisanie, komentarze, historię i załączniki.

Współpracuje z:

- `TicketService`
- DTO ticketów, komentarzy, historii i załączników

### config/SecurityConfig.java

Konfiguruje Spring Security: role dla ścieżek, form login, logout, opcjonalny Basic Auth, CORS i odpowiedzi błędów `401` oraz `403`.

Współpracuje z:

- `UserService`
- `SecurityUserDetailsService`
- `ApiErrorResponse`
- frontendowym `httpClient.js`

### config/GlobalExceptionHandler.java

Normalizuje błędy walidacji, konflikty, błędy domenowe i nieoczekiwane wyjątki do `ApiErrorResponse`.

Współpracuje z:

- DTO requestów z Bean Validation
- kontrolerami REST

### config/SeedConfig.java

Tworzy dane demonstracyjne przy starcie aplikacji: konta, profile, kategorie i przykładowe tickety w różnych statusach. W produkcji potrafi wymagać niestandardowych haseł demo.

Współpracuje z:

- `UserService`
- `CategoryService`
- `TicketService`
- `application.properties`

### contract/ApiContract.java

Zbiór limitów długości pól używanych przez DTO i encje. Chroni przed rozjechaniem walidacji API z modelem bazy.

Współpracuje z:

- DTO requestów
- encjami JPA
- frontendowym `FIELD_LIMITS`

### service/SecurityUserDetailsService.java

Adapter Spring Security. Pobiera użytkownika po loginie albo emailu, mapuje role na `GrantedAuthority` i uwzględnia flagę `enabled`.

Współpracuje z:

- `UserService`
- `SecurityConfig`

### service/UserService.java

Logika użytkowników. Obsługuje rejestrację, tworzenie i aktualizację kont, profile, role, blokadę konta oraz regułę logowania emailem tylko dla czystej roli `USER`.

Współpracuje z:

- `UserRepository`
- `RoleRepository`
- `PasswordEncoder`
- DTO użytkowników

### service/CategoryService.java

Logika kategorii. Utrzymuje aktywne kategorie, pilnuje unikalności nazwy i realizuje soft delete.

Współpracuje z:

- `CategoryRepository`
- DTO kategorii

### service/TicketService.java

Najważniejszy serwis domenowy. Tworzy tickety, waliduje dostęp, filtruje listy, zmienia statusy i priorytety, przypisuje agenta, zapisuje komentarze, obsługuje załączniki, buduje dashboardy oraz zapisuje historię.

Współpracuje z:

- `TicketRepository`
- `UserRepository`
- `CategoryRepository`
- `CommentRepository`
- `AttachmentRepository`
- `TicketHistoryRepository`
- enumami statusu, priorytetu i historii

### model/entity/User.java

Encja konta. Przechowuje login, email, hash hasła, flagi bezpieczeństwa, flagę `enabled`, daty oraz relacje z profilem, rolami, ticketami, komentarzami, historią i załącznikami.

Współpracuje z:

- `Role`
- `UserProfile`
- `Ticket`
- `UserRepository`

### model/entity/UserProfile.java

Opcjonalny profil użytkownika. Trzyma imię, nazwisko, telefon, miasto, adres i kod pocztowy.

Współpracuje z:

- `User`
- DTO profilu

### model/entity/Role.java

Rola systemowa zapisana jako tekst. Relacja wiele do wielu z użytkownikami działa przez tabelę `user_roles`.

Współpracuje z:

- `User`
- `RoleRepository`
- `SecurityConfig`

### model/entity/Category.java

Słownik kategorii ticketów. Flaga `active` pozwala ukryć kategorię bez usuwania powiązań historycznych.

Współpracuje z:

- `Ticket`
- `CategoryRepository`
- `CategoryService`

### model/entity/Ticket.java

Centralna encja domeny. Łączy tytuł, opis, status, priorytet, autora, przypisanego agenta, kategorię, daty oraz kolekcje komentarzy, załączników i historii.

Współpracuje z:

- `TicketService`
- `TicketRepository`
- `Comment`
- `Attachment`
- `TicketHistory`

### model/entity/Comment.java

Komentarz do ticketu. Może być publiczny albo wewnętrzny. Komentarze wewnętrzne są widoczne dla staffu i wpływają na filtrowanie załączników dla użytkownika.

Współpracuje z:

- `Ticket`
- `User`
- `Attachment`
- `CommentRepository`

### model/entity/Attachment.java

Metadane załącznika. Wskazuje ticket, opcjonalny komentarz, autora uploadu, nazwę pliku, ścieżkę, content type, rozmiar i datę dodania.

Współpracuje z:

- `TicketService`
- `AttachmentRepository`
- katalogiem storage na dysku

### model/entity/TicketHistory.java

Dziennik zdarzeń ticketu. Zapisuje typ akcji, stare i nowe statusy, priorytety, przypisania oraz notatkę techniczną.

Współpracuje z:

- `TicketService`
- `TicketHistoryRepository`
- `HistoryTimeline.js`

### repository/*.java

Repozytoria JPA. Dostarczają operacje CRUD i wyszukiwania po loginie, emailu, nazwie kategorii, ticketach użytkownika, komentarzach, załącznikach i historii.

Współpracują z:

- serwisami domenowymi
- encjami JPA

### dto/*.java

Kontrakty API. Requesty zawierają walidację wejścia, a response kształt danych zwracanych do frontendu. `ApiErrorResponse` jest standardem odpowiedzi błędów.

Współpracują z:

- kontrolerami REST
- frontendowymi modułami `src/api`

## Frontend

### helpdesk-frontend/package.json

Definiuje projekt Vite i skrypty `dev`, `build` oraz `preview`.

Współpracuje z:

- `vite.config.js`
- `Dockerfile`

### helpdesk-frontend/vite.config.js

Konfiguracja dev servera. Ustawia port `5173` i proxy `/api` do backendu na `localhost:8080`.

Współpracuje z:

- `httpClient.js`
- lokalnym backendem

### helpdesk-frontend/Dockerfile

Buduje statyczny frontend i pakuje go do nginx. Używany przez lokalny i produkcyjny Docker Compose.

Współpracuje z:

- `nginx.conf`
- plikami Docker Compose

### helpdesk-frontend/nginx.conf

Serwuje statyczne pliki frontendu i przekazuje requesty `/api` do backendu w sieci Dockera. Dzięki temu sesja działa jako same origin z perspektywy przeglądarki.

Współpracuje z:

- `docker-compose.yml`
- `docker-compose.prod.yml`
- `SecurityConfig`

### src/main.js

Wejście do aplikacji przeglądarkowej. Uruchamia logikę z `src/app/app.js`.

Współpracuje z:

- `startApp`

### src/app/app.js

Główna pętla aplikacji. Odświeża sesję, dopasowuje trasę, sprawdza role, renderuje layout albo stronę publiczną i obsługuje błędy widoków.

Współpracuje z:

- `router.js`
- `routes.js`
- `authGuard.js`
- `ShellLayout`
- `ToastHost`

Kiedy jest używany:

- przy pierwszym wejściu do aplikacji,
- po każdej zmianie hasha,
- po przekierowaniu wynikającym z sesji lub roli.

### src/app/routes.js

Mapa tras SPA. Przypisuje widoki do ścieżek i określa wymagane role.

Współpracuje z:

- `router.js`
- `roleGuard.js`
- modułami z `pages`

### src/app/router.js

Router hashowy. Dopasowuje ścieżkę, obsługuje parametry takie jak `:id` i udostępnia nawigację bez przeładowania strony.

Współpracuje z:

- `routes.js`
- wszystkimi stronami

### src/api/httpClient.js

Wspólny klient HTTP. Dokleja bazowy URL, buduje query string, serializuje JSON, wysyła `credentials: 'include'`, obsługuje loader i normalizuje błędy do `ApiError`.

Współpracuje z:

- wszystkimi modułami `src/api`
- `uiStore.js`
- backendowym `ApiErrorResponse`

### src/api/authApi.js

Wywołania logowania, logoutu, rejestracji i odczytu aktualnego użytkownika.

Współpracuje z:

- `AuthController`
- `SecurityConfig`
- `authService.js`

### src/api/ticketApi.js

Wywołania użytkownika dla ticketów: tworzenie, lista własnych ticketów, szczegóły, edycja, status, komentarze i historia.

Współpracuje z:

- `TicketController`
- stronami użytkownika
- `TicketDetailsPage`

### src/api/agentApi.js

Wywołania agenta: kolejka, dashboard, lista priorytetów, przypisanie, status i priorytet.

Współpracuje z:

- `TicketController`
- `AgentDashboardPage`
- `TicketQueuePage`
- `AssignedTicketsPage`

### src/api/adminApi.js

Wywołania administracyjne dla użytkowników i ticketów.

Współpracuje z:

- `UserController`
- `TicketController`
- stronami admina

### src/api/categoryApi.js

Wywołania słownika kategorii. Obsługuje listę aktywnych kategorii i administracyjne zarządzanie kategoriami.

Współpracuje z:

- `CategoryController`
- formularzami ticketów i kategorii

### src/api/attachmentApi.js

Lista, upload, pobieranie i usuwanie załączników.

Współpracuje z:

- `TicketController`
- `attachmentUpload.js`
- `TicketDetailsPage`

### src/auth/authService.js

Warstwa sesji frontendu. Loguje, odświeża użytkownika, czyści stan po logoutcie, ustala rolę priorytetową i trasę startową.

Współpracuje z:

- `authApi.js`
- `authStore.js`
- `routes.js`

### src/auth/authGuard.js

Sprawdza, czy użytkownik ma aktywną sesję. Przy wejściu na chronioną trasę próbuje odświeżyć dane przez backend.

Współpracuje z:

- `authService.js`
- `app.js`

### src/auth/roleGuard.js

Weryfikuje, czy aktualny użytkownik ma jedną z ról wymaganych przez trasę.

Współpracuje z:

- `routes.js`
- `authService.js`

### src/state/authStore.js

Prosty magazyn aktualnego użytkownika w pamięci aplikacji. Nie przechowuje sesji ani tokena w storage przeglądarki.

Współpracuje z:

- `authService.js`
- `ShellLayout`

### src/state/uiStore.js

Licznik aktywnych requestów i subskrypcje UI. Używany do globalnego loadera.

Współpracuje z:

- `httpClient.js`
- `Navbar.js`

### src/pages/auth/LoginPage.js

Formularz logowania. Po sukcesie zapisuje użytkownika i przenosi na trasę właściwą dla roli.

Współpracuje z:

- `authService.js`
- `authApi.js`

### src/pages/auth/RegisterPage.js

Publiczna rejestracja konta użytkownika. Tworzy wyłącznie konto z rolą `USER`.

Współpracuje z:

- `authApi.js`
- `RegisterUserRequest`

### src/pages/user/UserDashboardPage.js

Dashboard użytkownika. Pokazuje metryki, najnowsze tickety, sprawy wymagające reakcji, aktywność i szybkie wejścia do formularzy.

Współpracuje z:

- `GET /api/users/me/dashboard`
- `categoryApi.js`
- komponentami ticketów

### src/pages/user/MyTicketsPage.js

Lista własnych ticketów z filtrami i paginacją.

Współpracuje z:

- `ticketApi.getMyTickets`
- `TicketFilters`
- `TicketTable`
- `Pagination`

### src/pages/user/CreateTicketPage.js

Widok tworzenia ticketu. Pobiera aktywne kategorie i renderuje `TicketForm`.

Współpracuje z:

- `categoryApi.getActiveCategories`
- `TicketForm`

### src/pages/user/ProfilePage.js

Edycja profilu zalogowanego użytkownika.

Współpracuje z:

- `userApi.js`
- `UserProfileRequest`

### src/pages/agent/AgentDashboardPage.js

Centrum pracy agenta. Pokazuje KPI, kolejkę przypisaną, zgłoszenia do przejęcia, odpowiedzi klienta, tickety ryzykowne i szybkie akcje.

Współpracuje z:

- `agentApi.getAgentDashboard`
- `agentApi.assignTicket`
- `TicketStatusForm`
- `TicketPriorityForm`

### src/pages/agent/TicketQueuePage.js

Pełna kolejka zgłoszeń dla agenta z filtrami i paginacją.

Współpracuje z:

- `agentApi.getTicketQueue`
- `TicketFilters`
- `TicketTable`

### src/pages/agent/AssignedTicketsPage.js

Widok przypisanych ticketów. Frontend filtruje listę agenta po aktualnym użytkowniku, ponieważ backend nie ma jeszcze dedykowanego endpointu `assigned-to-me`.

Współpracuje z:

- `agentApi.getTicketQueue`
- `authService.currentUser`

### src/pages/admin/AdminDashboardPage.js

Administracyjne podsumowanie. Pobiera użytkowników i pierwszą stronę ticketów, liczy podstawowe KPI po stronie frontendu i pokazuje najnowsze zgłoszenia.

Współpracuje z:

- `adminApi.getUsers`
- `adminApi.getTickets`

### src/pages/admin/UsersManagementPage.js

Zarządzanie użytkownikami. Obsługuje listę, wybór użytkownika, tworzenie, edycję i włączanie lub wyłączanie konta.

Współpracuje z:

- `adminApi`
- `UserTable`
- `UserForm`

### src/pages/admin/CategoriesManagementPage.js

Zarządzanie słownikiem kategorii. Obsługuje tworzenie, edycję, dezaktywację i aktywację kategorii.

Współpracuje z:

- `categoryApi`
- `CategoryTable`
- `CategoryForm`

### src/pages/admin/AllTicketsPage.js

Administracyjna lista wszystkich ticketów z filtrami i paginacją.

Współpracuje z:

- `adminApi.getTickets`
- `TicketFilters`
- `TicketTable`

### src/pages/shared/TicketDetailsPage.js

Widok szczegółów ticketu dostępny dla ról zgodnie z backendem. Łączy dane ticketa, edycję, status, priorytet, przypisanie, komentarze, historię i załączniki.

Współpracuje z:

- `ticketApi`
- `agentApi`
- `attachmentApi`
- `TicketEditForm`
- `CommentPanel`
- `HistoryTimeline`

Kiedy jest używany:

- po kliknięciu ticketa na liście,
- po wejściu z dashboardu użytkownika albo agenta,
- przy obsłudze komentarzy, załączników i historii jednej sprawy.

### src/components/tickets/*.js

Zestaw komponentów domeny ticketów: formularz, edycja, filtry, tabela, paginacja, status, priorytet, komentarze, historia i upload załączników.

Współpracują z:

- stronami user, agent, admin i shared
- modułami `src/api`

### src/components/users/*.js

Tabela i formularz użytkownika dla panelu administratora.

Współpracują z:

- `UsersManagementPage`
- `adminApi`

### src/components/categories/*.js

Tabela i formularz kategorii.

Współpracują z:

- `CategoriesManagementPage`
- `categoryApi`

### src/components/layout/*.js

Shell aplikacji, sidebar i navbar. Odpowiadają za układ po zalogowaniu, menu per rola, logout i globalny loader.

Współpracują z:

- `authService`
- `uiStore`
- `routes`

### src/components/common/*.js

Komponenty wspólne: badge, komunikaty, modal potwierdzenia, nagłówek strony i toasty.

Współpracują z:

- stronami i komponentami domenowymi

### src/utils/constants.js

Stałe frontendu: role, statusy, priorytety, limity pól, etykiety i limit rozmiaru załącznika. Muszą pozostać zgodne z backendowym kontraktem.

Współpracuje z:

- `ApiContract.java`
- enumami backendu
- formularzami i badge

### src/utils/dom.js

Pomocnicze funkcje DOM: selektory, tworzenie elementów z HTML, ustawianie zawartości, escapowanie tekstu i odczyt formularzy.

Współpracuje z:

- większością stron i komponentów

### src/styles/*.css

Style aplikacji podzielone na zmienne, layout, komponenty i widoki. Wspierają jeden spójny język UI bez biblioteki komponentów.

Współpracują z:

- klasami CSS generowanymi przez strony i komponenty

## Testy

### helpdesk-api/src/test/java/.../HelpdeskApplicationTests.java

Test ładowania kontekstu aplikacji.

### helpdesk-api/src/test/java/.../BasicAuthDisabledIntegrationTests.java

Sprawdza, że domyślny tryb nie akceptuje Basic Auth i wymusza model sesyjny.

### helpdesk-api/src/test/java/.../TicketFlowIntegrationTests.java

Testuje kluczowe przepływy domeny ticketów w środowisku integracyjnym.

### helpdesk-api/src/test/resources/application.properties

Konfiguracja testowa z H2 in memory i schematem tworzonym od zera na czas testów.

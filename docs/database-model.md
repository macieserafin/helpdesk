# Model Bazy Danych

Backend używa PostgreSQL jako bazy aplikacyjnej. Schemat jest tworzony i aktualizowany przez Hibernate na podstawie encji JPA. W repozytorium nie ma jeszcze migracji Flyway ani Liquibase.

## Konfiguracja

Silnik:

- PostgreSQL 16 w Docker Compose.
- H2 in memory w testach backendu.

Najważniejsze właściwości:

- `spring.datasource.url`: domyślnie `jdbc:postgresql://postgres:5432/helpdesk`.
- `spring.jpa.hibernate.ddl-auto`: domyślnie `update`.
- `spring.servlet.multipart.max-file-size`: `10MB`.
- `app.attachments.storage-dir`: domyślnie `uploads/attachments`.

Implikacje:

- schema nie jest wersjonowana SQLem,
- zmiany encji mogą zmienić bazę przy starcie aplikacji,
- produkcyjnie warto przejść na migracje przed większym użyciem systemu.

## Encje I Tabele

`users`

Konta użytkowników. Przechowuje login, email, hash hasła, flagę aktywności, flagi bezpieczeństwa i daty utworzenia oraz aktualizacji.

Ważne pola:

- `login_identifier`, unikalne, do 50 znaków,
- `email`, unikalne, do 100 znaków,
- `password_hash`,
- `enabled`,
- `email_verified`,
- `two_factor_enabled`,
- `notifications_enabled`.

`user_profiles`

Opcjonalne dane profilowe powiązane z `users` relacją jeden do jednego. Profil trzyma imię, nazwisko, telefon, miasto, adres i kod pocztowy.

`roles`

Role systemowe. Znane wartości w aplikacji to `USER`, `AGENT` i `ADMIN`.

`user_roles`

Tabela pośrednia relacji wiele do wielu między `users` i `roles`.

`categories`

Słownik kategorii zgłoszeń. Nazwa jest unikalna. `active=false` oznacza kategorię wyłączoną, ale nie usuwa istniejących ticketów.

`tickets`

Centralna tabela domeny. Trzyma tytuł, opis, status, priorytet, autora, przypisanego agenta, kategorię i daty procesu.

Ważne pola:

- `status`: `TicketStatus`,
- `priority`: `TicketPriority`,
- `created_by_id`,
- `assigned_to_id`,
- `category_id`,
- `created_at`,
- `updated_at`,
- `resolved_at`,
- `closed_at`.

`comments`

Komentarze do ticketów. Pole `internal` rozróżnia komentarz wewnętrzny od publicznego.

`attachments`

Metadane załączników. Plik binarny nie jest trzymany w bazie. Tabela przechowuje nazwę, ścieżkę, typ MIME, rozmiar, autora uploadu, ticket i opcjonalny komentarz.

`ticket_history`

Dziennik zmian ticketu. Zapisuje akcję, stare i nowe statusy, stare i nowe priorytety, stare i nowe przypisanie, notatkę oraz czas zmiany.

## Enumy Domenowe

`TicketStatus`:

- `OPEN`,
- `IN_PROGRESS`,
- `WAITING_FOR_USER`,
- `RESOLVED`,
- `CLOSED`,
- `REJECTED`,
- `CANCELLED`.

`TicketPriority`:

- `UNASSIGNED`,
- `LOW`,
- `MEDIUM`,
- `HIGH`,
- `CRITICAL`.

`TicketHistoryActionType`:

- `TICKET_CREATED`,
- `TICKET_UPDATED`,
- `STATUS_CHANGED`,
- `PRIORITY_CHANGED`,
- `ASSIGNED_CHANGED`,
- `COMMENT_ADDED`,
- `ATTACHMENT_ADDED`,
- `ATTACHMENT_DELETED`,
- `TICKET_RESOLVED`,
- `TICKET_CLOSED`.

Enumy są zapisywane jako stringi.

## Relacje

`User` do `UserProfile`

Relacja jeden do zera lub jednego. `User` ma cascade i orphan removal dla profilu.

`User` do `Role`

Relacja wiele do wielu przez `user_roles`.

`User` do `Ticket`

Użytkownik może być twórcą wielu ticketów. Osobna relacja reprezentuje agenta przypisanego do ticketów.

`Category` do `Ticket`

Kategoria ma wiele ticketów. Ticket wymaga kategorii.

`Ticket` do `Comment`

Ticket ma wiele komentarzy. Komentarze nie mają API usuwania.

`Ticket` do `Attachment`

Ticket ma wiele załączników. Załącznik zawsze należy do ticketu.

`Comment` do `Attachment`

Komentarz może mieć wiele załączników. Relacja jest opcjonalna po stronie załącznika, bo plik może być dodany bez komentarza.

`Ticket` do `TicketHistory`

Ticket ma wiele wpisów historii. Historia jest dopisywana, nie modyfikowana przez normalne operacje domenowe.

`User` do `TicketHistory`

Użytkownik występuje jako autor zmiany oraz opcjonalnie jako stary lub nowy przypisany agent.

## Główne Przepływy Danych

Tworzenie użytkownika:

1. Walidacja unikalności loginu i emaila.
2. Hashowanie hasła przez BCrypt.
3. Zapis `users`.
4. Przypisanie ról przez `user_roles`.
5. Opcjonalny zapis profilu.

Logowanie:

1. Wyszukanie użytkownika po loginie albo emailu.
2. Pobranie ról.
3. Sprawdzenie hasła i flagi `enabled`.
4. Utworzenie sesji HTTP poza bazą danych.

Tworzenie ticketu:

1. Sprawdzenie, czy użytkownik jest klientem.
2. Wyszukanie aktywnej kategorii.
3. Zapis `tickets`.
4. Zapis `ticket_history`.

Komentarz:

1. Sprawdzenie dostępu do ticketu.
2. Zapis `comments`.
3. Ewentualna zmiana statusu ticketu.
4. Zapis `ticket_history`.

Załącznik:

1. Walidacja pliku.
2. Zapis pliku na dysk.
3. Zapis metadanych w `attachments`.
4. Zapis historii.

## Relacje W Skrócie

- `users` do `user_profiles`: jeden do zera lub jednego.
- `users` do `roles`: wiele do wielu przez `user_roles`.
- `users` do `tickets`: jeden do wielu jako autor.
- `users` do `tickets`: jeden do wielu jako przypisany agent.
- `categories` do `tickets`: jeden do wielu.
- `tickets` do `comments`: jeden do wielu.
- `users` do `comments`: jeden do wielu jako autor.
- `tickets` do `attachments`: jeden do wielu.
- `comments` do `attachments`: jeden do wielu, opcjonalnie po stronie załącznika.
- `users` do `attachments`: jeden do wielu jako autor uploadu.
- `tickets` do `ticket_history`: jeden do wielu.
- `users` do `ticket_history`: jeden do wielu jako autor zmiany.

## Uwagi Wydajnościowe

Listy ticketów używają `JpaSpecificationExecutor` i filtrów po statusie, priorytecie, kategorii, agencie oraz datach. W encjach nie ma jawnych indeksów dla tych pól.

Dashboard agenta pobiera wszystkie tickety i liczy metryki w pamięci. To wystarcza dla małej demonstracyjnej bazy, ale nie dla dużej instalacji.

Przed większym użyciem systemu warto dodać:

- migracje,
- indeksy na `tickets.status`, `tickets.priority`, `tickets.created_at`, `tickets.created_by_id`, `tickets.assigned_to_id`, `tickets.category_id`,
- zapytania agregujące dashboardów po stronie bazy.

# Baza danych / Database

[Polski](#polski) | [English](#english)

## Polski

Środowisko Compose korzysta z PostgreSQL 16, a testy z bazy H2. Encje JPA opisują relacje, a repozytoria Spring Data JPA obsługują zapis i odczyt. Schemat produkcyjny jest wersjonowany przez Flyway.

<a id="pl-schemat-lokalny-i-produkcyjny"></a>

### Schemat lokalny i produkcyjny

| Tryb | Flyway | Hibernate |
| --- | --- | --- |
| Lokalny, domyślna konfiguracja | Wyłączony | `ddl-auto=update` |
| Profil `prod` | Włączony | `ddl-auto=validate` |

Pierwsza migracja to `helpdesk-api/src/main/resources/db/migration/V1__initial_schema.sql`. Dotyczy nowej bazy. Nie włączaj baseline na istniejącej bazie bez porównania schematu i planu przeniesienia. Zasady przenoszenia danych i tworzenia kopii zapasowych opisuje [instrukcja wdrożenia](../operations/deployment.md#polski).

Migrację trzeba sprawdzić również na PostgreSQL. W CI odbywa się to przez uruchomienie pełnego zestawu kontenerów.

<a id="pl-tabele"></a>

### Tabele

| Tabela | Dane i powiązania |
| --- | --- |
| `users` | Unikalne login i e-mail, skrót hasła, flaga enabled, daty i flagi bezpieczeństwa |
| `user_profiles` | Opcjonalny profil 1:1: imię, nazwisko, telefon, miasto, adres i kod pocztowy |
| `roles`, `user_roles` | Role i relacja wiele-do-wielu z kontami |
| `categories` | Nazwa, opis, active; dezaktywacja zachowuje zgłoszenia |
| `tickets` | Tytuł, opis, status, priorytet, autor, opcjonalny agent, kategoria i daty |
| `comments` | Zgłoszenie, autor, treść, flaga internal i czas |
| `attachments` | Zgłoszenie, opcjonalny komentarz, osoba, która dodała plik, nazwa, ścieżka, MIME, rozmiar i czas |
| `ticket_history` | Zgłoszenie, aktor, typ zdarzenia, poprzednie i nowe wartości, notatka i czas |

Flagi `email_verified`, `two_factor_enabled` i `notifications_enabled` nie oznaczają, że istnieją pełne procesy weryfikacji, 2FA czy powiadomień.

<a id="pl-relacje"></a>

### Relacje

- Konto może być autorem wielu zgłoszeń i osobno agentem przypisanym do wielu spraw.
- Każde zgłoszenie ma kategorię, komentarze, załączniki i dopisywaną historię.
- Komentarz ma autora i może mieć załączniki.
- Załącznik zawsze należy do zgłoszenia; relacja z komentarzem jest opcjonalna.
- Historia wskazuje aktora, a przy zmianie przypisania także starego/nowego agenta.
- Zapis profilu jest kaskadowy; usunięcie powiązania z kontem usuwa osierocony profil (orphan removal).

Statusy, priorytety i typy historii są zapisane jako tekstowe enumy; ich wartości opisują [reguły zgłoszeń](../reference/tickets.md#polski). Limity pól są w [kontrakcie API](../reference/api.md#pl-limity-i-walidacja).

<a id="pl-załączniki-i-sesje"></a>

### Załączniki i sesje

W bazie są tylko metadane plików. Pliki są przechowywane w `APP_ATTACHMENTS_STORAGE_DIR`, lokalnie `uploads/attachments`, w Compose `/app/uploads/attachments` w wolumenie `backend_uploads`.

Dodanie załącznika obejmuje zapis pliku, metadanych i historii. Kopia zapasowa musi zawierać zarówno bazę, jak i pliki. Sam zrzut bazy nie wystarczy do odtworzenia załączników.

Sesja HTTP jest zarządzana przez serwer aplikacji, poza tabelami domenowymi. Hasła kont są zabezpieczane za pomocą BCrypt.

<a id="pl-wydajność"></a>

### Wydajność

`TicketRepository` używa `JpaSpecificationExecutor` do filtrowania list. Panel agenta pobiera zgłoszenia i liczy metryki w pamięci.

Przy większej bazie potrzebne są zapytania agregujące oraz indeksy dla statusów, priorytetów, dat, kategorii, autora, agenta i powiązań po ticket_id. Plan prac jest w [roadmapie](../roadmap.md#polski).

[Przejdź do wersji angielskiej](#english)

---

## English

[Wróć do wersji polskiej](#polski)

Compose uses PostgreSQL 16, while tests use H2. JPA entities describe relationships, and Spring Data JPA repositories handle reads and writes. Flyway versions the production schema.

<a id="en-local-and-production-schemas"></a>

### Local and production schemas

| Mode | Flyway | Hibernate |
| --- | --- | --- |
| Local, default settings | Disabled | `ddl-auto=update` |
| `prod` profile | Enabled | `ddl-auto=validate` |

The first migration is `helpdesk-api/src/main/resources/db/migration/V1__initial_schema.sql`. It targets a new database. Do not enable baseline on an existing database without comparing schemas and planning the transition. See [deployment](../operations/deployment.md#english) for data migration and backups.

Migrations must also be checked on PostgreSQL. CI does this by running the full container stack.

<a id="en-tables"></a>

### Tables

| Table | Data and relationships |
| --- | --- |
| `users` | Unique login and email, password hash, enabled, dates and security flags |
| `user_profiles` | Optional 1:1 profile: first name, last name, phone, city, address and postal code |
| `roles`, `user_roles` | Roles and many-to-many account relationships |
| `categories` | Name, description and active; deactivation preserves tickets |
| `tickets` | Title, description, status, priority, owner, optional agent, category and dates |
| `comments` | Ticket, author, content, internal flag and time |
| `attachments` | Ticket, optional comment, uploader, name, path, MIME, size and time |
| `ticket_history` | Ticket, actor, event type, old/new values, note and time |

The `email_verified`, `two_factor_enabled` and `notifications_enabled` flags do not mean that complete verification, 2FA or notification flows are implemented.

<a id="en-relationships"></a>

### Relationships

- An account can own many tickets and separately be the assigned agent on many tickets.
- Each ticket has a category, comments, attachments and append-only history.
- A comment has an author and may have attachments.
- An attachment always belongs to a ticket; its comment relationship is optional.
- History identifies the actor and, for assignment changes, the old/new agent.
- Profile persistence cascades from the account; removing the relationship removes the orphaned profile.

Statuses, priorities and history types are stored as string enums; values are listed in the [ticket rules](../reference/tickets.md#english). Field limits are in the [API contract](../reference/api.md#en-limits-and-validation).

<a id="en-attachments-and-sessions"></a>

### Attachments and sessions

Only file metadata is stored in the database. Files live in `APP_ATTACHMENTS_STORAGE_DIR`: locally `uploads/attachments`, or `/app/uploads/attachments` in Compose's `backend_uploads` volume.

Adding an attachment saves the file, metadata and history. Backups must include both the database and files. A database dump alone cannot restore attachments.

HTTP sessions are managed by the application server, outside the domain tables. Account passwords are hashed with BCrypt.

<a id="en-performance"></a>

### Performance

`TicketRepository` uses `JpaSpecificationExecutor` for list filtering. The agent dashboard loads tickets and calculates metrics in memory.

Larger databases need aggregation queries and indexes for status, priority, dates, category, owner, agent and ticket_id relationships. See the [roadmap](../roadmap.md#english).

[Back to the Polish version](#polski)

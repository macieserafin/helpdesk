# Dane demonstracyjne / Demo data

[Polski](#polski) | [English](#english)

## Polski

Mechanizm tworzenia danych początkowych (seed) dodaje konta, kategorie i przykładowe zgłoszenia podczas uruchamiania backendu. Jest domyślnie włączony w lokalnym Compose, a wyłączony przy samodzielnym uruchamianiu backendu. Loginy i hasła są w [README](../../README.md#pl-uruchomienie).

<a id="pl-przełączniki"></a>

### Przełączniki

| Zmienna | Działanie |
| --- | --- |
| `APP_DEMO_SEED_ENABLED` | Główny przełącznik; false wyłącza tworzenie danych |
| `APP_DEMO_SEED_ACCOUNTS_ENABLED` | Tworzenie kont i profili |
| `APP_DEMO_SEED_CATEGORIES_ENABLED` | Tworzenie kategorii |
| `APP_DEMO_SEED_TICKETS_ENABLED` | Tworzenie przykładowych zgłoszeń |
| `APP_DEMO_SEED_ADMIN_ONLY` | Tworzenie tylko admina, bez kont user/agent i zgłoszeń demo |
| `APP_DEMO_SEED_REQUIRE_CUSTOM_PASSWORDS` | Odrzucanie domyślnych haseł oraz haseł krótszych niż 12 znaków |

Hasła ustawisz za pomocą `APP_DEMO_SEED_USER_PASSWORD`, `APP_DEMO_SEED_AGENT_PASSWORD` i `APP_DEMO_SEED_ADMIN_PASSWORD`. `APP_DEMO_SEED_ADMIN_EMAIL` zmienia e-mail tworzonego admina.

Dane są tworzone kolejno: najpierw konta i profile, potem kategorie, a na końcu zgłoszenia. Zgłoszenia demo wymagają kont `user` i `agent` oraz aktywnych kategorii. Muszą one już istnieć lub zostać utworzone przez mechanizm danych początkowych.

<a id="pl-co-powstaje"></a>

### Co powstaje

Kategorie: Konto, Uprawnienia, Załączniki, Aplikacja i Inne. Konta user i agent mają adresy `user@example.com` i `agent@example.com`; admin domyślnie `admin@example.com`. Konto user może logować się też e-mailem; agent i admin używają loginu.

| Przykładowa sprawa | Status | Co można sprawdzić |
| --- | --- | --- |
| Nie mogę zalogować się do panelu | `OPEN` | Przejęcie zgłoszenia |
| Brak dostępu do raportów | `IN_PROGRESS` | Przypisanie i komentarz wewnętrzny |
| Nie mogę dodać załącznika | `WAITING_FOR_USER` | Odpowiedź użytkownika |
| Formularz profilu nie zapisuje zmian | `RESOLVED` | Zamknięcie rozwiązanej sprawy |
| Reset hasła do konta testowego | `CLOSED` | Sprawa zakończona |
| Prośba o dostęp do panelu administracyjnego | `REJECTED` | Odrzucenie |
| Duplikat zgłoszenia o problemie z logowaniem | `CANCELLED` | Anulowanie |

Przy kolejnym uruchomieniu mechanizm wyszukuje istniejące konta po loginie, kategorie po nazwie, a zgłoszenia po tytule. Nie resetuje danych ani haseł. Wyłączenie przełącznika również nie usuwa wcześniejszych danych. Reset opisują przewodniki [Docker](docker.md#pl-reset-środowiska) i [bazy dla IDE](local.md#pl-zatrzymanie-i-reset-bazy).

<a id="pl-testowanie-ról"></a>

### Testowanie ról

Użyj trzech osobnych profili przeglądarki, po jednym na rolę. Każdy ma własne cookie sesji. Sprawdź logowanie, odświeżenie strony, dostęp do widoku innej roli i wylogowanie. Następnie sprawdź cały przebieg zgłoszenia, od utworzenia do zamknięcia, zgodnie z [reguł zgłoszeń](../reference/tickets.md#polski).

W środowisku produkcyjnym aktualny zestaw Compose wymusza tworzenie tylko konta administratora i użycie własnego hasła. Szczegóły są w [instrukcji wdrożenia](../operations/deployment.md#polski).

[Przejdź do wersji angielskiej](#english)

---

## English

[Wróć do wersji polskiej](#polski)

The initial data mechanism (seed) adds accounts, categories and sample tickets when the backend starts. It is enabled by default in local Compose and disabled when the backend runs separately. Logins and passwords are in the [README](../../README.md#en-getting-started).

<a id="en-switches"></a>

### Switches

| Variable | Purpose |
| --- | --- |
| `APP_DEMO_SEED_ENABLED` | Main switch; false disables all seeding |
| `APP_DEMO_SEED_ACCOUNTS_ENABLED` | Creates accounts and profiles |
| `APP_DEMO_SEED_CATEGORIES_ENABLED` | Creates categories |
| `APP_DEMO_SEED_TICKETS_ENABLED` | Creates sample tickets |
| `APP_DEMO_SEED_ADMIN_ONLY` | Creates only admin, without user/agent accounts or demo tickets |
| `APP_DEMO_SEED_REQUIRE_CUSTOM_PASSWORDS` | Rejects default passwords and passwords shorter than 12 characters |

Set passwords with `APP_DEMO_SEED_USER_PASSWORD`, `APP_DEMO_SEED_AGENT_PASSWORD` and `APP_DEMO_SEED_ADMIN_PASSWORD`. `APP_DEMO_SEED_ADMIN_EMAIL` changes the new admin's email.

Accounts and profiles are created first, then categories, then tickets. Demo tickets need the `user` and `agent` accounts and active categories. These must already exist or be created by the seed.

<a id="en-created-data"></a>

### Created data

Categories: Konto, Uprawnienia, Załączniki, Aplikacja and Inne. The user and agent accounts have `user@example.com` and `agent@example.com` addresses; admin defaults to `admin@example.com`. The user account can also log in by email; agent and admin use their login.

The following titles are stored in Polish:

| Sample ticket | Status | What to test |
| --- | --- | --- |
| Nie mogę zalogować się do panelu | `OPEN` | Taking ownership |
| Brak dostępu do raportów | `IN_PROGRESS` | Assignment and internal comments |
| Nie mogę dodać załącznika | `WAITING_FOR_USER` | A user reply |
| Formularz profilu nie zapisuje zmian | `RESOLVED` | Closing a resolved ticket |
| Reset hasła do konta testowego | `CLOSED` | A completed case |
| Prośba o dostęp do panelu administracyjnego | `REJECTED` | Rejection |
| Duplikat zgłoszenia o problemie z logowaniem | `CANCELLED` | Cancellation |

On subsequent starts, seeding finds existing accounts by login, categories by name and tickets by title. It does not reset data or passwords. Turning it off does not delete existing data. See the [Docker reset](docker.md#en-resetting-the-environment) and [IDE database reset](local.md#en-stopping-and-resetting-the-database) guides.

<a id="en-testing-roles"></a>

### Testing roles

Use three separate browser profiles, one for each role. Each has its own session cookies. Check login, page refresh, access to another role's view and logout. Then test a ticket from creation to closure using the [workflow rules](../reference/tickets.md#english).

The current production Compose stack enforces admin-only seeding and a custom password. See the [deployment guide](../operations/deployment.md#english).

[Back to the Polish version](#polski)

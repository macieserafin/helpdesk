# Role i uprawnienia / Roles and permissions

[Polski](#polski) | [English](#english)

## Polski

Backend sprawdza dostęp do ścieżki w `SecurityConfig` oraz dostęp do konkretnego zasobu w serwisach. Ukrywanie przycisków w interfejsie nie zastępuje tych sprawdzeń.

| Rola | Odpowiedzialność |
| --- | --- |
| `USER` | Własne zgłoszenia i profil |
| `AGENT` | Kolejka zgłoszeń i obsługa spraw |
| `ADMIN` | Konta, kategorie i wgląd we wszystkie sprawy |

<a id="pl-dostęp-do-zgłoszeń"></a>

### Dostęp do zgłoszeń

| Operacja | USER | AGENT | ADMIN |
| --- | --- | --- | --- |
| Utworzenie zgłoszenia | Tak, bez ról AGENT i ADMIN | Nie | Nie |
| Odczyt zgłoszenia, historii i plików | Własne | Wszystkie | Wszystkie |
| Edycja tytułu, opisu i kategorii | Własne, jeśli nie są zakończone | Nie | Wszystkie, jeśli nie są zakończone |
| Przypisanie do siebie | Nie | Tak, jeśli sprawa nie jest zakończona | Tylko z dodatkową rolą AGENT |
| Zmiana priorytetu | Nie | Tak | Tak |
| Zmiana statusu | Własne: CANCELLED lub CLOSED | Poza CLOSED | Wszystkie dozwolone przejścia |
| Komentarz publiczny i dodanie pliku | Własne | Wszystkie | Wszystkie |
| Komentarz wewnętrzny i jego pliki | Brak dostępu | Tak | Tak |
| Usunięcie załącznika | Własny plik | Własny plik | Dowolny |

Zmiany statusu zawsze muszą spełniać [macierz przejść](tickets.md#pl-statusy-i-dozwolone-przejścia). Użytkownik, który nie ma ani roli AGENT, ani ADMIN, nie ma dostępu do cudzej sprawy.

<a id="pl-konta-i-kategorie"></a>

### Konta i kategorie

Każdy zalogowany użytkownik może odczytać i zmienić własny profil oraz pobrać aktywne kategorie. Tylko administrator zarządza kontami, rolami, aktywnością kont i kategoriami. Dezaktywacja kategorii zachowuje historyczne powiązania.

Role są zapisane w `roles` i przypisane przez `user_roles`. API używa nazw bez prefiksu `ROLE_`; Spring Security dodaje go wewnętrznie. `UserService` usuwa ten prefiks, normalizuje wielkość liter i dopuszcza wyłącznie USER, AGENT i ADMIN.

Konto może mieć kilka ról. „Klient” w regułach biznesowych oznacza USER bez AGENT i ADMIN. Role zespołu obsługi nie dziedziczą automatycznie roli USER ani AGENT.

<a id="pl-ścieżki-i-role"></a>

### Ścieżki i role

Pełny katalog endpointów z wymaganym dostępem jest w [API](api.md#polski). Najważniejsze wyjątki:

- `POST /api/tickets` wymaga USER i dodatkowo sprawdza, czy konto jest klientem.
- `PATCH /api/tickets/{id}` dopuszcza USER lub ADMIN, a serwis sprawdza właściciela i status.
- `PATCH /api/tickets/{id}/status` wymaga USER; administrator używa ścieżki przeznaczonej dla zespołu obsługi.
- Przypisanie do siebie, kolejka i panel agenta wymagają AGENT.
- Status, priorytet i lista priorytetów zespołu obsługi dopuszczają AGENT lub ADMIN.

<a id="pl-sesja-i-frontend"></a>

### Sesja i frontend

Wyłączenie konta lub zmiana ról odbiera dostęp istniejącej sesji przy następnym żądaniu. Sposób logowania, CSRF i wyjątek dla otwartego SSE opisują [sesje](security.md#polski).

Frontend wybiera stronę startową w kolejności ADMIN, AGENT, USER. Mapę tras znajdziesz w [architekturze frontendu](../architecture/frontend.md#pl-trasy). Ostateczną decyzję o dostępie podejmuje backend.

[Przejdź do wersji angielskiej](#english)

---

## English

[Wróć do wersji polskiej](#polski)

The backend checks path access in `SecurityConfig` and resource access in services. Hiding buttons in the UI does not replace these checks.

| Role | Responsibility |
| --- | --- |
| `USER` | Own tickets and profile |
| `AGENT` | Ticket queue and case handling |
| `ADMIN` | Accounts, categories and access to all tickets |

<a id="en-ticket-access"></a>

### Ticket access

| Operation | USER | AGENT | ADMIN |
| --- | --- | --- | --- |
| Create a ticket | Yes, without AGENT or ADMIN | No | No |
| Read tickets, history and files | Own | All | All |
| Edit title, description and category | Own, unless final | No | All, unless final |
| Assign to self | No | Yes, unless final | Only with an additional AGENT role |
| Change priority | No | Yes | Yes |
| Change status | Own: CANCELLED or CLOSED | Except CLOSED | All allowed transitions |
| Public comment and file upload | Own tickets | All | All |
| Internal comments and their files | No access | Yes | Yes |
| Delete an attachment | Own upload | Own upload | Any |

Status changes must follow the [transition table](tickets.md#en-statuses-and-allowed-transitions). Users without AGENT or ADMIN cannot access another user's ticket.

<a id="en-accounts-and-categories"></a>

### Accounts and categories

Any authenticated user can read and update their own profile and fetch active categories. Only administrators manage accounts, roles, account activity and categories. Deactivating a category preserves historical relationships.

Roles are stored in `roles` and assigned through `user_roles`. The API uses names without `ROLE_`; Spring Security uses that prefix internally. `UserService` removes the prefix, normalizes case and permits only USER, AGENT and ADMIN.

An account can have several roles. In business rules, a customer means USER without AGENT or ADMIN. Staff roles do not automatically inherit USER or AGENT.

<a id="en-paths-and-roles"></a>

### Paths and roles

The [API](api.md#english) lists every endpoint with its access requirements. Key exceptions:

- `POST /api/tickets` requires USER and also checks that the account is a customer.
- `PATCH /api/tickets/{id}` allows USER or ADMIN; the service checks ownership and status.
- `PATCH /api/tickets/{id}/status` requires USER; administrators use the staff path.
- Self-assignment, the agent queue and agent dashboard require AGENT.
- Staff status changes, priority changes and assignable priorities allow AGENT or ADMIN.

<a id="en-sessions-and-frontend"></a>

### Sessions and frontend

Disabling an account or changing its roles revokes an existing session's access on the next request. See [sessions](security.md#english) for authentication, CSRF and the open SSE exception.

The frontend chooses a home page in ADMIN, AGENT, USER order. Routes are listed in the [frontend guide](../architecture/frontend.md#en-routes). The backend makes the final access decision.

[Back to the Polish version](#polski)

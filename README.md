# Helpdesk

[Polski](#polski) | [English](#english)

## Polski

Helpdesk służy do obsługi zgłoszeń serwisowych. Użytkownik zgłasza problem, agent zajmuje się jego rozwiązaniem, a administrator zarządza kontami i kategoriami. Aplikacja udostępnia panele dla każdej z tych ról oraz obsługuje komentarze publiczne i wewnętrzne, załączniki i historię zmian.

<a id="pl-uruchomienie"></a>

### Uruchomienie

Zainstaluj i uruchom Dockera z obsługą Docker Compose, np. Docker Desktop. Następnie wykonaj poniższe polecenie w katalogu głównym repozytorium:

```bash
docker compose up --build -d
```

Pierwszy start może potrwać kilka minut. Otwórz [aplikację](http://localhost:3000). Backend jest dostępny pod `http://localhost:8080`, a jego gotowość sprawdzisz przez [GET /api/health](http://localhost:8080/api/health).

Przy pierwszym uruchomieniu lokalnego środowiska Compose tworzy konta demonstracyjne:

| Login | Hasło | Rola |
| --- | --- | --- |
| `user` | `user123` | Własne zgłoszenia |
| `agent` | `agent123` | Obsługa kolejki zgłoszeń |
| `admin` | `admin123` | Użytkownicy, kategorie i wszystkie zgłoszenia |

Te hasła służą wyłącznie do pracy lokalnej. Jeśli masz własny `.env`, adresy i konta mogą się różnić.

Aby zatrzymać aplikację i zachować dane, wykonaj:

```bash
docker compose down
```

Jeśli aplikacja nie odpowiada, sprawdź `docker compose ps` i `docker compose logs backend`. Zmianę portów, logi i reset danych opisuje [przewodnik Docker](docs/development/docker.md#polski).

<a id="pl-praca-nad-kodem"></a>

### Praca nad kodem

Backend korzysta z Javy 17, Spring Boot i PostgreSQL 16. Frontend jest napisany w JavaScript i budowany za pomocą Vite. Logowanie działa przez sesję HTTP, a załączniki są zapisywane na dysku backendu.

Jeśli chcesz uruchamiać backend w IDE i na bieżąco oglądać zmiany we frontendzie, skorzystaj z [instrukcji pracy lokalnej](docs/development/local.md#polski). Znajdziesz w niej wymagania, konfigurację bazy oraz polecenia uruchamiania testów.

<a id="pl-gdzie-szukać-dalej"></a>

### Gdzie szukać dalej

| Potrzebuję... | Dokument |
| --- | --- |
| Znaleźć konkretny temat | [Spis dokumentacji](docs/README.md#polski) |
| Zrozumieć statusy i obsługę zgłoszeń | [Przebieg zgłoszenia](docs/reference/tickets.md#polski) |
| Sprawdzić dostęp danej roli | [Role i uprawnienia](docs/reference/roles.md#polski) |
| Znaleźć kod do zmiany | [Architektura i mapa plików](docs/architecture/overview.md#polski) |
| Wywołać API | [Kontrakt i endpointy](docs/reference/api.md#polski) |
| Wdrożyć na VPS | [Wdrożenie, kopie zapasowe i przywracanie wersji](docs/operations/deployment.md#polski) |
| Sprawdzić plany rozwoju | [Roadmapa](docs/roadmap.md#polski) |

[Przejdź do wersji angielskiej](#english)

---

## English

[Wróć do wersji polskiej](#polski)

Helpdesk manages support tickets. Users report issues, agents work on resolving them, and administrators manage accounts and categories. Each role has its own dashboard. The application supports public and internal comments, attachments and change history.

<a id="en-getting-started"></a>

### Getting started

Install and start Docker with Docker Compose support, such as Docker Desktop. Then run this command from the repository root:

```bash
docker compose up --build -d
```

The first start may take a few minutes. Open the [application](http://localhost:3000). The backend is available at `http://localhost:8080`; check its readiness through [GET /api/health](http://localhost:8080/api/health).

The local Compose stack creates demo accounts on its first start:

| Login | Password | Role |
| --- | --- | --- |
| `user` | `user123` | Own tickets |
| `agent` | `agent123` | Ticket queue |
| `admin` | `admin123` | Users, categories and all tickets |

These passwords are for local development only. A custom `.env` may change the addresses and account credentials.

To stop the application and keep its data:

```bash
docker compose down
```

If the application does not respond, check `docker compose ps` and `docker compose logs backend`. The [Docker guide](docs/development/docker.md#english) covers ports, logs and data resets.

<a id="en-working-on-the-code"></a>

### Working on the code

The backend uses Java 17, Spring Boot and PostgreSQL 16. The frontend uses JavaScript and Vite. Authentication uses HTTP sessions, and attachment files are stored on the backend's disk.

To run the backend in an IDE and see frontend changes as you work, follow the [local development guide](docs/development/local.md#english). It includes requirements, database configuration and test commands.

<a id="en-where-to-look-next"></a>

### Where to look next

| I need to... | Document |
| --- | --- |
| Find a topic | [Documentation index](docs/README.md#english) |
| Understand ticket statuses and handling | [Ticket workflow](docs/reference/tickets.md#english) |
| Check a role's access | [Roles and permissions](docs/reference/roles.md#english) |
| Find the code to change | [Architecture and file map](docs/architecture/overview.md#english) |
| Call the API | [API contract and endpoints](docs/reference/api.md#english) |
| Deploy to a VPS | [Deployment, backups and rollback](docs/operations/deployment.md#english) |
| Check development plans | [Roadmap](docs/roadmap.md#english) |

[Back to the Polish version](#polski)

# Dokumentacja Helpdesk / Helpdesk documentation

[Polski](#polski) | [English](#english)

## Polski

Wybierz temat według tego, co chcesz zrobić. Najkrótsza instrukcja uruchomienia jest w [głównym README](../README.md#polski).

<a id="pl-uruchomienie-i-praca-lokalna"></a>

### Uruchomienie i praca lokalna

| Dokument | Co znajdziesz |
| --- | --- |
| [Docker](development/docker.md#polski) | Ustawienia Compose, porty, logi, wolumeny i reset danych |
| [Backend i frontend osobno](development/local.md#polski) | Baza dla IDE, zmienne środowiskowe, testy i budowanie aplikacji |
| [Dane demonstracyjne](development/demo-data.md#polski) | Tworzenie danych początkowych, przykładowe sprawy i testowanie ról |

<a id="pl-zasady-działania-i-api"></a>

### Zasady działania i API

| Dokument | Co znajdziesz |
| --- | --- |
| [Przebieg zgłoszenia](reference/tickets.md#polski) | Statusy, przejścia, priorytety, komentarze i historia |
| [Role i uprawnienia](reference/roles.md#polski) | Kto może zobaczyć, zmienić lub usunąć dane |
| [API](reference/api.md#polski) | Endpointy, format danych, limity, paginacja i błędy |
| [Logowanie i sesje](reference/security.md#polski) | Cookie, CSRF, CORS i unieważnianie dostępu |

<a id="pl-architektura-i-kod"></a>

### Architektura i kod

| Dokument | Co znajdziesz |
| --- | --- |
| [Architektura i mapa plików](architecture/overview.md#polski) | Podział odpowiedzialności i miejsca do zmiany kodu |
| [Frontend](architecture/frontend.md#polski) | Routing, stan, widoki, komponenty i komunikacja z API |
| [Baza danych](architecture/database.md#polski) | Tabele, relacje, migracje i zapis załączników |

<a id="pl-wdrożenie-i-utrzymanie"></a>

### Wdrożenie i utrzymanie

| Dokument | Co znajdziesz |
| --- | --- |
| [VPS z Nginx i GitHub Actions](operations/deployment.md#polski) | Przygotowanie, publikacja obrazów, kopie zapasowe i przywracanie wersji |
| [Starszy wariant Caddy](operations/caddy.md#polski) | Osobny sposób wdrożenia z lokalnym budowaniem obrazów |
| [Audyt z 13.10.2026](operations/security-audit.md#polski) | Wyniki przeglądu i pozostałe ryzyka |
| [Roadmapa](roadmap.md#polski) | Priorytety i planowane funkcje |

[Przejdź do wersji angielskiej](#english)

---

## English

[Wróć do wersji polskiej](#polski)

Choose a topic based on what you want to do. The shortest startup guide is in the [main README](../README.md#english).

<a id="en-getting-started-and-local-development"></a>

### Getting started and local development

| Document | What it covers |
| --- | --- |
| [Docker](development/docker.md#english) | Compose settings, ports, logs, volumes and data resets |
| [Backend and frontend separately](development/local.md#english) | Database for an IDE, environment variables, tests and builds |
| [Demo data](development/demo-data.md#english) | Initial data, sample tickets and role testing |

<a id="en-application-rules-and-api"></a>

### Application rules and API

| Document | What it covers |
| --- | --- |
| [Ticket workflow](reference/tickets.md#english) | Statuses, transitions, priorities, comments and history |
| [Roles and permissions](reference/roles.md#english) | Who can view, change or delete data |
| [API](reference/api.md#english) | Endpoints, data formats, limits, pagination and errors |
| [Authentication and sessions](reference/security.md#english) | Cookies, CSRF, CORS and access revocation |

<a id="en-architecture-and-code"></a>

### Architecture and code

| Document | What it covers |
| --- | --- |
| [Architecture and file map](architecture/overview.md#english) | Responsibilities and where to change the code |
| [Frontend](architecture/frontend.md#english) | Routing, state, views, components and API calls |
| [Database](architecture/database.md#english) | Tables, relationships, migrations and attachment storage |

<a id="en-deployment-and-maintenance"></a>

### Deployment and maintenance

| Document | What it covers |
| --- | --- |
| [VPS with Nginx and GitHub Actions](operations/deployment.md#english) | Setup, image publishing, backups and rollback |
| [Legacy Caddy setup](operations/caddy.md#english) | An alternative deployment with locally built images |
| [Audit of 13 October 2026](operations/security-audit.md#english) | Review findings and remaining risks |
| [Roadmap](roadmap.md#english) | Priorities and planned features |

[Back to the Polish version](#polski)

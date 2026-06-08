# Roadmap projektu Helpdesk

Stan po zmianach: 2026-06-08.

## Aktualny status

Projekt ma juz dzialajacy rdzen aplikacji helpdeskowej: uzytkownicy, role, profile, tickety, statusy, priorytety, kategorie, komentarze, historia zmian i zalaczniki. Po ostatnich zmianach doszly elementy, ktore wczesniej byly glownymi brakami P0/P1:

- publiczna rejestracja uzytkownika we frontendzie przez widok `/register`,
- przejscie frontendu z Basic Auth w `sessionStorage` na logowanie sesyjne przez `POST /api/auth/login`, `GET /api/auth/me` i `POST /api/auth/logout`,
- dokumentacja decyzji auth/session w `docs/auth-session-security.md`,
- ujednolicenie identyfikatora logowania jako `loginIdentifier`,
- backendowy endpoint `GET /api/users/me/dashboard`,
- przebudowany dashboard uzytkownika jako portal klienta z KPI, statusem spraw, ostatnimi zgloszeniami, szybka pomoca, aktywnoscia i stanem pustym,
- poprawki polskich znakow w kluczowych etykietach frontendu i seedowanych kategoriach.

Najblizszy rozwoj powinien teraz skupic sie na dokumentacji uruchomienia, dopracowaniu dashboardow agenta i admina, rozbudowie funkcji szczegolow ticketa oraz przygotowaniu fundamentu pod realtime chat.

## Zalozenia dla dashboardow po researchu

Dashboardy w systemie helpdesk powinny byc rozne dla kazdej roli, bo kazda rola ma inny cel:

- uzytkownik chce szybko zglosic problem, sprawdzic status, odpowiedziec supportowi i zobaczyc, czy musi cos zrobic,
- agent chce widziec kolejke pracy, priorytety, tickety zagrozone opoznieniem, nowe odpowiedzi od uzytkownikow i szybkie akcje,
- admin chce widziec zdrowie calego systemu: backlog, created vs resolved, czas pierwszej odpowiedzi, czas rozwiazania, SLA, obciazenie agentow, kategorie i aktywnosc.

Wnioski z analizy dobrych praktyk:

- dashboard nie powinien miec dziesiatek przypadkowych metryk; najpierw trzeba pokazac rzeczy, na ktore uzytkownik moze zareagowac,
- metryki czasowe powinny rozdzielac pierwsza odpowiedz od czasu rozwiazania,
- backlog powinien byc widoczny po statusie i w czasie,
- warto wykrywac tickety "stuck", czyli stojace zbyt dlugo w jednym statusie,
- warto pokazywac tickety wymagajace uwagi: cofniecia statusu, reassigny, SLA breaches, odpowiedzi klienta, krytyczne priorytety,
- customer portal powinien laczyc tworzenie ticketu, sledzenie statusu, komentarze, zalaczniki i docelowo self-service/knowledge base.

Zrodla/inspiracje:

- Zendesk Support dashboard: metryki first reply time, first/full resolution time i backlog po statusie.
- Atlassian Jira Service Management: created vs resolved, time to resolution, SLA met vs breached, workload i requests needing attention.
- Zendesk Customer Portal: submit/update/track requests, komentarze, status, request list i mark as solved.
- Freshdesk Customer Portal: sekcje Tickets, Knowledge Base, Forums, ticket forms, filtry i sugestie artykulow.

## Dokumentacja podstawowa

Cel: projekt ma byc latwy do uruchomienia i obronienia jako calosc.

1. Rozbudowac `README.md`:
   - opis projektu,
   - struktura monorepo,
   - wymagania techniczne,
   - uruchomienie backendu,
   - uruchomienie frontendu,
   - konta testowe,
   - opis auth/session,
   - podstawowe endpointy,
   - role i uprawnienia,
   - statusy i priorytety ticketow.
2. Dalszy rozwoj `docs`.

## Agent dashboard

Cel: agent ma dostac realne centrum pracy, nie tylko liste ticketow.

Obecny stan:

- widok `/agent` pokazuje liczbe ticketow w kolejce, moje, nieprzypisane i wysokiego priorytetu,
- pokazuje liste najpilniejszych,
- nie ma jeszcze prawdziwej kolejki priorytetowej,
- nie pokazuje SLA/ryzyka, ticketow stojacych w miejscu, odpowiedzi klienta ani szybkich akcji.

Docelowy układ dashboardu:

1. Karty KPI agenta:
   - moje aktywne tickety,
   - nieprzypisane tickety,
   - wysokie/krytyczne,
   - czekaja na moja odpowiedz,
   - czekaja na uzytkownika,
   - rozwiazane dzisiaj,
   - docelowo SLA at risk / SLA breached.
2. Sekcja "Moja kolejka pracy":
   - tickety przypisane do aktualnego agenta,
   - sortowanie: SLA/ryzyko, priorytet, ostatnia aktywnosc, data utworzenia,
   - szybkie akcje: komentarz, status, priorytet, przejdz do szczegolow.
3. Sekcja "Do przejecia":
   - nieprzypisane `OPEN`,
   - najpierw `CRITICAL` i `HIGH`,
   - szybki przycisk "Przypisz do mnie".
4. Sekcja "Klient odpowiedzial":
   - tickety, gdzie ostatni publiczny komentarz pochodzi od uzytkownika,
   - tickety po statusie `WAITING_FOR_USER`, ktore wrocily do `IN_PROGRESS`,
   - docelowo licznik nieprzeczytanych komentarzy.
5. Sekcja "Stuck / ryzykowne":
   - tickety bez zmiany statusu przez np. 3/7 dni,
   - tickety z wieloma reassignami,
   - tickety cofajace status,
   - tickety z wysokim priorytetem bez przypisania.
6. Sekcja "Moja efektywnosc":
   - pierwsze odpowiedzi dzisiaj,
   - rozwiazane dzisiaj/tydzien,
   - sredni czas pierwszej odpowiedzi,
   - sredni czas rozwiazania,
   - liczba reopen/wznowionych.
7. Filtry i widoki zapisane:
   - moje,
   - nieprzypisane,
   - wysokie/krytyczne,
   - czeka na klienta,
   - klient odpowiedzial,
   - stuck,
   - zamkniete/rozwiazane.

Potrzebne endpointy/backend:

1. `GET /api/agent/dashboard`
   - `assignedActive`,
   - `unassignedOpen`,
   - `highPriority`,
   - `waitingForAgent`,
   - `waitingForUser`,
   - `resolvedToday`,
   - `stuckTickets`,
   - `customerReplied`.
2. `GET /api/agent/tickets/assigned-to-me`
3. `GET /api/agent/tickets/unassigned`
4. `GET /api/agent/tickets/customer-replied`
5. `GET /api/agent/tickets/stuck?days=7`
6. `PATCH /api/agent/tickets/{id}/reassign` albo endpoint admin-only, jesli reassign ma byc tylko dla admina.

Reguly do doprecyzowania:

1. Czy agent moze zmieniac status dowolnego ticketa, czy tylko przypisanego do siebie.
2. Czy agent moze odrzucic ticket bez przypisania.
3. Czy agent moze zmieniac priorytet ticketa nieprzypisanego.
4. Czy admin moze reassignowac tickety miedzy agentami.
5. Czy komentarz agenta automatycznie ustawia `WAITING_FOR_USER`.

Kolejnosc wdrozenia:

1. Dodac endpointy "assigned-to-me" i "unassigned".
2. Przebudowac `/agent` na kolejke pracy, nie tylko metryki.
3. Dodac szybkie akcje w tabelach.
4. Dodac sekcje "Klient odpowiedzial".
5. Dodac stuck tickets.
6. Dopiero potem dodawac metryki czasowe i SLA.

## Admin dashboard

Cel: panel admina ma byc narzedziem zarzadzania systemem.

Obecny stan:

- widok `/admin` pokazuje liczbe uzytkownikow, aktywnych kont, ticketow i otwartych procesow,
- pokazuje najnowsze tickety,
- nie ma jeszcze statystyk operacyjnych, workload agentow, trendow, SLA, stuck tickets ani audytu.

Docelowy układ dashboardu:

1. Karty KPI systemu:
   - wszystkie otwarte tickety,
   - nowe tickety dzisiaj / w tym tygodniu,
   - rozwiazane dzisiaj / w tym tygodniu,
   - backlog,
   - wysokie/krytyczne,
   - nieprzypisane,
   - aktywni uzytkownicy,
   - aktywni agenci.
2. Sekcja "Created vs resolved":
   - trend dzienny/tygodniowy,
   - czy zespol zamyka wiecej niz przyjmuje,
   - osobno dla wszystkich i wg kategorii.
3. Sekcja "Backlog po statusie":
   - `OPEN`,
   - `IN_PROGRESS`,
   - `WAITING_FOR_USER`,
   - `RESOLVED`,
   - terminalne statusy jako kontekst.
4. Sekcja "SLA i czasy":
   - sredni czas pierwszej odpowiedzi,
   - mediana czasu pierwszej odpowiedzi,
   - sredni czas rozwiazania,
   - mediana czasu rozwiazania,
   - SLA met vs breached,
   - tickety zagrozone SLA.
5. Sekcja "Workload agentow":
   - liczba przypisanych aktywnych ticketow per agent,
   - wysokie/krytyczne per agent,
   - rozwiazane w okresie,
   - sredni czas reakcji,
   - reassign count.
6. Sekcja "Kategorie":
   - liczba ticketow wg kategorii,
   - sredni czas rozwiazania wg kategorii,
   - kategorie z najwiekszym backlogiem,
   - kategorie bez aktywnych agentow/specjalizacji jako przyszly wariant.
7. Sekcja "Requests needing attention":
   - stuck tickets,
   - tickety z wieloma reassignami,
   - tickety cofane w statusie,
   - nieprzypisane high/critical,
   - tickety bez odpowiedzi agenta.
8. Sekcja "Aktywnosc systemu":
   - ostatnie zmiany statusu,
   - ostatnie komentarze publiczne,
   - ostatnie komentarze wewnetrzne,
   - ostatnie zalaczniki,
   - operacje admina na uzytkownikach i kategoriach.
9. Sekcja "Zarzadzanie":
   - skroty do uzytkownikow,
   - agentow,
   - kategorii,
   - wszystkich ticketow,
   - docelowo raportow.

Potrzebne endpointy/backend:

1. `GET /api/admin/dashboard`
   - `ticketSummary`,
   - `userSummary`,
   - `agentSummary`,
   - `categorySummary`,
   - `createdVsResolved`,
   - `backlogByStatus`,
   - `priorityBreakdown`,
   - `recentActivity`,
   - `requestsNeedingAttention`.
2. `GET /api/admin/stats/tickets`
3. `GET /api/admin/stats/agents`
4. `GET /api/admin/stats/categories`
5. `GET /api/admin/activity`
6. `GET /api/admin/tickets/stuck?days=7`
7. `GET /api/admin/tickets/sla-risk`

Funkcje zarzadzania powiazane z dashboardem:

1. Reassign ticketa do konkretnego agenta.
2. Filtrowanie uzytkownikow po roli i statusie.
3. Wyszukiwarka uzytkownikow.
4. Paginacja uzytkownikow.
5. Widok szczegolow agenta.
6. Reset hasla jako przyszly endpoint.
7. Ostrzezenie przy dezaktywacji kategorii, jesli ma aktywne tickety.

Kolejnosc wdrozenia:

1. Dodac `GET /api/admin/dashboard` z podstawowym summary.
2. Przebudowac `/admin` na sekcje KPI + backlog + najnowsza aktywnosc.
3. Dodac workload agentow.
4. Dodac created vs resolved.
5. Dodac stuck tickets i requests needing attention.
6. Dodac metryki czasowe.
7. Dopiero po tym dodac SLA jako pelny mechanizm z progami i breachami.

## Kontrakty backendu i baza

Cel: backend ma byc przygotowany na bardziej produkcyjny tryb pracy.

1. Dodac OpenAPI/Swagger.
2. Dodac migracje Flyway albo Liquibase.
3. Przygotowac profil PostgreSQL.
4. Ograniczyc role do allowlisty:
   - `USER`,
   - `AGENT`,
   - `ADMIN`.
5. Dodac endpointy:
   - przypisane tickety aktualnego agenta,
   - tickety nieprzypisane,
   - reassign ticketu,
   - statystyki admina,
   - activity feed.
6. Dodac podstawowy audit dla operacji administracyjnych.
7. Dopracowac obsluge zalacznikow:
   - whitelist MIME,
   - limity,
   - strategia storage,
   - docelowo skanowanie plikow.

## Jakosc frontendu

Cel: frontend ma byc stabilny, spojny wizualnie i latwy w rozwoju.

1. Dodac linting.
2. Dodac formatowanie.
3. Dodac testy podstawowych utili i flow.
4. Ujednolicic nazewnictwo:
   - ticket vs zgloszenie,
   - login identifier vs identyfikator logowania.
5. Poprawic polskie znaki w etykietach.
6. Dopracowac responsywnosc dashboardow.
7. Rozwazyc podzial wiekszych widokow na mniejsze komponenty.

## Realtime chat

Cel: dodac realtime komunikacje bez mieszania jej z backendem Spring Boot.

Architektura docelowa:

- nowy modul `helpdesk-chat-server`,
- Node.js + Socket.IO,
- jeden ticket = jeden room, np. `ticket:123`,
- frontend ma tylko klienta Socket.IO i komponent czatu,
- logika realtime zostaje w osobnym serwerze.

Kroki:

1. Zaprojektowac kontrakt chatu:
   - event `join-ticket`,
   - event `message:new`,
   - event `typing:start`,
   - event `typing:stop`,
   - event `message:read`.
2. Zaprojektowac autoryzacje Socket.IO:
   - token krotkozyjacy wydawany przez backend,
   - albo walidacja sesji przez backendowy endpoint,
   - sprawdzanie dostepu do konkretnego ticketa.
3. Dodac model historii wiadomosci:
   - `ticket_id`,
   - `author_id`,
   - `content`,
   - `created_at`,
   - `edited_at`,
   - `deleted_at`,
   - `read_at`.
4. Dodac endpoint pobierania historii.
5. Dodac `ChatPanel` do `TicketDetailsPage`.
6. Dopiero potem dodac typing indicators, read receipts i powiadomienia.


## Funkcje komercyjne

Cel: rozwoj projektu w kierunku pelniejszego produktu.

1. SLA i terminy odpowiedzi.
2. Powiadomienia email.
3. Powiadomienia in-app.
4. Baza wiedzy.
5. Szablony odpowiedzi agentow.
6. Eksport CSV/raporty.
7. Integracje Slack/Teams/email.
8. Multi-tenant.
9. SSO/OAuth2.
10. 2FA.
11. Reset hasla.
12. Email verification.
13. Zaawansowane role i permission matrix.

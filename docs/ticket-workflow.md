# Statusy I Reguły Ticketów

Ten plik opisuje techniczne reguły cyklu życia ticketu. Szerszy proces biznesowy jest w [domain-ticket-flow.md](./domain-ticket-flow.md).

## Statusy

`OPEN`

Nowe zgłoszenie oczekujące na reakcję zespołu.

`IN_PROGRESS`

Sprawa jest obsługiwana przez agenta albo wróciła do pracy po odpowiedzi użytkownika.

`WAITING_FOR_USER`

Zespół czeka na informację od użytkownika.

`RESOLVED`

Sprawa jest rozwiązana, ale nie musi być jeszcze zamknięta przez użytkownika.

`CLOSED`

Sprawa zakończona. Status terminalny.

`REJECTED`

Sprawa odrzucona. Status terminalny.

`CANCELLED`

Sprawa anulowana. Status terminalny.

Statusy terminalne blokują edycję ticketu i przypisanie agenta.

## Priorytety

`UNASSIGNED`

Stan początkowy nowego ticketu. Nie można ustawić go przez endpoint zmiany priorytetu.

`LOW`

Niski priorytet.

`MEDIUM`

Średni priorytet.

`HIGH`

Wysoki priorytet.

`CRITICAL`

Krytyczny priorytet.

Priorytet jest niezależny od przypisanego agenta. `UNASSIGNED` nie znaczy, że ticket nie ma agenta. Brak agenta jest reprezentowany przez `assignedTo=null`.

## Macierz Przejść Statusu

`OPEN` może przejść do:

- `IN_PROGRESS`,
- `REJECTED`,
- `CANCELLED`.

`IN_PROGRESS` może przejść do:

- `WAITING_FOR_USER`,
- `RESOLVED`,
- `REJECTED`,
- `CANCELLED`.

`WAITING_FOR_USER` może przejść do:

- `IN_PROGRESS`,
- `RESOLVED`,
- `CANCELLED`.

`RESOLVED` może przejść do:

- `CLOSED`,
- `IN_PROGRESS`,
- `CANCELLED`.

`CLOSED`, `REJECTED` i `CANCELLED` nie mają dalszych przejść.

Niedozwolone przejście zwraca `400`.

## Kto Może Zmienić Status

`ADMIN`

Może wykonać każde przejście dopuszczone przez macierz.

`USER`

Może zmienić status tylko własnego ticketu i tylko na `CANCELLED` albo `CLOSED`.

`AGENT`

Może wykonać każde przejście dopuszczone przez macierz poza ustawieniem `CLOSED`.

Inny zalogowany użytkownik dostaje `403`.

## Daty Statusów

Przejście na `RESOLVED` ustawia `resolvedAt`.

Przejście z `RESOLVED` do `IN_PROGRESS` czyści `resolvedAt`.

Przejście na `CLOSED` ustawia `closedAt`.

`updatedAt` jest aktualizowane przez cykl życia encji JPA przy zmianie ticketu.

## Przypisanie Agenta

Endpoint `PATCH /api/agent/tickets/{id}/assign` przypisuje ticket do aktualnie zalogowanego agenta.

Reguły:

- wymagane jest `AGENT`,
- ticket nie może być terminalny,
- jeśli ticket był `OPEN`, przechodzi do `IN_PROGRESS`,
- ponowne przypisanie do tego samego agenta zwraca `400`.

W aktualnym kodzie nie ma reassignu do innego agenta.

## Automatyka Po Komentarzu

Komentarz wewnętrzny nie zmienia statusu.

Komentarz publiczny staffu może ustawić `WAITING_FOR_USER`.

Komentarz publiczny właściciela ticketu może ustawić `IN_PROGRESS`.

Automatyka pomija ticket terminalny i nie wykonuje niedozwolonych przejść.

## Widoczność Komentarzy I Załączników

Użytkownik widzi tylko komentarze publiczne. Staff widzi komentarze publiczne i wewnętrzne.

Załącznik przypięty do komentarza wewnętrznego nie jest widoczny dla użytkownika. Załącznik na poziomie ticketu albo komentarza publicznego jest widoczny dla osób mających dostęp do ticketu.

## Dashboard Agenta

`GET /api/agent/dashboard` liczy metryki w `TicketService.getAgentDashboard`.

Metryki:

- `assignedActive`: aktywne tickety przypisane do agenta.
- `unassignedOpen`: otwarte tickety bez agenta.
- `highPriority`: aktywne tickety z priorytetem `HIGH` albo `CRITICAL`.
- `waitingForAgent`: moje tickety w `OPEN` albo `IN_PROGRESS`.
- `waitingForUser`: moje tickety w `WAITING_FOR_USER`.
- `resolvedToday`: moje tickety rozwiązane dziś.
- `customerReplied`: moje tickety, gdzie ostatni publiczny komentarz pochodzi od klienta.
- `stuckTickets`: aktywne tickety bez aktywności dłużej niż 7 dni albo nieprzypisane high lub critical.

Aktualna implementacja liczy dashboard w pamięci po pobraniu wszystkich ticketów. Przy większej liczbie rekordów warto przenieść te metryki do zapytań bazodanowych.

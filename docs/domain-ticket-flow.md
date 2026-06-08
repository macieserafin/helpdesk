# Przepływ Zgłoszenia

Ticket jest głównym obiektem domeny. Łączy użytkownika, kategorię, status, priorytet, agenta, komentarze, załączniki i historię zmian. Backend traktuje ticket jako cały proces, a nie tylko rekord do edycji.

## Uczestnicy

`USER`

Klient tworzący zgłoszenie. Może widzieć własne tickety, komentować je, dodawać załączniki, anulować sprawę i zamknąć ją po rozwiązaniu.

`AGENT`

Osoba obsługująca zgłoszenia. Widzi kolejkę, przypisuje ticket do siebie, zmienia statusy poza `CLOSED`, ustawia priorytet i dodaje komentarze wewnętrzne.

`ADMIN`

Administrator systemu. Ma pełny wgląd w tickety i może wykonywać operacje administracyjne. Może zamknąć ticket statusem `CLOSED`.

## Utworzenie

Użytkownik tworzy zgłoszenie przez formularz frontendu albo `POST /api/tickets`.

Backend sprawdza:

- czy konto jest klientem, czyli ma rolę `USER` i nie ma roli `AGENT` ani `ADMIN`,
- czy tytuł i opis nie są puste,
- czy kategoria istnieje i jest aktywna.

Stan początkowy:

- `status=OPEN`,
- `priority=UNASSIGNED`,
- `assignedTo=null`,
- wpis historii `TICKET_CREATED`.

## Kolejka Agenta

Agent pobiera listę przez `GET /api/agent/tickets`. To pełna kolejka ticketów, a nie tylko tickety przypisane do bieżącego agenta. Filtrowanie po agencie jest dostępne parametrem `agent`.

Dashboard agenta używa `GET /api/agent/dashboard`. Backend liczy metryki z wszystkich ticketów i wybiera listy robocze:

- moje aktywne,
- nieprzypisane otwarte,
- wysokie i krytyczne,
- czekające na agenta,
- czekające na użytkownika,
- rozwiązane dziś,
- z odpowiedzią klienta,
- stojące zbyt długo albo nieprzypisane high lub critical.

## Przypisanie

Agent przypisuje ticket do siebie przez `PATCH /api/agent/tickets/{id}/assign`.

Reguły:

- ticket terminalny nie może zostać przypisany,
- nie można przypisać ticketu ponownie do tego samego agenta,
- jeśli ticket był `OPEN`, po przypisaniu przechodzi na `IN_PROGRESS`,
- historia dostaje wpis `ASSIGNED_CHANGED`.

Aktualny kod nie ma reassignu do wskazanego agenta. To oznacza, że admin bez roli `AGENT` nie przypisze ticketu przez endpoint `assign`.

## Nadanie Priorytetu

Agent albo admin ustawia priorytet przez `PATCH /api/agent/tickets/{id}/priority`.

Dozwolone wartości:

- `LOW`,
- `MEDIUM`,
- `HIGH`,
- `CRITICAL`.

`UNASSIGNED` jest stanem początkowym, ale nie jest poprawną wartością requestu zmiany priorytetu. Zmiana zapisuje wpis `PRIORITY_CHANGED`.

## Praca Nad Ticketem

Agent zmienia status przez `PATCH /api/agent/tickets/{id}/status`. Użytkownik ma osobną ścieżkę `PATCH /api/tickets/{id}/status`.

Dozwolone przejścia są opisane w [ticket-workflow.md](./ticket-workflow.md). Serwis najpierw sprawdza, czy przejście istnieje w macierzy, a dopiero potem rolę aktora.

Najważniejsze zasady:

- agent może przesuwać ticket między statusami roboczymi i rozwiązać go,
- agent nie może ustawić `CLOSED`,
- użytkownik może anulować własny ticket,
- użytkownik może zamknąć własny ticket, jeśli przejście z aktualnego statusu do `CLOSED` jest dozwolone,
- admin może wykonać każde dozwolone przejście.

## Komentarze

Komentarze są częścią pracy nad ticketem.

Komentarz publiczny:

- widzi go użytkownik i staff,
- może automatycznie zmienić status.

Komentarz wewnętrzny:

- może dodać tylko `AGENT` albo `ADMIN`,
- widzi go staff,
- nie zmienia statusu automatycznie,
- załączniki przypięte do komentarza wewnętrznego nie są widoczne dla użytkownika.

Automatyka statusów:

- publiczny komentarz staffu może ustawić `WAITING_FOR_USER`,
- publiczny komentarz właściciela może ustawić `IN_PROGRESS`,
- automatyka działa tylko wtedy, gdy przejście jest dozwolone.

## Załączniki

Załącznik jest zapisywany w dwóch miejscach:

- metadane w tabeli `attachments`,
- plik binarny w katalogu storage backendu.

Załącznik może należeć bezpośrednio do ticketu albo do komentarza. Jeśli komentarz jest wewnętrzny, użytkownik nie widzi tego załącznika.

Usunięcie załącznika:

- autor uploadu może usunąć własny plik,
- admin może usunąć dowolny plik,
- operacja usuwa metadane i próbuje usunąć plik z dysku,
- historia dostaje wpis `ATTACHMENT_DELETED`.

## Rozwiązanie I Zamknięcie

Agent oznacza sprawę jako `RESOLVED`. Backend ustawia wtedy `resolvedAt`.

Po rozwiązaniu właściciel ticketu albo admin może ustawić `CLOSED`. Backend ustawia wtedy `closedAt`. Status `CLOSED` kończy proces i blokuje dalszą edycję pól ticketu.

Jeśli ticket przejdzie z `RESOLVED` z powrotem do `IN_PROGRESS`, backend czyści `resolvedAt`.

## Odrzucenie I Anulowanie

`REJECTED` oznacza odrzucenie przez staff. Jest statusem terminalnym.

`CANCELLED` oznacza anulowanie przez użytkownika albo admina zgodnie z macierzą przejść. Też jest statusem terminalnym.

Status terminalny blokuje:

- edycję tytułu, opisu i kategorii,
- przypisanie agenta.

Komentarze i historia nadal mogą być odczytywane, jeśli użytkownik ma dostęp do ticketu.

## Historia

Każda istotna operacja zapisuje `TicketHistory`:

- utworzenie,
- edycja pól,
- zmiana statusu,
- zmiana priorytetu,
- przypisanie,
- komentarz,
- dodanie i usunięcie załącznika,
- rozwiązanie,
- zamknięcie.

Historia jest podstawą widoku timeline we frontendzie i pomaga odtworzyć przebieg sprawy bez czytania wszystkich komentarzy.

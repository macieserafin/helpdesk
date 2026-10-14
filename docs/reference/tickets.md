# Przebieg zgłoszenia / Ticket workflow

[Polski](#polski) | [English](#english)

## Polski

Użytkownik tworzy zgłoszenie w aktywnej kategorii. Agent przejmuje sprawę, nadaje priorytet, wymienia komentarze i oznacza rozwiązanie. Właściciel lub administrator zamyka rozwiązane zgłoszenie.

Typowa ścieżka:

```text
OPEN -> IN_PROGRESS -> WAITING_FOR_USER -> IN_PROGRESS -> RESOLVED -> CLOSED
```

Nie każda sprawa wymaga oczekiwania na odpowiedź użytkownika. Dostęp poszczególnych ról opisują [uprawnienia](roles.md#polski), a ścieżki HTTP znajdziesz w [API](api.md#polski).

<a id="pl-statusy-i-dozwolone-przejścia"></a>

### Statusy i dozwolone przejścia

| Status | Znaczenie | Może przejść do |
| --- | --- | --- |
| `OPEN` | Nowe zgłoszenie | `IN_PROGRESS`, `REJECTED`, `CANCELLED` |
| `IN_PROGRESS` | Sprawa w obsłudze | `WAITING_FOR_USER`, `RESOLVED`, `REJECTED`, `CANCELLED` |
| `WAITING_FOR_USER` | Potrzebna odpowiedź użytkownika | `IN_PROGRESS`, `RESOLVED`, `CANCELLED` |
| `RESOLVED` | Rozwiązane, czeka na zamknięcie | `CLOSED`, `IN_PROGRESS`, `CANCELLED` |
| `CLOSED` | Zamknięte | Brak |
| `REJECTED` | Odrzucone | Brak |
| `CANCELLED` | Anulowane | Brak |

Niedozwolone przejście zwraca `400`. Statusy końcowe (`CLOSED`, `REJECTED`, `CANCELLED`) blokują edycję tytułu, opisu i kategorii oraz przypisanie agenta.

Użytkownik może anulować własną sprawę albo zamknąć ją z `RESOLVED`. Agent może wykonać dozwolone przejścia poza ustawieniem `CLOSED`; administrator może wykonać każde przejście z tabeli.

<a id="pl-przypisanie-i-priorytet"></a>

### Przypisanie i priorytet

Nowe zgłoszenie ma `OPEN`, `priority=UNASSIGNED` i `assignedTo=null`. Przypisanie do zalogowanego agenta zmienia `OPEN` na `IN_PROGRESS`. Ponowne przypisanie do tego samego agenta zwraca `400`. Nie ma jeszcze operacji przekazania sprawy wskazanemu agentowi.

Agent i administrator mogą ustawić `LOW`, `MEDIUM`, `HIGH` lub `CRITICAL`. `UNASSIGNED` jest wyłącznie priorytetem początkowym i nie można ustawić go ponownie przez API. Priorytet i przypisanie są niezależne: brak agenta oznacza `assignedTo=null`.

<a id="pl-komentarze-i-załączniki"></a>

### Komentarze i załączniki

Komentarz publiczny zespołu może ustawić `WAITING_FOR_USER`, a odpowiedź właściciela `IN_PROGRESS`. Automatyczna zmiana działa tylko dla dozwolonych przejść i pomija statusy końcowe. Komentarz wewnętrzny nie zmienia statusu.

Użytkownik widzi wyłącznie komentarze publiczne. Agent i administrator widzą także wewnętrzne. Ta sama zasada dotyczy załączników powiązanych z komentarzem. Pliki dodane bez komentarza są widoczne osobom mającym dostęp do zgłoszenia.

Załącznik zawsze należy do zgłoszenia; opcjonalnie także do komentarza. Autor może usunąć własny plik, administrator dowolny. Podczas usuwania załącznika backend kasuje metadane i próbuje usunąć plik z dysku. Limity przesyłanych plików znajdziesz w [API](api.md#pl-limity-i-walidacja).

<a id="pl-daty-i-historia"></a>

### Daty i historia

- `RESOLVED` ustawia `resolvedAt`; powrót do `IN_PROGRESS` czyści tę datę.
- `CLOSED` ustawia `closedAt`.
- Pole `updatedAt` jest aktualizowane podczas zapisu encji JPA.

Historia jest zwracana od najstarszego wpisu. Typy zdarzeń: `TICKET_CREATED`, `TICKET_UPDATED`, `STATUS_CHANGED`, `PRIORITY_CHANGED`, `ASSIGNED_CHANGED`, `COMMENT_ADDED`, `ATTACHMENT_ADDED`, `ATTACHMENT_DELETED`, `TICKET_RESOLVED`, `TICKET_CLOSED`.

<a id="pl-panel-agenta"></a>

### Panel agenta

Kolejka obejmuje wszystkie zgłoszenia. Parametr `agent` pozwala zawęzić ją do konkretnego loginu. Panel pokazuje następujące metryki i listy robocze:

| Metryka | Znaczenie |
| --- | --- |
| `assignedActive` | Aktywne sprawy przypisane do mnie |
| `unassignedOpen` | Otwarte sprawy bez agenta |
| `highPriority` | Aktywne sprawy HIGH lub CRITICAL |
| `waitingForAgent` | Moje sprawy OPEN lub IN_PROGRESS |
| `waitingForUser` | Moje sprawy WAITING_FOR_USER |
| `resolvedToday` | Moje sprawy rozwiązane dziś |
| `customerReplied` | Moje sprawy, w których ostatni publiczny komentarz jest od klienta |
| `stuckTickets` | Aktywne sprawy bez aktywności ponad 7 dni lub nieprzypisane HIGH/CRITICAL |

Metryki są liczone w pamięci po pobraniu zgłoszeń. Przeniesienie obliczeń do bazy jest zaplanowane w [roadmapie](../roadmap.md#polski).

[Przejdź do wersji angielskiej](#english)

---

## English

[Wróć do wersji polskiej](#polski)

A user creates a ticket in an active category. An agent takes ownership, sets a priority, exchanges comments and marks it as resolved. The owner or an administrator closes the resolved ticket.

Typical flow:

```text
OPEN -> IN_PROGRESS -> WAITING_FOR_USER -> IN_PROGRESS -> RESOLVED -> CLOSED
```

Not every case requires a user reply. See [permissions](roles.md#english) for role access and the [API](api.md#english) for HTTP paths.

<a id="en-statuses-and-allowed-transitions"></a>

### Statuses and allowed transitions

| Status | Meaning | Can transition to |
| --- | --- | --- |
| `OPEN` | New ticket | `IN_PROGRESS`, `REJECTED`, `CANCELLED` |
| `IN_PROGRESS` | Being handled | `WAITING_FOR_USER`, `RESOLVED`, `REJECTED`, `CANCELLED` |
| `WAITING_FOR_USER` | A user reply is needed | `IN_PROGRESS`, `RESOLVED`, `CANCELLED` |
| `RESOLVED` | Resolved, awaiting closure | `CLOSED`, `IN_PROGRESS`, `CANCELLED` |
| `CLOSED` | Closed | None |
| `REJECTED` | Rejected | None |
| `CANCELLED` | Cancelled | None |

An invalid transition returns `400`. Final statuses (`CLOSED`, `REJECTED`, `CANCELLED`) prevent changes to the title, description and category, and prevent agent assignment.

Users can cancel their own tickets or close them from `RESOLVED`. Agents can perform allowed transitions except setting `CLOSED`; administrators can perform any transition in the table.

<a id="en-assignment-and-priority"></a>

### Assignment and priority

New tickets start with `OPEN`, `priority=UNASSIGNED` and `assignedTo=null`. Assigning an OPEN ticket to the current agent changes it to `IN_PROGRESS`. Assigning it to the same agent again returns `400`. Reassignment to a selected agent is not implemented yet.

Agents and administrators can set `LOW`, `MEDIUM`, `HIGH` or `CRITICAL`. `UNASSIGNED` is only the initial priority and cannot be set again through the API. Priority and assignment are independent: no agent is represented by `assignedTo=null`.

<a id="en-comments-and-attachments"></a>

### Comments and attachments

A public staff comment may set `WAITING_FOR_USER`; an owner's reply may set `IN_PROGRESS`. Automatic changes follow allowed transitions and skip final statuses. Internal comments do not change status.

Users see only public comments. Agents and administrators also see internal comments. The same rule applies to comment attachments. Files added without a comment are visible to anyone with access to the ticket.

An attachment always belongs to a ticket and may also belong to a comment. Uploaders can delete their own files; administrators can delete any file. Deletion removes metadata and attempts to remove the file from disk. Upload limits are in the [API](api.md#en-limits-and-validation).

<a id="en-dates-and-history"></a>

### Dates and history

- `RESOLVED` sets `resolvedAt`; returning to `IN_PROGRESS` clears it.
- `CLOSED` sets `closedAt`.
- `updatedAt` is updated when the JPA entity is saved.

History is returned oldest first. Event types: `TICKET_CREATED`, `TICKET_UPDATED`, `STATUS_CHANGED`, `PRIORITY_CHANGED`, `ASSIGNED_CHANGED`, `COMMENT_ADDED`, `ATTACHMENT_ADDED`, `ATTACHMENT_DELETED`, `TICKET_RESOLVED`, `TICKET_CLOSED`.

<a id="en-agent-dashboard"></a>

### Agent dashboard

The queue includes all tickets. The `agent` parameter filters it by an agent's login. The dashboard exposes these metrics and work lists:

| Metric | Meaning |
| --- | --- |
| `assignedActive` | Active tickets assigned to me |
| `unassignedOpen` | Open tickets without an agent |
| `highPriority` | Active HIGH or CRITICAL tickets |
| `waitingForAgent` | My OPEN or IN_PROGRESS tickets |
| `waitingForUser` | My WAITING_FOR_USER tickets |
| `resolvedToday` | My tickets resolved today |
| `customerReplied` | My tickets whose last public comment is from the customer |
| `stuckTickets` | Active tickets idle for over 7 days, or unassigned HIGH/CRITICAL tickets |

Metrics are calculated in memory after loading tickets. Moving these calculations to the database is planned in the [roadmap](../roadmap.md#english).

[Back to the Polish version](#polski)

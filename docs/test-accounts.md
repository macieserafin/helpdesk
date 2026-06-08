# Konta Testowe I Seed

`APP_DEMO_SEED_ENABLED` jest przełącznikiem głównym. Poszczególne części seeda można włączać i wyłączać osobno:

```env
APP_DEMO_SEED_ACCOUNTS_ENABLED=true
APP_DEMO_SEED_CATEGORIES_ENABLED=true
APP_DEMO_SEED_TICKETS_ENABLED=true
```

Przykładowe tickety wymagają kont `user`/`agent` oraz aktywnych kategorii, więc przy `APP_DEMO_SEED_TICKETS_ENABLED=true` te dane muszą już istnieć albo też być seedowane.

Backend tworzy dane demonstracyjne przez `SeedConfig`, jeśli `APP_DEMO_SEED_ENABLED=true`. Domyślnie seed jest włączony w konfiguracji lokalnej.

## Kolejność Seeda

1. Użytkownicy, role i profile.
2. Kategorie.
3. Przykładowe tickety.

Seed użytkowników i kategorii jest idempotentny po loginie albo nazwie. Tickety są idempotentne po tytule.

## Konta

`user`

- hasło: `user123`,
- email: `user@example.com`,
- rola: `USER`,
- profil: Jan Kowalski, Warszawa.

`agent`

- hasło: `agent123`,
- email: `agent@example.com`,
- rola: `AGENT`,
- profil: Anna Nowak, Kraków.

`admin`

- hasło: `admin123`,
- email: `admin@example.com`,
- rola: `ADMIN`,
- profil: Piotr Zieliński, Gdańsk.

Konto `user` może logować się także przez email. Konta `agent` i `admin` logują się przez `loginIdentifier`.

## Widoki Startowe

- `user`: `/user`
- `agent`: `/agent`
- `admin`: `/admin`

## Kategorie

Seed tworzy aktywne kategorie:

- Konto,
- Uprawnienia,
- Załączniki,
- Aplikacja,
- Inne.

Kategorie są używane przez przykładowe tickety i formularz tworzenia zgłoszenia.

## Przykładowe Tickety

`Nie mogę zalogować się do panelu`

Kategoria `Konto`, status `OPEN`, priorytet `HIGH`.

`Brak dostępu do raportów`

Kategoria `Uprawnienia`, status `IN_PROGRESS`, przypisany agent, komentarz wewnętrzny.

`Nie mogę dodać załącznika`

Kategoria `Załączniki`, status `WAITING_FOR_USER`, agent poprosił o dodatkowe dane.

`Formularz profilu nie zapisuje zmian`

Kategoria `Aplikacja`, status `RESOLVED`, wymiana komentarzy użytkownika i agenta.

`Reset hasła do konta testowego`

Kategoria `Konto`, status `CLOSED`, zamknięty przez użytkownika po rozwiązaniu.

`Prośba o dostęp do panelu administracyjnego`

Kategoria `Uprawnienia`, status `REJECTED`, priorytet `CRITICAL`.

`Duplikat zgłoszenia o problemie z logowaniem`

Kategoria `Inne`, status `CANCELLED`, anulowany przez użytkownika.

## Produkcyjne Hasła Demo

W produkcji można wymusić niestandardowe hasła demo:

```text
APP_DEMO_SEED_REQUIRE_CUSTOM_PASSWORDS=true
```

Wtedy backend nie wystartuje, jeśli hasła demo są domyślne albo krótsze niż 12 znaków.

Zmienne:

- `APP_DEMO_SEED_USER_PASSWORD`,
- `APP_DEMO_SEED_AGENT_PASSWORD`,
- `APP_DEMO_SEED_ADMIN_PASSWORD`.

## Reset Seeda

W Docker Compose:

```bash
docker compose down -v
docker compose build
docker compose up
```

W lokalnym PostgreSQL usuń bazę `helpdesk`, utwórz ją ponownie i uruchom backend.

## Testowanie Ról

Osobiście polecam trzy profile przeglądarki:

- User z kontem `user`,
- Agent z kontem `agent`,
- Admin z kontem `admin`.

Każdy profil ma własne cookie `JSESSIONID`, więc role można testować równolegle.

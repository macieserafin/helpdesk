# Produkcyjny Deploy Na VPS

Produkcja używa `docker-compose.prod.yml`. Publiczny ruch przyjmuje Caddy na portach `80` i `443`. Backend, PostgreSQL i frontendowy nginx działają w prywatnej sieci Dockera.

## Założenia

- DNS domeny wskazuje na IP VPS.
- Firewall przepuszcza tylko SSH, HTTP i HTTPS.
- Porty backendu i PostgreSQL nie są publiczne.
- `.env` produkcyjny ma mocne hasła.
- CORS zawiera wyłącznie produkcyjne domeny.
- Seed demo nie używa domyślnych haseł.
- Backup obejmuje bazę i uploady.

## Plik Środowiskowy

Na serwerze skopiuj szablon:

```bash
cp deploy/production.env.example .env
```

Ustaw domenę:

```env
APP_DOMAIN=helpdesk.example.com
APP_CORS_ALLOWED_ORIGIN_PATTERNS=https://helpdesk.example.com
```

Dla wariantu z `www`:

```env
APP_DOMAIN=example.com, www.example.com
APP_CORS_ALLOWED_ORIGIN_PATTERNS=https://example.com,https://www.example.com
```

Ustaw mocne hasła:

```env
POSTGRES_PASSWORD=tu_dlugie_losowe_haslo
APP_DEMO_SEED_USER_PASSWORD=tu_dlugie_losowe_haslo_user
APP_DEMO_SEED_AGENT_PASSWORD=tu_dlugie_losowe_haslo_agent
APP_DEMO_SEED_ADMIN_PASSWORD=tu_dlugie_losowe_haslo_admin
APP_DEMO_SEED_REQUIRE_CUSTOM_PASSWORDS=true
```

Jeśli nie chcesz kont demo w produkcji:

```env
APP_DEMO_SEED_ENABLED=false
```

## Start

Docker Compose automatycznie czyta plik `.env` z bieżącego katalogu. Uruchom:

```bash
docker compose -f docker-compose.prod.yml build
docker compose -f docker-compose.prod.yml up -d
```

Sprawdzenie usług:

```bash
docker compose -f docker-compose.prod.yml ps
docker compose -f docker-compose.prod.yml logs caddy
docker compose -f docker-compose.prod.yml logs backend
```

## Reverse Proxy

Caddy używa `deploy/Caddyfile`.

Ruch:

1. Przeglądarka łączy się z Caddy po HTTPS.
2. Caddy przekazuje request do `frontend:80`.
3. Frontendowy nginx serwuje SPA.
4. Requesty `/api` nginx przekazuje do backendu.
5. Backend komunikuje się z PostgreSQL po sieci Dockera.

## Firewall

Przykład dla `ufw`:

```bash
ufw allow OpenSSH
ufw allow 80/tcp
ufw allow 443/tcp
ufw enable
ufw status
```

Nie otwieraj portów `8080` ani `5432`.

## Dane Trwałe

Wolumeny produkcyjne:

- `helpdesk_prod_postgres_data`,
- `helpdesk_prod_backend_uploads`,
- `helpdesk_prod_caddy_data`,
- `helpdesk_prod_caddy_config`.

Backup powinien obejmować bazę i uploady. Caddy może odtworzyć certyfikaty, ale jego wolumeny przyspieszają restart i utrzymują stan ACME.

## Backup

```bash
sh deploy/backup.sh
```

Skrypt zapisuje backupy w katalogu `backups/`.

Po pierwszym wdrożeniu sprawdź ręcznie, czy backup zawiera:

- dump PostgreSQL,
- pliki uploadów,
- czytelne nazwy plików z datą.

## Aktualizacja

```bash
git pull
docker compose -f docker-compose.prod.yml build
docker compose -f docker-compose.prod.yml up -d
```

Po aktualizacji sprawdź:

- logi backendu,
- migrację schematu przez Hibernate,
- logowanie każdej roli,
- upload i pobranie załącznika,
- poprawność domeny i certyfikatu HTTPS.

## Ryzyka Produkcyjne

Przed użyciem w realnym środowisku trzeba domknąć:

- migracje bazy,
- CSRF token flow,
- politykę sesji i cookie,
- allowlistę ról,
- limity i whitelistę MIME dla załączników,
- monitoring i rotację logów,
- procedurę restore z backupu.

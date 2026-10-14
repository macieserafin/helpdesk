# Starszy wariant: VPS z Caddy / Legacy VPS setup with Caddy

[Polski](#polski) | [English](#english)

## Polski

Ten wariant używa `docker-compose.prod.yml` i lokalnego budowania obrazów. Aktualny proces z GitHub Actions, GHCR i Nginx opisuje [wdrożenie](deployment.md#polski).

Nie uruchamiaj obu wariantów dla jednej instalacji. Mają osobne wolumeny; przejście wymaga przeniesienia danych. Ten Compose nie włącza automatycznie profilu `prod` i jego migracji.

<a id="pl-przygotowanie"></a>

### Przygotowanie

DNS domeny powinien wskazywać VPS, a firewall dopuszczać SSH, HTTP i HTTPS. Backend i PostgreSQL pozostają w prywatnej sieci Dockera.

Z głównego katalogu repozytorium na serwerze:

```bash
cp deploy/production.env.example .env
```

Ustaw `APP_DOMAIN`, origin w `APP_CORS_ALLOWED_ORIGIN_PATTERNS` i mocne hasło PostgreSQL. Przykład:

```env
APP_DOMAIN=helpdesk.example.com
APP_CORS_ALLOWED_ORIGIN_PATTERNS=https://helpdesk.example.com
```

Dla wielu domen Caddy używa np. `APP_DOMAIN=example.com, www.example.com`, a CORS `https://example.com,https://www.example.com`.

Jeśli włączasz konta demo, ustaw własne hasła USER, AGENT i ADMIN za pomocą [zmienne seeda](../development/demo-data.md#polski) oraz `APP_DEMO_SEED_REQUIRE_CUSTOM_PASSWORDS=true`. Aby wyłączyć tworzenie wszystkich danych początkowych, ustaw `APP_DEMO_SEED_ENABLED=false`. Zadbaj też o HTTPS, parametry cookie i czas bezczynności sesji zgodnie z [opisem bezpieczeństwa](../reference/security.md#polski).

<a id="pl-start-i-aktualizacja"></a>

### Start i aktualizacja

```bash
docker compose -f docker-compose.prod.yml up --build -d
docker compose -f docker-compose.prod.yml ps
docker compose -f docker-compose.prod.yml logs --tail=100 caddy backend
```

Po `git pull` powtórz komendę startu. Sprawdź logowanie, zgłoszenia, załączniki i certyfikat.

`deploy/Caddyfile` przekazuje ruch HTTPS do `frontend:80`; frontendowy Nginx przekazuje `/api` do backendu.

<a id="pl-dane-i-kopie-zapasowe"></a>

### Dane i kopie zapasowe

Wolumeny: `helpdesk_prod_postgres_data`, `helpdesk_prod_backend_uploads`, `helpdesk_prod_caddy_data` i `helpdesk_prod_caddy_config`.

```bash
sh deploy/backup.sh
```

Kopie są w `backups/`. Sprawdź zrzut bazy i archiwum załączników oraz wykonaj próbę odtworzenia. Starszy skrypt nie zatrzymuje zapisu plików, dlatego na czas tworzenia kopii zaplanuj przerwę w pracy aplikacji. Nie używaj `down -v` na produkcji.

Przed realnym użyciem dostosuj migracje do istniejącej bazy oraz przygotuj politykę sesji, monitoring i kopie zapasowe. Pozostałe ryzyka aplikacji opisuje [audyt](security-audit.md#polski).

[Przejdź do wersji angielskiej](#english)

---

## English

[Wróć do wersji polskiej](#polski)

This setup uses `docker-compose.prod.yml` and local image builds. See [deployment](deployment.md#english) for the current GitHub Actions, GHCR and Nginx process.

Do not run both setups for one installation. They use separate volumes, so switching requires data migration. This Compose stack does not automatically enable the `prod` profile or its migrations.

<a id="en-preparation"></a>

### Preparation

Point the domain's DNS to the VPS and allow SSH, HTTP and HTTPS through the firewall. The backend and PostgreSQL stay in the private Docker network.

From the repository root on the server:

```bash
cp deploy/production.env.example .env
```

Set `APP_DOMAIN`, the origin in `APP_CORS_ALLOWED_ORIGIN_PATTERNS` and a strong PostgreSQL password. Example:

```env
APP_DOMAIN=helpdesk.example.com
APP_CORS_ALLOWED_ORIGIN_PATTERNS=https://helpdesk.example.com
```

For multiple domains, Caddy uses a value such as `APP_DOMAIN=example.com, www.example.com`, while CORS uses `https://example.com,https://www.example.com`.

If enabling demo accounts, set custom USER, AGENT and ADMIN passwords using the [seed variables](../development/demo-data.md#english) and `APP_DEMO_SEED_REQUIRE_CUSTOM_PASSWORDS=true`. Disable all seeding with `APP_DEMO_SEED_ENABLED=false`. Configure HTTPS, cookie settings and session idle timeout as described in [security](../reference/security.md#english).

<a id="en-startup-and-updates"></a>

### Startup and updates

```bash
docker compose -f docker-compose.prod.yml up --build -d
docker compose -f docker-compose.prod.yml ps
docker compose -f docker-compose.prod.yml logs --tail=100 caddy backend
```

After `git pull`, repeat the startup command. Check login, tickets, attachments and the certificate.

`deploy/Caddyfile` forwards HTTPS traffic to `frontend:80`; frontend Nginx forwards `/api` to the backend.

<a id="en-data-and-backups"></a>

### Data and backups

Volumes: `helpdesk_prod_postgres_data`, `helpdesk_prod_backend_uploads`, `helpdesk_prod_caddy_data` and `helpdesk_prod_caddy_config`.

```bash
sh deploy/backup.sh
```

Backups are in `backups/`. Check the database dump and attachment archive, and test restoration. The legacy script does not stop file writes, so schedule application downtime while creating a backup. Do not use `down -v` in production.

Before real use, adapt migrations to the existing database and prepare session policy, monitoring and backups. See the [audit](security-audit.md#english) for other application risks.

[Back to the Polish version](#polski)

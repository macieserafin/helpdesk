# Wdrożenie na VPS / VPS deployment

[Polski](#polski) | [English](#english)

## Polski

Aktualny wariant używa `compose.prod.yml`, GitHub Actions, obrazów GHCR i Nginx na VPS. Katalog aplikacji na serwerze to `~/apps/helpdesk`.

Starszy [wariant Caddy](caddy.md#polski) ma osobny Compose i wolumeny. Nie uruchamiaj obu zestawów dla tej samej instalacji. Przed przeniesieniem istniejących danych wykonaj kopię zapasową i przygotuj plan migracji.

<a id="pl-1-sprawdź-serwer-i-dostęp"></a>

### 1. Sprawdź serwer i dostęp

Potrzebujesz dostępu SSH, klucza, który nie wymaga interaktywnego podania hasła, zweryfikowanego odcisku klucza hosta, domeny i dostępu do jej ustawień DNS oraz dostępu do ustawień repozytorium GitHub.

Na VPS sprawdź zasoby, architekturę i porty:

```bash
free -h
df -h
uname -m
sudo ss -lntp
sudo docker ps
sudo -n docker ps
```

Zakładana platforma to x86_64. Port `3001` musi być wolny. Wykorzystaj istniejącego Dockera, Nginx i Certbot. Dopuść na zaporze sieciowej SSH, HTTP i HTTPS; nie wystawiaj bazy ani backendu.

Przed wdrożeniem ustal, czy na serwerze są dane do zachowania. Skrypt przygotowania znajduje się w `ops/prepare-vps.sh`; konfigurację porównaj z istniejącą instalacją.

<a id="pl-2-skonfiguruj-github"></a>

### 2. Skonfiguruj GitHub

W środowisku `production` ustaw:

| Typ | Nazwa | Wartość |
| --- | --- | --- |
| Sekret | `VPS_HOST` | Adres VPS |
| Sekret | `VPS_USER` | Użytkownik wdrożeniowy |
| Sekret | `VPS_SSH_KEY` | Prywatny klucz automatyzacji |
| Sekret | `VPS_KNOWN_HOSTS` | Zaufany wpis zgodny z hostem i portem SSH |
| Zmienna | `PUBLIC_URL` | https://twoja-domena |
| Zmienna | `APP_PORT` | 3001 |
| Zmienna | `VPS_SSH_PORT` | 22 lub właściwy port SSH |

Token GHCR zapewnia GitHub. Nie twórz ręcznie GITHUB_TOKEN. Nazwa projektu to helpdesk; frontend słucha na 80, backend na 8080, a gotowość sprawdza `/api/health`. Zmienne CONTAINER_PORT i HEALTH_PATH z przykładów dla pojedynczego kontenera nie są używane w tym projekcie.

<a id="pl-3-przygotuj-konfigurację-aplikacji"></a>

### 3. Przygotuj konfigurację aplikacji

Utwórz `~/apps/helpdesk/releases`. Katalog aplikacji powinien mieć prawa 700, a prywatny plik `~/apps/helpdesk/.env` prawa 600. Użyj [szablonu produkcyjnego](../../ops/production.env.example); nie nadpisuj istniejącego pliku.

Ustaw bazę, użytkownika, hasło PostgreSQL, adres produkcyjny w konfiguracji CORS, e-mail i hasło administratora. Hasła generuj na serwerze, np. przez `openssl rand -hex 32`, i zapisuj bez ujawniania w logach. Hasło administratora przechowuj w menedżerze haseł. Uprawnienia pliku nie chronią przed administratorem VPS.

Aktualny Compose wymusza tryb tworzenia tylko konta administratora. Gdy mechanizm danych początkowych jest włączony, tworzy konto `admin` i aktywne kategorie, ale nie dodaje kont user/agent ani zgłoszeń demo. Zmiana hasła w .env nie zmienia istniejącego konta. Wyłączenie tworzenia danych początkowych nie usuwa dawnych kont demo; ich usunięcie wymaga osobnej operacji z uwzględnieniem powiązań.

<a id="pl-4-ustaw-domenę-i-https"></a>

### 4. Ustaw domenę i HTTPS

Dodaj blok Nginx według [szablonu](../../ops/nginx/helpdesk.conf.example), dopasuj domenę i port. Sprawdź konfigurację poleceniem `sudo nginx -t`, a potem przeładuj Nginx.

Ustaw rekord DNS A wskazujący na VPS i skonfiguruj certyfikat za pomocą istniejącego Certbot. Sprawdź też odnawianie certyfikatu. Frontend jest dostępny wyłącznie na `127.0.0.1`; Nginx udostępnia go po HTTPS. PostgreSQL i backend nie publikują portów.

<a id="pl-5-wdrożenie-przez-ci"></a>

### 5. Wdrożenie przez CI

Proces zapisany w `.github/workflows/ci-cd.yml` obejmuje:

1. Testy aplikacji.
2. Budowę obrazów backendu i frontendu.
3. Test tych obrazów przez Compose z pustą bazą PostgreSQL.
4. Publikację tych samych obrazów do GHCR z tagiem SHA.
5. Wdrożenie przez SSH oraz sprawdzenie gotowości aplikacji i dostępności HTTPS.

Pull request testuje obrazy, ale ich nie publikuje i nie wdraża. Za wdrożenie odpowiada `ops/deploy.sh`.

<a id="pl-migracje-i-przywracanie-wersji"></a>

### Migracje i przywracanie wersji

Profil `prod` włącza Flyway i Hibernate validate. Nie wykonuje automatycznej aktualizacji schematu. Migracja V1 dotyczy **nowej bazy**. Nie włączaj baseline dla starej bazy bez porównania schematu.

Wydania trafiają do `releases/SHA`, a `current` wskazuje ostatnie udane. Skrypt blokuje równoczesne wdrożenia przez flock. Błąd pobrania pozostawia działającą wersję; błąd startu uruchamia próbę przywrócenia poprzednich obrazów.

Plik `release.env` zapisuje obrazy i port wydania. Przywrócenie poprzedniej wersji **nie cofa zmian w bazie ani we wspólnym pliku .env**. Błąd publicznego adresu DNS lub HTTPS nie powoduje przywrócenia poprzednich kontenerów.

Aby ręcznie przywrócić poprzednią wersję, wykonaj poniższe polecenia w Bash jako użytkownik wdrożeniowy:

```bash
cd ~/apps/helpdesk
old_sha=PELNE_SHA_POPRZEDNIEGO_WYDANIA
(
  flock -w 600 9
  sudo -n docker compose -p helpdesk --env-file .env \
    --env-file "releases/$old_sha/release.env" \
    -f "releases/$old_sha/compose.prod.yml" \
    up -d --pull never --wait --wait-timeout 240 && \
    ln -sfn "$PWD/releases/$old_sha" current
) 9>deploy.lock
```

<a id="pl-kopie-zapasowe-i-odtwarzanie-danych"></a>

### Kopie zapasowe i odtwarzanie danych

```bash
bash ~/apps/helpdesk/current/ops/backup.sh
```

Skrypt zapisuje zrzut PostgreSQL i archiwum załączników w jednym katalogu backups. Na czas tworzenia kopii skrypt zatrzymuje backend, co powoduje krótką przerwę w dostępności aplikacji. Ustal harmonogram i czas przechowywania kopii oraz zapisuj zaszyfrowaną kopię poza VPS.

Próbę odtworzenia wykonaj na izolowanej instalacji:

1. Odtwórz `database.dump` przez `pg_restore` do PostgreSQL.
2. Rozpakuj `attachments.tar.gz` do wolumenu `backend_uploads`.
3. Sprawdź logowanie, zgłoszenia i odczyt starego załącznika.

Nie używaj `down -v` na produkcji. Polecenie usuwa wolumeny z danymi.

<a id="pl-sprawdzenie-po-wdrożeniu"></a>

### Sprawdzenie po wdrożeniu

- HTTPS, odnawianie certyfikatu i `/api/health`.
- Logowanie i wylogowanie admina, utworzenie użytkownika/agenta.
- Utworzenie i obsługa sprawy, odmowa dostępu do cudzej sprawy, dodawanie i pobieranie plików.
- Logi backendu, nagłówki strony i brak publicznych portów bazy/backendu.
- Poprawność kopii zapasowej i udana próba odtworzenia.

Limity pamięci w Compose (512/768/128 MB) są wartościami początkowymi; dostosuj je na podstawie pomiarów. Logi są rotowane po osiągnięciu 10 MB, z zachowaniem trzech plików. Monitoruj HTTPS, gotowość aplikacji, wolne miejsce na dysku i czas od ostatniej kopii zapasowej. Pozostałe ograniczenia bezpieczeństwa są w [audycie](security-audit.md#polski).

[Przejdź do wersji angielskiej](#english)

---

## English

[Wróć do wersji polskiej](#polski)

The current setup uses `compose.prod.yml`, GitHub Actions, GHCR images and Nginx on the VPS. The server application directory is `~/apps/helpdesk`.

The [legacy Caddy setup](caddy.md#english) has separate Compose configuration and volumes. Do not run both stacks for the same installation. Back up existing data and plan its migration before moving it.

<a id="en-1-check-the-server-and-access"></a>

### 1. Check the server and access

You need SSH access, a key that does not prompt for a passphrase, a verified host key fingerprint, a domain and DNS access, and access to the GitHub repository settings.

Check resources, architecture and ports on the VPS:

```bash
free -h
df -h
uname -m
sudo ss -lntp
sudo docker ps
sudo -n docker ps
```

The assumed platform is x86_64. Port `3001` must be free. Use the existing Docker, Nginx and Certbot installations. Allow SSH, HTTP and HTTPS through the firewall; do not expose the database or backend.

Before deployment, check whether the server has data to preserve. The preparation script is `ops/prepare-vps.sh`; compare its configuration with the existing installation.

<a id="en-2-configure-github"></a>

### 2. Configure GitHub

Set these values in the `production` environment:

| Type | Name | Value |
| --- | --- | --- |
| Secret | `VPS_HOST` | VPS address |
| Secret | `VPS_USER` | Deployment user |
| Secret | `VPS_SSH_KEY` | Automation private key |
| Secret | `VPS_KNOWN_HOSTS` | Trusted entry matching the SSH host and port |
| Variable | `PUBLIC_URL` | https://your-domain |
| Variable | `APP_PORT` | 3001 |
| Variable | `VPS_SSH_PORT` | 22 or the actual SSH port |

GitHub provides the GHCR token; do not manually create GITHUB_TOKEN. The project name is helpdesk; the frontend listens on 80, the backend on 8080, and `/api/health` checks readiness. CONTAINER_PORT and HEALTH_PATH from single-container examples are not used here.

<a id="en-3-prepare-application-settings"></a>

### 3. Prepare application settings

Create `~/apps/helpdesk/releases`. Give the application directory mode 700 and the private `~/apps/helpdesk/.env` file mode 600. Use the [production template](../../ops/production.env.example); do not overwrite an existing file.

Set the database name, user and PostgreSQL password, production origin, and admin email/password. Generate passwords on the server, for example with `openssl rand -hex 32`, and save them without revealing them in logs. Keep the admin password in a password manager. File permissions do not prevent access by the VPS administrator.

The current Compose stack enforces admin-only seeding. When enabled, seeding creates `admin` and active categories, without user/agent accounts or demo tickets. Changing the password in .env does not change an existing account. Disabling seeding does not remove old demo accounts; deleting them requires a separate operation that accounts for relationships.

<a id="en-4-configure-the-domain-and-https"></a>

### 4. Configure the domain and HTTPS

Add an Nginx block using the [template](../../ops/nginx/helpdesk.conf.example), adjusting the domain and port. Check it with `sudo nginx -t`, then reload Nginx.

Point the DNS A record to the VPS and obtain a certificate through the existing Certbot. Check renewal. The frontend binds only to `127.0.0.1`; Nginx exposes it through HTTPS. PostgreSQL and the backend publish no ports.

<a id="en-5-deploy-through-ci"></a>

### 5. Deploy through CI

The `.github/workflows/ci-cd.yml` workflow performs:

1. Application tests.
2. Backend and frontend image builds.
3. A Compose test of those images with an empty PostgreSQL database.
4. Publication of the same images to GHCR with a SHA tag.
5. SSH deployment, application readiness checks and HTTPS checks.

Pull requests test images without publishing or deploying them. `ops/deploy.sh` handles deployment.

<a id="en-migrations-and-rollback"></a>

### Migrations and rollback

The `prod` profile enables Flyway and Hibernate validate, without automatic schema updates. Migration V1 targets a **new database**. Do not enable baseline on an old database without comparing schemas.

Releases are stored in `releases/SHA`, and `current` points to the last successful one. The script uses flock to prevent concurrent deployments. A pull failure leaves the running version intact; a startup failure triggers an attempt to restore the previous images.

`release.env` stores the release images and port. Rollback **does not revert the database or shared .env**. A public DNS/HTTPS failure does not roll back containers.

For manual rollback, run these commands in Bash as the deployment user:

```bash
cd ~/apps/helpdesk
old_sha=PELNE_SHA_POPRZEDNIEGO_WYDANIA
(
  flock -w 600 9
  sudo -n docker compose -p helpdesk --env-file .env \
    --env-file "releases/$old_sha/release.env" \
    -f "releases/$old_sha/compose.prod.yml" \
    up -d --pull never --wait --wait-timeout 240 && \
    ln -sfn "$PWD/releases/$old_sha" current
) 9>deploy.lock
```

<a id="en-backups-and-data-restoration"></a>

### Backups and data restoration

```bash
bash ~/apps/helpdesk/current/ops/backup.sh
```

The script saves a PostgreSQL dump and attachment archive in one backups directory. It stops the backend while copying, causing a brief interruption. Schedule backups, define retention and keep an encrypted copy off the VPS.

Test restoration in an isolated installation:

1. Restore `database.dump` to PostgreSQL with `pg_restore`.
2. Extract `attachments.tar.gz` into the `backend_uploads` volume.
3. Check login, tickets and an old attachment download.

Do not use `down -v` in production: it deletes data volumes.

<a id="en-post-deployment-checks"></a>

### Post-deployment checks

- HTTPS, certificate renewal and `/api/health`.
- Admin login/logout and creation of user/agent accounts.
- Ticket creation and handling, denial of access to others' tickets, uploads and downloads.
- Backend logs, page headers and no public database/backend ports.
- Backups and a successful restoration test.

Compose memory limits (512/768/128 MB) are starting values; adjust them using measurements. Logs rotate at 10 MB, keeping three files. Monitor HTTPS, readiness, free disk space and the time since the last backup. Other security limitations are in the [audit](security-audit.md#english).

[Back to the Polish version](#polski)

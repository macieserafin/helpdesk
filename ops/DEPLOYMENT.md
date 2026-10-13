# Helpdesk: wdrożenie na VPS

Docelowy proces: testy -> budowa obu obrazów -> test Compose z pustą bazą
-> publikacja tych samych obrazów GHCR z SHA -> SSH -> healthcheck -> HTTPS.
Pull request testuje także obrazy, ale nie publikuje ani nie wdraża.

Używaj `compose.prod.yml` i katalogu `~/apps/helpdesk`. Starszy
`docker-compose.prod.yml` z Caddy to odrębny wariant; nie uruchamiaj obu
zestawów dla tej samej instalacji. Nowy zestaw nie przejmuje starych wolumenów.
Istniejące dane wymagają backupu i zaplanowanego przeniesienia przed wdrożeniem.

## Dane i dostęp potrzebne do uruchomienia

- IP VPS, użytkownik SSH, port i lokalny klucz lub działająca sesja administracyjna.
- Zaufany fingerprint klucza hosta SSH; wpis do known_hosts odpowiada temu samemu portowi.
- Domena i dostęp do jej DNS, e-mail administratora/certyfikatu.
- Potwierdzenie istniejących danych helpdesku i sposobu ich zachowania.
- Dostęp do ustawień `macieserafin/helpdesk` w GitHubie.
- Zasoby i wolny port: sprawdzić `free -h`, `df -h`, `sudo ss -lntp`,
  `sudo docker ps`, `sudo -n docker ps`, `uname -m`.

Zakładana platforma to x86_64. Port domyślny 3001 musi być wolny.
Nginx i Certbot na istniejącym VPS pozostają wspólną infrastrukturą.
Nie instaluj drugiego Dockera obok istniejącego.

## GitHub

Środowisko `production`, sekrety: `VPS_HOST`, `VPS_USER`, `VPS_SSH_KEY`,
`VPS_KNOWN_HOSTS`. Klucz automatyzacji musi działać bez pytania o passphrase.
Zmienne: `PUBLIC_URL=https://twoja-domena`, `APP_PORT=3001`, `VPS_SSH_PORT=22`.
Nazwa projektu to helpdesk, frontend słucha w kontenerze na 80, backend na 8080,
endpoint gotowości to `/api/health`. Te parametry wynikają ze stosu projektu.
Nie ustawiaj dodatkowych `CONTAINER_PORT` lub `HEALTH_PATH` z jednokontenerowego
przykładu PDF. Token GHCR zapewnia GitHub; nie potrzeba ręcznie tworzyć GITHUB_TOKEN.

## Serwer

Utwórz `~/apps/helpdesk/releases`, katalog aplikacji z prawami 700, prywatny
`~/apps/helpdesk/.env` z prawami 600 według `ops/production.env.example`.
Nie nadpisuj istniejącego pliku. Hasła generuj na serwerze przez `openssl rand -hex 32`
i zapisuj bez wypisywania ich w logach. Hasło admina przechowuj w menedżerze haseł.
Nie wysyłaj go do GitHuba ani do czatu. Administrator VPS ma techniczny dostęp
do pliku środowiska; praw 600 nie należy traktować jako ochrony przed root.

Produkcja ustawia admin-only i tworzy tylko konto `admin`, bez user/agent i zgłoszeń
demo. E-mail ustaw przez `APP_DEMO_SEED_ADMIN_EMAIL`. Istniejące konto admina
nie zmienia hasła przy zmianie .env. Istniejące konta demo też nie są kasowane przy
starcie: ich usunięcie wymaga sprawdzenia referencji i osobnej operacji na danych.

Dodaj osobny blok Nginx według `ops/nginx/helpdesk.conf.example`, podmień domenę
i port, sprawdź `sudo nginx -t`, przeładuj Nginx. Skonfiguruj DNS A oraz HTTPS
przez istniejący Certbot i sprawdź odnawianie. PostgreSQL i backend nie publikują
portów, frontend jest dostępny wyłącznie przez 127.0.0.1.

## Migracje i rollback

Flyway V1 dotyczy NOWEJ bazy. Produkcja używa Hibernate validate i nie wykonuje
automatycznej aktualizacji schematu. Nie włączaj baseline dla starej bazy bez
porównania schematu. Lokalny development zachowuje dotychczasowe ORM update.

Wydania: `~/apps/helpdesk/releases/SHA`, ostatnie udane: `current`.
Skrypt blokuje równoczesne wdrożenia przez flock. Nieudane pobranie pozostawia
działającą wersję; nieudany start próbuje odtworzyć poprzednie obrazy.
Metadane portu i obrazów są zapisane w release.env każdego wydania.
Rollback nie cofa bazy ani wspólnego .env. Publiczny błąd DNS/HTTPS nie cofa kontenerów.

Ręczny rollback, jako użytkownik wdrożeniowy:

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

## Backup i odbiór

`bash ~/apps/helpdesk/current/ops/backup.sh` tworzy dump PostgreSQL i archiwum
załączników w jednym katalogu backups. Na czas kopii zatrzymuje backend,
aby uniemożliwić zapis nowych załączników; to powoduje krótką przerwę.
Zaplanuj cron, retencję, zaszyfrowaną kopię poza VPS i próbę odtworzenia osobno.
Odtwarzanie: na izolowanym zestawie PostgreSQL użyj pg_restore database.dump,
odtwórz attachments.tar.gz do wolumenu backend_uploads, sprawdź logowanie,
zgłoszenia i odczyt starego załącznika. Nie używaj `down -v` na produkcji.

Po wdrożeniu sprawdź HTTPS, /api/health, logowanie i wylogowanie admina,
utworzenie użytkownika/agenta, zgłoszenie i załącznik. Limity RAM (512/768/128 MB)
są początkowe; dostosuj do pomiarów VPS. Logi obracają się po 10 MB, trzy pliki.
Dodaj monitoring HTTPS/gotowości, miejsca na dysku i wieku ostatniego backupu.

# Audyt bezpieczeństwa z 13.10.2026 / Security audit of 13 October 2026

[Polski](#polski) | [English](#english)

## Polski

Przegląd obejmował pliki publikowane przez Git, wzorce sekretów w dostępnej historii, CI/CD, logowanie, role, sesje, HTML, załączniki i zależności npm. Dokument opisuje przegląd kodu i wyniki testów regresji z tego dnia. Nie obejmuje pełnego testu penetracyjnego.

<a id="pl-wyniki"></a>

### Wyniki

| Obszar | Wynik przeglądu |
| --- | --- |
| Sekrety | Nie znaleziono prywatnych kluczy ani tokenów w sprawdzonych plikach i wzorcach historii. Skan nie dowodzi braku każdego sekretu. |
| Publikacja | Szablony env, testowe hasła i odwołania secrets.* nie zawierają produkcyjnych sekretów. private-docs jest ignorowany i nie zawiera śledzonych plików. |
| CSRF i role | CSRF włączony także dla logowania, wylogowania i przesyłania plików; rejestracja nadaje USER, a backend dopuszcza tylko trzy role. |
| Sesje i hasła | Kontrola aktywności/roli przy kolejnym żądaniu, BCrypt, silniejsza polityka haseł i cookie produkcyjnych. Szczegóły w [sesjach](../reference/security.md#polski). |
| XSS | Kodowanie znaków specjalnych w tekstach i odznakach; CSP, nosniff, DENY i Referrer-Policy; usunięto zewnętrzny obraz tła. |
| Załączniki | Kontrola dostępu i katalogu także przy usuwaniu; limit 10 MB; pobieranie jako attachment/octet-stream bez zaufania do MIME klienta. |
| Frontend | Aktualizacja Vite do 8.3.4 i środowiska budowania do Node 22; npm zgłosił wtedy 0 znanych podatności. |
| Operacje | Baza i backend w prywatnej sieci, frontend dostępny przez adres lokalny, Flyway z Hibernate validate, osobne wolumeny, rotacja logów i przywracanie poprzednich obrazów. |

Rozszerzono gitignore/dockerignore o prywatną dokumentację, klucze, certyfikaty i dane administratora. Lokalny Compose jawnie włącza tworzenie danych początkowych; przy samodzielnym uruchamianiu backendu mechanizm ten jest domyślnie wyłączony. Pełny lokalny Compose wiąże porty aplikacji z 127.0.0.1.

Hasła produkcyjne, prywatne klucze i prywatne instrukcje dostępu pozostają poza repozytorium. Nie dodawaj ich przez `git add -f`.

<a id="pl-pozostałe-ryzyka"></a>

### Pozostałe ryzyka

- Brak ograniczenia prób logowania/rejestracji; potrzebne ograniczenie liczby żądań w Nginx lub backendzie.
- Zmiana hasła nie unieważnia pozostałych sesji; otwarty strumień SSE działa do upływu własnego limitu czasu.
- Brak skanowania przesyłanych plików, listy dozwolonych formatów i łącznego limitu miejsca na użytkownika.
- Brak pełnego resetu hasła, weryfikacji e-mail i 2FA.
- Tworzenie kopii zapasowych wymaga harmonogramu, ustalenia czasu przechowywania, kopii poza VPS i próby odtworzenia.
- Backend w kontenerze działa jako root; warto dodać dedykowanego użytkownika.
- Nie wykonano pełnego skanu CVE Maven ani testu penetracyjnego. Nagłówki i obrazy trzeba sprawdzić po wdrożeniu.

<a id="pl-weryfikacja-z-dnia-audytu"></a>

### Weryfikacja z dnia audytu

Pomyślnie zakończyły się 34 testy backendu, w tym przepływ CSRF i odebranie dostępu istniejącej sesji po dezaktywacji konta. Pomyślnie zakończyły się również 3 testy frontendu, budowanie aplikacji i walidacja Compose. Pięć testów wdrażania i przywracania wersji zakończyło się wcześniej powodzeniem na Linuxie.

Migrację lokalnie sprawdzano na H2; zadanie images w GitHub Actions sprawdza PostgreSQL i pełny start kontenerów. Kontrolę po publikacji opisuje [lista odbioru wdrożenia](deployment.md#pl-sprawdzenie-po-wdrożeniu).

[Przejdź do wersji angielskiej](#english)

---

## English

[Wróć do wersji polskiej](#polski)

The review covered Git-published files, secret patterns in available history, CI/CD, authentication, roles, sessions, HTML, attachments and npm dependencies. This document records the code review and regression test results from that date. It does not include a full penetration test.

<a id="en-findings"></a>

### Findings

| Area | Review result |
| --- | --- |
| Secrets | No private keys or tokens found in checked files and history patterns. The scan does not prove that every possible secret is absent. |
| Publication | Environment templates, test passwords and secrets.* references contain no production secrets. private-docs is ignored and has no tracked files. |
| CSRF and roles | CSRF enabled for login/logout/uploads; registration grants USER, and the backend permits only three roles. |
| Sessions and passwords | Account/role checks on the next request, BCrypt, stronger password policy and production cookies. See [sessions](../reference/security.md#english). |
| XSS | Text and badge escaping; CSP, nosniff, DENY and Referrer-Policy; external background image removed. |
| Attachments | Access and directory checks, including deletion; 10 MB limit; attachment/octet-stream downloads without trusting client MIME. |
| Frontend | Vite updated to 8.3.4 and build environment to Node 22; npm reported 0 known vulnerabilities at the time. |
| Operations | Private database/backend, frontend on loopback, Flyway with Hibernate validate, separate volumes, log rotation and image rollback. |

gitignore/dockerignore were extended to cover private documentation, keys, certificates and administrator data. Local Compose explicitly enables seeding; standalone backend startup disables it by default. The full local Compose stack binds application ports to 127.0.0.1.

Production passwords, private keys and private access instructions stay outside the repository. Do not add them with `git add -f`.

<a id="en-remaining-risks"></a>

### Remaining risks

- No login/registration rate limiting; add it in Nginx or the backend.
- Password changes do not invalidate other sessions; open SSE streams last until their timeout.
- No upload scanning, allowed-format list or total per-user storage limit.
- No complete password reset, email verification or 2FA.
- Backups need a schedule, retention, an off-VPS copy and a restoration test.
- The backend container runs as root; a dedicated user is desirable.
- No full Maven CVE scan or penetration test was performed. Check headers and images after deployment.

<a id="en-verification-on-the-audit-date"></a>

### Verification on the audit date

34 backend tests passed, including the CSRF flow and revocation of an existing session after account deactivation. Three frontend tests, the build and Compose validation also passed. Five deployment/rollback tests had passed earlier on Linux.

Migration was checked locally on H2; the images job in GitHub Actions checks PostgreSQL and the full stack. See the [post-deployment checklist](deployment.md#en-post-deployment-checks) for checks after publishing.

[Back to the Polish version](#polski)

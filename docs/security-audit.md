# Krótki audyt bezpieczeństwa - 13.10.2026

Zakres: pliki publikowane przez Git, wzorce sekretów w dostępnej historii,
CI/CD, logowanie i role, sesje, wyświetlanie HTML, załączniki i zależności npm.
To przegląd kodu i testy regresji, nie pełny pentest ani certyfikat bezpieczeństwa.
Nie wykonano commita ani pushu.

## Dane przeznaczone do publikacji

Nie znaleziono rzeczywistych prywatnych kluczy ani tokenów dostępowych w
przejrzanych plikach. Skan historii wzorcami kluczy prywatnych i tokenów GitHub
nie wykazał trafień. To nie dowodzi braku każdego możliwego sekretu.
`private-docs` z instrukcją dostępu, publicznym kluczem i known_hosts jest ignorowany;
nie ma śledzonych plików w tym katalogu. Hasło admina i prywatny klucz pozostają
poza repozytorium. Nie kopiuj ich do źródeł ani nie dodawaj przez `git add -f`.

Szablony `.env.example`, `ops/production.env.example`, `ops/ci.env`, testowe hasła,
nazwy zmiennych i odwołania `secrets.*` w workflow można publikować.
Nie są rzeczywistymi sekretami produkcyjnymi. Domena i IP też nie są hasłami,
ale konkretne dane dostępu pozostawiono w prywatnej dokumentacji; publiczny
szablon wcześniejszego wdrożenia otrzymał domenę example.com.

Rozszerzono `.gitignore` i `.dockerignore` o prywatną dokumentację, klucze,
certyfikaty i pliki z danymi administratora. Zwykły start backendu nie uruchamia
już seeda demo domyślnie; lokalny Compose jawnie włącza go na potrzeby developmentu.
Lokalne porty Compose są związane z 127.0.0.1.

## Wyniki i poprawki

| Obszar | Ocena po zmianach |
| --- | --- |
| CSRF | Włączony w backendzie, aktualny token sesji pobierany przez frontend przed zmianą danych, również loginem/logoutem/uploadem. |
| Role | Rejestracja nadaje tylko USER; backend dopuszcza wyłącznie USER, AGENT i ADMIN. |
| Sesje | Wyłączenie konta lub zmiana ról unieważnia dostęp przy następnym żądaniu. Produkcja: Secure, HttpOnly, SameSite=Lax, 30 minut bezczynności. |
| Hasła | BCrypt; tworzenie i zmiana hasła wymagają co najmniej 12 znaków, maks. 72 bajty UTF-8. Testy developmentowe jawnie używają niższego minimum. |
| XSS | Dane tekstowe są escapowane; poprawiono także odznaki ról/statusów. Dodano CSP i nagłówki nosniff, DENY oraz Referrer-Policy. Usunięto zewnętrzny obraz tła. |
| Załączniki | Kontrola właściciela/roli i komentarzy wewnętrznych, limit 10 MB, kontrola katalogu także przy usuwaniu. Pobranie jako attachment/octet-stream, bez zaufania do MIME klienta. |
| Zależności frontendu | Vite zaktualizowany do 8.3.4; npm po aktualizacji zgłosił 0 znanych podatności. Runtime budowania Docker zmieniony na Node 22. |
| Produkcja/DevOps | Prywatna baza i backend, loopback frontendu, osobne wolumeny, Flyway validate, rotacja logów i rollback obrazów. |

CSRF opiera się na [dokumentacji Spring Security](https://docs.spring.io/spring-security/reference/servlet/exploits/csrf.html).

## Pozostałe ryzyka

- Brak ograniczenia prób logowania/rejestracji. Przed szerokim udostępnieniem
  skonfiguruj rate limiting w publicznym Nginx lub backendzie; nie publikuj logów z hasłami.
- Zmiana hasła nie unieważnia automatycznie wszystkich pozostałych sesji. Wyłączenie
  konta lub zmiana ról odbiera dostęp przy kolejnym żądaniu; już otwarty SSE kończy
  się według własnego timeoutu i wysyła wyłącznie identyfikator/zdarzenie zmiany.
- Uploady nie mają antywirusa, listy dozwolonych formatów ani limitu łącznego
  miejsca na użytkownika. Bezpieczne pobieranie nie oznacza, że zawartość pliku jest bezpieczna.
- Brak pełnego resetu hasła, weryfikacji e-mail i 2FA. Nie traktuj istniejących flag
  w modelu danych jako działającego procesu weryfikacji.
- Backup wymaga harmonogramu, retencji, kopii poza VPS oraz próby odtworzenia.
  Backend działa w kontenerze jako root; docelowo warto dodać dedykowanego użytkownika.
- Nie wykonano pełnego skanu CVE zależności Maven ani testu penetracyjnego.
  Nagłówki Nginx i start obrazów wymagają sprawdzenia po pierwszym wdrożeniu.

## Weryfikacja przed pushem

Przeszły 34 testy backendu, w tym realny przepływ CSRF i odebranie dostępu istniejącej
sesji po dezaktywacji konta. Przeszły 3 testy frontendu (escaping, token i blokada
operacji po błędzie pobrania tokenu), build frontendu i walidacja Compose.
Test migracji lokalnie używa H2; PostgreSQL i komplet kontenerów sprawdzi job images
w GitHub Actions. Pięć testów skryptu deploy/rollback przeszło wcześniej na Linuxie.

Po własnym pushu sprawdź workflow, brak publicznych portów bazy/backendu,
logowanie i wylogowanie admina, tworzenie ról, odmowę dostępu do cudzych zgłoszeń,
upload/pobranie pliku oraz nagłówki strony. Nie używaj starszego Compose z Caddy
równolegle z nowym wdrożeniem Nginx.

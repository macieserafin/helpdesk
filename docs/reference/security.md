# Logowanie i sesje / Authentication and sessions

[Polski](#polski) | [English](#english)

## Polski

Aplikacja używa sesji Spring Security i cookie `JSESSIONID`. Frontend przechowuje użytkownika tylko w pamięci, bez hasła lub tokenów w localStorage/sessionStorage.

<a id="pl-przebieg-logowania"></a>

### Przebieg logowania

1. Pobierz `GET /api/auth/csrf`, zachowując cookie sesji.
2. Wyślij `POST /api/auth/login` jako formularz z `loginIdentifier`, `password` i nagłówkiem tokenu CSRF.
3. Po sukcesie backend zwraca `UserResponse`; kolejne wywołania korzystają z cookie.
4. `GET /api/auth/me` odświeża dane użytkownika, m.in. po odświeżeniu strony.
5. `POST /api/auth/logout` unieważnia sesję, czyści cookie i zwraca `204`.

Błędne dane lub brak sesji zwracają `401`. Konto z samą rolą USER może użyć loginu lub e-maila. Agent i administrator używają loginu. Konto z `enabled=false` nie może się zalogować.

Frontend wysyła `credentials: 'include'`. Sesja umożliwia unieważnienie dostępu po stronie serwera i dobrze współpracuje z aplikacją udostępnianą pod jednym adresem.

<a id="pl-csrf"></a>

### CSRF

Ochrona CSRF jest włączona, także dla logowania, rejestracji, wylogowania i uploadu. Operacje zmieniające dane wymagają tokenu także na publicznie dostępnych ścieżkach.

`GET /api/auth/csrf` zwraca `token`, `headerName` i `parameterName`. Przed każdą operacją zmieniającą dane `httpClient.js` pobiera aktualny token i wysyła go w nagłówku wskazanym przez `headerName` (standardowo `X-CSRF-TOKEN`). Zachowuje przy tym cookie. Ponowne pobranie uwzględnia zmianę tokenu po logowaniu i wylogowaniu.

Brak lub niepoprawny token powoduje odrzucenie żądania. Nie wyłączaj CSRF, żeby obejść `403`; sprawdź token i sesję. Mechanizm opisuje [Spring Security](https://docs.spring.io/spring-security/reference/servlet/exploits/csrf.html).

<a id="pl-cors-i-proxy"></a>

### CORS i proxy

Domyślne adresy źródłowe (origin) w `APP_CORS_ALLOWED_ORIGIN_PATTERNS` to `http://localhost:*`, `http://127.0.0.1:*` i `https://macieserafin.pl`. CORS pozwala przesyłać dane uwierzytelniające, w tym cookie sesji. W środowisku produkcyjnym dopuść wyłącznie używane domeny.

Vite i frontendowy Nginx przekazują `/api` do backendu, więc przeglądarka może używać jednego originu. Przy pełnym `VITE_API_BASE_URL` dopasuj CORS do adresu frontendu. Preferuj układ z proxy.

Basic Auth jest domyślnie wyłączony. `APP_SECURITY_BASIC_AUTH_ENABLED=true` służy pomocniczo do pracy lokalnej i testów; interfejs korzysta z sesji.

<a id="pl-ustawienia-produkcyjne"></a>

### Ustawienia produkcyjne

Profil `prod` ustawia cookie Secure, HttpOnly i SameSite=Lax, limit bezczynności sesji 30 minut oraz obsługę nagłówków reverse proxy. Wymaga HTTPS. Konfiguracja jest w `application-prod.properties`.

Hasła są zabezpieczane za pomocą BCrypt. Tworzenie i zmiana hasła wymagają domyślnie 12 znaków i maksymalnie 72 bajtów UTF-8. W konfiguracji testowej minimalna długość jest jawnie obniżona.

Wyłączenie konta lub zmiana ról odbiera dostęp przy następnym żądaniu. Zmiana hasła nie unieważnia wszystkich innych sesji. Otwarty strumień SSE może działać do upływu swojego limitu czasu; wysyła wyłącznie identyfikator i czas zdarzenia, a ponowny odczyt danych podlega autoryzacji.

Pozostałe ryzyka i zakres dotychczasowych testów opisuje [audyt](../operations/security-audit.md#polski). Konfiguracja serwera jest w [wdrożeniu](../operations/deployment.md#polski).

[Przejdź do wersji angielskiej](#english)

---

## English

[Wróć do wersji polskiej](#polski)

The application uses Spring Security sessions and a `JSESSIONID` cookie. The frontend keeps the current user only in memory, without passwords or tokens in localStorage/sessionStorage.

<a id="en-login-flow"></a>

### Login flow

1. Fetch `GET /api/auth/csrf`, keeping the session cookie.
2. Send `POST /api/auth/login` as a form with `loginIdentifier`, `password` and the CSRF token header.
3. On success, the backend returns `UserResponse`; subsequent requests use the cookie.
4. `GET /api/auth/me` refreshes user data, including after a page reload.
5. `POST /api/auth/logout` invalidates the session, clears the cookie and returns `204`.

Invalid credentials or a missing session return `401`. Accounts with only USER can use a login or email. Agents and administrators use their login. An account with `enabled=false` cannot log in.

The frontend sends `credentials: 'include'`. Sessions allow server-side access revocation and work well when the application is served through one address.

<a id="en-csrf"></a>

### CSRF

CSRF protection is enabled for login, registration, logout and uploads. State-changing operations require a token even on public paths.

`GET /api/auth/csrf` returns `token`, `headerName` and `parameterName`. Before each state-changing request, `httpClient.js` fetches a current token and sends it in the header specified by `headerName` (normally `X-CSRF-TOKEN`), keeping the cookie. Fetching it again handles token changes after login and logout.

Missing or invalid tokens cause rejection. If you receive `403`, check the token and session rather than disabling CSRF. See [Spring Security](https://docs.spring.io/spring-security/reference/servlet/exploits/csrf.html) for the mechanism.

<a id="en-cors-and-proxies"></a>

### CORS and proxies

Default origins in `APP_CORS_ALLOWED_ORIGIN_PATTERNS` are `http://localhost:*`, `http://127.0.0.1:*` and `https://macieserafin.pl`. CORS allows credentials, including session cookies. In production, allow only the domains in use.

Vite and frontend Nginx forward `/api` to the backend, allowing the browser to use a single origin. When setting a full `VITE_API_BASE_URL`, configure CORS for the frontend's address. Prefer a proxy setup.

Basic Auth is disabled by default. `APP_SECURITY_BASIC_AUTH_ENABLED=true` is an auxiliary option for development and tests; the browser UI uses sessions.

<a id="en-production-settings"></a>

### Production settings

The `prod` profile sets Secure, HttpOnly and SameSite=Lax cookies, a 30-minute session idle timeout and reverse proxy header handling. It requires HTTPS. Settings are in `application-prod.properties`.

Passwords are hashed with BCrypt. Creating or changing passwords requires at least 12 characters by default and no more than 72 UTF-8 bytes. Tests explicitly lower the minimum.

Disabling an account or changing roles revokes access on the next request. Changing a password does not invalidate all other sessions. An open SSE stream can remain until its timeout; it sends only the ticket identifier and event time. Reading data again requires authorization.

See the [audit](../operations/security-audit.md#english) for remaining risks and previous test coverage, and the [deployment guide](../operations/deployment.md#english) for server configuration.

[Back to the Polish version](#polski)

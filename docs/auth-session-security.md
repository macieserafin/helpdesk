# Logowanie I Bezpieczeństwo Sesji

Aplikacja używa sesji HTTP zarządzanej przez Spring Security. Frontend nie trzyma hasła, tokena Basic ani JWT po zalogowaniu. Aktualny użytkownik jest przechowywany tylko w pamięci aplikacji, a źródłem prawdy pozostaje backend.

## Logowanie

Frontend wysyła `POST /api/auth/login` z polami:

- `loginIdentifier`,
- `password`.

Spring Security sprawdza dane logowania przez `SecurityUserDetailsService` i `UserService.findForAuthentication`.

Po poprawnym logowaniu backend:

- tworzy sesję serwerową,
- ustawia cookie `JSESSIONID`,
- zwraca `UserResponse`,
- nie zwraca JWT.

Po błędnym logowaniu backend zwraca `401` i `ApiErrorResponse`.

## Aktualny Użytkownik

`GET /api/auth/me` zwraca użytkownika powiązanego z aktualną sesją. Frontend używa tego endpointu przy wejściu na chronioną trasę i po odświeżeniu strony.

Jeśli sesja wygasła albo nie istnieje, backend zwraca `401`.

## Wylogowanie

`POST /api/auth/logout` unieważnia sesję, czyści cookie `JSESSIONID` i zwraca `204`.

Endpoint jest dopuszczony bez wcześniejszego zalogowania, żeby UI mogło bezpiecznie wyczyścić stan także po wygaśnięciu sesji.

## Dlaczego Sesja, Nie JWT

Projekt jest aplikacją przeglądarkową, w której frontend i backend mają działać pod wspólną domeną lub za jednym proxy. W takim układzie sesja serwerowa jest prostsza i bezpieczniejsza operacyjnie niż przechowywanie tokena po stronie klienta.

Korzyści:

- logout unieważnia sesję po stronie backendu,
- frontend nie przechowuje sekretu w `localStorage` ani `sessionStorage`,
- mechanizm dobrze pasuje do Spring Security,
- requesty same origin upraszczają konfigurację cookie.

## Cookie JSESSIONID

`JSESSIONID` zawiera identyfikator sesji, a nie dane użytkownika. Przeglądarka dołącza cookie automatycznie, jeśli request idzie z obsługą credentials.

Frontendowy `httpClient.js` ustawia:

```js
credentials: 'include'
```

W produkcji aplikacja powinna działać po HTTPS, a konfiguracja cookie powinna być dopasowana do reverse proxy i domeny.

## CORS

Backend czyta dozwolone originy ze zmiennej:

```text
APP_CORS_ALLOWED_ORIGIN_PATTERNS
```

Domyślnie dopuszczone są lokalne originy `localhost` i `127.0.0.1` oraz domena `https://macieserafin.pl`.

CORS ma `allowCredentials=true`, bo sesja wymaga cookie.

W lokalnym Dockerze frontendowy nginx proxyuje `/api` do backendu, więc przeglądarka widzi jeden origin. W dev serverze Vite requesty `/api` są proxyowane do backendu na `localhost:8080`.

## Basic Auth

Basic Auth jest domyślnie wyłączony:

```text
APP_SECURITY_BASIC_AUTH_ENABLED=false
```

Można go włączyć zmienną środowiskową, ale model aplikacji przeglądarkowej opiera się na sesji. Testy integracyjne sprawdzają, że domyślny tryb nie akceptuje Basic Auth.

## CSRF

CSRF jest obecnie wyłączony w `SecurityConfig`.

To decyzja techniczna na obecnym etapie projektu, ponieważ frontend używa własnych requestów `fetch` i nie ma jeszcze przepływu tokenu CSRF. Przed produkcyjnym utwardzeniem aplikacji trzeba dodać pełny mechanizm:

- endpoint albo cookie wystawiające token,
- odczyt tokenu przez frontend,
- nagłówek CSRF w requestach `POST`, `PATCH`, `PUT` i `DELETE`,
- walidację tokenu w Spring Security.

## Logowanie Przez Email

Konto z samą rolą `USER` może logować się przez `loginIdentifier` albo email.

Konto z rolą `AGENT` lub `ADMIN` loguje się przez `loginIdentifier`. Ta reguła ogranicza przypadkowe logowanie kont staffu przez email, który może być bardziej publiczny.

## Konto Nieaktywne

Jeśli `enabled=false`, Spring Security traktuje konto jako wyłączone i odrzuca logowanie.

Administrator przełącza flagę przez:

```text
PATCH /api/admin/users/{id}/enabled
```

## Testowanie Wielu Ról

Do równoległego testowania sesji używaj osobnych profili przeglądarki:

- profil User: konto `user`,
- profil Agent: konto `agent`,
- profil Admin: konto `admin`.

Każdy profil ma osobny magazyn cookie, więc sesje się nie mieszają.

Scenariusz testowy:

1. Zaloguj konto właściwe dla profilu.
2. Wejdź na dashboard roli.
3. Odśwież stronę i sprawdź, czy sesja pozostaje aktywna.
4. Spróbuj wejść w trasę innej roli.
5. Wyloguj się.
6. Sprawdź, czy chroniona trasa przekierowuje do logowania.

## Ryzyka Do Zamknięcia Przed Produkcją

- dodać CSRF token flow,
- zawęzić CORS do realnych domen,
- ustawić produkcyjne parametry cookie,
- wymusić HTTPS,
- dopracować czas życia sesji,
- dodać reset hasła,
- dodać email verification,
- rozważyć 2FA dla kont staffu.

# Logowanie i bezpieczeństwo sesji

## Czym jest session cookie

Session cookie to ciasteczko przeglądarki, które identyfikuje sesję użytkownika po stronie serwera. W tej aplikacji takim ciasteczkiem jest `JSESSIONID`.

Cookie nie zawiera danych użytkownika, hasła ani ról. Zawiera identyfikator sesji. Backend używa tego identyfikatora, aby znaleźć po swojej stronie informację o zalogowanym użytkowniku.

Przeglądarka wysyła session cookie automatycznie przy kolejnych requestach do backendu. Frontend nie musi samodzielnie przechowywać tokena ani doklejać nagłówka autoryzacyjnego.

## Dlaczego aplikacja webowa może używać sesji zamiast JWT

Ten projekt jest aplikacją przeglądarkową, a frontend i backend mają działać pod tą samą domeną `macieserafin.pl`. Dla takiego modelu sesja serwerowa jest naturalnym wyborem.

JWT przydaje się szczególnie wtedy, gdy wiele niezależnych klientów lub usług musi przenosić podpisane informacje o użytkowniku. W tej aplikacji nie ma aplikacji mobilnej ani potrzeby trzymania tokena po stronie klienta.

Session cookie pozwala:

- trzymać dane sesji po stronie backendu,
- unieważnić sesję na serwerze podczas logoutu,
- nie przechowywać hasła ani tokena Basic w przeglądarce,
- korzystać z wbudowanego mechanizmu sesji Spring Security.

## Jak działa logowanie w tym projekcie

Użytkownik wpisuje identyfikator logowania i hasło w formularzu frontendu. Frontend wysyła te dane do backendu przez `POST /api/auth/login`.

Backend sprawdza dane logowania przez Spring Security. Po poprawnym logowaniu backend tworzy sesję, ustawia cookie `JSESSIONID` i zwraca dane aktualnego użytkownika jako `UserResponse`.

Frontend zapisuje aktualnego użytkownika wyłącznie w stanie aplikacji. Źródłem prawdy pozostaje backend oraz endpoint `GET /api/auth/me`.

## `POST /api/auth/login`

Endpoint `POST /api/auth/login` służy do zalogowania użytkownika.

Request zawiera:

- `loginIdentifier`,
- `password`.

Po poprawnym logowaniu backend:

- tworzy sesję serwerową,
- ustawia cookie `JSESSIONID`,
- zwraca `UserResponse`,
- nie zwraca JWT,
- nie wymaga od frontendu tworzenia nagłówka Basic Auth.

Jeśli login lub hasło są niepoprawne, backend zwraca `401`.

## `GET /api/auth/me`

Endpoint `GET /api/auth/me` zwraca aktualnie zalogowanego użytkownika.

Backend rozpoznaje użytkownika po cookie sesyjnym wysłanym przez przeglądarkę. Frontend używa tego endpointu po odświeżeniu strony i przed wejściem na chronione trasy.

Jeśli sesja jest ważna, backend zwraca `UserResponse`. Jeśli użytkownik nie jest zalogowany albo sesja wygasła, backend zwraca `401`.

## `POST /api/auth/logout`

Endpoint `POST /api/auth/logout` służy do wylogowania użytkownika.

Po poprawnym wylogowaniu backend:

- unieważnia sesję serwerową,
- czyści cookie `JSESSIONID`,
- zwraca odpowiedź sukcesu bez danych.

Po logoutcie kolejne wywołanie `GET /api/auth/me` powinno zwrócić `401`.

## Dlaczego frontend nie przechowuje hasła ani Basic tokena

Hasło użytkownika powinno być użyte tylko w momencie logowania. Po zalogowaniu frontend nie powinien przechowywać hasła ani wartości możliwej do odtworzenia jako `login:password`.

Basic Auth wymaga wysyłania danych uwierzytelniających przy requestach. Jeśli frontend zapisuje Basic token w `sessionStorage`, to w praktyce przechowuje zakodowaną parę login i hasło. To zwiększa ryzyko przy podatnościach XSS i utrudnia bezpieczne wylogowanie.

W modelu sesyjnym frontend przechowuje tylko bieżący stan UI. Uwierzytelnienie jest utrzymywane przez cookie sesyjne i backend.

## Rola cookie `JSESSIONID`

`JSESSIONID` jest identyfikatorem sesji HTTP.

Przeglądarka wysyła je automatycznie razem z requestami, jeśli request jest wykonywany z obsługą credentials. Backend używa tego cookie do odtworzenia kontekstu bezpieczeństwa użytkownika.

Cookie powinno być traktowane jako wrażliwy identyfikator sesji. W produkcji aplikacja powinna działać po HTTPS, a cookie powinno mieć bezpieczną konfigurację odpowiednią dla domeny produkcyjnej.

## Uwierzytelnianie i autoryzacja

Uwierzytelnianie odpowiada na pytanie: kim jest użytkownik. W tym projekcie odbywa się przez login, hasło i sesję.

Autoryzacja odpowiada na pytanie: co ten użytkownik może zrobić. W tym projekcie autoryzacja opiera się na rolach:

- `USER`,
- `AGENT`,
- `ADMIN`.

Użytkownik może być poprawnie zalogowany, ale nadal nie mieć dostępu do danej części aplikacji.

## Role `USER`, `AGENT`, `ADMIN`

Rola `USER` jest przeznaczona dla użytkownika końcowego. Taki użytkownik tworzy zgłoszenia, przegląda własne zgłoszenia i zarządza swoim profilem.

Rola `AGENT` jest przeznaczona dla osoby obsługującej zgłoszenia. Agent widzi kolejkę zgłoszeń i wykonuje operacje związane z obsługą ticketów.

Rola `ADMIN` jest przeznaczona dla administratora. Administrator ma dostęp do sekcji administracyjnych, takich jak zarządzanie użytkownikami, kategoriami i widokami administracyjnymi zgłoszeń.

Backend egzekwuje role w konfiguracji Spring Security i w logice serwisów. Frontend używa ról do ochrony tras i wyboru właściwego widoku startowego.

## Testowanie wielu kont

Nie należy dodawać produkcyjnego przełącznika kont. Testowanie kilku ról powinno odbywać się przez osobne profile przeglądarki.

Zalecany układ:

- Chrome profil Admin: konto `admin`,
- Chrome profil Agent: konto `agent`,
- Chrome profil User: konto `user`.

Każdy profil ma osobny magazyn cookie, więc może mieć osobną sesję. Dzięki temu można równolegle sprawdzać uprawnienia bez mieszania sesji użytkowników.

Podstawowy scenariusz dla każdego profilu:

1. Zalogować się na właściwe konto.
2. Wejść na stronę startową roli.
3. Odświeżyć stronę i sprawdzić, czy użytkownik nadal jest zalogowany.
4. Spróbować wejść w trasę przeznaczoną dla innej roli.
5. Wylogować się.
6. Sprawdzić, czy chroniona trasa przekierowuje do logowania.

## Development i produkcja

W development frontend Vite może działać na innym porcie niż backend. Requesty muszą wysyłać cookie sesyjne, więc klient HTTP powinien używać credentials. Backend musi pozwalać na lokalne originy developmentowe.

W produkcji frontend i backend mają działać pod domeną `macieserafin.pl`. Przy tym założeniu requesty do `/api` mogą działać jako requesty same-origin, co upraszcza obsługę cookie i CORS.

Produkcja powinna używać HTTPS. Konfiguracja cookie powinna być dopasowana do docelowego hostingu, reverse proxy i domeny.

## CSRF

Session cookie jest wysyłane przez przeglądarkę automatycznie. To oznacza, że docelowo aplikacja powinna mieć świadomie zaprojektowaną ochronę CSRF dla operacji zmieniających stan.

Na tym etapie CSRF pozostaje wyłączony, aby nie wprowadzać częściowego mechanizmu, który mógłby zablokować działanie formularzy i requestów frontendu. To jest decyzja tymczasowa.

Docelowy kierunek to dodanie CSRF token flow:

- backend wystawia token CSRF,
- frontend odczytuje token,
- requesty `POST`, `PATCH`, `PUT`, `DELETE` wysyłają token w nagłówku,
- backend odrzuca requesty modyfikujące stan bez poprawnego tokena.

## Przyszłe kroki bezpieczeństwa

Planowane obszary do dopracowania:

- CSRF token flow,
- email verification,
- 2FA,
- password reset,
- bezpieczna konfiguracja cookie na produkcji,
- HTTPS,
- przegląd polityki sesji i czasu wygaśnięcia,
- ograniczenie CORS do realnych originów produkcyjnych.

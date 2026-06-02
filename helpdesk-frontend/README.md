# Helpdesk Frontend

Frontend dla `helpdesk-api`, napisany w czystym JavaScript, HTML i CSS z ES Modules oraz Vite jako dev server.

## Uruchomienie backendu

```powershell
..\helpdesk-api
.\mvnw.cmd spring-boot:run
```

Domyslny adres API: `http://localhost:8080`.

## Uruchomienie frontendu

```powershell
..\helpdesk-frontend
npm install
npm run dev
```

Domyslny adres frontendu: `http://127.0.0.1:5173`.

## Konta testowe

| Rola | Login | Haslo |
| --- | --- | --- |
| USER | `user` | `user123` |
| AGENT | `agent` | `agent123` |
| ADMIN | `admin` | `admin123` |

## Zaimplementowane funkcje

- Logowanie i przekierowanie wedlug roli.
- Dashboard USER, AGENT i ADMIN.
- USER: lista wlasnych ticketow, tworzenie ticketa, szczegoly, komentarze, historia, edycja profilu.
- AGENT: kolejka ticketow, przypisanie do siebie, zmiana statusu, komentarze publiczne i internal, historia.
- ADMIN: uzytkownicy, szczegoly przez wybor z listy, tworzenie, edycja, aktywacja/dezaktywacja, wszystkie tickety, zmiana statusu, internal comments.
- Ukrywanie akcji wedlug roli.
- Komunikaty bledow API i globalny loader.
- Logout.


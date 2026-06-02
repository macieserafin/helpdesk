import { htmlToElement } from '../../utils/dom.js';

export function NotFoundPage() {
  return htmlToElement(`
    <main class="auth-shell">
      <section class="auth-card">
        <div>
          <p class="eyebrow">404</p>
          <h1>Nie znaleziono widoku</h1>
          <p>Wybrana trasa nie istnieje w aplikacji frontendu.</p>
          <a class="button button-primary" href="#/login">Przejdz do logowania</a>
        </div>
      </section>
    </main>
  `);
}

import { login, homeRouteFor } from '../../auth/authService.js';
import { htmlToElement } from '../../utils/dom.js';

export function LoginPage({ navigate, showToast }) {
  const page = htmlToElement(`
    <main class="auth-shell">
      <section class="auth-card">
        <div class="auth-copy">
          <p class="eyebrow">Java 17 + Spring Boot</p>
          <h1>Helpdesk API Console</h1>
          <p>Panel obslugi zgloszen dla uzytkownikow, agentow i administratorow.</p>
        </div>
        <form class="login-form">
          <label>Login
            <input name="username" autocomplete="username" required />
          </label>
          <label>Haslo
            <input name="password" type="password" autocomplete="current-password" required />
          </label>
          <button class="button button-primary" type="submit">Zaloguj</button>
        </form>
      </section>
    </main>
  `);

  const form = page.querySelector('.login-form');

  async function handleLogin(event) {
    event.preventDefault();
    const submit = form.querySelector('[type="submit"]');
    submit.disabled = true;
    submit.textContent = 'Logowanie...';
    try {
      const user = await login(form.elements.username.value, form.elements.password.value);
      showToast(`Zalogowano jako ${user.username}.`, 'success');
      navigate(homeRouteFor(user));
    } catch (error) {
      showToast(error.message, 'error');
    } finally {
      submit.disabled = false;
      submit.textContent = 'Zaloguj';
    }
  }

  form.addEventListener('submit', handleLogin);

  return page;
}

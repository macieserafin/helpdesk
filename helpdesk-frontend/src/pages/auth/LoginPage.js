import { login, homeRouteFor } from '../../auth/authService.js';
import { htmlToElement } from '../../utils/dom.js';
import { getErrorMessage } from '../../utils/errorMessage.js';
import { displayUserName } from '../../utils/userDisplay.js';

export function LoginPage({ navigate, showToast }) {
  const page = htmlToElement(`
    <main class="auth-shell">
      <section class="auth-card">
        <div class="auth-copy">
          <p class="eyebrow">SERVICE DESK PLATFORM</p>
          <h1>Helpdesk Portal</h1>
          <p>Panel obsługi zgłoszeń dla użytkowników, agentów i administratorów.</p>
        </div>
        <form class="login-form">
          <label>Identyfikator logowania lub email
            <input name="loginIdentifier" autocomplete="off" required />
          </label>
          <label>Hasło
            <input name="password" type="password" autocomplete="current-password" required />
          </label>
          <button class="button button-primary" type="submit">Zaloguj</button>
          <p class="auth-switch">Nie masz konta? <a href="#/register">Utwórz konto użytkownika</a></p>
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
      const user = await login(form.elements.loginIdentifier.value, form.elements.password.value);
      showToast(`Zalogowano jako ${displayUserName(user)}.`, 'success');
      navigate(homeRouteFor(user));
    } catch (error) {
      showToast(getErrorMessage(error), 'error');
    } finally {
      submit.disabled = false;
      submit.textContent = 'Zaloguj';
    }
  }

  form.addEventListener('submit', handleLogin);

  return page;
}

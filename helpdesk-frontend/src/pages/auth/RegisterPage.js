import * as authApi from '../../api/authApi.js';
import { FIELD_LIMITS } from '../../utils/constants.js';
import { compactObject, formToObject, htmlToElement } from '../../utils/dom.js';
import { getErrorMessage } from '../../utils/errorMessage.js';

const EMAIL_PATTERN = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

export function RegisterPage({ navigate, showToast }) {
  const page = htmlToElement(`
    <main class="auth-shell">
      <section class="auth-card">
        <div class="auth-copy">
          <p class="eyebrow">NOWE KONTO</p>
          <h1>Rejestracja</h1>
          <p>Utwórz konto użytkownika końcowego do zgłaszania i śledzenia spraw w helpdesku.</p>
        </div>
        <form class="login-form register-form">
          <label>Identyfikator logowania
            <input name="loginIdentifier" minlength="${FIELD_LIMITS.loginIdentifier.min}" maxlength="${FIELD_LIMITS.loginIdentifier.max}" required />
          </label>
          <label>Email
            <input name="email" type="email" maxlength="${FIELD_LIMITS.email.max}" required />
          </label>
          <label>Hasło
            <input name="password" type="password" minlength="${FIELD_LIMITS.password.min}" maxlength="${FIELD_LIMITS.password.max}" autocomplete="new-password" required />
          </label>
          <label>Powtórz hasło
            <input name="confirmPassword" type="password" minlength="${FIELD_LIMITS.password.min}" maxlength="${FIELD_LIMITS.password.max}" autocomplete="new-password" required />
          </label>
          <label>Imię
            <input name="firstName" maxlength="${FIELD_LIMITS.profile.firstName.max}" />
          </label>
          <label>Nazwisko
            <input name="lastName" maxlength="${FIELD_LIMITS.profile.lastName.max}" />
          </label>
          <button class="button button-primary" type="submit">Utwórz konto</button>
          <p class="auth-switch">Masz już konto? <a href="#/login">Przejdź do logowania</a></p>
        </form>
      </section>
    </main>
  `);

  const form = page.querySelector('.register-form');

  async function handleRegister(event) {
    event.preventDefault();
    const submit = form.querySelector('[type="submit"]');
    const originalText = submit.textContent;
    const payload = buildPayload(form);

    try {
      validateRegistration(payload);
    } catch (error) {
      showToast(error.message, 'error');
      return;
    }

    submit.disabled = true;
    submit.textContent = 'Tworzę konto...';
    try {
      await authApi.register(payload);
      showToast('Konto zostało utworzone. Możesz się zalogować.', 'success');
      navigate('/login');
    } catch (error) {
      showToast(getErrorMessage(error), 'error');
    } finally {
      submit.disabled = false;
      submit.textContent = originalText;
    }
  }

  form.addEventListener('submit', handleRegister);

  return page;
}

function buildPayload(form) {
  const data = formToObject(form);
  return compactObject({
    loginIdentifier: data.loginIdentifier?.trim(),
    email: data.email?.trim(),
    password: data.password,
    confirmPassword: data.confirmPassword,
    firstName: data.firstName?.trim(),
    lastName: data.lastName?.trim()
  });
}

function validateRegistration(payload) {
  if (!payload.loginIdentifier) {
    throw new Error('Identyfikator logowania jest wymagany.');
  }
  if (!payload.email) {
    throw new Error('Email jest wymagany.');
  }
  if (!EMAIL_PATTERN.test(payload.email)) {
    throw new Error('Podaj poprawny adres email.');
  }
  if (!payload.password) {
    throw new Error('Hasło jest wymagane.');
  }
  if (!payload.confirmPassword) {
    throw new Error('Powtórzenie hasła jest wymagane.');
  }
  if (payload.password !== payload.confirmPassword) {
    throw new Error('Hasła muszą być takie same.');
  }
}

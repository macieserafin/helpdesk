import { compactObject, escapeHtml, formToObject, htmlToElement } from '../../utils/dom.js';
import { FIELD_LIMITS } from '../../utils/constants.js';
import { normalizeRoles } from '../../utils/validators.js';
import { userLoginIdentifier } from '../../utils/userDisplay.js';

export function UserForm({ user = null, mode = 'create', onSubmit }) {
  const profile = user?.profile || {};
  const form = htmlToElement(`
    <form class="card form-grid">
      <h2 class="span-2">${mode === 'edit' ? 'Edycja uzytkownika' : 'Nowy uzytkownik'}</h2>
      <label>Identyfikator logowania
        <input name="loginIdentifier" minlength="${FIELD_LIMITS.loginIdentifier.min}" maxlength="${FIELD_LIMITS.loginIdentifier.max}" value="${escapeHtml(userLoginIdentifier(user))}" required />
      </label>
      <label>Email
        <input name="email" type="email" maxlength="${FIELD_LIMITS.email.max}" value="${escapeHtml(user?.email || '')}" required />
      </label>
      <label>Haslo
        <input name="password" type="password" minlength="${FIELD_LIMITS.password.min}" maxlength="${FIELD_LIMITS.password.max}" ${mode === 'create' ? 'required' : ''} placeholder="${mode === 'edit' ? 'Pozostaw puste bez zmian' : ''}" />
      </label>
      <label>Role
        <select name="roles" multiple size="3">
          ${['USER', 'AGENT', 'ADMIN'].map((role) => `<option value="${role}" ${user?.roles?.includes(role) ? 'selected' : ''}>${role}</option>`).join('')}
        </select>
      </label>
      <label class="checkbox"><input type="checkbox" name="enabled" ${user?.enabled ?? true ? 'checked' : ''} /> Konto aktywne</label>
      <label>Imie
        <input name="firstName" maxlength="${FIELD_LIMITS.profile.firstName.max}" value="${escapeHtml(profile.firstName || '')}" />
      </label>
      <label>Nazwisko
        <input name="lastName" maxlength="${FIELD_LIMITS.profile.lastName.max}" value="${escapeHtml(profile.lastName || '')}" />
      </label>
      <label>Telefon
        <input name="phoneNumber" maxlength="${FIELD_LIMITS.profile.phoneNumber.max}" value="${escapeHtml(profile.phoneNumber || '')}" />
      </label>
      <label>Miasto
        <input name="city" maxlength="${FIELD_LIMITS.profile.city.max}" value="${escapeHtml(profile.city || '')}" />
      </label>
      <label>Adres
        <input name="streetAddress" maxlength="${FIELD_LIMITS.profile.streetAddress.max}" value="${escapeHtml(profile.streetAddress || '')}" />
      </label>
      <label>Kod pocztowy
        <input name="postalCode" maxlength="${FIELD_LIMITS.profile.postalCode.max}" value="${escapeHtml(profile.postalCode || '')}" />
      </label>
      <div class="form-actions span-2">
        <button class="button button-primary" type="submit">${mode === 'edit' ? 'Zapisz zmiany' : 'Dodaj uzytkownika'}</button>
      </div>
    </form>
  `);

  form.addEventListener('submit', async (event) => {
    event.preventDefault();
    const submit = form.querySelector('[type="submit"]');
    const originalText = submit.textContent;
    const data = formToObject(form);
    const roles = [...form.querySelector('[name="roles"]').selectedOptions].map((option) => option.value);
    const payload = compactObject({
      loginIdentifier: data.loginIdentifier,
      email: data.email,
      password: data.password,
      enabled: form.querySelector('[name="enabled"]').checked,
      roles: normalizeRoles(roles),
      profile: compactObject({
        firstName: data.firstName,
        lastName: data.lastName,
        phoneNumber: data.phoneNumber,
        city: data.city,
        streetAddress: data.streetAddress,
        postalCode: data.postalCode
      })
    });

    submit.disabled = true;
    submit.textContent = 'Zapisuję...';
    try {
      await onSubmit(payload);
    } finally {
      submit.disabled = false;
      submit.textContent = originalText;
    }
  });

  return form;
}

import { compactObject, escapeHtml, formToObject, htmlToElement } from '../../utils/dom.js';
import { normalizeRoles } from '../../utils/validators.js';

export function UserForm({ user = null, mode = 'create', onSubmit }) {
  const profile = user?.profile || {};
  const form = htmlToElement(`
    <form class="card form-grid">
      <h2 class="span-2">${mode === 'edit' ? 'Edycja uzytkownika' : 'Nowy uzytkownik'}</h2>
      <label>Login
        <input name="username" value="${escapeHtml(user?.username || '')}" required />
      </label>
      <label>Email
        <input name="email" type="email" value="${escapeHtml(user?.email || '')}" required />
      </label>
      <label>Haslo
        <input name="password" type="password" ${mode === 'create' ? 'required' : ''} placeholder="${mode === 'edit' ? 'Pozostaw puste bez zmian' : ''}" />
      </label>
      <label>Role
        <select name="roles" multiple size="3">
          ${['USER', 'AGENT', 'ADMIN'].map((role) => `<option value="${role}" ${user?.roles?.includes(role) ? 'selected' : ''}>${role}</option>`).join('')}
        </select>
      </label>
      <label class="checkbox"><input type="checkbox" name="enabled" ${user?.enabled ?? true ? 'checked' : ''} /> Konto aktywne</label>
      <label>Imie
        <input name="firstName" value="${escapeHtml(profile.firstName || '')}" />
      </label>
      <label>Nazwisko
        <input name="lastName" value="${escapeHtml(profile.lastName || '')}" />
      </label>
      <label>Telefon
        <input name="phoneNumber" value="${escapeHtml(profile.phoneNumber || '')}" />
      </label>
      <label>Miasto
        <input name="city" value="${escapeHtml(profile.city || '')}" />
      </label>
      <label>Adres
        <input name="streetAddress" value="${escapeHtml(profile.streetAddress || '')}" />
      </label>
      <label>Kod pocztowy
        <input name="postalCode" value="${escapeHtml(profile.postalCode || '')}" />
      </label>
      <div class="form-actions span-2">
        <button class="button button-primary" type="submit">${mode === 'edit' ? 'Zapisz zmiany' : 'Dodaj uzytkownika'}</button>
      </div>
    </form>
  `);

  form.addEventListener('submit', async (event) => {
    event.preventDefault();
    const data = formToObject(form);
    const roles = [...form.querySelector('[name="roles"]').selectedOptions].map((option) => option.value);
    const payload = compactObject({
      username: data.username,
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

    await onSubmit(payload);
  });

  return form;
}

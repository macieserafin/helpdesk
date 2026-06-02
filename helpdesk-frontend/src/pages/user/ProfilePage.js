import { getMe, updateProfile } from '../../api/userApi.js';
import { PageHeader } from '../../components/common/PageHeader.js';
import { FIELD_LIMITS } from '../../utils/constants.js';
import { compactObject, escapeHtml, formToObject, htmlToElement } from '../../utils/dom.js';
import { getErrorMessage } from '../../utils/errorMessage.js';
import { displayUserName } from '../../utils/userDisplay.js';

export async function ProfilePage({ showToast }) {
  const me = await getMe();
  const profile = me.profile || {};
  const page = htmlToElement(`
    <section class="page stack">
      <div data-header></div>
      <form class="card form-grid">
        <label>Imie <input name="firstName" maxlength="${FIELD_LIMITS.profile.firstName.max}" value="${escapeHtml(profile.firstName || '')}" /></label>
        <label>Nazwisko <input name="lastName" maxlength="${FIELD_LIMITS.profile.lastName.max}" value="${escapeHtml(profile.lastName || '')}" /></label>
        <label>Telefon <input name="phoneNumber" maxlength="${FIELD_LIMITS.profile.phoneNumber.max}" value="${escapeHtml(profile.phoneNumber || '')}" /></label>
        <label>Miasto <input name="city" maxlength="${FIELD_LIMITS.profile.city.max}" value="${escapeHtml(profile.city || '')}" /></label>
        <label>Adres <input name="streetAddress" maxlength="${FIELD_LIMITS.profile.streetAddress.max}" value="${escapeHtml(profile.streetAddress || '')}" /></label>
        <label>Kod pocztowy <input name="postalCode" maxlength="${FIELD_LIMITS.profile.postalCode.max}" value="${escapeHtml(profile.postalCode || '')}" /></label>
        <div class="form-actions span-2">
          <button class="button button-primary" type="submit">Zapisz profil</button>
        </div>
      </form>
    </section>
  `);

  page.querySelector('[data-header]').replaceWith(PageHeader({
    eyebrow: 'Profil',
    title: displayUserName(me),
    description: me.email
  }));

  page.querySelector('form').addEventListener('submit', async (event) => {
    event.preventDefault();
    const form = event.currentTarget;
    const submit = form.querySelector('[type="submit"]');
    const originalText = submit.textContent;
    try {
      submit.disabled = true;
      submit.textContent = 'Zapisuję...';
      await updateProfile(compactObject(formToObject(form)));
      showToast('Profil został zaktualizowany.', 'success');
    } catch (error) {
      showToast(getErrorMessage(error), 'error');
    } finally {
      submit.disabled = false;
      submit.textContent = originalText;
    }
  });

  return page;
}

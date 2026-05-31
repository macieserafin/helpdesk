import { getMe, updateProfile } from '../../api/userApi.js';
import { PageHeader } from '../../components/common/PageHeader.js';
import { compactObject, escapeHtml, formToObject, htmlToElement } from '../../utils/dom.js';
import { displayUserName } from '../../utils/userDisplay.js';

export async function ProfilePage({ showToast }) {
  const me = await getMe();
  const profile = me.profile || {};
  const page = htmlToElement(`
    <section class="page stack">
      <div data-header></div>
      <form class="card form-grid">
        <label>Imie <input name="firstName" value="${escapeHtml(profile.firstName || '')}" /></label>
        <label>Nazwisko <input name="lastName" value="${escapeHtml(profile.lastName || '')}" /></label>
        <label>Telefon <input name="phoneNumber" value="${escapeHtml(profile.phoneNumber || '')}" /></label>
        <label>Miasto <input name="city" value="${escapeHtml(profile.city || '')}" /></label>
        <label>Adres <input name="streetAddress" value="${escapeHtml(profile.streetAddress || '')}" /></label>
        <label>Kod pocztowy <input name="postalCode" value="${escapeHtml(profile.postalCode || '')}" /></label>
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
    try {
      await updateProfile(compactObject(formToObject(event.currentTarget)));
      showToast('Profil zostal zaktualizowany.', 'success');
    } catch (error) {
      showToast(error.message, 'error');
    }
  });

  return page;
}

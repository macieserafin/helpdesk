import { FIELD_LIMITS } from '../../utils/constants.js';
import { escapeHtml, formToObject, htmlToElement } from '../../utils/dom.js';
import { requireFields } from '../../utils/validators.js';

export function TicketEditForm({ ticket, categories = [], onSubmit, onCancel, onError }) {
  const disabled = categories.length === 0;
  const form = htmlToElement(`
    <form class="inline-edit-form">
      <label>Tytul
        <input name="title" maxlength="${FIELD_LIMITS.ticket.title.max}" required value="${escapeHtml(ticket.title)}" />
      </label>
      <label>Kategoria
        <select name="category" required ${disabled ? 'disabled' : ''}>
          <option value="">Wybierz kategorie</option>
          ${categories.map((category) => `
            <option value="${escapeHtml(category.name)}" ${category.name === ticket.category ? 'selected' : ''}>
              ${escapeHtml(category.name)}
            </option>
          `).join('')}
        </select>
      </label>
      <label>Opis
        <textarea name="description" maxlength="${FIELD_LIMITS.ticket.description.max}" rows="6" required>${escapeHtml(ticket.description)}</textarea>
      </label>
      ${disabled ? '<p class="alert alert-warning">Brak aktywnych kategorii. Skontaktuj sie z administratorem.</p>' : ''}
      <div class="form-actions">
        <button class="button button-ghost" type="button" data-cancel-edit>Anuluj</button>
        <button class="button button-primary" type="submit" ${disabled ? 'disabled' : ''}>Zapisz</button>
      </div>
    </form>
  `);

  form.addEventListener('submit', async (event) => {
    event.preventDefault();
    const submit = form.querySelector('[type="submit"]');
    const originalText = submit.textContent;
    const payload = formToObject(form);
    try {
      requireFields(payload, ['title', 'description', 'category']);
      submit.disabled = true;
      submit.textContent = 'Zapisuję...';
      await onSubmit(payload);
    } catch (error) {
      onError?.(error);
    } finally {
      submit.disabled = disabled;
      submit.textContent = originalText;
    }
  });

  form.querySelector('[data-cancel-edit]').addEventListener('click', onCancel);

  return form;
}

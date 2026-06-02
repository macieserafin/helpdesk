import { compactObject, escapeHtml, formToObject, htmlToElement } from '../../utils/dom.js';
import { FIELD_LIMITS } from '../../utils/constants.js';
import { requireFields } from '../../utils/validators.js';

export function CategoryForm({ category = null, onSubmit, onCancel, onError }) {
  const editing = Boolean(category);
  const form = htmlToElement(`
    <form class="card form-grid">
      <h2 class="span-2">${editing ? 'Edycja kategorii' : 'Nowa kategoria'}</h2>
      <label>Nazwa
        <input name="name" maxlength="${FIELD_LIMITS.category.name.max}" required value="${escapeHtml(category?.name || '')}" />
      </label>
      <label class="checkbox">
        <input name="active" type="checkbox" ${category?.active ?? true ? 'checked' : ''} /> Aktywna
      </label>
      <label class="span-2">Opis
        <textarea name="description" maxlength="${FIELD_LIMITS.category.description.max}" rows="4">${escapeHtml(category?.description || '')}</textarea>
      </label>
      <div class="form-actions span-2">
        ${editing ? '<button class="button button-ghost" type="button" data-cancel>Anuluj</button>' : ''}
        <button class="button button-primary" type="submit">${editing ? 'Zapisz zmiany' : 'Dodaj kategorie'}</button>
      </div>
    </form>
  `);

  form.addEventListener('submit', async (event) => {
    event.preventDefault();
    const submit = form.querySelector('[type="submit"]');
    const originalText = submit.textContent;
    const data = formToObject(form);
    try {
      requireFields(data, ['name']);
      submit.disabled = true;
      submit.textContent = 'Zapisuję...';
      await onSubmit(compactObject({
        name: data.name,
        description: data.description,
        active: form.elements.active.checked
      }));
    } catch (error) {
      onError?.(error);
    } finally {
      submit.disabled = false;
      submit.textContent = originalText;
    }
  });

  form.querySelector('[data-cancel]')?.addEventListener('click', () => onCancel?.());

  return form;
}

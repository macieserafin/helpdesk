import { compactObject, escapeHtml, formToObject, htmlToElement } from '../../utils/dom.js';
import { requireFields } from '../../utils/validators.js';

export function CategoryForm({ category = null, onSubmit, onCancel }) {
  const editing = Boolean(category);
  const form = htmlToElement(`
    <form class="card form-grid">
      <h2 class="span-2">${editing ? 'Edycja kategorii' : 'Nowa kategoria'}</h2>
      <label>Nazwa
        <input name="name" maxlength="100" required value="${escapeHtml(category?.name || '')}" />
      </label>
      <label class="checkbox">
        <input name="active" type="checkbox" ${category?.active ?? true ? 'checked' : ''} /> Aktywna
      </label>
      <label class="span-2">Opis
        <textarea name="description" rows="4">${escapeHtml(category?.description || '')}</textarea>
      </label>
      <div class="form-actions span-2">
        ${editing ? '<button class="button button-ghost" type="button" data-cancel>Anuluj</button>' : ''}
        <button class="button button-primary" type="submit">${editing ? 'Zapisz zmiany' : 'Dodaj kategorie'}</button>
      </div>
    </form>
  `);

  form.addEventListener('submit', async (event) => {
    event.preventDefault();
    const data = formToObject(form);
    requireFields(data, ['name']);
    await onSubmit(compactObject({
      name: data.name,
      description: data.description,
      active: form.elements.active.checked
    }));
  });

  form.querySelector('[data-cancel]')?.addEventListener('click', () => onCancel?.());

  return form;
}

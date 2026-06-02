import { createTicket } from '../../api/ticketApi.js';
import { escapeHtml, formToObject, htmlToElement } from '../../utils/dom.js';
import { FIELD_LIMITS } from '../../utils/constants.js';
import { getErrorMessage } from '../../utils/errorMessage.js';
import { requireFields } from '../../utils/validators.js';

export function TicketForm({ categories = [], navigate, showToast }) {
  const disabled = categories.length === 0;
  const form = htmlToElement(`
    <form class="card form-grid">
      <label>Tytul
        <input name="title" maxlength="${FIELD_LIMITS.ticket.title.max}" required placeholder="Np. Problem z logowaniem" />
      </label>
      <label>Kategoria
        <select name="category" required ${disabled ? 'disabled' : ''}>
          <option value="">Wybierz kategorie</option>
          ${categories.map((category) => `<option value="${escapeHtml(category.name)}">${escapeHtml(category.name)}</option>`).join('')}
        </select>
      </label>
      <label class="span-2">Opis
        <textarea name="description" maxlength="${FIELD_LIMITS.ticket.description.max}" rows="7" required placeholder="Opisz problem, kroki i oczekiwany rezultat"></textarea>
      </label>
      ${disabled ? '<p class="alert alert-warning span-2">Brak aktywnych kategorii. Skontaktuj sie z administratorem.</p>' : ''}
      <div class="form-actions span-2">
        <button class="button button-primary" type="submit" ${disabled ? 'disabled' : ''}>Utworz ticket</button>
      </div>
    </form>
  `);

  form.addEventListener('submit', async (event) => {
    event.preventDefault();
    const payload = formToObject(form);
    const submit = form.querySelector('[type="submit"]');
    const originalText = submit.textContent;
    try {
      requireFields(payload, ['title', 'description', 'category']);
      submit.disabled = true;
      submit.textContent = 'Tworzę...';
      const ticket = await createTicket(payload);
      showToast(`Ticket #${ticket.id} został utworzony.`, 'success');
      navigate(`/tickets/${ticket.id}`);
    } catch (error) {
      showToast(getErrorMessage(error), 'error');
    } finally {
      submit.disabled = disabled;
      submit.textContent = originalText;
    }
  });

  return form;
}

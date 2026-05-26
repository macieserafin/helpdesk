import { createTicket } from '../../api/ticketApi.js';
import { DEFAULT_CATEGORIES, TICKET_PRIORITIES } from '../../utils/constants.js';
import { formToObject, htmlToElement } from '../../utils/dom.js';
import { requireFields } from '../../utils/validators.js';

export function TicketForm({ navigate, showToast }) {
  const form = htmlToElement(`
    <form class="card form-grid">
      <label>Tytul
        <input name="title" maxlength="150" required placeholder="Np. Problem z logowaniem" />
      </label>
      <label>Priorytet
        <select name="priority">
          ${TICKET_PRIORITIES.map((priority) => `<option value="${priority}">${priority}</option>`).join('')}
        </select>
      </label>
      <label>Kategoria
        <input name="category" list="category-options" required placeholder="Konto" />
        <datalist id="category-options">
          ${DEFAULT_CATEGORIES.map((category) => `<option value="${category}"></option>`).join('')}
        </datalist>
      </label>
      <label class="span-2">Opis
        <textarea name="description" rows="7" required placeholder="Opisz problem, kroki i oczekiwany rezultat"></textarea>
      </label>
      <div class="form-actions span-2">
        <button class="button button-primary" type="submit">Utworz ticket</button>
      </div>
    </form>
  `);

  form.addEventListener('submit', async (event) => {
    event.preventDefault();
    const payload = formToObject(form);
    try {
      requireFields(payload, ['title', 'description', 'category']);
      const ticket = await createTicket(payload);
      showToast(`Ticket #${ticket.id} zostal utworzony.`, 'success');
      navigate(`/tickets/${ticket.id}`);
    } catch (error) {
      showToast(error.message, 'error');
    }
  });

  return form;
}

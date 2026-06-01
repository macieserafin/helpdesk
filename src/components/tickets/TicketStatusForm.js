import { STATUS_LABELS, TICKET_STATUSES } from '../../utils/constants.js';
import { htmlToElement } from '../../utils/dom.js';

export function TicketStatusForm({ currentStatus, statuses = TICKET_STATUSES, onChange }) {
  const form = htmlToElement(`
    <form class="inline-form">
      <span class="inline-form-label">Zmien status</span>
      <div class="inline-form-row">
        <select name="status">
          ${statuses.map((status) => `<option value="${status}" ${status === currentStatus ? 'selected' : ''}>${STATUS_LABELS[status] || status}</option>`).join('')}
        </select>
        <button class="button button-secondary" type="submit">Zmien</button>
      </div>
    </form>
  `);

  form.addEventListener('submit', async (event) => {
    event.preventDefault();
    await onChange(new FormData(form).get('status'));
  });

  return form;
}

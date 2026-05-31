import { STATUS_LABELS, TICKET_STATUSES } from '../../utils/constants.js';
import { htmlToElement } from '../../utils/dom.js';

export function TicketStatusForm({ currentStatus, onChange }) {
  const form = htmlToElement(`
    <form class="inline-form">
      <select name="status">
        ${TICKET_STATUSES.map((status) => `<option value="${status}" ${status === currentStatus ? 'selected' : ''}>${STATUS_LABELS[status] || status}</option>`).join('')}
      </select>
      <button class="button button-secondary" type="submit">Zmien</button>
    </form>
  `);

  form.addEventListener('submit', async (event) => {
    event.preventDefault();
    await onChange(new FormData(form).get('status'));
  });

  return form;
}

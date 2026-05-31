import { ASSIGNABLE_PRIORITIES, PRIORITY_LABELS } from '../../utils/constants.js';
import { htmlToElement } from '../../utils/dom.js';

export function TicketPriorityForm({ currentPriority, onChange }) {
  const form = htmlToElement(`
    <form class="inline-form">
      <span class="inline-form-label">Zmien priorytet</span>
      <div class="inline-form-row">
        <select name="priority">
          ${ASSIGNABLE_PRIORITIES.map((priority) => `<option value="${priority}" ${priority === currentPriority ? 'selected' : ''}>${PRIORITY_LABELS[priority] || priority}</option>`).join('')}
        </select>
        <button class="button button-secondary" type="submit">Priorytet</button>
      </div>
    </form>
  `);

  form.addEventListener('submit', async (event) => {
    event.preventDefault();
    await onChange(new FormData(form).get('priority'));
  });

  return form;
}

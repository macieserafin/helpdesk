import { ASSIGNABLE_PRIORITIES, PRIORITY_LABELS } from '../../utils/constants.js';
import { htmlToElement } from '../../utils/dom.js';

export function TicketPriorityForm({ currentPriority, priorities = ASSIGNABLE_PRIORITIES, onChange }) {
  const form = htmlToElement(`
    <form class="inline-form">
      <span class="inline-form-label">Zmien priorytet</span>
      <div class="inline-form-row">
        <select name="priority">
          ${priorities.map((priority) => `<option value="${priority}" ${priority === currentPriority ? 'selected' : ''}>${PRIORITY_LABELS[priority] || priority}</option>`).join('')}
        </select>
        <button class="button button-secondary" type="submit">Priorytet</button>
      </div>
    </form>
  `);

  form.addEventListener('submit', async (event) => {
    event.preventDefault();
    const submit = form.querySelector('[type="submit"]');
    const originalText = submit.textContent;
    submit.disabled = true;
    submit.textContent = 'Zmieniam...';
    try {
      await onChange(new FormData(form).get('priority'));
    } finally {
      submit.disabled = false;
      submit.textContent = originalText;
    }
  });

  return form;
}

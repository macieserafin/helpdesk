import { PRIORITY_LABELS, STATUS_LABELS, TICKET_PRIORITIES, TICKET_STATUSES } from '../../utils/constants.js';
import { compactObject, escapeHtml, formToObject, htmlToElement } from '../../utils/dom.js';

function toDateTimeLocal(value) {
  if (!value) {
    return '';
  }

  return String(value).slice(0, 16);
}

function normalizeDateTime(value) {
  if (!value) {
    return '';
  }

  return value.length === 16 ? `${value}:00` : value;
}

export function TicketFilters({
  filters = {},
  categories = [],
  showAgent = false,
  onChange,
  onReset
}) {
  const form = htmlToElement(`
    <form class="card filter-grid ${showAgent ? 'has-agent-filter' : ''}">
      <button class="filter-toggle" type="button" data-filter-toggle aria-expanded="false">
        <span class="hamburger-icon" aria-hidden="true"></span>
        Filtry
      </button>
      <div class="filter-body">
        <label>Status
          <select name="status">
            <option value="">Wszystkie</option>
            ${TICKET_STATUSES.map((status) => `
              <option value="${status}" ${filters.status === status ? 'selected' : ''}>${STATUS_LABELS[status] || status}</option>
            `).join('')}
          </select>
        </label>
        <label>Priorytet
          <select name="priority">
            <option value="">Wszystkie</option>
            ${TICKET_PRIORITIES.map((priority) => `
              <option value="${priority}" ${filters.priority === priority ? 'selected' : ''}>${PRIORITY_LABELS[priority] || priority}</option>
            `).join('')}
          </select>
        </label>
        <label>Kategoria
          <select name="category">
            <option value="">Wszystkie</option>
            ${categories.map((category) => `
              <option value="${escapeHtml(category.name)}" ${filters.category === category.name ? 'selected' : ''}>${escapeHtml(category.name)}${category.active === false ? ' (nieaktywna)' : ''}</option>
            `).join('')}
          </select>
        </label>
        ${showAgent ? `
          <label>Agent
            <input name="agent" value="${escapeHtml(filters.agent || '')}" placeholder="username" />
          </label>
        ` : ''}
        <label>Od
          <input name="createdFrom" type="datetime-local" value="${toDateTimeLocal(filters.createdFrom)}" />
        </label>
        <label>Do
          <input name="createdTo" type="datetime-local" value="${toDateTimeLocal(filters.createdTo)}" />
        </label>
        <div class="filter-actions">
          <button class="button button-primary" type="submit">Filtruj</button>
          <button class="button button-ghost" type="button" data-reset>Wyczysc</button>
        </div>
      </div>
    </form>
  `);

  form.querySelector('[data-filter-toggle]').addEventListener('click', (event) => {
    const expanded = form.classList.toggle('filters-open');
    event.currentTarget.setAttribute('aria-expanded', String(expanded));
  });

  form.addEventListener('submit', (event) => {
    event.preventDefault();
    const data = compactObject(formToObject(form));
    onChange?.({
      ...data,
      createdFrom: normalizeDateTime(data.createdFrom),
      createdTo: normalizeDateTime(data.createdTo)
    });
  });

  form.querySelector('[data-reset]').addEventListener('click', () => {
    form.reset();
    onReset?.();
  });

  return form;
}

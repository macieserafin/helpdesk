import { EmptyState } from '../common/Feedback.js';
import { formatDateTime } from '../../utils/dateFormatter.js';
import { escapeHtml, htmlToElement } from '../../utils/dom.js';

export function CategoryTable({ categories, onSelect, onDeactivate, onActivate }) {
  if (!categories?.length) {
    return EmptyState('Brak kategorii', 'Slownik kategorii jest pusty.');
  }

  const table = htmlToElement(`
    <div class="table-wrap">
      <table>
        <thead>
          <tr>
            <th>ID</th>
            <th>Nazwa</th>
            <th>Opis</th>
            <th>Status</th>
            <th>Utworzono</th>
            <th>Akcje</th>
          </tr>
        </thead>
        <tbody>
          ${categories.map((category) => `
            <tr>
              <td data-label="ID">#${category.id}</td>
              <td data-label="Nazwa"><button class="link-button" data-select="${category.id}">${escapeHtml(category.name)}</button></td>
              <td data-label="Opis">${escapeHtml(category.description || 'Brak opisu')}</td>
              <td data-label="Status"><span class="badge ${category.active ? 'status-open' : 'status-cancelled'}">${category.active ? 'Aktywna' : 'Nieaktywna'}</span></td>
              <td data-label="Utworzono">${formatDateTime(category.createdAt)}</td>
              <td data-label="Akcje">
                <div class="row-actions">
                  <button class="button button-small" type="button" data-select="${category.id}">Edytuj</button>
                  ${category.active ? `
                    <button class="button button-small button-ghost" type="button" data-deactivate="${category.id}">Dezaktywuj</button>
                  ` : `
                    <button class="button button-small button-secondary" type="button" data-activate="${category.id}">Aktywuj</button>
                  `}
                </div>
              </td>
            </tr>
          `).join('')}
        </tbody>
      </table>
    </div>
  `);

  table.querySelectorAll('[data-select]').forEach((button) => {
    button.addEventListener('click', () => onSelect(Number(button.dataset.select)));
  });
  table.querySelectorAll('[data-deactivate]').forEach((button) => {
    button.addEventListener('click', async () => {
      const originalText = button.textContent;
      button.disabled = true;
      button.textContent = 'Dezaktywuję...';
      try {
        await onDeactivate(Number(button.dataset.deactivate));
      } finally {
        button.disabled = false;
        button.textContent = originalText;
      }
    });
  });
  table.querySelectorAll('[data-activate]').forEach((button) => {
    button.addEventListener('click', async () => {
      const originalText = button.textContent;
      button.disabled = true;
      button.textContent = 'Aktywuję...';
      try {
        await onActivate(Number(button.dataset.activate));
      } finally {
        button.disabled = false;
        button.textContent = originalText;
      }
    });
  });

  return table;
}

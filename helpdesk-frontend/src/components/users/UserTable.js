import { RoleBadges } from '../common/Badges.js';
import { EmptyState } from '../common/Feedback.js';
import { escapeHtml, htmlToElement } from '../../utils/dom.js';
import { userLoginIdentifier } from '../../utils/userDisplay.js';

export function UserTable({ users, onSelect, onToggle }) {
  if (!users?.length) {
    return EmptyState('Brak uzytkownikow', 'Lista uzytkownikow jest pusta.');
  }

  const table = htmlToElement(`
    <div class="table-wrap">
      <table>
        <thead>
          <tr>
            <th>ID</th>
            <th>Login</th>
            <th>Email</th>
            <th>Role</th>
            <th>Status</th>
            <th>Akcje</th>
          </tr>
        </thead>
        <tbody>
          ${users.map((user) => `
            <tr>
              <td data-label="ID">#${user.id}</td>
              <td data-label="Login"><button class="link-button" data-select="${user.id}">${escapeHtml(userLoginIdentifier(user))}</button></td>
              <td data-label="Email">${escapeHtml(user.email)}</td>
              <td data-label="Role">${RoleBadges(user.roles)}</td>
              <td data-label="Status"><span class="badge ${user.enabled ? 'status-open' : 'status-cancelled'}">${user.enabled ? 'Aktywny' : 'Nieaktywny'}</span></td>
              <td data-label="Akcje">
                <button class="button button-small" data-toggle="${user.id}" data-enabled="${user.enabled}">
                  ${user.enabled ? 'Dezaktywuj' : 'Aktywuj'}
                </button>
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
  table.querySelectorAll('[data-toggle]').forEach((button) => {
    button.addEventListener('click', async () => {
      const originalText = button.textContent;
      button.disabled = true;
      button.textContent = 'Zmieniam...';
      try {
        await onToggle(Number(button.dataset.toggle), button.dataset.enabled !== 'true');
      } finally {
        button.disabled = false;
        button.textContent = originalText;
      }
    });
  });

  return table;
}

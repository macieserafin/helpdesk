import { navigate } from '../../app/router.js';
import { PriorityBadge, StatusBadge } from '../common/Badges.js';
import { EmptyState } from '../common/Feedback.js';
import { formatDateTime } from '../../utils/dateFormatter.js';
import { escapeHtml, htmlToElement } from '../../utils/dom.js';

export function TicketTable({ tickets, actions = () => '' }) {
  if (!tickets?.length) {
    return EmptyState('Brak ticketow', 'Nie znaleziono zgloszen dla tej sekcji.');
  }

  const table = htmlToElement(`
    <div class="table-wrap">
      <table>
        <thead>
          <tr>
            <th>ID</th>
            <th>Tytul</th>
            <th>Status</th>
            <th>Priorytet</th>
            <th>Kategoria</th>
            <th>Autor</th>
            <th>Agent</th>
            <th>Utworzono</th>
            <th>Akcje</th>
          </tr>
        </thead>
        <tbody>
          ${tickets.map((ticket) => `
            <tr>
              <td>#${ticket.id}</td>
              <td><button class="link-button" data-ticket-id="${ticket.id}">${escapeHtml(ticket.title)}</button></td>
              <td>${StatusBadge(ticket.status)}</td>
              <td>${PriorityBadge(ticket.priority)}</td>
              <td>${escapeHtml(ticket.category)}</td>
              <td>${escapeHtml(ticket.createdBy)}</td>
              <td>${escapeHtml(ticket.assignedTo || 'Nieprzypisany')}</td>
              <td>${formatDateTime(ticket.createdAt)}</td>
              <td><div class="row-actions">${actions(ticket)}</div></td>
            </tr>
          `).join('')}
        </tbody>
      </table>
    </div>
  `);

  table.querySelectorAll('[data-ticket-id]').forEach((button) => {
    button.addEventListener('click', () => navigate(`/tickets/${button.dataset.ticketId}`));
  });

  return table;
}

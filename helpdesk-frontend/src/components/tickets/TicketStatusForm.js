import { ROLES, STATUS_LABELS } from '../../utils/constants.js';
import { htmlToElement } from '../../utils/dom.js';

const ADMIN_TRANSITIONS = {
  OPEN: ['IN_PROGRESS', 'REJECTED', 'CANCELLED'],
  IN_PROGRESS: ['WAITING_FOR_USER', 'RESOLVED', 'REJECTED', 'CANCELLED'],
  WAITING_FOR_USER: ['IN_PROGRESS', 'RESOLVED', 'CANCELLED'],
  RESOLVED: ['CLOSED', 'IN_PROGRESS', 'CANCELLED'],
  CLOSED: [],
  REJECTED: [],
  CANCELLED: []
};

const ACTION_LABELS = {
  IN_PROGRESS: 'Wznow prace',
  WAITING_FOR_USER: 'Czeka na uzytkownika',
  RESOLVED: 'Rozwiaz',
  CLOSED: 'Zamknij ticket',
  REJECTED: 'Odrzuc',
  CANCELLED: 'Anuluj ticket'
};

function hasRole(user, role) {
  return user.roles?.includes(role);
}

function agentActions(status, assignedToCurrentAgent) {
  if (status === 'OPEN') {
    return assignedToCurrentAgent ? ['IN_PROGRESS', 'REJECTED', 'CANCELLED'] : ['REJECTED'];
  }
  if (status === 'IN_PROGRESS') {
    return ['WAITING_FOR_USER', 'RESOLVED', 'REJECTED', 'CANCELLED'];
  }
  if (status === 'WAITING_FOR_USER') {
    return ['IN_PROGRESS', 'RESOLVED', 'CANCELLED'];
  }
  if (status === 'RESOLVED') {
    return ['IN_PROGRESS'];
  }
  return [];
}

function userActions(status, ticket, user) {
  if (ticket.createdBy !== user.username) {
    return [];
  }
  if (['OPEN', 'IN_PROGRESS', 'WAITING_FOR_USER', 'RESOLVED'].includes(status)) {
    return ['CANCELLED'];
  }
  return [];
}

function allowedActions(ticket, user) {
  if (hasRole(user, ROLES.ADMIN)) {
    return ADMIN_TRANSITIONS[ticket.status] || [];
  }
  if (hasRole(user, ROLES.AGENT)) {
    return agentActions(ticket.status, ticket.assignedTo === user.username);
  }
  return userActions(ticket.status, ticket, user);
}

export function TicketStatusForm({ ticket, user, onChange, compact = false }) {
  const actions = allowedActions(ticket, user);
  const panel = htmlToElement(`
    <div class="status-actions ${compact ? 'status-actions-compact' : ''}">
      <span class="inline-form-label">Dostepne akcje statusu</span>
      <div class="row-actions">
        ${actions.map((status) => `
          <button class="button button-secondary ${compact ? 'button-small' : ''}" type="button" data-status="${status}" title="${STATUS_LABELS[status] || status}">
            ${ACTION_LABELS[status] || STATUS_LABELS[status] || status}
          </button>
        `).join('') || '<p class="muted">Brak dostepnych zmian statusu.</p>'}
      </div>
    </div>
  `);

  panel.querySelectorAll('[data-status]').forEach((button) => {
    button.addEventListener('click', async () => {
      await onChange(button.dataset.status);
    });
  });

  return panel;
}

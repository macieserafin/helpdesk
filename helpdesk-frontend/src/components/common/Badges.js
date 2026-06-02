import { PRIORITY_LABELS, STATUS_LABELS } from '../../utils/constants.js';

export function StatusBadge(status) {
  return `<span class="badge status-${String(status || 'unknown').toLowerCase()}">${STATUS_LABELS[status] || status || 'Brak'}</span>`;
}

export function PriorityBadge(priority) {
  return `<span class="badge priority-${String(priority || 'unknown').toLowerCase()}">${PRIORITY_LABELS[priority] || priority || 'Brak'}</span>`;
}

export function RoleBadges(roles = []) {
  return roles.map((role) => `<span class="badge role-badge">${role}</span>`).join('');
}

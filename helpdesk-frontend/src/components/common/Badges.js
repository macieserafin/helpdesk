import { PRIORITY_LABELS, STATUS_LABELS } from '../../utils/constants.js';
import { escapeHtml } from '../../utils/dom.js';

export function StatusBadge(status) {
  return `<span class="badge status-${escapeHtml(String(status || 'unknown').toLowerCase())}">${escapeHtml(STATUS_LABELS[status] || status || 'Brak')}</span>`;
}

export function PriorityBadge(priority) {
  return `<span class="badge priority-${escapeHtml(String(priority || 'unknown').toLowerCase())}">${escapeHtml(PRIORITY_LABELS[priority] || priority || 'Brak')}</span>`;
}

export function RoleBadges(roles = []) {
  return roles.map((role) => `<span class="badge role-badge">${escapeHtml(role)}</span>`).join('');
}

import { hasRole } from '../../auth/authService.js';
import { ROLES } from '../../utils/constants.js';

const NAV = [
  { label: 'Dashboard', path: '/user', roles: [ROLES.USER] },
  { label: 'Moje tickety', path: '/user/tickets', roles: [ROLES.USER] },
  { label: 'Nowy ticket', path: '/user/tickets/new', roles: [ROLES.USER] },
  { label: 'Profil', path: '/user/profile', roles: [ROLES.USER, ROLES.AGENT, ROLES.ADMIN] },
  { label: 'Dashboard', path: '/agent', roles: [ROLES.AGENT] },
  { label: 'Kolejka', path: '/agent/tickets', roles: [ROLES.AGENT] },
  { label: 'Przypisane', path: '/agent/assigned', roles: [ROLES.AGENT] },
  { label: 'Dashboard', path: '/admin', roles: [ROLES.ADMIN] },
  { label: 'Uzytkownicy', path: '/admin/users', roles: [ROLES.ADMIN] },
  { label: 'Wszystkie tickety', path: '/admin/tickets', roles: [ROLES.ADMIN] }
];

export function Sidebar({ user, activePath }) {
  const items = NAV
    .filter((item) => item.roles.some((role) => hasRole(user, role)))
    .map((item) => `
      <a class="side-link ${activePath === item.path ? 'active' : ''}" href="#${item.path}">
        <span class="side-dot"></span>
        ${item.label}
      </a>
    `)
    .join('');

  return `
    <aside class="sidebar">
      <div class="brand">
        <span class="brand-mark">HD</span>
        <div>
          <strong>Helpdesk</strong>
          <small>API Console</small>
        </div>
      </div>
      <nav class="side-nav">${items}</nav>
    </aside>
  `;
}

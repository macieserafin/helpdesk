import { hasRole } from '../../auth/authService.js';
import { ROLES } from '../../utils/constants.js';

const NAV = [
  { label: 'Dashboard', path: '/user', roles: [ROLES.USER] },
  { label: 'Moje tickety', path: '/user/tickets', roles: [ROLES.USER] },
  { label: 'Nowy ticket', path: '/user/tickets/new', roles: [ROLES.USER] },
  { label: 'Dashboard', path: '/agent', roles: [ROLES.AGENT] },
  { label: 'Wszystkie zgłoszenia', path: '/agent/tickets', roles: [ROLES.AGENT] },
  { label: 'Przypisane', path: '/agent/assigned', roles: [ROLES.AGENT] },
  { label: 'Dashboard', path: '/admin', roles: [ROLES.ADMIN] },
  { label: 'Użytkownicy', path: '/admin/users', roles: [ROLES.ADMIN] },
  { label: 'Kategorie', path: '/admin/categories', roles: [ROLES.ADMIN] },
  { label: 'Wszystkie tickety', path: '/admin/tickets', roles: [ROLES.ADMIN] },
  { label: 'Profil', path: '/user/profile', roles: [ROLES.USER, ROLES.AGENT, ROLES.ADMIN] },
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

  queueMicrotask(() => {
    const sidebar = document.querySelector('[data-sidebar]');
    const toggle = sidebar?.querySelector('[data-nav-toggle]');
    if (!sidebar || !toggle || toggle.dataset.bound) {
      return;
    }

    toggle.dataset.bound = 'true';
    toggle.addEventListener('click', () => {
      const open = sidebar.classList.toggle('nav-open');
      toggle.setAttribute('aria-expanded', String(open));
    });
    sidebar.querySelectorAll('.side-link').forEach((link) => {
      link.addEventListener('click', () => {
        sidebar.classList.remove('nav-open');
        toggle.setAttribute('aria-expanded', 'false');
      });
    });
  });

  return `
    <aside class="sidebar" data-sidebar>
      <div class="sidebar-head">
        <div class="brand">
          <span class="brand-mark">HD</span>
          <div>
            <strong>Helpdesk</strong>
            <small>Support Portal</small>
          </div>
        </div>
        <button class="nav-toggle" type="button" data-nav-toggle aria-expanded="false" aria-label="Menu">
          <span class="hamburger-icon" aria-hidden="true"></span>
          Menu
        </button>
      </div>
      <nav class="side-nav">${items}</nav>
    </aside>
  `;
}

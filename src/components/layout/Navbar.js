import { logout } from '../../auth/authService.js';
import { navigate } from '../../app/router.js';
import { ROLE_LABELS } from '../../utils/constants.js';
import { escapeHtml } from '../../utils/dom.js';
import { displayUserName } from '../../utils/userDisplay.js';

export function Navbar({ user }) {
  const roles = user?.roles?.map((role) => ROLE_LABELS[role] || role).join(', ') || 'Brak roli';
  const userName = displayUserName(user);

  queueMicrotask(() => {
    const button = document.querySelector('[data-logout]');
    if (button && !button.dataset.bound) {
      button.dataset.bound = 'true';
      button.addEventListener('click', async () => {
        await logout();
        navigate('/login');
      });
    }
  });

  return `
    <header class="topbar">
      <div class="global-loader" data-global-loader hidden></div>
      <div class="topbar-user">
        <strong>${escapeHtml(userName)}</strong>
        <span>${escapeHtml(roles)}</span>
      </div>
      <button class="button button-ghost" type="button" data-logout>Wyloguj</button>
    </header>
  `;
}

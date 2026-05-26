import { logout } from '../../auth/authService.js';
import { navigate } from '../../app/router.js';
import { ROLE_LABELS } from '../../utils/constants.js';

export function Navbar({ user }) {
  const roles = user?.roles?.map((role) => ROLE_LABELS[role] || role).join(', ') || 'Brak roli';

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
      <div>
        <strong>${user.username}</strong>
        <span>${roles}</span>
      </div>
      <button class="button button-ghost" type="button" data-logout>Wyloguj</button>
    </header>
  `;
}

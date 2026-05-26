import { requireAuth } from '../auth/authGuard.js';
import { currentUser, homeRouteFor } from '../auth/authService.js';
import { requireRole } from '../auth/roleGuard.js';
import { clearAuthUser } from '../state/authStore.js';
import { ShellLayout } from '../components/layout/ShellLayout.js';
import { ToastHost, showToast } from '../components/common/Toast.js';
import { subscribeUi } from '../state/uiStore.js';
import { htmlToElement, setContent } from '../utils/dom.js';
import { currentPath, matchRoute, navigate } from './router.js';

const root = document.querySelector('#app');

export function startApp() {
  root.append(ToastHost());
  window.addEventListener('hashchange', render);
  render();
}

async function render() {
  const path = currentPath();
  const route = matchRoute(path);

  try {
    if (route.public && path === '/login' && currentUser()) {
      navigate(homeRouteFor(currentUser()));
      return;
    }

    let user = currentUser();
    if (!route.public) {
      user = await requireAuth();
      if (!requireRole(user, route.roles || [])) {
        navigate(homeRouteFor(user));
        showToast('Brak dostepu do wybranej sekcji.', 'warning');
        return;
      }
    }

    const page = await route.page({ params: route.params, user, navigate, showToast });
    const view = route.public ? page : ShellLayout({ user, content: page, activePath: path });
    setContent(root, view);
    root.append(ToastHost());
    bindGlobalLoading();
  } catch (error) {
    if (!route.public) {
      clearAuthUser();
      navigate('/login');
      showToast('Sesja wygasla albo wymagane jest logowanie.', 'warning');
      return;
    }

    setContent(root, htmlToElement(`<main class="auth-shell"><section class="auth-card"><h1>Blad</h1><p>${error.message}</p></section></main>`));
  }
}

function bindGlobalLoading() {
  const bar = document.querySelector('[data-global-loader]');
  if (!bar || bar.dataset.bound) {
    return;
  }

  bar.dataset.bound = 'true';
  subscribeUi(({ loading }) => {
    bar.hidden = !loading;
  });
}

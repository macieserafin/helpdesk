import { requireAuth } from '../auth/authGuard.js';
import { currentUser, homeRouteFor } from '../auth/authService.js';
import { requireRole } from '../auth/roleGuard.js';
import { ApiError } from '../api/httpClient.js';
import { clearAuthUser } from '../state/authStore.js';
import { ErrorState, LoadingState } from '../components/common/Feedback.js';
import { ShellLayout } from '../components/layout/ShellLayout.js';
import { ToastHost, showToast } from '../components/common/Toast.js';
import { subscribeUi } from '../state/uiStore.js';
import { escapeHtml, htmlToElement, setContent } from '../utils/dom.js';
import { getErrorMessage } from '../utils/errorMessage.js';
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
    if (route.public && ['/login', '/register'].includes(path) && currentUser()) {
      navigate(homeRouteFor(currentUser()));
      return;
    }

    let user = currentUser();
    if (!route.public) {
      try {
        user = await requireAuth();
      } catch (error) {
        clearAuthUser();
        navigate('/login');
        showToast('Sesja wygasła albo wymagane jest logowanie.', 'warning');
        return;
      }
      if (!requireRole(user, route.roles || [])) {
        navigate(homeRouteFor(user));
        showToast('Brak dostępu do wybranej sekcji.', 'warning');
        return;
      }

      setContent(root, ShellLayout({
        user,
        content: LoadingState('Ładowanie widoku...'),
        activePath: path
      }));
      root.append(ToastHost());
      bindGlobalLoading();
    }

    const page = await route.page({ params: route.params, user, navigate, showToast });
    const view = route.public ? page : ShellLayout({ user, content: page, activePath: path });
    setContent(root, view);
    root.append(ToastHost());
    bindGlobalLoading();
  } catch (error) {
    if (!route.public) {
      const message = error instanceof ApiError
        ? getErrorMessage(error)
        : getErrorMessage(error, 'Nie udało się załadować widoku. Spróbuj odświeżyć stronę.');
      const user = currentUser();
      showToast(message, 'error');
      if (user) {
        setContent(root, ShellLayout({
          user,
          content: ErrorState(message),
          activePath: path
        }));
        root.append(ToastHost());
        bindGlobalLoading();
      }
      return;
    }

    setContent(root, htmlToElement(`<main class="auth-shell"><section class="auth-card"><h1>Błąd</h1><p>${escapeHtml(getErrorMessage(error))}</p></section></main>`));
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

import { htmlToElement } from '../../utils/dom.js';

let host;

export function ToastHost() {
  if (!host) {
    host = htmlToElement('<div class="toast-host" aria-live="polite"></div>');
  }
  return host;
}

export function showToast(message, tone = 'info') {
  const container = ToastHost();
  const toast = htmlToElement(`<div class="toast toast-${tone}">${message}</div>`);
  container.append(toast);
  window.setTimeout(() => toast.remove(), 4200);
}

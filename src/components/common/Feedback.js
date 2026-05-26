import { htmlToElement } from '../../utils/dom.js';

export function LoadingState(message = 'Ladowanie danych...') {
  return htmlToElement(`
    <div class="state-panel">
      <span class="spinner"></span>
      <p>${message}</p>
    </div>
  `);
}

export function EmptyState(title, detail) {
  return htmlToElement(`
    <div class="empty-state">
      <h3>${title}</h3>
      <p>${detail}</p>
    </div>
  `);
}

export function ErrorState(message) {
  return htmlToElement(`
    <div class="alert alert-error">${message}</div>
  `);
}

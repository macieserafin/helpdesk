import { escapeHtml, htmlToElement } from '../../utils/dom.js';

export function confirmAction({
  title = 'Potwierdź operację',
  message,
  confirmText = 'Potwierdź',
  cancelText = 'Anuluj',
  tone = 'danger'
}) {
  return new Promise((resolve) => {
    const dialog = htmlToElement(`
      <div class="modal-backdrop" role="presentation">
        <section class="confirm-dialog" role="dialog" aria-modal="true" aria-labelledby="confirm-title">
          <h2 id="confirm-title">${escapeHtml(title)}</h2>
          <p>${escapeHtml(message || 'Czy na pewno chcesz kontynuować?')}</p>
          <div class="form-actions">
            <button class="button button-ghost" type="button" data-cancel>${escapeHtml(cancelText)}</button>
            <button class="button ${tone === 'danger' ? 'button-danger' : 'button-primary'}" type="button" data-confirm>
              ${escapeHtml(confirmText)}
            </button>
          </div>
        </section>
      </div>
    `);

    const cleanup = (result) => {
      document.removeEventListener('keydown', onKeydown);
      dialog.remove();
      resolve(result);
    };

    function onKeydown(event) {
      if (event.key === 'Escape') {
        cleanup(false);
      }
    }

    dialog.querySelector('[data-cancel]').addEventListener('click', () => cleanup(false));
    dialog.querySelector('[data-confirm]').addEventListener('click', () => cleanup(true));
    dialog.addEventListener('click', (event) => {
      if (event.target === dialog) {
        cleanup(false);
      }
    });

    document.addEventListener('keydown', onKeydown);
    document.body.append(dialog);
    dialog.querySelector('[data-confirm]').focus();
  });
}

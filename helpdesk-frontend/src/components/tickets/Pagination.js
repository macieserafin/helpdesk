import { htmlToElement } from '../../utils/dom.js';

export function Pagination({ page, onPageChange }) {
  if (!page || page.totalPages <= 1) {
    return htmlToElement('<div class="pagination pagination-empty"></div>');
  }

  const current = page.page + 1;
  const total = page.totalPages;
  const node = htmlToElement(`
    <nav class="pagination" aria-label="Paginacja">
      <span>Strona ${current} z ${total} | ${page.totalElements} wyników</span>
      <div class="row-actions">
        <button class="button button-secondary" type="button" data-prev ${page.first ? 'disabled' : ''}>Poprzednia</button>
        <button class="button button-secondary" type="button" data-next ${page.last ? 'disabled' : ''}>Następna</button>
      </div>
    </nav>
  `);

  node.querySelectorAll('button').forEach((button) => {
    button.addEventListener('click', async () => {
      const nextPage = button.dataset.prev !== undefined ? page.page - 1 : page.page + 1;
      button.disabled = true;
      try {
        await onPageChange(nextPage);
      } finally {
        button.disabled = false;
      }
    });
  });

  return node;
}

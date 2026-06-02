import { htmlToElement } from '../../utils/dom.js';

export function Pagination({ page, onPageChange }) {
  if (!page || page.totalPages <= 1) {
    return htmlToElement('<div class="pagination pagination-empty"></div>');
  }

  const current = page.page + 1;
  const total = page.totalPages;
  const node = htmlToElement(`
    <nav class="pagination" aria-label="Paginacja">
      <span>Strona ${current} z ${total} | ${page.totalElements} wynikow</span>
      <div class="row-actions">
        <button class="button button-secondary" type="button" data-prev ${page.first ? 'disabled' : ''}>Poprzednia</button>
        <button class="button button-secondary" type="button" data-next ${page.last ? 'disabled' : ''}>Nastepna</button>
      </div>
    </nav>
  `);

  node.querySelector('[data-prev]').addEventListener('click', () => onPageChange(page.page - 1));
  node.querySelector('[data-next]').addEventListener('click', () => onPageChange(page.page + 1));

  return node;
}

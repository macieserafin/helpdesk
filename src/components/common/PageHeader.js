import { htmlToElement } from '../../utils/dom.js';

export function PageHeader({ eyebrow, title, description, action = '' }) {
  return htmlToElement(`
    <div class="page-header">
      <div>
        <p class="eyebrow">${eyebrow || 'Helpdesk'}</p>
        <h1>${title}</h1>
        ${description ? `<p>${description}</p>` : ''}
      </div>
      <div class="page-actions">${action}</div>
    </div>
  `);
}

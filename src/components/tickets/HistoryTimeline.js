import { formatDateTime } from '../../utils/dateFormatter.js';
import { escapeHtml, htmlToElement } from '../../utils/dom.js';

export function HistoryTimeline({ history }) {
  return htmlToElement(`
    <section class="card stack">
      <div class="section-title">
        <h2>Historia</h2>
        <span>${history.length}</span>
      </div>
      <div class="timeline">
        ${history.map((item) => `
          <article class="timeline-item">
            <strong>${escapeHtml(item.actionType)}</strong>
            <p>${escapeHtml(item.note || 'Aktualizacja ticketa')}</p>
            <small>${escapeHtml(item.changedBy)} | ${formatDateTime(item.changedAt)}</small>
            ${item.oldStatus || item.newStatus ? `<small>Status: ${item.oldStatus || '-'} -> ${item.newStatus || '-'}</small>` : ''}
            ${item.oldAssignedTo || item.newAssignedTo ? `<small>Agent: ${item.oldAssignedTo || '-'} -> ${item.newAssignedTo || '-'}</small>` : ''}
          </article>
        `).join('') || '<p class="muted">Historia jest pusta.</p>'}
      </div>
    </section>
  `);
}

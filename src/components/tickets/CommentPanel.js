import { addComment } from '../../api/ticketApi.js';
import { formatDateTime } from '../../utils/dateFormatter.js';
import { escapeHtml, htmlToElement } from '../../utils/dom.js';
import { uploadTicketAttachment } from './AttachmentPanel.js';

export function CommentPanel({ ticketId, comments, staff, showToast, onSaved }) {
  const panel = htmlToElement(`
    <section class="card stack">
      <div class="section-title">
        <h2>Komentarze</h2>
        <span>${comments.length}</span>
      </div>
      <div class="comment-list">
        ${comments.map((comment) => `
          <article class="comment ${comment.internal ? 'comment-internal' : ''}">
            <div>
              <strong>${escapeHtml(comment.author)}</strong>
              <span>${formatDateTime(comment.createdAt)}</span>
              ${comment.internal ? '<span class="badge internal-badge">Internal</span>' : ''}
            </div>
            <p>${escapeHtml(comment.content)}</p>
            ${staff || !comment.internal ? `
              <form class="comment-attachment-form" data-comment-upload="${comment.id}">
                <input name="file" type="file" aria-label="Zalacznik do komentarza #${comment.id}" />
                <button class="button button-small button-secondary" type="submit">Dodaj plik</button>
              </form>
            ` : ''}
          </article>
        `).join('') || '<p class="muted">Brak komentarzy.</p>'}
      </div>
      <form class="comment-form">
        <label>Nowy komentarz
          <textarea name="content" rows="4" required></textarea>
        </label>
        ${staff ? '<label class="checkbox"><input type="checkbox" name="internal" /> Komentarz wewnetrzny</label>' : ''}
        <button class="button button-primary" type="submit">Dodaj komentarz</button>
      </form>
    </section>
  `);

  panel.querySelector('.comment-form').addEventListener('submit', async (event) => {
    event.preventDefault();
    const data = new FormData(event.currentTarget);
    try {
      await addComment(ticketId, {
        content: String(data.get('content') || '').trim(),
        internal: data.get('internal') === 'on'
      });
      showToast('Komentarz zostal dodany.', 'success');
      await onSaved();
    } catch (error) {
      showToast(error.message, 'error');
    }
  });

  panel.querySelectorAll('[data-comment-upload]').forEach((form) => {
    form.addEventListener('submit', async (event) => {
      event.preventDefault();
      try {
        await uploadTicketAttachment({
          ticketId,
          commentId: form.dataset.commentUpload,
          file: form.elements.file.files[0]
        });
        showToast('Zalacznik zostal dodany do komentarza.', 'success');
        await onSaved();
      } catch (error) {
        showToast(error.message, 'error');
      }
    });
  });

  return panel;
}

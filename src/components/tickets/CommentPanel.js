import { addComment } from '../../api/ticketApi.js';
import * as attachmentApi from '../../api/attachmentApi.js';
import { formatDateTime } from '../../utils/dateFormatter.js';
import { FIELD_LIMITS } from '../../utils/constants.js';
import { escapeHtml, htmlToElement } from '../../utils/dom.js';
import { formatFileSize } from '../../utils/fileFormatter.js';
import { uploadTicketAttachment, validateAttachmentFile } from './attachmentUpload.js';

function renderAttachment(attachment, user) {
  const canDelete = attachment.uploadedBy === user.username || user.roles?.includes('ADMIN');

  return `
    <article class="comment-attachment-item">
      <div>
        <strong>${escapeHtml(attachment.fileName)}</strong>
        <span>${formatFileSize(attachment.fileSize)} | ${escapeHtml(attachment.uploadedBy)} | ${formatDateTime(attachment.uploadedAt)}</span>
      </div>
      <div class="row-actions">
        <button class="button button-small button-secondary" type="button" data-download-attachment="${attachment.id}">Pobierz</button>
        ${canDelete ? `
          <button class="button button-small button-ghost" type="button" data-delete-attachment="${attachment.id}">Usun</button>
        ` : ''}
      </div>
    </article>
  `;
}

function renderCommentAttachments(commentAttachments, user) {
  if (!commentAttachments.length) {
    return '';
  }

  return `
    <div class="comment-attachments">
      ${commentAttachments.map((attachment) => renderAttachment(attachment, user)).join('')}
    </div>
  `;
}

function renderFileSummary(files) {
  if (!files.length) {
    return 'Nie wybrano plikow.';
  }

  return files
    .map((file) => `${escapeHtml(file.name)} (${formatFileSize(file.size)})`)
    .join('<br>');
}

export function CommentPanel({ ticketId, comments, attachments = [], user, staff, showToast, onSaved }) {
  const attachmentsByCommentId = attachments.reduce((grouped, attachment) => {
    const key = attachment.commentId == null ? 'unassigned' : String(attachment.commentId);
    grouped.set(key, [...(grouped.get(key) || []), attachment]);
    return grouped;
  }, new Map());
  const unassignedAttachments = attachmentsByCommentId.get('unassigned') || [];

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
            ${renderCommentAttachments(attachmentsByCommentId.get(String(comment.id)) || [], user)}
          </article>
        `).join('') || '<p class="muted">Brak komentarzy.</p>'}
        ${unassignedAttachments.length ? `
          <article class="comment comment-attachments-only">
            <div><strong>Zalaczniki bez komentarza</strong></div>
            ${renderCommentAttachments(unassignedAttachments, user)}
          </article>
        ` : ''}
      </div>
      <form class="comment-form">
        <label>Nowy komentarz
          <textarea name="content" maxlength="${FIELD_LIMITS.comment.content.max}" rows="4" required></textarea>
        </label>
        <label>Zalaczniki do komentarza
          <input name="attachments" type="file" multiple />
          <span class="file-summary" data-file-summary>Nie wybrano plikow.</span>
        </label>
        ${staff ? '<label class="checkbox"><input type="checkbox" name="internal" /> Komentarz wewnetrzny</label>' : ''}
        <button class="button button-primary" type="submit">Dodaj komentarz</button>
      </form>
    </section>
  `);

  const commentForm = panel.querySelector('.comment-form');
  const fileInput = commentForm.elements.attachments;
  const fileSummary = panel.querySelector('[data-file-summary]');

  fileInput.addEventListener('change', () => {
    fileSummary.innerHTML = renderFileSummary([...fileInput.files]);
  });

  commentForm.addEventListener('submit', async (event) => {
    event.preventDefault();
    const form = event.currentTarget;
    const data = new FormData(form);
    const files = [...fileInput.files];
    const submitButton = form.querySelector('button[type="submit"]');
    let commentCreated = false;

    try {
      files.forEach(validateAttachmentFile);
      submitButton.disabled = true;

      const savedComment = await addComment(ticketId, {
        content: String(data.get('content') || '').trim(),
        internal: data.get('internal') === 'on'
      });
      commentCreated = true;

      for (const file of files) {
        await uploadTicketAttachment({
          ticketId,
          commentId: savedComment.id,
          file
        });
      }

      showToast(files.length ? 'Komentarz i zalaczniki zostaly dodane.' : 'Komentarz zostal dodany.', 'success');
      form.reset();
      fileSummary.textContent = 'Nie wybrano plikow.';
      await onSaved();
    } catch (error) {
      showToast(commentCreated ? `Komentarz zostal dodany, ale zalacznik nie zostal wyslany: ${error.message}` : error.message, 'error');
      if (commentCreated) {
        await onSaved();
      }
    } finally {
      submitButton.disabled = false;
    }
  });

  panel.querySelectorAll('[data-download-attachment]').forEach((button) => {
    button.addEventListener('click', async () => {
      const attachment = attachments.find((item) => String(item.id) === button.dataset.downloadAttachment);
      try {
        await attachmentApi.downloadAttachment(ticketId, attachment.id, attachment.fileName);
      } catch (error) {
        showToast(error.message, 'error');
      }
    });
  });

  panel.querySelectorAll('[data-delete-attachment]').forEach((button) => {
    button.addEventListener('click', async () => {
      try {
        await attachmentApi.deleteAttachment(ticketId, button.dataset.deleteAttachment);
        showToast('Zalacznik zostal usuniety.', 'success');
        await onSaved();
      } catch (error) {
        showToast(error.message, 'error');
      }
    });
  });

  return panel;
}

import * as attachmentApi from '../../api/attachmentApi.js';
import { MAX_ATTACHMENT_BYTES } from '../../utils/constants.js';
import { formatDateTime } from '../../utils/dateFormatter.js';
import { escapeHtml, htmlToElement } from '../../utils/dom.js';
import { formatFileSize } from '../../utils/fileFormatter.js';

function validateFile(file) {
  if (!file) {
    throw new Error('Wybierz plik do wyslania.');
  }
  if (file.size > MAX_ATTACHMENT_BYTES) {
    throw new Error('Plik jest za duzy. Maksymalny rozmiar to 10 MB.');
  }
}

export async function uploadTicketAttachment({ ticketId, file, commentId = null }) {
  validateFile(file);
  return attachmentApi.uploadAttachment(ticketId, file, commentId);
}

export function AttachmentPanel({ ticketId, attachments = [], user, showToast, onChanged }) {
  const panel = htmlToElement(`
    <section class="card stack">
      <div class="section-title">
        <h2>Zalaczniki</h2>
        <span>${attachments.length}</span>
      </div>
      <div class="attachment-list">
        ${attachments.map((attachment) => `
          <article class="attachment-item">
            <div>
              <strong>${escapeHtml(attachment.fileName)}</strong>
              <span>${formatFileSize(attachment.fileSize)} | ${escapeHtml(attachment.uploadedBy)} | ${formatDateTime(attachment.uploadedAt)}</span>
              ${attachment.commentId ? `<small>Komentarz #${attachment.commentId}</small>` : ''}
            </div>
            <div class="row-actions">
              <button class="button button-small button-secondary" type="button" data-download="${attachment.id}">Pobierz</button>
              ${attachment.uploadedBy === user.username || user.roles?.includes('ADMIN') ? `
                <button class="button button-small button-ghost" type="button" data-delete="${attachment.id}">Usun</button>
              ` : ''}
            </div>
          </article>
        `).join('') || '<p class="muted">Brak zalacznikow.</p>'}
      </div>
      <form class="attachment-form">
        <label>Dodaj plik
          <input name="file" type="file" />
        </label>
        <button class="button button-primary" type="submit">Wyslij zalacznik</button>
      </form>
    </section>
  `);

  panel.querySelectorAll('[data-download]').forEach((button) => {
    button.addEventListener('click', async () => {
      const attachment = attachments.find((item) => String(item.id) === button.dataset.download);
      try {
        await attachmentApi.downloadAttachment(ticketId, attachment.id, attachment.fileName);
      } catch (error) {
        showToast(error.message, 'error');
      }
    });
  });

  panel.querySelectorAll('[data-delete]').forEach((button) => {
    button.addEventListener('click', async () => {
      try {
        await attachmentApi.deleteAttachment(ticketId, button.dataset.delete);
        showToast('Zalacznik zostal usuniety.', 'success');
        await onChanged();
      } catch (error) {
        showToast(error.message, 'error');
      }
    });
  });

  panel.querySelector('.attachment-form').addEventListener('submit', async (event) => {
    event.preventDefault();
    const file = event.currentTarget.elements.file.files[0];
    try {
      await uploadTicketAttachment({ ticketId, file });
      showToast('Zalacznik zostal dodany.', 'success');
      event.currentTarget.reset();
      await onChanged();
    } catch (error) {
      showToast(error.message, 'error');
    }
  });

  return panel;
}

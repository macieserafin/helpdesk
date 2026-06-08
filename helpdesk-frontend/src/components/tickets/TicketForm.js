import { createTicket } from '../../api/ticketApi.js';
import { escapeHtml, formToObject, htmlToElement } from '../../utils/dom.js';
import { FIELD_LIMITS } from '../../utils/constants.js';
import { getErrorMessage } from '../../utils/errorMessage.js';
import { formatFileSize } from '../../utils/fileFormatter.js';
import { requireFields } from '../../utils/validators.js';
import { uploadTicketAttachment, validateAttachmentFile } from './attachmentUpload.js';

function renderFileSummary(files) {
  if (!files.length) {
    return 'Nie wybrano plików.';
  }

  return files
    .map((file) => `${escapeHtml(file.name)} (${formatFileSize(file.size)})`)
    .join('<br>');
}

export function TicketForm({ categories = [], navigate, showToast }) {
  const disabled = categories.length === 0;
  const form = htmlToElement(`
    <form class="card form-grid">
      <label>Tytuł
        <input name="title" maxlength="${FIELD_LIMITS.ticket.title.max}" required placeholder="Np. Problem z logowaniem" />
      </label>
      <label>Kategoria
        <select name="category" required ${disabled ? 'disabled' : ''}>
          <option value="">Wybierz kategorię</option>
          ${categories.map((category) => `<option value="${escapeHtml(category.name)}">${escapeHtml(category.name)}</option>`).join('')}
        </select>
      </label>
      <label class="span-2">Opis
        <textarea name="description" maxlength="${FIELD_LIMITS.ticket.description.max}" rows="7" required placeholder="Opisz problem, kroki i oczekiwany rezultat"></textarea>
      </label>
      <label class="span-2">Załączniki
        <input name="attachments" type="file" multiple />
        <span class="file-summary" data-file-summary>Nie wybrano plików.</span>
      </label>
      ${disabled ? '<p class="alert alert-warning span-2">Brak aktywnych kategorii. Skontaktuj się z administratorem.</p>' : ''}
      <div class="form-actions span-2">
        <button class="button button-primary" type="submit" ${disabled ? 'disabled' : ''}>Utworz ticket</button>
      </div>
    </form>
  `);

  const fileInput = form.elements.attachments;
  const fileSummary = form.querySelector('[data-file-summary]');

  fileInput.addEventListener('change', () => {
    fileSummary.innerHTML = renderFileSummary([...fileInput.files]);
  });

  form.addEventListener('submit', async (event) => {
    event.preventDefault();
    const payload = formToObject(form);
    delete payload.attachments;
    const files = [...fileInput.files];
    const submit = form.querySelector('[type="submit"]');
    const originalText = submit.textContent;
    let ticket = null;
    try {
      requireFields(payload, ['title', 'description', 'category']);
      files.forEach(validateAttachmentFile);
      submit.disabled = true;
      submit.textContent = 'Tworzę...';
      ticket = await createTicket(payload);
      if (files.length) {
        submit.textContent = 'Wysyłam załączniki...';
        for (const file of files) {
          await uploadTicketAttachment({ ticketId: ticket.id, file });
        }
      }
      showToast(files.length
        ? `Ticket #${ticket.id} został utworzony z załącznikami.`
        : `Ticket #${ticket.id} został utworzony.`,
      'success');
      navigate(`/tickets/${ticket.id}`);
    } catch (error) {
      const message = getErrorMessage(error);
      if (ticket) {
        showToast(`Ticket #${ticket.id} został utworzony, ale załącznik nie został wysłany: ${message}`, 'error');
        navigate(`/tickets/${ticket.id}`);
        return;
      }
      showToast(message, 'error');
    } finally {
      submit.disabled = disabled;
      submit.textContent = originalText;
    }
  });

  return form;
}

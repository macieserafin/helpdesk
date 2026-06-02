import * as attachmentApi from '../../api/attachmentApi.js';
import { MAX_ATTACHMENT_BYTES } from '../../utils/constants.js';

export function validateAttachmentFile(file) {
  if (!file) {
    throw new Error('Wybierz plik do wyslania.');
  }
  if (file.size > MAX_ATTACHMENT_BYTES) {
    throw new Error('Plik jest za duzy. Maksymalny rozmiar to 10 MB.');
  }
}

export async function uploadTicketAttachment({ ticketId, file, commentId }) {
  validateAttachmentFile(file);
  return attachmentApi.uploadAttachment(ticketId, file, commentId);
}

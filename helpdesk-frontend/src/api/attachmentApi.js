import { del, get, post, requestBlob } from './httpClient.js';

export function listAttachments(ticketId) {
  return get(`/api/tickets/${ticketId}/attachments`);
}

export function uploadAttachment(ticketId, file, commentId = null) {
  const formData = new FormData();
  formData.append('file', file);
  if (commentId) {
    formData.append('commentId', String(commentId));
  }

  return post(`/api/tickets/${ticketId}/attachments`, formData);
}

export async function downloadAttachment(ticketId, attachmentId, fileName) {
  const blob = await requestBlob(`/api/tickets/${ticketId}/attachments/${attachmentId}`);
  const url = URL.createObjectURL(blob);
  const link = document.createElement('a');
  link.href = url;
  link.download = fileName;
  document.body.append(link);
  link.click();
  link.remove();
  window.setTimeout(() => URL.revokeObjectURL(url), 0);
}

export function deleteAttachment(ticketId, attachmentId) {
  return del(`/api/tickets/${ticketId}/attachments/${attachmentId}`);
}

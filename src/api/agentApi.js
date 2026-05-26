import { get, patch } from './httpClient.js';

export function getTicketQueue() {
  return get('/api/agent/tickets');
}

export function assignTicket(id) {
  return patch(`/api/agent/tickets/${id}/assign`, {});
}

export function updateTicketStatus(id, status) {
  return patch(`/api/agent/tickets/${id}/status`, { status });
}

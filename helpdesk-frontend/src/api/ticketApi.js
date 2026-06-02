import { buildQuery, get, patch, post } from './httpClient.js';

export function createTicket(payload) {
  return post('/api/tickets', payload);
}

export function getMyTickets(params = {}) {
  return get(`/api/tickets/me${buildQuery(params)}`);
}

export function getTicketStatuses() {
  return get('/api/tickets/statuses');
}

export function getTicketPriorities() {
  return get('/api/tickets/priorities');
}

export function getTicket(id) {
  return get(`/api/tickets/${id}`);
}

export function updateTicket(id, payload) {
  return patch(`/api/tickets/${id}`, payload);
}

export function addComment(id, payload) {
  return post(`/api/tickets/${id}/comments`, payload);
}

export function getComments(id) {
  return get(`/api/tickets/${id}/comments`);
}

export function getHistory(id) {
  return get(`/api/tickets/${id}/history`);
}

export function updateStatus(id, status) {
  return patch(`/api/tickets/${id}/status`, { status });
}

import { buildQuery, get, patch } from './httpClient.js';

export function getTicketQueue(params = {}) {
  return get(`/api/agent/tickets${buildQuery(params)}`);
}

export function getAgentDashboard() {
  return get('/api/agent/dashboard');
}

export function getAssignableTicketPriorities() {
  return get('/api/agent/tickets/assignable-priorities');
}

export function assignTicket(id) {
  return patch(`/api/agent/tickets/${id}/assign`, {});
}

export function updateTicketStatus(id, status) {
  return patch(`/api/agent/tickets/${id}/status`, { status });
}

export function updateTicketPriority(id, priority) {
  return patch(`/api/agent/tickets/${id}/priority`, { priority });
}

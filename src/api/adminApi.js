import { buildQuery, get, patch, post } from './httpClient.js';

export function getUsers() {
  return get('/api/admin/users');
}

export function getUser(id) {
  return get(`/api/admin/users/${id}`);
}

export function createUser(payload) {
  return post('/api/admin/users', payload);
}

export function updateUser(id, payload) {
  return patch(`/api/admin/users/${id}`, payload);
}

export function updateUserEnabled(id, enabled) {
  return patch(`/api/admin/users/${id}/enabled`, { enabled });
}

export function getTickets(params = {}) {
  return get(`/api/admin/tickets${buildQuery(params)}`);
}

import { del, get, patch, post } from './httpClient.js';

export function getActiveCategories() {
  return get('/api/categories');
}

export function getAdminCategories() {
  return get('/api/admin/categories');
}

export function getAdminCategory(id) {
  return get(`/api/admin/categories/${id}`);
}

export function createCategory(payload) {
  return post('/api/admin/categories', payload);
}

export function updateCategory(id, payload) {
  return patch(`/api/admin/categories/${id}`, payload);
}

export function deleteCategory(id) {
  return del(`/api/admin/categories/${id}`);
}

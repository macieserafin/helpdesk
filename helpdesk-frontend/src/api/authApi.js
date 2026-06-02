import { get, post } from './httpClient.js';

export function loginWithBasic(authHeader) {
  return get('/api/users/me', {
    skipAuth: true,
    headers: { Authorization: authHeader }
  });
}

export function register(payload) {
  return post('/api/auth/register', payload, { skipAuth: true });
}

export function getCurrentUser() {
  return get('/api/users/me');
}

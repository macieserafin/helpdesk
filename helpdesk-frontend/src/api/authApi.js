import { get, post } from './httpClient.js';

export function login(payload) {
  const body = new URLSearchParams();
  body.set('loginIdentifier', payload.loginIdentifier);
  body.set('password', payload.password);

  return post('/api/auth/login', body, { skipAuth: true });
}

export function logout() {
  return post('/api/auth/logout', null, { skipAuth: true });
}

export function register(payload) {
  return post('/api/auth/register', payload, { skipAuth: true });
}

export function getCurrentUser() {
  return get('/api/auth/me');
}

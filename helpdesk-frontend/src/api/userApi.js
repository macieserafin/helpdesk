import { get, patch } from './httpClient.js';

export function getMe() {
  return get('/api/auth/me');
}

export function getUserDashboard() {
  return get('/api/users/me/dashboard');
}

export function updateProfile(profile) {
  return patch('/api/users/me/profile', profile);
}

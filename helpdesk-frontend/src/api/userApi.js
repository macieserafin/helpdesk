import { get, patch } from './httpClient.js';

export function getMe() {
  return get('/api/users/me');
}

export function updateProfile(profile) {
  return patch('/api/users/me/profile', profile);
}

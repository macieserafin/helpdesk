import { clearAuthUser } from '../state/authStore.js';
import { refreshCurrentUser } from './authService.js';

export async function requireAuth() {
  try {
    return await refreshCurrentUser();
  } catch (error) {
    clearAuthUser();
    throw error;
  }
}

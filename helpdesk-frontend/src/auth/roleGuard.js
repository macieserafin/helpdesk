import { hasRole } from './authService.js';

export function requireRole(user, roles) {
  const accepted = Array.isArray(roles) ? roles : [roles];
  return accepted.some((role) => hasRole(user, role));
}

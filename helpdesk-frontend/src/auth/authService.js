import * as authApi from '../api/authApi.js';
import { clearAuthUser, getAuthUser, setAuthUser } from '../state/authStore.js';
import { ROLES } from '../utils/constants.js';

export async function login(loginIdentifier, password) {
  const user = await authApi.login({ loginIdentifier, password });
  setAuthUser(user);
  return user;
}

export async function refreshCurrentUser() {
  const user = await authApi.getCurrentUser();
  setAuthUser(user);
  return user;
}

export async function logout() {
  try {
    await authApi.logout();
  } finally {
    clearAuthUser();
  }
}

export function currentUser() {
  return getAuthUser();
}

export function hasRole(user, role) {
  return Boolean(user?.roles?.includes(role));
}

export function primaryRole(user) {
  if (hasRole(user, ROLES.ADMIN)) {
    return ROLES.ADMIN;
  }
  if (hasRole(user, ROLES.AGENT)) {
    return ROLES.AGENT;
  }
  return ROLES.USER;
}

export function homeRouteFor(user) {
  const role = primaryRole(user);
  if (role === ROLES.ADMIN) {
    return '/admin';
  }
  if (role === ROLES.AGENT) {
    return '/agent';
  }
  return '/user';
}

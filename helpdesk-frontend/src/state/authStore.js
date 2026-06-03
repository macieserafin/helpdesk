const USER_STORAGE_KEY = 'helpdesk.currentUser';
const TOKEN_STORAGE_KEY = 'helpdesk.basicToken';

let currentUser = readStoredUser();

function readStoredUser() {
  try {
    return JSON.parse(sessionStorage.getItem(USER_STORAGE_KEY));
  } catch {
    return null;
  }
}

export function createBasicToken(loginIdentifier, password) {
  return btoa(`${loginIdentifier}:${password}`);
}

export function getAuthUser() {
  return currentUser;
}

export function setAuthUser(user) {
  currentUser = user;
  if (user) {
    sessionStorage.setItem(USER_STORAGE_KEY, JSON.stringify(user));
  } else {
    sessionStorage.removeItem(USER_STORAGE_KEY);
  }
}

export function setAuthSession(user, token) {
  sessionStorage.setItem(TOKEN_STORAGE_KEY, token);
  setAuthUser(user);
}

export function getAuthHeader() {
  const token = sessionStorage.getItem(TOKEN_STORAGE_KEY);
  return token ? `Basic ${token}` : null;
}

export function clearAuthUser() {
  sessionStorage.removeItem(TOKEN_STORAGE_KEY);
  setAuthUser(null);
}

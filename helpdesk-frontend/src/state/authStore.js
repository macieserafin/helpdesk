let currentUser = null;

export function getAuthUser() {
  return currentUser;
}

export function setAuthUser(user) {
  currentUser = user;
}

export function clearAuthUser() {
  setAuthUser(null);
}

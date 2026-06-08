export function userLoginIdentifier(user) {
  return user?.loginIdentifier || '';
}

export function displayUserName(user) {
  const profile = user?.profile || {};
  const fullName = [profile.firstName, profile.lastName]
    .map((part) => String(part || '').trim())
    .filter(Boolean)
    .join(' ');

  return fullName || userLoginIdentifier(user) || 'Użytkownik';
}

export function displayUserName(user) {
  const profile = user?.profile || {};
  const fullName = [profile.firstName, profile.lastName]
    .map((part) => String(part || '').trim())
    .filter(Boolean)
    .join(' ');

  return fullName || user?.username || 'Uzytkownik';
}

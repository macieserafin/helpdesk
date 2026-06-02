export function requireFields(payload, fields) {
  const missing = fields.filter((field) => !String(payload[field] ?? '').trim());
  if (missing.length > 0) {
    throw new Error(`Uzupełnij wymagane pola: ${missing.join(', ')}`);
  }
}

export function normalizeRoles(value) {
  if (Array.isArray(value)) {
    return value;
  }

  return String(value || 'USER')
    .split(',')
    .map((role) => role.trim().toUpperCase())
    .filter(Boolean);
}

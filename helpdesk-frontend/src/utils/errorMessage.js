const API_MESSAGE_TRANSLATIONS = {
  'Validation failed': 'Popraw błędy w formularzu.',
  'Invalid request body': 'Nieprawidłowe dane żądania.',
  'Unexpected error': 'Wystąpił błąd serwera. Spróbuj ponownie później.',
  'Bad credentials': 'Nieprawidłowy login lub hasło.',
  'Invalid login identifier or password': 'Nieprawidłowy login lub hasło.'
};

const STATUS_FALLBACKS = {
  400: 'Nieprawidłowe dane. Sprawdź formularz i spróbuj ponownie.',
  401: 'Sesja wygasła albo wymagane jest logowanie.',
  403: 'Brak uprawnień do wykonania tej operacji.',
  404: 'Nie znaleziono zasobu.',
  409: 'Nie można wykonać operacji, bo powoduje konflikt danych.',
  413: 'Przesyłany plik jest za duży.',
  500: 'Wystąpił błąd serwera. Spróbuj ponownie później.'
};

function formatFieldErrors(details) {
  const errors = details?.errors;
  if (!Array.isArray(errors) || errors.length === 0) {
    return '';
  }

  return errors
    .map((error) => [error.field, error.message].filter(Boolean).join(': '))
    .filter(Boolean)
    .join('; ');
}

export function getErrorMessage(error, fallback = 'Nie udało się wykonać operacji. Spróbuj ponownie.') {
  const fieldErrors = formatFieldErrors(error?.details);
  if (fieldErrors) {
    return fieldErrors;
  }

  const message = String(error?.message || '').trim();
  if (message && API_MESSAGE_TRANSLATIONS[message]) {
    return API_MESSAGE_TRANSLATIONS[message];
  }

  if (message && !['Failed to fetch', 'NetworkError'].includes(message)) {
    return message;
  }

  if (message === 'Failed to fetch' || message === 'NetworkError') {
    return 'Nie można połączyć się z API. Sprawdź, czy serwer działa.';
  }

  return STATUS_FALLBACKS[error?.status] || fallback;
}

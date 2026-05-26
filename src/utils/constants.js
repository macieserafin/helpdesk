export const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';

export const ROLES = {
  USER: 'USER',
  AGENT: 'AGENT',
  ADMIN: 'ADMIN'
};

export const TICKET_STATUSES = [
  'OPEN',
  'IN_PROGRESS',
  'WAITING_FOR_USER',
  'RESOLVED',
  'CLOSED',
  'REJECTED',
  'CANCELLED'
];

export const TICKET_PRIORITIES = ['LOW', 'MEDIUM', 'HIGH', 'CRITICAL'];

export const DEFAULT_CATEGORIES = [
  'Konto',
  'Uprawnienia',
  'Zalaczniki',
  'Siec',
  'Sprzet',
  'Oprogramowanie',
  'Inne'
];

export const ROLE_LABELS = {
  USER: 'Uzytkownik',
  AGENT: 'Agent',
  ADMIN: 'Administrator'
};

export const STATUS_LABELS = {
  OPEN: 'Otwarte',
  IN_PROGRESS: 'W toku',
  WAITING_FOR_USER: 'Czeka na uzytkownika',
  RESOLVED: 'Rozwiazane',
  CLOSED: 'Zamkniete',
  REJECTED: 'Odrzucone',
  CANCELLED: 'Anulowane'
};

export const PRIORITY_LABELS = {
  LOW: 'Niski',
  MEDIUM: 'Sredni',
  HIGH: 'Wysoki',
  CRITICAL: 'Krytyczny'
};

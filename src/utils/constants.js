export const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';

export const FIELD_LIMITS = {
  username: { min: 3, max: 50 },
  email: { max: 100 },
  password: { min: 6, max: 100 },
  roleName: { max: 30 },
  profile: {
    firstName: { max: 50 },
    lastName: { max: 50 },
    phoneNumber: { max: 30 },
    city: { max: 100 },
    streetAddress: { max: 150 },
    postalCode: { max: 20 }
  },
  ticket: {
    title: { max: 150 },
    description: { max: 4000 },
    category: { max: 100 }
  },
  category: {
    name: { max: 100 },
    description: { max: 1000 }
  },
  comment: {
    content: { max: 2000 }
  }
};

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

export const TICKET_PRIORITIES = ['UNASSIGNED', 'LOW', 'MEDIUM', 'HIGH', 'CRITICAL'];

export const ASSIGNABLE_PRIORITIES = ['LOW', 'MEDIUM', 'HIGH', 'CRITICAL'];

export const MAX_ATTACHMENT_BYTES = 10 * 1024 * 1024;

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
  UNASSIGNED: 'Nieustalony',
  LOW: 'Niski',
  MEDIUM: 'Sredni',
  HIGH: 'Wysoki',
  CRITICAL: 'Krytyczny'
};

export const HISTORY_ACTION_LABELS = {
  TICKET_CREATED: 'Utworzono ticket',
  STATUS_CHANGED: 'Zmieniono status',
  PRIORITY_CHANGED: 'Zmieniono priorytet',
  ASSIGNED_CHANGED: 'Zmieniono przypisanie',
  COMMENT_ADDED: 'Dodano komentarz',
  ATTACHMENT_ADDED: 'Dodano zalacznik',
  ATTACHMENT_DELETED: 'Usunieto zalacznik',
  TICKET_RESOLVED: 'Rozwiazano ticket',
  TICKET_CLOSED: 'Zamknieto ticket'
};

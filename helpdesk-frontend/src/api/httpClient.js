import { API_BASE_URL } from '../utils/constants.js';
import { startLoading, stopLoading } from '../state/uiStore.js';
import { getAuthHeader } from '../state/authStore.js';

export class ApiError extends Error {
  constructor(message, status, details = null) {
    super(message);
    this.name = 'ApiError';
    this.status = status;
    this.details = details;
  }
}

async function parseResponse(response) {
  const contentType = response.headers.get('content-type') || '';
  if (response.status === 204) {
    return null;
  }

  if (contentType.includes('application/json')) {
    return response.json();
  }

  const text = await response.text();
  return text || null;
}

export function buildUrl(path) {
  if (path.startsWith('http')) {
    return path;
  }

  return `${API_BASE_URL}${path}`;
}

export function buildQuery(params = {}) {
  const query = new URLSearchParams();
  Object.entries(params).forEach(([key, value]) => {
    if (value === undefined || value === null || value === '') {
      return;
    }
    query.set(key, value);
  });

  const queryString = query.toString();
  return queryString ? `?${queryString}` : '';
}

export async function request(path, options = {}) {
  const { skipAuth = false, ...fetchOptions } = options;
  const headers = new Headers(options.headers || {});
  const authHeader = getAuthHeader();
  if (!skipAuth && authHeader && !headers.has('Authorization')) {
    headers.set('Authorization', authHeader);
  }

  const config = {
    credentials: 'omit',
    ...fetchOptions,
    headers
  };

  if (config.body && !(config.body instanceof FormData) && !(config.body instanceof URLSearchParams)) {
    headers.set('Content-Type', 'application/json');
    config.body = JSON.stringify(config.body);
  }

  startLoading();
  let response;
  let payload;
  try {
    response = await fetch(buildUrl(path), config);
    payload = await parseResponse(response);
  } finally {
    stopLoading();
  }

  if (!response.ok) {
    const message = payload?.message || payload?.error || payload?.detail || payload || `Blad API (${response.status})`;
    throw new ApiError(message, response.status, payload);
  }

  return payload;
}

export function get(path, options = {}) {
  return request(path, options);
}

export function post(path, body, options = {}) {
  return request(path, { method: 'POST', body, ...options });
}

export function patch(path, body, options = {}) {
  return request(path, { method: 'PATCH', body, ...options });
}

export function del(path, options = {}) {
  return request(path, { method: 'DELETE', ...options });
}

export async function requestBlob(path, options = {}) {
  const headers = new Headers(options.headers || {});
  const authHeader = getAuthHeader();
  if (authHeader && !headers.has('Authorization')) {
    headers.set('Authorization', authHeader);
  }

  startLoading();
  let response;
  try {
    response = await fetch(buildUrl(path), {
      credentials: 'omit',
      ...options,
      headers
    });
  } finally {
    stopLoading();
  }

  if (!response.ok) {
    let message = `Blad API (${response.status})`;
    try {
      const payload = await parseResponse(response);
      message = payload?.message || payload?.error || payload?.detail || payload || message;
      throw new ApiError(message, response.status, payload);
    } catch (error) {
      if (error instanceof ApiError) {
        throw error;
      }
      throw new ApiError(message, response.status);
    }
  }

  return response.blob();
}

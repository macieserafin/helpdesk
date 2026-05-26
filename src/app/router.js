import { routes } from './routes.js';

export function navigate(path) {
  window.location.hash = path;
}

export function currentPath() {
  const hash = window.location.hash.replace(/^#/, '');
  return hash || '/login';
}

export function matchRoute(path = currentPath()) {
  for (const route of routes) {
    const params = matchPath(route.path, path);
    if (params) {
      return { ...route, params };
    }
  }

  return { ...routes.find((route) => route.path === '*'), params: {} };
}

function matchPath(pattern, path) {
  if (pattern === '*') {
    return {};
  }
  if (pattern === path) {
    return {};
  }

  const patternParts = pattern.split('/').filter(Boolean);
  const pathParts = path.split('/').filter(Boolean);
  if (patternParts.length !== pathParts.length) {
    return null;
  }

  const params = {};
  for (let i = 0; i < patternParts.length; i += 1) {
    const patternPart = patternParts[i];
    const pathPart = pathParts[i];

    if (patternPart.startsWith(':')) {
      params[patternPart.slice(1)] = decodeURIComponent(pathPart);
      continue;
    }
    if (patternPart !== pathPart) {
      return null;
    }
  }

  return params;
}

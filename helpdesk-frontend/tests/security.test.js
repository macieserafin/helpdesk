import { test } from 'node:test';
import assert from 'node:assert/strict';
import { RoleBadges, StatusBadge, PriorityBadge } from '../src/components/common/Badges.js';
import { request } from '../src/api/httpClient.js';

test('badges escape untrusted HTML and attributes', () => {
  const attack = '\"><img src=x onerror=alert(1)>';
  for (const html of [RoleBadges([attack]), StatusBadge(attack), PriorityBadge(attack)]) {
    assert.equal(html.includes('<img'), false);
    assert.ok(html.includes('&lt;img'));
  }
});

test('mutations fetch current CSRF token and preserve login form body', async () => {
  const originalFetch = globalThis.fetch;
  const calls = [];
  globalThis.fetch = async (url, options) => {
    calls.push({ url, options });
    return url.endsWith('/csrf')
      ? new Response(JSON.stringify({ token: 'test-token', headerName: 'X-CSRF-TOKEN' }), { headers: { 'content-type': 'application/json' } })
      : new Response(null, { status: 204 });
  };
  try {
    const body = new URLSearchParams({ loginIdentifier: 'admin', password: 'test-only' });
    await request('/api/auth/login', { method: 'POST', body });
    assert.equal(calls[0].url, '/api/auth/csrf');
    assert.equal(calls[0].options.credentials, 'include');
    assert.equal(calls[1].options.headers.get('X-CSRF-TOKEN'), 'test-token');
    assert.equal(calls[1].options.body, body);
    assert.equal(calls[1].options.headers.has('Content-Type'), false);
  } finally {
    globalThis.fetch = originalFetch;
  }
});

test('failed token request prevents mutation', async () => {
  const originalFetch = globalThis.fetch;
  let calls = 0;
  globalThis.fetch = async () => { calls++; return new Response(null, { status: 503 }); };
  try {
    await assert.rejects(request('/api/admin/users', { method: 'POST', body: {} }));
    assert.equal(calls, 1);
  } finally {
    globalThis.fetch = originalFetch;
  }
});

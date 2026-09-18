import http from 'node:http';
import crypto from 'node:crypto';

const port = Number(process.env.PORT || 3001);
const apiKey = process.env.ARLO_API_KEY || '';
const providers = [
  { id: 'google-calendar', name: 'Google Calendar', auth: 'oauth2' },
  { id: 'google-drive', name: 'Google Drive', auth: 'oauth2' },
  { id: 'notion', name: 'Notion', auth: 'oauth2' },
  { id: 'slack', name: 'Slack', auth: 'oauth2' },
  { id: 'github', name: 'GitHub', auth: 'oauth2' },
  { id: 'linear', name: 'Linear', auth: 'oauth2' },
  { id: 'todoist', name: 'Todoist', auth: 'oauth2' },
  { id: 'weather', name: 'Weather API', auth: 'api-key' },
  { id: 'custom-api', name: 'Any other API', auth: 'manifest' }
];
const requests = new Map();
const rate = new Map();

function json(res, status, body) {
  res.writeHead(status, { 'content-type': 'application/json; charset=utf-8', 'cache-control': 'no-store', 'x-content-type-options': 'nosniff' });
  res.end(JSON.stringify(body));
}
function authorized(req) { return !apiKey || req.headers.authorization === `Bearer ${apiKey}`; }
function allowed(req) {
  const address = req.socket.remoteAddress || 'unknown';
  const now = Date.now();
  const recent = (rate.get(address) || []).filter((time) => now - time < 60_000);
  recent.push(now); rate.set(address, recent);
  return recent.length <= 60;
}
function body(req) {
  return new Promise((resolve, reject) => {
    let data = '';
    req.on('data', (chunk) => { data += chunk; if (data.length > 32_768) req.destroy(); });
    req.on('end', () => { try { resolve(data ? JSON.parse(data) : {}); } catch { reject(new Error('Invalid JSON')); } });
    req.on('error', reject);
  });
}
const server = http.createServer(async (req, res) => {
  if (!allowed(req)) return json(res, 429, { error: 'rate_limited' });
  if (req.method === 'GET' && req.url === '/api/health') return json(res, 200, { ok: true, service: 'arlo-api' });
  if (req.method === 'GET' && req.url === '/api/providers') return json(res, 200, { providers });
  if (req.url === '/api/connection-requests' && req.method === 'POST') {
    if (!authorized(req)) return json(res, 401, { error: 'unauthorized' });
    try {
      const input = await body(req);
      const provider = providers.find((item) => item.id === String(input.providerId || ''));
      if (!provider) return json(res, 400, { error: 'unknown_provider' });
      const request = { id: crypto.randomUUID(), providerId: provider.id, status: 'awaiting-provider-auth', createdAt: new Date().toISOString() };
      requests.set(request.id, request);
      return json(res, 202, request);
    } catch { return json(res, 400, { error: 'invalid_request' }); }
  }
  if (req.url?.startsWith('/api/connection-requests/') && req.method === 'GET') {
    if (!authorized(req)) return json(res, 401, { error: 'unauthorized' });
    const request = requests.get(req.url.split('/').pop());
    return request ? json(res, 200, request) : json(res, 404, { error: 'not_found' });
  }
  return json(res, 404, { error: 'not_found' });
});
server.listen(port, '0.0.0.0', () => console.log(`Arlo API listening on ${port}`));

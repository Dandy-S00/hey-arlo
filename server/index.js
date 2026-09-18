import http from 'node:http';
import fs from 'node:fs/promises';
import path from 'node:path';
import { fileURLToPath } from 'node:url';

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..');
const dist = path.join(root, 'dist');
const port = Number(process.env.PORT || 8080);
const apiProviders = [
  { id: 'google-calendar', name: 'Google Calendar', auth: 'oauth2' },
  { id: 'google-drive', name: 'Google Drive', auth: 'oauth2' },
  { id: 'notion', name: 'Notion', auth: 'oauth2' },
  { id: 'slack', name: 'Slack', auth: 'oauth2' },
  { id: 'github', name: 'GitHub', auth: 'oauth2' },
  { id: 'linear', name: 'Linear', auth: 'oauth2' },
  { id: 'todoist', name: 'Todoist', auth: 'oauth2' },
  { id: 'custom-api', name: 'Any other API', auth: 'manifest' }
];

function json(res, status, body) {
  res.writeHead(status, { 'content-type': 'application/json; charset=utf-8', 'cache-control': 'no-store' });
  res.end(JSON.stringify(body));
}
function safeFile(urlPath) {
  const decoded = decodeURIComponent(urlPath.split('?')[0]);
  const relative = decoded === '/' ? 'index.html' : decoded.replace(/^\/+/, '');
  const candidate = path.resolve(dist, relative);
  return candidate.startsWith(dist + path.sep) ? candidate : null;
}

const server = http.createServer(async (req, res) => {
  try {
    const url = new URL(req.url || '/', `http://${req.headers.host || 'localhost'}`);
    if (url.pathname === '/api/health') return json(res, 200, { ok: true, service: 'arlo', version: process.env.APP_VERSION || 'dev' });
    if (url.pathname === '/api/providers' && req.method === 'GET') return json(res, 200, { providers: apiProviders });
    if (url.pathname === '/api/connections' && req.method === 'POST') {
      const chunks = [];
      for await (const chunk of req) chunks.push(chunk);
      const body = JSON.parse(Buffer.concat(chunks).toString() || '{}');
      const provider = apiProviders.find((item) => item.id === body.providerId);
      if (!provider) return json(res, 400, { error: 'Unknown provider' });
      return json(res, 202, { provider: provider.id, status: 'awaiting-provider-auth', next: 'secure-provider-adapter' });
    }
    if (url.pathname.startsWith('/api/')) return json(res, 404, { error: 'API route not found' });
    let file = safeFile(req.url || '/');
    try { await fs.access(file); } catch { file = path.join(dist, 'index.html'); }
    const data = await fs.readFile(file);
    const ext = path.extname(file);
    const types = { '.html': 'text/html', '.js': 'text/javascript', '.css': 'text/css', '.json': 'application/json', '.png': 'image/png', '.svg': 'image/svg+xml' };
    res.writeHead(200, { 'content-type': types[ext] || 'application/octet-stream', 'x-content-type-options': 'nosniff' });
    res.end(data);
  } catch (error) {
    json(res, 500, { error: 'Internal server error' });
  }
});
server.listen(port, '0.0.0.0', () => console.log(`Arlo server listening on ${port}`));

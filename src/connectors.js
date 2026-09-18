const CONNECTIONS_KEY = 'arlo-connection-consents';

// Public, user-selectable API catalog. Secrets are never stored here.
const providers = [
  { id: 'google-calendar', name: 'Google Calendar', category: 'Productivity', logo: 'G', auth: 'OAuth 2.0', description: 'Read and create calendar events after approval.' },
  { id: 'google-drive', name: 'Google Drive', category: 'Files', logo: 'D', auth: 'OAuth 2.0', description: 'Find and organize files you choose.' },
  { id: 'notion', name: 'Notion', category: 'Notes', logo: 'N', auth: 'OAuth 2.0', description: 'Read and update selected pages and databases.' },
  { id: 'slack', name: 'Slack', category: 'Communication', logo: 'S', auth: 'OAuth 2.0', description: 'Search and send messages only where approved.' },
  { id: 'github', name: 'GitHub', category: 'Development', logo: 'GH', auth: 'OAuth 2.0', description: 'Read repositories and create changes you approve.' },
  { id: 'linear', name: 'Linear', category: 'Planning', logo: 'L', auth: 'OAuth 2.0', description: 'Read and update selected issues and projects.' },
  { id: 'todoist', name: 'Todoist', category: 'Tasks', logo: 'T', auth: 'OAuth 2.0', description: 'Read and manage tasks after approval.' },
  { id: 'weather', name: 'Weather API', category: 'Public data', logo: 'W', auth: 'API key or public', description: 'Retrieve weather data for a location.' },
  { id: 'custom-api', name: 'Any other API', category: 'Custom', logo: '+', auth: 'OAuth, API key, or bearer token', description: 'Describe an API by name or add its connection details.' }
];

function readConnections() {
  try { return JSON.parse(sessionStorage.getItem(CONNECTIONS_KEY) || '{}'); } catch { return {}; }
}
function saveConnections(value) { sessionStorage.setItem(CONNECTIONS_KEY, JSON.stringify(value)); }
function normalize(value) { return String(value || '').trim().toLowerCase(); }
function findProvider(query) {
  const needle = normalize(query);
  return providers.find((provider) => normalize(provider.name) === needle || normalize(provider.id) === needle) ||
    providers.find((provider) => normalize(provider.name).includes(needle) || needle.includes(normalize(provider.name)));
}
function providerLogo(provider) { return `<span class="connection-icon" aria-hidden="true">${provider.logo}</span>`; }

function openConnectionsPanel() {
  if (document.querySelector('.connections-panel')) return;
  const panel = document.createElement('section');
  panel.className = 'connections-panel card';
  panel.innerHTML = `
    <div class="connections-head">
      <div><span class="eyebrow">Permission center</span><h2>Connect Arlo to an API</h2>
      <p class="muted">Choose a service, or ask for one by name. Arlo will request only the access you approve.</p></div>
      <button class="delete" data-close-connections aria-label="Close">×</button>
    </div>
    <form class="api-search" data-api-search>
      <input name="query" autocomplete="off" placeholder="Ask: connect Google Calendar, Notion, or my weather API" aria-label="Find an API" required>
      <button class="primary" type="submit">Find connection</button>
    </form>
    <div class="connection-list" data-provider-list></div>
    <div class="notice">This catalog describes integrations only. Provider authorization and credentials are kept out of the catalog and must be handled by a secure adapter.</div>`;
  document.querySelector('.content')?.appendChild(panel);
  renderProviders(panel, providers);
  panel.querySelector('[data-close-connections]').onclick = () => panel.remove();
  panel.querySelector('[data-api-search]').onsubmit = (event) => {
    event.preventDefault();
    const query = new FormData(event.currentTarget).get('query');
    const provider = findProvider(query);
    if (provider) {
      renderProviders(panel, [provider]);
      panel.querySelector('[data-api-search] input').value = provider.name;
    } else {
      renderProviders(panel, [providers.at(-1)]);
      panel.querySelector('[data-api-search] input').value = query;
      alert(`I could not match that name yet. Choose Any other API to define a secure adapter for “${query}”.`);
    }
  };
}

function renderProviders(panel, list) {
  const target = panel.querySelector('[data-provider-list]');
  const connections = readConnections();
  target.innerHTML = list.map((provider) => {
    const active = connections[provider.id]?.status === 'awaiting-provider-auth';
    return `<article class="connection-row">
      ${providerLogo(provider)}<div class="connection-copy"><h3>${provider.name}<small>${provider.category}</small></h3>
      <p>${provider.description}</p><small class="muted">Auth: ${provider.auth}</small></div>
      <button class="${active ? 'ghost' : 'primary'}" data-connect="${provider.id}">${active ? 'Awaiting authorization' : 'Connect'}</button>
    </article>`;
  }).join('');
  target.querySelectorAll('[data-connect]').forEach((button) => {
    button.onclick = () => requestConnection(button.dataset.connect, panel);
  });
}

function requestConnection(id, panel) {
  const provider = providers.find((item) => item.id === id);
  if (!provider) return;
  const connections = readConnections();
  if (connections[id]?.status === 'awaiting-provider-auth') {
    delete connections[id];
  } else if (confirm(`Allow Arlo to start connecting to ${provider.name}? It will request only: ${provider.description}`)) {
    connections[id] = { provider: provider.name, status: 'awaiting-provider-auth', requestedAt: new Date().toISOString() };
    alert(`${provider.name} is ready for its provider authorization step. No data is active until you approve that step.`);
  }
  saveConnections(connections);
  renderProviders(panel, [provider]);
}

const observer = new MutationObserver(() => {
  if (document.querySelector('.shell') && !document.querySelector('[data-connections-button]')) {
    const button = document.createElement('button');
    button.className = 'connections-fab';
    button.dataset.connectionsButton = 'true';
    button.textContent = 'Connect APIs';
    button.onclick = openConnectionsPanel;
    document.body.appendChild(button);
  }
});
observer.observe(document.body, { childList: true, subtree: true });

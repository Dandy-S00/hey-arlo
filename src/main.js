import './styles.css';

const STORAGE_KEY = 'hey-arlo-local-v1';
const starter = {
  goals: [
    { id: crypto.randomUUID(), title: 'Make today count', detail: 'Choose one meaningful next step.', done: false }
  ],
  tasks: [
    { id: crypto.randomUUID(), title: 'Write down the one thing that matters today', done: false }
  ],
  notes: [],
  permissions: {
    calendar: false,
    location: false,
    notifications: true,
    files: false,
    microphone: false,
    camera: false,
    accessibility: false,
    cloudAI: false
  },
  lastCheckIn: null
};

let state = load();
let active = 'today';

function load() {
  try { return { ...starter, ...JSON.parse(localStorage.getItem(STORAGE_KEY)) }; }
  catch { return structuredClone(starter); }
}

function save() { localStorage.setItem(STORAGE_KEY, JSON.stringify(state)); }
function esc(value) { return String(value).replace(/[&<>\"']/g, c => ({'&':'&amp;','<':'&lt;','>':'&gt;','\"':'&quot;',"'":'&#039;'}[c])); }
function completed(items) { return items.filter(item => item.done).length; }
function todayGreeting() {
  const hour = new Date().getHours();
  return hour < 12 ? 'Good morning' : hour < 18 ? 'Good afternoon' : 'Good evening';
}

function render() {
  document.querySelector('#app').innerHTML = `
    <div class="shell">
      <aside class="sidebar">
        <div class="brand"><span class="brand-mark">✦</span><span>Hey Arlo</span></div>
        <p class="tagline">A tiny coach for big intentions.</p>
        <nav>
          ${navButton('today', '☀', 'Today')}
          ${navButton('goals', '◈', 'Goals')}
          ${navButton('journal', '✎', 'Journal')}
          ${navButton('privacy', '⌁', 'Privacy')}
        </nav>
        <div class="local-badge"><span class="dot"></span> Local mode<br><small>Your data stays here</small></div>
      </aside>
      <main class="content">${view()}</main>
    </div>`;
  bind();
}

function navButton(id, icon, label) {
  return `<button class="nav-btn ${active === id ? 'active' : ''}" data-nav="${id}"><span>${icon}</span>${label}</button>`;
}

function view() {
  if (active === 'goals') return goalsView();
  if (active === 'journal') return journalView();
  if (active === 'privacy') return privacyView();
  return todayView();
}

function todayView() {
  const done = completed(state.tasks);
  return `<header class="topbar"><div><p class="eyebrow">${new Date().toLocaleDateString(undefined, {weekday:'long', month:'long', day:'numeric'})}</p><h1>${todayGreeting()}, you.</h1></div><button class="pill" data-checkin>✦ Check in</button></header>
    <section class="hero card"><div><span class="eyebrow">A gentle nudge</span><h2>Progress beats perfect.</h2><p>Pick one small thing. Arlo will help you keep the promise you make to yourself.</p><button class="primary" data-focus>Add a focus for today</button></div><div class="sun">☼</div></section>
    <section class="grid two"><div class="card"><div class="card-head"><h3>Today's tasks</h3><span class="counter">${done}/${state.tasks.length}</span></div><form data-task-form><input name="title" placeholder="What would feel good to finish?" required /><button class="icon-btn" aria-label="Add task">+</button></form><div class="items">${state.tasks.map(task => taskItem(task)).join('') || empty('No tasks yet. Add a tiny one.')}</div></div>
    <div class="card reflection"><span class="eyebrow">Reflection</span><h3>How is your energy?</h3><div class="moods">${['😵','😕','😐','🙂','✨'].map((m,i)=>`<button data-mood="${i}" aria-label="Energy ${i+1}">${m}</button>`).join('')}</div><p class="muted">A check-in is saved only on this device.</p></div></section>`;
}

function taskItem(task) { return `<label class="item ${task.done ? 'complete' : ''}"><input type="checkbox" data-task="${task.id}" ${task.done ? 'checked' : ''}/><span>${esc(task.title)}</span><button class="delete" data-delete-task="${task.id}" aria-label="Delete task">×</button></label>`; }
function empty(text) { return `<p class="empty">${text}</p>`; }

function goalsView() {
  return `<header class="topbar"><div><p class="eyebrow">Direction</p><h1>Your goals</h1></div></header><section class="card"><div class="card-head"><div><h3>Keep the why nearby.</h3><p class="muted">Small actions become patterns when they have a reason.</p></div></div><form class="goal-form" data-goal-form><input name="title" placeholder="Goal title" required /><input name="detail" placeholder="Why does it matter?" /><button class="primary">Add goal</button></form><div class="goal-list">${state.goals.map(goal => `<article class="goal ${goal.done ? 'complete' : ''}"><button class="goal-check" data-goal="${goal.id}">${goal.done ? '✓' : '○'}</button><div><h3>${esc(goal.title)}</h3><p>${esc(goal.detail || 'No reason added yet.')}</p></div><button class="delete" data-delete-goal="${goal.id}">×</button></article>`).join('') || empty('No goals yet.')}</div></section>`;
}

function journalView() {
  return `<header class="topbar"><div><p class="eyebrow">Private notebook</p><h1>Journal</h1></div></header><section class="grid two"><div class="card"><form data-note-form><textarea name="body" placeholder="What is on your mind? Nothing leaves this device."></textarea><button class="primary">Save private note</button></form></div><div class="card"><span class="eyebrow">Local notes</span>${state.notes.length ? state.notes.map(note => `<article class="note"><p>${esc(note.body)}</p><small>${new Date(note.createdAt).toLocaleString()}</small></article>`).join('') : empty('Your notes will appear here.')}</div></section>`;
}

function privacyView() {
  const labels = {calendar:'Calendar and reminders',location:'Location signals',notifications:'Notifications',files:'Files and notes',microphone:'Microphone',camera:'Camera',accessibility:'Accessibility signals',cloudAI:'Optional cloud AI'};
  return `<header class="topbar"><div><p class="eyebrow">Control room</p><h1>Your privacy</h1></div><button class="danger ghost" data-pause>Pause all</button></header><section class="card privacy-card"><div class="privacy-intro"><span class="shield">⌁</span><div><h2>You are in charge.</h2><p>These switches are only product preferences in this prototype. Real device permissions will be requested separately by each platform.</p></div></div>${Object.entries(labels).map(([key,label]) => `<label class="permission"><span><strong>${label}</strong><small>${permissionDescription(key)}</small></span><input type="checkbox" data-permission="${key}" ${state.permissions[key] ? 'checked' : ''}/></label>`).join('')}<div class="notice">Cloud AI is off by default. When enabled in the production app, requests must be minimized and clearly disclosed before leaving the device.</div></section>`;
}
function permissionDescription(key) { return key === 'cloudAI' ? 'May send minimized, redacted context to a user-approved model.' : key === 'location' ? 'Use approximate routines, never raw history by default.' : key === 'accessibility' ? 'Use only for visible, user-approved assistance.' : 'Disabled until you choose to enable it.'; }

function bind() {
  document.querySelectorAll('[data-nav]').forEach(b => b.onclick = () => { active = b.dataset.nav; render(); });
  document.querySelectorAll('[data-task]').forEach(input => input.onchange = () => { const t = state.tasks.find(x => x.id === input.dataset.task); if (t) t.done = input.checked; save(); render(); });
  document.querySelectorAll('[data-delete-task]').forEach(b => b.onclick = () => { state.tasks = state.tasks.filter(x => x.id !== b.dataset.deleteTask); save(); render(); });
  document.querySelectorAll('[data-goal]').forEach(b => b.onclick = () => { const g = state.goals.find(x => x.id === b.dataset.goal); if(g) g.done = !g.done; save(); render(); });
  document.querySelectorAll('[data-delete-goal]').forEach(b => b.onclick = () => { state.goals = state.goals.filter(x => x.id !== b.dataset.deleteGoal); save(); render(); });
  document.querySelectorAll('[data-permission]').forEach(input => input.onchange = () => { state.permissions[input.dataset.permission] = input.checked; save(); });
  document.querySelector('[data-task-form]')?.addEventListener('submit', e => { e.preventDefault(); const title = new FormData(e.target).get('title'); state.tasks.unshift({id: crypto.randomUUID(), title, done:false}); save(); render(); });
  document.querySelector('[data-goal-form]')?.addEventListener('submit', e => { e.preventDefault(); const f = new FormData(e.target); state.goals.unshift({id:crypto.randomUUID(), title:f.get('title'), detail:f.get('detail'), done:false}); save(); render(); });
  document.querySelector('[data-note-form]')?.addEventListener('submit', e => { e.preventDefault(); const body = new FormData(e.target).get('body'); if(body.trim()) { state.notes.unshift({id:crypto.randomUUID(), body, createdAt:new Date().toISOString()}); save(); render(); } });
  document.querySelector('[data-checkin]')?.addEventListener('click', () => { state.lastCheckIn = new Date().toISOString(); save(); alert('Saved locally. What is one kind thing you can do for yourself today?'); });
  document.querySelector('[data-focus]')?.addEventListener('click', () => document.querySelector('[name=title]')?.focus());
  document.querySelector('[data-pause]')?.addEventListener('click', () => { Object.keys(state.permissions).forEach(k => state.permissions[k] = false); save(); render(); });
}

render();

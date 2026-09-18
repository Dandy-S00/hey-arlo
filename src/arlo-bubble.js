const AVATAR_KEY = 'arlo-contact-avatar-session';
const DEFAULT_AVATAR = '✦';

function mount() {
  if (!document.querySelector('.shell') || document.querySelector('.arlo-bubble')) return;
  const bubble = document.createElement('div');
  bubble.className = 'arlo-bubble-wrap';
  bubble.innerHTML = `<button class="arlo-bubble" aria-label="Arlo activity and contact picture" aria-expanded="false"><span class="arlo-avatar">${sessionStorage.getItem(AVATAR_KEY) || DEFAULT_AVATAR}</span><span class="active-pulse"></span></button><div class="arlo-popover" hidden><div class="arlo-popover-title"><strong>Arlo is active</strong><span class="listening-dot"></span></div><p>Arlo is looking and listening only through sources you have enabled.</p><p class="arlo-source-note">No source is active unless you turn it on.</p><div class="avatar-options"><button data-avatar="✦">✦</button><button data-avatar="☼">☼</button><button data-avatar="◈">◈</button><button data-avatar="☁">☁</button><label class="avatar-upload">Upload<input type="file" accept="image/png,image/jpeg,image/webp" /></label></div><button class="arlo-close">Close</button></div>`;
  document.body.appendChild(bubble);
  const button = bubble.querySelector('.arlo-bubble');
  const popover = bubble.querySelector('.arlo-popover');
  button.onclick = () => { const open = !popover.hidden; popover.hidden = open; button.setAttribute('aria-expanded', String(!open)); };
  bubble.querySelectorAll('[data-avatar]').forEach(option => option.onclick = () => setAvatar(option.dataset.avatar));
  bubble.querySelector('input[type=file]').onchange = event => {
    const file = event.target.files?.[0];
    if (!file || file.size > 1024 * 1024) return;
    const reader = new FileReader();
    reader.onload = () => setAvatar(reader.result);
    reader.readAsDataURL(file);
  };
  bubble.querySelector('.arlo-close').onclick = () => { popover.hidden = true; button.setAttribute('aria-expanded', 'false'); };
}
function setAvatar(value) { sessionStorage.setItem(AVATAR_KEY, value); const avatar = document.querySelector('.arlo-avatar'); avatar.textContent = value.startsWith('data:') ? '' : value; if (value.startsWith('data:')) avatar.style.backgroundImage = `url(${value})`; }
const observer = new MutationObserver(mount);
observer.observe(document.body, { childList: true, subtree: true });
mount();

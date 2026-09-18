// Phase 3 foundation: encrypted local vault using Web Crypto.
// AES-GCM is currently the browser-native authenticated encryption layer.
// Post-quantum protection requires a reviewed hybrid KEM implementation;
// Web Crypto does not currently provide one directly.

const PREFIX = 'arlo-vault-v1:';
const enc = new TextEncoder();
const dec = new TextDecoder();

function bytesToBase64(bytes) {
  let binary = '';
  bytes.forEach(byte => { binary += String.fromCharCode(byte); });
  return btoa(binary);
}

function base64ToBytes(value) {
  return Uint8Array.from(atob(value), char => char.charCodeAt(0));
}

async function deriveKey(passphrase, salt) {
  const material = await crypto.subtle.importKey('raw', enc.encode(passphrase), 'PBKDF2', false, ['deriveKey']);
  return crypto.subtle.deriveKey(
    { name: 'PBKDF2', salt, iterations: 600000, hash: 'SHA-256' },
    material,
    { name: 'AES-GCM', length: 256 },
    false,
    ['encrypt', 'decrypt']
  );
}

export function hasVault(storageKey = 'arlo-secure-vault') {
  return Boolean(localStorage.getItem(storageKey));
}

export async function encryptState(state, passphrase) {
  if (!passphrase || passphrase.length < 12) throw new Error('Use a passphrase of at least 12 characters.');
  const salt = crypto.getRandomValues(new Uint8Array(16));
  const iv = crypto.getRandomValues(new Uint8Array(12));
  const key = await deriveKey(passphrase, salt);
  const ciphertext = await crypto.subtle.encrypt({ name: 'AES-GCM', iv }, key, enc.encode(JSON.stringify(state)));
  return `${PREFIX}${JSON.stringify({ v: 1, kdf: 'PBKDF2-SHA-256', iterations: 600000, cipher: 'AES-256-GCM', salt: bytesToBase64(salt), iv: bytesToBase64(iv), data: bytesToBase64(new Uint8Array(ciphertext)) })}`;
}

export async function decryptState(payload, passphrase) {
  if (!payload?.startsWith(PREFIX)) throw new Error('Unsupported vault format.');
  const record = JSON.parse(payload.slice(PREFIX.length));
  const salt = base64ToBytes(record.salt);
  const iv = base64ToBytes(record.iv);
  const key = await deriveKey(passphrase, salt);
  const plaintext = await crypto.subtle.decrypt({ name: 'AES-GCM', iv }, key, base64ToBytes(record.data));
  return JSON.parse(dec.decode(plaintext));
}

export async function saveVault(state, passphrase, storageKey = 'arlo-secure-vault') {
  localStorage.setItem(storageKey, await encryptState(state, passphrase));
}

export async function openVault(passphrase, storageKey = 'arlo-secure-vault') {
  const payload = localStorage.getItem(storageKey);
  if (!payload) return null;
  return decryptState(payload, passphrase);
}

export function deleteVault(storageKey = 'arlo-secure-vault') {
  localStorage.removeItem(storageKey);
}

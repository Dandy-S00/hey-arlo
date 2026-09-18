import test, { beforeEach } from 'node:test';
import assert from 'node:assert/strict';

const storage = new Map();
globalThis.localStorage = {
  getItem(key) { return storage.has(key) ? storage.get(key) : null; },
  setItem(key, value) { storage.set(key, String(value)); },
  removeItem(key) { storage.delete(key); },
};

const { encryptState, decryptState, saveVault, openVault, hasVault, deleteVault } = await import('../src/security/vault.js');

beforeEach(() => {
  storage.clear();
});

test('encrypts and decrypts state with the correct passphrase', async () => {
  const state = { goals: [{ id: 'g1', title: 'Ship Arlo', done: false }], version: 2 };
  const payload = await encryptState(state, 'a sufficiently long passphrase');

  assert.match(payload, /^arlo-vault-v1:/);
  assert.notEqual(payload, JSON.stringify(state));
  assert.deepEqual(await decryptState(payload, 'a sufficiently long passphrase'), state);
});

test('rejects a wrong passphrase without returning plaintext', async () => {
  const payload = await encryptState({ privateNote: 'do not expose' }, 'correct passphrase');

  await assert.rejects(
    decryptState(payload, 'incorrect passphrase'),
    /OperationError|decrypt|authentication/i,
  );
});

test('rejects tampered ciphertext', async () => {
  const payload = await encryptState({ value: 42 }, 'correct passphrase');
  const record = JSON.parse(payload.slice('arlo-vault-v1:'.length));
  record.data = `${record.data.slice(0, -2)}AA`;

  await assert.rejects(
    decryptState(`arlo-vault-v1:${JSON.stringify(record)}`, 'correct passphrase'),
    /OperationError|decrypt|authentication/i,
  );
});

test('rejects short passphrases', async () => {
  await assert.rejects(
    encryptState({ value: 1 }, 'too-short'),
    /at least 12 characters/i,
  );
});

test('saves, detects, opens, and deletes the vault', async () => {
  const state = { notes: [{ id: 'n1', body: 'private' }] };
  const passphrase = 'a sufficiently long passphrase';

  assert.equal(hasVault(), false);
  await saveVault(state, passphrase);
  assert.equal(hasVault(), true);
  assert.deepEqual(await openVault(passphrase), state);

  deleteVault();
  assert.equal(hasVault(), false);
  assert.equal(await openVault(passphrase), null);
});

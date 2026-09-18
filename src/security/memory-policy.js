// Consent-first memory and personality policy.
// This module deliberately has no screen-reading, mailbox-reading, or background
// collection code. Sources must be explicitly imported and approved by the user.

export const SOURCE_TYPES = Object.freeze({
  userText: 'user_text',
  importedFile: 'imported_file',
  connectedMailbox: 'connected_mailbox',
  connectedCalendar: 'connected_calendar'
});

export const DEFAULT_MEMORY = Object.freeze({
  version: 1,
  entries: [],
  preferences: {
    tone: 'warm, direct, and encouraging',
    communicationStyle: 'ask before assuming',
    feedbackPreference: 'specific next steps',
    sensitiveTraits: [],
    sourcesAllowed: [SOURCE_TYPES.userText]
  },
  review: {
    lastReviewedAt: null,
    pendingSuggestions: []
  }
});

export function createMemoryEntry({ summary, sourceType = SOURCE_TYPES.userText, sourceLabel = 'User input', confidence = 'user-confirmed' }) {
  if (!summary?.trim()) throw new Error('A memory summary is required.');
  if (!Object.values(SOURCE_TYPES).includes(sourceType)) throw new Error('Unknown memory source.');
  return { id: crypto.randomUUID(), summary: summary.trim(), sourceType, sourceLabel, confidence, createdAt: new Date().toISOString(), status: 'active' };
}

export function proposePersonalityUpdate(memory, proposal) {
  // AI may propose; it must not silently apply. The user confirms each change.
  return { id: crypto.randomUUID(), proposal: proposal.trim(), basedOn: memory.entries.slice(0, 10).map(entry => entry.id), createdAt: new Date().toISOString(), status: 'needs-user-review' };
}

export function approveMemoryProposal(memory, proposalId) {
  const proposal = memory.review.pendingSuggestions.find(item => item.id === proposalId);
  if (!proposal) throw new Error('Memory proposal not found.');
  memory.preferences.tone = proposal.proposal;
  proposal.status = 'approved';
  memory.review.lastReviewedAt = new Date().toISOString();
  return memory;
}

export function revokeSource(memory, sourceType) {
  memory.preferences.sourcesAllowed = memory.preferences.sourcesAllowed.filter(source => source !== sourceType);
  memory.entries = memory.entries.map(entry => entry.sourceType === sourceType ? { ...entry, status: 'revoked' } : entry);
  return memory;
}

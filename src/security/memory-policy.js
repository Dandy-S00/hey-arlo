// Low-friction, consent-first adaptation policy.
// Arlo can learn locally and test low-risk communication changes, but it
// cannot silently collect from arbitrary devices or permanently retain raw data.

export const SOURCE_TYPES = Object.freeze({
  userText: 'user_text',
  importedFile: 'imported_file',
  connectedMailbox: 'connected_mailbox',
  connectedCalendar: 'connected_calendar',
  userTriggeredScreen: 'user_triggered_screen'
});

export const DEFAULT_MEMORY = Object.freeze({
  version: 2,
  entries: [],
  preferences: {
    tone: 'warm, direct, and encouraging',
    communicationStyle: 'ask before assuming',
    feedbackPreference: 'specific next steps',
    sensitiveTraits: [],
    sourcesAllowed: [SOURCE_TYPES.userText]
  },
  learning: {
    rawRetentionDays: 7,
    automaticLowRiskTrials: true,
    requireApprovalForPermanentChanges: true,
    pendingTrials: []
  },
  review: { lastReviewedAt: null, pendingSuggestions: [] }
});

export function createMemoryEntry({ summary, sourceType = SOURCE_TYPES.userText, sourceLabel = 'User input', confidence = 'user-confirmed' }) {
  if (!summary?.trim()) throw new Error('A memory summary is required.');
  if (!Object.values(SOURCE_TYPES).includes(sourceType)) throw new Error('Unknown memory source.');
  return { id: crypto.randomUUID(), summary: summary.trim(), sourceType, sourceLabel, confidence, createdAt: new Date().toISOString(), status: 'active' };
}

export function proposeAdaptiveTrial(memory, proposedChange, evidence = []) {
  if (!proposedChange?.trim()) throw new Error('A proposed change is required.');
  const trial = {
    id: crypto.randomUUID(),
    proposedChange: proposedChange.trim(),
    evidence: evidence.slice(0, 5).map(item => ({ id: item.id, summary: item.summary })),
    startedAt: new Date().toISOString(),
    expiresAt: new Date(Date.now() + 7 * 86400000).toISOString(),
    status: 'trial',
    userFeedback: null
  };
  memory.learning.pendingTrials.push(trial);
  return trial;
}

export function recordTrialFeedback(memory, trialId, liked) {
  const trial = memory.learning.pendingTrials.find(item => item.id === trialId);
  if (!trial) throw new Error('Adaptation trial not found.');
  trial.userFeedback = Boolean(liked);
  trial.status = liked ? 'approved' : 'rejected';
  if (liked && memory.learning.requireApprovalForPermanentChanges) {
    memory.preferences.communicationStyle = trial.proposedChange;
  }
  return memory;
}

export function proposePersonalityUpdate(memory, proposal) {
  return {
    id: crypto.randomUUID(),
    proposal: proposal.trim(),
    basedOn: memory.entries.slice(0, 10).map(entry => entry.id),
    createdAt: new Date().toISOString(),
    status: 'needs-user-review'
  };
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

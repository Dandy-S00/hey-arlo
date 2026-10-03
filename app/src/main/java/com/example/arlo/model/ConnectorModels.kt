package com.example.arlo.model

data class ApiProvider(
    val id: String,
    val name: String,
    val category: String,
    val logo: String,
    val auth: String,
    val description: String,
    val supportsDirectImport: Boolean = false,
    val importFormat: String = "",
    val syncTypeDescription: String = "Bi-directional notes, tasks & intentions"
)

data class ConnectionConsent(
    val providerId: String,
    val providerName: String,
    val status: String, // "awaiting-provider-auth" or "connected"
    val requestedAt: String,
    val lastSyncAt: String? = null,
    val itemsImportedCount: Int = 0,
    val syncMode: String = "Read & Import",
    val isAutoSyncEnabled: Boolean = true
)

data class SyncResultSummary(
    val totalSynced: Int,
    val successfulProviders: List<String>,
    val syncedItemsCount: Int,
    val timestamp: String,
    val details: String = ""
)

data class SyncItemResult(
    val providerId: String,
    val providerName: String,
    val success: Boolean,
    val itemsCount: Int,
    val message: String
)

object ProviderCatalog {
    val providers = listOf(
        // Note-Taking Apps
        ApiProvider(
            id = "obsidian",
            name = "Obsidian",
            category = "Note-Taking",
            logo = "💎",
            auth = "Markdown & Local Vault",
            description = "Import Markdown (.md) notes directly from your Obsidian vault or export reflections with YAML frontmatter without cloud exposure.",
            supportsDirectImport = true,
            importFormat = "Markdown (.md)",
            syncTypeDescription = "Local Markdown vault files & backlinks"
        ),
        ApiProvider(
            id = "notion",
            name = "Notion",
            category = "Note-Taking",
            logo = "N",
            auth = "OAuth 2.0 & Page Importer",
            description = "Connect workspace databases and import reflections, tasks, or exported Notion archives.",
            supportsDirectImport = true,
            importFormat = "Notion Export / Markdown",
            syncTypeDescription = "Databases, tasks & page reflections"
        ),
        ApiProvider(
            id = "google-notebooklm",
            name = "Google NotebookLM",
            category = "Note-Taking",
            logo = "📓",
            auth = "Google Takeout / Text Import",
            description = "Import synthesis notes, study guides, and research reflections exported from your NotebookLM notebooks.",
            supportsDirectImport = true,
            importFormat = "Markdown / Plain text",
            syncTypeDescription = "Synthesis notes & research sources"
        ),
        ApiProvider(
            id = "google-keep",
            name = "Google Keep",
            category = "Note-Taking",
            logo = "💡",
            auth = "Google Takeout / JSON",
            description = "Import quick thoughts, reflections, and pinned checklists from Google Keep archives.",
            supportsDirectImport = true,
            importFormat = "Google Takeout JSON / Text",
            syncTypeDescription = "Checklists, ideas & pinned items"
        ),
        ApiProvider(
            id = "logseq",
            name = "Logseq",
            category = "Note-Taking",
            logo = "🪵",
            auth = "Markdown & Org-Mode",
            description = "Connect local-first outliner journals and daily logs in Markdown or Org format.",
            supportsDirectImport = true,
            importFormat = "Markdown (.md)",
            syncTypeDescription = "Outliner journals & daily logs"
        ),

        // Cloud Storage Apps
        ApiProvider(
            id = "google-drive",
            name = "Google Drive",
            category = "Cloud Storage",
            logo = "▲",
            auth = "OAuth 2.0 & Cloud Picker",
            description = "Find and import notes, journals, and backup archives stored in your Google Drive cloud.",
            supportsDirectImport = true,
            importFormat = "Google Docs / Drive files",
            syncTypeDescription = "Cloud document backups & notes"
        ),
        ApiProvider(
            id = "dropbox",
            name = "Dropbox",
            category = "Cloud Storage",
            logo = "📦",
            auth = "OAuth 2.0 / App Key",
            description = "Connect Dropbox cloud storage to import documents, backups, and synchronize notes.",
            supportsDirectImport = true,
            importFormat = "Dropbox files / .md",
            syncTypeDescription = "Synced files & encrypted backups"
        ),
        ApiProvider(
            id = "onedrive",
            name = "Microsoft OneDrive",
            category = "Cloud Storage",
            logo = "☁",
            auth = "Microsoft Graph OAuth 2.0",
            description = "Connect to Microsoft personal or work cloud storage for notes, documents, and backups.",
            supportsDirectImport = true,
            importFormat = "OneDrive files / .txt",
            syncTypeDescription = "Microsoft OneNote & document backups"
        ),
        ApiProvider(
            id = "nextcloud",
            name = "Nextcloud / WebDAV",
            category = "Cloud Storage",
            logo = "🔒",
            auth = "WebDAV / App Password",
            description = "Connect to sovereign self-hosted cloud storage for private note vault synchronization.",
            supportsDirectImport = true,
            importFormat = "WebDAV / Markdown",
            syncTypeDescription = "Private self-hosted WebDAV vault"
        ),

        // Productivity & Dev
        ApiProvider(
            id = "google-calendar",
            name = "Google Calendar",
            category = "Productivity",
            logo = "📅",
            auth = "OAuth 2.0",
            description = "Read schedule events and create time-blocked intention sprints.",
            syncTypeDescription = "Events & daily rhythm schedule"
        ),
        ApiProvider(
            id = "todoist",
            name = "Todoist",
            category = "Productivity",
            logo = "⚡",
            auth = "OAuth 2.0",
            description = "Bi-directional task synchronization and priority filters.",
            syncTypeDescription = "Project tasks & completed items"
        ),
        ApiProvider(
            id = "github",
            name = "GitHub",
            category = "Productivity",
            logo = "🐙",
            auth = "Personal Access Token / OAuth",
            description = "Sync assigned issues, open PRs, and commit streaks to your daily momentum.",
            syncTypeDescription = "Assigned issues & commit streaks"
        ),

        // Communications
        ApiProvider(
            id = "sms-inbox",
            name = "SMS & Text Messages",
            category = "Communication",
            logo = "💬",
            auth = "Android Telephony Access",
            description = "Scan incoming SMS messages for action items and convert them into sprint tasks.",
            syncTypeDescription = "Incoming texts & action requests"
        ),
        ApiProvider(
            id = "email-inbox",
            name = "Connected Emails",
            category = "Communication",
            logo = "✉️",
            auth = "Google Account / IMAP",
            description = "Sync email digests and convert important emails into intentions.",
            syncTypeDescription = "Email threads & action digests"
        ),
        ApiProvider(
            id = "firebase-firestore",
            name = "Firebase Firestore",
            category = "Cloud Storage",
            logo = "🔥",
            auth = "Firebase Auth & Google Sign-In",
            description = "Encrypted cloud persistence with real-time multi-device sync.",
            syncTypeDescription = "Encrypted vault envelope & cloud backup"
        )
    )
}

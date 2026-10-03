package com.example.arlo.model

data class ApiProvider(
    val id: String,
    val name: String,
    val category: String,
    val logo: String,
    val auth: String,
    val description: String,
    val supportsDirectImport: Boolean = false,
    val importFormat: String = ""
)

data class ConnectionConsent(
    val providerId: String,
    val providerName: String,
    val status: String, // "awaiting-provider-auth" or "connected"
    val requestedAt: String,
    val lastSyncAt: String? = null,
    val itemsImportedCount: Int = 0,
    val syncMode: String = "Read & Import"
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
            importFormat = "Markdown (.md)"
        ),
        ApiProvider(
            id = "notion",
            name = "Notion",
            category = "Note-Taking",
            logo = "N",
            auth = "OAuth 2.0 & Page Importer",
            description = "Connect workspace databases and import reflections, tasks, or exported Notion archives.",
            supportsDirectImport = true,
            importFormat = "Notion Export / Markdown"
        ),
        ApiProvider(
            id = "google-notebooklm",
            name = "Google NotebookLM",
            category = "Note-Taking",
            logo = "📓",
            auth = "Google Takeout / Text Import",
            description = "Import synthesis notes, study guides, and research reflections exported from your NotebookLM notebooks.",
            supportsDirectImport = true,
            importFormat = "Markdown / Plain text"
        ),
        ApiProvider(
            id = "google-keep",
            name = "Google Keep",
            category = "Note-Taking",
            logo = "💡",
            auth = "Google Takeout / JSON",
            description = "Import quick thoughts, reflections, and pinned checklists from Google Keep archives.",
            supportsDirectImport = true,
            importFormat = "Google Takeout JSON / Text"
        ),
        ApiProvider(
            id = "logseq",
            name = "Logseq",
            category = "Note-Taking",
            logo = "🪵",
            auth = "Markdown & Org-Mode",
            description = "Connect local-first outliner journals and daily logs in Markdown or Org format.",
            supportsDirectImport = true,
            importFormat = "Markdown (.md)"
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
            importFormat = "Google Docs / Drive files"
        ),
        ApiProvider(
            id = "dropbox",
            name = "Dropbox",
            category = "Cloud Storage",
            logo = "📦",
            auth = "OAuth 2.0 / App Key",
            description = "Connect Dropbox cloud storage to import documents, backups, and synchronize notes.",
            supportsDirectImport = true,
            importFormat = "Dropbox files / .md"
        ),
        ApiProvider(
            id = "onedrive",
            name = "Microsoft OneDrive",
            category = "Cloud Storage",
            logo = "☁",
            auth = "Microsoft Graph OAuth 2.0",
            description = "Connect to Microsoft personal or work cloud storage for notes, documents, and backups.",
            supportsDirectImport = true,
            importFormat = "OneDrive files / .txt"
        ),
        ApiProvider(
            id = "nextcloud",
            name = "Nextcloud / WebDAV",
            category = "Cloud Storage",
            logo = "🔒",
            auth = "WebDAV / App Password",
            description = "Connect to sovereign self-hosted cloud storage for private note vault synchronization.",
            supportsDirectImport = true,
            importFormat = "WebDAV / Markdown"
        ),

        // Productivity & Dev
        ApiProvider(
            id = "google-calendar",
            name = "Google Calendar",
            category = "Productivity",
            logo = "G",
            auth = "OAuth 2.0",
            description = "Read and create calendar events after approval."
        ),
        ApiProvider(
            id = "todoist",
            name = "Todoist",
            category = "Productivity",
            logo = "T",
            auth = "OAuth 2.0",
            description = "Read and manage tasks after approval."
        ),
        ApiProvider(
            id = "slack",
            name = "Slack",
            category = "Communication",
            logo = "S",
            auth = "OAuth 2.0",
            description = "Search and send messages only where approved."
        ),
        ApiProvider(
            id = "github",
            name = "GitHub",
            category = "Development",
            logo = "GH",
            auth = "OAuth 2.0",
            description = "Read repositories, documentation, and create changes you approve."
        ),
        ApiProvider(
            id = "linear",
            name = "Linear",
            category = "Productivity",
            logo = "L",
            auth = "OAuth 2.0",
            description = "Read and update selected issues and projects."
        ),
        ApiProvider(
            id = "weather",
            name = "Weather API",
            category = "Public data",
            logo = "W",
            auth = "API key or public",
            description = "Retrieve weather data for a location."
        ),
        ApiProvider(
            id = "custom-api",
            name = "Any other API / Storage",
            category = "Custom",
            logo = "+",
            auth = "OAuth, API key, or bearer token",
            description = "Describe an API by name or add custom endpoint details.",
            supportsDirectImport = true,
            importFormat = "Custom JSON / Text"
        )
    )

    fun findProvider(query: String): ApiProvider? {
        val needle = query.trim().lowercase()
        if (needle.isEmpty()) return null
        return providers.firstOrNull { it.name.lowercase() == needle || it.id.lowercase() == needle }
            ?: providers.firstOrNull { it.name.lowercase().contains(needle) || needle.contains(it.name.lowercase()) }
    }
}

package com.example.arlo.model

data class ApiProvider(
    val id: String,
    val name: String,
    val category: String,
    val logo: String,
    val auth: String,
    val description: String
)

data class ConnectionConsent(
    val providerId: String,
    val providerName: String,
    val status: String, // "awaiting-provider-auth" or "connected"
    val requestedAt: String
)

object ProviderCatalog {
    val providers = listOf(
        ApiProvider(
            id = "google-calendar",
            name = "Google Calendar",
            category = "Productivity",
            logo = "G",
            auth = "OAuth 2.0",
            description = "Read and create calendar events after approval."
        ),
        ApiProvider(
            id = "google-drive",
            name = "Google Drive",
            category = "Files",
            logo = "D",
            auth = "OAuth 2.0",
            description = "Find and organize files you choose."
        ),
        ApiProvider(
            id = "notion",
            name = "Notion",
            category = "Notes",
            logo = "N",
            auth = "OAuth 2.0",
            description = "Read and update selected pages and databases."
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
            description = "Read repositories and create changes you approve."
        ),
        ApiProvider(
            id = "linear",
            name = "Linear",
            category = "Planning",
            logo = "L",
            auth = "OAuth 2.0",
            description = "Read and update selected issues and projects."
        ),
        ApiProvider(
            id = "todoist",
            name = "Todoist",
            category = "Tasks",
            logo = "T",
            auth = "OAuth 2.0",
            description = "Read and manage tasks after approval."
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
            name = "Any other API",
            category = "Custom",
            logo = "+",
            auth = "OAuth, API key, or bearer token",
            description = "Describe an API by name or add its connection details."
        )
    )

    fun findProvider(query: String): ApiProvider? {
        val needle = query.trim().lowercase()
        if (needle.isEmpty()) return null
        return providers.firstOrNull { it.name.lowercase() == needle || it.id.lowercase() == needle }
            ?: providers.firstOrNull { it.name.lowercase().contains(needle) || needle.contains(it.name.lowercase()) }
    }
}

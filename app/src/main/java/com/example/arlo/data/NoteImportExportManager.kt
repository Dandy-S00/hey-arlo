package com.example.arlo.data

import com.example.arlo.model.Goal
import com.example.arlo.model.Milestone
import com.example.arlo.model.Note
import java.text.SimpleDateFormat
import java.util.*

data class ParsedNoteImport(
    val fileName: String,
    val title: String,
    val body: String,
    val prompt: String = "",
    val mood: String = "",
    val tags: List<String> = emptyList(),
    val detectedDate: String? = null,
    val checklistItems: List<String> = emptyList(),
    val sourceApp: String = "Markdown File"
)

class NoteImportExportManager(
    private val repository: ArloRepository? = null
) {
    fun parseContent(
        rawText: String,
        fileName: String = "Untitled.md",
        sourceHint: String = "Markdown File"
    ): ParsedNoteImport {
        var content = rawText.trim()
        var extractedTitle = fileName.substringBeforeLast(".")
        var prompt = ""
        var mood = "Reflective"
        val tags = mutableListOf<String>()
        var detectedDate: String? = null

        // Parse YAML Frontmatter if present
        if (content.startsWith("---")) {
            val endIdx = content.indexOf("---", 3)
            if (endIdx != -1) {
                val frontmatter = content.substring(3, endIdx).trim()
                content = content.substring(endIdx + 3).trim()

                frontmatter.lines().forEach { line ->
                    val colonIdx = line.indexOf(':')
                    if (colonIdx != -1) {
                        val key = line.substring(0, colonIdx).trim().lowercase()
                        val value = line.substring(colonIdx + 1).trim().removeSurrounding("\"", "'")
                        when (key) {
                            "title" -> if (value.isNotBlank()) extractedTitle = value
                            "date" -> if (value.isNotBlank()) detectedDate = value
                            "mood" -> if (value.isNotBlank()) mood = value
                            "prompt" -> if (value.isNotBlank()) prompt = value
                            "tags" -> {
                                val tagList = value.removeSurrounding("[", "]")
                                    .split(",")
                                    .map { it.trim().removePrefix("#") }
                                    .filter { it.isNotBlank() }
                                tags.addAll(tagList)
                            }
                        }
                    }
                }
            }
        }

        // Extract first markdown header if title was default
        val lines = content.lines()
        val firstHeader = lines.firstOrNull { it.startsWith("#") }
        if (firstHeader != null && extractedTitle.equals("Untitled", ignoreCase = true)) {
            extractedTitle = firstHeader.trimStart('#', ' ').trim()
        }

        // Extract checklist items (- [ ] or - [x])
        val checklistItems = lines
            .filter { it.trim().startsWith("- [ ]") || it.trim().startsWith("- [x]") }
            .map { it.trim().substring(5).trim() }
            .filter { it.isNotBlank() }

        // Extract in-text hashtags
        val hashtagRegex = Regex("""#(\w+)""")
        hashtagRegex.findAll(content).forEach { match ->
            val tag = match.groupValues[1]
            if (tag !in tags) tags.add(tag)
        }

        return ParsedNoteImport(
            fileName = fileName,
            title = extractedTitle,
            body = content,
            prompt = prompt,
            mood = mood,
            tags = tags,
            detectedDate = detectedDate,
            checklistItems = checklistItems,
            sourceApp = sourceHint
        )
    }

    fun importAsReflection(
        parsed: ParsedNoteImport,
        moodOverride: String? = null
    ) {
        val finalMood = moodOverride ?: parsed.mood.ifBlank { "Reflective" }
        val promptText = if (parsed.prompt.isNotBlank()) {
            parsed.prompt
        } else {
            "Imported from ${parsed.sourceApp}: ${parsed.title}"
        }

        repository?.addReflection(
            body = parsed.body,
            prompt = promptText,
            mood = finalMood,
            tags = parsed.tags + parsed.sourceApp
        )
        repository?.audit("imported reflection from ${parsed.sourceApp}")
    }

    fun importAsGoal(
        parsed: ParsedNoteImport,
        category: String = "Personal Growth"
    ) {
        val milestones = parsed.checklistItems.map { Milestone(title = it, done = false) }
        val targetVal = if (milestones.isNotEmpty()) milestones.size else 1

        val goal = Goal(
            title = parsed.title,
            detail = parsed.body.take(160) + if (parsed.body.length > 160) "..." else "",
            category = category,
            targetValue = targetVal,
            currentValue = 0,
            unit = if (milestones.isNotEmpty()) "step" else "completion",
            milestones = milestones,
            done = false
        )
        repository?.addGoal(goal)
        repository?.audit("imported goal from ${parsed.sourceApp}")
    }

    fun exportReflectionsToObsidianMarkdown(notes: List<Note>): String {
        val sb = StringBuilder()
        sb.append("# Arlo Vault - Daily Reflections Export\n\n")
        sb.append("> Generated on: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())}\n")
        sb.append("> Compatible with Obsidian, Notion, Logseq, and Markdown tools.\n\n")

        notes.forEach { note ->
            sb.append("---\n")
            sb.append("date: \"${note.createdAt}\"\n")
            if (note.mood.isNotBlank()) sb.append("mood: \"${note.mood}\"\n")
            if (note.prompt.isNotBlank()) sb.append("prompt: \"${note.prompt.replace("\"", "\\\"")}\"\n")
            if (note.tags.isNotEmpty()) sb.append("tags: [${note.tags.joinToString(", ") { "\"$it\"" }}]\n")
            sb.append("---\n\n")

            if (note.prompt.isNotBlank()) {
                sb.append("### Prompt: ${note.prompt}\n\n")
            }
            sb.append("${note.body}\n\n")
            sb.append("---\n\n")
        }
        return sb.toString()
    }

    fun exportGoalsToMarkdown(goals: List<Goal>): String {
        val sb = StringBuilder()
        sb.append("# Arlo - Progress Goals Roadmap\n\n")
        sb.append("> Exported on: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())}\n\n")

        goals.forEach { goal ->
            val status = if (goal.done) "[x]" else "[ ]"
            sb.append("- $status **${goal.title}** (${goal.category}) - ${goal.progressPercent}%\n")
            if (goal.detail.isNotBlank()) {
                sb.append("  > ${goal.detail}\n")
            }
            if (goal.milestones.isNotEmpty()) {
                goal.milestones.forEach { m ->
                    val mStatus = if (m.done) "[x]" else "[ ]"
                    sb.append("    - $mStatus ${m.title}\n")
                }
            }
            sb.append("\n")
        }
        return sb.toString()
    }
}

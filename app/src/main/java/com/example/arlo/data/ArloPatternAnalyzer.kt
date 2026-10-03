package com.example.arlo.data

import com.example.arlo.model.ArloState
import com.example.arlo.model.Goal
import com.example.arlo.model.Milestone
import com.example.arlo.model.Note
import java.util.Calendar

data class ArloInsightBreakdown(
    val greeting: String,
    val hiddenPatterns: List<String>,
    val attentionItems: List<String>,
    val learningProgress: List<String>,
    val timeSensitiveGoal: Goal?,
    val nextStepSuggestion: String,
    val suggestedMilestone: Milestone?,
    val motivationSaying: String
)

object ArloPatternAnalyzer {

    fun analyze(state: ArloState): ArloInsightBreakdown {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)

        val timeGreeting = when {
            hour < 12 -> "Good morning"
            hour < 17 -> "Good afternoon"
            else -> "Good evening"
        }

        val goals = state.goals
        val notes = state.notes
        val tasks = state.tasks
        val checkIn = state.lastMoodIndex

        // 1. Identify Most Time-Sensitive Goal
        // Prioritize: uncompleted goals with incomplete milestones, or highest target progress
        val activeGoals = goals.filter { !it.done }
        val timeSensitiveGoal = activeGoals.maxByOrNull { goal ->
            // Score based on milestone urgency and partial progress
            val hasIncompleteMilestone = goal.milestones.any { !it.done }
            val progressScore = goal.progressPercent
            (if (hasIncompleteMilestone) 50 else 0) + progressScore
        } ?: goals.firstOrNull()

        val pendingMilestone = timeSensitiveGoal?.milestones?.firstOrNull { !it.done }

        val nextStepSuggestion = if (timeSensitiveGoal != null) {
            if (pendingMilestone != null) {
                "For '${timeSensitiveGoal.title}': Focus on completing milestone \"${pendingMilestone.title}\". A focused 15-minute push will unlock ${timeSensitiveGoal.progressPercent + 25}% overall completion."
            } else if (timeSensitiveGoal.targetValue > 1) {
                "For '${timeSensitiveGoal.title}': Advance your progress from ${timeSensitiveGoal.currentValue}/${timeSensitiveGoal.targetValue} ${timeSensitiveGoal.unit}s today."
            } else {
                "For '${timeSensitiveGoal.title}': Review your core motivation (\"${timeSensitiveGoal.detail.ifBlank { "Daily commitment" }}\") and mark it complete."
            }
        } else {
            "You don't have an active goal set yet! Define a milestone goal today to channel your focus."
        }

        // 2. Extract Hidden Behavior Patterns
        val hiddenPatterns = mutableListOf<String>()

        // Analyze reflection moods & timings
        if (notes.isNotEmpty()) {
            val moods = notes.mapNotNull { it.mood.takeIf { m -> m.isNotBlank() } }
            if (moods.isNotEmpty()) {
                val mostFrequentMood = moods.groupingBy { it }.eachCount().maxByOrNull { it.value }?.key
                hiddenPatterns.add("Your dominant reflection mindset is '$mostFrequentMood' (${notes.size} reflections recorded). Writing regularly helps stabilize your focus.")
            }

            val morningNotes = notes.count { note ->
                // Check if ISO timestamp or creation correlates
                note.createdAt.contains("T0") || note.createdAt.contains("T1")
            }
            if (morningNotes > notes.size / 2) {
                hiddenPatterns.add("Pattern detected: You reflect more deeply in the first half of the day, leading to calmer task execution.")
            } else {
                hiddenPatterns.add("Pattern detected: Evening reflections help you decompress and consolidate what you absorbed.")
            }
        } else {
            hiddenPatterns.add("Reflections are currently quiet. Capturing just 2 sentences per day reveals subconscious energy rhythms.")
        }

        // Analyze Goal execution patterns
        if (goals.isNotEmpty()) {
            val milestoneGoals = goals.filter { it.milestones.isNotEmpty() }
            if (milestoneGoals.isNotEmpty()) {
                val milestoneCompletionRate = milestoneGoals.count { it.done }
                hiddenPatterns.add("Structure preference: Goals with explicit milestone steps have a significantly higher momentum than generic targets.")
            }
            val categories = goals.groupBy { it.category }
            val topCategory = categories.maxByOrNull { it.value.size }?.key
            if (topCategory != null) {
                hiddenPatterns.add("Focus allocation: You invest highest dedication in '$topCategory' goals.")
            }
        }

        if (tasks.isNotEmpty()) {
            val completedRatio = tasks.count { it.done }.toFloat() / tasks.size
            if (completedRatio >= 0.6f) {
                hiddenPatterns.add("Task rhythm: High execution rate today (${(completedRatio * 100).toInt()}% completed). Momentum is on your side.")
            }
        }

        // 3. Things to Pay Attention To
        val attentionItems = mutableListOf<String>()

        val energy = checkIn
        if (energy != null) {
            when {
                energy <= 2 -> attentionItems.add("Energy was logged low ($energy/5). Protect your cognitive bandwidth by deferring non-essential chores.")
                energy == 3 -> attentionItems.add("Energy is steady ($energy/5). Ideal for deep, deliberate work without rushing.")
                else -> attentionItems.add("High energy logged ($energy/5). Great window to tackle the most demanding goal milestone.")
            }
        } else {
            attentionItems.add("No daily check-in logged yet today. Taking 5 seconds to calibrate your battery sets a realistic pace.")
        }

        val incompleteTasksCount = tasks.count { !it.done }
        if (incompleteTasksCount > 4) {
            attentionItems.add("You have $incompleteTasksCount pending items. Consider prioritizing the top 2 to avoid cognitive overload.")
        }

        if (timeSensitiveGoal != null && !timeSensitiveGoal.done && timeSensitiveGoal.milestones.any { !it.done }) {
            attentionItems.add("Urgency alert: Milestone \"${pendingMilestone?.title ?: timeSensitiveGoal.title}\" is waiting for your attention.")
        }

        if (notes.isEmpty()) {
            attentionItems.add("Zero daily reflections in vault. A quick reflection takes under 60 seconds and grounds your day.")
        }

        // 4. Things You Have Made Progress Learning
        val learningProgress = mutableListOf<String>()

        val completedGoals = goals.filter { it.done }
        if (completedGoals.isNotEmpty()) {
            learningProgress.add("Mastered & completed ${completedGoals.size} full goals, including \"${completedGoals.first().title}\".")
        }

        val completedMilestonesCount = goals.flatMap { it.milestones }.count { it.done }
        if (completedMilestonesCount > 0) {
            learningProgress.add("Reached $completedMilestonesCount incremental milestone milestones across your learning roadmap.")
        }

        if (notes.size >= 3) {
            learningProgress.add("Consistent habit established: Built an encrypted archive of ${notes.size} personal reflection entries.")
        } else if (notes.isNotEmpty()) {
            learningProgress.add("Started your journaling practice with ${notes.size} thoughtful reflection entries stored.")
        }

        val memoryCount = state.memory.entries.size
        if (memoryCount > 0) {
            learningProgress.add("Arlo's local memory holds $memoryCount verified insights about your working preferences.")
        }

        if (learningProgress.isEmpty()) {
            learningProgress.add("You're at the starting line of this journey. Completing your first task or reflection will begin populating your growth index.")
        }

        // 5. Cat Motivational Saying
        val motivationSayings = listOf(
            "“Like a cat surveying its kingdom from the highest shelf: observe, focus, and pounce on the next milestone.”",
            "“Small steps lead to great leaps. Purr through the difficulty and keep your claws sharp.”",
            "“Even a lazy afternoon cat nap is productive if your goals are clear. Take this next step!”",
            "“Paws on the keyboard, whiskers tuned to the signal. You're doing amazing today.”"
        )
        val motivation = motivationSayings.random()

        return ArloInsightBreakdown(
            greeting = "$timeGreeting! Arlo has reviewed your encrypted vault.",
            hiddenPatterns = hiddenPatterns.take(3),
            attentionItems = attentionItems.take(3),
            learningProgress = learningProgress.take(3),
            timeSensitiveGoal = timeSensitiveGoal,
            nextStepSuggestion = nextStepSuggestion,
            suggestedMilestone = pendingMilestone,
            motivationSaying = motivation
        )
    }

    fun generateInteractiveResponse(userQuery: String, breakdown: ArloInsightBreakdown): String {
        val q = userQuery.lowercase().trim()
        return when {
            q.contains("pattern") || q.contains("habit") || q.contains("behavior") -> {
                "Here are the hidden behavioral patterns I've spotted in your data:\n\n" +
                        breakdown.hiddenPatterns.mapIndexed { idx, p -> "• $p" }.joinToString("\n\n")
            }
            q.contains("attention") || q.contains("watch") || q.contains("careful") || q.contains("warn") -> {
                "Here are key things to pay attention to right now:\n\n" +
                        breakdown.attentionItems.mapIndexed { idx, p -> "⚠️ $p" }.joinToString("\n\n")
            }
            q.contains("learn") || q.contains("progress") || q.contains("growth") -> {
                "Here is what you have made progress learning and achieving:\n\n" +
                        breakdown.learningProgress.mapIndexed { idx, p -> "🌟 $p" }.joinToString("\n\n")
            }
            q.contains("goal") || q.contains("next") || q.contains("step") || q.contains("urgent") || q.contains("sensitive") -> {
                "Regarding your most time-sensitive goal:\n\n${breakdown.nextStepSuggestion}\n\nWould you like to tackle it right now?"
            }
            q.contains("cat") || q.contains("meow") || q.contains("purr") || q.contains("cheer") || q.contains("motivat") -> {
                "Meow! ${breakdown.motivationSaying}\n\nRemember: your data stays 100% on this device, and every small step counts!"
            }
            else -> {
                "Arlo here! I'm tracking your progress across goals and reflections. " +
                        "Based on your most time-sensitive goal:\n\n${breakdown.nextStepSuggestion}\n\n" +
                        "What would you like to explore: patterns, attention points, or learning progress?"
            }
        }
    }
}

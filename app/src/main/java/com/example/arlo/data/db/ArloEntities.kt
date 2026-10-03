package com.example.arlo.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.arlo.model.Goal
import com.example.arlo.model.Milestone
import com.example.arlo.model.Note
import com.example.arlo.model.Task
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

@Entity(tableName = "goals")
data class GoalEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val title: String,
    val detail: String = "",
    val done: Boolean = false,
    val category: String = "Habits & Routine",
    val targetValue: Int = 1,
    val currentValue: Int = 0,
    val unit: String = "step",
    val milestonesJson: String = "[]",
    val createdAt: String = "",
    val completedAt: String? = null
) {
    fun toDomain(): Goal {
        val milestones = mutableListOf<Milestone>()
        try {
            val arr = JSONArray(milestonesJson)
            for (i in 0 until arr.length()) {
                milestones.add(Milestone.fromJsonObject(arr.getJSONObject(i)))
            }
        } catch (_: Exception) {}

        return Goal(
            id = id,
            title = title,
            detail = detail,
            done = done,
            category = category,
            targetValue = targetValue,
            currentValue = currentValue,
            unit = unit,
            milestones = milestones,
            createdAt = createdAt,
            completedAt = completedAt
        )
    }

    companion object {
        fun fromDomain(goal: Goal): GoalEntity {
            val mArr = JSONArray()
            goal.milestones.forEach { mArr.put(it.toJsonObject()) }
            return GoalEntity(
                id = goal.id,
                title = goal.title,
                detail = goal.detail,
                done = goal.done,
                category = goal.category,
                targetValue = goal.targetValue,
                currentValue = goal.currentValue,
                unit = goal.unit,
                milestonesJson = mArr.toString(),
                createdAt = goal.createdAt,
                completedAt = goal.completedAt
            )
        }
    }
}

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val title: String,
    val done: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null
) {
    fun toDomain(): Task = Task(
        id = id,
        title = title,
        done = done
    )

    companion object {
        fun fromDomain(task: Task): TaskEntity = TaskEntity(
            id = task.id,
            title = task.title,
            done = task.done,
            createdAt = System.currentTimeMillis(),
            completedAt = if (task.done) System.currentTimeMillis() else null
        )
    }
}

@Entity(tableName = "reflections")
data class ReflectionEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val prompt: String = "",
    val body: String,
    val mood: String = "Steady",
    val createdAt: String = "",
    val createdAtMs: Long = System.currentTimeMillis()
) {
    fun toDomain(): Note = Note(
        id = id,
        prompt = prompt,
        body = body,
        mood = mood,
        createdAt = createdAt
    )

    companion object {
        fun fromDomain(note: Note): ReflectionEntity = ReflectionEntity(
            id = note.id,
            prompt = note.prompt,
            body = note.body,
            mood = note.mood,
            createdAt = note.createdAt,
            createdAtMs = System.currentTimeMillis()
        )
    }
}

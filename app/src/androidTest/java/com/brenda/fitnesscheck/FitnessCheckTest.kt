package com.brenda.fitnesscheck

import androidx.compose.ui.graphics.Color
import java.time.LocalDate
import java.util.UUID

// ========== DATA CLASSES ==========

data class UserProfile(
    val name: String = "Brenda",
    val stepGoal: Int = 10000,
    val waterGoal: Float = 2.0f,
    val sleepGoal: Float = 8.0f,
    val profilePictureIndex: Int = 0
) {
    fun toEntity(): UserProfileEntity {
        return UserProfileEntity(
            id = 1,
            name = name,
            stepGoal = stepGoal,
            waterGoal = waterGoal,
            sleepGoal = sleepGoal,
            profilePictureIndex = profilePictureIndex
        )
    }
}

data class UserProfileEntity(
    val id: Int,
    val name: String,
    val stepGoal: Int,
    val waterGoal: Float,
    val sleepGoal: Float,
    val profilePictureIndex: Int
)

enum class ChallengeType {
    STEPS, WATER, SLEEP, MEDITATION, MIXED
}

data class Challenge(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String,
    val type: ChallengeType,
    val duration: String,
    val participants: List<String>,
    val isActive: Boolean,
    val prize: String = "",
    val progress: Float = 0f,
    val maxProgress: Float = 100f
) {
    fun toChallengeEntity(): ChallengeEntity {
        return ChallengeEntity(
            id = id,
            title = title,
            description = description,
            type = type.name,
            duration = duration,
            participantIds = participants.joinToString(","),
            isActive = isActive,
            prize = prize,
            progress = progress,
            maxProgress = maxProgress
        )
    }
}

data class ChallengeEntity(
    val id: String,
    val title: String,
    val description: String,
    val type: String,
    val duration: String,
    val participantIds: String,
    val isActive: Boolean,
    val prize: String,
    val progress: Float,
    val maxProgress: Float
) {
    fun toChallenge(): Challenge {
        return Challenge(
            id = id,
            title = title,
            description = description,
            type = ChallengeType.valueOf(type),
            duration = duration,
            participants = if (participantIds.isBlank()) emptyList() else participantIds.split(","),
            isActive = isActive,
            prize = prize,
            progress = progress,
            maxProgress = maxProgress
        )
    }
}

data class DailyGoals(
    val date: LocalDate,
    val stepsAchieved: Boolean = false,
    val waterAchieved: Boolean = false,
    val sleepAchieved: Boolean = false,
    val moodLogged: Boolean = false
)

data class CalendarData(
    val date: LocalDate,
    val stepsCompleted: Boolean = false,
    val waterCompleted: Boolean = false,
    val sleepCompleted: Boolean = false,
    val moodLogged: Boolean = false
)

// ========== UTILITY FUNCTIONS ==========

fun getChallengeTypeEmoji(type: ChallengeType): String {
    return when (type) {
        ChallengeType.STEPS -> "🚶"
        ChallengeType.WATER -> "💧"
        ChallengeType.SLEEP -> "😴"
        ChallengeType.MEDITATION -> "🧘"
        ChallengeType.MIXED -> "🎯"
    }
}

fun getProfilePictureColor(index: Int): Color {
    val colors = listOf(
        Color(0xFF6200EE), // Purple
        Color(0xFF03DAC6), // Teal
        Color(0xFFFF6B6B), // Red
        Color(0xFF4ECDC4), // Turquoise
        Color(0xFF45B7D1)  // Blue
    )

    // Handle negative indices and wrap around
    val normalizedIndex = if (index < 0) 0 else index % colors.size
    return colors[normalizedIndex]
}

fun generateSampleCalendarData(): List<CalendarData> {
    val data = mutableListOf<CalendarData>()
    val today = LocalDate.now()

    // Generate data for the last 30 days + today (31 total)
    for (i in 30 downTo 0) {
        val date = today.minusDays(i.toLong())
        val calendarData = CalendarData(
            date = date,
            stepsCompleted = (i % 3 == 0), // Simulate some completed days
            waterCompleted = (i % 4 == 0),
            sleepCompleted = (i % 5 == 0),
            moodLogged = (i % 2 == 0)
        )
        data.add(calendarData)
    }

    return data
}
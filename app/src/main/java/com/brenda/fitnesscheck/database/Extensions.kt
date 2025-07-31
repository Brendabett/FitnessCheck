package com.brenda.fitnesscheck.database

import com.brenda.fitnesscheck.UserProfile
import com.brenda.fitnesscheck.DailyGoals
import java.time.LocalDate


fun UserProfile.toEntity() = UserProfileEntity(
    id = 1,
    name = name,
    stepGoal = stepGoal,
    waterGoal = waterGoal,
    sleepGoal = sleepGoal,
    profilePictureIndex = profilePictureIndex
)

fun UserProfileEntity?.toUserProfile() = this?.let {
    UserProfile(
        name = name,
        stepGoal = stepGoal,
        waterGoal = waterGoal,
        sleepGoal = sleepGoal,
        profilePictureIndex = profilePictureIndex
    )
} ?: UserProfile()

fun DailyTrackingEntity.toDailyGoals(): DailyGoals {
    return DailyGoals(
        date = LocalDate.parse(date),
        stepsAchieved = steps >= 10000, // You'd get this from user profile
        waterAchieved = waterIntake >= 2.0f, // You'd get this from user profile
        sleepAchieved = sleepHours >= 8.0f, // You'd get this from user profile
        moodLogged = moodLogged
    )
}
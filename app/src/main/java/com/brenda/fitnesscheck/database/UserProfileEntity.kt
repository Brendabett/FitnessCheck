package com.brenda.fitnesscheck.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey
    val id: Int = 1, // Single user profile
    val name: String,
    val stepGoal: Int,
    val waterGoal: Float,
    val sleepGoal: Float,
    val profilePictureIndex: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
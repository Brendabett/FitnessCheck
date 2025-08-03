package com.brenda.fitnesscheck.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "challenges")
data class ChallengeEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val description: String,
    val type: String, // ChallengeType as String
    val duration: String,
    val participantIds: String, // Comma-separated participant IDs
    val isActive: Boolean = true,
    val prize: String = "",
    val progress: Float = 0f,
    val maxProgress: Float = 100f,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
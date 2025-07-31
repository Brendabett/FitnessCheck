// ChallengeEntity.kt
package com.brenda.fitnesscheck.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "challenges")
data class ChallengeEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String,
    val type: String, // Store as String for Room
    val duration: String,
    val participantIds: String, // Store as comma-separated string
    val isActive: Boolean,
    val prize: String = "",
    val progress: Float = 0f,
    val maxProgress: Float = 100f,
    val createdAt: Long = System.currentTimeMillis()
)
// ChallengeDao.kt
package com.brenda.fitnesscheck.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ChallengeDao {
    @Query("SELECT * FROM challenges ORDER BY createdAt DESC")
    fun getAllChallenges(): Flow<List<ChallengeEntity>>

    @Query("SELECT * FROM challenges WHERE isActive = 1 ORDER BY createdAt DESC")
    fun getActiveChallenges(): Flow<List<ChallengeEntity>>

    @Insert
    suspend fun insertChallenge(challenge: ChallengeEntity)

    @Query("UPDATE challenges SET progress = :progress WHERE id = :challengeId")
    suspend fun updateChallengeProgress(challengeId: String, progress: Float)

    @Query("DELETE FROM challenges WHERE id = :challengeId")
    suspend fun deleteChallenge(challengeId: String)

    @Query("UPDATE challenges SET isActive = :isActive WHERE id = :challengeId")
    suspend fun updateChallengeStatus(challengeId: String, isActive: Boolean)
}
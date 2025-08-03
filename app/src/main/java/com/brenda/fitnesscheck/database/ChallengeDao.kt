package com.brenda.fitnesscheck.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ChallengeDao {

    @Query("SELECT * FROM challenges ORDER BY createdAt DESC")
    fun getAllChallenges(): Flow<List<ChallengeEntity>>

    @Query("SELECT * FROM challenges WHERE isActive = 1 ORDER BY createdAt DESC")
    fun getActiveChallenges(): Flow<List<ChallengeEntity>>

    @Query("SELECT * FROM challenges WHERE id = :challengeId LIMIT 1")
    suspend fun getChallengeById(challengeId: String): ChallengeEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChallenge(challenge: ChallengeEntity)

    @Update
    suspend fun updateChallenge(challenge: ChallengeEntity)

    @Query("UPDATE challenges SET progress = :progress, updatedAt = :updatedAt WHERE id = :challengeId")
    suspend fun updateChallengeProgress(
        challengeId: String,
        progress: Float,
        updatedAt: Long = System.currentTimeMillis()
    )

    @Query("UPDATE challenges SET isActive = :isActive, updatedAt = :updatedAt WHERE id = :challengeId")
    suspend fun updateChallengeStatus(
        challengeId: String,
        isActive: Boolean,
        updatedAt: Long = System.currentTimeMillis()
    )

    @Query("DELETE FROM challenges WHERE id = :challengeId")
    suspend fun deleteChallenge(challengeId: String)

    @Query("DELETE FROM challenges")
    suspend fun deleteAllChallenges()

    @Query("SELECT COUNT(*) FROM challenges")
    suspend fun getChallengeCount(): Int

    @Query("SELECT COUNT(*) FROM challenges WHERE isActive = 1")
    suspend fun getActiveChallengeCount(): Int
}
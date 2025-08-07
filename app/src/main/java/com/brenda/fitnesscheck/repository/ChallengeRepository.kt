package com.brenda.fitnesscheck.repository

import com.brenda.fitnesscheck.database.ChallengeDao
import com.brenda.fitnesscheck.database.ChallengeEntity
import kotlinx.coroutines.flow.Flow

@Suppress("unused")
class ChallengeRepository(private val challengeDao: ChallengeDao) {

    /**
     * Get all challenges as Flow for real-time updates
     */
    fun getAllChallenges(): Flow<List<ChallengeEntity>> = challengeDao.getAllChallenges()

    /**
     * Get active challenges only
     */
    fun getActiveChallenges(): Flow<List<ChallengeEntity>> = challengeDao.getActiveChallenges()

    /**
     * Get a specific challenge by ID
     */
    suspend fun getChallengeById(challengeId: String): ChallengeEntity? {
        return challengeDao.getChallengeById(challengeId)
    }

    /**
     * Insert a new challenge
     */
    suspend fun insertChallenge(challenge: ChallengeEntity) {
        challengeDao.insertChallenge(
            challenge.copy(
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    /**
     * Update an existing challenge
     */
    suspend fun updateChallenge(challenge: ChallengeEntity) {
        challengeDao.updateChallenge(
            challenge.copy(updatedAt = System.currentTimeMillis())
        )
    }

    /**
     * Update challenge progress
     */
    suspend fun updateChallengeProgress(challengeId: String, progress: Float) {
        challengeDao.updateChallengeProgress(challengeId, progress)
    }

    /**
     * Update challenge status (active/inactive)
     */
    suspend fun updateChallengeStatus(challengeId: String, isActive: Boolean) {
        challengeDao.updateChallengeStatus(challengeId, isActive)
    }

    /**
     * Delete a challenge
     */
    suspend fun deleteChallenge(challengeId: String) {
        challengeDao.deleteChallenge(challengeId)
    }

    /**
     * Delete all challenges
     */
    suspend fun deleteAllChallenges() {
        challengeDao.deleteAllChallenges()
    }

    /**
     * Get total challenge count
     */
    suspend fun getChallengeCount(): Int {
        return challengeDao.getChallengeCount()
    }

    /**
     * Get active challenge count
     */
    suspend fun getActiveChallengeCount(): Int {
        return challengeDao.getActiveChallengeCount()
    }
}
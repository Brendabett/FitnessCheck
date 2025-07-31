// ChallengeRepository.kt
package com.brenda.fitnesscheck.repository

import com.brenda.fitnesscheck.database.ChallengeDao
import com.brenda.fitnesscheck.database.ChallengeEntity
import kotlinx.coroutines.flow.Flow

class ChallengeRepository(private val challengeDao: ChallengeDao) {

    fun getAllChallenges(): Flow<List<ChallengeEntity>> = challengeDao.getAllChallenges()

    fun getActiveChallenges(): Flow<List<ChallengeEntity>> = challengeDao.getActiveChallenges()

    suspend fun insertChallenge(challenge: ChallengeEntity) {
        challengeDao.insertChallenge(challenge)
    }

    suspend fun updateChallengeProgress(challengeId: String, progress: Float) {
        challengeDao.updateChallengeProgress(challengeId, progress)
    }

    suspend fun deleteChallenge(challengeId: String) {
        challengeDao.deleteChallenge(challengeId)
    }

    suspend fun updateChallengeStatus(challengeId: String, isActive: Boolean) {
        challengeDao.updateChallengeStatus(challengeId, isActive)
    }
}
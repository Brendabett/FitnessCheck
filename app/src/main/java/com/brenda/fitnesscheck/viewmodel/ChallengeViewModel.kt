// ChallengeViewModel.kt
package com.brenda.fitnesscheck.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.brenda.fitnesscheck.database.ChallengeEntity
import com.brenda.fitnesscheck.repository.ChallengeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class ChallengeViewModel(private val repository: ChallengeRepository) : ViewModel() {

    val allChallenges: Flow<List<ChallengeEntity>> = repository.getAllChallenges()
    val activeChallenges: Flow<List<ChallengeEntity>> = repository.getActiveChallenges()

    fun addChallenge(challenge: ChallengeEntity) {
        viewModelScope.launch {
            repository.insertChallenge(challenge)
        }
    }

    fun updateProgress(challengeId: String, progress: Float) {
        viewModelScope.launch {
            repository.updateChallengeProgress(challengeId, progress)
        }
    }

    fun deleteChallenge(challengeId: String) {
        viewModelScope.launch {
            repository.deleteChallenge(challengeId)
        }
    }

    fun updateChallengeStatus(challengeId: String, isActive: Boolean) {
        viewModelScope.launch {
            repository.updateChallengeStatus(challengeId, isActive)
        }
    }
}
package com.brenda.fitnesscheck.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.brenda.fitnesscheck.database.ChallengeEntity
import com.brenda.fitnesscheck.repository.ChallengeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class ChallengeViewModel(private val repository: ChallengeRepository) : ViewModel() {

    // Exposed flows for UI
    val allChallenges: Flow<List<ChallengeEntity>> = repository.getAllChallenges()

    // Loading and error states
    private val _isLoading = MutableStateFlow(false)

    private val _error = MutableStateFlow<String?>(null)

    // Success message state
    private val _successMessage = MutableStateFlow<String?>(null)

    fun addChallenge(challenge: ChallengeEntity) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null

                repository.insertChallenge(challenge)
                _successMessage.value = "Challenge '${challenge.title}' created successfully!"
            } catch (e: Exception) {
                _error.value = "Failed to create challenge: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Update challenge progress
     */
    fun updateProgress(challengeId: String, progress: Float) {
        viewModelScope.launch {
            try {
                _error.value = null

                val validProgress = progress.coerceIn(0f, 100f)
                repository.updateChallengeProgress(challengeId, validProgress)

                if (validProgress >= 100f) {
                    _successMessage.value = "Challenge completed! 🎉"
                }
            } catch (e: Exception) {
                _error.value = "Failed to update progress: ${e.message}"
            }
        }
    }


    fun deleteChallenge(challengeId: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null

                repository.deleteChallenge(challengeId)
                _successMessage.value = "Challenge deleted successfully!"
            } catch (e: Exception) {
                _error.value = "Failed to delete challenge: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

}
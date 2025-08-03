package com.brenda.fitnesscheck.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.brenda.fitnesscheck.database.ChallengeEntity
import com.brenda.fitnesscheck.repository.ChallengeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ChallengeViewModel(private val repository: ChallengeRepository) : ViewModel() {

    // Exposed flows for UI
    val allChallenges: Flow<List<ChallengeEntity>> = repository.getAllChallenges()
    val activeChallenges: Flow<List<ChallengeEntity>> = repository.getActiveChallenges()

    // Loading and error states
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // Success message state
    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    /**
     * Add a new challenge
     */
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

    /**
     * Update challenge status
     */
    fun updateChallengeStatus(challengeId: String, isActive: Boolean) {
        viewModelScope.launch {
            try {
                _error.value = null
                repository.updateChallengeStatus(challengeId, isActive)

                val statusText = if (isActive) "activated" else "deactivated"
                _successMessage.value = "Challenge $statusText successfully!"
            } catch (e: Exception) {
                _error.value = "Failed to update challenge status: ${e.message}"
            }
        }
    }

    /**
     * Delete a challenge
     */
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

    /**
     * Get a specific challenge by ID
     */
    suspend fun getChallengeById(challengeId: String): ChallengeEntity? {
        return try {
            repository.getChallengeById(challengeId)
        } catch (e: Exception) {
            _error.value = "Failed to get challenge: ${e.message}"
            null
        }
    }

    /**
     * Delete all challenges
     */
    fun deleteAllChallenges() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null

                repository.deleteAllChallenges()
                _successMessage.value = "All challenges deleted successfully!"
            } catch (e: Exception) {
                _error.value = "Failed to delete all challenges: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Get challenge statistics
     */
    suspend fun getChallengeStats(): Pair<Int, Int> {
        return try {
            val totalCount = repository.getChallengeCount()
            val activeCount = repository.getActiveChallengeCount()
            Pair(totalCount, activeCount)
        } catch (e: Exception) {
            _error.value = "Failed to get challenge statistics: ${e.message}"
            Pair(0, 0)
        }
    }

    /**
     * Clear error message
     */
    fun clearError() {
        _error.value = null
    }

    /**
     * Clear success message
     */
    fun clearSuccessMessage() {
        _successMessage.value = null
    }

    /**
     * Complete a challenge (set progress to 100%)
     */
    fun completeChallenge(challengeId: String) {
        updateProgress(challengeId, 100f)
    }

    /**
     * Increment challenge progress by a specific amount
     */
    fun incrementProgress(challengeId: String, increment: Float) {
        viewModelScope.launch {
            try {
                val challenge = repository.getChallengeById(challengeId)
                if (challenge != null) {
                    val newProgress = (challenge.progress + increment).coerceAtMost(100f)
                    updateProgress(challengeId, newProgress)
                }
            } catch (e: Exception) {
                _error.value = "Failed to increment progress: ${e.message}"
            }
        }
    }
}
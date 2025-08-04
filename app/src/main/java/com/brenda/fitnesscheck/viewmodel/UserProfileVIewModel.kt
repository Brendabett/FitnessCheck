package com.brenda.fitnesscheck.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.brenda.fitnesscheck.database.UserProfileEntity
import com.brenda.fitnesscheck.repository.UserProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class UserProfileViewModel(private val repository: UserProfileRepository) : ViewModel() {

    private val _userProfile = MutableStateFlow<UserProfileEntity?>(null)
    val userProfile: StateFlow<UserProfileEntity?> = _userProfile.asStateFlow()

    private val _isLoading = MutableStateFlow(true)

    private val _error = MutableStateFlow<String?>(null)

    init {
        loadUserProfile()
    }

    /**
     * Load user profile from database
     */
    private fun loadUserProfile() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null

                // Create default profile if none exists
                repository.createDefaultProfileIfNeeded()

                // Observe profile changes
                repository.getUserProfile().collect { profile ->
                    _userProfile.value = profile
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                _error.value = "Failed to load profile: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    fun updateStepGoal(stepGoal: Int) {
        viewModelScope.launch {
            try {
                _error.value = null
                repository.updateStepGoal(stepGoal)
            } catch (e: Exception) {
                _error.value = "Failed to update step goal: ${e.message}"
            }
        }
    }

    fun updateWaterGoal(waterGoal: Float) {
        viewModelScope.launch {
            try {
                _error.value = null
                repository.updateWaterGoal(waterGoal)
            } catch (e: Exception) {
                _error.value = "Failed to update water goal: ${e.message}"
            }
        }
    }

    fun updateSleepGoal(sleepGoal: Float) {
        viewModelScope.launch {
            try {
                _error.value = null
                repository.updateSleepGoal(sleepGoal)
            } catch (e: Exception) {
                _error.value = "Failed to update sleep goal: ${e.message}"
            }
        }
    }

    /**
     * Reset profile to default values
     */
    fun resetProfile() {
        viewModelScope.launch {
            try {
                _error.value = null
                repository.resetToDefault()
            } catch (e: Exception) {
                _error.value = "Failed to reset profile: ${e.message}"
            }
        }
    }

    /**
     * Batch update multiple fields efficiently
     */
    fun updateMultipleFields(
        name: String? = null,
        stepGoal: Int? = null,
        waterGoal: Float? = null,
        sleepGoal: Float? = null,
        profilePictureIndex: Int? = null
    ) {
        viewModelScope.launch {
            try {
                _error.value = null

                val currentProfile = repository.getUserProfileOnce()
                if (currentProfile != null) {
                    val updatedProfile = currentProfile.copy(
                        name = name ?: currentProfile.name,
                        stepGoal = stepGoal ?: currentProfile.stepGoal,
                        waterGoal = waterGoal ?: currentProfile.waterGoal,
                        sleepGoal = sleepGoal ?: currentProfile.sleepGoal,
                        profilePictureIndex = profilePictureIndex ?: currentProfile.profilePictureIndex,
                        updatedAt = System.currentTimeMillis()
                    )
                    repository.updateProfile(updatedProfile)
                }
            } catch (e: Exception) {
                _error.value = "Failed to update profile: ${e.message}"
            }
        }
    }

}

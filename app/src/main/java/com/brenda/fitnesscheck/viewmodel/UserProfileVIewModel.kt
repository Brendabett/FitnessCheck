package com.brenda.fitnesscheck.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.brenda.fitnesscheck.database.UserProfileEntity
import com.brenda.fitnesscheck.repository.UserProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// REMOVED duplicate UserProfile data class - using the one from MainActivity

class UserProfileViewModel(private val repository: UserProfileRepository) : ViewModel() {

    private val _userProfile = MutableStateFlow<UserProfileEntity?>(null)
    val userProfile: StateFlow<UserProfileEntity?> = _userProfile.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

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

    /**
     * Update the entire profile
     */
    fun updateProfile(profile: UserProfileEntity) {
        viewModelScope.launch {
            try {
                _error.value = null
                repository.updateProfile(profile)
            } catch (e: Exception) {
                _error.value = "Failed to update profile: ${e.message}"
            }
        }
    }

    /**
     * Update individual profile fields
     */
    fun updateName(name: String) {
        viewModelScope.launch {
            try {
                _error.value = null
                repository.updateName(name)
            } catch (e: Exception) {
                _error.value = "Failed to update name: ${e.message}"
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

    fun updateProfilePictureIndex(index: Int) {
        viewModelScope.launch {
            try {
                _error.value = null
                repository.updateProfilePictureIndex(index)
            } catch (e: Exception) {
                _error.value = "Failed to update profile picture: ${e.message}"
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
     * Clear any error messages
     */
    fun clearError() {
        _error.value = null
    }

    /**
     * Convenience method to get current profile as UI model
     */
    fun getCurrentProfileAsUIModel(): UserProfileEntity? {
        return _userProfile.value
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

    /**
     * Additional helper functions for better usability
     */

    /**
     * Check if profile is loaded
     */
    fun isProfileLoaded(): Boolean {
        return _userProfile.value != null && !_isLoading.value
    }

    /**
     * Get profile synchronously (use with caution)
     */
    fun getProfileSnapshot(): UserProfileEntity? {
        return _userProfile.value
    }

    /**
     * Validate profile data before updating
     */
    private fun validateProfileData(
        name: String? = null,
        stepGoal: Int? = null,
        waterGoal: Float? = null,
        sleepGoal: Float? = null
    ): String? {
        return when {
            name != null && name.isBlank() -> "Name cannot be empty"
            stepGoal != null && stepGoal <= 0 -> "Step goal must be greater than 0"
            waterGoal != null && waterGoal <= 0 -> "Water goal must be greater than 0"
            sleepGoal != null && sleepGoal <= 0 -> "Sleep goal must be greater than 0"
            else -> null
        }
    }

    /**
     * Safe update with validation
     */
    fun updateProfileSafely(profile: UserProfileEntity) {
        viewModelScope.launch {
            try {
                _error.value = null

                val validationError = validateProfileData(
                    name = profile.name,
                    stepGoal = profile.stepGoal,
                    waterGoal = profile.waterGoal,
                    sleepGoal = profile.sleepGoal
                )

                if (validationError != null) {
                    _error.value = validationError
                    return@launch
                }

                repository.updateProfile(profile)
            } catch (e: Exception) {
                _error.value = "Failed to update profile: ${e.message}"
            }
        }
    }

    /**
     * Force refresh profile from database
     */
    fun refreshProfile() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null

                val profile = repository.getUserProfileOnce()
                _userProfile.value = profile
                _isLoading.value = false
            } catch (e: Exception) {
                _error.value = "Failed to refresh profile: ${e.message}"
                _isLoading.value = false
            }
        }
    }
}

/**
 * Factory for creating UserProfileViewModel with repository dependency
 */
class UserProfileViewModelFactory(
    private val repository: UserProfileRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UserProfileViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return UserProfileViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
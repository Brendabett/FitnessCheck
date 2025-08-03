package com.brenda.fitnesscheck.repository

import com.brenda.fitnesscheck.database.UserProfileDao
import com.brenda.fitnesscheck.database.UserProfileEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class UserProfileRepository(private val userProfileDao: UserProfileDao) {

    /**
     * Get user profile as Flow for real-time updates
     */
    fun getUserProfile(): Flow<UserProfileEntity?> = userProfileDao.getUserProfile()

    /**
     * Get user profile once (for one-time operations)
     */
    suspend fun getUserProfileOnce(): UserProfileEntity? = userProfileDao.getUserProfileOnce()

    /**
     * Insert or update the entire profile
     */
    suspend fun insertOrUpdateProfile(profile: UserProfileEntity) {
        userProfileDao.insertOrUpdateProfile(
            profile.copy(updatedAt = System.currentTimeMillis())
        )
    }

    /**
     * Update existing profile
     */
    suspend fun updateProfile(profile: UserProfileEntity) {
        userProfileDao.updateProfile(
            profile.copy(updatedAt = System.currentTimeMillis())
        )
    }

    /**
     * Update individual fields - more efficient for single field updates
     */
    suspend fun updateName(name: String) {
        userProfileDao.updateName(name, System.currentTimeMillis())
    }

    suspend fun updateStepGoal(stepGoal: Int) {
        userProfileDao.updateStepGoal(stepGoal, System.currentTimeMillis())
    }

    suspend fun updateWaterGoal(waterGoal: Float) {
        userProfileDao.updateWaterGoal(waterGoal, System.currentTimeMillis())
    }

    suspend fun updateSleepGoal(sleepGoal: Float) {
        userProfileDao.updateSleepGoal(sleepGoal, System.currentTimeMillis())
    }

    suspend fun updateProfilePictureIndex(index: Int) {
        userProfileDao.updateProfilePictureIndex(index, System.currentTimeMillis())
    }

    /**
     * Delete the profile (for reset functionality)
     */
    suspend fun deleteProfile() = userProfileDao.deleteProfile()

    /**
     * Check if profile exists
     */
    suspend fun profileExists(): Boolean = userProfileDao.profileExists() > 0

    /**
     * Create default profile if none exists
     */
    suspend fun createDefaultProfileIfNeeded() {
        if (!profileExists()) {
            val defaultProfile = UserProfileEntity(
                id = 1,
                name = "Brenda",
                stepGoal = 10000,
                waterGoal = 2.0f,
                sleepGoal = 8.0f,
                profilePictureIndex = 0,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            insertOrUpdateProfile(defaultProfile)
        }
    }

    /**
     * Reset profile to default values
     */
    suspend fun resetToDefault() {
        val defaultProfile = UserProfileEntity(
            id = 1,
            name = "Brenda",
            stepGoal = 10000,
            waterGoal = 2.0f,
            sleepGoal = 8.0f,
            profilePictureIndex = 0,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        insertOrUpdateProfile(defaultProfile)
    }

    /**
     * Get profile with fallback to default
     */
    suspend fun getProfileOrDefault(): UserProfileEntity {
        return getUserProfileOnce() ?: run {
            createDefaultProfileIfNeeded()
            getUserProfile().first()!!
        }
    }
}
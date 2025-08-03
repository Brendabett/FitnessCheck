package com.brenda.fitnesscheck.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserProfileDao {

    /**
     * Get user profile as Flow for real-time updates
     */
    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    fun getUserProfile(): Flow<UserProfileEntity?>

    /**
     * Get user profile once (for one-time operations)
     */
    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    suspend fun getUserProfileOnce(): UserProfileEntity?

    /**
     * Insert or replace the entire profile
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProfile(profile: UserProfileEntity)

    /**
     * Update existing profile
     */
    @Update
    suspend fun updateProfile(profile: UserProfileEntity)

    /**
     * Delete the profile (for reset functionality)
     */
    @Query("DELETE FROM user_profile")
    suspend fun deleteProfile()

    /**
     * Update individual fields - more efficient for single field updates
     */
    @Query("UPDATE user_profile SET name = :name, updatedAt = :updatedAt WHERE id = 1")
    suspend fun updateName(name: String, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE user_profile SET stepGoal = :stepGoal, updatedAt = :updatedAt WHERE id = 1")
    suspend fun updateStepGoal(stepGoal: Int, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE user_profile SET waterGoal = :waterGoal, updatedAt = :updatedAt WHERE id = 1")
    suspend fun updateWaterGoal(waterGoal: Float, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE user_profile SET sleepGoal = :sleepGoal, updatedAt = :updatedAt WHERE id = 1")
    suspend fun updateSleepGoal(sleepGoal: Float, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE user_profile SET profilePictureIndex = :index, updatedAt = :updatedAt WHERE id = 1")
    suspend fun updateProfilePictureIndex(index: Int, updatedAt: Long = System.currentTimeMillis())

    /**
     * Check if profile exists
     */
    @Query("SELECT COUNT(*) FROM user_profile WHERE id = 1")
    suspend fun profileExists(): Int

    /**
     * Get profile creation timestamp
     */
    @Query("SELECT createdAt FROM user_profile WHERE id = 1 LIMIT 1")
    suspend fun getProfileCreationTime(): Long?

    /**
     * Get profile last updated timestamp
     */
    @Query("SELECT updatedAt FROM user_profile WHERE id = 1 LIMIT 1")
    suspend fun getProfileLastUpdatedTime(): Long?

    /**
     * Update only the timestamp (useful for tracking activity)
     */
    @Query("UPDATE user_profile SET updatedAt = :updatedAt WHERE id = 1")
    suspend fun updateTimestamp(updatedAt: Long = System.currentTimeMillis())
}
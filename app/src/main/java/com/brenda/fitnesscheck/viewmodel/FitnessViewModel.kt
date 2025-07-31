package com.brenda.fitnesscheck.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import androidx.compose.runtime.collectAsState
import com.brenda.fitnesscheck.UserProfile
import com.brenda.fitnesscheck.database.*

class FitnessViewModel(private val repository: FitnessRepository) : ViewModel() {

    private val _userProfile = MutableStateFlow(UserProfile())
    val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

    private val _todayTracking = MutableStateFlow<DailyTrackingEntity?>(null)
    val todayTracking: StateFlow<DailyTrackingEntity?> = _todayTracking.asStateFlow()

    private val _achievements = MutableStateFlow<List<AchievementEntity>>(emptyList())
    val achievements: StateFlow<List<AchievementEntity>> = _achievements.asStateFlow()

    private val _calendarData = MutableStateFlow<List<DailyTrackingEntity>>(emptyList())
    val calendarData: StateFlow<List<DailyTrackingEntity>> = _calendarData.asStateFlow()

    init {
        loadUserProfile()
        loadTodayTracking()
        loadAchievements()
        loadCalendarData()
    }

    private fun loadUserProfile() {
        viewModelScope.launch {
            repository.getUserProfile().collect { profileEntity ->
                _userProfile.value = profileEntity.toUserProfile()
            }
        }
    }

    private fun loadTodayTracking() {
        viewModelScope.launch {
            repository.getTodayTrackingFlow().collect { tracking ->
                _todayTracking.value = tracking
            }
        }
    }

    private fun loadAchievements() {
        viewModelScope.launch {
            repository.getAllAchievements().collect { achievements ->
                _achievements.value = achievements
            }
        }
    }

    private fun loadCalendarData() {
        viewModelScope.launch {
            repository.getCalendarData().collect { data ->
                _calendarData.value = data
            }
        }
    }

    fun updateUserProfile(profile: UserProfile) {
        viewModelScope.launch {
            repository.updateUserProfile(profile.toEntity())
            _userProfile.value = profile
        }
    }

    fun updateSteps(steps: Int) {
        viewModelScope.launch {
            repository.updateDailyTracking(steps = steps)
        }
    }

    fun updateWaterIntake(water: Float) {
        viewModelScope.launch {
            repository.updateDailyTracking(waterIntake = water)
        }
    }

    fun updateSleepHours(sleep: Float) {
        viewModelScope.launch {
            repository.updateDailyTracking(sleepHours = sleep)
        }
    }

    fun updateMood(mood: Float) {
        viewModelScope.launch {
            repository.updateDailyTracking(mood = mood, moodLogged = true)
        }
    }

    fun startMeditationSession(duration: Int): Long {
        var sessionId = 0L
        viewModelScope.launch {
            sessionId = repository.startMeditationSession(duration)
        }
        return sessionId
    }

    fun completeMeditationSession(id: Long) {
        viewModelScope.launch {
            repository.completeMeditationSession(id)
        }
    }

    // Helper functions to get current values with defaults
    fun getCurrentSteps(): Int = _todayTracking.value?.steps ?: 0
    fun getCurrentWater(): Float = _todayTracking.value?.waterIntake ?: 0f
    fun getCurrentSleep(): Float = _todayTracking.value?.sleepHours ?: 0f
    fun getCurrentMood(): Float = _todayTracking.value?.mood ?: 5f

    // Check if goals are achieved
    fun isStepGoalAchieved(): Boolean = getCurrentSteps() >= _userProfile.value.stepGoal
    fun isWaterGoalAchieved(): Boolean = getCurrentWater() >= _userProfile.value.waterGoal
    fun isSleepGoalAchieved(): Boolean = getCurrentSleep() >= _userProfile.value.sleepGoal
}

class FitnessViewModelFactory(private val repository: FitnessRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FitnessViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FitnessViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
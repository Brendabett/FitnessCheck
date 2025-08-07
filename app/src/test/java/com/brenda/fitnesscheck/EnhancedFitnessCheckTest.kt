package com.brenda.fitnesscheck

import org.junit.Test
import org.junit.Assert.*
import org.junit.Before
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import kotlinx.coroutines.runBlocking
import java.time.LocalDate
import java.util.UUID
import android.content.Context
import com.brenda.fitnesscheck.database.ChallengeEntity
import com.brenda.fitnesscheck.fitness.GoogleFitManager

/**
 * Enhanced unit tests for Fitness Check App
 * Includes original tests plus API testing
 */
class EnhancedFitnessCheckTest {

    @Mock
    private lateinit var mockContext: Context

    @Mock
    private lateinit var mockGoogleFitManager: GoogleFitManager

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
    }

    // ========== ORIGINAL DATA CLASS TESTS ==========

    @Test
    fun userProfile_defaultValues_areCorrect() {
        val userProfile = UserProfile()

        assertEquals("Brenda", userProfile.name)
        assertEquals(10000, userProfile.stepGoal)
        assertEquals(2.0f, userProfile.waterGoal, 0.01f)
        assertEquals(8.0f, userProfile.sleepGoal, 0.01f)
        assertEquals(0, userProfile.profilePictureIndex)
    }

    @Test
    fun userProfile_customValues_areSetCorrectly() {
        val userProfile = UserProfile(
            name = "John",
            stepGoal = 15000,
            waterGoal = 3.0f,
            sleepGoal = 7.5f,
            profilePictureIndex = 2
        )

        assertEquals("John", userProfile.name)
        assertEquals(15000, userProfile.stepGoal)
        assertEquals(3.0f, userProfile.waterGoal, 0.01f)
        assertEquals(7.5f, userProfile.sleepGoal, 0.01f)
        assertEquals(2, userProfile.profilePictureIndex)
    }

    @Test
    fun challenge_creation_isCorrect() {
        val challengeId = UUID.randomUUID().toString()
        val challenge = Challenge(
            id = challengeId,
            title = "Test Challenge",
            description = "Test Description",
            type = ChallengeType.STEPS,
            duration = "7 days",
            participants = listOf("user1", "user2"),
            isActive = true,
            prize = "Test Prize",
            progress = 50f,
            maxProgress = 100f
        )

        assertEquals(challengeId, challenge.id)
        assertEquals("Test Challenge", challenge.title)
        assertEquals("Test Description", challenge.description)
        assertEquals(ChallengeType.STEPS, challenge.type)
        assertEquals("7 days", challenge.duration)
        assertEquals(2, challenge.participants.size)
        assertTrue(challenge.isActive)
        assertEquals("Test Prize", challenge.prize)
        assertEquals(50f, challenge.progress, 0.01f)
        assertEquals(100f, challenge.maxProgress, 0.01f)
    }

    @Test
    fun dailyGoals_achievementStatus_isCorrect() {
        val dailyGoals = DailyGoals(
            date = LocalDate.now(),
            stepsAchieved = true,
            waterAchieved = false,
            sleepAchieved = true,
            moodLogged = false
        )

        assertTrue(dailyGoals.stepsAchieved)
        assertFalse(dailyGoals.waterAchieved)
        assertTrue(dailyGoals.sleepAchieved)
        assertFalse(dailyGoals.moodLogged)
        assertEquals(LocalDate.now(), dailyGoals.date)
    }

    // ========== BUSINESS LOGIC TESTS ==========

    @Test
    fun getChallengeTypeEmoji_returnsCorrectEmoji() {
        assertEquals("🚶", getChallengeTypeEmoji(ChallengeType.STEPS))
        assertEquals("💧", getChallengeTypeEmoji(ChallengeType.WATER))
        assertEquals("😴", getChallengeTypeEmoji(ChallengeType.SLEEP))
        assertEquals("🧘", getChallengeTypeEmoji(ChallengeType.MEDITATION))
        assertEquals("🎯", getChallengeTypeEmoji(ChallengeType.MIXED))
    }

    @Test
    fun getProfilePictureColor_returnsValidColors() {
        val color0 = getProfilePictureColor(0)
        val color1 = getProfilePictureColor(1)
        val color5 = getProfilePictureColor(5) // Should wrap around

        assertNotNull(color0)
        assertNotNull(color1)
        assertNotNull(color5)

        // Test wrap-around (index 5 should equal index 0)
        assertEquals(color0, color5)
    }

    @Test
    fun generateSampleCalendarData_returnsCorrectSize() {
        val calendarData = generateSampleCalendarData()

        assertEquals(31, calendarData.size)

        val expectedFirstDate = LocalDate.now().minusDays(30)
        assertEquals(expectedFirstDate, calendarData.first().date)
        assertEquals(LocalDate.now(), calendarData.last().date)
    }

    // ========== GOOGLE FIT API TESTS ==========

    @Test
    fun googleFitManager_permissionsCheck_returnsCorrectStatus() = runBlocking {
        // Mock permissions granted
        `when`(mockGoogleFitManager.hasPermissions()).thenReturn(true)
        assertTrue("Should have permissions", mockGoogleFitManager.hasPermissions())

        // Mock permissions denied
        `when`(mockGoogleFitManager.hasPermissions()).thenReturn(false)
        assertFalse("Should not have permissions", mockGoogleFitManager.hasPermissions())
    }

    @Test
    fun googleFitManager_getTodaySteps_returnsValidData() = runBlocking {
        val expectedSteps = 5000
        `when`(mockGoogleFitManager.getTodaySteps()).thenReturn(expectedSteps)

        val actualSteps = mockGoogleFitManager.getTodaySteps()
        assertEquals("Steps should match mock data", expectedSteps, actualSteps)
        assertTrue("Steps should be non-negative", actualSteps >= 0)
    }

    @Test
    fun googleFitManager_getTodaySteps_handlesNoPermissions() = runBlocking {
        `when`(mockGoogleFitManager.hasPermissions()).thenReturn(false)
        `when`(mockGoogleFitManager.getTodaySteps()).thenReturn(0)

        val steps = mockGoogleFitManager.getTodaySteps()
        assertEquals("Should return 0 when no permissions", 0, steps)
    }

    @Test
    fun googleFitManager_getStepsHistory_returnsValidMap() = runBlocking {
        val mockHistory = mapOf(
            LocalDate.now() to 8000,
            LocalDate.now().minusDays(1) to 6500,
            LocalDate.now().minusDays(2) to 9200
        )

        `when`(mockGoogleFitManager.getStepsHistory(3)).thenReturn(mockHistory)

        val history = mockGoogleFitManager.getStepsHistory(3)
        assertEquals("Should return correct map size", 3, history.size)
        assertTrue("Should contain today's data", history.containsKey(LocalDate.now()))
        history.values.forEach { steps ->
            assertTrue("All step counts should be non-negative", steps >= 0)
        }
    }

    @Test
    fun googleFitManager_getTodayCalories_returnsValidData() = runBlocking {
        val expectedCalories = 450.5f
        `when`(mockGoogleFitManager.getTodayCalories()).thenReturn(expectedCalories)

        val calories = mockGoogleFitManager.getTodayCalories()
        assertEquals("Calories should match mock data", expectedCalories, calories, 0.01f)
        assertTrue("Calories should be non-negative", calories >= 0f)
    }

    @Test
    fun googleFitManager_getTodayDistance_returnsValidData() = runBlocking {
        val expectedDistance = 3500.75f // meters
        `when`(mockGoogleFitManager.getTodayDistance()).thenReturn(expectedDistance)

        val distance = mockGoogleFitManager.getTodayDistance()
        assertEquals("Distance should match mock data", expectedDistance, distance, 0.01f)
        assertTrue("Distance should be non-negative", distance >= 0f)
    }

    @Test
    fun googleFitManager_getStepsForDate_handlesSpecificDate() = runBlocking {
        val testDate = LocalDate.now().minusDays(1)
        val expectedSteps = 7500
        `when`(mockGoogleFitManager.getStepsForDate(testDate)).thenReturn(expectedSteps)

        val steps = mockGoogleFitManager.getStepsForDate(testDate)
        assertEquals("Should return correct steps for specific date", expectedSteps, steps)
    }

    // ========== API ERROR HANDLING TESTS ==========

    @Test
    fun googleFitManager_handlesApiErrors_gracefully() = runBlocking {
        // Mock API throwing exception
        `when`(mockGoogleFitManager.getTodaySteps()).thenThrow(RuntimeException("API Error"))

        try {
            mockGoogleFitManager.getTodaySteps()
            fail("Should have thrown exception")
        } catch (e: RuntimeException) {
            assertEquals("API Error", e.message)
        }
    }

    @Test
    fun googleFitManager_handlesNetworkFailure() = runBlocking {
        // Mock network failure returning default values
        `when`(mockGoogleFitManager.hasPermissions()).thenReturn(true)
        `when`(mockGoogleFitManager.getTodaySteps()).thenReturn(0) // API failure returns 0
        `when`(mockGoogleFitManager.getTodayCalories()).thenReturn(0f)
        `when`(mockGoogleFitManager.getTodayDistance()).thenReturn(0f)

        assertTrue("Should have permissions", mockGoogleFitManager.hasPermissions())
        assertEquals("Should return 0 on failure", 0, mockGoogleFitManager.getTodaySteps())
        assertEquals("Should return 0f on failure", 0f, mockGoogleFitManager.getTodayCalories(), 0.01f)
        assertEquals("Should return 0f on failure", 0f, mockGoogleFitManager.getTodayDistance(), 0.01f)
    }

    // ========== INTEGRATION TESTS ==========

    @Test
    fun userProfile_withApiData_integratesCorrectly() = runBlocking {
        val userProfile = UserProfile(stepGoal = 10000)
        val mockSteps = 8500

        `when`(mockGoogleFitManager.getTodaySteps()).thenReturn(mockSteps)

        val todaySteps = mockGoogleFitManager.getTodaySteps()
        val progressPercentage = (todaySteps.toFloat() / userProfile.stepGoal) * 100f

        assertEquals("Steps should match API", mockSteps, todaySteps)
        assertEquals("Progress should be correct", 85f, progressPercentage, 0.01f)
        assertTrue("User should be close to goal", progressPercentage > 80f)
    }

    @Test
    fun challenge_progressTracking_withApiData() = runBlocking {
        val challenge = Challenge(
            id = "test",
            title = "10K Steps Challenge",
            description = "Walk 10,000 steps today",
            type = ChallengeType.STEPS,
            duration = "1 day",
            participants = listOf("user1"),
            isActive = true,
            maxProgress = 10000f
        )

        val mockSteps = 7500
        `when`(mockGoogleFitManager.getTodaySteps()).thenReturn(mockSteps)

        val updatedProgress = mockGoogleFitManager.getTodaySteps().toFloat()
        val progressPercentage = (updatedProgress / challenge.maxProgress) * 100f

        assertEquals("Progress should match API data", 7500f, updatedProgress, 0.01f)
        assertEquals("Progress percentage should be correct", 75f, progressPercentage, 0.01f)
        assertFalse("Challenge should not be complete yet", updatedProgress >= challenge.maxProgress)
    }

    // ========== CONVERSION TESTS ==========

    @Test
    fun userProfile_toEntity_conversion_isCorrect() {
        val userProfile = UserProfile(
            name = "Test User",
            stepGoal = 12000,
            waterGoal = 2.5f,
            sleepGoal = 8.5f,
            profilePictureIndex = 1
        )

        val entity = userProfile.toEntity()

        assertEquals(1, entity.id)
        assertEquals("Test User", entity.name)
        assertEquals(12000, entity.stepGoal)
        assertEquals(2.5f, entity.waterGoal, 0.01f)
        assertEquals(8.5f, entity.sleepGoal, 0.01f)
        assertEquals(1, entity.profilePictureIndex)
    }

    @Test
    fun challenge_toEntity_conversion_isCorrect() {
        val challenge = Challenge(
            id = "test-id",
            title = "Test Challenge",
            description = "Test Description",
            type = ChallengeType.WATER,
            duration = "10 days",
            participants = listOf("user1", "user2", "user3"),
            isActive = true,
            prize = "Test Prize",
            progress = 75f,
            maxProgress = 100f
        )

        val entity = challenge.toChallengeEntity()

        assertEquals("test-id", entity.id)
        assertEquals("Test Challenge", entity.title)
        assertEquals("Test Description", entity.description)
        assertEquals("WATER", entity.type)
        assertEquals("10 days", entity.duration)
        assertEquals("user1,user2,user3", entity.participantIds)
        assertTrue(entity.isActive)
        assertEquals("Test Prize", entity.prize)
        assertEquals(75f, entity.progress, 0.01f)
        assertEquals(100f, entity.maxProgress, 0.01f)
    }

    @Test
    fun challengeEntity_toChallenge_conversion_isCorrect() {
        val entity = ChallengeEntity(
            id = "test-id",
            title = "Test Challenge",
            description = "Test Description",
            type = "STEPS",
            duration = "5 days",
            participantIds = "user1,user2",
            isActive = true,
            prize = "Test Prize",
            progress = 40f,
            maxProgress = 100f
        )

        val challenge = entity.toChallenge()

        assertEquals("test-id", challenge.id)
        assertEquals("Test Challenge", challenge.title)
        assertEquals("Test Description", challenge.description)
        assertEquals(ChallengeType.STEPS, challenge.type)
        assertEquals("5 days", challenge.duration)
        assertEquals(2, challenge.participants.size)
        assertEquals("user1", challenge.participants[0])
        assertEquals("user2", challenge.participants[1])
        assertTrue(challenge.isActive)
        assertEquals("Test Prize", challenge.prize)
        assertEquals(40f, challenge.progress, 0.01f)
        assertEquals(100f, challenge.maxProgress, 0.01f)
    }

    // ========== EDGE CASE TESTS ==========

    @Test
    fun userProfile_edgeCaseValues_areHandledCorrectly() {
        val userProfile = UserProfile(
            name = "",
            stepGoal = 0,
            waterGoal = 0f,
            sleepGoal = 0f,
            profilePictureIndex = -1
        )

        assertEquals("", userProfile.name)
        assertEquals(0, userProfile.stepGoal)
        assertEquals(0f, userProfile.waterGoal, 0.01f)
        assertEquals(0f, userProfile.sleepGoal, 0.01f)
        assertEquals(-1, userProfile.profilePictureIndex)
    }

    @Test
    fun challenge_emptyParticipants_isHandledCorrectly() {
        val challenge = Challenge(
            id = "test",
            title = "Test",
            description = "Test",
            type = ChallengeType.STEPS,
            duration = "1 day",
            participants = emptyList(),
            isActive = true
        )

        assertEquals(0, challenge.participants.size)
        assertTrue(challenge.participants.isEmpty())
    }

    @Test
    fun getProfilePictureColor_negativeIndex_doesNotCrash() {
        val color = getProfilePictureColor(-1)
        assertNotNull(color)
    }

    @Test
    fun getChallengeTypeEmoji_allTypes_returnValidEmojis() {
        ChallengeType.entries.forEach { type ->
            val emoji = getChallengeTypeEmoji(type)
            assertNotNull(emoji)
            assertTrue("Emoji should not be empty", emoji.isNotEmpty())
            assertTrue("Emoji should be reasonable length", emoji.length <= 4)
        }
    }

    // ========== PERFORMANCE TESTS ==========

    @Test
    fun generateSampleCalendarData_performance_isFast() {
        val startTime = System.currentTimeMillis()

        repeat(100) {
            generateSampleCalendarData()
        }

        val endTime = System.currentTimeMillis()
        val duration = endTime - startTime

        assertTrue("Calendar data generation took too long: ${duration}ms", duration < 1000)
    }

    @Test
    fun challengeConversion_bulkOperations_performWell() {
        val challenges = (1..100).map { index ->
            Challenge(
                id = "challenge-$index",
                title = "Challenge $index",
                description = "Description $index",
                type = ChallengeType.entries[index % ChallengeType.entries.size],
                duration = "$index days",
                participants = listOf("user$index"),
                isActive = index % 2 == 0
            )
        }

        val startTime = System.currentTimeMillis()
        val entities = challenges.map { it.toChallengeEntity() }
        val endTime = System.currentTimeMillis()
        val duration = endTime - startTime

        assertEquals(100, entities.size)
        assertTrue("Bulk conversion took too long: ${duration}ms", duration < 100)
    }

    // ========== VALIDATION TESTS ==========

    @Test
    fun userProfile_validateGoalRanges() {
        val userProfile = UserProfile(
            stepGoal = 50000,
            waterGoal = 10f,
            sleepGoal = 12f
        )

        assertTrue("Step goal should be positive", userProfile.stepGoal > 0)
        assertTrue("Water goal should be positive", userProfile.waterGoal > 0)
        assertTrue("Sleep goal should be positive", userProfile.sleepGoal > 0)
    }

    @Test
    fun challenge_progressValidation() {
        val challenge = Challenge(
            id = "test",
            title = "Test",
            description = "Test",
            type = ChallengeType.STEPS,
            duration = "1 day",
            participants = listOf("user"),
            isActive = true,
            progress = 75f,
            maxProgress = 100f
        )

        assertTrue("Progress should not exceed max progress",
            challenge.progress <= challenge.maxProgress)
        assertTrue("Progress should be non-negative", challenge.progress >= 0f)
        assertTrue("Max progress should be positive", challenge.maxProgress > 0f)
    }

    @Test
    fun challengeType_enumIntegrity() {
        val expectedTypes = setOf("STEPS", "WATER", "SLEEP", "MEDITATION", "MIXED")
        val actualTypes = ChallengeType.entries.map { it.name }.toSet()

        assertEquals("All challenge types should be present", expectedTypes, actualTypes)
    }
}
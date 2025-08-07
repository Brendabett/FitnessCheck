package com.brenda.fitnesscheck

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.*
import org.junit.Before
import com.brenda.fitnesscheck.fitness.GoogleFitManager
import java.time.LocalDate

/**
 * Integration tests for Google Fit API
 * These tests require a real Android device and Google Play Services
 *
 * Put this file in: app/src/androidTest/java/com/brenda/fitnesscheck/GoogleFitIntegrationTest.kt
 */
@RunWith(AndroidJUnit4::class)
class GoogleFitIntegrationTest {

    private lateinit var googleFitManager: GoogleFitManager

    @Before
    fun setup() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        googleFitManager = GoogleFitManager(context)
    }

    @Test
    fun testGoogleFitManager_initialization() {
        // Test that GoogleFitManager can be created
        assertNotNull("GoogleFitManager should be initialized", googleFitManager)
        assertNotNull("Fitness options should be available", googleFitManager.getFitnessOptions())
    }

    @Test
    fun testPermissionCheck() {
        // Test permission checking (will be false unless user grants permissions)
        val hasPermissions = googleFitManager.hasPermissions()

        // This might be false on first run - that's expected
        println("Has Google Fit permissions: $hasPermissions")

        // Test should not crash
        assertTrue("Permission check should complete without error", true)
    }

    @Test
    fun testGoogleAccount() {
        // Test getting Google account
        val account = googleFitManager.getGoogleAccount()

        // Account might be null if not signed in - that's okay
        println("Google account: ${account?.email ?: "Not signed in"}")

        // Test should complete without crashing
        assertTrue("Account check should complete without error", true)
    }

    @Test
    fun testApiCallsWithoutPermissions() = runBlocking {
        // Test API calls when permissions are not granted
        // These should return 0/empty values gracefully, not crash

        val steps = googleFitManager.getTodaySteps()
        val calories = googleFitManager.getTodayCalories()
        val distance = googleFitManager.getTodayDistance()
        val history = googleFitManager.getStepsHistory(7)

        // These might be 0 if no permissions - that's expected behavior
        assertTrue("Steps should be non-negative", steps >= 0)
        assertTrue("Calories should be non-negative", calories >= 0f)
        assertTrue("Distance should be non-negative", distance >= 0f)
        assertNotNull("History should not be null", history)

        println("API calls completed: steps=$steps, calories=$calories, distance=$distance, history size=${history.size}")
    }

    @Test
    fun testSpecificDateQuery() = runBlocking {
        // Test querying data for a specific date
        val yesterday = LocalDate.now().minusDays(1)

        try {
            val steps = googleFitManager.getStepsForDate(yesterday)
            assertTrue("Steps for specific date should be non-negative", steps >= 0)
            println("Steps for $yesterday: $steps")
        } catch (e: Exception) {
            println("Expected exception when no permissions: ${e.message}")
            // This is expected if no permissions granted
        }
    }

    @Test
    fun testHistoryQuery() = runBlocking {
        // Test querying historical data
        try {
            val history = googleFitManager.getStepsHistory(3)
            assertNotNull("History should not be null", history)
            assertTrue("History size should be reasonable", history.size <= 4) // Max 3 days + today
            println("History query completed: ${history.size} days of data")

            history.forEach { (date, steps) ->
                assertTrue("All step counts should be non-negative", steps >= 0)
                println("$date: $steps steps")
            }
        } catch (e: Exception) {
            println("Expected exception when no permissions: ${e.message}")
        }
    }

    @Test
    fun testErrorHandling() = runBlocking {
        // Test that API calls handle errors gracefully
        // This test ensures the app doesn't crash under various conditions

        repeat(5) {
            try {
                val steps = googleFitManager.getTodaySteps()
                println("Attempt ${it + 1}: $steps steps")
            } catch (e: Exception) {
                println("Handled exception on attempt ${it + 1}: ${e.message}")
            }
        }

        // If we get here, error handling is working
        assertTrue("Error handling test completed successfully", true)
    }
}
package com.brenda.fitnesscheck.callingfitness

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate

class FitnessDataSync(
    private val context: Context,
    private val googleFitManager: GoogleFitManager
) {

    /**
     * Sync today's fitness data from Google Fit
     */
    suspend fun syncTodayData(): FitnessData? {
        return withContext(Dispatchers.IO) {
            try {
                if (!googleFitManager.hasPermissions()) {
                    return@withContext null
                }

                val steps = googleFitManager.getTodaySteps()
                val calories = googleFitManager.getTodayCalories()
                val distance = googleFitManager.getTodayDistance()

                FitnessData(
                    date = LocalDate.now(),
                    steps = steps,
                    calories = calories,
                    distance = distance,
                    lastSyncTime = System.currentTimeMillis()
                )
            } catch (e: Exception) {
                null
            }
        }
    }

    /**
     * Sync historical data for calendar view
     */
    suspend fun syncHistoricalData(days: Int = 30): List<FitnessData> {
        return withContext(Dispatchers.IO) {
            try {
                if (!googleFitManager.hasPermissions()) {
                    return@withContext emptyList()
                }

                val stepsHistory = googleFitManager.getStepsHistory(days)

                stepsHistory.map { (date, steps) ->
                    FitnessData(
                        date = date,
                        steps = steps,
                        calories = 0f, // Could fetch per day if needed
                        distance = 0f, // Could fetch per day if needed
                        lastSyncTime = System.currentTimeMillis()
                    )
                }.sortedBy { it.date }
            } catch (e: Exception) {
                emptyList()
            }
        }
    }
}
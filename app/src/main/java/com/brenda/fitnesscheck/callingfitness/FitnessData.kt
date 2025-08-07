package com.brenda.fitnesscheck.callingfitness

import java.time.LocalDate

data class FitnessData(
    val date: LocalDate,
    val steps: Int,
    val calories: Float,
    val distance: Float, // in meters
    val lastSyncTime: Long
) {
    val distanceInKm: Float
        get() = distance / 1000f

    val distanceInMiles: Float
        get() = distance * 0.000621371f
}
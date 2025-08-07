package com.brenda.fitnesscheck.callingfitness

import android.content.Context
import android.util.Log
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.fitness.Fitness
import com.google.android.gms.fitness.FitnessOptions
import com.google.android.gms.fitness.data.Bucket
import com.google.android.gms.fitness.data.DataPoint
import com.google.android.gms.fitness.data.DataSet
import com.google.android.gms.fitness.data.DataType
import com.google.android.gms.fitness.data.Field
import com.google.android.gms.fitness.request.DataReadRequest
import java.time.LocalDate
import java.time.ZoneId
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class GoogleFitManager(private val context: Context) {

    companion object {
        private const val TAG = "GoogleFitManager"
    }

    private val fitnessOptions = FitnessOptions.builder()
        .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
        .addDataType(DataType.AGGREGATE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
        .addDataType(DataType.TYPE_DISTANCE_DELTA, FitnessOptions.ACCESS_READ)
        .addDataType(DataType.TYPE_CALORIES_EXPENDED, FitnessOptions.ACCESS_READ)
        .addDataType(DataType.TYPE_ACTIVITY_SEGMENT, FitnessOptions.ACCESS_READ)
        .addDataType(DataType.TYPE_SLEEP_SEGMENT, FitnessOptions.ACCESS_READ)
        .build()

    /**
     * Check if user has granted Google Fit permissions
     */
    fun hasPermissions(): Boolean {
        return GoogleSignIn.hasPermissions(getGoogleAccount(), fitnessOptions)
    }

    /**
     * Get the Google account for fitness data
     */
    fun getGoogleAccount() = GoogleSignIn.getAccountForExtension(context, fitnessOptions)

    /**
     * Get fitness options for requesting permissions
     */
    fun getFitnessOptions() = fitnessOptions

    /**
     * Get today's step count from Google Fit
     */
    suspend fun getTodaySteps(): Int {
        return suspendCoroutine { continuation ->
            try {
                if (!hasPermissions()) {
                    Log.w(TAG, "No Google Fit permissions")
                    continuation.resume(0)
                    return@suspendCoroutine
                }

                val today = LocalDate.now()
                val startOfDay = today.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                val endOfDay = today.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

                val readRequest = DataReadRequest.Builder()
                    .aggregate(DataType.TYPE_STEP_COUNT_DELTA)
                    .bucketByTime(1, TimeUnit.DAYS)
                    .setTimeRange(startOfDay, endOfDay, TimeUnit.MILLISECONDS)
                    .build()

                Fitness.getHistoryClient(context, getGoogleAccount())
                    .readData(readRequest)
                    .addOnSuccessListener { response ->
                        var totalSteps = 0
                        for (bucket: Bucket in response.buckets) {
                            for (dataSet: DataSet in bucket.dataSets) {
                                for (dataPoint: DataPoint in dataSet.dataPoints) {
                                    totalSteps += dataPoint.getValue(Field.FIELD_STEPS).asInt()
                                }
                            }
                        }
                        Log.d(TAG, "Today's steps: $totalSteps")
                        continuation.resume(totalSteps)
                    }
                    .addOnFailureListener { exception ->
                        Log.e(TAG, "Error getting today's steps", exception)
                        continuation.resume(0)
                    }
            } catch (e: Exception) {
                Log.e(TAG, "Error in getTodaySteps", e)
                continuation.resume(0)
            }
        }
    }

    /**
     * Get step count for a specific date
     */
    suspend fun getStepsForDate(date: LocalDate): Int {
        return suspendCoroutine { continuation ->
            try {
                if (!hasPermissions()) {
                    Log.w(TAG, "No Google Fit permissions")
                    continuation.resume(0)
                    return@suspendCoroutine
                }

                val startOfDay = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                val endOfDay = date.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

                val readRequest = DataReadRequest.Builder()
                    .aggregate(DataType.TYPE_STEP_COUNT_DELTA)
                    .bucketByTime(1, TimeUnit.DAYS)
                    .setTimeRange(startOfDay, endOfDay, TimeUnit.MILLISECONDS)
                    .build()

                Fitness.getHistoryClient(context, getGoogleAccount())
                    .readData(readRequest)
                    .addOnSuccessListener { response ->
                        var totalSteps = 0
                        for (bucket: Bucket in response.buckets) {
                            for (dataSet: DataSet in bucket.dataSets) {
                                for (dataPoint: DataPoint in dataSet.dataPoints) {
                                    totalSteps += dataPoint.getValue(Field.FIELD_STEPS).asInt()
                                }
                            }
                        }
                        continuation.resume(totalSteps)
                    }
                    .addOnFailureListener { exception ->
                        Log.e(TAG, "Error getting steps for date $date", exception)
                        continuation.resume(0)
                    }
            } catch (e: Exception) {
                Log.e(TAG, "Error in getStepsForDate", e)
                continuation.resume(0)
            }
        }
    }

    /**
     * Get steps for the last N days
     */
    suspend fun getStepsHistory(days: Int): Map<LocalDate, Int> {
        return suspendCoroutine { continuation ->
            try {
                if (!hasPermissions()) {
                    Log.w(TAG, "No Google Fit permissions")
                    continuation.resume(emptyMap())
                    return@suspendCoroutine
                }

                val endDate = LocalDate.now()
                val startDate = endDate.minusDays(days.toLong())

                val startTime = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                val endTime = endDate.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

                val readRequest = DataReadRequest.Builder()
                    .aggregate(DataType.TYPE_STEP_COUNT_DELTA)
                    .bucketByTime(1, TimeUnit.DAYS)
                    .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                    .build()

                Fitness.getHistoryClient(context, getGoogleAccount())
                    .readData(readRequest)
                    .addOnSuccessListener { response ->
                        val stepsMap = mutableMapOf<LocalDate, Int>()

                        for (bucket: Bucket in response.buckets) {
                            val bucketDate = LocalDate.ofEpochDay(
                                TimeUnit.MILLISECONDS.toDays(bucket.getStartTime(TimeUnit.MILLISECONDS))
                            )

                            var daySteps = 0
                            for (dataSet: DataSet in bucket.dataSets) {
                                for (dataPoint: DataPoint in dataSet.dataPoints) {
                                    daySteps += dataPoint.getValue(Field.FIELD_STEPS).asInt()
                                }
                            }
                            stepsMap[bucketDate] = daySteps
                        }

                        Log.d(TAG, "Retrieved ${stepsMap.size} days of step data")
                        continuation.resume(stepsMap)
                    }
                    .addOnFailureListener { exception ->
                        Log.e(TAG, "Error getting steps history", exception)
                        continuation.resume(emptyMap())
                    }
            } catch (e: Exception) {
                Log.e(TAG, "Error in getStepsHistory", e)
                continuation.resume(emptyMap())
            }
        }
    }

    /**
     * Get calories burned today
     */
    suspend fun getTodayCalories(): Float {
        return suspendCoroutine { continuation ->
            try {
                if (!hasPermissions()) {
                    continuation.resume(0f)
                    return@suspendCoroutine
                }

                val today = LocalDate.now()
                val startOfDay = today.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                val endOfDay = today.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

                val readRequest = DataReadRequest.Builder()
                    .aggregate(DataType.TYPE_CALORIES_EXPENDED)
                    .bucketByTime(1, TimeUnit.DAYS)
                    .setTimeRange(startOfDay, endOfDay, TimeUnit.MILLISECONDS)
                    .build()

                Fitness.getHistoryClient(context, getGoogleAccount())
                    .readData(readRequest)
                    .addOnSuccessListener { response ->
                        var totalCalories = 0f
                        for (bucket: Bucket in response.buckets) {
                            for (dataSet: DataSet in bucket.dataSets) {
                                for (dataPoint: DataPoint in dataSet.dataPoints) {
                                    totalCalories += dataPoint.getValue(Field.FIELD_CALORIES).asFloat()
                                }
                            }
                        }
                        continuation.resume(totalCalories)
                    }
                    .addOnFailureListener { exception ->
                        Log.e(TAG, "Error getting calories", exception)
                        continuation.resume(0f)
                    }
            } catch (e: Exception) {
                Log.e(TAG, "Error in getTodayCalories", e)
                continuation.resume(0f)
            }
        }
    }

    /**
     * Get distance walked today (in meters)
     */
    suspend fun getTodayDistance(): Float {
        return suspendCoroutine { continuation ->
            try {
                if (!hasPermissions()) {
                    continuation.resume(0f)
                    return@suspendCoroutine
                }

                val today = LocalDate.now()
                val startOfDay = today.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                val endOfDay = today.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

                val readRequest = DataReadRequest.Builder()
                    .aggregate(DataType.TYPE_DISTANCE_DELTA)
                    .bucketByTime(1, TimeUnit.DAYS)
                    .setTimeRange(startOfDay, endOfDay, TimeUnit.MILLISECONDS)
                    .build()

                Fitness.getHistoryClient(context, getGoogleAccount())
                    .readData(readRequest)
                    .addOnSuccessListener { response ->
                        var totalDistance = 0f
                        for (bucket: Bucket in response.buckets) {
                            for (dataSet: DataSet in bucket.dataSets) {
                                for (dataPoint: DataPoint in dataSet.dataPoints) {
                                    totalDistance += dataPoint.getValue(Field.FIELD_DISTANCE).asFloat()
                                }
                            }
                        }
                        continuation.resume(totalDistance)
                    }
                    .addOnFailureListener { exception ->
                        Log.e(TAG, "Error getting distance", exception)
                        continuation.resume(0f)
                    }
            } catch (e: Exception) {
                Log.e(TAG, "Error in getTodayDistance", e)
                continuation.resume(0f)
            }
        }
    }
}
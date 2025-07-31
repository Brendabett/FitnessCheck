package com.brenda.fitnesscheck.database

import androidx.room.*
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.format.DateTimeFormatter

// Entity classes (Database Tables)
@Entity(tableName = "user_profiles")
data class UserProfileEntity(
    @PrimaryKey val id: Int = 1, // Single user app, so always ID 1
    val name: String,
    val stepGoal: Int,
    val waterGoal: Float,
    val sleepGoal: Float,
    val profilePictureIndex: Int
)

@Entity(
    tableName = "daily_tracking",
    indices = [Index(value = ["date"], unique = true)]
)
data class DailyTrackingEntity(
    @PrimaryKey val date: String, // Format: YYYY-MM-DD
    val steps: Int = 0,
    val waterIntake: Float = 0f,
    val sleepHours: Float = 0f,
    val mood: Float = 5f,
    val moodLogged: Boolean = false,
    val notes: String = ""
)

@Entity(tableName = "achievements")
data class AchievementEntity(
    @PrimaryKey val id: String, // e.g., "step_master", "hydration_hero"
    val title: String,
    val description: String,
    val isCompleted: Boolean = false,
    val completedDate: String? = null, // YYYY-MM-DD format
    val category: String, // "steps", "water", "sleep", "meditation", "consistency"
    val targetValue: Int = 0 // For achievements with numeric targets
)

@Entity(tableName = "meditation_sessions")
data class MeditationSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String, // YYYY-MM-DD
    val duration: Int, // in minutes
    val completed: Boolean = false,
    val sessionType: String = "general" // "general", "breathing", "focus", etc.
)

// Type converters for Room
class Converters {
    @TypeConverter
    fun fromLocalDate(date: LocalDate?): String? {
        return date?.format(DateTimeFormatter.ISO_LOCAL_DATE)
    }

    @TypeConverter
    fun toLocalDate(dateString: String?): LocalDate? {
        return dateString?.let { LocalDate.parse(it, DateTimeFormatter.ISO_LOCAL_DATE) }
    }
}

// DAOs (Data Access Objects) - Database Operations
@Dao
interface UserProfileDao {
    @Query("SELECT * FROM user_profiles WHERE id = 1")
    fun getUserProfile(): Flow<UserProfileEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserProfile(profile: UserProfileEntity)

    @Update
    suspend fun updateUserProfile(profile: UserProfileEntity)
}

@Dao
interface DailyTrackingDao {
    @Query("SELECT * FROM daily_tracking WHERE date = :date")
    suspend fun getDailyTracking(date: String): DailyTrackingEntity?

    @Query("SELECT * FROM daily_tracking WHERE date = :date")
    fun getDailyTrackingFlow(date: String): Flow<DailyTrackingEntity?>

    @Query("SELECT * FROM daily_tracking ORDER BY date DESC LIMIT :limit")
    fun getRecentDailyTracking(limit: Int = 30): Flow<List<DailyTrackingEntity>>

    @Query("SELECT * FROM daily_tracking WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getDailyTrackingInRange(startDate: String, endDate: String): Flow<List<DailyTrackingEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDailyTracking(tracking: DailyTrackingEntity)

    @Update
    suspend fun updateDailyTracking(tracking: DailyTrackingEntity)

    // Helper queries for statistics
    @Query("SELECT AVG(steps) FROM daily_tracking WHERE date BETWEEN :startDate AND :endDate")
    suspend fun getAverageSteps(startDate: String, endDate: String): Float?

    @Query("SELECT COUNT(*) FROM daily_tracking WHERE steps >= :stepGoal AND date BETWEEN :startDate AND :endDate")
    suspend fun getStepGoalAchievedCount(stepGoal: Int, startDate: String, endDate: String): Int
}

@Dao
interface AchievementDao {
    @Query("SELECT * FROM achievements ORDER BY isCompleted ASC, category ASC")
    fun getAllAchievements(): Flow<List<AchievementEntity>>

    @Query("SELECT * FROM achievements WHERE isCompleted = 1 ORDER BY completedDate DESC")
    fun getCompletedAchievements(): Flow<List<AchievementEntity>>

    @Query("SELECT * FROM achievements WHERE id = :id")
    suspend fun getAchievement(id: String): AchievementEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAchievement(achievement: AchievementEntity)

    @Update
    suspend fun updateAchievement(achievement: AchievementEntity)

    @Query("UPDATE achievements SET isCompleted = 1, completedDate = :date WHERE id = :id")
    suspend fun completeAchievement(id: String, date: String)
}

@Dao
interface MeditationDao {
    @Query("SELECT * FROM meditation_sessions ORDER BY date DESC, id DESC")
    fun getAllMeditationSessions(): Flow<List<MeditationSessionEntity>>

    @Query("SELECT * FROM meditation_sessions WHERE date = :date")
    fun getMeditationSessionsForDate(date: String): Flow<List<MeditationSessionEntity>>

    @Query("SELECT COUNT(*) FROM meditation_sessions WHERE completed = 1")
    suspend fun getCompletedSessionsCount(): Int

    @Query("SELECT SUM(duration) FROM meditation_sessions WHERE completed = 1 AND date BETWEEN :startDate AND :endDate")
    suspend fun getTotalMeditationMinutes(startDate: String, endDate: String): Int?

    @Insert
    suspend fun insertMeditationSession(session: MeditationSessionEntity): Long

    @Update
    suspend fun updateMeditationSession(session: MeditationSessionEntity)

    @Query("UPDATE meditation_sessions SET completed = 1 WHERE id = :id")
    suspend fun completeMeditationSession(id: Long)
}

// Database class
@Database(
    entities = [
        UserProfileEntity::class,
        DailyTrackingEntity::class,
        AchievementEntity::class,
        MeditationSessionEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class FitnessDatabase : RoomDatabase() {
    abstract fun userProfileDao(): UserProfileDao
    abstract fun dailyTrackingDao(): DailyTrackingDao
    abstract fun achievementDao(): AchievementDao
    abstract fun meditationDao(): MeditationDao

    companion object {
        @Volatile
        private var INSTANCE: FitnessDatabase? = null

        fun getDatabase(context: android.content.Context): FitnessDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FitnessDatabase::class.java,
                    "fitness_database"
                )
                    .addCallback(DatabaseCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

// Database callback to pre-populate with initial data
class DatabaseCallback : RoomDatabase.Callback() {
    override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)
        // Pre-populate achievements
        db.execSQL("""
            INSERT INTO achievements (id, title, description, category, targetValue) VALUES
            ('step_master', 'Step Master', 'Walk 10,000 steps in a day', 'steps', 10000),
            ('hydration_hero', 'Hydration Hero', 'Drink 2L of water daily for 7 days', 'water', 7),
            ('sleep_champion', 'Sleep Champion', 'Get 8 hours of sleep for 5 consecutive nights', 'sleep', 5),
            ('meditation_master', 'Meditation Master', 'Complete 30 meditation sessions', 'meditation', 30),
            ('consistency_king', 'Consistency King', 'Meet all daily goals for 7 days straight', 'consistency', 7),
            ('perfect_week', 'Perfect Week', 'Complete all goals for an entire week', 'consistency', 7)
        """)
    }
}

// Repository pattern for clean architecture
class FitnessRepository(
    private val userProfileDao: UserProfileDao,
    private val dailyTrackingDao: DailyTrackingDao,
    private val achievementDao: AchievementDao,
    private val meditationDao: MeditationDao
) {
    // User Profile operations
    fun getUserProfile() = userProfileDao.getUserProfile()

    suspend fun updateUserProfile(profile: UserProfileEntity) {
        userProfileDao.insertUserProfile(profile)
    }

    // Daily tracking operations
    suspend fun getTodayTracking(): DailyTrackingEntity? {
        val today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
        return dailyTrackingDao.getDailyTracking(today)
    }

    fun getTodayTrackingFlow(): Flow<DailyTrackingEntity?> {
        val today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
        return dailyTrackingDao.getDailyTrackingFlow(today)
    }

    suspend fun updateDailyTracking(
        steps: Int? = null,
        waterIntake: Float? = null,
        sleepHours: Float? = null,
        mood: Float? = null,
        moodLogged: Boolean? = null,
        notes: String? = null
    ) {
        val today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
        val existing = dailyTrackingDao.getDailyTracking(today) ?: DailyTrackingEntity(date = today)

        val updated = existing.copy(
            steps = steps ?: existing.steps,
            waterIntake = waterIntake ?: existing.waterIntake,
            sleepHours = sleepHours ?: existing.sleepHours,
            mood = mood ?: existing.mood,
            moodLogged = moodLogged ?: existing.moodLogged,
            notes = notes ?: existing.notes
        )

        dailyTrackingDao.insertDailyTracking(updated)
        checkAndUpdateAchievements(updated)
    }

    // Achievement operations
    fun getAllAchievements() = achievementDao.getAllAchievements()

    private suspend fun checkAndUpdateAchievements(dailyTracking: DailyTrackingEntity) {
        // Check step master achievement
        if (dailyTracking.steps >= 10000) {
            val achievement = achievementDao.getAchievement("step_master")
            if (achievement != null && !achievement.isCompleted) {
                achievementDao.completeAchievement("step_master", dailyTracking.date)
            }
        }
    }

    // Calendar data for the last 30 days
    fun getCalendarData(): Flow<List<DailyTrackingEntity>> {
        return dailyTrackingDao.getRecentDailyTracking(30)
    }

    // Meditation operations
    suspend fun startMeditationSession(duration: Int): Long {
        val today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
        val session = MeditationSessionEntity(
            date = today,
            duration = duration,
            completed = false
        )
        return meditationDao.insertMeditationSession(session)
    }

    suspend fun completeMeditationSession(id: Long) {
        meditationDao.completeMeditationSession(id)

        // Check meditation master achievement
        val completedCount = meditationDao.getCompletedSessionsCount()
        if (completedCount >= 30) {
            val achievement = achievementDao.getAchievement("meditation_master")
            if (achievement != null && !achievement.isCompleted) {
                val today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
                achievementDao.completeAchievement("meditation_master", today)
            }
        }
    }
}
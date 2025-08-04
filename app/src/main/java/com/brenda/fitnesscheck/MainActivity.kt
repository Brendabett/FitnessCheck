package com.brenda.fitnesscheck

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.brenda.fitnesscheck.database.ChallengeEntity
import com.brenda.fitnesscheck.database.FitnessDatabase
import com.brenda.fitnesscheck.database.UserProfileEntity
import com.brenda.fitnesscheck.repository.ChallengeRepository
import com.brenda.fitnesscheck.repository.UserProfileRepository
import com.brenda.fitnesscheck.viewmodel.ChallengeViewModel
import com.brenda.fitnesscheck.viewmodel.ChallengeViewModelFactory
import com.brenda.fitnesscheck.viewmodel.UserProfileViewModel
import com.brenda.fitnesscheck.viewmodel.UserProfileViewModelFactory
import java.time.LocalDate
import java.util.UUID
import kotlin.math.roundToInt
import kotlin.random.Random

// Data classes for managing user data
data class UserProfile(
    var name: String = "Brenda",
    var stepGoal: Int = 10000,
    var waterGoal: Float = 2.0f,
    var sleepGoal: Float = 8.0f,
    var profilePictureIndex: Int = 0
)

// Extension functions for UserProfile conversion
fun UserProfile.toEntity(): UserProfileEntity {
    return UserProfileEntity(
        id = 1,
        name = this.name,
        stepGoal = this.stepGoal,
        waterGoal = this.waterGoal,
        sleepGoal = this.sleepGoal,
        profilePictureIndex = this.profilePictureIndex
    )
}

fun UserProfileEntity.toUserProfile(): UserProfile {
    return UserProfile(
        name = this.name,
        stepGoal = this.stepGoal,
        waterGoal = this.waterGoal,
        sleepGoal = this.sleepGoal,
        profilePictureIndex = this.profilePictureIndex
    )
}

data class DailyGoals(
    val date: LocalDate,
    val stepsAchieved: Boolean = false,
    val waterAchieved: Boolean = false,
    val sleepAchieved: Boolean = false,
    val moodLogged: Boolean = false
)

data class Challenge(
    val id: String,
    val title: String,
    val description: String,
    val type: ChallengeType,
    val duration: String,
    val participants: List<String>,
    val isActive: Boolean,
    val prize: String = "",
    val progress: Float = 0f,
    val maxProgress: Float = 100f
)

enum class ChallengeType {
    STEPS, WATER, SLEEP, MEDITATION, MIXED
}

// Extension functions to convert between Challenge and ChallengeEntity
fun Challenge.toChallengeEntity(): ChallengeEntity {
    return ChallengeEntity(
        id = this.id,
        title = this.title,
        description = this.description,
        type = this.type.name,
        duration = this.duration,
        participantIds = this.participants.joinToString(","),
        isActive = this.isActive,
        prize = this.prize,
        progress = this.progress,
        maxProgress = this.maxProgress
    )
}

fun ChallengeEntity.toChallenge(): Challenge {
    return Challenge(
        id = this.id,
        title = this.title,
        description = this.description,
        type = try { ChallengeType.valueOf(this.type) } catch (_: Exception) { ChallengeType.MIXED },
        duration = this.duration,
        participants = if (this.participantIds.isEmpty()) emptyList() else this.participantIds.split(","),
        isActive = this.isActive,
        prize = this.prize,
        progress = this.progress,
        maxProgress = this.maxProgress
    )
}

class MainActivity : ComponentActivity() {
    // Initialize database and repositories
    private val database by lazy { FitnessDatabase.getDatabase(this) }
    private val challengeRepository by lazy { ChallengeRepository(database.challengeDao()) }
    private val userProfileRepository by lazy { UserProfileRepository(database.userProfileDao()) }

    // Create ViewModelFactories
    private val challengeViewModelFactory by lazy { ChallengeViewModelFactory(challengeRepository) }
    private val userProfileViewModelFactory by lazy { UserProfileViewModelFactory(userProfileRepository) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            FitnessCheckTheme {
                AppNavigationWithDatabase(
                    challengeViewModelFactory = challengeViewModelFactory,
                    userProfileViewModelFactory = userProfileViewModelFactory
                )
            }
        }
    }
}

@Composable
fun AppNavigationWithDatabase(
    challengeViewModelFactory: ChallengeViewModelFactory,
    userProfileViewModelFactory: UserProfileViewModelFactory
) {
    // Create ViewModels using the factories
    val challengeViewModel: ChallengeViewModel = viewModel(factory = challengeViewModelFactory)
    val userProfileViewModel: UserProfileViewModel = viewModel(factory = userProfileViewModelFactory)

    AppNavigation(
        challengeViewModel = challengeViewModel,
        userProfileViewModel = userProfileViewModel
    )
}

@Composable
fun FitnessCheckTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Color(0xFF1976D2),
            secondary = Color(0xFF4CAF50),
            tertiary = Color(0xFF9C27B0)
        ),
        content = content
    )
}

@Composable
fun AppNavigation(
    challengeViewModel: ChallengeViewModel,
    userProfileViewModel: UserProfileViewModel
) {
    val navController = rememberNavController()
    val calendarData = remember { generateSampleCalendarData() }

    // Observe user profile from database
    val userProfileEntity by userProfileViewModel.userProfile.collectAsState()
    val userProfile = userProfileEntity?.toUserProfile() ?: UserProfile()

    NavHost(navController, startDestination = "main_app") {
        composable("main_app") {
            MainApp(
                navController = navController,
                challengeViewModel = challengeViewModel,
                userProfileViewModel = userProfileViewModel,
                userProfile = userProfile
            )
        }
        composable("settings") { SettingsScreen(navController) }
        composable("achievements") { AchievementsScreen(navController) }
        composable("detailed_tracking") { DetailedTrackingScreen(navController) }
        composable("profile_settings") {
            ProfileSettingsScreen(
                navController = navController,
                userProfile = userProfile,
                userProfileViewModel = userProfileViewModel,
                modifier = Modifier
            )
        }
        composable("calendar") { CalendarScreen(navController, calendarData) }
    }
}

@Composable
fun MainApp(
    navController: NavController,
    challengeViewModel: ChallengeViewModel,
    userProfileViewModel: UserProfileViewModel,
    userProfile: UserProfile
) {
    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = { MainTopBar(navController) },
        bottomBar = {
            BottomNavBar(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it }
            )
        }
    ) { innerPadding ->
        when (selectedTab) {
            0 -> HomeScreen(
                modifier = Modifier.padding(innerPadding),
                challengeViewModel = challengeViewModel,
                userProfile = userProfile,
                onNavigateToTab = { tabIndex ->
                    selectedTab = tabIndex // Navigate to the specified tab
                }
            )
            1 -> FitnessScreen(
                modifier = Modifier.padding(innerPadding),
                userProfile = userProfile,
                userProfileViewModel = userProfileViewModel
            )
            2 -> ChallengesScreen(
                modifier = Modifier.padding(innerPadding),
                challengeViewModel = challengeViewModel
            )
            3 -> ProfileScreen(
                modifier = Modifier.padding(innerPadding),
                navController = navController,
                userProfile = userProfile,
                userProfileViewModel = userProfileViewModel
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainTopBar(navController: NavController) {
    TopAppBar(
        title = { Text("Fitness Check", fontWeight = FontWeight.Bold) },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color(0xFF1976D2),
            titleContentColor = Color.White,
            actionIconContentColor = Color.White
        ),
        actions = {
            IconButton(onClick = { navController.navigate("calendar") }) {
                Icon(Icons.Default.DateRange, contentDescription = "Calendar")
            }
            IconButton(onClick = { navController.navigate("achievements") }) {
                Icon(Icons.Default.Star, contentDescription = "Achievements")
            }
            IconButton(onClick = { navController.navigate("settings") }) {
                Icon(Icons.Default.Settings, contentDescription = "Settings")
            }
        }
    )
}

@Composable
fun BottomNavBar(selectedTab: Int, onTabSelected: (Int) -> Unit) {
    NavigationBar(
        containerColor = Color(0xFF1976D2)
    ) {
        NavigationBarItem(
            selected = selectedTab == 0,
            onClick = { onTabSelected(0) },
            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
            label = { Text("Home") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color.White,
                selectedTextColor = Color.White,
                unselectedIconColor = Color.White.copy(alpha = 0.6f),
                unselectedTextColor = Color.White.copy(alpha = 0.6f),
                indicatorColor = Color(0xFF4CAF50)
            )
        )
        NavigationBarItem(
            selected = selectedTab == 1,
            onClick = { onTabSelected(1) },
            icon = { Icon(Icons.Default.FitnessCenter, contentDescription = "Fitness") },
            label = { Text("Fitness") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color.White,
                selectedTextColor = Color.White,
                unselectedIconColor = Color.White.copy(alpha = 0.6f),
                unselectedTextColor = Color.White.copy(alpha = 0.6f),
                indicatorColor = Color(0xFF4CAF50)
            )
        )
        NavigationBarItem(
            selected = selectedTab == 2,
            onClick = { onTabSelected(2) },
            icon = { Icon(Icons.Default.Star, contentDescription = "Challenges") },
            label = { Text("Challenges") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color.White,
                selectedTextColor = Color.White,
                unselectedIconColor = Color.White.copy(alpha = 0.6f),
                unselectedTextColor = Color.White.copy(alpha = 0.6f),
                indicatorColor = Color(0xFF4CAF50)
            )
        )
        NavigationBarItem(
            selected = selectedTab == 3,
            onClick = { onTabSelected(3) },
            icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
            label = { Text("Profile") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color.White,
                selectedTextColor = Color.White,
                unselectedIconColor = Color.White.copy(alpha = 0.6f),
                unselectedTextColor = Color.White.copy(alpha = 0.6f),
                indicatorColor = Color(0xFF4CAF50)
            )
        )
    }
}

// Screen 1: Enhanced Home Screen
@SuppressLint("DefaultLocale")
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    challengeViewModel: ChallengeViewModel,
    userProfile: UserProfile,
    onNavigateToTab: (Int) -> Unit = {} // Add navigation callback
) {
    val challengeEntities by challengeViewModel.allChallenges.collectAsState(initial = emptyList())
    val context = LocalContext.current

    // TODO: Replace these with actual tracking data from your fitness tracking system
    // For now, I'll create sample progress that updates with profile changes
    var currentSteps by remember { mutableIntStateOf(7500) }
    var currentWaterIntake by remember { mutableFloatStateOf(1.8f) }
    var currentSleepHours by remember { mutableFloatStateOf(7.2f) }

    // Calculate progress percentages based on current profile goals
    val stepProgress = (currentSteps.toFloat() / userProfile.stepGoal.toFloat()).coerceAtMost(1.0f)
    val waterProgress = (currentWaterIntake / userProfile.waterGoal).coerceAtMost(1.0f)
    val sleepProgress = (currentSleepHours / userProfile.sleepGoal).coerceAtMost(1.0f)

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcome Header
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1976D2)),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Welcome back, ${userProfile.name}! 👋",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "Ready to crush your goals today?",
                    fontSize = 16.sp,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }
        }

        // Quick Stats Row - NOW CLICKABLE!
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ClickableQuickStatCard(
                title = "Challenges",
                value = challengeEntities.size.toString(),
                icon = Icons.Default.Star,
                color = Color(0xFF4CAF50),
                modifier = Modifier.weight(1f),
                onClick = {
                    onNavigateToTab(2) // Navigate to Challenges tab (index 2)
                    Toast.makeText(context, "Opening Challenges", Toast.LENGTH_SHORT).show()
                }
            )
            ClickableQuickStatCard(
                title = "Step Goal",
                value = "${userProfile.stepGoal}",
                icon = Icons.AutoMirrored.Filled.DirectionsWalk,
                color = Color(0xFF2196F3),
                modifier = Modifier.weight(1f),
                onClick = {
                    onNavigateToTab(1) // Navigate to Fitness tab (index 1)
                    Toast.makeText(context, "Opening Fitness Tracker", Toast.LENGTH_SHORT).show()
                }
            )
            ClickableQuickStatCard(
                title = "Water Goal",
                value = "${userProfile.waterGoal}L",
                icon = Icons.Default.LocalDrink,
                color = Color(0xFF00BCD4),
                modifier = Modifier.weight(1f),
                onClick = {
                    onNavigateToTab(1) // Navigate to Fitness tab (index 1)
                    Toast.makeText(context, "Opening Water Tracker", Toast.LENGTH_SHORT).show()
                }
            )
        }

        // Today's Progress - NOW DYNAMIC!
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF3E5F5))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Today's Progress",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "${((stepProgress + waterProgress + sleepProgress) / 3 * 100).toInt()}% overall",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF1976D2)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))

                // Steps Progress - Dynamic and Clickable
                ClickableDynamicProgressItem(
                    title = "Steps",
                    current = currentSteps.toString(),
                    goal = userProfile.stepGoal.toString(),
                    progress = stepProgress,
                    onQuickAdd = { currentSteps += 500 },
                    onClick = {
                        onNavigateToTab(1) // Navigate to Fitness tab
                        Toast.makeText(context, "Opening Step Tracker", Toast.LENGTH_SHORT).show()
                    },
                    icon = "🚶"
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Water Progress - Dynamic and Clickable
                ClickableDynamicProgressItem(
                    title = "Water",
                    current = String.format("%.1f", currentWaterIntake) + "L",
                    goal = "${userProfile.waterGoal}L",
                    progress = waterProgress,
                    onQuickAdd = { currentWaterIntake += 0.2f },
                    onClick = {
                        onNavigateToTab(1) // Navigate to Fitness tab
                        Toast.makeText(context, "Opening Water Tracker", Toast.LENGTH_SHORT).show()
                    },
                    icon = "💧"
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Sleep Progress - Dynamic and Clickable
                ClickableDynamicProgressItem(
                    title = "Sleep",
                    current = String.format("%.1f", currentSleepHours) + "h",
                    goal = "${userProfile.sleepGoal}h",
                    progress = sleepProgress,
                    onQuickAdd = { currentSleepHours += 0.5f },
                    onClick = {
                        onNavigateToTab(1) // Navigate to Fitness tab
                        Toast.makeText(context, "Opening Sleep Tracker", Toast.LENGTH_SHORT).show()
                    },
                    icon = "😴"
                )
            }
        }

        // Goal Achievement Celebration
        if (stepProgress >= 1.0f || waterProgress >= 1.0f || sleepProgress >= 1.0f) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E8))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("🎉", fontSize = 32.sp)
                    Text(
                        text = "Congratulations!",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4CAF50)
                    )
                    Text(
                        text = buildString {
                            val achievements = mutableListOf<String>()
                            if (stepProgress >= 1.0f) achievements.add("steps")
                            if (waterProgress >= 1.0f) achievements.add("water")
                            if (sleepProgress >= 1.0f) achievements.add("sleep")
                            append("You've achieved your ${achievements.joinToString(" & ")} goal${if (achievements.size > 1) "s" else ""}!")
                        },
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
            }
        }

        // Recent Challenges - NOW CLICKABLE!
        if (challengeEntities.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        onNavigateToTab(2) // Navigate to Challenges tab
                        Toast.makeText(context, "Opening All Challenges", Toast.LENGTH_SHORT).show()
                    },
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E8))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Active Challenges",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "View all",
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    challengeEntities.take(3).forEach { challenge ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = challenge.title,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = challenge.duration,
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                            }
                            Text(
                                text = "${challenge.progress.toInt()}%",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF4CAF50)
                            )
                        }
                    }

                    if (challengeEntities.size > 3) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Tap to view ${challengeEntities.size - 3} more challenges",
                            fontSize = 12.sp,
                            color = Color.Gray,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    }
                }
            }
        }

        // Enhanced Quick Actions
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Quick Actions",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            // Quick log workout - add some steps and water
                            currentSteps += 1000
                            currentWaterIntake += 0.3f
                            Toast.makeText(context, "Workout logged! +1000 steps, +300ml water", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Log Workout", fontSize = 12.sp)
                    }

                    Button(
                        onClick = {
                            Toast.makeText(context, "Track mood feature coming soon!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9C27B0))
                    ) {
                        Icon(Icons.Default.Mood, contentDescription = "Mood")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Track Mood", fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Reset Progress Button (for testing)
                OutlinedButton(
                    onClick = {
                        currentSteps = 0
                        currentWaterIntake = 0f
                        currentSleepHours = 0f
                        Toast.makeText(context, "Progress reset for testing", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = "Reset")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Reset Today's Progress")
                }
            }
        }
    }
}

// New Clickable QuickStatCard
@Composable
fun ClickableQuickStatCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = modifier.clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = color),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = title, tint = Color.White, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Text(title, fontSize = 12.sp, color = Color.White.copy(alpha = 0.8f))

            // Click indicator
            Spacer(modifier = Modifier.height(2.dp))
            Icon(
                Icons.Default.TouchApp,
                contentDescription = "Tap to open",
                tint = Color.White.copy(alpha = 0.7f),
                modifier = Modifier.size(12.dp)
            )
        }
    }
}

// New Clickable DynamicProgressItem
@Composable
fun ClickableDynamicProgressItem(
    title: String,
    current: String,
    goal: String,
    progress: Float,
    onQuickAdd: () -> Unit,
    onClick: () -> Unit,
    icon: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (progress >= 1.0f) Color(0xFFE8F5E8) else Color(0xFFF8F9FA)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(icon, fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(title, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        Icons.Default.TouchApp,
                        contentDescription = "Tap to open",
                        tint = Color.Gray,
                        modifier = Modifier.size(12.dp)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("$current / $goal", fontSize = 14.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.width(8.dp))

                    // Quick add button
                    IconButton(
                        onClick = {
                            onQuickAdd()
                            // Prevent propagation to card click
                        },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Quick add",
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Progress bar with achievement indicator
            Box(modifier = Modifier.fillMaxWidth()) {
                LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth(),
                color = if (progress >= 1.0f) Color(0xFF4CAF50) else Color(0xFF2196F3),
                trackColor = Color(0xFFE0E0E0),
                strokeCap = ProgressIndicatorDefaults.LinearStrokeCap,
                )

                // Achievement checkmark
                if (progress >= 1.0f) {
                    Row(
                        modifier = Modifier.align(Alignment.CenterEnd),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = "Achieved",
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // Progress percentage
            Text(
                text = "${(progress * 100).toInt()}% complete • Tap for details",
                fontSize = 12.sp,
                color = if (progress >= 1.0f) Color(0xFF4CAF50) else Color.Gray,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

// Screen 2: Fitness Screen
@SuppressLint("DefaultLocale")
@Composable
fun FitnessScreen(
    modifier: Modifier = Modifier,
    userProfile: UserProfile,
    userProfileViewModel: UserProfileViewModel
) {
    var steps by remember { mutableIntStateOf(7500) }
    var waterIntake by remember { mutableFloatStateOf(1.8f) }
    var sleepHours by remember { mutableFloatStateOf(7.2f) }
    var mood by remember { mutableFloatStateOf(7f) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Fitness Tracker",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1976D2)
        )

        // Goal Adjustment Card with Database Integration
        GoalAdjustmentCard(userProfile, userProfileViewModel)

        // Existing interactive cards...
        InteractiveCard(
            title = "Steps Today",
            value = steps.toString(),
            unit = "steps",
            goal = userProfile.stepGoal.toString(),
            isGoalAchieved = steps >= userProfile.stepGoal,
            onIncrement = { steps += 100 },
            onDecrement = { if (steps > 0) steps -= 100 },
            icon = Icons.AutoMirrored.Filled.DirectionsWalk
        )

        InteractiveCard(
            title = "Water Intake",
            value = String.format("%.1f", waterIntake),
            unit = "L",
            goal = userProfile.waterGoal.toString(),
            isGoalAchieved = waterIntake >= userProfile.waterGoal,
            onIncrement = { waterIntake += 0.1f },
            onDecrement = { if (waterIntake > 0) waterIntake -= 0.1f },
            icon = Icons.Default.LocalDrink
        )

        InteractiveCard(
            title = "Sleep Duration",
            value = String.format("%.1f", sleepHours),
            unit = "hours",
            goal = userProfile.sleepGoal.toString(),
            isGoalAchieved = sleepHours >= userProfile.sleepGoal,
            onIncrement = { sleepHours += 0.1f },
            onDecrement = { if (sleepHours > 0) sleepHours -= 0.1f },
            icon = Icons.Default.Bedtime
        )

        // Enhanced Mood Tracker
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Mood Today", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                    Icon(Icons.Default.Mood, contentDescription = "Mood", tint = Color(0xFFFF9800))
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text("${mood.toInt()}/10", fontSize = 24.sp, fontWeight = FontWeight.Bold)

                Slider(
                    value = mood,
                    onValueChange = { mood = it },
                    valueRange = 1f..10f,
                    steps = 8,
                    colors = SliderDefaults.colors(
                        thumbColor = Color(0xFFFF9800),
                        activeTrackColor = Color(0xFFFF9800)
                    )
                )

                Text(
                    text = when (mood.toInt()) {
                        in 1..3 -> "😔 Not great"
                        in 4..6 -> "😐 Okay"
                        in 7..8 -> "😊 Good"
                        else -> "😄 Excellent!"
                    },
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

// Screen 3: Challenges Screen
@Composable
fun ChallengesScreen(
    modifier: Modifier = Modifier,
    challengeViewModel: ChallengeViewModel
) {
    val challengeEntities by challengeViewModel.allChallenges.collectAsState(initial = emptyList())
    val context = LocalContext.current
    var showCreateDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "My Challenges",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1976D2)
            )

            Button(
                onClick = { showCreateDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
            ) {
                Icon(Icons.Default.Add, contentDescription = "Create")
                Spacer(modifier = Modifier.width(4.dp))
                Text("Create")
            }
        }

        if (challengeEntities.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(48.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("🎯", fontSize = 64.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No Challenges Yet!",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Create your first challenge to get started",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { showCreateDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Create")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Create Challenge")
                    }
                }
            }
        } else {
            challengeEntities.forEach { challenge ->
                EnhancedChallengeCard(
                    challenge = challenge.toChallenge(),
                    onUpdateProgress = { challengeId, progress ->
                        challengeViewModel.updateProgress(challengeId, progress)
                    },
                    onDeleteChallenge = { challengeId ->
                        challengeViewModel.deleteChallenge(challengeId)
                    }
                )
            }
        }
    }

    if (showCreateDialog) {
        CreateChallengeDialog(
            onDismiss = { showCreateDialog = false },
            onCreateChallenge = { challenge ->
                val challengeEntity = challenge.toChallengeEntity()
                challengeViewModel.addChallenge(challengeEntity)
                Toast.makeText(context, "Challenge '${challenge.title}' created!", Toast.LENGTH_SHORT).show()
                showCreateDialog = false
            }
        )
    }
}

// Screen 4: Enhanced Profile Screen
@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    userProfile: UserProfile,
    userProfileViewModel: UserProfileViewModel
) {
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Profile",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1976D2)
            )

            IconButton(onClick = { navController.navigate("profile_settings") }) {
                Icon(Icons.Default.Settings, contentDescription = "Settings", tint = Color(0xFF1976D2))
            }
        }

        // Enhanced Profile Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1976D2)),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Profile Picture
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(getProfilePictureColor(userProfile.profilePictureIndex))
                        .border(4.dp, Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = userProfile.name.first().toString(),
                        fontSize = 40.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = userProfile.name,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "Fitness Enthusiast 💪",
                    fontSize = 16.sp,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }

        // Goals Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF3E5F5))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Your Goals",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(12.dp))

                GoalItem("🚶", "Daily Steps", "${userProfile.stepGoal}")
                GoalItem("💧", "Water Intake", "${userProfile.waterGoal}L")
                GoalItem("😴", "Sleep Duration", "${userProfile.sleepGoal}h")
            }
        }

        // Statistics Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E8))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "This Week's Stats",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(12.dp))

                StatItem("Workouts Completed", "5/7")
                StatItem("Goals Achieved", "18/21")
                StatItem("Current Streak", "3 days 🔥")
                StatItem("Weekly Progress", "85%")
            }
        }

        // Action Buttons with Database Integration
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = { navController.navigate("profile_settings") },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9C27B0))
            ) {
                Icon(Icons.Default.Edit, contentDescription = "Edit")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Edit Profile & Goals")
            }

            Button(
                onClick = { navController.navigate("calendar") },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
            ) {
                Icon(Icons.Default.DateRange, contentDescription = "Calendar")
                Spacer(modifier = Modifier.width(8.dp))
                Text("View Achievement Calendar")
            }

            OutlinedButton(
                onClick = {
                    userProfileViewModel.resetProfile()
                    Toast.makeText(context, "Profile reset to default!", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Refresh, contentDescription = "Reset")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Reset Profile")
            }
        }
    }
}

@Composable
fun GoalAdjustmentCard(userProfile: UserProfile, userProfileViewModel: UserProfileViewModel) {
    val context = LocalContext.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Quick Goal Adjustments",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "Tap to quickly adjust your daily goals",
                fontSize = 14.sp,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Quick Step Goal Adjustment
                OutlinedButton(
                    onClick = {
                        userProfileViewModel.updateStepGoal(userProfile.stepGoal + 1000)
                        Toast.makeText(context, "Step goal increased to ${userProfile.stepGoal + 1000}!", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("🚶 +1K", fontSize = 12.sp)
                }

                // Quick Water Goal Adjustment
                OutlinedButton(
                    onClick = {
                        userProfileViewModel.updateWaterGoal(userProfile.waterGoal + 0.5f)
                        Toast.makeText(context, "Water goal increased to ${userProfile.waterGoal + 0.5f}L!", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("💧 +0.5L", fontSize = 12.sp)
                }

                // Quick Sleep Goal Adjustment
                OutlinedButton(
                    onClick = {
                        userProfileViewModel.updateSleepGoal(userProfile.sleepGoal + 0.5f)
                        Toast.makeText(context, "Sleep goal increased to ${userProfile.sleepGoal + 0.5f}h!", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("😴 +0.5h", fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun InteractiveCard(
    title: String,
    value: String,
    unit: String,
    goal: String,
    isGoalAchieved: Boolean,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    icon: ImageVector
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isGoalAchieved) Color(0xFFE8F5E8) else Color(0xFFE3F2FD)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        icon,
                        contentDescription = title,
                        tint = if (isGoalAchieved) Color(0xFF4CAF50) else Color(0xFF1976D2),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(title, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                        Text("$value $unit / $goal $unit", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }
                }
                if (isGoalAchieved) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = "Achieved",
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onDecrement,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF44336)),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Remove, contentDescription = "Decrease")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Decrease")
                }
                Button(
                    onClick = onIncrement,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Increase")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Increase")
                }
            }
        }
    }
}

@Composable
fun EnhancedChallengeCard(
    challenge: Challenge,
    onUpdateProgress: (String, Float) -> Unit = { _, _ -> },
    onDeleteChallenge: (String) -> Unit = { }
) {
    val context = LocalContext.current
    val progressPercentage = (challenge.progress / challenge.maxProgress * 100).roundToInt()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                Toast.makeText(context, "Viewing ${challenge.title} details", Toast.LENGTH_SHORT).show()
            },
        colors = CardDefaults.cardColors(
            containerColor = if (challenge.isActive) Color(0xFFFFF3E0) else Color(0xFFF5F5F5)
        ),
        border = if (challenge.isActive) BorderStroke(2.dp, Color(0xFF4CAF50)) else null,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = getChallengeTypeEmoji(challenge.type),
                            fontSize = 24.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = challenge.title,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = challenge.description,
                                fontSize = 14.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }

                Row {
                    IconButton(
                        onClick = {
                            val newProgress = (challenge.progress + 10f).coerceAtMost(challenge.maxProgress)
                            onUpdateProgress(challenge.id, newProgress)
                        }
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Update Progress", tint = Color(0xFF4CAF50))
                    }

                    IconButton(
                        onClick = { onDeleteChallenge(challenge.id) }
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFF44336))
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (challenge.isActive) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFFE0E0E0))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(challenge.progress / challenge.maxProgress)
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFF4CAF50))
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "$progressPercentage% complete",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF4CAF50)
                    )

                    Text(
                        text = "⏱️ ${challenge.duration}",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "👥 ${challenge.participants.size} participants",
                    fontSize = 12.sp,
                    color = Color.Gray
                )

                if (challenge.prize.isNotEmpty()) {
                    Text(
                        text = "🏆 ${challenge.prize}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF1976D2)
                    )
                }
            }
        }
    }
}

@Composable
fun GoalItem(emoji: String, title: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(emoji, fontSize = 20.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Text(title, fontSize = 14.sp)
        }
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1976D2))
    }
}

@Composable
fun StatItem(title: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(title, fontSize = 14.sp, color = Color.Gray)
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.Bold)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateChallengeDialog(
    onDismiss: () -> Unit,
    onCreateChallenge: (Challenge) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(ChallengeType.STEPS) }
    var duration by remember { mutableStateOf("") }
    var prize by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Create Challenge",
                    tint = Color(0xFF4CAF50)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Create New Challenge")
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Challenge Title") },
                    placeholder = { Text("e.g., Weekend Warriors") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    placeholder = { Text("e.g., Walk 20,000 steps this weekend") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )

                Text("Challenge Type:", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    ChallengeType.entries.forEach { type ->
                        Button(
                            onClick = { selectedType = type },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selectedType == type) Color(0xFF4CAF50) else Color(0xFFE0E0E0),
                                contentColor = if (selectedType == type) Color.White else Color.Black
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(getChallengeTypeEmoji(type), fontSize = 12.sp)
                        }
                    }
                }

                OutlinedTextField(
                    value = duration,
                    onValueChange = { duration = it },
                    label = { Text("Duration") },
                    placeholder = { Text("e.g., 7 days, 2 weeks") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = prize,
                    onValueChange = { prize = it },
                    label = { Text("Prize (Optional)") },
                    placeholder = { Text("e.g., 🏆 Winner's Badge") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotEmpty() && description.isNotEmpty() && duration.isNotEmpty()) {
                        val challenge = Challenge(
                            id = UUID.randomUUID().toString(),
                            title = title,
                            description = description,
                            type = selectedType,
                            duration = duration,
                            participants = listOf("user"),
                            isActive = true,
                            prize = prize,
                            progress = 0f,
                            maxProgress = 100f
                        )
                        onCreateChallenge(challenge)
                    }
                },
                enabled = title.isNotEmpty() && description.isNotEmpty() && duration.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
            ) {
                Icon(Icons.Default.Add, contentDescription = "Create")
                Spacer(modifier = Modifier.width(4.dp))
                Text("Create Challenge")
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Gray,
                    contentColor = Color.White
                )
            ) {
                Text("Cancel")
            }
        }
    )
}

// Additional screens and helper functions
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileSettingsScreen(
    navController: NavController,
    userProfile: UserProfile,
    userProfileViewModel: UserProfileViewModel,
    modifier: Modifier = Modifier
) {
    var tempName by remember { mutableStateOf(userProfile.name) }
    var tempStepGoal by remember { mutableStateOf(userProfile.stepGoal.toString()) }
    var tempWaterGoal by remember { mutableStateOf(userProfile.waterGoal.toString()) }
    var tempSleepGoal by remember { mutableStateOf(userProfile.sleepGoal.toString()) }
    var tempProfilePictureIndex by remember { mutableIntStateOf(userProfile.profilePictureIndex) }

    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile Settings") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            val stepGoal = tempStepGoal.toIntOrNull() ?: userProfile.stepGoal
                            val waterGoal = tempWaterGoal.toFloatOrNull() ?: userProfile.waterGoal
                            val sleepGoal = tempSleepGoal.toFloatOrNull() ?: userProfile.sleepGoal

                            // Update multiple fields at once using ViewModel
                            userProfileViewModel.updateMultipleFields(
                                name = tempName.ifBlank { userProfile.name },
                                stepGoal = stepGoal,
                                waterGoal = waterGoal,
                                sleepGoal = sleepGoal,
                                profilePictureIndex = tempProfilePictureIndex
                            )

                            Toast.makeText(context, "Profile updated successfully!", Toast.LENGTH_SHORT).show()
                            navController.popBackStack()
                        }
                    ) {
                        Icon(Icons.Default.Check, contentDescription = "Save")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E8))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Profile Picture", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        repeat(5) { index ->
                            Box(
                                modifier = Modifier
                                    .size(50.dp)
                                    .clip(CircleShape)
                                    .background(getProfilePictureColor(index))
                                    .border(
                                        width = if (tempProfilePictureIndex == index) 3.dp else 0.dp,
                                        color = Color(0xFF1976D2),
                                        shape = CircleShape
                                    )
                                    .clickable { tempProfilePictureIndex = index },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = tempName.firstOrNull()?.toString() ?: "?",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Personal Information", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = tempName,
                        onValueChange = { tempName = it },
                        label = { Text("Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Daily Goals", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = tempStepGoal,
                        onValueChange = { tempStepGoal = it },
                        label = { Text("Step Goal") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = tempWaterGoal,
                        onValueChange = { tempWaterGoal = it },
                        label = { Text("Water Goal (L)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = tempSleepGoal,
                        onValueChange = { tempSleepGoal = it },
                        label = { Text("Sleep Goal (hours)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

// Helper functions
fun generateSampleCalendarData(): List<DailyGoals> {
    val today = LocalDate.now()
    val data = mutableListOf<DailyGoals>()
    for (i in -30..0) {
        val date = today.plusDays(i.toLong())
        data.add(
            DailyGoals(
                date = date,
                stepsAchieved = Random.nextBoolean(),
                waterAchieved = Random.nextBoolean(),
                sleepAchieved = Random.nextBoolean(),
                moodLogged = Random.nextBoolean()
            )
        )
    }
    return data
}

fun getProfilePictureColor(index: Int): Color {
    val colors = listOf(
        Color(0xFF1976D2), Color(0xFF4CAF50), Color(0xFFFF9800),
        Color(0xFF9C27B0), Color(0xFFF44336)
    )
    return colors[index % colors.size]
}

fun getChallengeTypeEmoji(type: ChallengeType): String {
    return when (type) {
        ChallengeType.STEPS -> "🚶"
        ChallengeType.WATER -> "💧"
        ChallengeType.SLEEP -> "😴"
        ChallengeType.MEDITATION -> "🧘"
        ChallengeType.MIXED -> "🎯"
    }
}

// Additional screens for navigation
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController, modifier: Modifier = Modifier) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("App Settings", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("• Notifications: Enabled")
                    Text("• Dark Mode: Disabled")
                    Text("• Data Sync: Enabled")
                    Text("• Privacy: High")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AchievementsScreen(navController: NavController, modifier: Modifier = Modifier) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Achievements") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            AchievementCard("Step Master", "Walk 10,000 steps in a day", true)
            AchievementCard("Hydration Hero", "Drink 2L of water daily for 7 days", true)
            AchievementCard("Sleep Champion", "Get 8 hours of sleep for 5 consecutive nights", false)
            AchievementCard("Meditation Master", "Complete 30 meditation sessions", false)
            AchievementCard("Consistency King", "Meet all daily goals for 7 days straight", false)
        }
    }
}

@Composable
fun AchievementCard(title: String, description: String, isCompleted: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isCompleted) Color(0xFFE8F5E8) else Color(0xFFF5F5F5)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                Text(description, fontSize = 14.sp, color = Color.Gray)
            }

            Icon(
                if (isCompleted) Icons.Default.CheckCircle else Icons.Default.Lock,
                contentDescription = if (isCompleted) "Completed" else "Locked",
                tint = if (isCompleted) Color(0xFF4CAF50) else Color.Gray
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailedTrackingScreen(navController: NavController, modifier: Modifier = Modifier) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detailed Tracking") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Advanced Analytics", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("• Weekly trends")
                    Text("• Monthly comparisons")
                    Text("• Goal progression")
                    Text("• Health insights")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    navController: NavController,
    calendarData: List<DailyGoals>,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Achievement Calendar") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1976D2),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Legend Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Legend", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        LegendItem("🟢", "All Goals")
                        LegendItem("🟡", "Partial")
                        LegendItem("🔴", "No Goals")
                        LegendItem("⚪", "No Data")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Last 30 Days",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Calendar Grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(7),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(calendarData) { dayData ->
                    CalendarDayItem(dayData = dayData)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Statistics Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E8))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Monthly Statistics", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(8.dp))

                    val completedDays = calendarData.count {
                        it.stepsAchieved && it.waterAchieved && it.sleepAchieved
                    }
                    val totalDays = calendarData.size
                    val completionRate = if (totalDays > 0) (completedDays * 100) / totalDays else 0

                    StatItem("🎯 Perfect Days", "$completedDays/$totalDays")
                    StatItem("📊 Completion Rate", "$completionRate%")
                    StatItem("🚶 Step Goals Met", "${calendarData.count { it.stepsAchieved }}")
                    StatItem("💧 Water Goals Met", "${calendarData.count { it.waterAchieved }}")
                    StatItem("😴 Sleep Goals Met", "${calendarData.count { it.sleepAchieved }}")
                }
            }
        }
    }
}

@Composable
fun LegendItem(emoji: String, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(emoji, fontSize = 16.sp)
        Spacer(modifier = Modifier.width(4.dp))
        Text(label, fontSize = 12.sp)
    }
}

@Composable
fun CalendarDayItem(dayData: DailyGoals) {
    val goalsAchieved = listOf(
        dayData.stepsAchieved,
        dayData.waterAchieved,
        dayData.sleepAchieved
    ).count { it }

    val backgroundColor = when (goalsAchieved) {
        3 -> Color(0xFF4CAF50) // Green - all goals
        2 -> Color(0xFFFF9800) // Orange - 2 goals
        1 -> Color(0xFFFFC107) // Yellow - 1 goal
        else -> Color(0xFFF44336) // Red - no goals
    }

    val today = LocalDate.now()
    val isToday = dayData.date == today

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .border(
                width = if (isToday) 2.dp else 0.dp,
                color = Color.Black,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable {
                // Could add day detail functionality here
            },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = dayData.date.dayOfMonth.toString(),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            if (goalsAchieved > 0) {
                Text(
                    text = "$goalsAchieved/3",
                    fontSize = 8.sp,
                    color = Color.White
                )
            }
        }
    }
}

// Preview functions
@Preview(showBackground = true)
@Composable
fun PreviewMainApp() {
    FitnessCheckTheme {
        // Preview would need mock data
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Text(
                text = "Fitness Check App",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}
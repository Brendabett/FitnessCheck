package com.brenda.fitnesscheck.screen

import android.widget.Toast
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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.brenda.fitnesscheck.viewmodel.ChallengeViewModel
import com.brenda.fitnesscheck.viewmodel.UserProfileViewModel
import java.time.LocalDate
import java.util.UUID
import kotlin.math.roundToInt

// Data classes that should be in this file
data class UserProfile(
    var name: String = "Brenda",
    var stepGoal: Int = 10000,
    var waterGoal: Float = 2.0f,
    var sleepGoal: Float = 8.0f,
    var profilePictureIndex: Int = 0
)

data class DailyGoals(
    val date: LocalDate,
    val stepsAchieved: Boolean = false,
    val waterAchieved: Boolean = false,
    val sleepAchieved: Boolean = false,
    val moodLogged: Boolean = false
)

// Challenge-related data classes
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

// Navigation data class
data class NavigationItem(
    val label: String,
    val icon: ImageVector
)
// Add these conversion functions right after your data classes and before the @Composable functions
// Place them after line 73 (after the NavigationItem data class)

@Composable
fun FourScreenApp(
    navController: NavController,
    challengeViewModel: ChallengeViewModel,
    userProfile: UserProfile,
    profileViewModel: UserProfileViewModel
) {
    var selectedScreen by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            AppTopBar(navController)
        },
        bottomBar = {
            AppBottomNavigationBar(
                selectedScreen = selectedScreen,
                onScreenSelected = { selectedScreen = it }
            )
        }
    ) { innerPadding ->
        when (selectedScreen) {
            0 -> HomeScreen(
                modifier = Modifier.padding(innerPadding),
                challengeViewModel = challengeViewModel,
                userProfile = userProfile
            )
            1 -> FitnessScreen(
                modifier = Modifier.padding(innerPadding),
                userProfile = userProfile,
                profileViewModel = profileViewModel
            )
            2 -> ChallengesScreen(
                modifier = Modifier.padding(innerPadding),
                challengeViewModel = challengeViewModel
            )
            3 -> ProfileScreen(
                modifier = Modifier.padding(innerPadding),
                navController = navController,
                userProfile = userProfile,
                profileViewModel = profileViewModel
            )
        }
    }
}

// Top Bar with Navigation
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(navController: NavController) {
    TopAppBar(
        title = {
            Text(
                "Fitness Check",
                fontWeight = FontWeight.Bold
            )
        },
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

// Bottom Navigation
@Composable
fun AppBottomNavigationBar(
    selectedScreen: Int,
    onScreenSelected: (Int) -> Unit
) {
    val navigationItems = listOf(
        NavigationItem("Home", Icons.Default.Home),
        NavigationItem("Fitness", Icons.Default.FitnessCenter),
        NavigationItem("Challenges", Icons.Default.Star),
        NavigationItem("Profile", Icons.Default.Person)
    )

    NavigationBar(
        containerColor = Color(0xFF1976D2)
    ) {
        navigationItems.forEachIndexed { index, item ->
            NavigationBarItem(
                selected = selectedScreen == index,
                onClick = { onScreenSelected(index) },
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) },
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
}

// Screen 1: Enhanced Home Screen
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    challengeViewModel: ChallengeViewModel,
    userProfile: UserProfile
) {
    val challengeEntities by challengeViewModel.allChallenges.collectAsState(initial = emptyList())
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcome Header
        WelcomeCard(userProfile.name)

        // Quick Stats Row
        QuickStatsRow(
            challengeCount = challengeEntities.size,
            stepGoal = userProfile.stepGoal,
            waterGoal = userProfile.waterGoal
        )

        // Today's Progress
        TodaysProgressCard(userProfile)

        // Active Challenges Preview
        if (challengeEntities.isNotEmpty()) {
            ActiveChallengesCard(challengeEntities.take(3))
        }

        // Quick Actions
        QuickActionsCard(context)
    }
}

// Screen 2: Enhanced Fitness Screen with Database Integration
@Composable
fun FitnessScreen(
    modifier: Modifier = Modifier,
    userProfile: UserProfile,
    profileViewModel: UserProfileViewModel
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
        ScreenTitle("Fitness Tracker")

        // Fitness Trackers with Database Integration
        InteractiveTrackingCard(
            title = "Steps Today",
            value = steps.toString(),
            unit = "steps",
            goal = userProfile.stepGoal.toString(),
            icon = Icons.Default.DirectionsWalk,
            isGoalAchieved = steps >= userProfile.stepGoal,
            onIncrement = { steps += 100 },
            onDecrement = { if (steps > 0) steps -= 100 }
        )

        InteractiveTrackingCard(
            title = "Water Intake",
            value = String.format("%.1f", waterIntake),
            unit = "L",
            goal = userProfile.waterGoal.toString(),
            icon = Icons.Default.LocalDrink,
            isGoalAchieved = waterIntake >= userProfile.waterGoal,
            onIncrement = { waterIntake += 0.1f },
            onDecrement = { if (waterIntake > 0) waterIntake -= 0.1f }
        )

        InteractiveTrackingCard(
            title = "Sleep Duration",
            value = String.format("%.1f", sleepHours),
            unit = "hours",
            goal = userProfile.sleepGoal.toString(),
            icon = Icons.Default.Bedtime,
            isGoalAchieved = sleepHours >= userProfile.sleepGoal,
            onIncrement = { sleepHours += 0.1f },
            onDecrement = { if (sleepHours > 0) sleepHours -= 0.1f }
        )

        // Enhanced Mood Tracker
        MoodTrackerCard(mood) { mood = it }

        // Goal Adjustment Card
        GoalAdjustmentCard(userProfile, profileViewModel)
    }
}

// Screen 3: Enhanced Challenges Screen
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
        // Header with Create Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ScreenTitle("My Challenges")

            Button(
                onClick = { showCreateDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
            ) {
                Icon(Icons.Default.Add, contentDescription = "Create")
                Spacer(modifier = Modifier.width(4.dp))
                Text("Create")
            }
        }

        // Challenges List
        if (challengeEntities.isEmpty()) {
            EmptyChallengesCard { showCreateDialog = true }
        } else {
            challengeEntities.forEach { challengeEntity ->
                ChallengeCard(
                    challenge = challengeEntity.toChallenge(),
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

    // Create Challenge Dialog
    if (showCreateDialog) {
        CreateChallengeDialog(
            onDismiss = { showCreateDialog = false },
            onCreateChallenge = { challenge ->
                challengeViewModel.addChallenge(challenge.toChallengeEntity())
                Toast.makeText(context, "Challenge '${challenge.title}' created!", Toast.LENGTH_SHORT).show()
                showCreateDialog = false
            }
        )
    }
}

// Screen 4: Enhanced Profile Screen with Database Integration
@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    userProfile: UserProfile,
    profileViewModel: UserProfileViewModel
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header with Settings Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ScreenTitle("Profile")

            IconButton(onClick = { navController.navigate("profile_settings") }) {
                Icon(Icons.Default.Settings, contentDescription = "Settings", tint = Color(0xFF1976D2))
            }
        }

        // Profile Card with Database Data
        ProfileCard(userProfile)

        // Goals Card with Real-time Data
        GoalsCard(userProfile)

        // Statistics Card
        StatisticsCard()

        // Action Buttons
        ActionButtonsColumn(navController, profileViewModel)
    }
}

// Component Composables
@Composable
fun WelcomeCard(userName: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1976D2)),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Welcome back, $userName! 👋",
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
}

@Composable
fun QuickStatsRow(challengeCount: Int, stepGoal: Int, waterGoal: Float) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        QuickStatCard(
            title = "Challenges",
            value = challengeCount.toString(),
            icon = Icons.Default.Star,
            color = Color(0xFF4CAF50),
            modifier = Modifier.weight(1f)
        )
        QuickStatCard(
            title = "Step Goal",
            value = stepGoal.toString(),
            icon = Icons.Default.DirectionsWalk,
            color = Color(0xFF2196F3),
            modifier = Modifier.weight(1f)
        )
        QuickStatCard(
            title = "Water Goal",
            value = "${waterGoal}L",
            icon = Icons.Default.LocalDrink,
            color = Color(0xFF00BCD4),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun TodaysProgressCard(userProfile: UserProfile) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF3E5F5))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Today's Progress",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(12.dp))

            ProgressItem("Steps", "7,500", userProfile.stepGoal.toString(), 0.75f)
            Spacer(modifier = Modifier.height(8.dp))
            ProgressItem("Water", "1.8L", "${userProfile.waterGoal}L", 0.9f)
            Spacer(modifier = Modifier.height(8.dp))
            ProgressItem("Sleep", "7.2h", "${userProfile.sleepGoal}h", 0.9f)
        }
    }
}

@Composable
fun ActiveChallengesCard(challenges: List<com.brenda.fitnesscheck.database.ChallengeEntity>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E8))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Active Challenges",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(12.dp))

            challenges.forEach { challenge ->
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
        }
    }
}

@Composable
fun QuickActionsCard(context: android.content.Context) {
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
                        Toast.makeText(context, "Log workout feature coming soon!", Toast.LENGTH_SHORT).show()
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
        }
    }
}

@Composable
fun GoalAdjustmentCard(userProfile: UserProfile, profileViewModel: UserProfileViewModel) {
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
                        profileViewModel.updateStepGoal(userProfile.stepGoal + 1000)
                        Toast.makeText(context, "Step goal increased!", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("🚶 +1K", fontSize = 12.sp)
                }

                // Quick Water Goal Adjustment
                OutlinedButton(
                    onClick = {
                        profileViewModel.updateWaterGoal(userProfile.waterGoal + 0.5f)
                        Toast.makeText(context, "Water goal increased!", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("💧 +0.5L", fontSize = 12.sp)
                }

                // Quick Sleep Goal Adjustment
                OutlinedButton(
                    onClick = {
                        profileViewModel.updateSleepGoal(userProfile.sleepGoal + 0.5f)
                        Toast.makeText(context, "Sleep goal increased!", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("😴 +0.5h", fontSize = 12.sp)
                }
            }
        }
    }
}

// Helper Composables
@Composable
fun ScreenTitle(title: String) {
    Text(
        text = title,
        fontSize = 28.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF1976D2)
    )
}

@Composable
fun InteractiveTrackingCard(
    title: String,
    value: String,
    unit: String,
    goal: String,
    icon: ImageVector,
    isGoalAchieved: Boolean,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit
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
fun MoodTrackerCard(mood: Float, onMoodChange: (Float) -> Unit) {
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
                onValueChange = onMoodChange,
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

@Composable
fun ProfileCard(userProfile: UserProfile) {
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
}

@Composable
fun GoalsCard(userProfile: UserProfile) {
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
}

@Composable
fun StatisticsCard() {
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
}

@Composable
fun ActionButtonsColumn(navController: NavController, profileViewModel: UserProfileViewModel) {
    val context = LocalContext.current

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
                profileViewModel.resetProfile()
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

// Shared Helper Composables
@Composable
fun QuickStatCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
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
        }
    }
}

@Composable
fun ProgressItem(title: String, current: String, goal: String, progress: Float) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(title, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            Text("$current / $goal", fontSize = 14.sp, color = Color.Gray)
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = progress,
            modifier = Modifier.fillMaxWidth(),
            color = Color(0xFF4CAF50),
            trackColor = Color(0xFFE0E0E0)
        )
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

// Additional screens placeholders - these would be in separate files
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(navController: NavController, calendarData: List<DailyGoals>) {
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
            modifier = Modifier
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AchievementsScreen(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Achievements") },
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
            modifier = Modifier
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
            AchievementCard("Perfect Week", "Complete all goals for an entire week", true)
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
fun SettingsScreen(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
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
            modifier = Modifier
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

// Helper functions and extension functions
fun getChallengeTypeEmoji(type: ChallengeType): String {
    return when (type) {
        ChallengeType.STEPS -> "🚶"
        ChallengeType.WATER -> "💧"
        ChallengeType.SLEEP -> "😴"
        ChallengeType.MEDITATION -> "🧘"
        ChallengeType.MIXED -> "🎯"
    }
}

fun getProfilePictureColor(index: Int): Color {
    val colors = listOf(
        Color(0xFF1976D2), Color(0xFF4CAF50), Color(0xFFFF9800),
        Color(0xFF9C27B0), Color(0xFFF44336)
    )
    return colors[index % colors.size]
}

// Extension functions for Challenge Entity conversion
fun Challenge.toChallengeEntity(): com.brenda.fitnesscheck.database.ChallengeEntity {
    return com.brenda.fitnesscheck.database.ChallengeEntity(
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

fun com.brenda.fitnesscheck.database.ChallengeEntity.toChallenge(): Challenge {
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

// Placeholder composables for missing components
@Composable
fun EmptyChallengesCard(onCreateChallenge: () -> Unit) {
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
                onClick = onCreateChallenge,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
            ) {
                Icon(Icons.Default.Add, contentDescription = "Create")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Create Challenge")
            }
        }
    }
}

@Composable
fun ChallengeCard(
    challenge: Challenge,
    onUpdateProgress: (String, Float) -> Unit,
    onDeleteChallenge: (String) -> Unit
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

            if (challenge.isActive) {
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = challenge.progress / challenge.maxProgress,
                    modifier = Modifier.fillMaxWidth(),
                    color = Color(0xFF4CAF50)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "$progressPercentage% complete",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF4CAF50)
                )
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
                Text(
                    text = "⏱️ ${challenge.duration}",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }

            if (challenge.prize.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
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
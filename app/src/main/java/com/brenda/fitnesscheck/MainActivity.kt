// Enhanced Interactive Jetpack Compose Fitness Check App with Profile Settings and Calendar
package com.brenda.fitnesscheck

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Build
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import java.time.LocalDate
import java.time.LocalDate.now
import java.time.format.DateTimeFormatter
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

data class DailyGoals(
    val date: LocalDate,
    val stepsAchieved: Boolean = false,
    val waterAchieved: Boolean = false,
    val sleepAchieved: Boolean = false,
    val moodLogged: Boolean = false
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FitnessCheckTheme {
                AppNavigation()
            }
        }
    }
}

@Composable
fun FitnessCheckTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Color(0xFF1976D2),
            secondary = Color(0xFF4CAF50)
        ),
        content = content
    )
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    // Shared user profile state
    var userProfile by remember { mutableStateOf(UserProfile()) }

    // Generate sample calendar data
    val calendarData = remember { generateSampleCalendarData() }

    NavHost(navController, startDestination = "wellness_home") {
        composable("wellness_home") {
            WellnessApp(navController, userProfile) { userProfile = it }
        }
        composable("home") { HomeScreen(navController) }
        composable("second") { SecondScreen(navController) }
        composable("settings") { SettingsScreen(navController) }
        composable("achievements") { AchievementsScreen(navController) }
        composable("detailed_tracking") { DetailedTrackingScreen(navController) }
        composable("profile_settings") {
            ProfileSettingsScreen(
                navController = navController,
                userProfile = userProfile,
                onProfileUpdate = { userProfile = it },
                modifier = Modifier
            )
        }
        composable("calendar") { CalendarScreen(navController, calendarData) }
    }
}

// Helper function to generate sample calendar data
fun generateSampleCalendarData(): List<DailyGoals> {
    val today = now()
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

// Helper function for profile picture colors
fun getProfilePictureColor(index: Int): Color {
    val colors = listOf(
        Color(0xFF1976D2), // Blue
        Color(0xFF4CAF50), // Green
        Color(0xFFFF9800), // Orange
        Color(0xFF9C27B0), // Purple
        Color(0xFFF44336)  // Red
    )
    return colors[index % colors.size]
}

@Composable
fun WellnessApp(
    mainNavController: NavController,
    userProfile: UserProfile,
    onProfileUpdate: (UserProfile) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = { WellnessTopBar(mainNavController) },
        bottomBar = { BottomNavBar(selectedTab = selectedTab, onTabSelected = { selectedTab = it }) }
    ) { innerPadding ->
        when (selectedTab) {
            0 -> WellnessHomeScreen(
                modifier = Modifier.padding(innerPadding),
                userProfile = userProfile
            )
            1 -> TrackingScreen(
                modifier = Modifier.padding(innerPadding),
                navController = mainNavController
            )
            2 -> MeditationScreen(modifier = Modifier.padding(innerPadding))
            3 -> FriendsScreen(modifier = Modifier.padding(innerPadding))
            4 -> ProfileScreen(
                modifier = Modifier.padding(innerPadding),
                navController = mainNavController,
                userProfile = userProfile,
                onProfileUpdate = onProfileUpdate
            )
            else -> WellnessHomeScreen(
                modifier = Modifier.padding(innerPadding),
                userProfile = userProfile
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WellnessTopBar(navController: NavController) {
    TopAppBar(
        title = { Text("Fitness Check") },
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
fun WellnessDashboardCard(
    title: String,
    value: String,
    unit: String,
    goal: String = "",
    onIncrement: () -> Unit = {},
    onDecrement: () -> Unit = {},
    showControls: Boolean = false,
    isGoalAchieved: Boolean = false
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isGoalAchieved) Color(0xFFE8F5E8) else Color(0xFFE3F2FD)
        ),
        border = if (isGoalAchieved) BorderStroke(2.dp, Color(0xFF4CAF50)) else null
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = title, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                if (isGoalAchieved) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = "Goal Achieved",
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "$value $unit", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    if (goal.isNotEmpty()) {
                        Text(text = "Goal: $goal", fontSize = 14.sp, color = Color.Gray)
                    }
                }

                if (showControls) {
                    Row {
                        IconButton(onClick = onDecrement) {
                            Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Decrease")
                        }
                        IconButton(onClick = onIncrement) {
                            Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Increase")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MoodTracker(moodValue: Float = 5f, onMoodChange: (Float) -> Unit = {}) {
    val moodLabels = listOf("Terrible", "Poor", "Fair", "Good", "Great")
    val moodIndex = ((moodValue - 1) / 2).roundToInt().coerceIn(0, 4)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("How do you feel today?", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(8.dp))

            Slider(
                value = moodValue,
                onValueChange = onMoodChange,
                valueRange = 1f..10f,
                steps = 8
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Mood: ${moodValue.roundToInt()}/10", fontSize = 16.sp)
                Text(moodLabels[moodIndex], fontSize = 16.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
fun BottomNavBar(selectedTab: Int, onTabSelected: (Int) -> Unit) {
    NavigationBar {
        NavigationBarItem(
            selected = selectedTab == 0,
            onClick = { onTabSelected(0) },
            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
            label = { Text("Home") }
        )
        NavigationBarItem(
            selected = selectedTab == 1,
            onClick = { onTabSelected(1) },
            icon = { Icon(Icons.Default.Favorite, contentDescription = "Track") },
            label = { Text("Track") }
        )
        NavigationBarItem(
            selected = selectedTab == 2,
            onClick = { onTabSelected(2) },
            icon = { Icon(Icons.Default.Star, contentDescription = "Meditate") },
            label = { Text("Meditate") }
        )
        NavigationBarItem(
            selected = selectedTab == 3,
            onClick = { onTabSelected(3) },
            icon = { Icon(Icons.Default.Person, contentDescription = "Friends") },
            label = { Text("Friends") }
        )
        NavigationBarItem(
            selected = selectedTab == 4,
            onClick = { onTabSelected(4) },
            icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
            label = { Text("Profile") }
        )
    }
}

@SuppressLint("DefaultLocale")
@Composable
fun WellnessHomeScreen(modifier: Modifier = Modifier, userProfile: UserProfile) {
    var steps by remember { mutableIntStateOf(8230) }
    var waterIntake by remember { mutableFloatStateOf(1.8f) }
    var sleepHours by remember { mutableFloatStateOf(7.2f) }
    var mood by remember { mutableFloatStateOf(5f) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcome message
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF3E5F5))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Welcome back, ${userProfile.name}! 👋",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Let's check your progress today",
                    fontSize = 16.sp,
                    color = Color.Gray
                )
            }
        }

        WellnessDashboardCard(
            title = "Steps Today",
            value = steps.toString(),
            unit = "steps",
            goal = "${userProfile.stepGoal} steps",
            showControls = true,
            onIncrement = { steps += 100 },
            onDecrement = { if (steps > 0) steps -= 100 },
            isGoalAchieved = steps >= userProfile.stepGoal
        )

        WellnessDashboardCard(
            title = "Water Intake",
            value = String.format("%.1f", waterIntake),
            unit = "litres",
            goal = "${userProfile.waterGoal}L",
            showControls = true,
            onIncrement = { waterIntake += 0.1f },
            onDecrement = { if (waterIntake > 0) waterIntake -= 0.1f },
            isGoalAchieved = waterIntake >= userProfile.waterGoal
        )

        WellnessDashboardCard(
            title = "Sleep Duration",
            value = String.format("%.1f", sleepHours),
            unit = "hours",
            goal = "${userProfile.sleepGoal}h",
            showControls = true,
            onIncrement = { sleepHours += 0.1f },
            onDecrement = { if (sleepHours > 0) sleepHours -= 0.1f },
            isGoalAchieved = sleepHours >= userProfile.sleepGoal
        )

        MoodTracker(moodValue = mood, onMoodChange = { mood = it })
    }
}

@Composable
fun TrackingScreen(modifier: Modifier = Modifier, navController: NavController) {
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Track Your Progress",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Quick Actions", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        Toast.makeText(context, "Quick track activated!", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = "Quick Track")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Quick Track")
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = { navController.navigate("detailed_tracking") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Star, contentDescription = "Detailed Tracking")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Detailed Tracking")
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = { navController.navigate("calendar") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.DateRange, contentDescription = "Calendar")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("View Calendar")
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Weekly Summary", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(8.dp))
                Text("• Average daily steps: 8,450")
                Text("• Water intake goal: 85% achieved")
                Text("• Sleep quality: Good")
                Text("• Mood trend: Improving")
            }
        }
    }
}

@Composable
fun MeditationScreen(modifier: Modifier = Modifier) {
    var isPlaying by remember { mutableStateOf(false) }
    var duration by remember { mutableIntStateOf(5) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Default.Star,
            contentDescription = "Meditate",
            modifier = Modifier.size(64.dp),
            tint = Color(0xFF4CAF50)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Meditation",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))

        Text("Duration: $duration minutes")
        Slider(
            value = duration.toFloat(),
            onValueChange = { duration = it.toInt() },
            valueRange = 1f..30f,
            steps = 28
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { isPlaying = !isPlaying },
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isPlaying) Color(0xFFF44336) else Color(0xFF4CAF50)
            )
        ) {
            Icon(
                Icons.Default.PlayArrow,
                contentDescription = if (isPlaying) "Pause" else "Play"
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(if (isPlaying) "Pause" else "Start Meditation")
        }

        if (isPlaying) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Meditation in progress...",
                fontSize = 16.sp,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun FriendsScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Default.Person,
            contentDescription = "Friends",
            modifier = Modifier.size(64.dp),
            tint = Color(0xFF1976D2)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Friends & Challenges",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Connect with friends and join challenges",
            fontSize = 16.sp,
            color = Color.Gray
        )
    }
}

@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    userProfile: UserProfile,
    onProfileUpdate: (UserProfile) -> Unit
) {
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
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )

            IconButton(onClick = { navController.navigate("profile_settings") }) {
                Icon(Icons.Default.Settings, contentDescription = "Profile Settings")
            }
        }

        // Profile Picture and Name
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E8))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Profile Picture Placeholder
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(getProfilePictureColor(userProfile.profilePictureIndex)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = userProfile.name.first().toString(),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = userProfile.name,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Goals Overview
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Your Goals", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(8.dp))
                Text("🚶 Daily Steps: ${userProfile.stepGoal}")
                Text("💧 Water Intake: ${userProfile.waterGoal}L")
                Text("😴 Sleep Duration: ${userProfile.sleepGoal}h")
            }
        }

        // Quick Actions
        Button(
            onClick = { navController.navigate("profile_settings") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Edit, contentDescription = "Edit Profile")
            Spacer(modifier = Modifier.width(8.dp))
            Text("Edit Profile & Goals")
        }

        Button(
            onClick = { navController.navigate("calendar") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.DateRange, contentDescription = "View Calendar")
            Spacer(modifier = Modifier.width(8.dp))
            Text("View Achievement Calendar")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileSettingsScreen(
    navController: NavController,
    userProfile: UserProfile,
    onProfileUpdate: (UserProfile) -> Unit,
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
                            // Validate and save changes
                            val stepGoal = tempStepGoal.toIntOrNull() ?: userProfile.stepGoal
                            val waterGoal = tempWaterGoal.toFloatOrNull() ?: userProfile.waterGoal
                            val sleepGoal = tempSleepGoal.toFloatOrNull() ?: userProfile.sleepGoal

                            val updatedProfile = userProfile.copy(
                                name = tempName.ifBlank { userProfile.name },
                                stepGoal = stepGoal,
                                waterGoal = waterGoal,
                                sleepGoal = sleepGoal,
                                profilePictureIndex = tempProfilePictureIndex
                            )

                            onProfileUpdate(updatedProfile)
                            Toast.makeText(context, "Profile updated!", Toast.LENGTH_SHORT).show()
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
            // Profile Picture Selection
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

            // Name Setting
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

            // Goals Settings
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

            // Tips Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF3E5F5))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("💡 Tips", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("• Set realistic goals that you can achieve consistently")
                    Text("• Adjust your goals as you progress in your fitness journey")
                    Text("• Remember: Small daily improvements lead to big results!")
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
                }
            )
        }
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            // Legend
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

            // Calendar Grid
            Text(
                text = "Last 30 Days",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(7),
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(calendarData) { dayData ->
                    CalendarDayItem(dayData = dayData)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Statistics
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

                    Text("🎯 Perfect Days: $completedDays/$totalDays")
                    Text("📊 Completion Rate: $completionRate%")
                    Text("🚶 Step Goals Met: ${calendarData.count { it.stepsAchieved }}")
                    Text("💧 Water Goals Met: ${calendarData.count { it.waterAchieved }}")
                    Text("😴 Sleep Goals Met: ${calendarData.count { it.sleepAchieved }}")
                }
            }
        }
    }
}

@Composable
fun LegendItem(emoji: String, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
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
        3 -> Color(0xFF4CAF50) // Green - All goals
        2 -> Color(0xFFFF9800) // Orange - 2 goals
        1 -> Color(0xFFFFC107) // Yellow - 1 goal
        else -> Color(0xFFF44336) // Red - No goals
    }

    val today = now()
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
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
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
fun HomeScreen(navController: NavController, modifier: Modifier = Modifier) {
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Home Screen") },
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
            Button(
                onClick = {
                    Toast.makeText(context, "Home button is clicked.", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Click me")
            }

            Button(
                onClick = { navController.navigate("second") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Go to Second Screen")
            }

            Button(
                onClick = { navController.navigate("wellness_home") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Back to Wellness App")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecondScreen(navController: NavController, modifier: Modifier = Modifier) {
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Second Screen") },
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
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Welcome to Second Screen!",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    Toast.makeText(context, "Second screen button clicked!", Toast.LENGTH_SHORT).show()
                }
            ) {
                Text("Click me too!")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { navController.navigate("wellness_home") }
            ) {
                Text("Back to Wellness App")
            }
        }
    }
}

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

            Button(
                onClick = { navController.navigate("home") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Go to Home Screen")
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
            AchievementCard(
                title = "Step Master",
                description = "Walk 10,000 steps in a day",
                isCompleted = true
            )

            AchievementCard(
                title = "Hydration Hero",
                description = "Drink 2L of water daily for 7 days",
                isCompleted = true
            )

            AchievementCard(
                title = "Sleep Champion",
                description = "Get 8 hours of sleep for 5 consecutive nights",
                isCompleted = false
            )

            AchievementCard(
                title = "Meditation Master",
                description = "Complete 30 meditation sessions",
                isCompleted = false
            )

            AchievementCard(
                title = "Consistency King",
                description = "Meet all daily goals for 7 days straight",
                isCompleted = false
            )

            AchievementCard(
                title = "Perfect Week",
                description = "Complete all goals for an entire week",
                isCompleted = true
            )
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

            Button(
                onClick = { navController.navigate("calendar") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.DateRange, contentDescription = "Calendar")
                Spacer(modifier = Modifier.width(8.dp))
                Text("View Achievement Calendar")
            }

            Text(
                text = "Coming Soon - Detailed charts and analytics",
                fontSize = 16.sp,
                color = Color.Gray,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

// Preview functions
@Preview(showBackground = true)
@Composable
fun PreviewWellnessHomeScreen() {
    FitnessCheckTheme {
        WellnessHomeScreen(userProfile = UserProfile())
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewMeditationScreen() {
    FitnessCheckTheme {
        MeditationScreen()
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewCalendarDayItem() {
    FitnessCheckTheme {
        CalendarDayItem(
            dayData = DailyGoals(
                date = now(),
                stepsAchieved = true,
                waterAchieved = true,
                sleepAchieved = false
            )
        )
    }
}
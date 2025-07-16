// Fixed Interactive Jetpack Compose Fitness Check App with Navigation
package com.brenda.fitnesscheck

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlin.math.roundToInt

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

    NavHost(navController, startDestination = "wellness_home") {
        composable("wellness_home") { WellnessApp(navController) }
        composable("home") { HomeScreen(navController) }
        composable("second") { SecondScreen(navController) }
        composable("settings") { SettingsScreen(navController) }
        composable("achievements") { AchievementsScreen(navController) }
        composable("detailed_tracking") { DetailedTrackingScreen(navController) }
    }
}

@Composable
fun WellnessApp(mainNavController: NavController) {
    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = { WellnessTopBar(mainNavController) },
        bottomBar = { BottomNavBar(selectedTab = selectedTab, onTabSelected = { selectedTab = it }) }
    ) { innerPadding ->
        when (selectedTab) {
            0 -> WellnessHomeScreen(modifier = Modifier.padding(innerPadding))
            1 -> TrackingScreen(modifier = Modifier.padding(innerPadding), navController = mainNavController)
            2 -> MeditationScreen(modifier = Modifier.padding(innerPadding))
            3 -> FriendsScreen(modifier = Modifier.padding(innerPadding))
            4 -> ProfileScreen(modifier = Modifier.padding(innerPadding))
            else -> WellnessHomeScreen(modifier = Modifier.padding(innerPadding))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WellnessTopBar(navController: NavController) {
    TopAppBar(
        title = { Text("Fitness Check") },
        actions = {
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
    onIncrement: () -> Unit = {},
    onDecrement: () -> Unit = {},
    showControls: Boolean = false
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "$value $unit", fontSize = 24.sp, fontWeight = FontWeight.Bold)

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
fun WellnessHomeScreen(modifier: Modifier = Modifier) {
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
        WellnessDashboardCard(
            title = "Steps Today",
            value = steps.toString(),
            unit = "steps",
            showControls = true,
            onIncrement = { steps += 100 },
            onDecrement = { if (steps > 0) steps -= 100 }
        )

        WellnessDashboardCard(
            title = "Water Intake",
            value = String.format("%.1f", waterIntake),
            unit = "litres",
            showControls = true,
            onIncrement = { waterIntake += 0.1f },
            onDecrement = { if (waterIntake > 0) waterIntake -= 0.1f }
        )

        WellnessDashboardCard(
            title = "Sleep Duration",
            value = String.format("%.1f", sleepHours),
            unit = "hours",
            showControls = true,
            onIncrement = { sleepHours += 0.1f },
            onDecrement = { if (sleepHours > 0) sleepHours -= 0.1f }
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
fun ProfileScreen(modifier: Modifier = Modifier) {
    var name by remember { mutableStateOf("Brenda") }
    var goal by remember { mutableStateOf("10000") }
    var isEditing by remember { mutableStateOf(false) }

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

            IconButton(onClick = { isEditing = !isEditing }) {
                Icon(
                    if (isEditing) Icons.Default.Check else Icons.Default.Edit,
                    contentDescription = if (isEditing) "Save" else "Edit"
                )
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E8))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Name", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                if (isEditing) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    Text(name, fontSize = 18.sp)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text("Daily Step Goal", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                if (isEditing) {
                    OutlinedTextField(
                        value = goal,
                        onValueChange = { goal = it },
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    Text("$goal steps", fontSize = 18.sp)
                }
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
        WellnessHomeScreen()
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
fun PreviewFriendsScreen() {
    FitnessCheckTheme {
        FriendsScreen()
    }
}
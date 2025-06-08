// Jetpack Compose Mockups for CP3406 Assignment - Scenario 1: Personalised Health and Wellness Assistant

package com.brenda.fitnesscheck

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.brenda.fitnesscheck.ui.theme.FitnessCheckTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FitnessCheckTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = { BottomNavBar() }
                ) { innerPadding ->
                    WellnessHomeScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun WellnessDashboardCard(title: String, value: String, unit: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "$value $unit", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun MoodTracker(moodValue: Float = 5f, onMoodChange: (Float) -> Unit = {}) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text("How do you feel today?", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(8.dp))
        Slider(
            value = moodValue,
            onValueChange = onMoodChange,
            valueRange = 0f..10f,
            steps = 9
        )
        Text("Mood: ${moodValue.toInt()}/10", fontSize = 16.sp)
    }
}

@Composable
fun BottomNavBar() {
    NavigationBar {
        NavigationBarItem(
            selected = true,
            onClick = {},
            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
            label = { Text("Home") }
        )
        NavigationBarItem(
            selected = false,
            onClick = {},
            icon = { Icon(Icons.Default.Favorite, contentDescription = "Track") },
            label = { Text("Track") }
        )
        NavigationBarItem(
            selected = false,
            onClick = {},
            icon = { Icon(Icons.Default.Star, contentDescription = "Meditate") },
            label = { Text("Meditate") }
        )

        NavigationBarItem(
            selected = false,
            onClick = {},
            icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
            label = { Text("Profile") }
        )
    }
}

@Composable
fun WellnessHomeScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        WellnessDashboardCard(title = "Steps Today", value = "8230", unit = "steps")
        WellnessDashboardCard(title = "Water Intake", value = "1.8", unit = "litres")
        WellnessDashboardCard(title = "Sleep Duration", value = "7.2", unit = "hours")
        MoodTracker()
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewWellnessHomeScreen() {
    FitnessCheckTheme {
        Scaffold(
            bottomBar = { BottomNavBar() }
        ) { padding ->
            WellnessHomeScreen(modifier = Modifier.padding(padding))
        }
    }
}

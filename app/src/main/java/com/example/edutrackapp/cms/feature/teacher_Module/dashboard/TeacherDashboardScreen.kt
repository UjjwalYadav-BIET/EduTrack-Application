package com.example.edutrackapp.cms.feature.teacher_Module.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.edutrackapp.cms.ui.navigation.Screen

@Composable
fun TeacherDashboardScreen(
    navController: NavController,
    teacherName: String = "Prof. Ujjwal Yadav" // Hardcoded for now
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5)) // Light Gray Background
    ) {
        // 1. Top Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                .background(Color(0xFF6200EE))
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Profile Image Placeholder
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "S", fontSize = 24.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(text = "Welcome back,", color = Color.White.copy(alpha = 0.8f))
                        Text(text = teacherName, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 2. Dashboard Grid Options
        val menuItems = listOf(
            DashboardItem("Attendance", Icons.Default.CheckCircle, Color(0xFF4CAF50)),
            DashboardItem("Timetable", Icons.Default.DateRange, Color(0xFF2196F3)),
            DashboardItem("Assignments", Icons.Default.Edit, Color(0xFFFF9800)),
            DashboardItem("Notices", Icons.Default.Notifications, Color(0xFFE91E63)),
            DashboardItem("Results", Icons.Default.Star, Color(0xFF9C27B0)),
            DashboardItem("Profile", Icons.Default.Person, Color(0xFF607D8B))
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // In TeacherDashboardScreen.kt
            items(menuItems) { item ->
                DashboardCard(item) {
                    when(item.title) {
                        "Attendance" -> navController.navigate(Screen.Attendance.route)
                        "Timetable" -> navController.navigate(Screen.Timetable.route) // <--- Add this
                        "Assignments" -> navController.navigate(Screen.TeacherAssignmentList.route)
                        "Notices" -> navController.navigate(Screen.Notices.route)
                        "Results" -> navController.navigate(Screen.Results.route)
                        "Profile" -> navController.navigate(Screen.Profile.route)
                    }
                }
            }
        }
    }
}

@Composable
fun DashboardCard(item: DashboardItem, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .height(120.dp)
            // FIX: Call the onClick function here
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = item.title,
                tint = item.color,
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = item.title, fontWeight = FontWeight.Medium, color = Color.Black)
        }
    }
}

data class DashboardItem(val title: String, val icon: ImageVector, val color: Color)
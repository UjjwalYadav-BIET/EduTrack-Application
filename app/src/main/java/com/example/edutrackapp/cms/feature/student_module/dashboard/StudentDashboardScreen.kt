package com.example.edutrackapp.cms.feature.student_module.dashboard

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
fun StudentDashboardScreen(
    navController: NavController,
    studentName: String = "Ujjwal Yadav" // Hardcoded for now
) {
    val studentColor = Color(0xFF009688) // Teal Color for Students

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF0F4F4)) // Very light teal background
    ) {
        // 1. Student Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                .background(studentColor)
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("U", fontSize = 24.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(text = "Hello,", color = Color.White.copy(alpha = 0.8f))
                        Text(text = studentName, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                        Text(text = "Class: CS-A | Roll: 101", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // "Quick Stats" Card floating inside header
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.15f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Attendance", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                            Text("85%", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        }
                        Column {
                            Text("CGPA", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                            Text("8.2", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        }
                        Column {
                            Text("Notices", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                            Text("3 New", color = Color.Yellow, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 2. Grid Options
        val menuItems = listOf(
            StudentMenuItem("My Attendance", Icons.Default.DateRange, Color(0xFF009688)),
            StudentMenuItem("My Results", Icons.Default.Equalizer, Color(0xFF3F51B5)),
            StudentMenuItem("Timetable", Icons.Default.Schedule, Color(0xFFFF9800)),
            StudentMenuItem("Notices", Icons.Default.Campaign, Color(0xFFE91E63)),
            StudentMenuItem("Assignments", Icons.Default.Assignment, Color(0xFF673AB7)),
            StudentMenuItem("Profile", Icons.Default.Person, Color(0xFF607D8B))
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(menuItems) { item ->
                StudentMenuCard(item) {
                    // TODO: Navigate to Student Features
                    when(item.title) {
                        "My Attendance" -> navController.navigate(Screen.StudentAttendance.route)
                        // Inside StudentMenuCard click logic:
                        "Timetable" -> navController.navigate(Screen.StudentTimetable.route)
                        "Notices" -> navController.navigate(Screen.StudentNotices.route)
                        "My Results" -> navController.navigate(Screen.StudentResults.route)
                        "Assignments" -> navController.navigate(Screen.StudentAssignments.route)
                        "Profile" -> navController.navigate(Screen.StudentProfile.route)
                        // We will build specific screens for Attendance/Results later
                    }
                }
            }
        }
    }
}

@Composable
fun StudentMenuCard(item: StudentMenuItem, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .height(110.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
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
                modifier = Modifier.size(36.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = item.title, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        }
    }
}

data class StudentMenuItem(val title: String, val icon: ImageVector, val color: Color)
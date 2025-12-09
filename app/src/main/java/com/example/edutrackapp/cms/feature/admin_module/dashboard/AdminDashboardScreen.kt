package com.example.edutrackapp.cms.feature.admin_module.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.edutrackapp.cms.ui.navigation.Screen

@Composable
fun AdminDashboardScreen(
    navController: NavController
) {
    // Admin Brand Color (Charcoal/Dark Gray)
    val adminColor = Color(0xFF263238)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFECEFF1)) // Light Gray BG
    ) {
        // 1. Admin Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .background(
                    color = adminColor,
                    shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
                )
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .align(Alignment.CenterStart)
            ) {
                Text("Administrator", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text("Control Panel", fontSize = 14.sp, color = Color.Gray)
            }

            Icon(
                imageVector = Icons.Default.AdminPanelSettings,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.2f),
                modifier = Modifier
                    .size(100.dp)
                    .align(Alignment.CenterEnd)
                    .offset(x = (-20).dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 2. Control Grid
        val menuItems = listOf(
            AdminMenuItem("Add Teacher", Icons.Default.PersonAdd, Color(0xFF4CAF50)),
            AdminMenuItem("Manage Students", Icons.Default.School, Color(0xFF2196F3)),
            AdminMenuItem("Analytics", Icons.Default.Analytics, Color(0xFFFF9800)),
            AdminMenuItem("Broadcast", Icons.Default.Campaign, Color(0xFFE91E63)),
            AdminMenuItem("Database", Icons.Default.Storage, Color(0xFF9C27B0)),
            AdminMenuItem("Logout", Icons.Default.ExitToApp, Color(0xFFF44336))
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(menuItems) { item ->
                AdminMenuCard(item) {
                    when(item.title) {
                        "Add Teacher" -> navController.navigate(Screen.AddTeacher.route)
                        "Manage Students" -> navController.navigate(Screen.AddStudent.route)
                        "Logout" -> {
                            navController.navigate(Screen.Login.route) {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                        // Other features can be added later
                    }
                }
            }
        }
    }
}

@Composable
fun AdminMenuCard(item: AdminMenuItem, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .height(130.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp),
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
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = item.title, fontWeight = FontWeight.Bold, color = Color.DarkGray)
        }
    }
}

data class AdminMenuItem(val title: String, val icon: ImageVector, val color: Color)
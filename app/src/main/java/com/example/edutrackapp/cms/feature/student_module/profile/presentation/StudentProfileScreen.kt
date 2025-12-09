package com.example.edutrackapp.cms.feature.student_module.profile.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentProfileScreen(
    navController: NavController
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Profile") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF009688), // Student Teal
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF0F4F4)),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. Header with Avatar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                    .background(Color(0xFF009688)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("U", fontSize = 40.sp, fontWeight = FontWeight.Bold, color = Color(0xFF009688))
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Ujjwal Yadav", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    Text("B.Tech - Computer Science", color = Color.White.copy(alpha = 0.9f))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 2. Academic Details Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Academic Details", fontWeight = FontWeight.Bold, color = Color(0xFF009688))
                    Spacer(modifier = Modifier.height(16.dp))

                    ProfileRow(Icons.Default.Badge, "Roll Number", "CS-101")
                    Divider(Modifier.padding(vertical = 8.dp), color = Color(0xFFF0F0F0))
                    ProfileRow(Icons.Default.Class, "Current Batch", "CS-A (Semester 5)")
                    Divider(Modifier.padding(vertical = 8.dp), color = Color(0xFFF0F0F0))
                    ProfileRow(Icons.Default.Email, "Email", "student@test.com")
                    Divider(Modifier.padding(vertical = 8.dp), color = Color(0xFFF0F0F0))
                    ProfileRow(Icons.Default.Phone, "Guardian Phone", "+91 98765 00000")
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // 3. Logout Button
            Button(
                onClick = {
                    // LOGOUT: Clear stack and go to Login
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.ExitToApp, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("LOGOUT", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun ProfileRow(icon: ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(text = label, fontSize = 12.sp, color = Color.Gray)
            Text(text = value, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}
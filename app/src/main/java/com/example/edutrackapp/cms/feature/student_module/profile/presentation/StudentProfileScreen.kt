package com.example.edutrackapp.cms.feature.student_module.profile.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.edutrackapp.cms.feature.student_module.dashboard.StudentViewModel  // ← import ViewModel
import com.example.edutrackapp.cms.ui.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentProfileScreen(
    navController: NavController,
    viewModel: StudentViewModel = hiltViewModel()   // ← inject shared ViewModel
) {
    // ── Collect real data from Firestore ──────────────────────────────────────
    val profile by viewModel.profile.collectAsState()

    val displayName   = profile.name.ifBlank { "Student" }
    val avatarLetter  = displayName.firstOrNull()?.uppercaseChar()?.toString() ?: "S"
    val department    = profile.department.ifBlank { "N/A" }
    val enrollmentId  = profile.enrollmentId.ifBlank { "N/A" }
    val email         = profile.email.ifBlank { "N/A" }
    val phone         = profile.phone.ifBlank { "N/A" }

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
                    containerColor = Color(0xFF009688),
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
            // ── Header with Avatar ────────────────────────────────────────────
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
                        // ← real initial from Firestore name
                        Text(
                            text       = avatarLetter,
                            fontSize   = 40.sp,
                            fontWeight = FontWeight.Bold,
                            color      = Color(0xFF009688)
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    // ← real name
                    Text(
                        displayName,
                        color      = Color.White,
                        fontSize   = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                    // ← real department
                    Text(
                        "B.Tech - $department",
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── Academic Details Card ─────────────────────────────────────────
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors    = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp),
                shape     = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Academic Details",
                        fontWeight = FontWeight.Bold,
                        color      = Color(0xFF009688)
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // ← real enrollmentId
                    ProfileRow(Icons.Default.Badge, "Enrollment ID", enrollmentId)
                    Divider(Modifier.padding(vertical = 8.dp), color = Color(0xFFF0F0F0))
                    // ← real department
                    ProfileRow(Icons.Default.Class, "Department", department)
                    Divider(Modifier.padding(vertical = 8.dp), color = Color(0xFFF0F0F0))
                    // ← real email
                    ProfileRow(Icons.Default.Email, "Email", email)
                    Divider(Modifier.padding(vertical = 8.dp), color = Color(0xFFF0F0F0))
                    // ← real phone
                    ProfileRow(Icons.Default.Phone, "Guardian Phone", phone)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // ── Logout Button ─────────────────────────────────────────────────
            Button(
                onClick = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                shape  = RoundedCornerShape(12.dp)
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
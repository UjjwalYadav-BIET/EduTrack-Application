package com.example.edutrackapp.cms.feature.student_module.attendance.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentAttendanceScreen(
    navController: NavController,
    viewModel: StudentAttendanceViewModel = hiltViewModel()
) {
    val sessions by viewModel.sessions.collectAsState()
    val overall  by viewModel.overallPercentage.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Attendance") },
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
                .background(Color(0xFFF0F4F4))
        ) {
            // Overall Score Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF009688))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Overall Attendance", color = Color.White.copy(alpha = 0.8f))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "$overall%",
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${sessions.count { it.status == "present" }} / ${sessions.size} sessions",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    if (sessions.isNotEmpty()) {
                        if (overall < 75) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.Red)
                                    .padding(horizontal = 12.dp, vertical = 4.dp)
                            ) {
                                Text("Shortage Alert!", color = Color.White,
                                    fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        } else {
                            Text("You are safe!", color = Color.White.copy(alpha = 0.9f), fontSize = 12.sp)
                        }
                    }
                }
            }

            Text(
                text = "Attendance History",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                fontWeight = FontWeight.Bold,
                color = Color.Gray
            )

            if (sessions.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No attendance records yet.", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(sessions) { session -> SessionCard(session) }
                }
            }
        }
    }
}

@Composable
fun SessionCard(session: AttendanceSession) {
    val isPresent = session.status == "present"
    val color     = if (isPresent) Color(0xFF4CAF50) else Color(0xFFF44336)

    Card(
        colors    = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        shape     = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(session.date, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Text(session.time, fontSize = 12.sp, color = Color.Gray)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (isPresent) Icons.Default.CheckCircle else Icons.Default.Cancel,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isPresent) "Present" else "Absent",
                    fontWeight = FontWeight.Bold,
                    color = color
                )
            }
        }
    }
}
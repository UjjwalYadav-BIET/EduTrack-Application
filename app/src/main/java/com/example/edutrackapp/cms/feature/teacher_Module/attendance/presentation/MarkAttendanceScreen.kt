package com.example.edutrackapp.cms.feature.teacher_Module.attendance.presentation

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.edutrackapp.cms.ui.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarkAttendanceScreen(
    navController: NavController,
    viewModel: AttendanceViewModel = hiltViewModel()
) {
    val students = viewModel.students
    val context = LocalContext.current

    // ==============================================
    // 1. LISTEN FOR CAMERA RESULT (FIXED)
    // ==============================================
    // We get the BackStackEntry first
    val backStackEntry = navController.currentBackStackEntry

    // FIX: We use 'remember' for the fallback state to avoid the composition error
    val faceCount by backStackEntry?.savedStateHandle
        ?.getStateFlow<Int?>("face_count", null)
        ?.collectAsState()
        ?: remember { mutableStateOf(null) }

    // When we get a count from the camera, update the list automatically
    LaunchedEffect(faceCount) {
        faceCount?.let { count ->
            if (count > 0) {
                viewModel.markStudentsBasedOnCount(count)
                Toast.makeText(context, "Auto-marked $count students!", Toast.LENGTH_LONG).show()

                // Clear data so it doesn't trigger again
                backStackEntry?.savedStateHandle?.remove<Int>("face_count")
            }
        }
    }
    // ==============================================

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mark Attendance (CS-A)") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF6200EE),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        // Floating Action Button to Open Camera
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    navController.navigate(Screen.FaceScan.route)
                },
                containerColor = Color(0xFF6200EE),
                contentColor = Color.White
            ) {
                Icon(Icons.Default.CameraAlt, contentDescription = "Smart Attendance")
            }
        },
        bottomBar = {
            Button(
                onClick = {
                    val presentCount = students.count { it.isPresent }
                    val total = students.size

                    Toast.makeText(
                        context,
                        "Attendance Submitted! Present: $presentCount / $total",
                        Toast.LENGTH_SHORT
                    ).show()

                    navController.popBackStack()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6200EE)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("SUBMIT ATTENDANCE", fontWeight = FontWeight.Bold)
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF5F5F5)),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(students) { student ->
                StudentRow(
                    student = student,
                    onToggle = { viewModel.toggleAttendance(student.id) }
                )
            }
        }
    }
}

@Composable
fun StudentRow(student: StudentUiModel, onToggle: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() }, // Clicking the whole card toggles checkbox
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(text = student.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(text = "Roll No: ${student.rollNo}", color = Color.Gray, fontSize = 14.sp)
            }

            // Custom Checkbox UI
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = if (student.isPresent) Color(0xFF4CAF50) else Color.LightGray,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}
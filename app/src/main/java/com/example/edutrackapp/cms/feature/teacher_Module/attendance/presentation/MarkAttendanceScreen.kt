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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.edutrackapp.cms.ui.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarkAttendanceScreen(
    navController: NavController,
    viewModel: AttendanceViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val students = viewModel.students
    val subjects by viewModel.subjects.collectAsState()

    val selectedSubjectId = viewModel.selectedSubjectId
    val selectedLecture = viewModel.selectedLecture
    val selectedDate = viewModel.selectedDate

    var section by remember { mutableStateOf("A") }
    val error = viewModel.errorMessage

    LaunchedEffect(error) {
        error?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
        }
    }


    LaunchedEffect(Unit) {
        viewModel.loadSubjects()
    }

    LaunchedEffect(viewModel.selectedSemester, viewModel.selectedSection) {
        viewModel.loadStudents(
            semester = viewModel.selectedSemester,
            section = viewModel.selectedSection
        )
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mark Attendance") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, null)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF6200EE),
                    titleContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(Screen.FaceScan.route) },
                containerColor = Color(0xFF6200EE)
            ) {
                Icon(Icons.Default.CameraAlt, null)
            }
        },
        bottomBar = {
            Button(
                onClick = {
                    viewModel.submitAttendance()
                    Toast.makeText(context, "Attendance Submitted", Toast.LENGTH_SHORT).show()
                    navController.popBackStack()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("SUBMIT ATTENDANCE", fontWeight = FontWeight.Bold)
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF5F5F5))
        ) {



            SubjectDropdown(
                subjects = subjects,
                selectedId = selectedSubjectId,
                onSelected = { viewModel.selectedSubjectId= it }
            )

            LecturePeriodDropdown(
                selected = selectedLecture,
                onSelected = { viewModel.selectedLecture= it }
            )


            Text(
                text = "Date: $selectedDate",
                modifier = Modifier.padding(16.dp),
                fontWeight = FontWeight.Medium
            )

            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(students) { student ->
                    StudentRow(student) {
                        viewModel.toggleAttendance(student.id)
                    }
                }
            }
        }
    }
}

@Preview(showBackground=true)
@Composable
fun MarkAttendanceScreenPreview(){
    val navController= rememberNavController();
    MarkAttendanceScreen(navController)
}


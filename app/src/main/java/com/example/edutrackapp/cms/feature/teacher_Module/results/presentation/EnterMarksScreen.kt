package com.example.edutrackapp.cms.feature.teacher_Module.results.presentation

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnterMarksScreen(
    navController: NavController,
    testId: Int,
    viewModel: ResultViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val state by viewModel.uiState.collectAsState()

    val filledCount = state.students.count { !it.marks.isNullOrBlank() }

    LaunchedEffect(Unit) {
        viewModel.loadStudents(testId, "CSE", 5, "A")
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Enter Marks") },
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
        bottomBar = {
            Button(
                onClick = {
                    viewModel.saveResults(testId) {
                        Toast.makeText(context, "Results Saved Successfully!", Toast.LENGTH_SHORT).show()
                        navController.popBackStack()
                    }
                },
                enabled = filledCount > 0,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6200EE))
            ) {
                Text("SAVE RESULTS", color = Color.White)
            }
        },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {

                // Loading
                if (state.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                }

                // Error
                state.error?.let {
                    Text("Error: $it", color = Color.Red)
                }

                // Progress
                Text(
                    text = "$filledCount / ${state.students.size} Completed",
                    modifier = Modifier.padding(vertical = 8.dp),
                    fontWeight = FontWeight.Bold
                )

                // Students List
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 80.dp) // so button doesn’t overlap
                ) {
                    itemsIndexed(state.students) { index, student ->
                        val isEmpty = student.marks.isNullOrBlank()

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            border = if (isEmpty) BorderStroke(1.dp, Color.Red) else null
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(student.name)
                                    Text("Roll: ${student.rollNo}")
                                }

                                OutlinedTextField(
                                    value = student.marks ?: "",
                                    onValueChange = {
                                        if (it == "AB" || (it.toIntOrNull() in 0..100)) {
                                            viewModel.onMarksChange(index, it)
                                        }
                                    },
                                    modifier = Modifier.width(100.dp),
                                    singleLine = true
                                )
                            }
                        }
                    }
                }
            }
        }
    )
}
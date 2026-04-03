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
import androidx.compose.ui.text.style.TextAlign
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

    LaunchedEffect(testId) {
        viewModel.loadStudents(testId)
        viewModel.loadTestById(testId)
    }
    val maxMarks = viewModel.selectedTest?.maxMarks ?: 0

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
                    Text("Error: $it", color = Color.Black)
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
                            modifier = Modifier
                                .fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = Color.White
                            ),
                            border = BorderStroke(1.dp, Color.Gray),
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {

                                // 👈 LEFT SIDE (Student Info)
                                Column(
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = student.name,
                                        color = Color.Black,
                                        fontWeight = FontWeight.SemiBold
                                    )

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Text(
                                        text = "Roll: ${student.rollNo}",
                                        color = Color.DarkGray
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                // 👉 RIGHT SIDE (Marks Input)
                                OutlinedTextField(
                                    value = student.marks ?: "",
                                    onValueChange = { input ->

                                        val number = input.toIntOrNull()

                                        if (
                                            input.isEmpty() ||                  // ✅ allow clearing
                                            input == "A" ||                   // ✅ allow AB
                                            (number != null && number in 0..maxMarks)  // ✅ valid number
                                        ) {
                                            viewModel.onMarksChange(index, input)
                                        }
                                    },
                                    modifier = Modifier.width(80.dp),
                                    singleLine = true,

                                    textStyle = LocalTextStyle.current.copy(color = Color.Black),

                                    placeholder = {
                                        Text(
                                            text = if (maxMarks != null) "0 / $maxMarks" else "Loading...",
                                            color = Color.Gray
                                        )
                                    },

                                    // ✅ Rounded modern shape
                                    shape = MaterialTheme.shapes.medium,

                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Color(0xFF6200EE),
                                        unfocusedBorderColor = Color.Gray,
                                        cursorColor = Color.Black
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    )
}
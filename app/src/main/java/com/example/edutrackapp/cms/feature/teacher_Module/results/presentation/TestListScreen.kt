package com.example.edutrackapp.cms.feature.teacher_Module.results.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.edutrackapp.cms.ui.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TestListScreen(
    navController: NavController,
    teacherId: Int,
    viewModel: ResultViewModel = hiltViewModel()
) {

    val tests = viewModel.tests

    LaunchedEffect(Unit) {
        viewModel.loadTestsByTeacher(teacherId)
    }
    LaunchedEffect(Unit) {
        viewModel.loadStudents(tests.firstOrNull()?.testId ?: 0)
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

        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    navController.navigate(Screen.CreateTest.route)
                }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Test")
            }
        }

    ) { paddingValues ->

        // ✅ EMPTY STATE
        if (tests.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(20.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "No Tests Created Yet",
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // ✅ LIST
        else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = paddingValues
            ) {

                items(tests) { test ->

                    Card(
                        modifier = Modifier
                            .padding(8.dp)
                            .fillMaxWidth()
                            .clickable {
                                navController.navigate(
                                    Screen.EnterMarks.createRoute(test.testId)
                                )
                            },
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {

                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {

                            // Test Name
                            Text(
                                text = test.testName,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            // Subject (currently ID)
                            Text(
                                text = "Subject ID: ${test.subject}",
                                color = Color.Gray
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            // Class Info
                            Text(
                                text = "Branch: ${test.branch} | Year: ${test.year} | Sec: ${test.section}",
                                color = Color.Gray
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            // Created Date
                            Text(
                                text = "Created: ${test.date}",
                                color = Color.LightGray
                            )
                        }
                    }
                }
            }
        }
    }
}
package com.example.edutrackapp.cms.feature.student_module.results.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.edutrackapp.cms.core.data.local.entity.ResultEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentResultScreen(
    navController: NavController,
    viewModel: StudentResultViewModel = hiltViewModel()
) {
    val results = viewModel.results.collectAsState().value

    // Define the Student Brand Color (Teal)
    val studentColor = Color(0xFF009688)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Report Card") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = studentColor, // <--- CHANGED TO TEAL
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        if (results.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("No results published yet.", color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(Color(0xFFF0F4F4)), // <--- CHANGED TO LIGHT TEAL BACKGROUND
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header Summary
                item {
                    // Pass the student color to the card
                    ResultSummaryCard(results, studentColor)
                }

                // Individual Subject Marks
                items(results) { result ->
                    MarksCard(result)
                }
            }
        }
    }
}

@Composable
fun ResultSummaryCard(results: List<ResultEntity>, themeColor: Color) {
    val totalObtained = results.sumOf { it.marksObtained.toIntOrNull() ?: 0 }
    val totalMax = results.size * 100
    val percentage = if (totalMax > 0) (totalObtained.toFloat() / totalMax) * 100 else 0f

    Card(
        modifier = Modifier.fillMaxWidth(),
        // Use the passed theme color (Teal)
        colors = CardDefaults.cardColors(containerColor = themeColor),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(24.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("Total Score", color = Color.White.copy(alpha = 0.8f))
                Text(
                    text = "$totalObtained / $totalMax",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            Box(
                modifier = Modifier
                    .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                Text(
                    text = "${"%.1f".format(percentage)}%",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun MarksCard(result: ResultEntity) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = result.subject, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(text = result.examType, fontSize = 12.sp, color = Color.Gray)
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.EmojiEvents, null, tint = Color(0xFFFFC107), modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = result.marksObtained,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = if ((result.marksObtained.toIntOrNull() ?: 0) < 35) Color.Red else Color(0xFF4CAF50)
                )
            }
        }
    }
}
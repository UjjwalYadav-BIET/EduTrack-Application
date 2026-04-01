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
import com.example.edutrackapp.Domain.Model.StudentResultUi
import com.example.edutrackapp.cms.core.data.local.entity.ResultEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentResultScreen(
    navController: NavController,
    viewModel: StudentResultViewModel = hiltViewModel()
) {
    val results = viewModel.results.collectAsState().value
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
                    containerColor = studentColor,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->

        if (results.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("No results published yet.", color = Color.Gray)
            }
        } else {

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(Color(0xFFF7FAFA)),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                item {
                    ResultSummaryCard(results, studentColor)
                }

                items(results) { result ->
                    MarksCard(result)
                }
            }
        }
    }
}





fun getGrade(marks: Int): String {
    return when {
        marks >= 90 -> "A+"
        marks >= 75 -> "A"
        marks >= 60 -> "B"
        marks >= 50 -> "C"
        marks >= 35 -> "D"
        else -> "F"
    }
}

fun isPass(marks: Int): Boolean {
    return marks >= 35
}
package com.example.edutrackapp.cms.feature.student_module.timetable.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentTimeTableScreen(
    navController: NavController,
    viewModel: StudentTimeTableViewModel = hiltViewModel()
) {
    val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri")
    val selectedDay = viewModel.selectedDay.value
    val classes = viewModel.currentClasses.value
    val studentColor = Color(0xFF009688) // Teal

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Schedule") },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF0F4F4))
        ) {
            // 1. Day Selector (Floating Pills)
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(days) { day ->
                    DayPill(
                        day = day,
                        isSelected = day == selectedDay,
                        color = studentColor,
                        onClick = { viewModel.selectDay(day) }
                    )
                }
            }

            // 2. Timeline List
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(classes) { slot ->
                    val isLive = viewModel.isClassLive(slot.startTime)
                    TimelineItem(slot, isLive)
                }
            }
        }
    }
}

@Composable
fun DayPill(day: String, isSelected: Boolean, color: Color, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(if (isSelected) color else Color.White)
            .clickable { onClick() }
            .padding(horizontal = 24.dp, vertical = 10.dp)
            .border(
                width = if (isSelected) 0.dp else 1.dp,
                color = if (isSelected) Color.Transparent else Color.LightGray,
                shape = RoundedCornerShape(50)
            )
    ) {
        Text(
            text = day,
            color = if (isSelected) Color.White else Color.Gray,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun TimelineItem(slot: StudentClassSlot, isLive: Boolean) {
    Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {

        // LEFT: The Time & Line
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(60.dp)
        ) {
            Text(
                text = slot.time.substringBefore(" -"),
                fontWeight = FontWeight.Bold,
                color = if(isLive) Color(0xFF009688) else Color.Gray,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            // The Vertical Line
            Box(
                modifier = Modifier
                    .width(2.dp)
                    .weight(1f) // Fills remaining height
                    .background(Color.LightGray.copy(alpha=0.5f))
            )
        }

        // RIGHT: The Class Card
        Column(modifier = Modifier.weight(1f).padding(bottom = 24.dp)) {
            // The Dot on the line
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(if(isLive) Color(0xFF009688) else Color.Gray)
                        .offset(x = (-67).dp) // Shift left to sit on the line
                )
            }

            // The Card
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if(isLive) Color(0xFFE0F2F1) else Color.White // Highlight if live
                ),
                elevation = CardDefaults.cardElevation(if(isLive) 8.dp else 2.dp),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(16.dp)) {
                    // Icon based on type
                    val icon = when(slot.type) {
                        "Lab" -> Icons.Default.Computer
                        "Break" -> Icons.Default.Schedule
                        else -> Icons.Default.Book
                    }

                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF009688).copy(alpha=0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(icon, null, tint = Color(0xFF009688))
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        Text(text = slot.subject, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(4.dp))

                        if (slot.type != "Break") {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Person, null, modifier = Modifier.size(14.dp), tint = Color.Gray)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = slot.teacher, fontSize = 12.sp, color = Color.Gray)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.LocationOn, null, modifier = Modifier.size(14.dp), tint = Color.Gray)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = slot.room, fontSize = 12.sp, color = Color.Gray)
                            }
                        } else {
                            Text(text = "Relax & Recharge", fontSize = 12.sp, color = Color.Gray, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                        }
                    }
                }
            }
        }
    }
}
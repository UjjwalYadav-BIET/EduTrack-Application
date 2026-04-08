package com.example.edutrackapp.cms.feature.teacher_Module.timetable.presentation

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController

// ─── Theme Colors (matching Teacher Module) ───────────────────────────────────
private val DarkNavy     = Color(0xFF0D1B2A)
private val AccentPurple = Color(0xFF6200EE)
private val SurfaceWhite = Color(0xFFF5F5F5)
private val CardWhite    = Color.White
private val LiveGreen    = Color(0xFF4CAF50)

// ─── Screen ──────────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeTableScreen(
    navController: NavController,
    viewModel: TimeTableViewModel = hiltViewModel()
) {
    val days        = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
    val selectedDay = viewModel.selectedDay.value
    val classes     = viewModel.currentClasses.value
    val todayLabel  = viewModel.todayLabel          // e.g. "Mon"
    val totalHours  = viewModel.totalHoursForDay(selectedDay)

    Scaffold(
        topBar = {
            // ── Gradient Top Bar matching Teacher Module header ──────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(DarkNavy, AccentPurple.copy(alpha = 0.85f))
                        )
                    )
                    .statusBarsPadding()
            ) {
                Column {
                    // Nav row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                        }
                        Text(
                            text = "My Timetable",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(
                            Icons.Default.DateRange,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.8f),
                            modifier = Modifier.padding(end = 12.dp)
                        )
                    }

                    // ── Daily Summary Strip ──────────────────────────────────
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        SummaryChip(label = "Classes", value = "${classes.size}")
                        SummaryChip(label = "Hours", value = "$totalHours hrs")
                        SummaryChip(
                            label = "Today",
                            value = todayLabel,
                            highlight = selectedDay == todayLabel
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(SurfaceWhite)
        ) {
            // ── Day Selector ─────────────────────────────────────────────────
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(days) { day ->
                    DayChip(
                        day = day,
                        isSelected = day == selectedDay,
                        isToday = day == todayLabel,
                        onClick = { viewModel.selectDay(day) }
                    )
                }
            }

            // ── Class List or Empty State ────────────────────────────────────
            if (classes.isEmpty()) {
                EmptyDayView(selectedDay)
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(classes) { slot ->
                        TimeTableCard(
                            slot = slot,
                            isLive = viewModel.isSlotLive(slot)
                        )
                    }
                }
            }
        }
    }
}

// ─── Summary Chip in Header ───────────────────────────────────────────────────
@Composable
private fun SummaryChip(label: String, value: String, highlight: Boolean = false) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            color = if (highlight) Color(0xFFFFD700) else Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
        )
        Text(
            text = label,
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 11.sp
        )
    }
}

// ─── Day Chip ─────────────────────────────────────────────────────────────────
@Composable
fun DayChip(
    day: String,
    isSelected: Boolean,
    isToday: Boolean,
    onClick: () -> Unit
) {
    val bgColor by animateColorAsState(
        targetValue = when {
            isSelected -> AccentPurple
            else       -> CardWhite
        },
        animationSpec = tween(200), label = "chipColor"
    )
    val textColor = if (isSelected) Color.White else Color.Black

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(bgColor)
                .padding(horizontal = 22.dp, vertical = 11.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(text = day, color = textColor, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
        // "Today" dot indicator
        if (isToday) {
            Spacer(modifier = Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) AccentPurple else LiveGreen)
            )
        }
    }
}

// ─── Timetable Card ───────────────────────────────────────────────────────────
@Composable
fun TimeTableCard(slot: TimeTableSlot, isLive: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min),
        verticalAlignment = Alignment.Top
    ) {
        // Time column
        Column(
            modifier = Modifier
                .width(62.dp)
                .padding(top = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = slot.startTime,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = Color(0xFF1A1A2E)
            )
            Text(
                text = slot.endTime,
                color = Color.Gray,
                fontSize = 11.sp
            )
            // Duration pill
            Spacer(modifier = Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFFEDE7F6))
                    .padding(horizontal = 4.dp, vertical = 2.dp)
            ) {
                Text(
                    text = slot.duration,
                    fontSize = 10.sp,
                    color = AccentPurple,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // Timeline dot + line
        Column(
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .fillMaxHeight(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(14.dp))
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(Color(slot.colorHex))
            )
            Box(
                modifier = Modifier
                    .width(2.dp)
                    .weight(1f)
                    .background(Color(slot.colorHex).copy(alpha = 0.25f))
            )
        }

        // Card
        Card(
            modifier = Modifier.weight(1f),
            colors = CardDefaults.cardColors(containerColor = CardWhite),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth()) {
                // Colored accent stripe
                Box(
                    modifier = Modifier
                        .width(6.dp)
                        .fillMaxHeight()
                        .background(Color(slot.colorHex))
                )

                Column(
                    modifier = Modifier
                        .padding(12.dp)
                        .weight(1f)
                ) {
                    // Subject + LIVE badge row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = slot.subject,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color(0xFF1A1A2E),
                            modifier = Modifier.weight(1f)
                        )
                        if (isLive) {
                            LiveBadge()
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Room + Batch row
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = Color.Gray
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(text = slot.room, fontSize = 13.sp, color = Color.Gray)
                        Spacer(modifier = Modifier.width(12.dp))
                        BatchBadge(slot.batch)
                    }

                    // Optional: teacher note / type tag
                    if (slot.classType.isNotBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        ClassTypeBadge(slot.classType, Color(slot.colorHex))
                    }
                }
            }
        }
    }
}

// ─── LIVE Badge ───────────────────────────────────────────────────────────────
@Composable
private fun LiveBadge() {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFFFFF3E0))
            .padding(horizontal = 8.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(LiveGreen)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "LIVE",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFE65100)
        )
    }
}

// ─── Batch Badge ──────────────────────────────────────────────────────────────
@Composable
private fun BatchBadge(batch: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(5.dp))
            .background(Color(0xFFF3E5F5))
            .padding(horizontal = 7.dp, vertical = 2.dp)
    ) {
        Text(text = batch, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = AccentPurple)
    }
}

// ─── Class Type Badge (Lecture / Lab / Seminar) ───────────────────────────────
@Composable
private fun ClassTypeBadge(type: String, color: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(5.dp))
            .background(color.copy(alpha = 0.12f))
            .padding(horizontal = 7.dp, vertical = 2.dp)
    ) {
        Text(text = type, fontSize = 11.sp, color = color, fontWeight = FontWeight.Medium)
    }
}

// ─── Empty State ──────────────────────────────────────────────────────────────
@Composable
private fun EmptyDayView(day: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 64.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Default.Info,
            contentDescription = null,
            tint = Color.LightGray,
            modifier = Modifier.size(56.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No classes on $day",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = Color.Gray
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Enjoy your free time! 🎉",
            fontSize = 14.sp,
            color = Color.LightGray,
            textAlign = TextAlign.Center
        )
    }
}
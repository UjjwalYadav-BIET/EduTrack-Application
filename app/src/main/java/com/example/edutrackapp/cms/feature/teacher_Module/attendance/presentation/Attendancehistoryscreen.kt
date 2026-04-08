package com.example.edutrackapp.cms.feature.teacher_Module.attendance.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController

// ─── Design tokens ─────────────────────────────────────────────────────────────
private val DarkNavy     = Color(0xFF1B2438)
private val BgLight      = Color(0xFFF5F6FA)
private val CardWhite    = Color(0xFFFFFFFF)
private val AccentYellow = Color(0xFFFFB800)
private val GreenPresent = Color(0xFF2ECC71)
private val RedAbsent    = Color(0xFFE74C3C)
private val TextDark     = Color(0xFF1A1A2E)
private val TextGray     = Color(0xFF8A8A9A)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceHistoryScreen(
    navController: NavController,
    viewModel: AttendanceViewModel = hiltViewModel()
) {
    val history = viewModel.attendanceHistory

    Scaffold(
        containerColor = BgLight,
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkNavy)
            ) {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                "Attendance History",
                                fontWeight = FontWeight.Bold,
                                fontSize   = 18.sp,
                                color      = Color.White
                            )
                            Text(
                                "CS-A  •  ${history.size} sessions",
                                fontSize = 11.sp,
                                color    = Color.White.copy(alpha = 0.55f)
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Default.ArrowBack, null, tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            }
        }
    ) { paddingValues ->

        if (history.isEmpty()) {
            // ── Empty state ───────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(BgLight),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .background(DarkNavy.copy(0.07f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.History,
                            null,
                            modifier = Modifier.size(36.dp),
                            tint     = DarkNavy.copy(0.3f)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "No sessions recorded yet",
                        fontWeight = FontWeight.SemiBold,
                        fontSize   = 16.sp,
                        color      = TextDark
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        "Submit attendance to see history here",
                        fontSize = 13.sp,
                        color    = TextGray
                    )
                }
            }
        } else {
            LazyColumn(
                modifier        = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding  = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Section header
                item {
                    Row(
                        modifier          = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 2.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .width(4.dp)
                                .height(18.dp)
                                .background(AccentYellow, RoundedCornerShape(2.dp))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Recent Sessions",
                            fontWeight = FontWeight.Bold,
                            fontSize   = 15.sp,
                            color      = TextDark
                        )
                    }
                }

                itemsIndexed(history) { index, record ->
                    AttendanceHistoryCard(record = record, index = index)
                }

                item { Spacer(modifier = Modifier.height(24.dp)) }
            }
        }
    }
}

// ─── History Card ──────────────────────────────────────────────────────────────
@Composable
fun AttendanceHistoryCard(record: AttendanceRecord, index: Int) {
    val attendancePct = if (record.totalCount > 0)
        (record.presentCount * 100f / record.totalCount).toInt() else 0

    val pctColor = when {
        attendancePct >= 75 -> GreenPresent
        attendancePct >= 50 -> AccentYellow
        else                -> RedAbsent
    }

    Card(
        modifier  = Modifier.fillMaxWidth(),
        colors    = CardDefaults.cardColors(containerColor = CardWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape     = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier          = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left accent bar
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(88.dp)
                    .background(
                        pctColor,
                        RoundedCornerShape(topStart = 14.dp, bottomStart = 14.dp)
                    )
            )

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f).padding(vertical = 16.dp)) {
                // Date + Time
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.CalendarToday,
                        null,
                        tint     = AccentYellow,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text       = record.date,
                        fontWeight = FontWeight.SemiBold,
                        fontSize   = 14.sp,
                        color      = TextDark
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Icon(
                        Icons.Default.AccessTime,
                        null,
                        tint     = TextGray,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text     = record.time,
                        fontSize = 12.sp,
                        color    = TextGray
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text     = "${record.className}  •  Session ${record.id.takeLast(4)}",
                    fontSize = 12.sp,
                    color    = TextGray
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Mini stats
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    MiniStat("Present", record.presentCount, GreenPresent)
                    MiniStat("Absent",  record.totalCount - record.presentCount, RedAbsent)
                    MiniStat("Total",   record.totalCount,  DarkNavy)
                }
            }

            // Percentage badge
            Column(
                modifier            = Modifier.padding(end = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier         = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(pctColor.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text       = "$attendancePct%",
                        fontWeight = FontWeight.Bold,
                        fontSize   = 13.sp,
                        color      = pctColor
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text("Rate", fontSize = 10.sp, color = TextGray)
            }
        }
    }
}

// ─── Mini Stat ─────────────────────────────────────────────────────────────────
@Composable
fun MiniStat(label: String, count: Int, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text       = "$count $label",
            fontSize   = 11.sp,
            color      = TextGray,
            fontWeight = FontWeight.Medium
        )
    }
}